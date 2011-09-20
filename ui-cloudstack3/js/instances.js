(function($, cloudStack, testData) {
  login();	
	
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
            custom: cloudStack.instanceWizard({
              dataProvider: function(args) {
                args.response.success({
                  data: {
                    zones: testData.data.zones,
                    isos: {
                      featured: $.grep(testData.data.isos, function(elem) {
                        return elem.isfeatured === true;
                      }),
                      community: [],
                      mine: $.grep(testData.data.isos, function(elem) {
                        return elem.account === 'admin';
                      })
                    },
                    serviceOfferings: testData.data.serviceOfferings,
                    diskOfferings: testData.data.diskOfferings,
                    defaultNetworks: $.grep(testData.data.networks, function(elem) {
                      return elem.isdefault === true;
                    }),
                    optionalNetworks: $.grep(testData.data.networks, function(elem) {
                      return elem.isdefault === false;
                    }),
                    groups: [
                      {
                        id: '123',
                        groupname: 'Group A'
                      },
                      {
                        id: '1242',
                        groupname: 'Group B'
                      },
                      {
                        id: '125',
                        groupname: 'Group C'
                      }
                    ]
                  }
                });
              },
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

        reboot: {
          label: 'Reboot instance',
          action: function(args) {	    
			$.ajax({
			  url: createURL("rebootVirtualMachine&id=" + args.data.id),
			  dataType: "json",
			  async: true,
			  success: function(json) { 			    
				var jid = json.rebootvirtualmachineresponse.jobid;    				
				args.response.success({_custom:{jobId: jid}});							
			  }
			});  	
		  },
          messages: {
            confirm: function(args) {
              return 'Are you sure you want to reboot ' + args.name + '?';
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
            poll: pollAsyncJobResult
          }
        },
        stop: {
          label: 'Stop instance',
		  /*
          action: function(args) {
            setTimeout(function() {
              args.response.success();
            }, 500);
          },
		  */
		  action: function(args) {	    
			$.ajax({
			  url: createURL("stopVirtualMachine&id=" + args.data.id),
			  dataType: "json",
			  async: true,
			  success: function(json) { 			    
				var jid = json.stopvirtualmachineresponse.jobid;    				
				args.response.success({_custom:{jobId: jid}});							
			  }
			});  	
		  },		  
          messages: {
            confirm: function(args) {
              return 'Are you sure you want to stop ' + args.name + '?';
            },
            success: function(args) {
              return args.name + ' is stopping.';
            },
            notification: function(args) {
              return 'Rebooting VM: ' + args.name;
            },
            complete: function(args) {
              return args.name + ' has been stopped.';
            }
          },
          notification: {
            //poll: testData.notifications.testPoll
			poll: pollAsyncJobResult
          }
        },
		
        start: { 
		  label: 'Start instance' ,
		  action: function(args) {	    
			$.ajax({
			  url: createURL("startVirtualMachine&id=" + args.data.id),
			  dataType: "json",
			  async: true,
			  success: function(json) { 			    
				var jid = json.startvirtualmachineresponse.jobid;    				
				args.response.success({_custom:{jobId: jid}});							
			  }
			});  	
		  },
		  messages: {
            confirm: function(args) {
              return 'Are you sure you want to start ' + args.name + '?';
            },
            success: function(args) {
              return args.name + ' is starting.';
            },
            notification: function(args) {
              return 'Started VM: ' + args.name;
            },
            complete: function(args) {
              return args.name + ' has been started.';
            }
          },		  
          notification: {           
			poll: pollAsyncJobResult
          }		  
		},
		
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
			$.ajax({
		      url: createURL("destroyVirtualMachine&id=" + args.data.id),
			  dataType: "json",
			  async: true,
			  success: function(json) { 			    
				var jid = json.destroyvirtualmachineresponse.jobid;    				
				args.response.success({_custom:{jobId: jid}});							
			  }
			});  	
		  },		  
          notification: {
            poll: pollAsyncJobResult	
          }
        }
      },
      
	  //dataProvider: testData.dataProvider.listView('instances'),
	  dataProvider: function(args) {        
		$.ajax({
		  url: createURL("listVirtualMachines&page="+args.page+"&pagesize="+pageSize),
		  dataType: "json",
		  async: true,
		  success: function(json) { 	
			var items = json.listvirtualmachinesresponse.virtualmachine;			    
			args.response.success({data:items});			                			
		  }
		});  	
	  },
	  
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
          stop: { 
		    label: 'Stop VM', 			
			action: function(args) {	    
			  $.ajax({
				url: createURL("stopVirtualMachine&id=" + args.data.id),
				dataType: "json",
				async: true,
				success: function(json) { 			    
				  var jid = json.stopvirtualmachineresponse.jobid;    				
				  args.response.success({_custom:{jobId: jid}});							
				}
			  });  	
			},		  
			messages: {
			  confirm: function(args) {
				return 'Are you sure you want to stop ' + args.name + '?';
		      },
			  success: function(args) {
				return args.name + ' is stopping.';
			  },
			  notification: function(args) {
				return 'Rebooting VM: ' + args.name;
			  },
			  complete: function(args) {
				return args.name + ' has been stopped.';
			  }
			},
			notification: {			  
			  poll: pollAsyncJobResult
			}			
	      },
          reboot: {
            label: 'Reboot VM',
			action: function(args) {	    
			  $.ajax({
				url: createURL("rebootVirtualMachine&id=" + args.data.id),
				dataType: "json",
				async: true,
				success: function(json) { 			    
				  var jid = json.rebootvirtualmachineresponse.jobid;    				
				  args.response.success({_custom:{jobId: jid}});							
				}
			  });  	
			},
            messages: {
              confirm: function(args) {
                return 'Are you sure you want to reboot ' + args.name + '?';
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
              poll: pollAsyncJobResult
            }           
          },
          destroy: {
            label: 'Destroy VM',
			action: function(args) {	    
			  $.ajax({
				url: createURL("destroyVirtualMachine&id=" + args.data.id),
				dataType: "json",
				async: true,
				success: function(json) { 			    
				  var jid = json.destroyvirtualmachineresponse.jobid;    				
				  args.response.success({_custom:{jobId: jid}});							
				}
			  });  	
			},
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
              poll: pollAsyncJobResult
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
			
            //dataProvider: testData.dataProvider.detailView('instances')
			dataProvider: function(args) {	    
			  $.ajax({
				url: createURL("listVirtualMachines&id="+args.id),
				dataType: "json",
				async: true,
				success: function(json) { 	
				  var items = json.listvirtualmachinesresponse.virtualmachine;
				  if(items != null && items.length > 0) {
					args.response.success({data:items[0]});		
				  }    			
				}
			  });  	
		    }			
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
})(jQuery, cloudStack, testData);
