(function(cloudStack) {
  cloudStack.sections.storage = {
    title: 'Storage',
    id: 'storage',
    sectionSelect: {
      label: 'Select view'
    },
    sections: {
      /**
       * Volumes
       */
      volumes: {
        type: 'select',
        title: 'Volumes',
        listView: {
          id: 'volumes',
          label: 'Volumes',
          fields: {
            name: { label: 'Name', editable: true },
            type: { label: 'Type' },
            zonename: { label: 'Zone' },
            size: { label: 'Size' },
            state: { 
              label: 'Status',
              indicator: {
                'Ready': 'on'
              } 
            }
          },

          filters: {
            mine: { label: 'My volumes' },
            large: { label: 'Large volumes' },
            small: { label: 'Small volumes' }
          },

          // List view actions
          actions: {
            // Add volume
            add: {
              label: 'Add volume',

              action: function(args) {
                args.response.success({ 
                  _custom: { jobID: new Date() }
                });
              },

              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to add ' + args.name + '?';
                },
                notification: function(args) {
                  return 'Creating new volume';
                }
              },

              createForm: {
                title: 'Add volume',
                desc: 'Please fill in the following data to add a new volume.',
                fields: {
                  name: {
                    label: 'Name',
                    validation: { required: true }
                  },
                  availabilityZone: {
                    label: 'Availability Zone',
                    select: function(args) {
                      args.response.success({
                        descriptionField: 'displayname',
                        data: [
                          { id: 'sanjose', displayname: 'San Jose' },
                          { id: 'Chicago', displayname: 'Chicago' }
                        ]
                      });
                    }
                  },
                  diskOffering: {
                    label: 'Disk Offering',
                    dependsOn: 'availabilityZone',
                    select: function(args) {
                      /**
                       * Example to show/hide fields
                       * 
                       * -Select Pod2 to show conditional fields
                       * -Select any other field to hide conditional fields
                       */
                      args.$select.change(function() {
                        var $input = $(this);
                        var $form = $input.closest('form');

                        // Note: need to actually select the .form-item div containing the input
                        var $diskSize = $form.find('.form-item[rel=diskSize]');

                        $diskSize.hide();

                        if ($input.val() == 'custom') {
                          // Note: need to show by setting display=inline-block, not .show()
                          $diskSize.css('display', 'inline-block');
                        }
                      });

                      args.response.success({
                        descriptionField: 'description',
                        data: [
                          { id: 'small', description: 'Small Disk, 5GB' },
                          { id: 'medium', description: 'Medium Disk, 20GB' },
                          { id: 'large', description: 'Large Disk, 100GB' },
                          { id: 'custom', description: 'Custom Disk Size' }
                        ]
                      });
                    }
                  },
                  diskSize: {
                    label: 'Disk size (in GB)',
                    validation: { required: true, number: true },
                    hidden: true
                  }
                }
              },

              notification: {
                poll: testData.notifications.customPoll(
                  testData.data.storage[0]
                )
              }
            },
            edit: {
              label: 'Edit volume name',
              action: function(args) {
                args.response.success(args.data[0]);
              }
            },
            snapshot: {
              label: 'Take snapshot',
              messages: {
                confirm: function(args) {
                  return 'Please confirm that you want to take a snapshot of ' + args.name;
                },
                notification: function(args) {
                  return 'Made snapshot of volume: ' + args.name;
                }
              },
              action: function(args) {
                args.response.success({
                  data: { state: 'Shapshotting' }
                });
              },
              notification: {
                poll: testData.notifications.customPoll({
                  state: 'Ready'
                })
              }
            },
            create: {
              label: 'Create template',
              addRow: 'false',
              messages: {
                success: function(args) {
                  return 'Your new snapshot ' + args.name + ' is being created.';
                },
                notification: function(args) {
                  return 'Making new snapshot: ' + args.name;
                }
              },
              action: function(args) {
                args.response.success({
                  data: {
                    state: 'Templating'
                  }
                });
              },
              createForm: {
                title: 'Create a template',
                desc: 'To create a template from the selected volume, please complete the fields below.',
                fields: {
                  name: { label: 'Name' },
                  displayText: { label: 'Display text' },
                  'public': {
                    label: 'Public', isBoolean: true
                  },
                  osType: {
                    label: 'OS Type',
                    dependsOn: 'public',
                    hidden: true,
                    select: function(args) {
                      args.response.success({
                        descriptionField: 'description',
                        data: [
                          { id: 'centos53-64', description: 'CentOS 5.3 (64-bit)' },
                          { id: 'rhel5-64', description: 'Red Hat Enterprise Linux 5.0 (64-bit)' },
                          { id: 'deb6-32', description: 'Debian GNU/Linux 6.0 (32-bit)' }
                        ]
                      });
                    }
                  },
                  usePassword: {
                    label: 'Use Password?',
                    isBoolean: true,
                    hidden: true,
                    dependsOn: 'public'
                  },
                  password: {
                    label: 'Password',
                    hidden: true,
                    dependsOn: 'usePassword',
                    password: true
                  }
                }
              },
              notification: {
                poll: testData.notifications.customPoll({
                  state: 'Ready'
                })
              }
            }
          },
          dataProvider: testData.dataProvider.listView('storage'),
          detailView: {
            name: 'Volume details',
            viewAll: { path: 'storage.snapshots', label: 'Snapshots' },
            actions: {
              edit: {
                label: 'Edit volume details',
                action: function(args) {
                  args.response.success();
                },
                notification: {
                  poll: testData.notifications.testPoll
                }
              },
              snapshot: {
                label: 'Take snapshot',
                messages: {
                  confirm: function(args) {
                    return 'Please confirm that you want to take a snapshot of ' + args.name;
                  },
                  notification: function(args) {
                    return 'Made snapshot of volume: ' + args.name;
                  }
                },
                action: function(args) {
                  args.response.success();
                },
                notification: {
                  poll: testData.notifications.testPoll
                }
              },
              createTemplate: {
                label: 'Create template',
                messages: {
                  success: function(args) {
                    return 'Your new template ' + args.name + ' is being created.';
                  },
                  notification: function(args) {
                    return 'Making new template: ' + args.name;
                  }
                },
                action: function(args) {
                  args.response.success();
                },
                createForm: {
                  title: 'Create a template',
                  desc: 'To create a template from the selected volume, please complete the fields below.',
                  fields: {
                    name: { label: 'Name', validation: { required: true } },
                    displayText: { label: 'Display text' },
                    osType: {
                      label: 'OS Type',
                      select: function(args) {
                        args.response.success({
                          descriptionField: 'description',
                          data: [
                            { id: 'centos53-64', description: 'CentOS 5.3 (64-bit)' },
                            { id: 'rhel5-64', description: 'Red Hat Enterprise Linux 5.0 (64-bit)' },
                            { id: 'deb6-32', description: 'Debian GNU/Linux 6.0 (32-bit)' }
                          ]
                        });
                      }
                    },
                    'public': {
                      label: 'Public', isBoolean: true
                    },
                    usePassword: {
                      label: 'Use password?', isBoolean: true
                    }
                  }
                },
                notification: {
                  poll: testData.notifications.testPoll
                }
              }
            },
            tabs: {
              details: {
                title: 'Details',
                fields: [
                  {
                    name: { label: 'Name', isEditable: true }
                  },
                  {
                    id: { label: 'ID' },
                    type: { label: 'Type' },
                    zone: { label: 'Zone' }
                  }
                ],
                dataProvider: testData.dataProvider.detailView('storage')
              }
            }
          }
        }
      },

      /**
       * Snapshots
       */
      snapshots: {
        type: 'select',
        title: 'Snapshots',
        listView: {
          id: 'snapshots',
          label: 'Snapshots',
          fields: {
            volumename: { label: 'Volume' },
            state: { label: 'State' },
            intervaltype: { label: 'Interval Type' },
            created: { label: 'Date' }
          },
          filters: {
            mine: { label: 'My snapshots' }
          },
          dataProvider: testData.dataProvider.listView('snapshots'),
          detailView: {
            name: 'Snapshot detail',
            tabs: {
              details: {
                title: 'Details',
                fields: [
                  {
                    name: { label: 'Name' }
                  },
                  {
                    id: { label: 'ID' },
                    volume: { label: 'Volume' },
                    state: { label: 'State' },
                    intervalType: { label: 'Interval Type' },
                    account: { label: 'Account' },
                    domain: { label: 'Domain' }
                  }
                ],
                dataProvider: testData.dataProvider.detailView('snapshots')
              }
            }
          }
        }
      }
    }
  };
})(cloudStack);
