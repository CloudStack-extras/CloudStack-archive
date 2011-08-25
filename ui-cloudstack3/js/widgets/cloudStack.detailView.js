(function($) {
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

  /**
   * Available UI actions to perform for buttons
   */
  var uiActions = {
    /**
     * Default behavior for actions -- just show a confirmation popup and add notification
     */
    standard: function($detailView, args, additional) {
      var action = args.actions[args.actionName];
      var notification = action.notification;
      var messages = action.messages;
      var messageArgs = { name: $detailView.find('tr.name td.value').html() };

      notification.desc = messages.notification(messageArgs);

      var performAction = function() {
        action.action({
          response: {
            success: function(args) {
              cloudStack.dialog.notice({
                message: messages.success(messageArgs) + ' ' +
                  '<br />Please see the notifications section for status.'
              });

              if (additional && additional.success) additional.success(args);
              addNotification(notification, function() {
                if ($detailView.is(':visible')) {
                  cloudStack.dialog.notice({ message: messages.complete(messageArgs) });
                }

                if (additional && additional.complete) additional.complete(args);
              });
            },
            error: function(data) {
              if (data.message)
                alert(data.message);
            }
          }
        });        
      };

      if (!action.createForm)
        cloudStack.dialog.confirm({
          message: messages.confirm(messageArgs),
          action: performAction
        });
      else
        cloudStack.dialog.createForm({
          form: action.createForm,
          after: performAction
        });
    },

    /**
     * Convert editable fields to text boxes; clicking again saves data
     *
     * @param $detailView
     * @param callback
     */
    edit: function($detailView, args) {
      // Convert value TDs
      var $inputs = $detailView.find('input[type=text], select');

      if ($inputs.size()) {
        $inputs.animate({ opacity: 0.5 }, 500);
        args.actions[args.actionName].action({
          response: {
            success: function(data) {
              // Save and turn back into labels
              $inputs.each(function() {
                var $input = $(this);
                var $value = $input.closest('td.value');

                if ($input.is('input[type=text]'))
                  $value.html(
                    $input.attr('value')
                  );
                else if ($input.is('select')) {
                  $value.html(
                    $input.find('option:selected').html()
                  );
                  $value.data('detail-view-selected-option', $input.find('option:selected').val());
                }                  
              });

              addNotification(
                { desc: 'Renamed VM' },
                function(data) {
                  
                },
                []
              );
            },
            error: function(data) {
              // Put in original values on error
              $inputs.each(function() {
                var $input = $(this);
                var $value = $input.closest('td.value');
                var originalValue = $input.data('original-value');

                $value.html(originalValue);
              });
            }
          }
        });

        return $detailView;
      }

      $detailView.find('td.value').each(function() {
        var $value = $(this);
        if (!$value.data('detail-view-is-editable')) return true;

        // Turn into form field
        var selectData = $value.data('detail-view-editable-select');
        if (selectData) {
          // Select
          var data = $value.html();
          $value.html('');
          $value.append(
            $('<select>')
              .attr({
                type: 'text',
                value: data
              })
              .data('original-value', data)
          );

          // Make option values from given array
          $(selectData).each(function() {
            $('<option>')
              .attr({
                value: this.id
              })
              .html(this.description)
              .appendTo($value.find('select'));
          });

          $value.find('select').val($value.data('detail-view-selected-option'));
        } else {
          // Text input
          var data = $value.html();
          $value.html('');
          $value.append(
            $('<input>').attr({
              type: 'text',
              value: data
            }).data('original-value', data)
          );
        }

        return true;
      });

      return $detailView;
    },

    /**
     * Removes detail view and instance from the list, on success
     */
    destroy: function($detailView, args) {
      var $listView = $detailView.data('list-view');

      uiActions.standard($detailView, args, {
        complete: function(args) {
          if (!$listView.is(':visible')) return false;

          $('#browser .container').cloudBrowser('selectPanel', {
            panel: $listView.closest('div.panel')
          });

          var $dataTable = $listView.find('div.data-table');
          $dataTable.dataTable('removeRow', $dataTable.find('tr').filter(function() {
            return $(this).hasClass('selected');
          }).index());

          return true;
        }
      });
    }
  };

  var viewAll = function(viewAllID) {
    var $detailView = $('div.detail-view');
    var args = $detailView.data('detail-view-args');
    var cloudStackArgs = $('[cloudstack-container]').data('cloudStack-args');
    var $browser = args.$browser;
    var listViewArgs, viewAllPath;

    // Get path in cloudStack args
    viewAllPath = viewAllID.split('.');

    if (viewAllPath.length == 2)
      listViewArgs = cloudStackArgs.sections[viewAllPath[0]].sections[viewAllPath[1]];
    else
      listViewArgs = cloudStackArgs.sections[viewAllPath[0]];

    // Make panel
    var $panel = $browser.cloudBrowser('addPanel', {
      title: listViewArgs.title,
      data: '',
      noSelectPanel: true,
      maximizeIfSelected: true
    });
    $browser.cloudBrowser('toggleMaximizePanel', {
      panel: $panel
    });

    // Make list view
    listViewArgs.$browser = $browser;
    $panel.listView(listViewArgs);
  };

  /**
   * Make action button elements
   *
   * @param actions {object} Actions to generate
   */
  var makeActionButtons = function(actions) {
    var $actions = $('<td>').addClass('detail-actions').append(
      $('<div>').addClass('buttons')
    );

    if (actions) {
      $.each(actions, function(key, value) {
        var $action = $('<div></div>')
              .addClass('action').addClass(key)
              .appendTo($actions.find('div.buttons'));
        var $actionLink = $('<a></a>')
              .attr({
                href: '#',
                title: value.label,
                alt: value.label,
                'detail-action': key
              })
              .data('detail-view-action-callback', value.action)
              .append(
                $('<span>').addClass('icon').html('&nbsp;')
              )
              .appendTo($action);
      });

      var $actionButtons = $actions.find('div.action');
      if ($actionButtons.size() == 1)
        $actionButtons.addClass('single');
      else {
        $actionButtons.filter(':first').addClass('first');
        $actionButtons.filter(':last').addClass('last');
      }
    }

    return $('<div>')
      .addClass('detail-group actions')
      .append(
        $('<table>').append(
          $('<tbody>').append(
            $('<tr>').append($actions)
          )
        )
      );
  };

  /**
   * Generate attribute field rows in tab
   */
  makeFieldContent = function(tabData, $detailView, data, args) {
    if (!args) args = {};

    var $detailGroups = $('<div>').addClass('details');
    var isOddRow = false; // Even/odd row coloring
    var $header;
    var detailViewArgs = $detailView.data('detail-view-args');

    // Make header
    if (args.header) {
      $detailGroups.addClass('group-multiple');
      $header = $('<table>').addClass('header').appendTo($detailGroups);
      $header.append($('<thead>').append($('<tr>')));
      $header.find('tr').append($('<th>'));
    }

    $(tabData.fields).each(function() {
      var fieldGroup = this;

      var $detailTable = $('<tbody></tbody>').appendTo(
        $('<table></table>').appendTo(
          $('<div></div>').addClass('detail-group').appendTo($detailGroups)
        ));

      $.each(fieldGroup, function(key, value) {
        if ($header && key == args.header) {
          $header.find('th').html(data[key]);
          return true;
        }

        var $detail = $('<tr></tr>').addClass(key).appendTo($detailTable);
        var $name = $('<td></td>').addClass('name').appendTo($detail);
        var $value = $('<td></td>').addClass('value').appendTo($detail);

        // Even/odd row coloring
        if (isOddRow && key != 'name') {
          $detail.addClass('odd');
          isOddRow = false;
        } else if (key != 'name') {
          isOddRow = true;
        }

        // Generate action buttons, if present on prop
        if (value.actions) {
          makeActionButtons(value.actions).appendTo($detail);
        }

        $name.html(value.label);
        $value.html(data[key]);

        // Set up editable metadata
        $value.data('detail-view-is-editable', value.isEditable);
        if (value.select) {
          value.selected = $value.html();

          // Get matching select data
          var matchedSelectValue = $.grep(value.select, function(option, index) {
            return option.id == value.selected;
          })[0];

          if (!matchedSelectValue) {
            $value.data('detail-view-is-editable', false);
            return true;
          }
            
          $value.html(matchedSelectValue.description);
          $value.data('detail-view-selected-option', matchedSelectValue.id);
          $value.data('detail-view-editable-select', value.select);
        }

        return true;
      });
    });

    if (args.isFirstPanel) {
      var $firstRow = $detailGroups.filter(':first').find('div.detail-group:first table tr:first');
      var $actions;
      var actions = detailViewArgs.actions;

      // Detail view actions
      $actions = makeActionButtons(detailViewArgs.actions).prependTo($firstRow.closest('div.detail-group'));

      // 'View all' button
      if (detailViewArgs.viewAll) {
        $('<a>')
          .attr({ href: '#' })
          .data('detail-view-link-view-all', detailViewArgs.viewAll)
          .append(
            $('<span>').html('View ' + detailViewArgs.viewAll.label)
          )
          .appendTo(
            $('<td>')
              .addClass('view-all')
              .appendTo($actions.find('tr'))
          );
      }
    }

    return $detailGroups;
  };

  /**
   * Load field data for specific tab from data provider
   *
   * @param $tabContent {jQuery} tab div to load content into
   * @param args {object} Detail view data
   */
  var loadTabContent = function($tabContent, args) {
    $tabContent.html('');
    var targetTabID = $tabContent.data('detail-view-tab-id');
    var tabs = args.tabs[targetTabID];
    var dataProvider = tabs.dataProvider;
    var isMultiple = tabs.multiple;
    var viewAll = args.viewAll;

    return dataProvider({
      tab: targetTabID,
      id: args.id,
      response: {
        success: function(args) {
          var tabData = $tabContent.data('detail-view-tab-data');
          var data = args.data;
          var isFirstPanel = $tabContent.index($tabContent.parent().find('div.detail-group.ui-tabs-panel')) == 0;

          if (isMultiple) {
            $(data).each(function() {
              makeFieldContent(tabs, $tabContent.closest('div.detail-view'), this, { header: 'name', isFirstPanel: isFirstPanel }).appendTo($tabContent);
            });

            return true;
          }

          makeFieldContent(tabs, $tabContent.closest('div.detail-view'), data, { isFirstPanel: isFirstPanel }).appendTo($tabContent);

          return true;
        },
        error: function() {
          alert('error!');
        }
      }
    });
  };

  $.fn.detailView = function(args) {
    var $detailView = this;

    $detailView.addClass('detail-view');
    $detailView.data('detail-view-args', args);

    // Create toolbar
    var $toolbar = $('<div class="toolbar">').appendTo($detailView);
    var $tabs = $('<ul></ul>').appendTo($detailView);

    // Make tabs
    $.each(args.tabs, function(key, value) {
      var propGroup = key;
      var prop = value;
      var title = prop.title;
      var $tab = $('<li></li>').attr('detail-view-tab', true).appendTo($tabs);

      var $tabLink = $('<a></a>').attr({
        href: '#details-tab-' + propGroup
      }).html(title).appendTo($tab);

      var $tabContent = $('<div></div>').attr({
        id: 'details-tab-' + propGroup
      }).addClass('detail-group').appendTo($detailView);

      $tabContent.data('detail-view-tab-id', key);
      $tabContent.data('detail-view-tab-data', value);
    });

    $detailView.tabs();

    return $detailView;
  };

  // Setup tab events
  $(document).bind('tabsshow', function(event, ui) {
    var $target = $(event.target);

    if (!$target.hasClass('detail-view') || $target.hasClass('detail-view ui-state-active')) return true;

    var $targetDetailGroup = $(ui.panel);
    loadTabContent($targetDetailGroup, $target.data('detail-view-args'));

    return true;
  });

  // View all links
  $('a').live('click', function(event) {
    var $target = $(event.target);

    if ($target.closest('div.detail-view').size() && $target.closest('td.view-all a').size()) {
      var $viewAll = $target.closest('td.view-all a');
      viewAll($viewAll.data('detail-view-link-view-all').path);
      return false;
    }

    return true;
  });

  // Setup action button events
  $(document).bind('click', function(event) {
    var $target = $(event.target);

    if ($target.closest('div.detail-view div.action a[detail-action]').size()) {
      var $action = $target.closest('div.detail-view div.action a[detail-action]');
      var actionName = $action.attr('detail-action');
      var actionCallback = $action.data('detail-view-action-callback');
      var detailViewArgs = $action.closest('div.detail-view').data('detail-view-args');
      var uiCallback = uiActions[actionName];

      if (!uiCallback)
        uiCallback = uiActions['standard'];

      detailViewArgs.actionName = actionName;
      uiCallback($target.closest('div.detail-view'), detailViewArgs);

      return false;
    }

    return true;
  });
}(jQuery));
