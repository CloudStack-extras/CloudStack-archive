(function(cloudStack, testData) {
    login();

    var getIpAddresses = function(r) {        
        $.ajax({
	        url: createURL("listPublicIpAddresses"),
		    dataType: "json",
		    async: true,
		    success: function(json) { 	
			    var items = json.listpublicipaddressesresponse.publicipaddress;			    
				r.response.success({data:items});			                			
		    }
	    });  	
    };
    var getOneIpAddress = function(r) {        
        $.ajax({
	        url: createURL("listPublicIpAddresses&id="+r.id),
		    dataType: "json",
		    async: true,
		    success: function(json) { 	
			    var items = json.listpublicipaddressesresponse.publicipaddress;		
                if(items != null && items.length > 0) {				
				    r.response.success({data:items[0]});	
                }					
		    }
	    });  	
    };
	
	var getSecurityGroups = function(r) {        
        $.ajax({
	        url: createURL("listSecurityGroups"),
		    dataType: "json",
		    async: true,
		    success: function(json) { 	
			    var items = json.listsecuritygroupsresponse.securitygroup;			    
				r.response.success({data:items});			                			
		    }
	    });  	
    };
    var getOneSecurityGroup = function(r) {        
        $.ajax({
	        url: createURL("listSecurityGroups&id="+r.id),
		    dataType: "json",
		    async: true,
		    success: function(json) { 	
			    var items = json.listsecuritygroupsresponse.securitygroup;		
				if(items != null && items.length > 0) {						
				    r.response.success({data:items[0]});		
                }					
		    }
	    });  	
    };
  cloudStack.sections.network = {
    title: 'Network',
    id: 'network',
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
            },
            stop: {
              label: 'Disable static NAT',
              action: function(args) {
                setTimeout(function() {
                  args.response.success();
                }, 500);
              },
              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to disable ' + args.name + '?';
                },
                success: function(args) {
                  return args.name + ' is being disabled.';
                },
                notification: function(args) {
                  return 'Disabled NAT: ' + args.name;
                },
                complete: function(args) {
                  return args.name + ' is now disabled.';
                }
              },
              notification: {
                poll: testData.notifications.testPoll
              }
            }
          },
          
		  //dataProvider: testData.dataProvider.listView('network'),
		  dataProvider: getIpAddresses,

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
                
				//dataProvider: testData.dataProvider.detailView('network')
				dataProvider: getOneIpAddress
				
              },

              portRange: {
                title: 'Port range',
                multiEdit: true,
                fields: {
                  start: { label: 'Start Port', editable: true },
                  end: { label: 'End Port', editable: true },
                  protocol: {
                    label: 'Protocol',
                    editable: true,
                    select: [
                      { id: 'tcp', label: 'TCP' },
                      { id: 'udp', label: 'UDP' }
                    ]
                  },
                  state: { label: 'State' }
                },
                actions: {
                  create: {
                    label: 'Add port range',
                    messages: {
                      confirm: function(args) {
                        return 'Are you sure you want to add this port range?';
                      },
                      success: function(args) {
                        return 'Added port range';
                      },
                      notification: function(args) {
                        return 'Added port range';
                      },
                      complete: function(args) {
                        return 'Port range has been added.';
                      }
                    },
                    notification: {
                      poll: testData.notifications.testPoll
                    },
                    action: function(args) {
                      setTimeout(function() {
                        args.response.success();                        
                      }, 500);
                    }
                  },
                  destroy: {
                    label: 'Remove',
                    messages: {
                      confirm: function(args) {
                        return 'Are you sure you want to remove this port range?';
                      },
                      success: function(args) {
                        return 'Removed port range';
                      },
                      notification: function(args) {
                        return 'Removed port range: ' + args.name;
                      },
                      complete: function(args) {
                        return 'Port range has been removed.';
                      }
                    },
                    notification: {
                      poll: testData.notifications.testPoll
                    },
                    action: function(args) {
                      setTimeout(function() {
                        args.response.success();
                      }, 400);
                    }
                  }
                },
                dataProvider: function(args) {
                  setTimeout(function() {
                    args.response.success({
                      data: [
                        {
                          start: '1',
                          end: '100',
                          protocol: 'TCP',
                          state: 'Active'
                        },
                        {
                          start: '50',
                          end: '90',
                          protocol: 'UDP',
                          state: 'Active'
                        }
                      ]
                    });
                  });
                }
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
            },
            destroy: {
              label: 'Delete security group',
              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to delete ' + args.name + '?';
                },
                success: function(args) {
                  return args.name + ' is being deleted.';
                },
                notification: function(args) {
                  return 'Deleted security group: ' + args.name;
                },
                complete: function(args) {
                  return args.name + ' has been deleted.';
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
		  
          //dataProvider: testData.dataProvider.listView('securityGroups'),
		  dataProvider: getSecurityGroups,
		  
          detailView: {
            name: 'Security group details',
            tabs: {
              details: {
                title: 'Details',
                fields: [
                  {
                    name: { label: 'Name' }
                  },
                  {
                    domain: { label: 'Domain' },
                    account: { label: 'Account' }
                  } 
                ],
				
                //dataProvider: testData.dataProvider.detailView('securityGroups')
				dataProvider: getOneSecurityGroup
				
              },
              ingressRules: {
                title: 'Ingress Rules',
                multiEdit: true,
                fields: {
                  protocol: { 
                    label: 'Protocol',
                    editable: true,
                    select: [
                      { id: 'tcp', label: 'TCP' },
                      { id: 'udp', label: 'UDP' }
                    ]
                  },
                  startport: { label: 'Start Port', editable: true },
                  endport: { label: 'End Port', editable: true },
                  cidr: { label: 'CIDR', editable: true }
                },
                actions: {
                  create: {
                    label: 'Add ingress rule',
                    messages: {
                      confirm: function(args) {
                        return 'Are you sure you want to add this port range?';
                      },
                      success: function(args) {
                        return 'Added port range';
                      },
                      notification: function(args) {
                        return 'Added port range';
                      },
                      complete: function(args) {
                        return 'Port range has been added.';
                      }
                    },
                    notification: { 
                      poll: testData.notifications.testPoll
                    },
                    action: function(args) {
                      setTimeout(function() {
                        args.response.success();
                      }, 500);
                    } 
                  },
                  destroy: {
                    label: 'Remove rule',
                    messages: {
                      confirm: function(args) {
                        return 'Are you sure you want to remove this ingress rule?';
                      },
                      success: function(args) {
                        return 'Removed ingress rule';
                      },
                      notification: function(args) {
                        return 'Removed ingress rule: ' + args.name;
                      },
                      complete: function(args) {
                        return 'Ingress rule has been removed.';
                      }
                    },
                    notification: {
                      poll: testData.notifications.testPoll
                    },
                    action: function(args) {
                      setTimeout(function() {
                        args.response.success();
                      }, 500);
                    }
                  }
                },
                dataProvider: function(args) {
                  setTimeout(function() {
                    args.response.success({
                      data: [
                        {
                          "ruleid": 2,
                          "protocol": "tcp",
                          "startport": 22,
                          "endport": 22,
                          "cidr": "0.0.0.0/0"
                        },
                        {
                          "ruleid": 3,
                          "protocol": "icmp",
                          "startport": 80,
                          "endport": 90,
                          "cidr": "0.0.0.0/0"
                        }
                      ]
                    });
                  });
                }
              }
            }
          }
        }
      }
    }
  };
})(cloudStack, testData);
