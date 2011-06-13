 /**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
 
 function afterLoadPodJSP($leftmenuItem1) {   
    hideMiddleMenu();	
 
    var $topButtonContainer = clearButtonsOnTop();			    	       
	$("#top_buttons").appendTo($topButtonContainer); 
 
    initDialog("dialog_add_external_cluster");
    initDialog("dialog_add_host", 400);    
    initDialog("dialog_add_iprange_to_pod");
    
    //add pool dialog
    initDialog("dialog_add_pool", 400);	
    bindEventHandlerToDialogAddPool($("#dialog_add_pool"));	 
    
    $.ajax({
        data: createURL("command=listHypervisors"),
        dataType: "json",
        success: function(json) {            
            var items = json.listhypervisorsresponse.hypervisor;
            var $hypervisorDropdown = $("#dialog_add_external_cluster").find("#cluster_hypervisor");
            if(items != null && items.length > 0) {                
                for(var i=0; i<items.length; i++) {                    
                    $hypervisorDropdown.append("<option value='"+fromdb(items[i].name)+"'>"+fromdb(items[i].name)+"</option>");
                }
            }
        }    
    });   
    
    //switch between different tabs 
    var tabArray = [$("#tab_details"), $("#tab_ipallocation")];
    var tabContentArray = [$("#tab_content_details"), $("#tab_content_ipallocation")];
    var afterSwitchFnArray = [podJsonToDetailsTab, podJsonToNetworkTab];
    switchBetweenDifferentTabs(tabArray, tabContentArray, afterSwitchFnArray);       
   
    $readonlyFields  = $("#tab_content_details").find("#name, #netmask, #ipRange, #gateway");
    $editFields = $("#tab_content_details").find("#name_edit, #netmask_edit, #startIpRange_edit, #endIpRange_edit, #gateway_edit");
        
	podJsonToRightPanel($leftmenuItem1);     	
}

function podJsonToRightPanel($leftmenuItem1) {	    
    bindAddClusterButton($leftmenuItem1); 
    bindAddHostButton($leftmenuItem1); 
    bindAddPrimaryStorageButton($leftmenuItem1);  
           
    $("#right_panel_content").data("$leftmenuItem1", $leftmenuItem1); 
    cancelEditMode($("#tab_content_details"));  
    $("#tab_details").click();   
}

function podJsonToDetailsTab() {	    
    var $leftmenuItem1 = $("#right_panel_content").data("$leftmenuItem1");
    if($leftmenuItem1 == null) {
        podClearDetailsTab();
        return;
    }
    
    var jsonObj = $leftmenuItem1.data("jsonObj");    
    if(jsonObj == null) {
        podClearDetailsTab();
	    return;	
	}
    
    $.ajax({
        data: createURL("command=listPods&id="+jsonObj.id),
        dataType: "json",
        async: false,
        success: function(json) {            
            var items = json.listpodsresponse.pod;			
			if(items != null && items.length > 0) {
                jsonObj = items[0];
                $leftmenuItem1.data("jsonObj", jsonObj);                  
            }
        }
    });    
    
    var $thisTab = $("#right_panel_content #tab_content_details");  
    $thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();       
        
    $thisTab.find("#id").text(fromdb(jsonObj.id));
    $thisTab.find("#grid_header_title").text(fromdb(jsonObj.name));
    
    $thisTab.find("#name").text(fromdb(jsonObj.name));
    $thisTab.find("#name_edit").val(fromdb(jsonObj.name));
        
    $thisTab.find("#netmask").text(fromdb(jsonObj.netmask));   
    $thisTab.find("#netmask_edit").val(fromdb(jsonObj.netmask));   
         
    $thisTab.find("#ipRange").text(getIpRange(jsonObj.startip, jsonObj.endip));
    $thisTab.find("#startIpRange_edit").val(fromdb(jsonObj.startip));
    $thisTab.find("#endIpRange_edit").val(fromdb(jsonObj.endip));
    
    $thisTab.find("#gateway").text(fromdb(jsonObj.gateway));  
    $thisTab.find("#gateway_edit").val(fromdb(jsonObj.gateway));  
    
    
    // hide network tab upon zone vlan
    var networkType;  
    $.ajax({	    
	    data: createURL("command=listZones&id="+fromdb(jsonObj.zoneid)),
		dataType: "json",	
		async: false,	
		success: function(json) {
			var items = json.listzonesresponse.zone;						
			if (items != null && items.length > 0) {					    
				networkType = items[0].networktype;
			}				
		}
	});	
    if(networkType == "Basic") { //basic-mode network (pod-wide VLAN)
        $("#tab_ipallocation, #add_iprange_button").show();  
        bindAddIpRangeToPodButton($leftmenuItem1);  
    }
    else if(networkType == "Advanced") { //advanced-mode network (zone-wide VLAN)
        $("#tab_ipallocation, #add_iprange_button").hide();
        $("#midmenu_add_directIpRange_button").unbind("click").hide();         
    }
    
    
    //actions ***   
    var $actionLink = $thisTab.find("#action_link"); 
    bindActionLink($actionLink);
    /*
    $actionLink.bind("mouseover", function(event) {	    
        $(this).find("#action_menu").show();    
        return false;
    });
    $actionLink.bind("mouseout", function(event) {       
        $(this).find("#action_menu").hide();    
        return false;
    });	  
    */
    
    var $actionMenu = $actionLink.find("#action_menu");
    $actionMenu.find("#action_list").empty();   
    buildActionLinkForTab("label.action.edit.pod", podActionMap, $actionMenu, $leftmenuItem1, $thisTab);  
    buildActionLinkForTab("label.action.delete.pod", podActionMap, $actionMenu, $leftmenuItem1, $thisTab); 
    
    $thisTab.find("#tab_spinning_wheel").hide();    
    $thisTab.find("#tab_container").show();      
}	

