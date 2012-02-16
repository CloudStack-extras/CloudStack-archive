/**
 * Create dynamic list view based on data callbacks
 */
(function($, cloudStack, _l) {
  var uiActions = {
    standard: function($instanceRow, args, additional) {
      var listViewArgs = $instanceRow.closest('div.list-view').data('view-args');
      var notification = args.action.notification ? args.action.notification : {};
      var messages = args.action ? args.action.messages : {};
      var messageArgs = { name: $instanceRow.find('td.name span').html() };
      var preAction = args.action ? args.action.preAction : {};
      var action = args.action ? args.action.action : {};
      var section;
      var data = {
        id: $instanceRow.data('list-view-item-id'),
        jsonObj: $instanceRow.data('jsonObj')
      };
      var $listView = $instanceRow.closest('.list-view');

      if (args.data) $.extend(true, data, args.data);
      if (listViewArgs) section = listViewArgs.section;

      notification.desc = messages ?
        messages.notification(messageArgs) : null;

      if (listViewArgs)
        notification.section = listViewArgs.id;

      // Handle pre-action (occurs before any other behavior happens)
      if (preAction) {
        var preActionContext = $.extend(true, {}, listViewArgs.context);

        preActionContext[
          listViewArgs.activeSection
        ] = [$instanceRow.data('jsonObj')];

        if (!preAction({ context: preActionContext })) return false;
      }

      var performAction = function(data, options) {
        if (!options) options = {};

        var $form = options.$form;
        var isHeader = options.isHeader;

        $instanceRow = options.$item ? options.$item : $instanceRow;
        var $item = options.$item;
        var context = $.extend(true, {}, listViewArgs.context);
        context[
          listViewArgs.activeSection
        ] = [$instanceRow.data('jsonObj')];

        if (action.custom && !action.noAdd) {
          action.custom({
            data: data,
            ref: options.ref,
            context: context,
            $instanceRow: $instanceRow,
            complete: function(args) {
              args = args ? args : {};

              var $item = args.$item;

              notification.desc = messages.notification(args.messageArgs);
              notification._custom = args._custom;

              cloudStack.ui.notifications.add(
                notification,
                function(args) {
                  if ($item.is(':visible') && !isHeader) {
                    replaceItem(
                      $item,
                      args.data,
                      args.actionFilter ?
                        args.actionFilter : $instanceRow.next().data('list-view-action-filter')
                    );
                  }
                },

                {},

                // Error
                function(args) {
                  $item.remove();
                }
              );
            }
          });
        } else if (action.uiCustom) {
          action.uiCustom({
            $item: $instanceRow
          });
        } else {
          var actionArgs = {
            data: data,
            ref: options.ref,
            context: options.context,
            $form: $form,
            response: {
              success: function(args) {
                args = args ? args : {};

                var $prevRow, $newRow;

                // Make copy of previous row, in case data is needed
                $prevRow = $instanceRow.clone();
                $prevRow.data($instanceRow.data());

                // Set loading appearance
                if (args.data && !isHeader) {
                  $instanceRow = replaceItem(
                    $instanceRow,
                    $.extend($instanceRow.data('json-obj'), args.data),
                    $instanceRow.data('list-view-action-filter')
                  );
                }

                $instanceRow.find('td:last').children().remove();
                $instanceRow.find('td:last').append($('<div>').addClass('loading'));
                $instanceRow.addClass('loading');

                if (options.$item) $instanceRow.data('list-view-new-item', true);

                // Disable any clicking/actions for row
                $instanceRow.bind('click', function() { return false; });

                notification._custom = args._custom;

                if (additional && additional.success) additional.success(args);

                cloudStack.ui.notifications.add(
                  notification,

                  // Success
                  function(args) {
                    if (!args) args = {};

                    var actionFilter = args.actionFilter ?
                      args.actionFilter : $instanceRow.data('list-view-action-filter');

                    if (!isHeader) {
                      if ($instanceRow.is(':visible')) {
                        if (args.data) {
                          $newRow = replaceItem($instanceRow,
                                                $.extend($instanceRow.data('json-obj'), args.data),
                                                actionFilter);
                        }
                        else {
                          // Nothing new, so just put in existing data
                          $newRow = replaceItem($instanceRow,
                                                $instanceRow.data('json-obj'),
                                                actionFilter);
                        }
                      }

                      if (additional && additional.complete)
                        additional.complete(args, $newRow);
                    }

                    if (messages.complete) {
                      cloudStack.dialog.notice({
                        message: messages.complete(args.data)
                      });
                    }

                    if (options.complete) {
                      options.complete(args);
                    }
                  },

                  {},

                  // Error
                  function(args) {
                    if (!isHeader) {
                      if ($instanceRow.data('list-view-new-item')) {
                        // For create forms
                        $instanceRow.remove();
                      } else {
                        // For standard actions
                        replaceItem(
                          $instanceRow,
                          $.extend($instanceRow.data('json-obj'), args.data),
                          args.actionFilter ?
                            args.actionFilter :
                            $instanceRow.data('list-view-action-filter')
                        );
                      }
                    }                 

                    if (options.error) {
                      options.error(args);
                    }
                  }
                );
              },
              error: function(message) {
                if (!isHeader) {
                  if (($.isPlainObject(args.action.createForm) && args.action.addRow != 'false') ||
                      (!args.action.createForm && args.action.addRow == 'true')) {
                    $instanceRow.remove();                  
                  }
                }

                if (options.error) options.error(message);

                if (message) cloudStack.dialog.notice({ message: message });
              }
            }
          };

          if (action.custom && action.noAdd) {
            action.custom({
              data: data,
              ref: options.ref,
              context: context,
              $instanceRow: $instanceRow,
              complete: actionArgs.response.success
            });
          } else {
            action(actionArgs);
          }
        }
      };

      var context = $.extend({}, listViewArgs.context);
      context[
        listViewArgs.activeSection
      ] = [$instanceRow.data('jsonObj')];

      if (!args.action.createForm &&
          args.action.addRow != 'true' &&
          !action.custom && !action.uiCustom)
        cloudStack.dialog.confirm({
          message: messages.confirm(messageArgs),
          action: function() {
            performAction({
              id: $instanceRow.data('list-view-item-id')
            }, {
              context: context
            });
          }
        });
      else if (action.custom || action.uiCustom)
        performAction();
      else {
        var addRow = args.action.addRow == "false" ? false : true;
        var isHeader = args.action.isHeader;
        var createFormContext = $.extend({}, context);

        if (args.action.createForm) {
          cloudStack.dialog.createForm({
            form: args.action.createForm,
            after: function(args) {
              var $newItem;

              if (!isHeader) {
                if (addRow != false) {
                  $newItem = $listView.listView('prependItem', {
                    data: [
                      $.extend(args.data, {
                        state: 'Creating',
                        status: 'Creating',
                        allocationstate: 'Creating'
                      })
                    ]
                  });
                } else {
                  $newItem = $instanceRow;
                }

                performAction(args.data, {
                  ref: args.ref,
                  context: createFormContext,
                  $item: $newItem,
                  $form: args.$form
                });
              } else {
                var $loading = $('<div>').addClass('loading-overlay');

                $loading.appendTo($listView);
                performAction(args.data, {
                  ref: args.ref,
                  context: createFormContext,
                  $form: args.$form,
                  isHeader: isHeader,
                  complete: function(args) {
                    $loading.remove();
                    $listView.listView('refresh');
                  },
                  error: function(args) {
                    $loading.remove();
                  }
                });
              }
            },
            ref: listViewArgs.ref,
            context: createFormContext
          });
        } else {
          cloudStack.dialog.confirm({
            message: messages.confirm(messageArgs),
            action: function() {
              var $newItem;
              if (addRow && !action.isHeader) {
                $newItem = $listView.listView('prependItem', {
                  data: [
                    $.extend(args.data, {
                      state: 'Creating',
                      status: 'Creating',
                      allocationstate: 'Creating'
                    })
                  ]
                });
              } else if (action.isHeader) {
                $newItem = $('<div>');
              } else {
                $newItem = $instanceRow;
              }

              performAction(args.data, {
                ref: args.ref,
                context: createFormContext,
                $item: $newItem,
                $form: args.$form
              });
            }
          });
        }
      }
    },
    edit: function($instanceRow, args) {
      var $td = $instanceRow.find('td.editable');
      var $edit = $td.find('div.edit');
      var $editInput = $edit.find('input');
      var $label = $td.find('span');
      var $listView = $instanceRow.closest('.list-view');
      var listViewArgs = $listView.data('view-args');

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
      var showLabel = function(val, options) {
        if (!options) options = {};

        var oldVal = $label.html();
        $label.html(val);

        var data = {
          id: $instanceRow.data('list-view-item-id'),
          jsonObj: $instanceRow.data('jsonObj')
        };

        data[$td.data('list-view-item-field')] = $editInput.val();

        var context = $.extend({}, listViewArgs.context);
        context[
          listViewArgs.activeSection
        ] = $instanceRow.data('jsonObj');

        args.callback({
          data: data,
          context: context,
          response: {
            success: function(args) {
              $edit.hide();
              $label.fadeIn();
              $instanceRow.closest('div.data-table').dataTable('refresh');

              if (options.success) options.success(args);
            },
            error: function(message) {
              if (message) {
                cloudStack.dialog.notice({ message: message });
                $edit.hide(),
                $label.html(oldVal).fadeIn();
                $instanceRow.closest('div.data-table').dataTable('refresh');

                if (options.error) options.error(args);
              }
            }
          }
        });
      };

      if (args.cancel) {
        showLabel();
        return false;
      }

      if (!$editInput.is(':visible')) {
        showEditField();
      } else if ($editInput.val() != $label.html()) {
        $edit.animate({ opacity: 0.5 });

        var originalName = $label.html();
        var newName = $editInput.val();
        showLabel(newName, {
          success: function() {
            cloudStack.ui.notifications.add(
              {
                section: $instanceRow.closest('div.view').data('view-args').id,
                desc: newName ?
                  _l('Set value of') +
                  ' ' + $instanceRow.find('td.name span').html() +
                  ' ' + _l('to') +
                  ' ' + newName :
                  _l('Unset value for') +
                  ' ' + $instanceRow.find('td.name span').html()
              },
              function(args) {},
              [{ name: newName }]
            );
          }
        });
      } else {
        showLabel();
      }

      return $instanceRow;
    }
  };

  var rowActions = {
    _std: function($tr, action) {
      action();

      $tr.closest('.data-table').dataTable('refresh');

      setTimeout(function() {
        $tr.closest('.data-table').dataTable('selectRow', $tr.index());
      }, 0);
    },

    moveTop: function($tr) {
      rowActions._std($tr, function() {
        $tr.closest('tbody').prepend($tr);
        $tr.closest('.list-view').animate({ scrollTop: 0 });
      });
    },

    moveBottom: function($tr) {
      rowActions._std($tr, function() {
        $tr.closest('tbody').append($tr);
        $tr.closest('.list-view').animate({ scrollTop: 0 });
      });
    },

    moveUp: function($tr) {
      rowActions._std($tr, function() {
        $tr.prev().before($tr);
      });
    },

    moveDown: function($tr) {
      rowActions._std($tr, function() {
        $tr.next().after($tr);
      });
    },

    moveTo: function($tr, index, after) {
      rowActions._std($tr, function() {
        var $target = $tr.closest('tbody').find('tr').filter(function() {
          return $(this).index() == index;
        });

        if ($target.index() > $tr.index()) $target.after($tr);
        else $target.before($tr);

        $tr.closest('.list-view').scrollTop($tr.position().top - $tr.height() * 2);

        if (after)
          setTimeout(function() {
            after();
          });
      });
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
      'title': _l('Save')
    });
    var $cancelButton = $actionButton.clone().addClass('cancel').attr({
      'title': _l('Cancel edit')
    });

    $([$editField, $saveButton, $cancelButton]).each(function() {
      this.appendTo($editArea);
    });

    return $editArea.hide();
  };

  var renderActionCol = function(actions) {
    return $.grep(
      $.map(actions, function(value, key) {
        return key;
      }),
      function(elem) { return elem != 'add'; }
    ).length;
  };

  var createHeader = function(fields, $table, actions, options) {
    if (!options) options = {};

    var $thead = $('<thead>').prependTo($table).append($('<tr>'));
    var reorder = options.reorder;

    $.each(fields, function(key) {
      var field = this;
      var $th = $('<th>').appendTo($thead.find('tr'));

      if ($th.index()) $th.addClass('reduced-hide');

      $th.html(_l(field.label));
    });

    if (reorder) {
      $thead.find('tr').append(
        $('<th>').html(_l('label.order')).addClass('reorder-actions reduced-hide')
      );
    }

    if (actions && renderActionCol(actions)) {
      $thead.find('tr').append(
        $('<th></th>')
          .html(_l('label.actions'))
          .addClass('actions reduced-hide')
      );
    }

    return $thead;
  };

  var createFilters = function($toolbar, filters) {
    if (!filters) return false;

    var $filters = $('<div></div>').addClass('filters reduced-hide');
    $filters.append($('<label>').html(_l('label.filterBy')));

    var $filterSelect = $('<select id="filterBy"></select>').appendTo($filters);

    if (filters)
      $.each(filters, function(key) {			  
				if(this.preFilter != null && this.preFilter() == false) {
				  return true; //skip to next item in each loop
				}				
        var $option = $('<option>').attr({
          value: key
        }).html(_l(this.label));

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
  var makeActionIcons = function($td, actions, options) {
    options = options ? options : {};
    var allowedActions = options.allowedActions;

    $.each(actions, function(actionName, action) {
      if (actionName == 'add' || action.isHeader)
        return true;

      if (action.type == 'radio') {
        $td.append(
          $('<div></div>')
            .addClass('action')
            .addClass(actionName)
            .append(
              $('<input>').attr({
                type: 'radio',
                name: actionName
              })
            )
            .attr({
              alt: _l(action.label),
              title: _l(action.label)
            })
            .data('list-view-action-id', actionName)
        );

        return true;
      } else if (action.type == 'checkbox') {
        $td.append(
          $('<div></div>')
            .addClass('action')
            .addClass(actionName)
            .append(
              $('<input>').attr({
                type: 'checkbox',
                name: actionName
              })
            )
            .attr({
              alt: _l(action.label),
              title: _l(action.label)
            })
            .data('list-view-action-id', actionName)
        );

        return true;
      }

      var $action = $('<div></div>')
            .addClass('action')
            .addClass(actionName)
            .append($('<span>').addClass('icon'))
            .attr({
              alt: _l(action.label),
              title: _l(action.label)
            })
            .data('list-view-action-id', actionName);

      // Disabled appearance/behavior for filtered actions
      if (allowedActions && $.inArray(actionName, allowedActions) == -1) {
        $action.addClass('disabled');
      }

      $td.append($action);

      return true;
    });
  };

  /**
   * Initialize detail view for specific ID from list view
   */
  var createDetailView = function(args, complete, $row) {
    var $panel = args.$panel;
    var title = args.title;
    var id = args.id;
    var data = $.extend(true, {}, args.data, {
      $browser: $('#browser .container'),
      id: id,
      jsonObj: args.jsonObj,
      section: args.section,
      context: args.context,
      $listViewRow: $row
    });

    var $detailView, $detailsPanel;
    var panelArgs = {
      title: title,
      parent: $panel,
      maximizeIfSelected: data.isMaximized,
      complete: function($newPanel) {
        // Make detail view element
        if (!args.pageGenerator && !data.isMaximized)
          $detailView = $('<div>').addClass('detail-view').detailView(data).appendTo($newPanel);
        else if (!args.pageGenerator && data.isMaximized)
          $detailView = $newPanel.detailView(data);
        else
          $detailView = args.pageGenerator(data).appendTo($newPanel);

        if (complete) complete($detailView);

        return $detailView;
      }
    };

    // Create panel
    $detailsPanel = data.$browser.cloudBrowser('addPanel', panelArgs);
  };

  var addTableRows = function(fields, data, $tbody, actions, options) {
    if (!options) options = {};
    var rows = [];
    var reorder = options.reorder;

    if (!data || ($.isArray(data) && !data.length)) {
      if (!$tbody.find('tr').size()) {
        return [
          $('<tr>').addClass('empty').append(
            $('<td>').html(_l('label.no.data'))
          ).appendTo($tbody)
        ];
      }

      return $tbody.find('tr:last').addClass('last');
    }

    $tbody.find('tr.empty').remove();

    $(data).each(function() {
      var dataItem = this;
      var id = dataItem.id;

      var $tr = $('<tr>');
      rows.push($tr);

      if (options.prepend) {
        $tr.prependTo($tbody);
      } else {
        $tr.appendTo($tbody);
      }

      // Add field data
      $.each(fields, function(key) {
        var field = this;
        var $td = $('<td>')
              .addClass(key)
              .data('list-view-item-field', key)
              .appendTo($tr);
        var content = dataItem[key];

        if (field.indicator) {
          $td.addClass('state').addClass(field.indicator[content]);
        }
        if (field.id == true) id = field.id;
        if ($td.index()) $td.addClass('reduced-hide');
        if (field.action) {
          $td.data('list-view-action', key);
        }
        if (field.converter) {
          content = _l(field.converter(content, dataItem));
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

      $tr.find('td:first').addClass('first');

      // Add reorder actions
      if (reorder) {
        var sort = function($tr, action) {
          var $listView = $tr.closest('.list-view');
          var viewArgs = $listView.data('view-args');
          var context = $.extend(
            true, {},
            $tr.closest('.list-view').data('view-args').context
          );
          var rowIndex = $tr.closest('tbody').find('tr').size() - ($tr.index());

          context[viewArgs.activeSection] = $tr.data('json-obj');
          
          action.action({
            context: context,
            index: rowIndex,
            response: {
              success: function(args) {},
              error: function(args) {
                // Move back to previous position
                rowActions.moveTo($tr, rowIndex);
              }
            }
          });
        };

        $('<td>').addClass('actions reorder').appendTo($tr).append(function() {
          var $td = $(this);

          $.each(reorder, function(actionName, action) {
            var fnLabel = {
              moveTop: _l('label.move.to.top'),
              moveUp: _l('label.move.up.row'),
              moveDown: _l('label.move.down.row'),
              moveDrag: _l('label.drag.new.position')
            };

            $('<div>')
              .addClass('action reorder')
              .addClass(actionName)
              .append(
                $('<span>').addClass('icon').html('&nbsp;')
              )
              .attr({
                title: _l(fnLabel[actionName])
              })
              .appendTo($td)
              .click(function() {
                if (actionName == 'moveDrag') return false;

                rowActions[actionName]($tr);
                $tr.closest('tbody').find('tr').each(function() {
                  sort($(this), action);
                });
                $tr.closest('.data-table').dataTable('selectRow', $tr.index());

                return false;
              });
          });
        });

        // Draggable action
        var initDraggable = function($tr) {
          var originalIndex;

          return $tr.closest('tbody').sortable({
            handle: '.action.moveDrag',
            start: function(event, ui) {
              originalIndex = ui.item.index();
            },
            stop: function(event, ui) {
              rowActions._std($tr, function() {});

              $tr.closest('tbody').find('tr').each(function() {
                sort($(this), reorder.moveDrag);
              });
            }
          });
        };

        if (reorder && reorder.moveDrag) {
          initDraggable($tr);
        }
      }

      // Add action data
      $tr.data('list-view-item-id', id);
      $tr.data('jsonObj', dataItem);
      $tr.data('list-view-action-filter', options.actionFilter);

      if (actions && renderActionCol(actions)) {
        var allowedActions = $.map(actions, function(value, key) {
          return key;
        });

        var $listView = $tr.closest('.list-view');
        var isUICustom = $listView.data('view-args') ?
              $tr.closest('.list-view').data('view-args').uiCustom : false;

        if ($.isFunction(options.actionFilter) && !isUICustom) {
          allowedActions = options.actionFilter({
            context: $.extend(true, {}, options.context, {
              actions: allowedActions,
              item: dataItem
            })
          });
        }

        makeActionIcons(
          $('<td></td>').addClass('actions reduced-hide')
            .appendTo($tr),
          actions,
          {
            allowedActions: allowedActions
          }
        );
      }
    });

    return rows;
  };

  var setLoading = function($table, completeFn) {
    var $loading = $('<tr>')
          .addClass('loading')
          .appendTo($table.find('tbody'))
          .append(
            $('<td>')
              .addClass('loading icon')
              .attr({
                'colspan': $table.find('th').size()
              })
          );

    $table.closest('div.list-view').scrollTop($table.height() + 100);

    return completeFn({
      loadingCompleted: function() {
        $loading.remove();
      }
    });
  };

  var loadBody = function($table, dataProvider, fields, append, loadArgs, actions, options) {
    if (!options) options = {};
    var context = options.context;
    var reorder = options.reorder;

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

    var viewArgs = $table.closest('.list-view').data('view-args');
    var uiCustom = viewArgs.listView ? viewArgs.listView.uiCustom : false;

    setLoading($table, function(setLoadingArgs) {
      $table.dataTable();
      $.extend(loadArgs, {
        context: options.context,
        response: {
          success: function(args) {
            setLoadingArgs.loadingCompleted();

            addTableRows(fields, args.data, $tbody, actions, {
              actionFilter: args.actionFilter,
              context: context,
              reorder: reorder
            });
            $table.dataTable(null, { noSelect: uiCustom });

            setTimeout(function() {
              $table.dataTable('refresh');
            });
          },
          error: function(args) {
            setLoadingArgs.loadingCompleted();
            addTableRows(fields, [], $tbody, actions);
            $table.find('td:first').html(_l('ERROR'));
            $table.dataTable(null, { noSelect: uiCustom });
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
          .appendTo(
            $('<div></div>')
              .addClass('section-select')
              .appendTo($switcher)
          );
    var sectionPreFilter;

    if (args.sectionSelect) {
      $('<label>')
        .prependTo($sectionSelect.parent())
        .html(_l(args.sectionSelect.label) + ':');

      sectionPreFilter = args.sectionSelect.preFilter ?
        args.sectionSelect.preFilter({
          context: cloudStack.context
        }) : null;
    } else {
      $sectionSelect.hide();
    }

    // No need to display switcher if only one entry is present
    if (sectionPreFilter && sectionPreFilter.length == 1) {
      $switcher.find('select').hide();
      $switcher.find('label').html(
        _l('label.viewing') + ' ' + _l(sections[sectionPreFilter[0]].title)
      );
    }

    $.each(sections, function(key) {
      if (sectionPreFilter && $.inArray(key, sectionPreFilter) == -1) {
        return true;
      }

      var $sectionButton;

      if (!this.type || this.type == 'button') {
        $sectionButton = $('<div>')
          .addClass('section')
          .append(
            $('<a>')
              .addClass(key)
              .attr({ href: '#' })
              .data('list-view-section-id', key)
              .html(_l(this.title))
          );

        $sectionButton.appendTo($switcher);
      } else if (this.type == 'select') {
        $sectionSelect.append(
          $('<option></option>')
            .attr('value', key)
            .html(_l(this.title))
        );
      }

      return true;
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
    args.activeSection = section ? section : (
      args.listView.id ? args.listView.id : args.id
    );

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

    $listView.data('view-args', args);

    var $toolbar = $('<div>').addClass('toolbar').appendTo($listView);
    var $table = $('<table>').appendTo($listView);
    var infScrollTimer;
    var page = 1;
    var actions = listViewData.actions;
    var reorder = listViewData.reorder;

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

    if ($switcher && $switcher.find('option').size() == 1) {
      listViewData = args.sections[
        $switcher.find('select').val()
      ].listView;

      args.activeSection = listViewData.id;
    }

    if (listViewData.hideToolbar) {
      $toolbar.hide();
    }

    // Add panel controls
    $('<div class="panel-controls">').append($('<div class="control expand">').attr({
      'ui-id': 'toggle-expand-panel'
    })).appendTo($toolbar);

    if (listViewData.actions && listViewData.actions.add) {
      var showAdd = listViewData.actions.add.preFilter ?
        listViewData.actions.add.preFilter({
          context: listViewData.context ?
            listViewData.context : cloudStack.context
        }) : true;
      
      if (showAdd) {
        $toolbar
          .append(
            $('<div>')
              .addClass('button action add reduced-hide')
              .data('list-view-action-id', 'add')
              .append(
                $('<span>').html(_l(listViewData.actions.add.label))
              )
          ); 
      }
    }

    // List view header actions
    if (listViewData.actions) {
      $.each(listViewData.actions, function(actionName, action) {
        if (!action.isHeader || (
          action.preFilter && !action.preFilter({
            context: listViewData.context ? listViewData.context : cloudStack.context
          })
        )) return true;

        $toolbar
          .append(
            $('<div>')
              .addClass('button action main-action reduced-hide').addClass(actionName)
              .data('list-view-action-id', actionName)
              .append($('<span>').addClass('icon'))
              .append($('<span>').html(_l(action.label)))
          );

        return true;
      });
    }

    $('<tbody>').appendTo($table);

    createHeader(listViewData.fields,
                 $table,
                 listViewData.actions,
                 { reorder: reorder });
    createFilters($toolbar, listViewData.filters);
    createSearchBar($toolbar);

    loadBody(
      $table,
      listViewData.dataProvider,
      listViewData.fields,
      false,
      {
        page: page,
        filterBy: {
          kind: $listView.find('select[id=filterBy]').val(),
          search: {
            value: $listView.find('input[type=text]').val(),
            by: 'name'
          }
        },
        ref: args.ref
      },
      listViewData.actions,
      {
        context: args.context,
        reorder: reorder
      }
    );

    // Keyboard events
    $listView.bind('keypress', function(event) {
      var code = (event.keyCode ? event.keyCode : event.which);
      var $input = $listView.find('input:focus');

      if ($input.size() && $input.hasClass('edit') && code === 13) {
        uiActions.edit($input.closest('tr'), {
          callback: listViewData.actions.edit.action
        });
      }
    });

    // Setup item events
    $listView.find('tbody').bind('click', function(event) {
      var $target = $(event.target);
      var listViewAction = $target.data('list-view-action');

      if (!listViewAction) return true;

      listViewData.fields[listViewAction].action();

      return true;
    });

    var search = function() {
      loadBody(
        $table,
        listViewData.dataProvider,
        listViewData.fields,
        false,
        {
          page: 1,
          filterBy: {
            kind: $listView.find('select[id=filterBy]').val(),
            search: {
              value: $listView.find('input[type=text]').val(),
              by: 'name'
            }
          }
        },
        listViewData.actions,
        {
          context: $listView.data('view-args').context
        }
      );
    };

    $listView.find('.search-bar input[type=text]').change(function(event) {
      search();
    });

    // Setup filter events
    $listView.find('.button.search, select').bind('change', function(event) {
      if ($(event.target).closest('.section-select').size()) return true;
      if ((event.type == 'click' ||
           event.type == 'mouseup') &&
          ($(event.target).is('select') ||
           $(event.target).is('option') ||
           $(event.target).is('input')))
        return true;

      search();

      return true;
    });

    // Infinite scrolling event
    $listView.bind('scroll', function(event) {
      if (args.listView && args.listView.disableInfiniteScrolling) return false;
      if ($listView.find('tr.last, td.loading:visible').size()) return false;

      clearTimeout(infScrollTimer);
      infScrollTimer = setTimeout(function() {
        var loadMoreData = $listView.scrollTop() >= ($table.height() - $listView.height()) - $listView.height() / 4;
        var context = $listView.data('view-args').context;

        if (loadMoreData) {
          page = page + 1;

          loadBody($table, listViewData.dataProvider, listViewData.fields, true, {
            context: context,
            page: page,
            filterBy: {
              search: {},
              kind: 'all'
            }
          }, actions, {
            reorder: listViewData.reorder
          });
        }
      }, 500);

      return true;
    });

    // Action events
    $(window).bind('cloudstack.view-item-action', function(event, data) {
      var actionName = data.actionName;
      var $tr = $listView.find('tr').filter(function() {
        return $(this).data('list-view-item-id') == data.id;
      });

      if (actionName == 'destroy') {
        $tr.animate({ opacity: 0.5 });
        $tr.bind('click', function() { return false; });
      }
    });

    $listView.bind('click change', function(event) {
      var $target = $(event.target);
      var id = $target.closest('tr').data('list-view-item-id');
      var jsonObj = $target.closest('tr').data('jsonObj');
      var detailViewArgs;
      var detailViewPresent = ($target.closest('div.data-table tr td').size() &&
                               $target.closest('div.data-table tr td').index() == 0 &&
                               listViewData.detailView && !$target.closest('div.edit').size());
      var uiCustom = args.uiCustom == true ? true : false;

      // Click on first item will trigger detail view (if present)
      if (detailViewPresent && !uiCustom && !$target.closest('.empty, .loading').size()) {
        listViewData.detailView.$browser = args.$browser;
        detailViewArgs = {
          $panel: $target.closest('div.panel'),
          data: listViewData.detailView,
          title: $target.closest('td').find('span').html(),
          id: id,
          jsonObj: jsonObj,
          ref: jsonObj,
          context: $.extend(true, {}, $listView.data('view-args').context)
        };

        // Populate context object w/ instance data
        var listViewActiveSection = $listView.data('view-args').activeSection;

        // Create custom-generated detail view
        if (listViewData.detailView.pageGenerator) {
          detailViewArgs.pageGenerator = listViewData.detailView.pageGenerator;
        }

        var listViewArgs = $listView.data('view-args');

        detailViewArgs.section = listViewArgs.activeSection ?
          listViewArgs.activeSection : listViewArgs.id;

        detailViewArgs.context[
          listViewActiveSection != '_zone' ?
            listViewActiveSection : detailViewArgs.section
        ] = [jsonObj];

        if ($.isFunction(detailViewArgs.data)) {
          detailViewArgs.data = detailViewArgs.data({
            context: detailViewArgs.context
          });
        }

        createDetailView(detailViewArgs, function($detailView) {
          $detailView.data('list-view', $listView);
        }, $target.closest('tr'));

        return false;
      }

      // Action icons
      if (!$target.closest('td.actions').hasClass('reorder') &&
          ($target.closest('td.actions').size() ||
           $target.closest('.action.add').size() ||
           $target.closest('.action.main-action').size())) {
        var actionID = $target.closest('.action').data('list-view-action-id');
        var $tr;

        if ($target.closest('.action').is('.disabled')) {
          return false;
        }

        if ($target.closest('.action.add').size() ||
            $target.closest('.action.main-action').size()) {
          $tr = $target.closest('div.list-view').find('tr:first'); // Dummy row
        } else {
          $tr = $target.closest('tr');
        }

        var uiCallback = uiActions[actionID];

        if (!uiCallback)
          uiCallback = uiActions['standard'];

        uiCallback($tr, {
          action: listViewData.actions[actionID]
        });

        return true;
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

  var prependItem = function(listView, data, actionFilter, options) {
    if (!options) options = {};

    var viewArgs = listView.data('view-args');
    var listViewArgs = viewArgs.listView ? viewArgs.listView : viewArgs;
    var targetArgs = listViewArgs.activeSection ? listViewArgs.sections[
      listViewArgs.activeSection
    ].listView : listViewArgs;
    var reorder = targetArgs.reorder;

    var $tr = addTableRows(
      targetArgs.fields,
      data,
      listView.find('table tbody'),
      targetArgs.actions,
      {
        prepend: true,
        actionFilter: actionFilter,
        reorder: reorder
      }
    )[0];
    listView.find('table').dataTable('refresh');

    $tr.addClass('loading').find('td:last').prepend($('<div>').addClass('loading'));
    $tr.find('.action').remove();

    return $tr;
  };

  var replaceItem = function($row, data, actionFilter, after) {
    var $newRow;
    var $listView = $row.closest('.list-view');
    var viewArgs = $listView.data('view-args');
    var listViewArgs = viewArgs.listView ? viewArgs.listView : viewArgs;
    var targetArgs = listViewArgs.activeSection ? listViewArgs.sections[
      listViewArgs.activeSection
    ].listView : listViewArgs;
    var reorder = targetArgs.reorder;
    var $table = $row.closest('table');
    var defaultActionFilter = $row.data('list-view-action-filter');

    $newRow = addTableRows(
      targetArgs.fields,
      data,
      $listView.find('table tbody'),
      targetArgs.actions,
      {
        actionFilter: actionFilter ? actionFilter : defaultActionFilter,
        reorder: reorder
      }
    )[0];

    $newRow.data('json-obj', data);
    $row.replaceWith($newRow);
    $table.dataTable('refresh');

    if (after) after($newRow);

    return $newRow;
  };

  $.fn.listView = function(args, options) {
    if (!options) options = {};
    if (args == 'prependItem') {
      return prependItem(this, options.data, options.actionFilter);
    } else if (args =='replaceItem') {
      replaceItem(options.$row, options.data, options.actionFilter, options.after);
    } else if (args.sections) {
      var targetSection;
      $.each(args.sections, function(key) {
        targetSection = key;
        return false;
      });
      makeListView(this, $.extend(true, {}, args, { context: options.context }), targetSection);
    } else if (args == 'refresh') {
      var activeSection = this.data('view-args').activeSection;
      var listViewArgs = this.data('view-args').sections ?
            this.data('view-args').sections[activeSection].listView :
            this.data('view-args').listView;

      loadBody(
        this.find('table:last'),
        listViewArgs.dataProvider,
        listViewArgs.fields,
        false,
        null,
        listViewArgs.actions,
        {
          context: this.data('view-args').context
        }
      );
    } else {
      makeListView(
        this,
        $.extend(true, {}, args, {
          context: options.context ? options.context : cloudStack.context
        }));
    }

    return this;
  };

  // List view refresh handler
  $(window).bind('cloudStack.fullRefresh', function() {
    var $listViews = $('.list-view');

    $listViews.each(function() {
      var $listView = $(this);

      $listView.listView('refresh');
    });
  });
})(jQuery, cloudStack, _l);
