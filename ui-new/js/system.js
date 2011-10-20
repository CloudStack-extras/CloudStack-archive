(function($, cloudStack, testData) {

  var zoneObjs, podObjs, clusterObjs, domainObjs;
  var selectedClusterObj;

  cloudStack.sections.system = {
    title: 'System',
    id: 'system',
    sectionSelect: {
      label: 'Select view'
    },
    sections: {
      physicalResources: {
        type: 'select',
        title: 'Physical Resources',
        listView: {
          id: 'physicalResources',
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
				  steps: [
                    // Step 1: Setup
                    null,

                    // Step 2: Setup Zone
                    function(args) {					  				  
					  $.ajax({
						url: createURL("listDomains"),				
						dataType: "json",
						async: false,
						success: function(json) {					   
						  domainObjs = json.listdomainsresponse.domain;						
						}
					  });						
                      args.response.success({domains: domainObjs});
                    },

                    // Step 3: Setup Pod
                    null,

                    // Step 4: Setup IP Range
                    function(args) {
                      args.response.success({domains: domainObjs});
                    }
                  ],				
				
                  action: function(args) {				   
					var array1 = [];	
	
					//var networktype = $thisWizard.find("#step1").find("input:radio[name=basic_advanced]:checked").val();  //"Basic", "Advanced"
					var networktype = args.data["network-model"];	
					array1.push("&networktype=" + todb(networktype));
					
					array1.push("&name=" + todb(args.data.name));	
					
					array1.push("&dns1=" + todb(args.data.dns1));	
					
					var dns2 = args.data.dns2;
					if (dns2 != null && dns2.length > 0) 
						array1.push("&dns2=" + todb(dns2));		
						
					array1.push("&internaldns1="+todb(args.data.internaldns1));
					
					var internaldns2 = args.data.internaldns2;
					if (internaldns2 != null && internaldns2.length > 0) 
						array1.push("&internaldns2=" + todb(internaldns2));		

					if(networktype == "Advanced") {
					    if(args.data["isolation-mode"] == "security-groups") {
						    array1.push("&securitygroupenabled=true"); 
                        }							
						else { //args.data["isolation-mode"] == "vlan"
						    array1.push("&securitygroupenabled=false");  
														
							var vlanStart = args.data["vlan-range-start"];
							if(vlanStart != null && vlanStart.length > 0) {	
								var vlanEnd = args.data["vlan-range-end"];						
								if (vlanEnd != null && vlanEnd.length > 0) 
									array1.push("&vlan=" + todb(vlanStart + "-" + vlanEnd));									
								else 							
									array1.push("&vlan=" + todb(vlanStart));		
							}													
							
							var guestcidraddress = args.data["guest-cidr"];
							if(guestcidraddress != null && guestcidraddress.length > 0) {								
								array1.push("&guestcidraddress="+todb(guestcidraddress));	
							}				
						}
					}						
					                    				
					if(args.data["public"] == null) //public checkbox is unchecked
					  array1.push("&domainid=" + args.data["zone-domain"]);		
													
					var zoneId, podId;										
					$.ajax({
						url: createURL("createZone" + array1.join("")),
						dataType: "json",
						async: false,
						success: function(json) {	
							var item = json.createzoneresponse.zone;	
                            args.response.success({data:item});	
							
							zoneId = item.id;	
							
							//listZonesUpdate();	
							//g_directAttachSecurityGroupsEnabled = true;
							//$("#leftmenu_security_group_container").show();
							
                            /*							
							$.ajax({
								data: createURL("command=listCapabilities"),
								dataType: "json",
								async: false,
								success: function(json) {										
									g_supportELB = json.listcapabilitiesresponse.capability.supportELB;					    
									$.cookie('supportELB', g_supportELB, { expires: 1}); 
														
									if(g_supportELB == "guest")  //ips are allocated on guest network
										bindAndListMidMenuItems($("#leftmenu_ip"), "listPublicIpAddresses&forvirtualnetwork=false&forloadbalancing=true", ipGetSearchParams, "listpublicipaddressesresponse", "publicipaddress", "jsp/ipaddress.jsp", afterLoadIpJSP, ipToMidmenu, ipToRightPanel, ipGetMidmenuId, false);
									else if(g_supportELB == "public")  //ips are allocated on public network
										bindAndListMidMenuItems($("#leftmenu_ip"), "listPublicIpAddresses&forvirtualnetwork=true&forloadbalancing=true", ipGetSearchParams, "listpublicipaddressesresponse", "publicipaddress", "jsp/ipaddress.jsp", afterLoadIpJSP, ipToMidmenu, ipToRightPanel, ipGetMidmenuId, false);
									else			
										bindAndListMidMenuItems($("#leftmenu_ip"), "listPublicIpAddresses", ipGetSearchParams, "listpublicipaddressesresponse", "publicipaddress", "jsp/ipaddress.jsp", afterLoadIpJSP, ipToMidmenu, ipToRightPanel, ipGetMidmenuId, false); //remove "&forvirtualnetwork=true" for advanced zone whose security group is enabled
								}		
							});  
							*/
							
						},
						error: function(XMLHttpResponse) {     
							var errorMsg = parseXMLHttpResponse(XMLHttpResponse); 
						    args.response.error(errorMsg);		
						}
					});
				
					if(zoneId != null) {   
						// create pod (begin) 
						var array1 = [];
						array1.push("&zoneId=" + zoneId);
						
						//var name = trim($thisWizard.find("#add_pod_name").val());						
						array1.push("&name=" + todb(args.data["pod-name"]));
						
						//var netmask = trim($thisWizard.find("#add_pod_netmask").val());						
						array1.push("&netmask=" + todb(args.data["pod-netmask"]));
						
						//var startip = trim($thisWizard.find("#add_pod_startip").val());						
						array1.push("&startIp=" + todb(args.data["pod-ip-range-start"]));
						
						//var endip = trim($thisWizard.find("#add_pod_endip").val());	    //optional
						var endip = args.data["pod-ip-range-end"];
						if (endip != null && endip.length > 0)
							array1.push("&endIp=" + todb(endip));
							
						//var gateway = trim($thisWizard.find("#add_pod_gateway").val());	                       					
						array1.push("&gateway=" + todb(args.data["pod-gateway"]));			
										
						$.ajax({
							url: createURL("createPod"+array1.join("")), 
							dataType: "json",
							async: false,
							success: function(json) {	
								var item = json.createpodresponse.pod; 	
								args.response.success({data:item});									
								podId = item.id;	
							},
							error: function(XMLHttpResponse) {	
								var errorMsg = parseXMLHttpResponse(XMLHttpResponse); 
						        args.response.error(errorMsg);		
							}
						});	
						// create pod (end) 
												
					
						// create direct VLAN (basic zone, advanced zone + security group)
						//var $createDirectVlan = $thisWizard.find("#step4").find("#create_direct_vlan");
						//if ($createDirectVlan.css("display") != "none") {
						if(networktype == "Basic" || (networktype == "Advanced" && args.data["isolation-mode"] == "security-groups")) {
						    var array1 = [];
													    
							array1.push("&gateway=" + todb(args.data["guest-gateway"]));
							array1.push("&netmask=" + todb(args.data["guest-netmask"]));								
							array1.push("&startip=" + todb(args.data["guest-ip-range-start"]));
							
							var endip = args.data["guest-ip-range-end"];	
							if(endip != null && endip.length > 0)
								array1.push("&endip=" + todb(endip));														
							
							array1.push("&forVirtualNetwork=false"); //direct VLAN	
							array1.push("&zoneid=" + zoneId);
							
							var isValid = true;							
							if(networktype == "Basic") { //Basic zone (default VLAN is at pod-level)
								array1.push("&vlan=untagged");	
								array1.push("&podId=" + podId);	
								if(podId == null)
									isValid = false;		        
							}	
							else { //Advanced zone + security group  (default VLAN is at zone-level)	      
								array1.push("&vlan=" + args.data["vlan-id"]);
							}							
							  							
							if(isValid) {
								$.ajax({
									url: createURL("createVlanIpRange" + array1.join("")),
									dataType: "json",
									async: false,
									success: function(json) {                                         								
										var item = json.createvlaniprangeresponse.vlan;
										args.response.success({data:item});													
									},		   
									error: function(XMLHttpResponse) {	
										var errorMsg = parseXMLHttpResponse(XMLHttpResponse); 
						                args.response.error(errorMsg);			
									}
								});	
							}	            
						}
						
						
						// create virtual VLAN (advanced zone + virtual)
						//var $createVirtualVlan = $thisWizard.find("#step4").find("#create_virtual_vlan");
						//if ($createVirtualVlan.css("display") != "none") {  
						else if (networktype == "Advanced" && args.data["isolation-mode"] == "vlan") {
							var array1 = [];								
							//if ($createVirtualVlan.find("#add_publicip_vlan_tagged").val() == "tagged") 							
							if(args.data["vlan-type"] == "tagged") {
								array1.push("&vlan=" + args.data["vlan-id"]);									
								if(args.data["ip-scope-tagged"] == "account-specific") {
									array1.push("&domainId=" + args.data["ip-domain"]);							
									array1.push("&account=" + args.data.account);
								}								
                            }								
							else { //args.data["vlan-type"] == "untagged"
								array1.push("&vlan=untagged");						
							}																				
							
							array1.push("&gateway=" + todb(args.data["guest-gateway"]));	
							array1.push("&netmask=" + todb(args.data["guest-netmask"]));					
							array1.push("&startip=" + todb(args.data["guest-ip-range-start"]));
							
							var endip = args.data["guest-ip-range-end"];	//optional field (might be empty)
							if(endip != null && endip.length > 0)
								array1.push("&endip=" + todb(endip));										
							
							$.ajax({
								url: createURL("createVlanIpRange&forVirtualNetwork=true&zoneId=" + zoneId + array1.join("")),
								dataType: "json",
								success: function(json) {	
									var item = json.createvlaniprangeresponse.vlan;	
                                    args.response.success({data:item});
								},
								error: function(XMLHttpResponse) {				    
									var errorMsg = parseXMLHttpResponse(XMLHttpResponse); 
						            args.response.error(errorMsg);		
								}
							});   
						}						
					}                     
                  }
                })
              },
              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to add ' + args.name + '?';
                },
                notification: function(args) {
                  return 'Created new zone';
                }
              },
              notification: {
                poll: testData.notifications.testPoll
              }
            }
          },
          
          dataProvider: function(args) {  
            $.ajax({
              url: createURL("listZones&page=" + args.page + "&pagesize=" + pageSize),
              dataType: "json",
              async: true,
              success: function(json) { 
                zoneObjs = json.listzonesresponse.zone;         
                args.response.success({data:zoneObjs});                           
              }
            });   
          },
          detailView: {
            pageGenerator: cloudStack.zoneChart({              
              dataProvider: function(args) {                             
                args.response.success({data:args.jsonObj}); 
              },        
              detailView: {
                name: 'Zone details',
                viewAll: { path: '_zone.pods', label: 'Pods' },
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
                  },
                  resources: {
                    title: 'Resources',
                    fields: [
                      {
                        iptotal: { label: 'Total IPs' },
                        cputotal: { label: 'Total CPU' },
                        bandwidthout: { label: 'Bandwidth (Out)'},
                        bandwidthin: { label: 'Bandwidth (In)'}
                      }
                    ],
                    dataProvider: function(args) {
                      setTimeout(function() {
                        args.response.success({
                          data: {
                            iptotal: 1000,
                            cputotal: '500 Ghz',
                            bandwidthout: '14081 mb',
                            bandwidthin: '31000 mb'
                          }
                        });
                      }, 500);
                    }
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
              id: 'networks',
              fields: {
                name: { label: 'Name' },
                startip: { label: 'Start IP' },
                endip: { label: 'End IP' },
                type: { label: 'Type' }
              },
              
              //dataProvider: testData.dataProvider.listView('networks'),
              dataProvider: function(args) {                   
                var zoneObj = args.context.zones[0];              
                var networkArray = [];
                var showPublicNetwork = true;
                if(zoneObj.networktype == "Basic") {   
                  //$("#add_network_button").hide();  
                  $.ajax({
                    url: createURL("listExternalFirewalls&zoneid=" + zoneObj.id),
                    dataType: "json",
                    async: false,
                    success: function(json) {            
                      var items = json.listexternalfirewallsresponse.externalfirewall;           
                      if(items != null && items.length > 0) {
                        showPublicNetwork = true;
                        //$("#add_iprange_button,#tab_ipallocation").show();
                      }
                      else {
                        showPublicNetwork = false;  
                        //$("#add_iprange_button,#tab_ipallocation").hide();                  
                      }
                    }
                  });         
                }
                else { // "Advanced"  
                  showPublicNetwork = true;
                  //$("#add_network_button,#add_iprange_button,#tab_ipallocation").show();  
                  
                  //listMidMenuItems2(("listNetworks&type=Direct&zoneId="+zoneObj.id), networkGetSearchParams, "listnetworksresponse", "network", directNetworkToMidmenu, directNetworkToRightPanel, directNetworkGetMidmenuId, false, 1);
                  $.ajax({
                    url: createURL("listNetworks&type=Direct&zoneId=" + args.ref.zoneID + "&page=" + args.page + "&pagesize=" + pageSize),
                    dataType: "json",
                    async: false,
                    success: function(json) {             
                      networkArray = json.listnetworksresponse.network;           
                      //args.response.success({data:items});                            
                    }
                  });           
                }
                
                if(showPublicNetwork == true && zoneObj.securitygroupsenabled == false) { //public network           
                  //$midmenuContainer.find("#midmenu_container_no_items_available").remove();  //There is always at least one item (i.e. public network) in middle menu. So, "no items available" shouldn't be in middle menu even there is zero direct network item in middle menu.   
                  $.ajax({
                    url: createURL("listNetworks&trafficType=Public&isSystem=true&zoneId=" + zoneObj.id),
                    dataType: "json",
                    async: false,
                    success: function(json) {       
                      var items = json.listnetworksresponse.network;       
                      if(items != null && items.length > 0) {
                        var item = items[0];
                        //var $midmenuItem1 = $("#midmenu_item").clone();                      
                        //$midmenuItem1.data("toRightPanelFn", publicNetworkToRightPanel);  //to implement later                           
                        //publicNetworkToMidmenu(item, $midmenuItem1);    
                        //bindClickToMidMenu($midmenuItem1, publicNetworkToRightPanel, publicNetworkGetMidmenuId);   
                        
                        //$midmenuContainer.prepend($midmenuItem1.show());    //prepend public network on the top of middle menu
                        networkArray.unshift(item);
                        
                        //$midmenuItem1.click();  
                      }
                    }
                  });  
                }
                else if (showPublicNetwork == true && zoneObj.securitygroupsenabled == true){
                  //$midmenuContainer.find("#midmenu_container_no_items_available").remove();  //There is always at least one item (i.e. public network) in middle menu. So, "no items available" shouldn't be in middle menu even there is zero direct network item in middle menu.   
                  $.ajax({
                    data: createURL("command=listNetworks&type=Direct&trafficType=Guest&isSystem=true&zoneId="+zoneObj.id),
                    dataType: "json",
                    async: false,
                    success: function(json) {       
                      var items = json.listnetworksresponse.network;       
                      if(items != null && items.length > 0) {
                        var item = items[0];
                        //var $midmenuItem1 = $("#midmenu_item").clone();                      
                        //$midmenuItem1.data("toRightPanelFn", publicNetworkToRightPanel);                             
                        //publicNetworkToMidmenu(item, $midmenuItem1);    
                        //bindClickToMidMenu($midmenuItem1, publicNetworkToRightPanel, publicNetworkGetMidmenuId);   
                        
                        //$midmenuContainer.prepend($midmenuItem1.show());    //prepend public network on the top of middle menu
                        networkArray.unshift(item);
                        
                        //$midmenuItem1.click();  
                      }
                    }
                  });  
                }
                else {
                  //publicNetworkToRightPanel(null);  
                }       
                args.response.success({data:networkArray});                  
              },  
              
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
            title: 'Pods',
            listView: {
              id: 'pods',
              section: 'pods',
              fields: {
                name: { label: 'Name' },
                startip: { label: 'Start IP' },
                endip: { label: 'End IP' },
                allocationstate: { label: 'Status' }
              },
              
              //dataProvider: testData.dataProvider.listView('pods'),
              dataProvider: function(args) {                        
                $.ajax({
                  url: createURL("listPods&zoneid=" + args.ref.zoneID + "&page=" + args.page + "&pagesize=" + pageSize),
                  dataType: "json",
                  async: true,
                  success: function(json) {             
                    var items = json.listpodsresponse.pod;            
                    args.response.success({data:items});                            
                  }
                });   
              },  
              
			  //???
             actions: {
                add: {
                  label: 'Add pod',

                  createForm: {
                    title: 'Add new pod',
                    desc: 'Please fill in the following information to add a new pod',
                   
                    preFilter: function(args) {                      				  
                      var $guestFields = args.$form.find('.form-item[rel=guestGateway], .form-item[rel=guestNetmask], .form-item[rel=startGuestIp], .form-item[rel=endGuestIp]');                      					  
                      if (args.context.zones[0].networktype == "Basic") {
                        $guestFields.css('display', 'inline-block');                        
                      }
					  else if(args.context.zones[0].networktype == "Advanced") { //advanced-mode network (zone-wide VLAN)
					    $guestFields.hide();
					  }					  
                    },					
                    
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
                      startip: {
                        label: 'Start IP',
                        validation: { required: true }
                      },
                      endip: {
                        label: 'End IP',
                        validation: { required: false }
                      },
                           
						   
                      guestGateway: {
                        label: 'Guest Gateway',
                        validation: { required: true },
                        isHidden: true
                      },
                      guestNetmask: {
                        label: 'Guest Netmask',
                        validation: { required: true },
                        isHidden: true
                      },
                      startGuestIp: {
                        label: 'Start Guest IP',
                        validation: { required: true },
                        isHidden: true
                      },
                      endGuestIp: {
                        label: 'End Guest IP',
                        validation: { required: false },
                        isHidden: true
                      }	  
                    }
                  },

                  action: function(args) {                     				                  
                    var array1 = [];
                    array1.push("&zoneId=" + args.context.zones[0].id);
                    array1.push("&name=" + todb(args.data.name));
                    array1.push("&netmask=" + todb(args.data.netmask));
                    array1.push("&startIp=" + todb(args.data.startip));
					
					var endip = args.data.endip;	    //optional
                    if (endip != null && endip.length > 0)
                        array1.push("&endIp=" + todb(endip));
						
                    array1.push("&gateway=" + todb(args.data.gateway));			
    								
		            $.ajax({
		                url: createURL("createPod" + array1.join("")), 
			            dataType: "json",
			            success: function(json) {						    		              
			                var item = json.createpodresponse.pod; 	
							args.response.success({data:item});		
                            var podId = item.id;							
    		                   				        	            
				            //Create IP Range 
                            //if($thisDialog.find("#guestip_container").css("display") != "none") { 
							if(args.context.zones[0].networktype == "Basic") {
		                        var array1 = [];
		                        array1.push("&vlan=untagged");	
		                        array1.push("&zoneid=" + args.context.zones[0].id);
		                        array1.push("&podId=" + podId);	
		                        array1.push("&forVirtualNetwork=false"); //direct VLAN	
		                        array1.push("&gateway=" + todb(args.data.guestGateway));
		                        array1.push("&netmask=" + todb(args.data.guestNetmask));	
		                        array1.push("&startip=" + todb(args.data.startGuestIp));
								
								var endip = args.data.endGuestIp;	
		                        if(endip != null && endip.length > 0)
		                            array1.push("&endip=" + todb(endip));
                                
                                $.ajax({
		                            url: createURL("createVlanIpRange" + array1.join("")),
			                        dataType: "json",
			                        async: false,
			                        success: function(json) { 	                    			                			    
				                        //var item = json.createvlaniprangeresponse.vlan;	                                       	                    			
			                        },		   
		                            error: function(XMLHttpResponse) {					                    
				                        //var errorMsg = parseXMLHttpResponse(XMLHttpResponse); 
										//args.response.error(errorMsg);				                    			
                                    }
		                        });		
                            }				        
    				          
			            },
		                error: function(XMLHttpResponse) {	
						    var errorMsg = parseXMLHttpResponse(XMLHttpResponse); 
						    args.response.error(errorMsg);	
		                }
		            });	
                    //???                   
                  },

                  notification: {
                    poll: function(args) {			  
					    args.complete();
					}	
                  },

                  messages: {
                    notification: function(args) {
                      return 'Added new pod';
                    }
                  }
                },  
				
                destroy: testData.actions.destroy('pod')
              },	
			  //???
              detailView: {
                viewAll: { path: '_zone.clusters', label: 'Clusters' },
                tabs: {
                  details: {
                    title: 'Details',
                    fields: [
                      {
                        name: { label: 'Name' },
                      },
                      {
                        allocationstate: { label: 'State' },
                        startip: { label: 'Start IP' },
                        endip: { label: 'End IP' },
                      }
                    ],
                    dataProvider: testData.dataProvider.detailView('pods')
                  },
                }
              }
            }
          },
          clusters: {
            title: 'Clusters',
            listView: {
              id: 'clusters',
              section: 'clusters',
              fields: {
                name: { label: 'Name' },
                zonename: { label: 'Zone' },
                podname: { label: 'Pod' }
              },
              
              //dataProvider: testData.dataProvider.listView('clusters'),
              dataProvider: function(args) {                        
                $.ajax({
                  url: createURL("listClusters&zoneid=" + args.ref.zoneID + "&page=" + args.page + "&pagesize=" + pageSize),
                  dataType: "json",
                  async: true,
                  success: function(json) {             
                    var items = json.listclustersresponse.cluster;            
                    args.response.success({data:items});                            
                  }
                });   
              },
              
              actions: {          
                add: {
                  label: 'Add cluster',

                  messages: {
                    confirm: function(args) {
                      return 'Are you sure you want to add a cluster?';
                    },
                    success: function(args) {
                      return 'Your new cluster is being created.';
                    },
                    notification: function(args) {
                      return 'Creating new cluster';
                    },
                    complete: function(args) {
                      return 'Cluster has been created successfully!';
                    }
                  },

                  createForm: {
                    title: 'Add cluster',
                    desc: 'Please fill in the following data to add a new cluster.',
                    fields: {
                      hypervisor: {
                        label: 'Hypervisor',
                        select: function(args) {            
                          $.ajax({
                            url: createURL("listHypervisors"),       
                            dataType: "json",
                            async: false,
                            success: function(json) {            
                              var hypervisors = json.listhypervisorsresponse.hypervisor;  
                              var items = [];
                              $(hypervisors).each(function() {                    
                                items.push({id: this.name, description: this.name})
                              });         
                              args.response.success({data: items});           
                            }
                          });     
                          
                          args.$select.bind("change", function(event) {               
                            var $form = $(this).closest('form');
                            if($(this).val() == "VMware") {               
                              //$('li[input_sub_group="external"]', $dialogAddCluster).show();
                              $form.find('.form-item[rel=vCenterHost]').css('display', 'inline-block'); 
                              $form.find('.form-item[rel=vCenterUsername]').css('display', 'inline-block'); 
                              $form.find('.form-item[rel=vCenterPassword]').css('display', 'inline-block'); 
                              $form.find('.form-item[rel=vCenterDatacenter]').css('display', 'inline-block'); 
                              
                              //$("#cluster_name_label", $dialogAddCluster).text("vCenter Cluster:");               
                            } 
                            else {
                              //$('li[input_group="vmware"]', $dialogAddCluster).hide();
                              $form.find('.form-item[rel=vCenterHost]').css('display', 'none'); 
                              $form.find('.form-item[rel=vCenterUsername]').css('display', 'none'); 
                              $form.find('.form-item[rel=vCenterPassword]').css('display', 'none'); 
                              $form.find('.form-item[rel=vCenterDatacenter]').css('display', 'none'); 
                              
                              //$("#cluster_name_label", $dialogAddCluster).text("Cluster:");              
                            }             
                          });                                 
                        }       
                      },
                      podId: {
                        label: 'Pod',
                        select: function(args) {              
                          $.ajax({
                            url: createURL("listPods&zoneid=" + args.context.zones[0].id),       
                            dataType: "json",
                            async: true,
                            success: function(json) {            
                              var pods = json.listpodsresponse.pod;                               
                              var items = [];
                              $(pods).each(function() {
                                items.push({id: this.id, description: this.name});
                              });               
                              args.response.success({data: items});           
                            }
                          });  
                        }
                      },
                      name: {
                        label: 'Cluster Name',
                        validation: { required: true }
                      },
                      
                      //hypervisor==VMWare begins here
                      vCenterHost: {
                        label: 'vCenter Host',
                        validation: { required: true }
                      },
                      vCenterUsername: {
                        label: 'vCenter Username',
                        validation: { required: true }
                      },
                      vCenterPassword: {
                        label: 'vCenter Password',
                        validation: { required: true }, 
						isPassword: true
                      },
                      vCenterDatacenter: {
                        label: 'vCenter Datacenter',
                        validation: { required: true }
                      }
                      //hypervisor==VMWare ends here        
                    }
                  },                  
                  
                  action: function(args) {                
                    var array1 = [];
                    array1.push("&zoneId=" + args.context.zones[0].id); 
                    array1.push("&hypervisor=" + args.data.hypervisor); 
                    array1.push("&clustertype=CloudManaged");
                    array1.push("&podId=" + args.data.podId);

                    var clusterName = args.data.name;
                    if(args.data.hypervisor == "VMware") {
                      array1.push("&username=" + todb(args.data.vCenterUsername));  
                      array1.push("&password=" + todb(args.data.vCenterPassword));
                      
                      var hostname = args.data.vCenterHost;
                      var dcName = args.data.vCenterDatacenter;
                      
                      var url;          
                      if(hostname.indexOf("http://") == -1)
                        url = "http://" + hostname;
                      else
                        url = hostname;
                      url += "/" + dcName + "/" + clusterName;
                      array1.push("&url=" + todb(url));
                      
                      clusterName = hostname + "/" + dcName + "/" + clusterName; //override clusterName
                    }           
                    array1.push("&clustername=" + todb(clusterName));
                    
                    $.ajax({
                      url: createURL("addCluster" + array1.join("")),
                      dataType: "json",
                      async: true,
                      success: function(json) {   
                        var item = json.addclusterresponse.cluster[0];              
                        args.response.success({data: item});              
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
                
                destroy: testData.actions.destroy('cluster')
              },
              detailView: {
                viewAll: { path: '_zone.hosts', label: 'Hosts' },
                tabs: {
                  details: {
                    title: 'Details',
                    fields: [
                      {
                        name: { label: 'Name' },
                      },
                      {
                        allocationstate: { label: 'State' },
                        podname: { label: 'Pod' },
                        hypervisortype: { label: 'Hypervisor' },
                        clustertype: { label: 'Cluster' },
                      }
                    ],

                    dataProvider: testData.dataProvider.detailView('clusters')
                  },
                }
              }
            }
          },
          hosts: {
            title: 'Hosts',
            id: 'hosts',
            listView: {
              section: 'hosts',
              fields: {
                name: { label: 'Name' },
                zonename: { label: 'Zone' },
                podname: { label: 'Pod' }
              },
              
              //dataProvider: testData.dataProvider.listView('hosts'), 
              dataProvider: function(args) {                        
                $.ajax({
                  url: createURL("listHosts&type=Routing&zoneid=" + args.ref.zoneID + "&page=" + args.page + "&pagesize=" + pageSize),
                  dataType: "json",
                  async: true,
                  success: function(json) {             
                    var items = json.listhostsresponse.host;          
                    args.response.success({
					  actionFilter: hostActionfilter,
					  data: items
					});                            
                  }
                });   
              },
              
              actions: {
                add: {
                  label: 'Add host',

                  createForm: {
                    title: 'Add new host',
                    desc: 'Please fill in the following information to add a new host fro the specified zone configuration.',
                    fields: {
                      podId: {
                        label: 'Pod',
                        validation: { required: true },
                        select: function(args) {                          
                          $.ajax({
                            url: createURL("listPods&zoneid=" + args.context.zones[0].id),       
                            dataType: "json",
                            async: true,
                            success: function(json) {            
                              var pods = json.listpodsresponse.pod;                               
                              var items = [];
                              $(pods).each(function() {
                                items.push({id: this.id, description: this.name});
                              });               
                              args.response.success({data: items});           
                            }
                          });  
                        }
                      },
                      
                      clusterId: {
                        label: 'Cluster',
                        validation: { required: true },
                        dependsOn: 'podId',
                        select: function(args) {
                          
                          
                          $.ajax({
                            url: createURL("listClusters&podid=" + args.podId),      
                            dataType: "json",
                            async: false,
                            success: function(json) {            
                              clusterObjs = json.listclustersresponse.cluster;                        
                              var items = [];
                              $(clusterObjs).each(function() {
                                items.push({id: this.id, description: this.name});
                              });               
                              args.response.success({data: items});           
                            }
                          });  
                          
                          args.$select.change(function() {                            
                            var $form = $(this).closest('form');
                            
                            var clusterId = $(this).val();
                            if(clusterId == null)
                              return;    
                            
                            var items = [];             
                            $(clusterObjs).each(function(){                 
                              if(this.id == clusterId){
                                selectedClusterObj = this;
                                return false; //break the $.each() loop 
                              }                   
                            }); 
                            if(selectedClusterObj == null)
                              return;   
                            
                            if(selectedClusterObj.hypervisortype == "VMware") {               
                              //$('li[input_group="general"]', $dialogAddHost).hide();
                              $form.find('.form-item[rel=hostname]').hide();
                              $form.find('.form-item[rel=username]').hide();
                              $form.find('.form-item[rel=password]').hide();
                              
                              //$('li[input_group="vmware"]', $dialogAddHost).show();
                              $form.find('.form-item[rel=vcenterHost]').css('display', 'inline-block'); 
                              
                              //$('li[input_group="baremetal"]', $dialogAddHost).hide();
                              $form.find('.form-item[rel=baremetalCpuCores]').hide();
                              $form.find('.form-item[rel=baremetalCpu]').hide();
                              $form.find('.form-item[rel=baremetalMemory]').hide();
                              $form.find('.form-item[rel=baremetalMAC]').hide();
                              
                              //$('li[input_group="Ovm"]', $dialogAddHost).hide();
                              $form.find('.form-item[rel=agentUsername]').hide();
                              $form.find('.form-item[rel=agentPassword]').hide();
                            } 
                            else if (selectedClusterObj.hypervisortype == "BareMetal") {
                              //$('li[input_group="general"]', $dialogAddHost).show();
                              $form.find('.form-item[rel=hostname]').css('display', 'inline-block'); 
                              $form.find('.form-item[rel=username]').css('display', 'inline-block'); 
                              $form.find('.form-item[rel=password]').css('display', 'inline-block'); 
                              
                              //$('li[input_group="baremetal"]', $dialogAddHost).show();
                              $form.find('.form-item[rel=baremetalCpuCores]').css('display', 'inline-block'); 
                              $form.find('.form-item[rel=baremetalCpu]').css('display', 'inline-block'); 
                              $form.find('.form-item[rel=baremetalMemory]').css('display', 'inline-block'); 
                              $form.find('.form-item[rel=baremetalMAC]').css('display', 'inline-block'); 
                              
                              //$('li[input_group="vmware"]', $dialogAddHost).hide();
                              $form.find('.form-item[rel=vcenterHost]').hide();               
                              
                              //$('li[input_group="Ovm"]', $dialogAddHost).hide();
                              $form.find('.form-item[rel=agentUsername]').hide();
                              $form.find('.form-item[rel=agentPassword]').hide();               
                            } 
                            else if (selectedClusterObj.hypervisortype == "Ovm") {                
                              //$('li[input_group="general"]', $dialogAddHost).show();   
                              $form.find('.form-item[rel=hostname]').css('display', 'inline-block'); 
                              $form.find('.form-item[rel=username]').css('display', 'inline-block'); 
                              $form.find('.form-item[rel=password]').css('display', 'inline-block'); 
                              
                              //$('li[input_group="vmware"]', $dialogAddHost).hide();     
                              $form.find('.form-item[rel=vcenterHost]').hide();   
                              
                              //$('li[input_group="baremetal"]', $dialogAddHost).hide();
                              $form.find('.form-item[rel=baremetalCpuCores]').hide();
                              $form.find('.form-item[rel=baremetalCpu]').hide();
                              $form.find('.form-item[rel=baremetalMemory]').hide();
                              $form.find('.form-item[rel=baremetalMAC]').hide();
                              
                              //$('li[input_group="Ovm"]', $dialogAddHost).show();  
                              $form.find('.form-item[rel=agentUsername]').css('display', 'inline-block'); 
                              $form.find('.form-item[rel=agentUsername]').find('input').val("oracle");
                              $form.find('.form-item[rel=agentPassword]').css('display', 'inline-block');               
                            } 
                            else {        
                              //$('li[input_group="general"]', $dialogAddHost).show();
                              $form.find('.form-item[rel=hostname]').css('display', 'inline-block'); 
                              $form.find('.form-item[rel=username]').css('display', 'inline-block'); 
                              $form.find('.form-item[rel=password]').css('display', 'inline-block'); 
                              
                              //$('li[input_group="vmware"]', $dialogAddHost).hide();
                              $form.find('.form-item[rel=vcenterHost]').hide(); 
                              
                              //$('li[input_group="baremetal"]', $dialogAddHost).hide();
                              $form.find('.form-item[rel=baremetalCpuCores]').hide();
                              $form.find('.form-item[rel=baremetalCpu]').hide();
                              $form.find('.form-item[rel=baremetalMemory]').hide();
                              $form.find('.form-item[rel=baremetalMAC]').hide();
                              
                              //$('li[input_group="Ovm"]', $dialogAddHost).hide();
                              $form.find('.form-item[rel=agentUsername]').hide();
                              $form.find('.form-item[rel=agentPassword]').hide();         
                            }                   
                          });   
                          
                          args.$select.trigger("change");
                        }
                      },

                      hosttags: {
                        label: 'Host tags',
                        validation: { required: false }
                      },
                      
                      //input_group="general" starts here
                      hostname: {
                        label: 'Host name',
                        validation: { required: true },
                        isHidden: true
                      },

                      username: {
                        label: 'User name',
                        validation: { required: true },
                        isHidden: true
                      },

                      password: {
                        label: 'Password',
                        validation: { required: true },
                        isHidden: true, 
                        isPassword: true
                      },              
                      //input_group="general" ends here
                      
                      //input_group="VMWare" starts here
                      vcenterHost: {
                        label: 'ESX/ESXi Host',
                        validation: { required: true },
                        isHidden: true
                      },
                      //input_group="VMWare" ends here
                      
                      //input_group="BareMetal" starts here
                      baremetalCpuCores: {
                        label: '# of CPU Cores',
                        validation: { required: true },
                        isHidden: true
                      },
                      baremetalCpu: {
                        label: 'CPU (in MHz)',
                        validation: { required: true },
                        isHidden: true
                      },
                      baremetalMemory: {
                        label: 'Memory (in MB)',
                        validation: { required: true },
                        isHidden: true
                      },
                      baremetalMAC: {
                        label: 'Host MAC',
                        validation: { required: true },
                        isHidden: true
                      },
                      //input_group="BareMetal" ends here
                      
                      //input_group="OVM" starts here
                      agentUsername: {
                        label: 'Agent Username',
                        validation: { required: false },
                        isHidden: true
                      },
                      agentPassword: {
                        label: 'Agent Password',
                        validation: { required: true },
                        isHidden: true, 
						isPassword: true
                      }
                      //input_group="OVM" ends here
                    }
                  },

                  action: function(args) {            
                    var array1 = [];            
                    array1.push("&zoneid=" + args.context.zones[0].id);                   
                    array1.push("&podid=" + args.data.podId); 
                    array1.push("&clusterid=" + args.data.clusterId); 
                    array1.push("&hypervisor=" + todb(selectedClusterObj.hypervisortype));          
                    var clustertype = selectedClusterObj.clustertype;
                    array1.push("&clustertype=" + todb(clustertype));
                    array1.push("&hosttags=" + todb(args.data.hosttags));         
                    
                    if(selectedClusterObj.hypervisortype == "VMware") {
                      array1.push("&username=");
                      array1.push("&password=");
                      var hostname = args.data.vcenterHost;           
                      var url;          
                      if(hostname.indexOf("http://")==-1)
                        url = "http://" + hostname;
                      else
                        url = hostname;
                      array1.push("&url=" + todb(url));           
                    } 
                    else {            
                      array1.push("&username=" + todb(args.data.username));   
                      array1.push("&password=" + todb(args.data.password));
                      
                      var hostname = args.data.hostname;
                      
                      var url;          
                      if(hostname.indexOf("http://")==-1)
                        url = "http://" + hostname;
                      else
                        url = hostname;
                      array1.push("&url="+todb(url));
                      
                      if (selectedClusterObj.hypervisortype == "BareMetal") {             
                        array1.push("&cpunumber=" + todb(args.data.baremetalCpuCores));
                        array1.push("&cpuspeed=" + todb(args.data.baremetalCpu));
                        array1.push("&memory=" + todb(args.data.baremetalMemory));              
                        array1.push("&hostmac=" + todb(args.data.baremetalMAC));
                      }
                      else if(selectedClusterObj.hypervisortype == "Ovm") {             
                        array1.push("&agentusername=" + todb(args.data.agentUsername)); 
                        array1.push("&agentpassword=" + todb(args.data.agentPassword));               
                      }
                    }
                    
                    $.ajax({
                      url: createURL("addHost" + array1.join("")),
                      dataType: "json",
                      success: function(json) {                   
                        var item = json.addhostresponse.host[0];              
                        args.response.success({data: item});              
                      },
                      error: function(XMLHttpResponse) {  
                        var errorMsg = parseXMLHttpResponse(XMLHttpResponse); 
                        args.response.error(errorMsg);
                      }   
                    });                 
                  },

                  notification: {
                    poll: function(args){ 
					  args.complete(); 
					}
                  },

                  messages: {
                    notification: function(args) {
                      return 'Added new host';
                    }
                  }
                },        
                destroy: testData.actions.destroy('host')
              },
              detailView: {
			    name: "Host details",
				actions: {				  
				  enableMaintenaceMode: { 
					label: 'Enable Maintenace' ,
					action: function(args) {	
					  $.ajax({
						url: createURL("prepareHostForMaintenance&id=" + args.context.hosts[0].id),
						dataType: "json",
						async: true,
						success: function(json) { 			    
						  var jid = json.preparehostformaintenanceresponse.jobid;    				
						  args.response.success(
							{_custom:
							  {jobId: jid,
							   getUpdatedItem: function(json) {
								 return json.queryasyncjobresultresponse.jobresult.host;
							   },
							   getActionFilter: function() {
								 return hostActionfilter;
							   }					 
							  }
							}
						  );							
						}
					  });  	
					},
					messages: {
					  confirm: function(args) {
						return 'Enabling maintenance mode will cause a live migration of all running instances on this host to any available host.';
					  },
					  success: function(args) {
						return 'Maintenance is being enabled.';
					  },
					  notification: function(args) {
						return 'Enabling maintenance';
					  },
					  complete: function(args) {
						return 'Maintenance has been enabled.';
					  }
					},		  
					notification: {           
					  poll: pollAsyncJobResult
					}		  
				  },
				  
				  cancelMaintenaceMode: { 
					label: 'Cancel Maintenace' ,
					action: function(args) {	
					  $.ajax({
						url: createURL("cancelHostMaintenance&id=" + args.context.hosts[0].id),
						dataType: "json",
						async: true,
						success: function(json) { 			    
						  var jid = json.cancelhostmaintenanceresponse.jobid;    				
						  args.response.success(
							{_custom:
							  {jobId: jid,
							   getUpdatedItem: function(json) {
								 return json.queryasyncjobresultresponse.jobresult.host;
							   },
							   getActionFilter: function() {
								 return hostActionfilter;
							   }					 
							  }
							}
						  );							
						}
					  });  	
					},
					messages: {
					  confirm: function(args) {
						return 'Please confirm that you want to cancel this maintenance.';
					  },
					  success: function(args) {
						return 'Maintenance is being cancelled.';
					  },
					  notification: function(args) {
						return 'Cancelling maintenance';
					  },
					  complete: function(args) {
						return 'Maintenance has been cancelled.';
					  }
					},		  
					notification: {           
					  poll: pollAsyncJobResult
					}		  
				  }
				  
				},			  
                tabs: {
                  details: {
                    title: 'Details',
                    fields: [
                      {
                        name: { label: 'Name' },
                      },
                      {
                        type: { label: 'Type' },
                        zonename: { label: 'Zone' },
                      }
                    ],
                    
                    dataProvider: function(args) {                        			
                      args.response.success({
					    actionFilter: hostActionfilter,
					    data: args.context.hosts[0]
				      }); 
                    }   
                    
                  },
                }
              }
            }
          },
          'primary-storage': {
            id: 'primaryStorage',
            listView: {
              section: 'primary-storage',
              fields: {
                name: { label: 'Name' },
                zonename: { label: 'Zone' },
                podname: { label: 'Pod' }
              },
              
              //dataProvider: testData.dataProvider.listView('clusters'),
              dataProvider: function(args) {                        
                $.ajax({
                  url: createURL("listStoragePools&zoneid=" + args.ref.zoneID + "&page=" + args.page + "&pagesize=" + pageSize),
                  dataType: "json",
                  async: true,
                  success: function(json) {             
                    var items = json.liststoragepoolsresponse.storagepool;            
                    args.response.success({data:items});                            
                  }
                });   
              },  
              			  
              actions: {
                add: {
                  label: 'Add primary storage',

                  createForm: {
                    title: 'Add new primary storage',
                    desc: 'Please fill in the following information to add a new primary storage',
                    fields: {
                      podId: {
                        label: 'Pod',
                        validation: { required: true },
                        select: function(args) {                          
                          $.ajax({
                            url: createURL("listPods&zoneid=" + args.context.zones[0].id),       
                            dataType: "json",
                            async: true,
                            success: function(json) {            
                              var pods = json.listpodsresponse.pod;                               
                              var items = [];
                              $(pods).each(function() {
                                items.push({id: this.id, description: this.name});
                              });               
                              args.response.success({data: items});           
                            }
                          });  
                        }
                      },
                      
                      clusterId: {
                        label: 'Cluster',
                        validation: { required: true },
                        dependsOn: 'podId',
                        select: function(args) {   
                          $.ajax({
                            url: createURL("listClusters&podid=" + args.podId),      
                            dataType: "json",
                            async: false,
                            success: function(json) {            
                              clusterObjs = json.listclustersresponse.cluster;                        
                              var items = [];
                              $(clusterObjs).each(function() {
                                items.push({id: this.id, description: this.name});
                              });               
                              args.response.success({data: items});           
                            }
                          });                            
                        }
                      },

                      name: {
                        label: 'Name',
                        validation: { required: true }
                      },
                      
                      protocol: {
                        label: 'Protocol',
                        validation: { required: true },
                        dependsOn: 'clusterId',
                        select: function(args) {                            					  
						  var clusterId = args.clusterId;
                          if(clusterId == null)
                            return;   
                          var items = [];             
                          $(clusterObjs).each(function(){                 
                            if(this.id == clusterId){
                              selectedClusterObj = this;
                              return false; //break the $.each() loop 
                            }                   
                          }); 
                          if(selectedClusterObj == null)
                            return;   
						  
                          if(selectedClusterObj.hypervisortype == "KVM") {						    						                            
                            var items = [];
							items.push({id: "nfs", description: "nfs"});
							items.push({id: "SharedMountPoint", description: "SharedMountPoint"});							
                            args.response.success({data: items}); 
						  } 
						  else if(selectedClusterObj.hypervisortype == "XenServer") {
						    var items = [];
							items.push({id: "nfs", description: "nfs"});
							items.push({id: "PreSetup", description: "PreSetup"});
							items.push({id: "iscsi", description: "iscsi"});							
                            args.response.success({data: items}); 						  
						  } 
						  else if(selectedClusterObj.hypervisortype == "VMware") {
						    var items = [];
							items.push({id: "nfs", description: "nfs"});
							items.push({id: "vmfs", description: "vmfs"});							
                            args.response.success({data: items}); 							
						  } 
						  else if(selectedClusterObj.hypervisortype == "Ovm") {
						    var items = [];
							items.push({id: "nfs", description: "nfs"});
							items.push({id: "ocfs2", description: "ocfs2"});														
                            args.response.success({data: items}); 							
						  } 
						  else {
							args.response.success({data:[]});
						  }                                                  
                         
                          args.$select.change(function() {   
                            var $form = $(this).closest('form');
                            
                            var protocol = $(this).val();							
                            if(protocol == null)
                              return;  
                           	
							if(protocol == "nfs") {
								//$("#add_pool_server_container", $dialogAddPool).show();
								$form.find('.form-item[rel=server]').css('display', 'inline-block'); 
								//$dialogAddPool.find("#add_pool_nfs_server").val("");
								$form.find('.form-item[rel=server]').find(".value").find("input").val("");
								
								//$('li[input_group="nfs"]', $dialogAddPool).show();
								$form.find('.form-item[rel=path]').css('display', 'inline-block'); 								
								//$dialogAddPool.find("#add_pool_path_container").find("label").text(g_dictionary["label.path"]+":");
								$form.find('.form-item[rel=path]').find(".name").find("label").text("Path:");				
								
								//$('li[input_group="iscsi"]', $dialogAddPool).hide();
								$form.find('.form-item[rel=iqn]').hide();
								$form.find('.form-item[rel=lun]').hide();
								
								//$('li[input_group="vmfs"]', $dialogAddPool).hide();
								$form.find('.form-item[rel=vCenterDataCenter]').hide();
								$form.find('.form-item[rel=vCenterDataStore]').hide();	
							}   
							else if(protocol == "ocfs2") {//ocfs2 is the same as nfs, except no server field.  
                                //$dialogAddPool.find("#add_pool_server_container").hide();   
								$form.find('.form-item[rel=server]').hide();	
								//$dialogAddPool.find("#add_pool_nfs_server").val("");
								$form.find('.form-item[rel=server]').find(".value").find("input").val("");								
							
								//$('li[input_group="nfs"]', $dialogAddPool).show();
								$form.find('.form-item[rel=path]').css('display', 'inline-block'); 
								//$dialogAddPool.find("#add_pool_path_container").find("label").text(g_dictionary["label.path"]+":");
								$form.find('.form-item[rel=path]').find(".name").find("label").text("Path:");
								
								//$('li[input_group="iscsi"]', $dialogAddPool).hide();
								$form.find('.form-item[rel=iqn]').hide();
								$form.find('.form-item[rel=lun]').hide();
								
								//$('li[input_group="vmfs"]', $dialogAddPool).hide();
								$form.find('.form-item[rel=vCenterDataCenter]').hide();
								$form.find('.form-item[rel=vCenterDataStore]').hide();
							}     	
							else if(protocol == "PreSetup") {
								//$dialogAddPool.find("#add_pool_server_container").hide(); 
								$form.find('.form-item[rel=server]').hide();								
								//$dialogAddPool.find("#add_pool_nfs_server").val("localhost");  
								$form.find('.form-item[rel=server]').find(".value").find("input").val("localhost");
								
								//$('li[input_group="nfs"]', $dialogAddPool).show();
								$form.find('.form-item[rel=path]').css('display', 'inline-block'); 
								//$dialogAddPool.find("#add_pool_path_container").find("label").text(g_dictionary["label.SR.name"]+":");
								$form.find('.form-item[rel=path]').find(".name").find("label").text("SR Name-Label:");
								
								//$('li[input_group="iscsi"]', $dialogAddPool).hide();
								$form.find('.form-item[rel=iqn]').hide();
								$form.find('.form-item[rel=lun]').hide();
								
								//$('li[input_group="vmfs"]', $dialogAddPool).hide();
								$form.find('.form-item[rel=vCenterDataCenter]').hide();
								$form.find('.form-item[rel=vCenterDataStore]').hide();
							} 
							else if(protocol == "iscsi") {
								//$dialogAddPool.find("#add_pool_server_container").show();
								$form.find('.form-item[rel=server]').css('display', 'inline-block'); 
								//$dialogAddPool.find("#add_pool_nfs_server").val("");	
								$form.find('.form-item[rel=server]').find(".value").find("input").val("");
								
								//$('li[input_group="nfs"]', $dialogAddPool).hide();
								$form.find('.form-item[rel=path]').hide();
								
								//$('li[input_group="iscsi"]', $dialogAddPool).show();
								$form.find('.form-item[rel=iqn]').css('display', 'inline-block'); 
								$form.find('.form-item[rel=lun]').css('display', 'inline-block'); 
								
								//$('li[input_group="vmfs"]', $dialogAddPool).hide();
								$form.find('.form-item[rel=vCenterDataCenter]').hide();
								$form.find('.form-item[rel=vCenterDataStore]').hide();	
							} 
							else if(protocol == "vmfs") {
								//$dialogAddPool.find("#add_pool_server_container").show();
								$form.find('.form-item[rel=server]').css('display', 'inline-block'); 
								//$dialogAddPool.find("#add_pool_nfs_server").val(""); 	
								$form.find('.form-item[rel=server]').find(".value").find("input").val("");
								
								//$('li[input_group="nfs"]', $dialogAddPool).hide();
								$form.find('.form-item[rel=path]').hide();
								
								//$('li[input_group="iscsi"]', $dialogAddPool).hide();
								$form.find('.form-item[rel=iqn]').hide();
								$form.find('.form-item[rel=lun]').hide();
								
								//$('li[input_group="vmfs"]', $dialogAddPool).show();   
								$form.find('.form-item[rel=vCenterDataCenter]').css('display', 'inline-block'); 
								$form.find('.form-item[rel=vCenterDataStore]').css('display', 'inline-block'); 									
							}
							else if(protocol == "SharedMountPoint") {  //"SharedMountPoint" show the same fields as "nfs" does.
								//$dialogAddPool.find("#add_pool_server_container").hide();
								$form.find('.form-item[rel=server]').hide();
								//$dialogAddPool.find("#add_pool_nfs_server").val("localhost");	
								$form.find('.form-item[rel=server]').find(".value").find("input").val("localhost");								
								
								//$('li[input_group="nfs"]', $dialogAddPool).show();
								$form.find('.form-item[rel=path]').css('display', 'inline-block'); 
								$form.find('.form-item[rel=path]').find(".name").find("label").text("Path:");
								
								//$('li[input_group="iscsi"]', $dialogAddPool).hide();
								$form.find('.form-item[rel=iqn]').hide();
								$form.find('.form-item[rel=lun]').hide();
								
								//$('li[input_group="vmfs"]', $dialogAddPool).hide();
								$form.find('.form-item[rel=vCenterDataCenter]').hide();
								$form.find('.form-item[rel=vCenterDataStore]').hide();	
							} 
							else {    
                                //$dialogAddPool.find("#add_pool_server_container").show();
								$form.find('.form-item[rel=server]').css('display', 'inline-block'); 							    
								//$dialogAddPool.find("#add_pool_nfs_server").val(""); 	
								$form.find('.form-item[rel=server]').find(".value").find("input").val("");
							
								//$('li[input_group="iscsi"]', $dialogAddPool).hide();
								$form.find('.form-item[rel=iqn]').hide();
								$form.find('.form-item[rel=lun]').hide();
								
								//$('li[input_group="vmfs"]', $dialogAddPool).hide();
								$form.find('.form-item[rel=vCenterDataCenter]').hide();
								$form.find('.form-item[rel=vCenterDataStore]').hide();								
							}   
                          });                            
                          
                          args.$select.trigger("change");
                        }
                      },   
                         
					  storageTags: {
                        label: 'Storage Tags',
                        validation: { required: false }
                      },	 
					  
					  server: {
                        label: 'Server',
                        validation: { required: true },
						isHidden: true
                      },	
					  					  
					  //nfs
					  path: {
                        label: 'Path',
                        validation: { required: true },
						isHidden: true
                      },	
					  
					  //iscsi
					  iqn: {
                        label: 'Target IQN',
                        validation: { required: true },
						isHidden: true
                      },					  
					  lun: {
                        label: 'LUN #',
                        validation: { required: true },
						isHidden: true
                      },	
					  
					  //vmfs
					  vCenterDataCenter: {
                        label: 'vCenter Datacenter',
                        validation: { required: true },
						isHidden: true
                      },				  
					  vCenterDataStore: {
                        label: 'vCenter Datastore',
                        validation: { required: true },
						isHidden: true
                      }
                    }
                  },

                  action: function(args) {  
					var array1 = [];
					array1.push("&zoneid=" + args.context.zones[0].id);					
					array1.push("&podId=" + args.data.podId);		
					array1.push("&clusterid=" + args.data.clusterId);	
					array1.push("&name=" + todb(args.data.name));
			
					var server = args.data.server;	
					var url = null;
					if (args.data.protocol == "nfs") {
						//var path = trim($thisDialog.find("#add_pool_path").val());
						var path = args.data.path;
						
						if(path.substring(0,1) != "/")
							path = "/" + path; 
						url = nfsURL(server, path);
					} 
					else if (args.data.protocol == "PreSetup") {
						//var path = trim($thisDialog.find("#add_pool_path").val());
						var path = args.data.path;
						
						if(path.substring(0,1) != "/")
							path = "/" + path; 
						url = presetupURL(server, path);
					} 
					else if (args.data.protocol == "ocfs2") {
						//var path = trim($thisDialog.find("#add_pool_path").val());
						var path = args.data.path;
					
						if(path.substring(0,1) != "/")
							path = "/" + path; 
						url = ocfs2URL(server, path);						
					} 
					else if (args.data.protocol == "SharedMountPoint") {
						//var path = trim($thisDialog.find("#add_pool_path").val());
						var path = args.data.path;
						
						if(path.substring(0,1) != "/")
							path = "/" + path; 
						url = SharedMountPointURL(server, path);
					} 
					else if (args.data.protocol == "vmfs") {
						//var path = trim($thisDialog.find("#add_pool_vmfs_dc").val());
						var path = args.data.vCenterDataCenter;
						
						if(path.substring(0,1) != "/")
							path = "/" + path; 
						path += "/" + args.data.vCenterDataStore;
						url = vmfsURL("dummy", path);
					} 
					else {
						//var iqn = trim($thisDialog.find("#add_pool_iqn").val());
						var iqn = args.data.iqn;
						
						if(iqn.substring(0,1) != "/")
							iqn = "/" + iqn; 
						var lun = args.data.lun;
						url = iscsiURL(server, iqn, lun);
					}
					array1.push("&url=" + todb(url));
										
					if(args.data.storageTags != null && args.data.storageTags.length > 0)
						array1.push("&tags=" + todb(args.data.storageTags));				    
					
					$.ajax({
						url: createURL("createStoragePool" + array1.join("")),
						dataType: "json",
						success: function(json) {  
						    var item = json.createstoragepoolresponse.storagepool[0];              
						    args.response.success({data: item});              
						},
						error: function(XMLHttpResponse) {  						    
						    var errorMsg = parseXMLHttpResponse(XMLHttpResponse); 
						    args.response.error(errorMsg);
						}   						    
					});					
                  },

                  notification: {
                    poll: function(args){ 
					  args.complete(); 
					}
                  },

                  messages: {
                    notification: function(args) {
                      return 'Added new primary storage';
                    }
                  }
                },        
                destroy: testData.actions.destroy('cluster')
              },
			  			  
              detailView: {
                tabs: {
                  details: {
                    title: 'Details',
                    fields: [
                      {
                        name: { label: 'Name' },
                      },
                      {
                        zonename: { label: 'Zone' },
                        hostname: { label: 'Host' }
                      }
                    ],

                    dataProvider: testData.dataProvider.detailView('clusters')
                  }
                }
              }
            }
          },
          
          'secondary-storage': {
            id: 'secondary-storage',
            listView: {
              section: 'seconary-storage',
              fields: {
                name: { label: 'Name' },
                zonename: { label: 'Zone' },
                podname: { label: 'Pod' }
              },
              
              //dataProvider: testData.dataProvider.listView('clusters'),
              dataProvider: function(args) {                        
                $.ajax({
                  url: createURL("listHosts&type=SecondaryStorage&zoneid=" + args.ref.zoneID + "&page=" + args.page + "&pagesize=" + pageSize),
                  dataType: "json",
                  async: true,
                  success: function(json) {             
                    var items = json.listhostsresponse.host;            
                    args.response.success({data:items});                            
                  }
                });   
              },  
              			  
              actions: {
                add: {
                  label: 'Add secondary storage',

                  createForm: {
                    title: 'Add new secondary storage',
                    desc: 'Please fill in the following information to add a new secondary storage',
                    fields: {                      
                      nfsServer: {
                        label: 'NFS Server',
                        validation: { required: true }
                      },
                      path: {
                        label: 'Path',
                        validation: { required: true }
                      }			  
                    }
                  },

                  action: function(args) {  					
					var zoneId = args.context.zones[0].id;		
					var nfs_server = args.data.nfsServer;		
					var path = args.data.path;    					    				    					   					
					var url = nfsURL(nfs_server, path);  
									  
					$.ajax({
						url: createURL("addSecondaryStorage&zoneId=" + zoneId + "&url=" + todb(url)),
						dataType: "json",
						success: function(json) {						    
						    var item = json.addsecondarystorageresponse.secondarystorage;		    
							args.response.success({data:item});									        					    
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
                  },

                  messages: {
                    notification: function(args) {
                      return 'Added new secondary storage';
                    }
                  }
                },        
                destroy: testData.actions.destroy('secondary storage')
              },			  
			  
              detailView: {
                tabs: {
                  details: {
                    title: 'Details',
                    fields: [
                      {
                        name: { label: 'Name' },
                      },
                      {
                        zonename: { label: 'Zone' },
                        hostname: { label: 'Host' }
                      }
                    ],

                    dataProvider: testData.dataProvider.detailView('clusters')
                  }
                }
              }
            }
          }
        }        
      },
      virtualAppliances: {
        type: 'select',
        title: 'Virtual Appliances',
        id: 'virtualAppliances',
        listView: {
          label: 'Virtual Appliances',
          fields: {
            name: { label: 'Name' },
            hostname: { label: 'Hostname' },
            publicip: { label: 'Public IP' },
            publicmacaddress: { label: 'Public MAC' },
            state: { label: 'Status', indicator: { 'Stopped': 'off', 'Running': 'on' } }
          },
          actions: {
            start: {
              label: 'Start router',
              action: function(args) {
                $.ajax({
                  url: createURL('startRouter&id=' + args.data.id),
                  dataType: 'json',
                  async: true,
                  success: function(json) {
                    var jid = json.startrouterresponse.jobid;
                    args.response.success({
                      _custom: { 
                        jobId: jid
                      }
                    });
                  },
                  error: function(json) {
                    args.response.error({ message: 'Cannot start router' });
                  }
                });
              },
              messages: {
                confirm: function(args) {
                  return 'Are you sure you want to start router ' + args.name + '?';
                },
                notification: function(args) {
                  return 'Starting router: ' + args.name;
                }
              },
              notification: {
                poll: pollAsyncJobResult
              }

            }
          },
          dataProvider: function(args) {
            $.ajax({
              url: createURL('listRouters&page=' + args.page + '&pagesize=' + pageSize),
              dataType: 'json',
              async: true,
              success: function(json) {
                var items = json.listroutersresponse.router;
                args.response.success({ data: items });
              }
            });
          }
        }
      },
      systemVMs: {
        type: 'select',
        title: 'System VMs',
        listView: {
          label: 'System VMs',
          fields: {
            name: { label: 'Name' },
            zonename: { label: 'Zone' },
            state: { label: 'Status' }
          },
          dataProvider: function(args) {
		        $.ajax({
		          url: createURL("listSystemVms&page=" + args.page + "&pagesize=" + pageSize),
		          dataType: "json",
		          async: true,
		          success: function(json) { 	
			          var items = json.listsystemvmsresponse.systemvm;
                
                args.response.success({
                  data: items
                });					                			
		          }
		        });
          }
        }
      }
    }
  };
   
  function nfsURL(server, path) {
    var url;
    if(server.indexOf("://")==-1)
	    url = "nfs://" + server + path;
	else
	    url = server + path;
	return url;
  }
  
  function presetupURL(server, path) {
    var url;
    if(server.indexOf("://")==-1)
	    url = "presetup://" + server + path;
	else
	    url = server + path;
	return url;
  }

  function ocfs2URL(server, path) { 
    var url;
    if(server.indexOf("://")==-1)
	    url = "ocfs2://" + server + path;
	else
	    url = server + path;
	return url;
  }

  function SharedMountPointURL(server, path) {
    var url;
    if(server.indexOf("://")==-1)
	    url = "SharedMountPoint://" + server + path;
	else
	    url = server + path;
	return url;
  }

  function vmfsURL(server, path) {
    var url;
    if(server.indexOf("://")==-1)
	    url = "vmfs://" + server + path;
	else
	    url = server + path;
	return url;
  }

  function iscsiURL(server, iqn, lun) {
    var url;
    if(server.indexOf("://")==-1)
	    url = "iscsi://" + server + iqn + "/" + lun;
	else
	    url = server + iqn + "/" + lun;
	return url;
  }  
  
  //action filters (begin)
  var hostActionfilter = function(args) {	    		  
    var jsonObj = args.context.item;
	var allowedActions = [];	
	allowedActions.push("enableMaintenaceMode");	
	allowedActions.push("cancelMaintenaceMode");
    return allowedActions;
  }  
  //action filters (end)
  
})($, cloudStack, testData);
