(function(cloudStack, testData) {
  login();

  cloudStack.sections.configuration = {
    title: 'Configuration',
    id: 'configuration',
    sectionSelect: {
      label: 'Select Offering'
    },
    sections: {
      serviceOfferings: {
        type: 'select',
        title: 'Service',
        listView: {
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
                    select: [
                      { id: 'shared', description: 'Shared' },
                      { id: 'local', description: 'Local' }
                    ]
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
          dataProvider: function(args) {          
			$.ajax({
			  url: createURL("listServiceOfferings&issystem=false&page="+args.page+"&pagesize="+pageSize),
			  dataType: "json",
			  async: true,
			  success: function(json) { 	
				var items = json.listserviceofferingsresponse.serviceoffering;			    
				args.response.success({data:items});			                			
			  }
			});  	
		  }
        }
      },
      diskOfferings: {
        type: 'select',
        title: 'Disk',
        listView: {
          label: 'Disk Offerings',
          fields: {
            displaytext: { label: 'Name' },
            disksize: { label: 'Disk Size' },
            domain: { label: 'Domain'}
          },
          dataProvider: function(args) {          
			$.ajax({
			  url: createURL("listDiskOfferings&page="+args.page+"&pagesize="+pageSize),
			  dataType: "json",
			  async: true,
			  success: function(json) { 	
				var items = json.listdiskofferingsresponse.diskoffering;			    
				args.response.success({data:items});			                			
			  }
			});  	
		  }
        }
      },
      networkOfferings: {
        type: 'select',
        title: 'Network',
        listView: {
          label: 'Network Offerings',
          fields: {
            name: { label: 'Name', editable: true },
            networkrate: { label: 'Network Rate' },
            traffictype: { label: 'Traffic Type'}
          },
          dataProvider: function(args) {          
			$.ajax({
			  url: createURL("listNetworkOfferings&guestiptype=Virtual&page="+args.page+"&pagesize="+pageSize),
			  dataType: "json",
			  async: true,
			  success: function(json) { 	
				var items = json.listnetworkofferingsresponse.networkoffering;			    
				args.response.success({data:items});			                			
			  }
			});  	
		  }
        }
      },
      globalSettings: {
        type: 'button',
        title: 'Global Settings',
        listView: {
          label: 'Global Settings',
          actions: {
            edit: {
              label: 'Change value',
              action: function(args) {
                args.response.success();
              },
              notification: {
                poll: testData.notifications.testPoll
              }
            }
          },
          fields: {
            name: { label: 'Name', id: true },
            description: { label: 'Description' },
            value: { label: 'Value', editable: true }
          },
          dataProvider: function(args) {          
			$.ajax({
			  url: createURL("listConfigurations&page="+args.page+"&pagesize="+pageSize),
			  dataType: "json",
			  async: true,
			  success: function(json) { 	
				var items = json.listconfigurationsresponse.configuration;			    
				args.response.success({data:items});			                			
			  }
			});  	
		  }
        }
      }
    }
  };  
})(cloudStack, testData);
