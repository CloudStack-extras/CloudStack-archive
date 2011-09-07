(function(cloudStack) {
  cloudStack.sections.domains = {
    title: 'Domains',
    id: 'domains',

    // Domain tree
    treeView: {
      // Details
      detailView: {
        name: 'Domain details',
        viewAll: {
          label: 'Accounts',
          path: 'accounts'
        },

        // Detail actions
        actions: {
          // Destroy
          destroy: {
            label: 'Remove domain',
            messages: {
              confirm: function(args) {
                return 'Are you sure you want to destroy this domain?'
              },
              notification: function(args) {
                return 'Removed domain: ' + args.name;
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
          },
          
          // Edit domain
          edit: {
            label: 'Edit domain details',
            messages: {
              notification: function(args) {
                return 'Edited domain: ' + args.name;
              }
            },
            action: function(args) {
              setTimeout(function() {
                args.response.success();
              }, 200);
            }
          },

          // Add domain
          create: {
            label: 'Add domain',

            action: function(args) {
              args.response.success();
            },

            messages: {
              notification: function(args) {
                return 'Created domain'
              }
            },

            createForm: {
              title: 'Add subdomain',
              desc: 'Please specify the domain you want to create.',
              fields: {
                name: {
                  label: 'Name',
                  validation: { required: true }
                },
                parent: {
                  label: 'Parent Domain',
                  validation: { required: true }
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
                accounts: { label: 'Accounts' },
                instances: { label: 'Instances' },
                volumes: { label: 'Volumes' }
              }
            ],
            dataProvider: function(args) {
              args.response.success({
                data: {
                  name: 'Domain name',
                  accounts: 12,
                  volumes: 23
                }
              });
            }
          }
        }
      },
      labelField: 'name',
      dataProvider: function(args) {
        args.response.success({
          data: [
            { id: 'domainA', name: 'Domain A' },
            { id: 'domainB', name: 'Domain B' }
          ]
        });
      }
    }
  };
})(cloudStack);