function podJsonToNetworkTab() {       
	var $leftmenuItem1 = $("#right_panel_content").data("$leftmenuItem1");
    if($leftmenuItem1 == null) {
        podClearNetworkTab();
        return;
    }
    
    var jsonObj = $leftmenuItem1.data("jsonObj");    
    if(jsonObj == null) {
        podClearNetworkTab();
	    return;	
	}
     
    var $thisTab = $("#right_panel_content #tab_content_ipallocation");
	$thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();   		 
        
    $.ajax({
		data: createURL("command=listVlanIpRanges&zoneid="+fromdb(jsonObj.zoneid)+"&podid="+fromdb(jsonObj.id)),
		dataType: "json",
		success: function(json) {			       
			var items = json.listvlaniprangesresponse.vlaniprange;
			var $container = $thisTab.find("#tab_container").empty();
			var template = $("#network_tab_template");	
			if (items != null && items.length > 0) {					    
				for (var i = 0; i < items.length; i++) {	
				    var newTemplate = template.clone(true);	               
	                podNetworkJsonToTemplate(items[i], newTemplate); 
	                $container.append(newTemplate.show());	
				}
			}
			$thisTab.find("#tab_spinning_wheel").hide();    
            $thisTab.find("#tab_container").show();    	
		}			
	});			
} 

function podClearNetworkTab() {    
    var $thisTab = $("#right_panel_content #tab_content_ipallocation");
	$thisTab.find("#tab_container").empty();
} 

function podNetworkJsonToTemplate(jsonObj, template) {
    template.data("jsonObj", jsonObj);     
    template.attr("id", "pod_VLAN_"+fromdb(jsonObj.id)).data("podVLANId", fromdb(jsonObj.id));    
    template.find("#grid_header_title").text(fromdb(jsonObj.description));			   
    template.find("#id").text(fromdb(jsonObj.id));    
    template.find("#iprange").text(fromdb(getIpRange(jsonObj.startip, jsonObj.endip)));
    template.find("#netmask").text(fromdb(jsonObj.netmask));
    template.find("#gateway").text(fromdb(jsonObj.gateway));
    template.find("#podname").text(fromdb(jsonObj.podname)); 
   
    var $actionLink = template.find("#action_link");	
    bindActionLink($actionLink);
    /*	
	$actionLink.bind("mouseover", function(event) {
        $(this).find("#action_menu").show();    
        return false;
    });
    $actionLink.bind("mouseout", function(event) {
        $(this).find("#action_menu").hide();    
        return false;
    });		
	*/
	
	var $actionMenu = $actionLink.find("#action_menu");
    $actionMenu.find("#action_list").empty();	
    
    buildActionLinkForSubgridItem("Delete IP Range", podNetworkActionMap, $actionMenu, template);	
}

var podNetworkActionMap = {  
    "Delete IP Range": {              
        api: "deleteVlanIpRange",     
        isAsyncJob: false,   
        inProcessText: "Deleting IP Range....",
        afterActionSeccessFn: function(json, id, $subgridItem) {                 
            $subgridItem.slideUp("slow", function() {
                $(this).remove();
            });
        }
    } 
}  

function podClearRightPanel() {	 
    podClearDetailsTab();
    podClearNetworkTab();
}

function podClearDetailsTab() {	    
    var $thisTab = $("#right_panel_content #tab_content_details");  
    $thisTab.find("#grid_header_title").text("");    
    $thisTab.find("#id").text("");
    
    $thisTab.find("#name").text("");
    $thisTab.find("#name_edit").val("");
    
    $thisTab.find("#netmask").text("");  
    $thisTab.find("#netmask_edit").val("");
          
    $thisTab.find("#ipRange").text("");
    $thisTab.find("#startIpRange_edit").val("");
    $thisTab.find("#endIpRange_edit").val("");
    
    $thisTab.find("#gateway").text(""); 
    $thisTab.find("#gateway_edit").val(""); 
}
	
function getIpRange(startip, endip) {
    var ipRange = "";
	if (startip != null && startip.length > 0) {
		ipRange = startip;
	}
	if (endip != null && endip.length > 0) {
		ipRange = ipRange + " - " + endip;
	}		
	return ipRange;
}	

