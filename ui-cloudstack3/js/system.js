(function($, cloudStack, testData) {

  var zoneObjs, podObjs, clusterObjs;

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
                custom: cloudStack.zoneWizard({
                  action: function(args) {
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
          		  
		  dataProvider: function(args) {  
			$.ajax({
			  url: createURL("listZones&page="+args.page+"&pagesize="+pageSize),
			  dataType: "json",
			  async: true,
			  success: function(json) { 
				zoneObjs = json.listzonesresponse.zone;			    
				args.response.success({data:zoneObjs});			                			
			  }
			});  	
		  },
		  
          actions: {
            add: {
              pod: {
                action: function(args) {
                  args.response.success();
                },

                messages: {
                  notification: function(args) {
                    return 'Created new pod';
                  }
                },

                createForm: {
                  title: 'Add pod',
                  desc: 'Please fill in the following data to add a new pod',
                  fields: {
                    name: {
                      label: 'Name',
                      validation: { required: true }
                    },
                    gateway: {
                      label: 'Gateway',
                      validation: { required: true }
                    },
                    netmask: {
                      label: 'Netmask',
                      validation: { required: true }
                    },
                    ipRange: {
                      label: 'Private IP Range',
                      range: true,
                      validation: { required: true, number: true }
                    }
                  }
                },

                notification: {
                  poll: testData.notifications.testPoll
                }
              },
              cluster: {
                
              },
              'primary-storage': {
                
              },
              host: {
                
              }
            }
          },
          detailView: {
            pageGenerator: cloudStack.zoneChart({
              dataProvider: testData.dataProvider.detailView('zones'),
              detailView: {
                name: 'Zone details',
                tabs: {
                  details: {
                    title: 'Details',
                    fields: [
                      {
                        name: { label: 'Zone' }
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
              }
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
})($, cloudStack, testData);
