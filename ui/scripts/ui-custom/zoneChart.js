(function($, cloudStack) {
  /**
   * Make system zone 'org' chart
   */
  cloudStack.zoneChart = function(args) {
    return function(listViewArgs) {
      var naas = cloudStack.sections.system.naas;
      var $browser = listViewArgs.$browser;
      var $charts = $('<div>').addClass('system-charts');
      var context = listViewArgs.context;

      /**
       * Generates provider-wide actions
       */
      var providerActions = function(actions, options) {
        if (!options) options = {};

        var $actions = $('<div>').addClass('main-actions');
        var allowedActions = options.actionFilter ? options.actionFilter({
          actions: $.map(actions, function(value, key) { return key; })
        }) : null;

        $.each(actions, function(actionID, action) {
          if (allowedActions && $.inArray(actionID, allowedActions) == -1)
            return true;

          var $action = $('<div>').addClass('button action main-action');

          $action.addClass(actionID);
          $action.append($('<span>').addClass('icon'));
          $action.append($('<span>').html(action.label));
          $action.click(function() {
            action.action({
              context: { zones: listViewArgs.context.physicalResources },
              response: {
                success: function(args) {
                  $(window).trigger('cloudStack.fullRefresh');
                  if (options.success) options.success($.extend(args, {
                    action: action
                  }));
                }
              }
            });
          });

          $action.appendTo($actions);

          return true;
        });

        return $actions;
      };

      /**
       * Render specified network's system chart
       */
      var chartView = function(network) {
        var $chartView = $('<div>').addClass('system-chart-view')
              .append($('#template').find('div.zone-chart').clone());
        var $naasView = $chartView.find('.resources.naas ul.system-main');
        var networkStatus = naas.networkProviders.statusCheck({
          context: $.extend(true, {}, context, {
            systemNetworks: [network]
          })
        });

        // Update title
        var $title = $chartView.find('.head span');
        $title.html($title.html() + ' - ' + network.name);

        // Render network provider items
        var $networkProviders = $('<ul>')
              .appendTo(
                $('<li>').addClass('network-providers').appendTo($naasView)
              );

        $.each(naas.networkProviders.types, function(name, type) {
          var status = networkStatus[name];
          var statusLabel = naas.networkProviders.statusLabels ?
                naas.networkProviders.statusLabels[status] : {};

          var $item = $('<li>').addClass('provider')
                .attr('rel', name)
                .attr('network-status', status)
                .addClass(name).addClass(status)
                .appendTo($networkProviders)
                .append($('<div>').addClass('name').html(type.label))
                .append($('<div>').addClass('status')
                        .append($('<span>').html(
                          statusLabel ? statusLabel : status
                        )))
                .append($('<div>').addClass('view-all configure').html('Configure'));
        });

        // View all action
        $chartView.find('ul li div.view-all').click(function() {
          var $target = $(this);

          if ($target.hasClass('configure')) return false;

          var $panel = $browser.cloudBrowser('addPanel', {
            title: $target.closest('li').find('div.name span').html(),
            data: '',
            noSelectPanel: true,
            maximizeIfSelected: true,
            complete: function($newPanel) {
              $panel.listView(
                $.extend(cloudStack.sections.system.subsections[
                  $target.attr('zone-target')
                ], {
                  $browser: $browser,
                  $chartView: $chartView,
                  ref: { zoneID: listViewArgs.id },
                  context: { zones: listViewArgs.context.physicalResources }
                })
              );
            }
          });

          return false;
        });

        // View details action
        $chartView.find('ul li div.view-details').click(function() {
          var $target = $(this);
          var $panel = $browser.cloudBrowser('addPanel', {
            title: 'Zone Details',
            data: '',
            noSelectPanel: true,
            maximizeIfSelected: true,
            complete: function($newPanel) {
              // Create detail view
              $.extend(args.detailView, {
                id: listViewArgs.id,
                context: { zones: listViewArgs.context.physicalResources },
                $browser: listViewArgs.$browser
              });

              $panel.detailView(args.detailView);
            }
          });

          return false;
        });

        // Add Resource button action
        $chartView.find('#add_resource_button').click(function() {
          var completeAction = function() { return false; };
          var $addResource = $('<div>').addClass('add-zone-resource');
          var $header = $('<div>').addClass('head').appendTo($addResource)
                .append(
                  $('<span>').addClass('message').html('Select resource to add:')
                );
          var $select = $('<select>').change(function() {
            var action = cloudStack.sections.system.subsections[$select.val()]
                  .listView.actions.add;
            var createForm = action.createForm;

            $addResource.find('.form-container').remove();

            // Create dialog
            var formData = cloudStack.dialog.createForm({
              form: createForm,
              context: { zones: listViewArgs.context.physicalResources },
              after: function(args) {
                action.action($.extend(args, {
                  response: {
                    success: function(args) {
                      $('div.notifications').notifications('add', {
                        desc: action.messages.notification({}),
                        interval: 1000,
                        poll: action.notification.poll,
                        _custom: args ? args._custom : null
                      });
                    }
                  }
                }));
              },
              noDialog: true
            });

            var $formContainer = formData.$formContainer
                  .appendTo($addResource).validate();
            completeAction = formData.completeAction;

            $(':ui-dialog').dialog('option', 'position', 'center');
          });

          // Append list of 'add new' items, based on subsection actions
          $.each(cloudStack.sections.system.subsections, function(sectionID, section) {
            var addAction = section.listView && section.listView.actions ?
                  section.listView.actions.add : null;

            if (addAction) {
              $('<option>').appendTo($select)
                .html(section.title)
                .val(sectionID);
            }
          });

          $header.append($select);
          $addResource.dialog({
            dialogClass: 'create-form',
            width: 400,
            title: 'Add resource',
            buttons: [
              {
                text: 'Create',
                'class': 'ok',
                click: function() {
                  if (!completeAction($addResource.find('.form-container'))) {
                    return false;
                  }

                  $('div.overlay').remove();
                  $(this).dialog('destroy');

                  return true;
                }
              },
              {
                text: 'Cancel',
                'class': 'cancel',
                click: function() {
                  $('div.overlay').remove();
                  $(this).dialog('destroy');
                }
              }
            ]
          }).closest('.ui-dialog').overlay();
          $select.trigger('change');

          return false;
        });

        return $chartView;
      };

      // Iterate through networks; render tabs
      var loadNetworkData = function() {
        // Toolbar
        var $toolbar = $('<div>').addClass('toolbar').appendTo($charts);
        var $refresh = $('<div>').addClass('button refresh').appendTo($toolbar)
              .append($('<span>').html('Refresh'));

        // Tab content
        var $tabMain = $('<div>').addClass('network-tabs').appendTo($charts);
        var $loading = $('<div>').addClass('loading-overlay').appendTo($tabMain);
        naas.networks.dataProvider({
          context: context,
          response: {
            success: function(args) {
              var $tabs = $('<ul>').appendTo($tabMain);

              $loading.remove();

              // Populate network data with individual zone charts
              $(args.data).each(function() {
                var tabID = 'tab-system-networks-' + this.id;
                var $tab = $('<li>').appendTo($tabs).append(
                  $('<a>')
                    .attr({
                      href: '#' + tabID
                    })
                    .html(this.name)
                );
                var $tabContent = $('<div>').appendTo($tabMain)
                      .attr({ id: tabID })
                      .append(chartView(this));

                // Tooltip hover event
                var $tooltip = $tabContent.find('.tooltip-info:visible').hide();
                $tabContent.find('li.main').mouseenter(function(event) {
                  $tooltip.css({ opacity: 0 });
                  $tooltip.show().animate({ opacity: 1 }, { queue: false });

                  var $item = $(this);

                  $item.siblings().each(function() {
                    $tooltip.removeClass($(this).attr('rel'));
                  });
                  $tooltip.addClass('tooltip-info ' + $item.attr('rel'));
                });

                $tabContent.find('li.main').mouseleave(function(event) {
                  $tooltip.animate({ opacity: 0 }, { queue: false });
                });

                // Main items pre-filter
                if (naas.mainNetworksPreFilter) {
                  var disabledNetworks = naas.mainNetworksPreFilter({
                    context: context
                  });

                  $(disabledNetworks).each(function() {
                    var $item = $tabContent.find('li.main[rel=' + this + ']');

                    $item.addClass('disabled');
                  });
                }

                // Main items configure event
                $tabContent.find('li.main .view-all.configure').click(function() {
                  var itemID = $(this).closest('li').attr('rel');

                  $browser.cloudBrowser('addPanel', {
                    title: itemID + ' details',
                    maximizeIfSelected: true,
                    complete: function($newPanel) {
                      $newPanel.detailView(
                        $.extend(true, {}, naas.mainNetworks[itemID].detailView, {
                          $browser: listViewArgs.$browser,
                          context: { zones: context.physicalResources }
                        })
                      );
                    }
                  });

                  return false;
                });

                // Provider configure event
                $tabContent.find('li.provider .view-all.configure').click(function() {
                  var $li = $(this).closest('li');
                  var itemID = $li.attr('rel');
                  var status = $li.attr('network-status');
                  var networkProviderArgs = naas.networkProviders.types[itemID];
                  var action = networkProviderArgs.actions ? networkProviderArgs.actions.add : null;
                  var createForm = action ? networkProviderArgs.actions.add.createForm : null;
                  var itemName = networkProviderArgs.label;

                  /**
                   * Generate provider-wide actions
                   */
                  var loadProviderActions = function($listView) {
                    $listView.find('.toolbar .main-actions').remove();

                    var $providerActions = providerActions(
                      networkProviderArgs.providerActions ?
                        networkProviderArgs.providerActions : {},
                      {
                        success: function(args) {
                          var action = args.action;
                          $loading.appendTo($listView);

                          $('div.notifications').notifications('add', {
                            desc: action.messages.notification({}),
                            interval: 2000,
                            poll: action.notification.poll,
                            _custom: args ? args._custom : null,
                            complete: function(args) {
                              $loading.remove();
                              loadProviderActions($listView);
                            }
                          });
                        },

                        actionFilter: networkProviderArgs.providerActionFilter
                      }
                    );

                    $providerActions.appendTo($listView.find('.toolbar'));
                  };

                  var loadProviderDetails = function($container) {
                    var provider = naas.networkProviders.types[itemID];

                    if (provider.type == 'detailView') {
                      var $detailView = $container.detailView($.extend(true, {}, provider, {
                        $browser: $browser
                      }));
                    } else {
                      var $listView = $container.listView({
                        listView: provider
                      });

                      loadProviderActions($listView);
                    }
                  };


                  $browser.cloudBrowser('addPanel', {
                    title: itemName + ' details',
                    maximizeIfSelected: true,
                    complete: function($newPanel) {
                      if (status == 'not-configured') {
                        // Create form
                        var formData = cloudStack.dialog.createForm({
                          form: createForm,
                          context: { zones: listViewArgs.context.physicalResources },
                          after: function(args) {
                            action.action($.extend(args, {
                              response: {
                                success: function(args) {
                                  $newPanel.find('form').prepend($('<div>').addClass('loading-overlay'));
                                  $('div.notifications').notifications('add', {
                                    desc: action.messages.notification({}),
                                    interval: 1000,
                                    poll: action.notification.poll,
                                    _custom: args ? args._custom : null,
                                    complete: function(args) {
                                      refreshChart();
                                      $newPanel.html('');
                                      $loading.remove();
                                      loadProviderDetails($newPanel);
                                    }
                                  });
                                }
                              }
                            }));
                          },
                          noDialog: true
                        });

                        var $formContainer = formData.$formContainer.addClass('add-first-network-resource');
                        var $form = $formContainer.find('form');
                        var completeAction = formData.completeAction;

                        $newPanel.append(
                          $formContainer
                            .prepend(
                              $('<div>').addClass('title').html('Add new ' + itemName + ' device')
                            )
                            .append(
                              $('<div>')
                                .addClass('button submit')
                                .append($('<span>').html('Add'))
                                .click(function() {
                                  if ($form.valid()) {
                                    completeAction($formContainer);
                                  }
                                })
                            )
                        );
                      } else {
                        loadProviderDetails($newPanel);
                      }
                    }
                  });
                });
              });

              $tabMain.tabs();
              $tabMain.find('li:first').addClass('first');
              $tabMain.find('li:last').addClass('last');

              var $info = $charts.find('.side-info, .tooltip-icon').filter(function() {
                return $(this).hasClass(
                  context.physicalResources[0].networktype == 'Basic' ?
                    'basic' : 'advanced'
                );
              }).show();
            }
          }
        });

        var refreshChart = function() {
          $charts.children().remove();
          loadNetworkData();
        };

        $refresh.click(function() {
          refreshChart();
          return false;
        });

        var fullRefreshEvent = function(event) {
          if ($charts.is(':visible')) {
            refreshChart();
          } else {
            $(window).unbind('cloudStack.fullRefresh', fullRefreshEvent);
          }
        };
        $(window).bind('cloudStack.fullRefresh', fullRefreshEvent);
      };

      loadNetworkData();

      return $charts;
    };
  };


  /**
   * Zone details chart
   */
  cloudStack.uiCustom.systemChart = function(chartID) {
    /**
     * Make view all button
     */
    var viewAllButton = function(args) {
      var $viewAll = $('<div>').addClass('button view-all');
      var $label = $('<span>').addClass('view-all-label').html('View all');
      var $browser = args.$browser;
      var action = args.action;

      // Launch a list view
      $viewAll.click(function() {
        $browser.cloudBrowser('addPanel', {
          title: args.title,
          maximizeIfSelected: true,
          complete: function($newPanel) {
            action({ $panel: $newPanel });
          }
        });
      });

      $viewAll.append($label);

      return $viewAll;
    };

    /**
     * Chart button action generators
     */
    var actions = {
      /**
       * Makes a list view from given zone sub-section
       */
      listView: function(targetID, context) {
        return function(args) {
          var $elem = args.$panel;
          var listViewArgs = cloudStack.sections.system.subsections[targetID].listView;

          $elem.listView({
            context: context,
            listView: listViewArgs
          });
        };
      }
    };

    /**
     * Chart generators
     */
    var charts = {
      /**
       * Compute tab
       */
      compute: function(args) {
        var $chart = $('<div>');
        var context = args.context;
        var $browser = $('#browser .container');

        // Fix zone context naming
        context.zones = context.physicalResources;

        // Resource items
        var computeResources = {
          zone: {
            label: 'Zone'
          },

          pods: {
            label: 'Pods',
            viewAll: {
              action: actions.listView('pods', context), 
            }
          },

          clusters: {
            label: 'Clusters',
            viewAll: {
              action: actions.listView('clusters', context),
            }
          },

          hosts: {
            label: 'Hosts',
            viewAll: {
              action: actions.listView('hosts', context),
            }
         },

          primaryStorage: {
            label: 'Primary Storage',
            viewAll: {
              action: actions.listView('primary-storage', context),
            }
          },

          secondaryStorage: {
            label: 'Secondary Storage',
            viewAll: {
              action: actions.listView('secondary-storage', context),
            }
          }
        };


        var $computeResources = $('<ul>').addClass('resources');

        // Make resource items
        $.each(computeResources, function(id, resource) {
          var $li = $('<li>');
          var $label = $('<span>').addClass('label');

          $li.addClass(id);
          $label.html(resource.label);
          $label.appendTo($li);

          // View all
          if (resource.viewAll) {
            viewAllButton($.extend(resource.viewAll, {
              title: resource.label,
              $browser: $browser,
              context: context
            })).appendTo($li);
          }

          $li.appendTo($computeResources);
        });

        $chart.append($computeResources);

        return $chart;
      },

      network: function(args) {
        var $chart = $('<div>').html('Network');

        return $chart;
      },

      resources: function(args) {
        var $chart = $('<div>').html('Resources');

        return $chart;
      }
    };

    return function(args) {
      var $chart = charts[chartID](args).addClass('system-chart').addClass(chartID);

      return $chart;
    };
  };
})(jQuery, cloudStack);