var clustersUnderOnePod = {};
function refreshClsuterFieldInAddHostDialog(dialogAddHost, podId, clusterId) {   
	if(podId == null)
	    return;
	
	var arrayParams = [];
	arrayParams.push("&podid=" + podId);
	
    $.ajax({
        data: createURL("command=listClusters"+arrayParams.join("")),
        dataType: "json",
        async: false,
        success: function(json) {			            
            var items = json.listclustersresponse.cluster;
            var $clusterSelect = dialogAddHost.find("#cluster_select").empty();		
            if(items != null && items.length > 0) {		                                        
                for(var i=0; i<items.length; i++) {	
                    clustersUnderOnePod[fromdb(items[i].id)] = items[i];
                    if(clusterId != null && items[i].id == clusterId)
                        $clusterSelect.append("<option value='" + fromdb(items[i].id) + "' selected>" + fromdb(items[i].name) + "</option>");	
                    else               
                        $clusterSelect.append("<option value='" + fromdb(items[i].id) + "'>" + fromdb(items[i].name) + "</option>");		
                }    
            }            
        }
    });     
}      

function bindAddClusterButton($leftmenuItem1) {
    var $button = $("#add_cluster_button");
    $button.unbind("click").bind("click", function(event) {
        dialogAddCluster = $("#dialog_add_external_cluster");      
        dialogAddCluster.find("#info_container").hide();    
    	
        var zoneId, podId;               
        if(currentRightPanelJSP == "jsp/pod.jsp") {
            var podObj = $leftmenuItem1.data("jsonObj");   
            zoneId = podObj.zoneid;
            podId = podObj.id;
            dialogAddCluster.find("#zone_name").text(fromdb(podObj.zonename));  
            dialogAddCluster.find("#pod_name").text(fromdb(podObj.name)); 
        }

        dialogAddCluster.find("#cluster_hypervisor").change(function() {
        	if($(this).val() == "VMware") {
        		$('li[input_group="vmware"]', dialogAddCluster).show();
        		
        		$('li[input_sub_group="external"]', dialogAddCluster).show();
        		$("#cluster_name_label", dialogAddCluster).text("vCenter Cluster:");
        		
        		// dialogAddCluster.find("#type_dropdown").change();
        	} else {
        		$('li[input_group="vmware"]', dialogAddCluster).hide();
        		$("#cluster_name_label", dialogAddCluster).text("Cluster:");
        	}
        }).change();

/*        
        dialogAddCluster.find("#type_dropdown").change(function() {
        	if($(this).val() == "ExternalManaged") {
        		$('li[input_sub_group="external"]', dialogAddCluster).show();
        		$("#cluster_name_label", dialogAddCluster).text("vCenter Cluster:");
        	} else {
        		$('li[input_sub_group="external"]', dialogAddCluster).hide();
        		$("#cluster_name_label", dialogAddCluster).text("Cluster:");
        	}
        });
*/        
        
        dialogAddCluster.dialog('option', 'buttons', { 				
	        "Add": function() { 
	            var $thisDialog = $(this);		            
                $thisDialog.find("#info_container").hide(); 
                
			    var hypervisor = $thisDialog.find("#cluster_hypervisor").val();
			    var clusterType="CloudManaged";
			    if(hypervisor == "VMware") {
			    	// clusterType = $thisDialog.find("#type_dropdown").val();
			    	clusterType="ExternalManaged";
			    }
			    
		        // validate values
		        var isValid = true;
		        if(hypervisor == "VMware" && clusterType != "CloudManaged") {
			        isValid &= validateString("vCenter Server", $thisDialog.find("#cluster_hostname"), $thisDialog.find("#cluster_hostname_errormsg"));
			        isValid &= validateString("vCenter user", $thisDialog.find("#cluster_username"), $thisDialog.find("#cluster_username_errormsg"));
			        isValid &= validateString("Password", $thisDialog.find("#cluster_password"), $thisDialog.find("#cluster_password_errormsg"));	
			        isValid &= validateString("Datacenter", $thisDialog.find("#cluster_datacenter"), $thisDialog.find("#cluster_datacenter_errormsg"));	
		        }
		        isValid &= validateString("Cluster name", $thisDialog.find("#cluster_name"), $thisDialog.find("#cluster_name_errormsg"));	
		        if (!isValid) 
		            return;
		            				
				$thisDialog.find("#spinning_wheel").show(); 				
				
		        var array1 = [];
			    var hypervisor = $thisDialog.find("#cluster_hypervisor").val();
			    array1.push("&hypervisor="+hypervisor);
			    array1.push("&clustertype=" + clusterType);
		        array1.push("&zoneId="+zoneId);
		        array1.push("&podId="+podId);

		        var clusterName = trim($thisDialog.find("#cluster_name").val());
		        if(hypervisor == "VMware" && clusterType != "CloudManaged") {
			        
			        var username = trim($thisDialog.find("#cluster_username").val());
			        array1.push("&username="+todb(username));
					
			        var password = trim($thisDialog.find("#cluster_password").val());
			        array1.push("&password="+todb(password));
			        
			        var hostname = trim($thisDialog.find("#cluster_hostname").val());
			        var dcName = trim($thisDialog.find("#cluster_datacenter").val());
			        
			        var url;					
			        if(hostname.indexOf("http://")==-1)
			            url = "http://" + todb(hostname);
			        else
			            url = hostname;
			        url += "/" + todb(dcName) + "/" + todb(clusterName);
			        array1.push("&url=" + todb(url));
			        
			        clusterName = hostname + "/" + dcName + "/" + clusterName
		        }

		        array1.push("&clustername=" + todb(clusterName));
		        
		        $.ajax({
			       data: createURL("command=addCluster" + array1.join("")),
			        dataType: "json",
			        success: function(json) {
			            $thisDialog.find("#spinning_wheel").hide();
			            $thisDialog.dialog("close");
			            	
                        var item = json.addclusterresponse.cluster[0];                                                                   
                        var $podNode = $("#pod_" + podId);
                        if($podNode.length > 0 && $podNode.css("display") != "none") {
                            if($podNode.find("#pod_arrow").hasClass("white_nonexpanded_close")) {
                                $podNode.find("#pod_arrow").removeClass("white_nonexpanded_close").addClass("expanded_close");    
                            }
                            
                            if($podNode.find("#pod_arrow").hasClass("expanded_close")) { //if pod node is closed
                                $podNode.find("#pod_arrow").click(); //expand pod node
                                var $clusterNode = $podNode.find("#cluster_"+item.id);
                                $clusterNode.find("#cluster_arrow").click(); //expand cluster node to see host node and storage node   
                                $clusterNode.find("#cluster_name").click();  //click cluster node to show cluster info
                            }
                            else { //if pod node is expanded                                
                                var $clusterNode = $("#leftmenu_cluster_node_template").clone(true);  
                                clusterJSONToTreeNode(item, $clusterNode);
                                $podNode.find("#clusters_container").append($clusterNode.show());   
                                $clusterNode.find("#cluster_arrow").click(); //expand cluster node to see host node and storage node   
                                $clusterNode.find("#cluster_name").click();  //click cluster node to show cluster info                                                            
                            }                                      
                        }
			        },			
                    error: function(XMLHttpResponse) {	
						handleError(XMLHttpResponse, function() {							
							handleErrorInDialog(XMLHttpResponse, $thisDialog);							
						});
                    }				
		        });
	        }, 
	        "Cancel": function() { 
		        $(this).dialog("close"); 
	        } 
        }).dialog("open");            
        return false;
    });
}

