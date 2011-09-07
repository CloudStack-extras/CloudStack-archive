(function($, cloudStack) {
  /**
   * Instance wizard
   */
  var instanceWizard = function(args) {
    return function(listViewArgs) {
      var $wizard = $('#template').find('div.instance-wizard').clone();
      var $progress = $wizard.find('div.progress ul li');
      var $steps = $wizard.find('div.steps').children().hide();
      var $diagramParts = $wizard.find('div.diagram').children().hide();
      var $form = $wizard.find('form')

      $form.validate();

      // Close instance wizard
      var close = function() {
        $wizard.dialog('destroy');
        $('div.overlay').fadeOut(function() { $('div.overlay').remove(); });
      };

      // Save instance and close wizard
      var completeAction = function() {
        args.complete({
          // Populate data
          data: cloudStack.serializeForm($form),
          response: {
           success: function(args) {
              listViewArgs.complete({
                messageArgs: cloudStack.serializeForm($form)
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
        if ($targetStep.hasClass('review')) {
          $nextButton.find('span').html('Launch VM');
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

        // Update diagram; show/hide as necessary
        $diagramParts.filter(function() {
          return $(this).index() <= targetIndex;
        }).fadeIn('slow');
        $diagramParts.filter(function() {
          return $(this).index() > targetIndex;
        }).fadeOut('slow');


        setTimeout(function() {
          if (!$targetStep.find('input[type=radio]:checked').size()) {
            $targetStep.find('input[type=radio]:first').click();
          }
        }, 50);
      };

      // Events
      $wizard.click(function(event) {
        var $target = $(event.target);

        // Next button
        if ($target.closest('div.button.next').size()) {
          if (!$form.valid()) return false;

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
        title: 'Add instance',
        width: 800,
        height: 570,
        zIndex: 5000
      })
        .closest('.ui-dialog').overlay();
    };
  };

  cloudStack.sections.instances = {
    title: 'Instances',
    id: 'instances',
    listView: {
      section: 'instances',
      filters: {
        mine: { label: 'My instances' },
        all: { label: 'All instances' },
        running: { label: 'Running instances' },
        destroyed: { label: 'Destroyed instances' }
      },
      fields: {
        name: { label: 'Name', editable: true },
        account: { label: 'Account' },
        zonename: { label: 'Zone' },
        state: { label: 'Status' }
      },

      // List view actions
      actions: {
        // Add instance wizard
        add: {
          label: 'Add instance',

          action: {
            custom: instanceWizard({
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
              return 'Creating new VM: ' + args.name;
            },
            complete: function(args) {
              return args.name + ' has been created successfully!';
            }
          },
          notification: {
            poll: testData.notifications.testPoll
          }
        },

        edit: {
          label: 'Edit instance name',
          action: function(args) {
            args.response.success(args.data[0]);
          }
        },

        restart: {
          label: 'Restart instance',
          action: function(args) {
            setTimeout(function() {
              args.response.success();
            }, 1000);
          },
          messages: {
            confirm: function(args) {
              return 'Are you sure you want to restart ' + args.name + '?';
            },
            success: function(args) {
              return args.name + ' is being rebooted.';
            },
            notification: function(args) {
              return 'Rebooting VM: ' + args.name;
            },
            complete: function(args) {
              return args.name + ' has been rebooted successfully.';
            }
          },
          notification: {
            poll: testData.notifications.testPoll
          }
        },
        stop: {
          label: 'Stop instance',
          action: function(args) {
            setTimeout(function() {
              args.response.success();
            }, 500);
          },
          messages: {
            confirm: function(args) {
              return 'Are you sure you want to shutdown ' + args.name + '?';
            },
            success: function(args) {
              return args.name + ' is shutting down.';
            },
            notification: function(args) {
              return 'Rebooting VM: ' + args.name;
            },
            complete: function(args) {
              return args.name + ' has been shut down.';
            }
          },
          notification: {
            poll: testData.notifications.testPoll
          }
        },
        start: { label: 'Start instance' },
        destroy: {
          label: 'Destroy instance',
          messages: {
            confirm: function(args) {
              return 'Are you sure you want to destroy ' + args.name + '?';
            },
            success: function(args) {
              return args.name + ' is being destroyed.';
            },
            notification: function(args) {
              return 'Destroyed VM: ' + args.name;
            },
            complete: function(args) {
              return args.name + ' has been destroyed.';
            }
          },
          action: function(args) {
            setTimeout(function() {
              args.response.success();
            }, 200);
          },
          notification: {
            poll: testData.notifications.testPoll
          }
        }
      },
      dataProvider: testData.dataProvider.listView('instances'),
      detailView: {
        name: 'Instance details',
        viewAll: { path: 'storage.volumes', label: 'Volumes' },

        // Detail view actions
        actions: {
          edit: {
            label: 'Edit VM details', action: function(args) {
              setTimeout(function() {
                args.response.success();
              }, 500);
            },
            notification: {
              poll: testData.notifications.testPoll
            }
          },
          stop: { label: 'Shut down VM', action: function(args) {
            args.response.success();
          } },
          restart: {
            label: 'Restart VM',
            messages: {
              confirm: function(args) {
                return 'Are you sure you want to restart ' + args.name + '?';
              },
              success: function(args) {
                return args.name + ' is being rebooted.';
              },
              notification: function(args) {
                return 'Rebooting VM: ' + args.name;
              },
              complete: function(args) {
                return args.name + ' has been rebooted successfully.';
              }
            },
            notification: {
              poll: testData.notifications.testPoll
            },
            action: function(args) {
              setTimeout(function() {
                args.response.success();
              }, 1000);
            }
          },
          destroy: {
            label: 'Destroy VM',
            messages: {
              confirm: function(args) {
                return 'Are you sure you want to destroy ' + args.name + '?';
              },
              success: function(args) {
                return args.name + ' is being destroyed.';
              },
              notification: function(args) {
                return 'Destroying VM: ' + args.name;
              },
              complete: function(args) {
                return args.name + ' has been destroyed.';
              }
            },
            notification: {
              poll: testData.notifications.testPoll
            },
            action: function(args) {
              setTimeout(function() {
                args.response.success();
              }, 1000);
            }
          },
          migrate: {
            notification: {
              desc: 'Migrated VM',
              poll: testData.notifications.testPoll
            },
            label: 'Migrate VM', action: function(args) {
              args.response.success();
            }
          },
          attach: {
            label: 'Attach VM', action: function(args) {

            }
          },
          'reset-password': {
            label: 'Reset admin password for VM', action: function(args) {

            }
          },
          change: {
            label: 'Change VM', action: function(args) {

            }
          }
        },
        tabs: {
          // Details tab
          details: {
            title: 'Details',
            fields: [
              {
                name: {
                  label: 'Name', isEditable: true
                }
              },
              {
                id: { label: 'ID', isEditable: false },
                zonename: { label: 'Zone', isEditable: false },
                templateid: {
                  label: 'Template type',
                  isEditable: true,
                  select: (function() {
                    var items = [];

                    $(testData.data.templates).each(function() {
                      items.push({ id: this.id, description: this.name });
                    });

                    return items;
                  })()
                },
                serviceofferingname: { label: 'Service offering', isEditable: false },
                group: { label: 'Group', isEditable: true }
              }
            ],
            dataProvider: testData.dataProvider.detailView('instances')
          },

          /**
           * NICs tab
           */
          nics: {
            title: 'NICs',
            multiple: true,
            fields: [
              {
                name: { label: 'Name', header: true },
                ipaddress: { label: 'IP Address' },
                gateway: { label: 'Default gateway' },
                netmask: { label: 'Netmask' },
                type: { label: 'Type' }
              }
            ],
            dataProvider: function(args) {
              setTimeout(function() {
                var instance = $.grep(testData.data.instances, function(elem) {
                  return elem.id == args.id;
                });
                args.response.success({
                  data: $.map(instance[0].nic, function(item, index) {
                    item.name = 'NIC ' + (index + 1);
                    return item;
                  })
                });
              }, 500);
            }
          },

          /**
           * Statistics tab
           */
          stats: {
            title: 'Statistics',
            fields: {
              cpuspeed: { label: 'Total CPU' },
              cpuused: { label: 'CPU Utilized' },
              networkkbsread: { label: 'Network Read' },
              networkkbswrite: { label: 'Network Write' }
            },
            dataProvider: testData.dataProvider.detailView('instances')
          }
        }
      }
    }
  };
})(jQuery, cloudStack);
