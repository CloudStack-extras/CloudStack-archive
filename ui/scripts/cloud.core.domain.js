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

var $selectedDomainTreeNode;
var defaultRootLevel = 0;	   
var childParentMap = {};  //map childDomainId to parentDomainId
var domainIdNameMap = {}; //map domainId to domainName    

function drawRootNode(rootDomainId) {    
    var $domainTree = $("#leftmenu_domain_tree").find("#tree_container").hide();
   
    $.ajax({
        data: createURL("command=listDomains&id="+rootDomainId), 
        dataType: "json",
        async: false,
        success: function(json) {					        
            var domains = json.listdomainsresponse.domain;	
            $domainTree.empty();			        	    
	        if (domains != null && domains.length > 0) {				   					    
			    var node = drawNode(domains[0], $("#domain_tree_node_template"), $domainTree); 
			    
			    var treeLevelsbox = node.find(".tree_levelsbox");	//root node shouldn't have margin-left:20px				   
			    if(treeLevelsbox!=null && treeLevelsbox.length >0)
			        treeLevelsbox[0].style.marginLeft="0px";        //set root node's margin-left to 0px.
			}				
            $domainTree.show();			
        }
    });
}

function drawNode(json, template, container) {		  
    if("parentdomainid" in json)
        childParentMap[json.id] = json.parentdomainid;	//map childDomainId to parentDomainId   
    domainIdNameMap[json.id] = json.name;               //map domainId to domainName

    var $treeNode = template.clone(true).attr("id", "domain_tree_node_template_clone");	  
    $treeNode.find("#domain_indent").css("marginLeft", (30*(json.level+1)));           
    $treeNode.attr("id", "domain_"+fromdb(json.id));	         
    $treeNode.data("jsonObj", json).data("domainLevel", json.level); 	      
    $treeNode.find("#domain_title_container").attr("id", "domain_title_container_"+fromdb(json.id)); 	        
    $treeNode.find("#domain_expand_icon").attr("id", "domain_expand_icon_"+fromdb(json.id)); 
    $treeNode.find("#domain_name").attr("id", "domain_name_"+fromdb(json.id)).text(fromdb(json.name));        	              	
    $treeNode.find("#domain_children_container").attr("id", "domain_children_container_"+fromdb(json.id));          
    container.append($treeNode.show());	 
    return $treeNode;   	       
}          

function drawTree(id, container) {
	var $treeNodeTemplate = $("#domain_tree_node_template");
    $.ajax({
	    data: createURL("command=listDomainChildren&id="+id),
	    dataType: "json",
	    async: false,
	    success: function(json) {					        
	        var domains = json.listdomainchildrenresponse.domain;				        	    
		    if (domains != null && domains.length > 0) {					    
			    for (var i = 0; i < domains.length; i++) {						    
				    drawNode(domains[i], $treeNodeTemplate, container);	
			    }
		    }				
	    }
    }); 
}	

function clickExpandIcon(domainId) {
    var $treeNode = $("#domain_"+domainId);
    var expandIcon = $treeNode.find("#domain_expand_icon_"+domainId);
    if (expandIcon.hasClass("expanded_close")) {	
		drawTree(domainId, $treeNode.find("#domain_children_container_"+domainId));								
		expandIcon.removeClass("expanded_close").addClass("expanded_open");
	} 
	else if (expandIcon.hasClass("expanded_open")) {	
	    $treeNode.find("#domain_children_container_"+domainId).empty();
		expandIcon.removeClass("expanded_open").addClass("expanded_close");
	}			
}					

function domainAccountJSONToTemplate(jsonObj, $template) {   
    $template.data("jsonObj", jsonObj);  
    $template.find("#grid_header_title").text(fromdb(jsonObj.name));
    $template.find("#id").text(jsonObj.id);
    $template.find("#role").text(toRole(jsonObj.accounttype));
    $template.find("#account").text(fromdb(jsonObj.name));
    $template.find("#domain").text(fromdb(jsonObj.domain));
    $template.find("#vm_total").text(jsonObj.vmtotal);
    $template.find("#ip_total").text(jsonObj.iptotal);
    $template.find("#bytes_received").text(convertBytes(jsonObj.receivedbytes));
    $template.find("#bytes_sent").text(convertBytes(jsonObj.sentbytes));
    $template.find("#state").text(jsonObj.state);
}