function bindAddHostButton($leftmenuItem1) {     
    var $button = $("#add_host_button");   
    
    var dialogAddHost = $("#dialog_add_host");   
    dialogAddHost.find("#cluster_select").change(function() {        
        var clusterId = $(this).val();
        if(clusterId == null)
            return;        
        var clusterObj = clustersUnderOnePod[clusterId];                    
    	if(clusterObj.hypervisortype == "VMware") {
    		$('li[input_group="vmware"]', dialogAddHost).show();
    		$('li[input_group="general"]', dialogAddHost).hide();
    	} else {
    		$('li[input_group="vmware"]', dialogAddHost).hide();
    		$('li[input_group="general"]', dialogAddHost).show();
    	}   
    });
    
    $button.unbind("click").bind("click", function(event) {             
        dialogAddHost.find("#info_container").hide();    
        dialogAddHost.find("#new_cluster_name").val("");
       
        var zoneId, podId, clusterId;                   
        if(currentRightPanelJSP == "jsp/pod.jsp") {        
            var podObj = $leftmenuItem1.data("jsonObj");   
            zoneId = podObj.zoneid;
            podId = podObj.id;
            dialogAddHost.find("#zone_name").text(fromdb(podObj.zonename));  
            dialogAddHost.find("#pod_name").text(fromdb(podObj.name)); 
        }
        else if(currentRightPanelJSP == "jsp/cluster.jsp") {
            var clusterObj = $leftmenuItem1.data("jsonObj");   
            zoneId = clusterObj.zoneid;
            podId = clusterObj.podid;    
            clusterId = clusterObj.id;  
            dialogAddHost.find("#zone_name").text(fromdb(clusterObj.zonename));  
            dialogAddHost.find("#pod_name").text(fromdb(clusterObj.podname)); 
        }
        else if(currentRightPanelJSP == "jsp/host.jsp") {            
            var clusterObj = $leftmenuItem1.data("clusterObj");  
            zoneId = clusterObj.zoneid;
            podId = clusterObj.podid;    
            clusterId = clusterObj.id;    
            dialogAddHost.find("#zone_name").text(fromdb(clusterObj.zonename));  
            dialogAddHost.find("#pod_name").text(fromdb(clusterObj.podname)); 
        }       
        
        refreshClsuterFieldInAddHostDialog(dialogAddHost, podId, clusterId); 
        
        dialogAddHost.find("#cluster_select").change();
                        
        dialogAddHost
        .dialog('option', 'buttons', { 				
	        "Add": function() { 
	            var $thisDialog = $(this);		            
	            $thisDialog.find("#info_container").hide(); 
	            
		        // validate values
		        var isValid = true;		
			    isValid &= validateDropDownBox("Cluster", $thisDialog.find("#cluster_select"), $thisDialog.find("#cluster_select_errormsg"), false);  //required, reset error text					    			
				var clusterId = $thisDialog.find("#cluster_select").val();	
				var clusterObj, hypervisor;
				if(clusterId != null) {
				    clusterObj = clustersUnderOnePod[clusterId];    
                    hypervisor = clusterObj.hypervisortype;                     
				    if(hypervisor == "VMware") {

				    	// for VMware, we can only add host to existing cluster, only host address is needed as of now
/*
			            isValid &= validateString("vCenter Address", $thisDialog.find("#host_vcenter_address"), $thisDialog.find("#host_vcenter_address_errormsg"));
			            isValid &= validateString("vCenter User", $thisDialog.find("#host_vcenter_username"), $thisDialog.find("#host_vcenter_username_errormsg"));
			            isValid &= validateString("vCenter Password", $thisDialog.find("#host_vcenter_password"), $thisDialog.find("#host_vcenter_password_errormsg"));	
			            isValid &= validateString("vCenter Datacenter", $thisDialog.find("#host_vcenter_dc"), $thisDialog.find("#host_vcenter_dc_errormsg"));	
*/
			            isValid &= validateString("vCenter Host", $thisDialog.find("#host_vcenter_host"), $thisDialog.find("#host_vcenter_host_errormsg"));	
		            } else {
			            isValid &= validateString("Host name", $thisDialog.find("#host_hostname"), $thisDialog.find("#host_hostname_errormsg"));
			            isValid &= validateString("User name", $thisDialog.find("#host_username"), $thisDialog.find("#host_username_errormsg"));
			            isValid &= validateString("Password", $thisDialog.find("#host_password"), $thisDialog.find("#host_password_errormsg"));	
		            }			
		        }    	
		        if (!isValid) 
		            return;
		            				
				$thisDialog.find("#spinning_wheel").show(); 				
				
		        var array1 = [];			    
		        array1.push("&zoneId="+zoneId);
		        array1.push("&podId="+podId);	
			    array1.push("&clusterid="+clusterId);	
                array1.push("&hypervisor=" + hypervisor);   
                var clustertype = clusterObj.clustertype;
                array1.push("&clustertype=" + clustertype);	
		        	
			    if(hypervisor == "VMware") {
/*			    	
			        var username = trim($thisDialog.find("#host_vcenter_username").val());
			        array1.push("&username="+todb(username));
					
			        var password = trim($thisDialog.find("#host_vcenter_password").val());
			        array1.push("&password="+todb(password));
				    
			        var hostname = trim($thisDialog.find("#host_vcenter_address").val());
			        hostname += "/" + todb(trim($thisDialog.find("#host_vcenter_dc").val()));
			        hostname += "/" + todb(trim($thisDialog.find("#host_vcenter_host").val()));
*/
			        array1.push("&username=");
			        array1.push("&password=");
			        var hostname = trim($thisDialog.find("#host_vcenter_host").val());
			    	
			        var url;					
			        if(hostname.indexOf("http://")==-1)
			            url = "http://" + hostname;
			        else
			            url = hostname;
			        array1.push("&url="+todb(url));
			    } else {
			        var username = trim($thisDialog.find("#host_username").val());
			        array1.push("&username="+todb(username));
					
			        var password = trim($thisDialog.find("#host_password").val());
			        array1.push("&password="+todb(password));
					
			        var hostname = trim($thisDialog.find("#host_hostname").val());
			        var url;					
			        if(hostname.indexOf("http://")==-1)
			            url = "http://" + todb(hostname);
			        else
			            url = hostname;
			        array1.push("&url="+todb(url));
			    }			
		                
		        $.ajax({
			       data: createURL("command=addHost" + array1.join("")),
			        dataType: "json",
			        success: function(json) {
			            $thisDialog.find("#spinning_wheel").hide();
			            $thisDialog.dialog("close");		         
                                               
                        var item = json.addhostresponse.host;	
												
						var $podArrow = $("#pod_"+podId).find("#pod_arrow");
						if($podArrow.hasClass("expanded_close")) {	
						    $podArrow.click();
						}
						
						var $clusterArrow = $("#cluster_"+clusterId).find("#cluster_arrow");
						if($clusterArrow.hasClass("expanded_close")) {	
						    $clusterArrow.click();
						}
						
						$("#cluster_"+clusterId+"_host").click();	                                           
			        },			
                    error: function(XMLHttpResponse) {	
						handleError(XMLHttpResponse, function() {							
							//refreshClsuterFieldInAddHostDialog($thisDialog, podId, clusterId, dialogAddHost.find("#host_hypervisor").val());                                
							handleErrorInDialog(XMLHttpResponse, $thisDialog);								
						});
                    }				
		        });
	        }, 
	        "Cancel": function() { 
		        $(this).dialog("close"); 
	        } 
        }).dialog("open");            
        return false;
    });        
}

