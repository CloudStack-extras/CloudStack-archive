(function(cloudStack, testData) { 

  cloudStack.sections.templates = {
    title: 'Templates',
    id: 'templates',
    sectionSelect: {
      label: 'Select view'
    },
    sections: {
      templates: {
        type: 'select',
        title: 'Templates',
        listView: {
          id: 'templates',
          label: 'Templates',
          fields: {
            name: { label: 'Name', editable: true },
            id: { label: 'ID' },
            zonename: { label: 'Zone' },
            hypervisor: { label: 'Hypervisor' }
          },
          actions: {		    
			add: {
              label: 'Create template',			   		  

              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to create a template?';
                },
                success: function(args) {
                  return 'Your new template is being created.';
                },
                notification: function(args) {
                  return 'Creating new template';
                },
                complete: function(args) {
                  return 'Template has been created successfully!';
                }
              },

              createForm: {
                title: 'Create template',
                desc: 'Please fill in the following data to create a new template.',                
                preFilter: cloudStack.preFilter.createTemplate,	               
                fields: {
                  name: {
                    label: 'Name',
                    validation: { required: true }
                  },
                  description: {
                    label: 'Description',
                    validation: { required: true }
                  },
                  url: {
                    label: 'URL',
                    validation: { required: true }
                  },
                  zone: {
                    label: 'Zone',
                    select: function(args) {	
                      $.ajax({
						url: createURL("listZones&available=true"),			 
						dataType: "json",
						async: true,
						success: function(json) { 				   
						  var zoneObjs = json.listzonesresponse.zone;							  
						  var items = [];
						  if (isAdmin()) 
						    items.push({id: -1, description: "All Zones"});
						  $(zoneObjs).each(function() {
						    items.push({id: this.id, description: this.name});						  
						  });			  
						  args.response.success({data: items});					  
						}
					  });  						 
					}		
                  },     
                  hypervisor: {
                    label: 'Hypervisor',
                    dependsOn: 'zone',
                    select: function(args) {     
                      if(args.zone == null)                           
	                    return; 	                    
                	    
	                  var apiCmd;
	                  if(args.zone == -1) 	
	                    apiCmd = "listHypervisors&zoneid=-1";	 
	                  else	  
	                    apiCmd = "listHypervisors&zoneid=" + args.zone;	  	    
                	    
	                  $.ajax({
                        url: createURL(apiCmd),
                        dataType: "json",
						async: false,
                        success: function(json) {            
                          var hypervisorObjs = json.listhypervisorsresponse.hypervisor;
						  var items = [];
						  $(hypervisorObjs).each(function(){
							items.push({id: this.name, description: this.name});
						  });								
                          args.response.success({data: items});	
                        }    
                      });   
					}		
                  },        
                  format: {
                    label: 'Format',
                    dependsOn: 'hypervisor',
                    select: function(args) {     
                      var items = [];                      
                      if(args.hypervisor == "XenServer") {
	                    //formatSelect.append("<option value='VHD'>VHD</option>");	  
	                    items.push({id:'VHD', description: 'VHD'});
	                  }  
	                  else if(args.hypervisor == "VMware") {
	                    //formatSelect.append("<option value='OVA'>OVA</option>");
	                    items.push({id:'OVA', description: 'OVA'});
	                  }
	                  else if(args.hypervisor == "KVM") {
	                    //formatSelect.append("<option value='QCOW2'>QCOW2</option>");
	                    items.push({id:'QCOW2', description: 'QCOW2'});
	                  }
		              else if(args.hypervisor == "BareMetal") {
	                    //formatSelect.append("<option value='BareMetal'>BareMetal</option>");
	                    items.push({id:'BareMetal', description: 'BareMetal'});
	                  }
		              else if(args.hypervisor == "Ovm") {
	                    //formatSelect.append("<option value='RAW'>RAW</option>");  
	                    items.push({id:'RAW', description: 'RAW'});  
	                  }	                    
	                  args.response.success({data: items});	                	        
					}		
                  },        
                                    
                  osTypeId: {
                    label: 'OS Type',
                    select: function(args) {	
                      $.ajax({
						url: createURL("listOsTypes"),			 
						dataType: "json",
						async: true,
						success: function(json) { 				   
						  var items = json.listostypesresponse.ostype;						  
						  args.response.success({data: items});					  
						}
					  });  						 
					}		
                  },
                  
                  isExtractable: {
                    label: "Extractable",
                    isBoolean: true
                  },
                  
                  isPasswordEnabled: {
                    label: "Password Enabled",
                    isBoolean: true
                  },
                  
                  isPublic: {
                    label: "Public",
                    isBoolean: true,
					isHidden: true
                  },
                  
                  isFeatured: {
                    label: "Featured",
                    isBoolean: true,
					isHidden: true
                  }                  				  
                }
              },
              
              action: function(args) {					
				var array1 = [];				
				array1.push("&name=" + todb(args.data.name));				
				array1.push("&displayText=" + todb(args.data.description));				
				array1.push("&url=" + todb(args.data.url));				
				array1.push("&zoneid=" + args.data.zone);					
				array1.push("&format=" + args.data.format);		
				array1.push("&isextractable=" + (args.data.isExtractable=="on"));					
				array1.push("&passwordEnabled=" + (args.data.isPasswordEnabled=="on"));					
				array1.push("&osTypeId=" + args.data.osTypeId);			
				array1.push("&hypervisor=" + args.data.hypervisor);
								        
				if(args.$form.find('.form-item[rel=isPublic]').css("display") != "none")
				  array1.push("&ispublic=" + (args.data.isPublic == "on"));	
				if(args.$form.find('.form-item[rel=isFeatured]').css("display") != "none")
				  array1.push("&isfeatured=" + (args.data.isFeatured == "on")); 			
				
				$.ajax({
				  url: createURL("registerTemplate" + array1.join("")),
				  dataType: "json",
				  success: function(json) {					    
					var items = json.registertemplateresponse.template;	 //items might have more than one array element if it's create templates for all zones.			       
				    args.response.success({data:items[0]});	
					/*                        
                    if(items.length > 1) {                               
                      for(var i=1; i<items.length; i++) {   
                        var $midmenuItem2 = $("#midmenu_item").clone();
                        templateToMidmenu(items[i], $midmenuItem2);
                        bindClickToMidMenu($midmenuItem2, templateToRightPanel, templateGetMidmenuId); 
                        $("#midmenu_container").append($midmenuItem2.show());
                      }                                    
                    }  
                    */						
				  }, 
				  error: function(XMLHttpResponse) {	
					var errorMsg = parseXMLHttpResponse(XMLHttpResponse); 
					args.response.error(errorMsg);		
				  }						
				});						
			  },			

              notification: {                
				poll: function(args) {			  
                  args.complete();
                }		
              }
            },			
			    
            edit: {
              label: 'Edit template name',
              action: function(args) {
                args.response.success(args.data[0]);
              }
            }
          },
		            
		  dataProvider: function(args) {        
			$.ajax({
			  url: createURL("listTemplates&templatefilter=self&page="+args.page+"&pagesize="+pageSize),
			  dataType: "json",
			  async: true,
			  success: function(json) { 	
				var items = json.listtemplatesresponse.template;			    
				args.response.success({
				  actionFilter: templateActionfilter,
				  data: items
				});				                			
			  }
		    });  	
		  }	,
          
          detailView: {
            name: 'Template details',            
            actions: {			  
			  copyTemplate: {
				label: 'Copy template',
				messages: {     
				  confirm: function(args) {			
					return 'Are you sure you want to copy template?';
				  },
				  success: function(args) {
					return 'Template is being copied.';
				  },
				  notification: function(args) {			
					return 'Copying template';
				  },
				  complete: function(args) {			  
					return 'Template has been copied.';
				  }
				}, 
				createForm: {
				  title: 'Copy template',
				  desc: '',
				  fields: {  
					destinationZoneId: {
					  label: 'Destination zone',
					  select: function(args) {	                        					  
						$.ajax({
						  url: createURL("listZones&available=true"),			 
						  dataType: "json",
						  async: true,
						  success: function(json) {                        					  
							var zoneObjs = json.listzonesresponse.zone;		
							var items = [];								
							$(zoneObjs).each(function() {	
                              if(this.id != args.context.templates[0].zoneid)						  
							      items.push({id: this.id, description: this.name});	 
							});	                        						
							args.response.success({data: items});					  
						  }
						});  		
					  }				
					}				 
				  }
				},        			
				action: function(args) {                  			
				  $.ajax({		
					url: createURL("copyTemplate&id=" + args.context.templates[0].id + "&sourcezoneid=" + args.context.templates[0].zoneid + "&destzoneid=" + args.data.destinationZoneId),
					dataType: "json",
					async: true,
					success: function(json) {  	                                    
					  var jid = json.copytemplateresponse.jobid;    				
					  args.response.success(
						{_custom:
						  {jobId: jid,
						   getUpdatedItem: function(json) {								 
							 return {}; //nothing in this template needs to be updated
						   },
						   getActionFilter: function() {
							 return templateActionfilter;							 
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
			  	
              downloadTemplate: {
				label: 'Download template',
				messages: {
				  confirm: function(args) {
					return 'Are you sure you want to download template ?';
				  },
				  success: function(args) {
					return 'Template is being downloaded.';
				  },
				  notification: function(args) {			
					return 'Downloading template';
				  },
				  complete: function(args) {                			  
                    var url = decodeURIComponent(args.url);		                          
                    var htmlMsg = 'Please click <a href="#">00000</a> to download template';		                            
                    var htmlMsg2 = htmlMsg.replace(/#/, url).replace(/00000/, url);   
					return htmlMsg2;
				  }
				},         
				action: function(args) {	                   		
				  $.ajax({
					url: createURL("extractTemplate&id=" + args.context.templates[0].id + "&zoneid=" + args.context.templates[0].zoneid + "&mode=HTTP_DOWNLOAD"),
					dataType: "json",
					async: true,
					success: function(json) {                       			
					  var jid = json.extracttemplateresponse.jobid;    				
					  args.response.success(
						{_custom:
						  {jobId: jid,
						   getUpdatedItem: function(json) {					     
							 return {}; //nothing in this template needs to be updated
						   },
						   getActionFilter: function() {
							 return templateActionfilter;
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
                
                /*				
				preFilter: function(args) {   		  
                  if(isAdmin()) {
                    args.$form.find('.form-item[rel=storage]').css('display', 'inline-block');                        
                  }
				  else {
					args.$form.find('.form-item[rel=storage]').hide();
				  }					  
                },	
			    */
				
                fields: [
                  {
                    name: { label: 'Name', isEditable: true }
                  },
                  {
                    id: { label: 'ID' },                    
					zonename: { label: 'Zone name' },
                    zoneid: { label: 'Zone ID' },
					
					displaytext: { label: 'Description', isEditable: true },
					hypervisor: { label: 'Hypervisor' },
					templatetype: { label: 'Template Type' },
					isready: { label: 'Ready', converter:cloudStack.converters.toBooleanText },
					status: { label: 'Status' },
					size : { 
					  label: 'Size', 
					  converter: function(args) {					    
						if (args == null || args == 0)
						  return "";
						else
						  return cloudStack.converters.convertBytes(args);                       
                      }				
					},
					isextractable: { 
					  label: 'Extractable', 
					  converter:cloudStack.converters.toBooleanText 
					},
					passwordenabled: { 
					  label: 'Password Enabled', 
					  isEditable: true,
					  converter:cloudStack.converters.toBooleanText
					},
					ispublic: { 
					  label: 'Public', 
					  isEditable: true,
					  converter:cloudStack.converters.toBooleanText 
					},
					isfeatured: { 
					  label: 'Featured', 
					  isEditable: true,
					  converter:cloudStack.converters.toBooleanText 
					},
					crossZones: { 
					  label: 'Cross Zones', 
					  converter:cloudStack.converters.toBooleanText 
					},
					
					ostypeid: {
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
					
					domain: { label: 'Domain' },
					account: { label: 'Account' },
					created: { label: 'Created' }								
                  }
                ],
                
				dataProvider: function(args) {	                  	
				  args.response.success(
					{
					  actionFilter: templateActionfilter,
					  data:args.jsonObj
					}
				  );	
				}			
              }		  
            }
          }		  
        }
      },
      isos: {
        type: 'select',
        title: 'ISOs',
        listView: {
          label: 'ISOs',
          fields: {
            displaytext: { label: 'Name' },
            id: { label: 'ID' },
            size: { label: 'Size' },
            zonename: { label: 'Zone' }
          },		  
          actions: {	
			add: {
              label: 'Create ISO',			   		  

              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to create a ISO?';
                },
                success: function(args) {
                  return 'Your new ISO is being created.';
                },
                notification: function(args) {
                  return 'Creating new ISO';
                },
                complete: function(args) {
                  return 'ISO has been created successfully!';
                }
              },

              createForm: {
                title: 'Create ISO',
                desc: 'Please fill in the following data to create a new ISO.',                
                preFilter: cloudStack.preFilter.createTemplate,	             
                fields: {
                  name: {
                    label: 'Name',
                    validation: { required: true }
                  },
                  description: {
                    label: 'Description',
                    validation: { required: true }
                  },
                  url: {
                    label: 'URL',
                    validation: { required: true }
                  },
                  zone: {
                    label: 'Zone',
                    select: function(args) {	
                      $.ajax({
						url: createURL("listZones&available=true"),			 
						dataType: "json",
						async: true,
						success: function(json) { 				   
						  var zoneObjs = json.listzonesresponse.zone;							  
						  var items = [];
						  if (isAdmin()) 
						    items.push({id: -1, description: "All Zones"});
						  $(zoneObjs).each(function() {
						    items.push({id: this.id, description: this.name});						  
						  });			  
						  args.response.success({data: items});					  
						}
					  });  						 
					}		
                  },                                    
                                      
                  isBootable: {
                    label: "Bootable",
                    isBoolean: true
                  },
                                                       
                  osTypeId: {
                    label: 'OS Type',  
					dependsOn: 'isBootable',
					isHidden: true,
                    validation: { required: true },					
                    select: function(args) {                                   
                      $.ajax({
						url: createURL("listOsTypes"),			 
						dataType: "json",
						async: true,
						success: function(json) { 				   
						  var osTypeObjs = json.listostypesresponse.ostype;		
						  var items = [];
						  items.push({id: "", description: "None"});
						  $(osTypeObjs).each(function(){
						    items.push({id: this.id, description: this.description});						  
						  });						  				  
						  args.response.success({data: items});					  
						}
					  });	  						  		 
					}		
                  },
                  
                  isExtractable: {
                    label: "Extractable",
                    isBoolean: true
                  },
                 
                  isPublic: {
                    label: "Public",
                    isBoolean: true,
					isHidden: true
                  },
                  
                  isFeatured: {
                    label: "Featured",
                    isBoolean: true,
					isHidden: true
                  }                  				  
                }
              },
              
              action: function(args) {	
				var array1 = [];			   
			    array1.push("&name=" + todb(args.data.name));			    			    
			    array1.push("&displayText=" + todb(args.data.description));					   
			    array1.push("&url=" + todb(args.data.url));					    								   
			    array1.push("&zoneid=" + args.data.zone);				    			   
				array1.push("&isextractable=" + (args.data.isExtractable=="on"));				    					 	   
			    array1.push("&bootable=" + (args.data.isBootable=="on"));	
			                   		   
				if(args.$form.find('.form-item[rel=osTypeId]').css("display") != "none")
			        array1.push("&osTypeId=" + args.data.osTypeId);		
			              		        
				if(args.$form.find('.form-item[rel=isPublic]').css("display") != "none")
				  array1.push("&ispublic=" + (args.data.isPublic == "on"));	
				if(args.$form.find('.form-item[rel=isFeatured]').css("display") != "none")
				  array1.push("&isfeatured=" + (args.data.isFeatured == "on")); 
    		       		    				
			    $.ajax({
			      url: createURL("registerIso" + array1.join("")),
				  dataType: "json",
				  success: function(json) {					
				    var items = json.registerisoresponse.iso;	//items might have more than one array element if it's create ISOs for all zones.			       
				    args.response.success({data:items[0]});	
						
                    /*						
					if(items.length > 1) {                               
					  for(var i=1; i<items.length; i++) {   
						var $midmenuItem2 = $("#midmenu_item").clone();
						ISOToMidmenu(items[i], $midmenuItem2);
						bindClickToMidMenu($midmenuItem2, templateToRightPanel, ISOGetMidmenuId); 
						$("#midmenu_container").append($midmenuItem2.show());						  }                                    
					  }  	
                    */						
				  }, 
			      error: function(XMLHttpResponse) {
					var errorMsg = parseXMLHttpResponse(XMLHttpResponse); 
					args.response.error(errorMsg);		
				  }				
			    });				
			  },			

              notification: {                
				poll: function(args) {			  
                  args.complete();
                }		
              }
            }				
          },	
		  
		  dataProvider: function(args) {        
			$.ajax({
			  url: createURL("listIsos&isofilter=self&page="+args.page+"&pagesize="+pageSize),
			  dataType: "json",
			  async: true,
			  success: function(json) { 	
				var items = json.listisosresponse.iso;			    
				args.response.success({data:items});		                			
			  }
			});  	
		  },

          //???
          detailView: {
            name: 'ISO details',            
            actions: {
			  /*
              edit: {
                label: 'Edit ISO details',
                action: function(args) {
                  args.response.success();
                },
                notification: {
                  poll: testData.notifications.testPoll
                }
              },
			  */              
            },
            tabs: {
              details: {
                title: 'Details',
                
                /*				
				preFilter: function(args) {   		  
                  if(isAdmin()) {
                    args.$form.find('.form-item[rel=storage]').css('display', 'inline-block');                        
                  }
				  else {
					args.$form.find('.form-item[rel=storage]').hide();
				  }					  
                },	
			    */
				
                fields: [
                  {
                    name: { label: 'Name', isEditable: true }
                  },
                  {
                    id: { label: 'ID' },                    
					zonename: { label: 'Zone name' },
                    zoneid: { label: 'Zone ID' },					
					displaytext: { label: 'Description', isEditable: true },	
					isready: { label: 'Ready', converter:cloudStack.converters.toBooleanText },
					status: { label: 'Status' },
					size : { 
					  label: 'Size', 
					  converter: function(args) {					    
						if (args == null || args == 0)
						  return "";
						else
						  return cloudStack.converters.convertBytes(args);                       
                      }				
					},
					isextractable: { 
					  label: 'Extractable', 
					  converter:cloudStack.converters.toBooleanText 
					},
					bootable: { 
					  label: 'Bootable', 
					  isEditable: true,
					  converter:cloudStack.converters.toBooleanText
					},
					ispublic: { 
					  label: 'Public', 
					  isEditable: true,
					  converter:cloudStack.converters.toBooleanText 
					},
					isfeatured: { 
					  label: 'Featured', 
					  isEditable: true,
					  converter:cloudStack.converters.toBooleanText 
					},
					crossZones: { 
					  label: 'Cross Zones', 
					  converter:cloudStack.converters.toBooleanText 
					},
					
					ostypeid: {
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
					
					domain: { label: 'Domain' },
					account: { label: 'Account' },
					created: { label: 'Created' }								
                  }
                ],
                
				dataProvider: function(args) {	                  	
				  args.response.success(
					{
					  actionFilter: isoActionfilter,
					  data:args.jsonObj
					}
				  );	
				}			
              }		  
            }
          }		         
        }
      }
    }
  };  
    
  var templateActionfilter = function(args) {	    
    var jsonObj = args.context.item;
	var allowedActions = [];	
    allowedActions.push("copyTemplate");	
    allowedActions.push("downloadTemplate");		
    return allowedActions;
  }

  var isoActionfilter = function(args) {	    		  
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
  
})(cloudStack, testData);
