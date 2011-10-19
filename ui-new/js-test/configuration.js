(function(cloudStack) {
  cloudStack.sections.configuration = {
    title: 'Configuration',
    id: 'configuration',
    sectionSelect: {
      label: 'Select offering'
    },
    sections: {
      serviceOfferings: {
        type: 'select',
        title: 'Service',
        listView: {
          id: 'serviceOfferings',
          label: 'Service Offerings',
          fields: {
            name: { label: 'Name', editable: true },
            storagetype: { label: 'Storage Type' },
            cpuspeed: { label: 'CPU' },
            memory: { label: 'Memory' },
            domain: { label: 'Domain'}
          },
          actions: {
            add: {
              label: 'Add service offering',

              action: function(args) {
                args.response.success();
              },

              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to add ' + args.name + '?';
                },
                success: function(args) {
                  return 'Your new service offering is being created.';
                },
                notification: function(args) {
                  return 'Created service offering';
                },
                complete: function(args) {
                  return 'Service offering has been created';
                }
              },

              createForm: {
                title: 'New service offering',
                desc: 'Please fill in the following data to add a new service offering.',
                fields: {
                  name: { label: 'Name', editable: true },
                  displayText: { label: 'Display Text' },
                  storageType: {
                    label: 'Storage Type',
                    select: function(args){
                      args.response.success({
                        data: [
                          { id: 'small', name: 'Small Disk, 5GB' },
                          { id: 'medium', name: 'Medium Disk, 20GB' },
                          { id: 'large', name: 'Large Disk, 100GB' }
                        ]
                      });
                    }
                  },
                  cpuCores: { label: '# of CPU cores' },
                  cpuSpeed: { label: 'CPU Speed (in MHz)'},
                  memory: { label: 'Memory (in MB)' },
                  tags: { label: 'Tags' },
                  offerHA: { label: 'Offer HA', isBoolean: true },
                  isPublic: { label: 'Public', isBoolean: true }
                }
              },

              notification: {
                poll: testData.notifications.testPoll
              }
            }
          },
          dataProvider: testData.dataProvider.listView('serviceOfferings')
        }
      },
      diskOfferings: {
        type: 'select',
        title: 'Disk',
        listView: {
          id: 'diskOfferings',
          label: 'Disk Offerings',
          fields: {
            displaytext: { label: 'Name' },
            disksize: { label: 'Disk Size' },
            domain: { label: 'Domain'}
          },
          dataProvider: testData.dataProvider.listView('diskOfferings')
        }
      },
      networkOfferings: {
        type: 'select',
        title: 'Network',
        listView: {
          id: 'networkOfferings',
          label: 'Network Offerings',
          fields: {
            name: { label: 'Name', editable: true },
            networkrate: { label: 'Network Rate' },
            traffictype: { label: 'Traffic Type'}
          },
          dataProvider: testData.dataProvider.listView('networkOfferings')
        }
      }
    }
  };  
})(cloudStack);