function bindAddPrimaryStorageButton($leftmenuItem1) {    
    var $button = $("#add_primarystorage_button");  
    $dialogAddPool = $("#dialog_add_pool");  
     
    var zoneId, podId, sourceClusterId;        
    if(currentRightPanelJSP == "jsp/pod.jsp") { 
        var podObj = $leftmenuItem1.data("jsonObj");  
        zoneId = podObj.zoneid;
        podId = podObj.id;
        $dialogAddPool.find("#zone_name").text(fromdb(podObj.zonename));  
        $dialogAddPool.find("#pod_name").text(fromdb(podObj.name)); 
    }        
    else if(currentRightPanelJSP == "jsp/cluster.jsp") {
        var clusterObj = $leftmenuItem1.data("jsonObj");   
        zoneId = clusterObj.zoneid;
        podId = clusterObj.podid;    
        sourceClusterId = clusterObj.id;   
        $dialogAddPool.find("#zone_name").text(fromdb(clusterObj.zonename));  
        $dialogAddPool.find("#pod_name").text(fromdb(clusterObj.podname)); 
    }          
    else if(currentRightPanelJSP == "jsp/primarystorage.jsp") {
        var clusterObj = $leftmenuItem1.data("clusterObj");   
        zoneId = clusterObj.zoneid;
        podId = clusterObj.podid;    
        sourceClusterId = clusterObj.id;    
        $dialogAddPool.find("#zone_name").text(fromdb(clusterObj.zonename));  
        $dialogAddPool.find("#pod_name").text(fromdb(clusterObj.podname)); 
    }
                                         
    populateClusterFieldInAddPoolDialog($dialogAddPool, podId);   
        		 
    $button.unbind("click").bind("click", function(event) {           
        $dialogAddPool.find("#info_container").hide();	        
	    $dialogAddPool.find("#pool_cluster").change();
	    
        $("#dialog_add_pool")
	    .dialog('option', 'buttons', { 				    
		    "Add": function() { 	
		    	var $thisDialog = $(this);
		    	$thisDialog.find("#info_container").hide(); 
		    			    	
			    // validate values
				var protocol = $thisDialog.find("#add_pool_protocol").val();
				
			    var isValid = true;						    
			    isValid &= validateDropDownBox("Cluster", $thisDialog.find("#pool_cluster"), $thisDialog.find("#pool_cluster_errormsg"), false);  //required, reset error text					    				
			    isValid &= validateString("Name", $thisDialog.find("#add_pool_name"), $thisDialog.find("#add_pool_name_errormsg"));
				if (protocol == "nfs" || protocol == "PreSetup" || protocol == "SharedMountPoint") {
				    isValid &= validateString("Server", $thisDialog.find("#add_pool_nfs_server"), $thisDialog.find("#add_pool_nfs_server_errormsg"));	
					isValid &= validateString("Path", $thisDialog.find("#add_pool_path"), $thisDialog.find("#add_pool_path_errormsg"));	
				} else if(protocol == "iscsi") {
				    isValid &= validateString("Server", $thisDialog.find("#add_pool_nfs_server"), $thisDialog.find("#add_pool_nfs_server_errormsg"));	
					isValid &= validateString("Target IQN", $thisDialog.find("#add_pool_iqn"), $thisDialog.find("#add_pool_iqn_errormsg"));	
					isValid &= validateString("LUN #", $thisDialog.find("#add_pool_lun"), $thisDialog.find("#add_pool_lun_errormsg"));	
				} else if(protocol == "vmfs") {
					isValid &= validateString("vCenter Datacenter", $thisDialog.find("#add_pool_vmfs_dc"), $thisDialog.find("#add_pool_vmfs_dc_errormsg"));	
					isValid &= validateString("vCenter Datastore", $thisDialog.find("#add_pool_vmfs_ds"), $thisDialog.find("#add_pool_vmfs_ds_errormsg"));	
				}
				isValid &= validateString("Tags", $thisDialog.find("#add_pool_tags"), $thisDialog.find("#add_pool_tags_errormsg"), true);	//optional
			    if (!isValid) 
			        return;
			        			    
				$thisDialog.find("#spinning_wheel").show()  
							
				var array1 = [];
				array1.push("&zoneId="+zoneId);
		        array1.push("&podId="+podId);
				
				var clusterId = $thisDialog.find("#pool_cluster").val();
			    array1.push("&clusterid="+clusterId);	
				
			    var name = trim($thisDialog.find("#add_pool_name").val());
			    array1.push("&name="+todb(name));
			    
			    var server = trim($thisDialog.find("#add_pool_nfs_server").val());						
				
				var url = null;
				if (protocol == "nfs") {
					var path = trim($thisDialog.find("#add_pool_path").val());
					if(path.substring(0,1)!="/")
						path = "/" + path; 
					url = nfsURL(server, path);
				} 		
				else if (protocol == "PreSetup") {
					var path = trim($thisDialog.find("#add_pool_path").val());
					if(path.substring(0,1)!="/")
						path = "/" + path; 
					url = presetupURL(server, path);
				} 			
				else if (protocol == "SharedMountPoint") {
					var path = trim($thisDialog.find("#add_pool_path").val());
					if(path.substring(0,1)!="/")
						path = "/" + path; 
					url = SharedMountPointURL(server, path);
				} 
				else if (protocol == "vmfs") {
					var path = trim($thisDialog.find("#add_pool_vmfs_dc").val());
					if(path.substring(0,1)!="/")
						path = "/" + todb(path); 
					path += "/" + todb(trim($thisDialog.find("#add_pool_vmfs_ds").val()));
					url = vmfsURL("dummy", path);
				} 
				else {
					var iqn = trim($thisDialog.find("#add_pool_iqn").val());
					if(iqn.substring(0,1)!="/")
						iqn = "/" + iqn; 
					var lun = trim($thisDialog.find("#add_pool_lun").val());
					url = iscsiURL(server, iqn, lun);
				}
				array1.push("&url="+todb(url));
				
			    var tags = trim($thisDialog.find("#add_pool_tags").val());
				if(tags != null && tags.length > 0)
				    array1.push("&tags="+todb(tags));				    
			    
			    $.ajax({
				    data: createURL("command=createStoragePool" + array1.join("")),
				    dataType: "json",
				    success: function(json) {
				        $thisDialog.find("#spinning_wheel").hide();					       
				        $thisDialog.dialog("close");					
						 
						var item = json.createstoragepoolresponse.storagepool;	
												
						var $podArrow = $("#pod_"+podId).find("#pod_arrow");
						if($podArrow.hasClass("expanded_close")) {	
						    $podArrow.click();
						}
						
						var $clusterArrow = $("#cluster_"+clusterId).find("#cluster_arrow");
						if($clusterArrow.hasClass("expanded_close")) {	
						    $clusterArrow.click();
						}
						
						$("#cluster_"+clusterId+"_primarystorage").click();	                   
				    },			
                    error: function(XMLHttpResponse) {	  
						handleError(XMLHttpResponse, function() {
							handleErrorInDialog(XMLHttpResponse, $thisDialog);	
						});
                    }							    
			    });
		    }, 
		    "Cancel": function() { 
			    $(this).dialog("close"); 
		    } 
	    }).dialog("open");            
        return false;
    });             
}

