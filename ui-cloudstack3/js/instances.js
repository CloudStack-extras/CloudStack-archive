(function($, cloudStack, testData) {

  var zoneObjs, hypervisorObjs, featuredTemplateObjs, communityTemplateObjs, myTemplateObjs, isoObjs, serviceOfferingObjs, diskOfferingObjs;
  var selectedZoneObj, selectedTemplateObj, selectedHypervisor, selectedDiskOfferingObj;
  var step5ContainerType = 'nothing-to-select'; //'nothing-to-select', 'select-network', 'select-security-group'	
  	
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
        state: { 
		  label: 'Status',
          indicator: {
            'Running': 'on',
            'Stopped': 'off',
			'Error': 'off'
          } 
		}
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
				  $(zoneObjs).each(function(){
				    if(this.id == args.currentData.zoneid) {
					  selectedZoneObj = this;
					  return false; //break the $.each() loop 
					}
				  });
				  if(selectedZoneObj == null) {
				    alert("error: can't find matched zone object");		
                    return;					
				  }			
				  				
				  $.ajax({
					url: createURL("listHypervisors&zoneid="+args.currentData.zoneid),			 
					dataType: "json",
					async: false,
					success: function(json) { 				   
					  hypervisorObjs = json.listhypervisorsresponse.hypervisor;		  				  
					}
				  });  
				  				 
				  $.ajax({
					url: createURL("listTemplates&templatefilter=featured&zoneid="+args.currentData.zoneid),			 
					dataType: "json",
					async: false,
					success: function(json) { 				   
					  featuredTemplateObjs = json.listtemplatesresponse.template;						  			  
					}
				  });  		
			  			      
				  $.ajax({
					url: createURL("listTemplates&templatefilter=community&zoneid="+args.currentData.zoneid),			 
					dataType: "json",
					async: false,
					success: function(json) { 				   
					  communityTemplateObjs = json.listtemplatesresponse.template;						  			  
					}
				  });  		
			  			     
				  $.ajax({
					url: createURL("listTemplates&templatefilter=selfexecutable&zoneid="+args.currentData.zoneid),			 
					dataType: "json",
					async: false,
					success: function(json) { 				   
					  myTemplateObjs = json.listtemplatesresponse.template;						  			  
					}
				  });  
				  
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
				  if(args.currentData["select-template"] == "select-template") {	
					for(var i=0; i < featuredTemplateObjs.length; i++) {
						if(featuredTemplateObjs[i].id == args.currentData.templateid) {
							selectedTemplateObj = featuredTemplateObjs[i];
							break;
						}            
					}		        
					if(selectedTemplateObj == null) {	
						for(var i=0; i < communityTemplateObjs.length; i++) {
							if(communityTemplateObjs[i].id == args.currentData.templateid) {
								selectedTemplateObj = communityTemplateObjs[i];
								break;
							}            
						}
					}  
					if(selectedTemplateObj == null) {	
						for(var i=0; i < myTemplateObjs.length; i++) {
							if(myTemplateObjs[i].id == args.currentData.templateid) {
								selectedTemplateObj = myTemplateObjs[i];
								break;
						    }            
					    }
					}		        		        
					if(selectedTemplateObj == null)		
						alert("unable to find matched template object");  
					else
					    selectedHypervisor = selectedTemplateObj.hypervisor;							
				  }
				  else { //(args.currentData["select-template"] == "select-iso" 						
                    selectedHypervisor = args.currentData.hypervisorid;						
				  }
  			      				
				  $.ajax({
					url: createURL("listServiceOfferings&issystem=false"),			 
					dataType: "json",
					async: true,
					success: function(json) { 				   
					  serviceOfferingObjs = json.listserviceofferingsresponse.serviceoffering;								  
					  args.response.success({ 
					    data: {serviceOfferings: serviceOfferingObjs}
					  });					  
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
					  diskOfferingObjs = json.listdiskofferingsresponse.diskoffering;	
                      args.response.success({
						required: isRequred,
						customFlag: 'iscustomized', // Field determines if custom slider is shown
						data: {diskOfferings: diskOfferingObjs}
					  });	
					}
				  }); 	
                },

                // Step 5: Network
                function(args) {	                 		 
				  if(diskOfferingObjs != null && diskOfferingObjs.length > 0) {
				    for(var i=0; i < diskOfferingObjs.length; i++) {
					  if(diskOfferingObjs[i].id == args.currentData.diskofferingid) {
					    selectedDiskOfferingObj = diskOfferingObjs[i];
					    break;
					  }
					}
				  }				 		  
				  		  
				  if (selectedZoneObj.securitygroupsenabled == false) {  //show network container				
					//vmWizardShowNetworkContainer($thisPopup);	 
					step5ContainerType = 'select-network';
				  } 
				  else if (selectedZoneObj.securitygroupsenabled == true) {  // if security group is enabled			    
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

                    //hasDedicatedDirectTaggedDefaultNetwork = true; //for testing only, comment it out before checking in!!!!!!!!!!!!					
					if(hasDedicatedDirectTaggedDefaultNetwork == true) {
					  if(confirm("Do you wish to launch your instance on your own private dedicated network?")) {
					    step5ContainerType = 'select-network';
					  }
					  else {
					    if(selectedHypervisor == "VMware" || g_directAttachSecurityGroupsEnabled != "true") 	
						  step5ContainerType = 'nothing-to-select'; 
                        else
						  step5ContainerType = 'select-security-group';	
					  }					 			  
					}					    
					else {
					  if(selectedHypervisor == "VMware" || g_directAttachSecurityGroupsEnabled != "true") 						  
						step5ContainerType = 'nothing-to-select'; 
                      else
						step5ContainerType = 'select-security-group';					  
					}
				  }						              
				  
				  //step5ContainerType = 'nothing-to-select'; //for testing only, comment it out before checking in!!!!!!!!!!!!
				  if(step5ContainerType == 'select-network') {	
                    var defaultNetworkArray = [], optionalNetworkArray = [];											  
					$.ajax({
						url: createURL("listNetworks&domainid="+g_domainid+"&account="+g_account+"&zoneId="+args.currentData.zoneid),
						dataType: "json",
						async: false,
						success: function(json) {						    
							var networks = json.listnetworksresponse.network;								
                            
							//***** Setup Virtual Network (begin) *****
							//virtualNetwork is first radio button in required network section. Show virtualNetwork when its networkofferingavailability is 'Required' or'Optional'							
							var virtualNetwork = null;							
							if(selectedZoneObj.securitygroupsenabled == false) {								
								if (networks != null && networks.length > 0) {
									for (var i = 0; i < networks.length; i++) {
										if (networks[i].type == 'Virtual') {
											virtualNetwork = networks[i];
											break;
										}
									}
								}																
								if (virtualNetwork == null) { //if there is no virtualNetwork
									$.ajax({
										url: createURL("listNetworkOfferings&guestiptype=Virtual"), //check DefaultVirtualizedNetworkOffering
										dataType: "json",
										async: false,
										success: function(json) {
											var networkOfferings = json.listnetworkofferingsresponse.networkoffering;
											if (networkOfferings != null && networkOfferings.length > 0) {
												for (var i = 0; i < networkOfferings.length; i++) {
													if (networkOfferings[i].isdefault == true 
													&& (networkOfferings[i].availability == "Required" || networkOfferings[i].availability == "Optional")
													) {
														// Create a virtual network
														var networkName = "Virtual Network";
														var networkDesc = "A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.";				
														$.ajax({
															url: createURL("createNetwork&networkOfferingId="+networkOfferings[i].id+"&name="+todb(networkName)+"&displayText="+todb(networkDesc)+"&zoneId="+args.currentData.zoneid),
															dataType: "json",
															async: false,
															success: function(json) {
																virtualNetwork = json.createnetworkresponse.network;														
																defaultNetworkArray.push(virtualNetwork);				 
															}
														});
													}
												}
											}
										}
									});
								} 
								else { //virtualNetwork != null (there is already a virtualNetwork)
									if (virtualNetwork.networkofferingavailability == 'Required' || virtualNetwork.networkofferingavailability == 'Optional') {		
										defaultNetworkArray.push(virtualNetwork);										
									} 
									else { //virtualNetwork.networkofferingavailability == 'Unavailable'
										//do not show virtualNetwork
									}
								}
							}					
							//***** Setup Virtual Network (end) *****
														
							
							//***** Setup Direct Networks (begin) *****
							//direct networks whose isdefault==true is 2nd~Nth radio buttons in required network section 
							//direct networks whose isdefault==false is a bunch of checkboxes in optional network section 							
							if (networks != null && networks.length > 0) {
								for (var i = 0; i < networks.length; i++) {
									//if selectedZoneObj.securitygroupsenabled is true and users still choose to select network instead of security group (from dialog), then UI won't show networks whose securitygroupenabled is true.
									if(selectedZoneObj.securitygroupsenabled == true && networks[i].securitygroupenabled == true) {
										continue;
									}
									
									if (networks[i].type != 'Direct') {
										continue;
									}
																		
									if (networks[i].isdefault) {
										if (virtualNetwork.networkofferingavailability == 'Required') { 
											continue; //don't display 2nd~Nth radio buttons in required network section when networkofferingavailability == 'Required'
										}										
										defaultNetworkArray.push(networks[i]);											
									} 
									else {										
										optionalNetworkArray.push(networks[i]);										
									}									
								}
							}														
							//***** Setup Direct Networks (end) *****
						}
					});															
					args.response.success({
						type: 'select-network', 
						data: {
						  defaultNetworks: defaultNetworkArray,
						  optionalNetworks: optionalNetworkArray,
						  securityGroups: []
						}
					});							
				  }
				  
				  else if(step5ContainerType == 'select-security-group') {	                    
					var securityGroupArray = [];
					$.ajax({					
						url: createURL("listSecurityGroups"+"&domainid="+g_domainid+"&account="+g_account),		
						dataType: "json",
						async: false,
						success: function(json) {			    		
							var items = json.listsecuritygroupsresponse.securitygroup;	
							if (items != null && items.length > 0) {
								for (var i = 0; i < items.length; i++) {
									if(items[i].name != "default") //exclude default security group because it is always applied
										securityGroupArray.push(items[i]);
								}
							}					    
						}
					});	
					args.response.success({
						type: 'select-security-group', 
						data: {
						  defaultNetworks: [],
						  optionalNetworks: [],
						  securityGroups: securityGroupArray
						}
					});	
				  }
				  				  
				  else if(step5ContainerType == 'nothing-to-select') {	  
					args.response.success({
						type: 'nothing-to-select', 
						data: {
						  defaultNetworks: [],
						  optionalNetworks: [],
						  securityGroups: []
						}
					});	
				  }
				  
                },

                // Step 6: Review
                function(args) {
                  return false;
                }
              ],
              complete: function(args) {	//Brian will rename "complete" to "action" later
				/*					
				var isValid = true;									
				isValid &= validateString("Name", $thisPopup.find("#wizard_vm_name"), $thisPopup.find("#wizard_vm_name_errormsg"), true);	 //optional	
				isValid &= validateString("Group", $thisPopup.find("#wizard_vm_group"), $thisPopup.find("#wizard_vm_group_errormsg"), true); //optional					
				if (!isValid) 
					return;		    
				*/
				
				// Create a new VM!!!!				
				var array1 = [];
				
				//step 1 : select zone		    					
				array1.push("&zoneId=" + args.data.zoneid);		
								 
				//step 2: select template        								
				array1.push("&templateId=" + args.data.templateid);  
                array1.push("&hypervisor=" + selectedHypervisor);					
					
				//step 3: select service offering						
				array1.push("&serviceOfferingId=" + args.data.serviceofferingid);
				
				//step 4: select disk offering				
				if(args.data.diskofferingid != null && args.data.diskofferingid != "0") {
				    array1.push("&diskOfferingId=" + args.data.diskofferingid);				    
					if(selectedDiskOfferingObj.iscustomized == true) 
					    array1.push("&size=" + args.data.size);									
				}
				
				//step 5: select network			
				if (step5ContainerType == 'select-network') {	                    		
					var array2 = [];
					var defaultNetwork = args.data["default-network"];
					if(defaultNetwork != null && defaultNetwork.length > 0)
					    array2.push(defaultNetwork);
						
					var optionalNetworks = args.data["optional-networks"]; 
					//optionalNetworks might be: (1) an array of string, e.g. ["203", "202"], 					
					if(typeof(optionalNetworks) == "object" && optionalNetworks.length != null) {
						if(optionalNetworks != null && optionalNetworks.length > 0) {
							for(var i=0; i < optionalNetworks.length; i++) {
								array2.push(optionalNetworks[i]);
							}
						}
					}					
					//optionalNetworks might be: (2) just an string, e.g. "202"
					else if(typeof(optionalNetworks) == "string" && optionalNetworks.length > 0) {
					    array2.push(optionalNetworks);
					}					
					array1.push("&networkIds=" + array2.join(","));				
				} 
				else if (step5ContainerType == 'select-security-group') {  	
					var securityGroupList;
                    var groups = args.data["security-groups"];	
                    if(groups != null && groups.length > 0) {
                        for(var i=0; i < groups.length; i++) {
						    if(i == 0)
							    securityGroupList = groups[i];
						    else
							    securityGroupList += ("," + groups[i]);
						}
                    }	
                    if(securityGroupList != null)					
					    array1.push("&securitygroupids=" + securityGroupList);				       			
				}
								
				var displayname = args.data.displayname;
				if(displayname != null && displayname.length > 0)
				    array1.push("&displayname="+todb(displayname));	
				
				var group = args.data.groupname;
				if (group != null && group.length > 0) 
					array1.push("&group="+todb(group));										    	
				               			
				$.ajax({
					url: createURL("deployVirtualMachine"+array1.join("")),
					dataType: "json",
					success: function(json) {
						var jid = json.deployvirtualmachineresponse.jobid;					    
						args.response.success({ _custom: { jobId: jid } });											    									    
					},
					error: function(XMLHttpResponse) {	                        				
						//args.response.error(); //wait for Brian to implement
						alert("Failed to deploy VM.");	
					}					
				});	               
              }			  
            })
          },

          messages: {		    
            confirm: function(args) {  //never being called              		
              return 'Are you sure you want to add ' + args.name + '?'; 
            },			
            success: function(args) {  //never being called 			 
              return args.name + ' is being created.';
            },
            notification: function(args) {	             		
              //return 'Creating new VM: ' + args.name; //args.name is not available			  
			  return 'Creating new VM';
            },
            complete: function(args) {  //never being called  			  
              return args.name + ' has been created successfully!';
            }
          },
          notification: {
            poll: pollAsyncJobResult
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
              return 'Stopping VM: ' + args.name;
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
              return 'Starting VM: ' + args.name;
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
				return 'Stopping VM: ' + args.name;
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