function afterLoadDomainJSP() {
    hideMiddleMenu();   
	clearMiddleMenu();
	 
	initDialog("dialog_confirmation_delete_domain"); 
		
    if(isAdmin()) {        
	    var $topButtonContainer = clearButtonsOnTop();			    	       
	    $("#top_buttons").appendTo($topButtonContainer); 	
	    $("#top_buttons").find("#add_domain_button").show();	
        initAddDomainDialog();        
        $("#dialog_confirmation_delete_domain").find("#force_delete_domain_container").show();		 
    }
    
    //switch between different tabs 
    var tabArray = [$("#tab_details"), $("#tab_admin_account"), $("#tab_resource_limits")];
    var tabContentArray = [$("#tab_content_details"), $("#tab_content_admin_account"), $("#tab_content_resource_limits")];
    var afterSwitchFnArray = [domainJsonToDetailsTab, domainJsonToAdminAccountTab ,domainJsonToResourceLimitsTab ];
    switchBetweenDifferentTabs(tabArray, tabContentArray, afterSwitchFnArray);   
    
    $readonlyFields  = $("#tab_content_details").find("#name");
    $editFields = $("#tab_content_details").find("#name_edit");               
}

function initAddDomainDialog() {
    initDialog("dialog_add_domain", 450);
    
    var $dialogAddDomain = $("#dialog_add_domain");              
	   
    $("#add_domain_button").unbind("click").bind("click", function(event) { 
        $dialogAddDomain.find("#add_domain_name").val("");
        
        $dialogAddDomain.find("#parent_domain").val($("#right_panel_content").find("#tab_content_details").find("#name").text()); 
        var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
        var jsonObj = $midmenuItem1.data("jsonObj");
        autoCompleteDomains.push(jsonObj);
        
        applyAutoCompleteToDomainField($dialogAddDomain.find("#parent_domain"));      
        
		$dialogAddDomain
		.dialog('option', 'buttons', { 					
			"Add": function() { 	
			    var $thisDialog = $(this);
			    				    			
				// validate values
				var isValid = true;					
				isValid &= validateString("Name", $thisDialog.find("#add_domain_name"), $thisDialog.find("#add_domain_name_errormsg"));					
				
				isValid &= validateString("Parent Domain", $thisDialog.find("#parent_domain"), $thisDialog.find("#parent_domain_errormsg"), false);                             //required	
				var domainName = $thisDialog.find("#parent_domain").val();
				var parentDomainId;
				if(domainName != null && domainName.length > 0) { 				    
				    if(autoCompleteDomains != null && autoCompleteDomains.length > 0) {									
					    for(var i=0; i < autoCompleteDomains.length; i++) {					        
					      if(fromdb(autoCompleteDomains[i].name).toLowerCase() == domainName.toLowerCase()) {
					          parentDomainId = autoCompleteDomains[i].id;
					          break;	
					      }
				        } 					   			    
				    }					    				    
				    if(parentDomainId == null) {
				        showError(false, $thisDialog.find("#parent_domain"), $thisDialog.find("#parent_domain_errormsg"), g_dictionary["label.not.found"]);
				        isValid &= false;
				    }				    
				}				
				
				if (!isValid) 
				    return;
				 
				$thisDialog.dialog("close");    			
							
				var array1 = [];	
				var name = trim($thisDialog.find("#add_domain_name").val());	
				array1.push("&name="+todb(name));
								
				array1.push("&parentdomainid="+parentDomainId);	
						
				$.ajax({
				    data: createURL("command=createDomain"+array1.join("")),
					dataType: "json",
					async: false,
					success: function(json) {	   
						var item = json.createdomainresponse.domain;						
						var $parentDomainNode = $("#leftmenu_domain_tree").find("#domain_"+item.parentdomainid);
											
					    var $expandIcon = $parentDomainNode.find("#domain_expand_icon_"+item.parentdomainid);
					    if($expandIcon.hasClass("expanded_close"))
					        $expandIcon.click(); //expand parentDomain node		
					    else			    
					        drawNode(item, $("#domain_tree_node_template"), $("#domain_children_container_"+item.parentdomainid));	
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

function domainToRightPanel($midmenuItem1) {  
    if(currentRightPanelJSP != "jsp/domain.jsp") {     
        $("#right_panel").load("jsp/domain.jsp", function(){     
            currentRightPanelJSP = "jsp/domain.jsp";
            afterLoadDomainJSP();
			domainToRightPanel2($midmenuItem1);       
		});        
    }
    else {        
        domainToRightPanel2($midmenuItem1); 
    }
}

function domainToRightPanel2($midmenuItem1) {
    $("#right_panel_content").data("$midmenuItem1", $midmenuItem1);  
    cancelEditMode($("#tab_content_details")); 
    $("#tab_details").click();   
}

function domainJsonToDetailsTab() {    
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
    if($midmenuItem1 == null) {
        domainClearDetailsTab();
        return;
    }
    
    var jsonObj = $midmenuItem1.data("jsonObj");  
    if(jsonObj == null) {
        domainClearDetailsTab();
        return;
    }
    
    var domainId = jsonObj.id;   
           
    $("#right_panel").data("onRefreshFn", function() {	    
	    $("#domain_name_"+domainId).click();
	});	
        	
	$.ajax({
	    data: createURL("command=listDomains&id="+domainId),
	    dataType: "json",
	    success: function(json) {
	        var items = json.listdomainsresponse.domain;
            if(items != null && items.length > 0) {
                jsonObj = items[0];
                $midmenuItem1.data("jsonObj", jsonObj);                  
            }
	    }
	});		 
    var $thisTab = $("#right_panel_content").find("#tab_content_details");    
    $thisTab.find("#id").text(domainId);
    $thisTab.find("#grid_header_title").text(fromdb(jsonObj.name));	
    $thisTab.find("#name").text(fromdb(jsonObj.name));	    
    $thisTab.find("#name_edit").val(fromdb(jsonObj.name));	    		   
				  	
  	$.ajax({
	    cache: false,				
	    data: createURL("command=listAccounts&domainid="+domainId),
	    dataType: "json",
	    success: function(json) {				       
		    var accounts = json.listaccountsresponse.account;					
		    if (accounts != null) 	
		        $thisTab.find("#redirect_to_account_page").text(accounts.length);
		    else 
		        $thisTab.find("#redirect_to_account_page").text("0");		
	    }		
    });		 
  			 				 			 
    $.ajax({
	    cache: false,				
	    data: createURL("command=listVirtualMachines&domainid="+domainId),
	    dataType: "json",
	    success: function(json) {
		    var instances = json.listvirtualmachinesresponse.virtualmachine;					
		    if (instances != null) 	
		        $thisTab.find("#redirect_to_instance_page").text(instances.length);	
		    else 
		        $thisTab.find("#redirect_to_instance_page").text("0");	
	    }		
    });		
     			    
    $.ajax({
	    cache: false,				
	    data: createURL("command=listVolumes&domainid="+domainId),
	    dataType: "json",
	    success: function(json) {
		    var volumes = json.listvolumesresponse.volume;						
		    if (volumes != null) 	
		        $thisTab.find("#redirect_to_volume_page").text(volumes.length);	
		    else 
		        $thisTab.find("#redirect_to_volume_page").text("0");		
	    }		
    });
    
    //actions ***   
    var $actionLink = $thisTab.find("#action_link"); 
    bindActionLink($actionLink);
        
    var $actionMenu = $actionLink.find("#action_menu");
    $actionMenu.find("#action_list").empty();   
    var noAvailableActions = true;
    if(isAdmin()) { 
    	if(domainId != 1) { //"ROOT" domain is not allowed to edit or delete
	        buildActionLinkForTab("label.action.edit.domain", domainActionMap, $actionMenu, $midmenuItem1, $thisTab);   
	        buildActionLinkForTab("label.action.delete.domain", domainActionMap, $actionMenu, $midmenuItem1, $thisTab);          
	        noAvailableActions = false; 
    	}    	
    }   
	buildActionLinkForTab("label.action.update.resource.count", domainActionMap, $actionMenu, $midmenuItem1, $thisTab);   
    noAvailableActions = false; 
    
    // no available actions 
	if(noAvailableActions == true) {
	    $actionMenu.find("#action_list").append($("#no_available_actions").clone().show());
	}	 
}

function domainJsonToAdminAccountTab() {     
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
    if($midmenuItem1 == null) {
        domainClearAdminAccountTab();
        return;
    }
    
    var jsonObj = $midmenuItem1.data("jsonObj"); 
    if(jsonObj == null) {
        domainClearAdminAccountTab();
        return; 
    }
    
    var domainId = jsonObj.id;
   
    listAdminAccounts(domainId);  
}

function domainJsonToResourceLimitsTab() {       
	if (isAdmin() || (isDomainAdmin() && (g_domainid != domainId))) {	
	    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
	    if($midmenuItem1 == null) {
	        domainClearResourceLimitsTab();    
	        return;
	    }
	    
        var jsonObj = $midmenuItem1.data("jsonObj");  
        if(jsonObj == null) {
            domainClearResourceLimitsTab();    
            return;
        }
        
        var domainId = jsonObj.id;    
				
		var $resourceLimitsTab = $("#right_panel_content #tab_content_resource_limits");	
		$.ajax({
			cache: false,				
			data: createURL("command=listResourceLimits&domainid="+domainId),
			dataType: "json",
			success: function(json) {
				var limits = json.listresourcelimitsresponse.resourcelimit;		
				var preInstanceLimit, preIpLimit, preDiskLimit, preSnapshotLimit, preTemplateLimit = -1;
				if (limits != null) {	
					for (var i = 0; i < limits.length; i++) {
						var limit = limits[i];
						switch (limit.resourcetype) {
							case "0":
								preInstanceLimit = limit.max;
								$resourceLimitsTab.find("#limits_vm").text(preInstanceLimit);
								$resourceLimitsTab.find("#limits_vm_edit").val(preInstanceLimit);
								break;
							case "1":
								preIpLimit = limit.max;
								$resourceLimitsTab.find("#limits_ip").text(preIpLimit);
								$resourceLimitsTab.find("#limits_ip_edit").val(preIpLimit);
								break;
							case "2":
								preDiskLimit = limit.max;
								$resourceLimitsTab.find("#limits_volume").text(preDiskLimit);
								$resourceLimitsTab.find("#limits_volume_edit").val(preDiskLimit);
								break;
							case "3":
								preSnapshotLimit = limit.max;
								$resourceLimitsTab.find("#limits_snapshot").text(preSnapshotLimit);
								$resourceLimitsTab.find("#limits_snapshot_edit").val(preSnapshotLimit);
								break;
							case "4":
								preTemplateLimit = limit.max;
								$resourceLimitsTab.find("#limits_template").text(preTemplateLimit);
								$resourceLimitsTab.find("#limits_template_edit").val(preTemplateLimit);
								break;
						}
					}
				}						
			}
		});		
		
		domainToResourceLimitsTab();	
		$("#tab_resource_limits").show();	
	} 
	else {
		$("#tab_resource_limits").hide();
	}		 		 
}

function domainJsonClearRightPanel() {
    domainClearDetailsTab();
    domainClearAdminAccountTab();
    domainClearResourceLimitsTab();    
}

function domainClearDetailsTab() {
    var $thisTab = $("#right_panel_content").find("#tab_content_details");
    $thisTab.find("#id").text("");
    $thisTab.find("#grid_header_title").text("");	
    $thisTab.find("#name").text("");	
    $thisTab.find("#name_edit").val("");
    $thisTab.find("#redirect_to_account_page").text("");	
    $thisTab.find("#redirect_to_instance_page").text("");	
    $thisTab.find("#redirect_to_volume_page").text("");	
    
    var $actionMenu = $thisTab.find("#action_link #action_menu");
    $actionMenu.find("#action_list").empty();    
	$actionMenu.find("#action_list").append($("#no_available_actions").clone().show());		 
}

function domainClearAdminAccountTab() {
    var $thisTab = $("#right_panel_content").find("#tab_content_admin_account");
    $thisTab.empty();	
}

function domainClearResourceLimitsTab() {
    var $thisTab = $("#right_panel_content").find("#tab_content_resource_limits");

    $thisTab.find("#limits_vm").text("");
	$thisTab.find("#limits_vm_edit").val("");
	
	$thisTab.find("#limits_ip").text("");
	$thisTab.find("#limits_ip_edit").val("");
	
	$thisTab.find("#limits_volume").text("");
	$thisTab.find("#limits_volume_edit").val("");
	
	$thisTab.find("#limits_snapshot").text("");
	$thisTab.find("#limits_snapshot_edit").val("");
	
	$thisTab.find("#limits_template").text("");
	$thisTab.find("#limits_template_edit").val("");	
		
	var $actionMenu = $thisTab.find("#action_link #action_menu");
    $actionMenu.find("#action_list").empty();    
	$actionMenu.find("#action_list").append($("#no_available_actions").clone().show());		 	
}

function domainToResourceLimitsTab() {   
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
    if($midmenuItem1 == null)
        return;
    
    var jsonObj = $midmenuItem1.data("jsonObj");
    if(jsonObj == null)
        return;    
    
    var $thisTab = $("#right_panel_content").find("#tab_content_resource_limits");  
    
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
    var noAvailableActions = true;
    if(isAdmin()) {
        buildActionLinkForTab("label.action.edit.resource.limits", domainResourceLimitsActionMap, $actionMenu, $midmenuItem1, $thisTab);	
        noAvailableActions = false;		
    }    
    // no available actions 
	if(noAvailableActions == true) {
	    $actionMenu.find("#action_list").append($("#no_available_actions").clone().show());
	}	 	
}

function bindEventHandlerToDomainTreeNode() {
	$("#domain_tree_node_template").unbind("click").bind("click", function(event) {			     
		var $thisNode = $(this);
		var target = $(event.target);
		var action = target.attr("id");
		var id = $thisNode.attr("id");
		var jsonObj = $thisNode.data("jsonObj");	
		var domainId = jsonObj.id;	
		var domainName = jsonObj.name;										
		if (action.indexOf("domain_expand_icon")!=-1) {		
		    clickExpandIcon(domainId);					
		}
		else {
            if($selectedDomainTreeNode != null && $selectedDomainTreeNode.data("jsonObj") != null)
                $selectedDomainTreeNode.find("#domain_title_container_"+$selectedDomainTreeNode.data("jsonObj").id).removeClass("selected");      
            $thisNode.find("#domain_title_container_"+domainId).addClass("selected");
            $selectedDomainTreeNode = $thisNode;            
            domainToRightPanel($thisNode);                   
		}					
		return false;
    });
}

function updateResourceLimitForDomain(domainId, type, max, $readonlyField) {
	$.ajax({
	    data: createURL("command=updateResourceLimit&domainid="+domainId+"&resourceType="+type+"&max="+max),
		dataType: "json",
		async: false,
		success: function(json) {	
		    $readonlyField.text(max);							    												
		}
	});
}

function listAdminAccounts(domainId) {   
    var $thisTab = $("#right_panel_content").find("#tab_content_admin_account");
    $thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();   
    	    	   	
    var accountType = (domainId==1)? 1: 2; 	    		
    $.ajax({
		cache: false,				
	    data: createURL("command=listAccounts&domainid="+domainId+"&accounttype="+accountType),
		dataType: "json",
		success: function(json) {
			var items = json.listaccountsresponse.account;					
			var $container = $thisTab.find("#tab_container").empty();	
			if (items != null && items.length > 0) {					    
				var $template = $("#admin_account_tab_template");				
				for (var i = 0; i < items.length; i++) {
					var $newTemplate = $template.clone(true);
	                domainAccountJSONToTemplate(items[i], $newTemplate); 
	                $container.append($newTemplate.show());	
				}				    				
			} 		
			$thisTab.find("#tab_spinning_wheel").hide();    
            $thisTab.find("#tab_container").show();						         
		}		
	});		
}		

var domainResourceLimitsActionMap = {  
    "label.action.edit.resource.limits": {
        dialogBeforeActionFn: doEditResourceLimits
    }
}   

function doEditResourceLimits($actionLink, $detailsTab, $midmenuItem1) {       
    var $readonlyFields  = $detailsTab.find("#limits_vm, #limits_ip, #limits_volume, #limits_snapshot, #limits_template");
    var $editFields = $detailsTab.find("#limits_vm_edit, #limits_ip_edit, #limits_volume_edit, #limits_snapshot_edit, #limits_template_edit"); 
           
    $readonlyFields.hide();
    $editFields.show();  
    $detailsTab.find("#cancel_button, #save_button").show();
    
    $detailsTab.find("#cancel_button").unbind("click").bind("click", function(event){    
        $editFields.hide();
        $readonlyFields.show();   
        $("#save_button, #cancel_button").hide();       
        return false;
    });
    $detailsTab.find("#save_button").unbind("click").bind("click", function(event){        
        doEditResourceLimits2($actionLink, $detailsTab, $midmenuItem1, $readonlyFields, $editFields);   
        return false;
    });   
}

function doEditResourceLimits2($actionLink, $detailsTab, $midmenuItem1, $readonlyFields, $editFields) {  
    var $resourceLimitsTab = $("#right_panel_content #tab_content_resource_limits");

    var isValid = true;	        			
	isValid &= validateInteger("Instance Limit", $resourceLimitsTab.find("#limits_vm_edit"), $resourceLimitsTab.find("#limits_vm_edit_errormsg"), -1, 32000, false);
	isValid &= validateInteger("Public IP Limit", $resourceLimitsTab.find("#limits_ip_edit"), $resourceLimitsTab.find("#limits_ip_edit_errormsg"), -1, 32000, false);
	isValid &= validateInteger("Disk Volume Limit", $resourceLimitsTab.find("#limits_volume_edit"), $resourceLimitsTab.find("#limits_volume_edit_errormsg"), -1, 32000, false);
	isValid &= validateInteger("Snapshot Limit", $resourceLimitsTab.find("#limits_snapshot_edit"), $resourceLimitsTab.find("#limits_snapshot_edit_errormsg"), -1, 32000, false);
	isValid &= validateInteger("Template Limit", $resourceLimitsTab.find("#limits_template_edit"), $resourceLimitsTab.find("#limits_template_edit_errormsg"), -1, 32000, false);
	if (!isValid) 
	    return;
								
	var jsonObj = $midmenuItem1.data("jsonObj");
	var domainId = jsonObj.id;
	
	var instanceLimit = trim($resourceLimitsTab.find("#limits_vm_edit").val());
	var ipLimit = trim($resourceLimitsTab.find("#limits_ip_edit").val());
	var diskLimit = trim($resourceLimitsTab.find("#limits_volume_edit").val());
	var snapshotLimit = trim($resourceLimitsTab.find("#limits_snapshot_edit").val());
	var templateLimit = trim($resourceLimitsTab.find("#limits_template_edit").val());
				
	if (instanceLimit != $resourceLimitsTab.find("#limits_vm").text()) {
		updateResourceLimitForDomain(domainId, 0, instanceLimit, $resourceLimitsTab.find("#limits_vm"));
	}
	if (ipLimit != $resourceLimitsTab.find("#limits_ip").text()) {
		updateResourceLimitForDomain(domainId, 1, ipLimit, $resourceLimitsTab.find("#limits_ip"));
	}
	if (diskLimit != $resourceLimitsTab.find("#limits_volume").text()) {
		updateResourceLimitForDomain(domainId, 2, diskLimit, $resourceLimitsTab.find("#limits_volume"));
	}
	if (snapshotLimit != $resourceLimitsTab.find("#limits_snapshot").text()) {
		updateResourceLimitForDomain(domainId, 3, snapshotLimit, $resourceLimitsTab.find("#limits_snapshot"));
	}
	if (templateLimit != $resourceLimitsTab.find("#limits_template").text()) {
		updateResourceLimitForDomain(domainId, 4, templateLimit, $resourceLimitsTab.find("#limits_template"));
	}    
	
	$editFields.hide();      
    $readonlyFields.show();       
    $("#save_button, #cancel_button").hide();      
}


function doEditDomain($actionLink, $detailsTab, $midmenuItem1) {     
    $readonlyFields.hide();
    $editFields.show();  
    $detailsTab.find("#cancel_button, #save_button").show();
    
    $detailsTab.find("#cancel_button").unbind("click").bind("click", function(event){    
        cancelEditMode($detailsTab);     
        return false;
    });
    $detailsTab.find("#save_button").unbind("click").bind("click", function(event){        
        doEditDomain2($actionLink, $detailsTab, $midmenuItem1, $readonlyFields, $editFields);   
        return false;
    });   
}

function doEditDomain2($actionLink, $detailsTab, $midmenuItem1, $readonlyFields, $editFields) {     
    // validate values
    var isValid = true;					
    isValid &= validateString("Name", $detailsTab.find("#name_edit"), $detailsTab.find("#name_edit_errormsg"));    
    if (!isValid) 
        return;
       
    var jsonObj = $midmenuItem1.data("jsonObj"); 
	var id = jsonObj.id;
	
	var array1 = [];
	
	var newName = $detailsTab.find("#name_edit").val();
	if(newName != jsonObj.name) 	    
	    array1.push("&name="+todb(newName));
	
	if(array1.length > 0) {		
	    $.ajax({
	        data: createURL("command=updateDomain&id="+id+array1.join("")),
		    dataType: "json",
		    async: false,
		    success: function(json) {			        
		        jsonObj = json.updatedomainresponse.domain;
		        $midmenuItem1.data("jsonObj", jsonObj);		   
		        domainJsonToDetailsTab();	
		        
		        $("#leftmenu_domain_tree").find("#tree_container").find("#domain_name_"+id).text(newName);	         
		    }
	    });
	}
			        	
    $editFields.hide();      
    $readonlyFields.show();       
    $("#save_button, #cancel_button").hide();  		  
}

function doUpdateResourceCountForDomain($actionLink, $detailsTab, $midmenuItem1) {      
	var jsonObj = $midmenuItem1.data("jsonObj");
	var id = jsonObj.id;			
	var apiCommand = "command=updateResourceCount&domainid="+id;
	doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);			
}

function doDeleteDomain($actionLink, $detailsTab, $midmenuItem1) {       
    var jsonObj = $midmenuItem1.data("jsonObj");
	var id = jsonObj.id;
		
	$("#dialog_confirmation_delete_domain")	
	.dialog('option', 'buttons', { 					
		"Confirm": function() { 			
			$(this).dialog("close");
			var isForced = $("#dialog_confirmation_delete_domain").find("#force_delete_domain").attr("checked").toString();
			var apiCommand = "command=deleteDomain&id="+id+"&cleanup="+isForced;       
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);				
		}, 
		"Cancel": function() { 
			$(this).dialog("close"); 
		}
	}).dialog("open");
}

var domainActionMap = {        
    "label.action.edit.domain": {
        dialogBeforeActionFn: doEditDomain
    },   
    "label.action.update.resource.count": {   
    	isAsyncJob: false,   
    	dialogBeforeActionFn : doUpdateResourceCountForDomain,                     
        inProcessText: "label.action.update.resource.count.processing",
        afterActionSeccessFn: function(json, $midmenuItem1, id){}           
    },    
    "label.action.delete.domain": {           
        isAsyncJob: true,
        dialogBeforeActionFn : doDeleteDomain,          
        asyncJobResponse: "deletedomainresponse",          
        inProcessText: "label.action.delete.domain.processing",
        afterActionSeccessFn: function(json, $midmenuItem1, id) {        
    		$midmenuItem1.remove();   	
            if(id.toString() == $("#right_panel_content").find("#tab_content_details").find("#id").text()) {
                clearRightPanel();
                domainJsonClearRightPanel();
            }            
        }
    }    
} 