function bindAddIpRangeToPodButton($leftmenuItem1) {       
    $("#add_iprange_button").unbind("click").bind("click", function(event) {   
        if($("#tab_content_ipallocation").css("display") == "none")
            $("#tab_ipallocation").click();    
            
        var podObj = $leftmenuItem1.data("jsonObj");               
        var zoneId = podObj.zoneid;        
        var podId = podObj.id;
        var podName = podObj.name;      
                
        $("#dialog_add_iprange_to_pod").find("#pod_name_label").text(podName);
                
        $("#dialog_add_iprange_to_pod")
	    .dialog('option', 'buttons', {
	        "Add": function() {             
	            var $thisDialog = $(this);		
			   				
				// validate values
				var isValid = true;	
				isValid &= validateIp("Netmask", $thisDialog.find("#netmask"), $thisDialog.find("#netmask_errormsg"));
				isValid &= validateIp("Gateway", $thisDialog.find("#guestgateway"), $thisDialog.find("#guestgateway_errormsg"));
				isValid &= validateIp("Start IP Range", $thisDialog.find("#startip"), $thisDialog.find("#startip_errormsg"));   //required
				isValid &= validateIp("End IP Range", $thisDialog.find("#endip"), $thisDialog.find("#endip_errormsg"), true);  //optional
				if (!isValid) 
				    return;							
				
				$thisDialog.find("#spinning_wheel").show(); 									
				
				var netmask = trim($thisDialog.find("#netmask").val());
				var guestgateway = trim($thisDialog.find("#guestgateway").val());
				var startip = trim($thisDialog.find("#startip").val());
				var endip = trim($thisDialog.find("#endip").val());		
				
				var array1 = [];
				array1.push("&vlan=untagged");	
				array1.push("&zoneid=" + zoneId);
				array1.push("&podId=" + podId);	
				array1.push("&forVirtualNetwork=false"); //direct VLAN			
				array1.push("&gateway="+todb(guestgateway));
				array1.push("&netmask="+todb(netmask));	
				array1.push("&startip="+todb(startip));
				if(endip != null && endip.length > 0)
				    array1.push("&endip="+todb(endip));	
				
				$.ajax({
				  data: createURL("command=createVlanIpRange" + array1.join("")),
					dataType: "json",
					success: function(json) {					    
					    $thisDialog.find("#spinning_wheel").hide();				        
				        $thisDialog.dialog("close");
					    
					    var item = json.createvlaniprangeresponse.vlan;
					    var $subgridItem = $("#network_tab_template").clone(true);
					    podNetworkJsonToTemplate(item, $subgridItem); 	
					    $subgridItem.find("#after_action_info").text(g_dictionary["label.adding.succeeded"]);
                        $subgridItem.find("#after_action_info_container").removeClass("error").addClass("success").show();  				                        
	                    $("#tab_content_ipallocation").find("#tab_container").append($subgridItem.fadeIn("slow"));	
					},
				    error: function(XMLHttpResponse) {					        				        
				        handleError(XMLHttpResponse, function() {				           
							handleErrorInDialog(XMLHttpResponse, $thisDialog);
						});
				    }
				});		        
	        },
	        "Cancel": function() {
	            $(this).dialog("close");
	        }		    
	    }).dialog("open");
        
        return false;
    });             
}

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

