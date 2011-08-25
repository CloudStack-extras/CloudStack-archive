(function(cloudStack) {
  cloudStack.sections.network = {
    title: 'Network',
    sections: {
      ipAddresses: {
        title: 'IP Addresses',
        listView: {
          label: 'IPs',
          filters: {
            allocated: { label: 'Allocated ' },
            mine: { label: 'My network' }
          },
          fields: {
            ipaddress: { label: 'IP' },
            state: { label: 'State' },
            zonename: { label: 'Zone' },
            vlanname: { label: 'VLAN' },
            networkid: { label: 'Network Type' }
          },

          actions: {
            add: {
              label: 'Acquire new IP',

              action: function(args) {
                args.response.success();
              },

              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to add this new IP?';
                },
                success: function(args) {
                  return 'Your new IP is being allocated.';
                },
                notification: function(args) {
                  return 'Allocated IP';
                },
                complete: function(args) {
                  return 'IP has been acquired successfully';
                }
              },

              createForm: {
                title: 'Acquire new IP',
                desc: 'Please select a zone from which you want to acquire your new IP from.',
                fields: {
                  availabilityZone: {
                    label: 'Zone',
                    select: [
                      { id: 'sanjose', description: 'San Jose' },
                      { id: 'Chicago', description: 'Chicago' }
                    ]
                  }
                }
              },

              notification: {
                poll: testData.notifications.testPoll
              }
            }
          },
          dataProvider: testData.dataProvider.listView('network'),

          // Detail view
          detailView: {
            name: 'IP address detail',
            tabs: {
              details: {
                title: 'Details',
                fields: [
                  {
                    ipaddress: { label: 'IP' }
                  },
                  {
                    state: { label: 'State' },
                    zonename: { label: 'Zone' },
                    vlanname: { label: 'VLAN' },
                    issourcenat: { label: 'Source NAT' }
                  }
                ],
                dataProvider: testData.dataProvider.detailView('network')
              }
            }
          }
        }
      },
      securityGroups: {
        title: 'Security Groups',
        listView: {
          label: 'Security Groups',
          fields: {
            name: { label: 'Name', editable: true },
            description: { label: 'Description' },
            domain: { label: 'Domain' },
            account: { label: 'Account' }
          },
          actions: {
            add: {
              label: 'Add security group',

              action: function(args) {
                args.response.success();
              },

              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to add ' + args.name + '?';
                },
                success: function(args) {
                  return 'Your new security group is being created.';
                },
                notification: function(args) {
                  return 'Created security group';
                },
                complete: function(args) {
                  return 'Security group has been created';
                }
              },

              createForm: {
                title: 'New security group',
                desc: 'Please name your security group.',
                fields: {
                  name: { label: 'Name' },
                  description: { label: 'Description' }
                }
              },

              notification: {
                poll: testData.notifications.testPoll
              }
            }
          },
          dataProvider: testData.dataProvider.listView('securityGroups')
        }
      }
    }
  };  
})(cloudStack);
