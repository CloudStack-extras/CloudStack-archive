(function($, cloudStack) {
  /**
   * Instance wizard
   */
  var zoneWizard = function(args) {
    return function(listViewArgs) {
      var $wizard = $('#template').find('div.zone-wizard').clone();
      var $progress = $wizard.find('div.progress ul li');
      var $steps = $wizard.find('div.steps').children().hide();
      var $diagramParts = $wizard.find('div.diagram').children().hide();

      // Close wizard
      var close = function() {
        $wizard.dialog('destroy');
        $('div.overlay').fadeOut(function() { $('div.overlay').remove(); });
      };

      // Save and close wizard
      var completeAction = function() {
        args.complete({
          data: cloudStack.serializeForm($wizard.find('form')),
          response: {
            success: function(args) {
              listViewArgs.complete({
                messageArgs: {
                  name: $wizard.find('div.review div.vm-instance-name input').val()
                }
              });
              close();
            }
          }
        });
      };

      // Go to specified step in wizard,
      // updating nav items and diagram
      var showStep = function(index) {
        var targetIndex = index - 1;

        if (index <= 1) targetIndex = 0;
        if (targetIndex == $steps.size()) {
          completeAction();
        }

        var $targetStep = $($steps.hide()[targetIndex]).show();

        // Show launch vm button if last step
        var $nextButton = $wizard.find('.button.next');
        $nextButton.find('span').html('Next');
        $nextButton.removeClass('final');
        if ($targetStep.index() == $steps.size() - 1) {
          $nextButton.find('span').html('Add zone');
          $nextButton.addClass('final');
        }

        // Show relevant conditional sub-step if present
        if ($targetStep.has('.wizard-step-conditional')) {
          $targetStep.find('.wizard-step-conditional').hide();
          $targetStep.find('.wizard-step-conditional.select-network').show();
        }

        // Update progress bar
        var $targetProgress = $progress.removeClass('active').filter(function() {
          return $(this).index() <= targetIndex;
        }).toggleClass('active');

        setTimeout(function() {
          if (!$targetStep.find('input[type=radio]:checked').size()) {
            $targetStep.find('input[type=radio]:first').click();
          }
        }, 50);

        $targetStep.find('form').validate();
      };

      // Events
      $wizard.click(function(event) {
        var $target = $(event.target);

        // Next button
        if ($target.closest('div.button.next').size()) {
          // Check validation first
          var $form = $steps.filter(':visible').find('form');
          if ($form.size() && !$form.valid()) return false;

          showStep($steps.filter(':visible').index() + 2);

          return false;
        }

        // Previous button
        if ($target.closest('div.button.previous').size()) {
          showStep($steps.filter(':visible').index());

          return false;
        }

        // Close button
        if ($target.closest('div.button.cancel').size()) {
          close();
          
          return false;
        }


        // Edit link
        if ($target.closest('div.edit').size()) {
          var $edit = $target.closest('div.edit');

          showStep($edit.find('a').attr('href'));

          return false;
        }

        return true;
      });

      showStep(1);

      // Setup tabs and slider
      $wizard.find('.tab-view').tabs();
      $wizard.find('.slider').slider({
        min: 1,
        max: 100,
        start: function(event) {
          $wizard.find('div.data-disk-offering div.custom-size input[type=radio]').click();
        },
        slide: function(event, ui) {
          $wizard.find('div.data-disk-offering div.custom-size input[type=text]').val(
            ui.value
          );
        }
      });

      return $wizard.dialog({
        title: 'Add zone',
        width: 665,
        height: 665,
        zIndex: 5000,
        resizable: false
      })
        .closest('.ui-dialog').overlay();
    };
  };

  /**
   * Make system zone 'org' chart
   */
  var zoneChart = function(args) {
    return function(listViewArgs) {
      var $browser = listViewArgs.$browser;
      var $chartView = $('<div>').addClass('system-chart-view')
        .append(
          $('<div>').addClass('toolbar')
        )
        .append(
          $('#template').find('div.zone-chart').clone()
        );
      
      args.dataProvider({
        id: listViewArgs.id,
        response: {
          success: function(dataProviderArgs) {
            var data = dataProviderArgs.data;
            var name = data.name;
            
            // Replace cell contents
            $chartView.find('li.zone div.name span').html(name);

            // Events
            $chartView.click(function(event) {
              var $target = $(event.target);
              var $panel;

              // View zone details button
              if ($target.is('ul li div.view-details')) {
                $panel = $browser.cloudBrowser('addPanel', {
                  title: $target.closest('li').find('div.name span').html(),
                  data: '',
                  noSelectPanel: true,
                  maximizeIfSelected: true
                });
                $browser.cloudBrowser('toggleMaximizePanel', {
                  panel: $panel
                });

                // Create detail view
                $.extend(args.detailView, {
                  id: listViewArgs.id,
                  $browser: listViewArgs.$browser
                });
                $panel.detailView(args.detailView);

                return false;
              }

              // View all
              if ($target.is('ul li div.view-all')) {
                $panel = $browser.cloudBrowser('addPanel', {
                  title: $target.closest('li').find('div.name span').html(),
                  data: '',
                  noSelectPanel: true,
                  maximizeIfSelected: true
                });
                $browser.cloudBrowser('toggleMaximizePanel', {
                  panel: $panel
                });

                $panel.listView(
                  $.extend(cloudStack.sections.system.sections.physicalResources.subsections[
                    $target.attr('zone-target')
                  ], {
                    $browser: $browser,
                    $chartView: $chartView
                  })
                );

                return false;
              };

              return true;
            }); 
          }          
        }
      });

      return $chartView;
    };
  };

  cloudStack.sections.system = {
    title: 'System',
    id: 'system',
    sections: {
      physicalResources: {
        title: 'Physical Resources',
        listView: {
          label: 'Physical Resources',
          fields: {
            name: { label: 'Zone' },
            dns1: { label: 'DNS' },
            internaldns1: { label: 'Internal DNS' },
            networktype: { label: 'Network Type' },
            allocationstate: { label: 'State' }
          },
          actions: {
            add: {
              label: 'Add zone',
              action: {
                custom: zoneWizard({
                  complete: function(args) {
                    args.response.success({});
                  }
                })
              },
              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to add ' + args.name + '?';
                },
                success: function(args) {
                  return args.name + ' is being created.';
                },
                notification: function(args) {
                  return 'Created new zone';
                },
                complete: function(args) {
                  return 'Zone has been created successfully!';
                }
              },
              notification: {
                poll: testData.notifications.testPoll
              }
            }
          },
          dataProvider: testData.dataProvider.listView('zones'),
          detailView: {
            pageGenerator: zoneChart({
              dataProvider: testData.dataProvider.detailView('zones'),
              detailView: {
                name: 'Zone details',
                tabs: {
                  details: {
                    title: 'Details',
                    fields: [
                      {
                        name: { label: 'Zone' },
                      },
                      {
                        dns1: { label: 'DNS 1' },
                        dns2: { label: 'DNS 2' },
                        internaldns1: { label: 'Internal DNS 1' },
                        internaldns2: { label: 'Internal DNS 2' }
                      },
                      {
                        networktype: { label: 'Network Type' },
                        allocationstate: { label: 'State' },
                        vlan: { label: 'VLAN' },
                        networktype: { label: 'Network Type' },
                        securitygroupenabled: { label: 'Security Group?' }
                      }
                    ],
                    dataProvider: testData.dataProvider.detailView('zones')
                  }
                }
              },
            })
          }
        },
        subsections: {
          networks: {
            listView: {
              section: 'networks',
              fields: {
                name: { label: 'Name' },
                startip: { label: 'Start IP' },
                endip: { label: 'End IP' },
                type: { label: 'Type' }
              },
              dataProvider: testData.dataProvider.listView('networks'),
              detailView: {
                viewAll: { label: 'Hosts', path: 'instances' },
                tabs: {
                  details: {
                    title: 'Details',
                    fields: [
                      {
                        name: { label: 'name' },
                        displaytext: { label: 'displaytext' }
                      },
                      {
                        broadcastdomaintype: { label: 'broadcastdomaintype' },
                        traffictype: { label: 'traffictype' },
                        gateway: { label: 'gateway' },
                        netmask: { label: 'netmask' },
                        startip: { label: 'startip' },
                        endip: { label: 'endip' },
                        zoneid: { label: 'zoneid' },
                        networkofferingid: { label: 'networkofferingid' },
                        networkofferingname: { label: 'networkofferingname' },
                        networkofferingdisplaytext: { label: 'networkofferingdisplaytext' },
                        networkofferingavailability: { label: 'networkofferingavailability' },
                        isshared: { label: 'isshared' },
                        issystem: { label: 'issystem' },
                        state: { label: 'state' },
                        related: { label: 'related' },
                        broadcasturi: { label: 'broadcasturi' },
                        dns1: { label: 'dns1' },
                        type: { label: 'type' } 
                      }
                    ],                
                    dataProvider: testData.dataProvider.detailView('networks')
                  }
                }
              }
            }
          },
          pods: {
            listView: {
              section: 'pods',
              fields: {
                name: { label: 'Name' },
                startip: { label: 'Start IP' },
                endip: { label: 'End IP' },
                allocationstate: { label: 'Status' }
              },
              dataProvider: testData.dataProvider.listView('pods')
            }
          },
          clusters: {
            
          },
          hosts: {
            
          },
          primaryStorage: {
            
          },
          secondaryStorage: {
            
          }
        }        
      },
      virtualAppliances: {
        title: 'Virtual Appliances',
        listView: {
          label: 'Virtual Appliances',
          fields: {
            name: { label: 'Name' },
            hostname: { label: 'Hostname' },
            publicip: { label: 'Public IP' },
            publicmacaddress: { label: 'Public MAC' },
            state: { label: 'Status' }
          },
          dataProvider: testData.dataProvider.listView('virtualAppliances')
        }
      },
      systemVMs: {
        title: 'System VMs',
        listView: {
          label: 'System VMs',
          fields: {
            name: { label: 'Name' },
            zonename: { label: 'Zone' },
            hostname: { label: 'Hostname' },
            privateip: { label: 'Private IP' },
            publicip: { label: 'Public IP' },
            state: { label: 'Status' }
          },
          dataProvider: testData.dataProvider.listView('systemVMs')
        }
      }
    }
  };
})($, cloudStack);
