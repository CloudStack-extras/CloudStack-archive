(function(cloudStack, testData) {  

  var diskofferingObjs, selectedDiskOfferingObj;

  var actionfilter = function(args) {	    		  
    var jsonObj = args.context.item;
	var allowedActions = [];	
    /*	
	if (jsonObj.state == 'Destroyed') {
		if(isAdmin() || isDomainAdmin()) {
		    allowedActions.push("restore");												
		}	
	} 
	*/	
    return allowedActions;
  }
  
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
            deviceid: { label: 'Device ID' },
            size: { label: 'Size' }
          },

          // List view actions
          actions: {
            // Add volume
            add: {
              label: 'Add volume',
			  
              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to add ' + args.name + '?';
                },
                success: function(args) {
                  return 'Your new volume is being created.';
                },
                notification: function(args) {
                  return 'Creating new volume';
                },
                complete: function(args) {
                  return 'Volume has been created successfully!';
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
                      $.ajax({
						url: createURL("listZones&available=true"),			 
						dataType: "json",
						async: true,
						success: function(json) { 				   
						  var items = json.listzonesresponse.zone;								  
						  args.response.success({descriptionField: 'name', data: items});					  
						}
					  });  						 
					}		
                  },
                  diskOffering: {
                    label: 'Disk Offering',
                    select: function(args) {					  
					  $.ajax({
						url: createURL("listDiskOfferings"),			 
						dataType: "json",
						async: false,
						success: function(json) { 				   
						  diskofferingObjs = json.listdiskofferingsresponse.diskoffering;	                          
						  var items = [];
						  $(diskofferingObjs).each(function(){						   
						    items.push({id: this.id, description: this.displaytext});
						  });						  
						  args.response.success({data: items});					  
						}
					  });  	
					  
					  args.$select.change(function() {                           
						var diskOfferingId = $(this).val();
						$(diskofferingObjs).each(function(){						  
						  if(this.id == diskOfferingId) {
						    selectedDiskOfferingObj = this;
						    return false; //break the $.each() loop 
						  }
						});
						if(selectedDiskOfferingObj == null)
						  return;						
						
						var $form = $(this).closest('form');
                        var $diskSize = $form.find('.form-item[rel=diskSize]');
                        if (selectedDiskOfferingObj.iscustomized == true) {                          
                          $diskSize.css('display', 'inline-block');  
                        }
						else {
						  $diskSize.hide();
						}
                      });
					}				
                  }
				  
				  ,
                  diskSize: {
                    label: 'Disk size (in GB)',
                    validation: { required: true, number: true },
					hidden: true
                  }
				  
                }
              },

			  action: function(args) {	
                var array1 = [];
				array1.push("&name=" + args.data.name);
				array1.push("&zoneId=" + args.data.availabilityZone);
				array1.push("&diskOfferingId=" + args.data.diskOffering);
				
				// if(thisDialog.find("#size_container").css("display") != "none") { //wait for Brian to include $form in args
			      array1.push("&size=" + args.data.diskSize);
			    //}
				
				$.ajax({
				  url: createURL("createVolume" + array1.join("")),
				  dataType: "json",
				  async: true,
				  success: function(json) { 			    
					var jid = json.createvolumeresponse.jobid;	 
					args.response.success(
					  {_custom:
						{jobId: jid,
						  getUpdatedItem: function(json) {							   
							return json.queryasyncjobresultresponse.jobresult.volume;
						  },
						  getActionFilter: function() {
							return actionfilter;
						  }					 
						}
					  }
					);					
				  }
				});  	
			  },			  		  
			  
              notification: {                
				poll: pollAsyncJobResult
              }
            },
           
            takeSnapshot: {
              label: 'Take snapshot',
              messages: {
                confirm: function(args) {
                  return 'Please confirm that you want to take a snapshot of ' + args.name;
                },
                success: function(args) {
                  return 'Your new snapshot ' + args.name + ' is being created.';
                },
                notification: function(args) {
                  return 'Made snapshot of volume: ' + args.name;
                },
                complete: function(args) {
                  return 'Snapshot ' + args.name + ' is ready.';
                }
              },
              action: function(args) {
				$.ajax({
					url: createURL("createSnapshot&volumeid=" + args.data.id),
					dataType: "json",
					async: true,
					success: function(json) { 			    
					  var jid = json.createsnapshotresponse.jobid; 
					  args.response.success({_custom:{jobId: jid}});							
					}
				});  							
              },
              notification: {
                poll: pollAsyncJobResult
              }
            }			
            
          },
          		  
		  dataProvider: function(args) {        
			$.ajax({
			  url: createURL("listVolumes&page="+args.page+"&pagesize="+pageSize),
			  dataType: "json",
			  async: true,
			  success: function(json) { 				    
				var items = json.listvolumesresponse.volume;			    
				args.response.success({data:items});			                			
			  }
			});  	
		  },
		  
          detailView: {
            name: 'Volume details',
            viewAll: { path: 'storage.snapshots', label: 'Snapshots' },
            actions: {          
			  takeSnapshot: {
                label: 'Take snapshot',
                messages: {
                  confirm: function(args) {
                    return 'Please confirm that you want to take a snapshot of ' + args.name;
                  },
                  success: function(args) {
                    return 'Your new snapshot ' + args.name + ' is being created.';
                  },
                  notification: function(args) {
                    return 'Made snapshot of volume: ' + args.name;
                  },
                  complete: function(args) {
                    return 'Snapshot ' + args.name + ' is ready.';
                  }
                },
                action: function(args) {
				  $.ajax({
					url: createURL("createSnapshot&volumeid=" + args.data.id),
					dataType: "json",
					async: true,
					success: function(json) { 			    
					  var jid = json.createsnapshotresponse.jobid; 
					  args.response.success({_custom:{jobId: jid}});							
					}
				  });  							
                },
                notification: {
                  poll: pollAsyncJobResult
                }
              },
			               
              attachDisk: {
				label: 'Attach Disk',
				messages: {
				  confirm: function(args) {
					return 'Are you sure you want to attach disk?';
				  },
				  success: function(args) {
					return 'Disk is being attached to instance';                
				  },
				  notification: function(args) {
					return 'Attaching disk to instance';
				  },
				  complete: function(args) {
					return 'Disk has been attached to instance';                
				  }
				},
				createForm: {
				  title: 'Attach Disk',
				  desc: 'Attach Disk to Instance',
				  fields: {  
					virtualMachineId: {
					  label: 'Instance',
					  select: function(args) {						    
						var items = [];						   
						$.ajax({							
						  url: createURL("listVirtualMachines&state=Running&zoneid=" + args.context.volumes[0].zoneid + "&domainid=" + args.context.volumes[0].domainid + "&account=" + args.context.volumes[0].account),
						  dataType: "json",
						  async: false,
						  success: function(json) {	                                						
							var instanceObjs= json.listvirtualmachinesresponse.virtualmachine;	
                            $(instanceObjs).each(function() {
							  items.push({id: this.id, description: this.displayname});		
							});                                								
						  }
						});												
						$.ajax({							
						  url: createURL("listVirtualMachines&state=Stopped&zoneid=" + args.context.volumes[0].zoneid + "&domainid=" + args.context.volumes[0].domainid + "&account=" + args.context.volumes[0].account),
						  dataType: "json",
						  async: false,
						  success: function(json) {	                                						
							var instanceObjs= json.listvirtualmachinesresponse.virtualmachine;	
                            $(instanceObjs).each(function() {
							  items.push({id: this.id, description: this.displayname});		
							});                              
						  }
						});							
						args.response.success({data: items});									
					  }				
					}				 
				  }
				},       
				action: function(args) {                       			  
				  $.ajax({
					url: createURL("attachVolume&id=" + args.context.volumes[0].id + '&virtualMachineId=' + args.data.virtualMachineId),			   
					dataType: "json",
					async: true,
					success: function(json) { 			    
					  var jid = json.attachvolumeresponse.jobid;    				
					  args.response.success(
						{_custom:
						  {jobId: jid,
						   getUpdatedItem: function(json) {					     
							 return json.queryasyncjobresultresponse.jobresult.volume;
						   },
						   getActionFilter: function() {
							 return actionfilter;
						   }					 
						  }
						}
					  );									
					}
				  });                   	  
				},				
				notification: {
				  poll: pollAsyncJobResult
				}                 
			  }       
                          
			  ,
              detachDisk: {
				label: 'Detach disk',
				messages: {
				  confirm: function(args) {
					return 'Are you sure you want to detach disk ?';
				  },
				  success: function(args) {
					return 'Disk is being detached.';
				  },
				  notification: function(args) {			
					return 'Detaching disk';
				  },
				  complete: function(args) {			  
					return 'Disk has been detached.';
				  }
				},         
				action: function(args) {	 
                  debugger;				
				  $.ajax({
					url: createURL("detachVolume&id=" + args.context.volumes[0].id),
					dataType: "json",
					async: true,
					success: function(json) {                       			
					  var jid = json.detachvolumeresponse.jobid;    				
					  args.response.success(
						{_custom:
						  {jobId: jid,
						   getUpdatedItem: function(json) {					     
							 return json.queryasyncjobresultresponse.jobresult.volume;
						   },
						   getActionFilter: function() {
							 return actionfilter;
						   }					 
						  }
						}
					  );						  
					}
				  });  	
				},
				notification: {
				  poll: pollAsyncJobResult		
				}
			  }	
                            
            },
            tabs: {
              details: {
                title: 'Details',
                		
				preFilter: function(args) {   		  
                  if(isAdmin()) {
                    args.$form.find('.form-item[rel=storage]').css('display', 'inline-block');                        
                  }
				  else {
					args.$form.find('.form-item[rel=storage]').hide();
				  }					  
                },	
			    
                fields: [
                  {
                    name: { label: 'Name', isEditable: true }
                  },
                  {
                    id: { label: 'ID' },
                    name: { label: 'Name' },
                    zonename: { label: 'Zone' },
					deviceid: { label: 'Device ID' },
					state: { label: 'State' },
					type: { label: 'Type' },
					storagetype: { label: 'Storage Type' },
					storage: { label: 'Storage' },
					size : { 
					  label: 'Size ', 
					  converter: function(args) {					    
						if (args == null || args == 0)
						  return "";
						else
						  return cloudStack.converters.convertBytes(args);                       
                      }				
					},
					virtualmachineid: { 
					  label: 'VM ID',
					  converter: function(args) {					    
						if (args == null)
						  return "detached";
						else
						  return args;                       
                      }		
					},
					vmname: { label: 'VM Name' },
					vmdisplayname: { label: 'VM Display Name' },
					created: { label: 'Created' },
					domain: { label: 'Domain' },
					account: { label: 'Account' }				
                  }
                ],
                
				dataProvider: function(args) {        
				  $.ajax({
					url: createURL("listVolumes&id="+args.id),
					dataType: "json",
					async: true,
					success: function(json) { 				    
					  var items = json.listvolumesresponse.volume;
					  if(items != null && items.length > 0) {
						args.response.success({data:items[0]});		
					  }    			
					}
				  });  	
				}			
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
          
		  //dataProvider: testData.dataProvider.listView('snapshots'),
		  dataProvider: function(args) {        
			$.ajax({
			  url: createURL("listSnapshots&page="+args.page+"&pagesize="+pageSize),
			  dataType: "json",
			  async: true,
			  success: function(json) { 				    
				var items = json.listsnapshotsresponse.snapshot;			   
				args.response.success({data:items});			                			
			  }
			});  	
		  },
		  
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
                    name: { label: 'Name' },
					volumename: { label: 'Volume Name' },
					state: { label: 'State' },
					intervaltype: { label: 'Interval Type' },					
					domain: { label: 'Domain' },
					account: { label: 'Account' },
					created: { label: 'Created' }
                  }
                ],
                //dataProvider: testData.dataProvider.detailView('snapshots')
				dataProvider: function(args) {        
				  $.ajax({
					url: createURL("listSnapshots&id="+args.id),
					dataType: "json",
					async: true,
					success: function(json) { 				    
					  var items = json.listsnapshotsresponse.snapshot;
					  if(items != null && items.length > 0) {
						args.response.success({data:items[0]});		
					  }    			
					}
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