(function($, cloudStack, testData) {

  var zoneObjs;
	
  cloudStack.sections.instances = {
    title: 'Instances',
    id: 'instances',
    listView: {
      section: 'instances',
      filters: {       
        mine: { label: 'Mine' },
        running: { label: 'Running' },
        stopped: { label: 'Stopped' },
		destroyed: { label: 'Destroyed' }
      },
      fields: {
        name: { label: 'Name', editable: true },
        displayname: { label: 'Display Name' },
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
			  steps: [
                // Step 1: Setup
                function(args) {				 
				  $.ajax({
					url: createURL("listZones&available=true"),			 
					dataType: "json",
					async: true,
					success: function(json) { 				   
					  zoneObjs = json.listzonesresponse.zone;								  
					  args.response.success({ data: {zones: zoneObjs}});					  
					}
				  });  
                },

                // Step 2: Select template
                function(args) {				  
				  var hypervisorObjs;
				  $.ajax({
					url: createURL("listHypervisors&zoneid="+args.currentData.zoneid),			 
					dataType: "json",
					async: false,
					success: function(json) { 				   
					  hypervisorObjs = json.listhypervisorsresponse.hypervisor;		  				  
					}
				  });  
				  
				  var featuredTemplateObjs;
				  $.ajax({
					url: createURL("listTemplates&templatefilter=featured&zoneid="+args.currentData.zoneid),			 
					dataType: "json",
					async: false,
					success: function(json) { 				   
					  featuredTemplateObjs = json.listtemplatesresponse.template;						  			  
					}
				  });  		
			  
			      var communityTemplateObjs;
				  $.ajax({
					url: createURL("listTemplates&templatefilter=community&zoneid="+args.currentData.zoneid),			 
					dataType: "json",
					async: false,
					success: function(json) { 				   
					  communityTemplateObjs = json.listtemplatesresponse.template;						  			  
					}
				  });  		
			  
			      var myTemplateObjs;
				  $.ajax({
					url: createURL("listTemplates&templatefilter=selfexecutable&zoneid="+args.currentData.zoneid),			 
					dataType: "json",
					async: false,
					success: function(json) { 				   
					  myTemplateObjs = json.listtemplatesresponse.template;						  			  
					}
				  });  

				  var isoObjs;
				  $.ajax({
					url: createURL("listIsos&isReady=true&bootable=true&isofilter=executable&zoneid="+args.currentData.zoneid),			 
					dataType: "json",
					async: false,
					success: function(json) { 				   
					  isoObjs = json.listisosresponse.iso;						  			  
					}
				  });  					  
				  				  
                  args.response.success({
                    hypervisor: {
                      idField: 'name',
                      nameField: 'name'
                    },
                    data: {
                      templates: {
                        featuredtemplates: featuredTemplateObjs,
                        communitytemplates: communityTemplateObjs,
						mytemplates: myTemplateObjs,
                        isos: isoObjs
                      },
                      hypervisors: hypervisorObjs
                    }
                  });
                },

                // Step 3: Service offering
                function(args) {				  
				  $.ajax({
					url: createURL("listServiceOfferings&issystem=false"),			 
					dataType: "json",
					async: true,
					success: function(json) { 				   
					  var items = json.listserviceofferingsresponse.serviceoffering;								  
					  args.response.success({ data: {serviceOfferings: items}});					  
					}
				  }); 				  
                },

                // Step 4: Data disk offering
                function(args) {					  
                  var isRequred = (args.currentData["select-template"] == "select-iso"? true: false); 					  
				  $.ajax({
					url: createURL("listDiskOfferings"),			 
					dataType: "json",
					async: true,
					success: function(json) { 				   
					  var items = json.listdiskofferingsresponse.diskoffering;	
                      args.response.success({
						required: isRequred,
						customFlag: 'iscustomized', // Field determines if custom slider is shown
						data: {
						  diskOfferings: testData.data.diskOfferings
						}
					  });	
					}
				  }); 	
                },

                // Step 5: Network
                function(args) {				  			  
				  var zoneObj;
				  $(zoneObjs).each(function(){
				    if(this.id == args.currentData.zoneid) {
					  zoneObj = this;
					  return false; //break the $.each() loop 
					}
				  });
				  if(zoneObj == null) {
				    alert("error: can't find matched zone object");		
                    return;					
				  }
				 				  
				  var containerType = 'no-network'; //'no-network', 'select-network', 'select-security-group'				  
				  if (zoneObj.securitygroupsenabled == false) {  //show network container				
					//vmWizardShowNetworkContainer($thisPopup);	 
					containerType = 'select-network';
				  } 
				  else if (zoneObj.securitygroupsenabled == true) {  // if security group is enabled			    
					var hasDedicatedDirectTaggedDefaultNetwork = false;					
					$.ajax({
					  url: createURL("listNetworks&type=Direct&domainid="+g_domainid+"&account="+g_account+"&zoneId="+args.currentData.zoneid),
					  dataType: "json",
					  async: false,
					  success: function(json) {					    
						var items = json.listnetworksresponse.network;											
						if (items != null && items.length > 0) {
						  for (var i = 0; i < items.length; i++) {								
							if(items[i].isshared ==	false && items[i].isdefault == true) { //dedicated, is default one.
							  var broadcasturi = items[i].broadcasturi;	//e.g. "vlan://53"
							  if(broadcasturi != null && broadcasturi.length > 0) {
							  var vlanIdString = broadcasturi.substring(7); //e.g. "53"
							  if(isNaN(vlanIdString) == false)
								hasDedicatedDirectTaggedDefaultNetwork = true;
							  }
							}
						  }
						}
				      }
					});							
					if(hasDedicatedDirectTaggedDefaultNetwork == true) {
					  /*
					  $("#dialog_confirmation")
					  .text(dictionary["message.launch.vm.on.private.network"])
					  .dialog("option", "buttons", {	                    
						"Yes": function() {
						  //present the current UI we have today	
						  //vmWizardShowNetworkContainer($thisPopup);  
						  containerType = 'select-network';
						  $(this).dialog("close");
						},
						"No": function() {	                         
						  //present security groups for user to select
						  //vmWizardShowSecurityGroupContainer($thisPopup);	
						  containerType = 'select-security-group';
						  $(this).dialog("close");	
						}
					  }).dialog("open");   
                      */	
                      containerType = 'select-network'; //temporary					  
					}					    
					else {
					  //vmWizardShowSecurityGroupContainer($thisPopup);	
                      containerType = 'select-security-group';					  
					}
				  }						              
				 
                  args.response.success({
                    type: containerType, //'no-network', 'select-network', 'select-security-group'
                    data: {
                      defaultNetworks: $.grep(testData.data.networks, function(elem) {
                        return elem.isdefault === true;
                      }),
                      optionalNetworks: $.grep(testData.data.networks, function(elem) {
                        return elem.isdefault === false;
                      }),
                      securityGroups: testData.data.securityGroups
                    }
                  });
                },

                // Step 6: Review
                function(args) {
                  return false;
                }
              ],
              complete: function(args) {
                args.response.success({ _custom: { jobID: 12345 } });
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
		var array1 = [];	
		if(args.filterBy != null) {
		  if(args.filterBy.kind != null) {
			switch(args.filterBy.kind) {				
				case "mine":
				  array1.push("&domainid=" + g_domainid + "&account=" + g_account);
				  break;
				case "running":
				  array1.push("&state=Running");
				  break;
				case "stopped":
				  array1.push("&state=Stopped");
				  break;
				case "destroyed":
				  array1.push("&state=Destroyed");
				  break;
			}
		  }
		  if(args.filterBy.search != null && args.filterBy.search.by != null && args.filterBy.search.value != null) {
		    switch(args.filterBy.search.by) {
			  case "name":
			    array1.push("&keyword=" + args.filterBy.search.value);
			    break;
			}
		  }
		}
		
		$.ajax({
		  url: createURL("listVirtualMachines&page=" + args.page + "&pagesize=" + pageSize + array1.join("")),
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
				guestosid: {
                  label: 'OS Type',
                  isEditable: true,
                  select: function(args) {	
					$.ajax({
					  url: createURL("listOsTypes"),			 
					  dataType: "json",
					  async: true,
					  success: function(json) { 				   
						var ostypes = json.listostypesresponse.ostype;
                        var items = [];		
                        $(ostypes).each(function() {
						  items.push({id: this.id, description: this.description});
						});						
						args.response.success({data: items});					  
					  }
					});   
                  }				  
                },				
				templateid: {
                  label: 'Template type',
                  isEditable: false
				  /*
				  ,
                  select: function(args) {
                    var items = [];

                    $(testData.data.templates).each(function() {
                      items.push({ id: this.id, description: this.name });
                    });
                   
                    args.response.success({ data: items });                   
                  }
				  */
                },
                serviceofferingname: { label: 'Service offering', isEditable: false },
                group: { label: 'Group', isEditable: true }
              }
            ],
			
            //dataProvider: testData.dataProvider.detailView('instances')
			dataProvider: function(args) {	              
              args.response.success({data:args.jsonObj});	
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
