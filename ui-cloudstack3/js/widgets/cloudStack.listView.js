/**
 * Create dynamic list view based on data callbacks
 */
(function($, cloudStack) {
  /**
   * Add 'pending' notification
   */
  var addNotification = function(notification, success, successArgs) {
    if (!notification) {
      success(successArgs);

      return false;
    };

    var $notifications = $('div.notifications');

    if (!notification.poll) {
      $notifications.notifications('add', {
        desc: notification.desc,
        interval: 0,
        poll: function(args) { success(successArgs); args.complete(); }
      });
    } else {
      $notifications.notifications('add', {
        desc: notification.desc,
        interval: 1000,
        poll: function(args) {
          var complete = args.complete;

          notification.poll({
            complete: function() {
              success(successArgs);
              complete();
            }
          });
        }
      });
    }

    return true;
  };

  var uiActions = {
    standard: function($instanceRow, args, additional) {
      var notification = args.action.notification;
      var messages = args.action.messages;
      var messageArgs = { name: $instanceRow.find('td.name span').html() };

      notification.desc = messages.notification(messageArgs);

      var performAction = function() {
        args.action.action({
          response: {
            success: function(args) {
              if (additional && additional.success) additional.success(args);
              addNotification(notification, function() {
                if ($instanceRow.is(':visible')) {
                  cloudStack.dialog.notice({ message: messages.complete(messageArgs) });

                  if (additional && additional.complete) additional.complete(args);
                }
              });
            },
            error: function(data){
              if (data.message)
                alert(data.message);
            }
          }
        });
      };

      if (args.action.wizard) {
        alert('Add instance wizard goes here');
      } else if (!args.action.createForm)
        cloudStack.dialog.confirm({
          message: messages.confirm(messageArgs),
          action: performAction
        });
      else
        cloudStack.dialog.createForm({
          form: args.action.createForm,
          after: performAction
        });
    },

    destroy: function($instanceRow, args) {
      uiActions.standard($instanceRow, args, {
        complete: function(args) {
          $instanceRow.animate({ opacity: 0.5 });
          $instanceRow.find('td.actions').children().remove();
        }
      });
    },
    edit: function($instanceRow, args) {
      var $td = $instanceRow.find('td.editable');
      var $edit = $td.find('div.edit');
      var $editInput = $edit.find('input');
      var $label = $td.find('span');

      // Hide label, show edit field
      var showEditField = function() {
        $edit.css({ opacity: 1 });
        $label.fadeOut('fast', function() {
          $edit.fadeIn();
          $editInput.focus();
          $instanceRow.closest('div.data-table').dataTable('refresh');
        });
      };

      // Hide edit field, validate and save changes
      var showLabel = function(val) {
        if (val) $label.html(val);

        $edit.hide();
        $label.fadeIn();
        $instanceRow.closest('div.data-table').dataTable('refresh');
      };

      if (args.cancel) {
        showLabel();
        return false;
      }

      if ($label.is(':visible')) {
        showEditField();
      } else if ($editInput.val() != $label.html()) {
        $edit.animate({ opacity: 0.5 });

        var originalName = $label.html();
        var newName = $editInput.val();

        addNotification(
          { desc: 'Renamed ' + originalName + ' to ' + newName },
          function(data) {
            showLabel(newName);
          },
          [{ name: newName }]
        );
      } else {
        showLabel();
      }

      return $instanceRow;
    }
  };

  /**
   * Edit field text
   *
   * @param $td {jQuery} <td> to put input field into
   */
  var createEditField = function($td) {
    $td.addClass('editable');

    // Put <td> label into a span
    var value = $td.html();
    $('<span></span>').html(value).appendTo($td.html(''));

    var $editArea = $('<div></div>').addClass('edit');
    var $editField = $('<input />').addClass('edit').attr({
      type: 'text',
      value: value
    });
    var $actionButton = $('<div></div>').addClass('action');
    var $saveButton = $actionButton.clone().addClass('save').attr({
      'title': 'Save'
    });
    var $cancelButton = $actionButton.clone().addClass('cancel').attr({
      'title': 'Cancel edit'
    });

    $([$editField, $saveButton, $cancelButton]).each(function() {
      this.appendTo($editArea);
    });

    return $editArea.hide();
  };

  var createHeader = function(fields, $table, actions) {
    var $thead = $('<thead>').appendTo($table);

    $.each(fields, function(key) {
      var field = this;
      var $th = $('<th>').appendTo($thead);

      if ($th.index()) $th.addClass('reduced-hide');

      $th.html(field.label);
    });

    if (actions) {
      $thead.append(
        $('<th></th>')
          .html('Actions')
          .addClass('actions reduced-hide')
      );
    }

    return $thead;
  };

  var createFilters = function($toolbar, filters) {
    if (!filters) return false;

    var $filters = $('<div></div>').addClass('filters reduced-hide');
    $filters.append('<label>Filter By: </label>');

    var $filterSelect = $('<select></select>').appendTo($filters);
    $filterSelect.append('<option value="all">All</option>'); // Always appears by default

    if (filters)
      $.each(filters, function(key) {
        var $option = $('<option>').attr({
          value: key
        }).html(this.label);

        $option.appendTo($filterSelect);
      });

    return $filters.appendTo($toolbar);
  };

  var createSearchBar = function($toolbar) {
    var $search = $('<div></div>').addClass('text-search reduced-hide');
    var $searchBar = $('<div></div>').addClass('search-bar reduced hide').appendTo($search);
    $searchBar.append('<input type="text" />');
    $search.append('<div class="button search"></div>');

    return $search.appendTo($toolbar);
  };

  /**
   * Makes set of icons from data, in the for of a table cell
   */
  var makeActionIcons = function($td, actions) {
    $.each(actions, function(key, value) {
      if (key == 'add') return true;
      var actionName = key;
      var action = value;

      $td.append(
        $('<div></div>')
          .addClass('action')
          .addClass(key)
          .attr({
            alt: action.label,
            title: action.label
          })
          .data('list-view-action-id', key)
      );

      return true;
    });
  };

  /**
   * Initialize detail view for specific ID from list view
   */
  var createDetailView = function(args) {
    var $panel = args.$panel;
    var title = args.title;
    var id = args.id;
    var data = $.extend(args.data, { id: id });
    var $detailView, $detailsPanel;
    var panelArgs = {
      title: title,
      data: '<div class="detail-view"></div>',
      parent: $panel
    };

    // Create panel
    $detailsPanel = data.$browser.cloudBrowser('addPanel', panelArgs);

    // Make detail view element
    if (!args.pageGenerator)
      $detailView = $detailsPanel.find('div.detail-view').detailView(data);
    else
      $detailView = args.pageGenerator(data).appendTo($detailsPanel);

    return $detailView;
  };

  var addTableRows = function(fields, data, $tbody, actions) {
    $(data).each(function() {
      var dataItem = this;
      var id = dataItem.id;
      var $tr = $('<tr>').appendTo($tbody);

      $.each(fields, function(key) {
        var field = this;
        var $td = $('<td>').addClass(key).appendTo($tr);
        var content = dataItem[key];

        if (field.id == true) id = field.id;
        if ($td.index()) $td.addClass('reduced-hide');
        if (field.action) {
          $td.data('list-view-action', key);
        }

        $td.html(content);

        if (field.editable) createEditField($td).appendTo($td);
        else {
          var origValue = $td.html();
          $td.html('');
          $td.append(
            $('<span></span>').html(origValue)
          );
        }
      });

      $tr.data('list-view-item-id', id);

      if (actions) {
        makeActionIcons(
          $('<td></td>').addClass('actions reduced-hide')
            .appendTo($tr),
          actions
        );
      }
    });
  };

  var setLoading = function($table, completeFn) {
    var $loading = $('<tr>')
          .appendTo($table.find('tbody'))
          .append(
            $('<td>')
              .addClass('loading')
              .html('Loading...')
              .attr({
                'colspan': $table.find('th').size()
              })
          );

    $('div.list-view').scrollTop($table.height() + 100);

    return completeFn({
      loadingCompleted: function() {
        $loading.remove();
      }
    });
  };

  var loadBody = function($table, dataProvider, fields, append, loadArgs, actions) {
    var $tbody = $table.find('tbody');
    if (!loadArgs) loadArgs = {
      page: 1,
      filterBy: {
        search: {},
        kind: 'all',
        page: 1
      }
    };

    if (!append) {
      if (!append) $table.find('tbody tr').remove();
    }

    setLoading($table, function(setLoadingArgs) {
      $table.dataTable();
      $.extend(loadArgs, {
        response: {
          success: function(args) {
            setLoadingArgs.loadingCompleted();
            addTableRows(fields, args.data, $tbody, actions);
            $table.dataTable();
          },
          error: function() {
            alert('error');
          }
        }
      });
    });

    return dataProvider(loadArgs);
  };

  /**
   * Make 'switcher' buttons for sections
   */
  var createSectionSwitcher = function(args) {
    var sections = args.sections;
    var $switcher = $('<div>').addClass('section-switcher reduced-hide');
    var $sectionSelect = $('<select></select>')
          .append(
            $('<option disabled=\"disabled\">')
          )
          .appendTo(
            $('<div></div>')
              .addClass('section-select')
              .appendTo($switcher)
          );

    if (args.sectionSelect) {
      $('<label>')
        .prependTo($sectionSelect.parent())
        .html(args.sectionSelect.label + ':');
    } else {
      $sectionSelect.hide();
    }

    $.each(sections, function(key) {
      var $sectionButton;

      if (!this.type || this.type == 'button') {
        $sectionButton = $('<div>')
          .addClass('section')
          .append(
          $('<a>')
            .addClass(key)
            .attr({ href: '#' })
            .data('list-view-section-id', key)
            .html(this.title)
        );

        $sectionButton.appendTo($switcher);
      } else if (this.type == 'select') {
        $sectionSelect.append(
          $('<option></option>')
            .attr('value', key)
            .html(this.title)
        );
      }
    });

    $switcher.find('div.section:first').addClass('first');
    $switcher.find('div.section:last').addClass('last');

    return $switcher;
  };

  /**
   * Generate/reset entire list view elements
   *
   * @param $container Container to place list view inside
   * @param args List view setup data
   * @param section If section, reset list view to specified section
   */
  var makeListView = function($container, args, section) {
    args.activeSection = section;
    
    // Clear out any existing list view
    var $existingListView = $container.find('div.list-view');
    if ($existingListView.size()) {
      $existingListView.remove();
    }

    var listViewData = args.listView;

    if (section) {
      listViewData = args.sections[section].listView;
    }

    // Create table and other elems
    var $listView = $('<div></div>')
          .addClass('view list-view')
          .addClass(listViewData.section);

    var $toolbar = $('<div>').addClass('toolbar').appendTo($listView);
    var $table = $('<table>').appendTo($listView);
    var infScrollTimer;
    var page = 1;
    var actions = listViewData.actions;

    // Add panel controls
    $('<div class="panel-controls">').append($('<div class="control expand">').attr({
      'ui-id': 'toggle-expand-panel'
    })).appendTo($toolbar);
    
    if (listViewData.actions && listViewData.actions.add) {
      $toolbar
        .append(
          $('<div>')
            .addClass('button action add reduced-hide')
            .data('list-view-action-id', 'add')
            .append(
              $('<span>').html(listViewData.actions.add.label)
            )
        );
    }

    $('<tbody>').appendTo($table);

    createHeader(listViewData.fields, $table, actions);

    var $switcher;
    if (args.sections) {
      $switcher = createSectionSwitcher(args);
      if (section) {
        $switcher
          .appendTo($toolbar)
          .find('a.' + section).addClass('active'); 
        $switcher.find('div.section-select select').val(section);
      }
    }

    createFilters($toolbar, listViewData.filters);
    createSearchBar($toolbar);
    loadBody($table, listViewData.dataProvider, listViewData.fields, false, null, actions);

    // Setup item events
    $listView.find('tbody').bind('click', function(event) {
      var $target = $(event.target);
      var listViewAction = $target.data('list-view-action');

      if (!listViewAction) return true;

      listViewData.fields[listViewAction].action();

      return true;
    });

    // Setup filter events
    $listView.find('.button.search, select, input[type=text]').bind('click change', function(event) {
      if ((event.type == 'click' ||
           event.type == 'mouseup') &&
          ($(event.target).is('select') ||
           $(event.target).is('option') ||
           $(event.target).is('input')))
        return true;

      loadBody($table, listViewData.dataProvider, listViewData.fields, false, {
        page: 1,
        filterBy: {
          kind: $listView.find('select').val(),
          search: {
            value: $listView.find('input[type=text]').val(),
            by: 'name'
          }
        }
      }, listViewData.actions);

      return true;
    });

    // Infinite scrolling event
    $listView.bind('scroll', function(event) {
      if ($('td.loading:visible').size()) return false;

      clearTimeout(infScrollTimer);
      infScrollTimer = setTimeout(function() {
        var loadMoreData = $listView.scrollTop() >= ($table.height() - $listView.height()) - $listView.height() / 4;

        if (loadMoreData) {
          page = page + 1;

          loadBody($table, listViewData.dataProvider, listViewData.fields, true, {
            page: page,
            filterBy: {
              search: {},
              kind: 'all'
            }
          }, actions);
        }
      }, 500);

      return true;
    });

    $listView.bind('click change', function(event) {
      var $target = $(event.target);
      var id = $target.closest('tr').data('list-view-item-id');
      var detailViewArgs;
      var detailViewPresent = ($target.closest('div.data-table tr td').size() &&
                               $target.closest('div.data-table tr td').index() == 0 &&
                               listViewData.detailView && !$target.closest('div.edit').size());

      // Click on first item will trigger detail view (if present)
      if (detailViewPresent) {
        listViewData.detailView.$browser = args.$browser;
        detailViewArgs = {
          $panel: $target.closest('div.panel'),
          data: listViewData.detailView,
          title: $target.closest('td').find('span').html(),
          id: id
        };

        // Create custom-generated detail view
        if (listViewData.detailView.pageGenerator) {
          detailViewArgs.pageGenerator = listViewData.detailView.pageGenerator;
        }

        createDetailView(detailViewArgs).data('list-view', $listView);

        return false;
      }

      // Action icons
      if ($target.hasClass('action') && ($target.parent().is('td.actions')) || $target.closest('.action.add').size()) {
        var actionID = $target.closest('.action').data('list-view-action-id');
        var $tr = $target.closest('tr');
        var uiCallback = uiActions[actionID];

        if (!uiCallback)
          uiCallback = uiActions['standard'];

        uiCallback($tr, {
          action: listViewData.actions[actionID]
        });

        return false;
      }

      // Edit field action icons
      if ($target.hasClass('action') && $target.parent().is('div.edit')) {
        uiActions.edit($target.closest('tr'), {
          callback: listViewData.actions.edit.action,
          cancel: $target.hasClass('cancel')
        });
        return false;
      }

      // Section switcher
      if ($target.is('a') && $target.closest('div.section-switcher').size()) {
        makeListView($container, args, $target.data('list-view-section-id'));

        return false;
      }

      if ($target.is('div.section-switcher select') && event.type == 'change') {
        makeListView($container, args, $target.val());

        return false;
      }

      return true;
    });

    return $listView.appendTo($container);
  };

  $.fn.listView = function(args) {
    if (args.sections) {
      var targetSection;
      $.each(args.sections, function(key) {
        targetSection = key;
        return false;
      });
      makeListView(this, args, targetSection);
    } else {
      makeListView(this, args);
    }

    return this;
  };
})(jQuery, cloudStack);