var podActionMap = {
    "label.action.edit.pod": {
        dialogBeforeActionFn: doEditPod  
    },
    "label.action.delete.pod": {                   
        isAsyncJob: false, 
        dialogBeforeActionFn : doDeletePod,        
        inProcessText: "label.action.delete.pod.processing",
        afterActionSeccessFn: function(json, $midmenuItem1, id) {       
            $midmenuItem1.slideUp("slow", function() {
                $(this).remove();                
                if(id.toString() == $("#right_panel_content").find("#tab_content_details").find("#id").text()) {
                    clearRightPanel();
                    podClearRightPanel();
                }                
            });           
        }
    }
}

function doEditPod($actionLink, $detailsTab, $midmenuItem1) {       
    $readonlyFields.hide();
    $editFields.show();  
    $detailsTab.find("#cancel_button, #save_button").show();
    
    $detailsTab.find("#cancel_button").unbind("click").bind("click", function(event){    
        cancelEditMode($detailsTab);     
        return false;
    });
    $detailsTab.find("#save_button").unbind("click").bind("click", function(event){        
        doEditPod2($actionLink, $detailsTab, $midmenuItem1, $readonlyFields, $editFields);              
        return false;
    });   
}

function doEditPod2($actionLink, $detailsTab, $midmenuItem1, $readonlyFields, $editFields) {    
    var jsonObj = $midmenuItem1.data("jsonObj");    
    var id = jsonObj.id;		
	var zoneid = jsonObj.zoneid;				
	var oldName = jsonObj.name;	
	var oldNetmask = jsonObj.netmask;	
	var oldStartip = jsonObj.startip;					
	var oldEndip = jsonObj.endip;	
	var oldGateway = jsonObj.gateway;
	
    // validate values
	var isValid = true;			
	isValid &= validateString("Name", $detailsTab.find("#name_edit"), $detailsTab.find("#name_edit_errormsg"));
	isValid &= validateIp("Netmask", $detailsTab.find("#netmask_edit"), $detailsTab.find("#netmask_edit_errormsg"));	
	isValid &= validateIp("Start IP Range", $detailsTab.find("#startIpRange_edit"), $detailsTab.find("#startIpRange_edit_errormsg"));  //required
	isValid &= validateIp("End IP Range", $detailsTab.find("#endIpRange_edit"), $detailsTab.find("#endIpRange_edit_errormsg"), true);  //optional
	isValid &= validateIp("Gateway", $detailsTab.find("#gateway_edit"), $detailsTab.find("#gateway_edit_errormsg"), true);  //optional when editing	
	if (!isValid) 
	    return;			
  
    var newName = trim($detailsTab.find("#name_edit").val());
	var newNetmask = trim($detailsTab.find("#netmask_edit").val());
	var newStartip = trim($detailsTab.find("#startIpRange_edit").val());
	var newEndip = trim($detailsTab.find("#endIpRange_edit").val());	
	var newIpRange = getIpRange(newStartip, newEndip);	
	var newGateway = trim($detailsTab.find("#gateway_edit").val());				
        
    var array1 = []; 	    
    if(newName != oldName)
        array1.push("&name="+todb(newName));
    if(newNetmask != oldNetmask)
        array1.push("&netmask="+todb(newNetmask));
    if(newStartip != oldStartip)
        array1.push("&startIp="+todb(newStartip));    
    if(newEndip != oldEndip && newEndip != null && newEndip.length > 0) { 
        if(newStartip == oldStartip) {
            array1.push("&startIp="+todb(newStartip));  //startIp needs to be passed to updatePod API when endIp is passed to updatePod API.
        }
		array1.push("&endIp="+todb(newEndip));	
    }
	if(newGateway != oldGateway && newGateway != null && newGateway.length > 0)				             
	    array1.push("&gateway="+todb(newGateway)); 	
	
	if(array1.length > 0) {
	    $.ajax({
	      data: createURL("command=updatePod&id="+id+array1.join("")),
		    dataType: "json",
		    success: function(json) {		   	   				    
		        var item = json.updatepodresponse.pod;	
		        $midmenuItem1.data("jsonObj", item);
		        $midmenuItem1.find("#pod_name").text(item.name);
		        podJsonToRightPanel($midmenuItem1);			    
    		    
		        $editFields.hide();      
                $readonlyFields.show();       
                $("#save_button, #cancel_button").hide();      			
		    }
	    });	   
	}
	else {
	    $editFields.hide();      
        $readonlyFields.show();       
        $("#save_button, #cancel_button").hide();   
	}
}

function doDeletePod($actionLink, $detailsTab, $midmenuItem1) {       
    var jsonObj = $midmenuItem1.data("jsonObj");
	var id = jsonObj.id;
		
	$("#dialog_confirmation")
	.text(dictionary["message.action.delete.pod"])
	.dialog('option', 'buttons', { 					
		"Confirm": function() { 			
			$(this).dialog("close");			
			var apiCommand = "command=deletePod&id="+id;
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);	
		}, 
		"Cancel": function() { 
			$(this).dialog("close"); 
		}
	}).dialog("open");
}

