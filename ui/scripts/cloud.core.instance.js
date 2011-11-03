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

function vmGetSearchParams() {
    var moreCriteria = [];	

    var searchInput = $("#basic_search").find("#search_input").val();	 
    if (searchInput != null && searchInput.length > 0) {	           
        moreCriteria.push("&keyword="+todb(searchInput));	       
    }     

	var $advancedSearchPopup = getAdvancedSearchPopupInSearchContainer();
	if ($advancedSearchPopup.length > 0 && $advancedSearchPopup.css("display") != "none" ) {
		if($advancedSearchPopup.find("#adv_search_state").length > 0) {		
		    var state = $advancedSearchPopup.find("#adv_search_state").val();				
		    if (state!=null && state.length > 0) 
			    moreCriteria.push("&state="+todb(state));	
		}
				
		var zone = $advancedSearchPopup.find("#adv_search_zone").val();		
	    if (zone!=null && zone.length > 0) 
			moreCriteria.push("&zoneid="+todb(zone));			
		
	    if ($advancedSearchPopup.find("#adv_search_domain_li").css("display") != "none"
	        && $advancedSearchPopup.find("#domain").hasClass("textwatermark") == false) {
	        var domainName = $advancedSearchPopup.find("#domain").val();
	        if (domainName != null && domainName.length > 0) { 	
				var domainId;							    
			    if(autoCompleteDomains != null && autoCompleteDomains.length > 0) {									
				    for(var i=0; i < autoCompleteDomains.length; i++) {					        
				      if(fromdb(autoCompleteDomains[i].name).toLowerCase() == domainName.toLowerCase()) {
				          domainId = autoCompleteDomains[i].id;
				          break;	
				      }
			        } 					   			    
			    } 	     	
	            if(domainId == null) { 
			        showError(false, $advancedSearchPopup.find("#domain"), $advancedSearchPopup.find("#domain_errormsg"), g_dictionary["label.not.found"]);
			    }
			    else { //e.g. domainId == 5 (number)
			        showError(true, $advancedSearchPopup.find("#domain"), $advancedSearchPopup.find("#domain_errormsg"), null)
			        moreCriteria.push("&domainid="+todb(domainId));	
			    }
			}
	    }
						
		if ($advancedSearchPopup.find("#adv_search_account_li").css("display") != "none" 
    	    && $advancedSearchPopup.find("#adv_search_account").hasClass("textwatermark") == false) {			    
	        var account = $advancedSearchPopup.find("#adv_search_account").val();	
	        if(account != null && account.length > 0) 
		        moreCriteria.push("&account="+todb(account));	
        }
	} 	
	
	return moreCriteria.join("");          
}
function instanceBuildSubMenu() {    
    if (isAdmin() || isDomainAdmin()) {
		$("#leftmenu_instance_expandedbox").find("#leftmenu_instances_my_instances_container, #leftmenu_instances_all_instances_container, #leftmenu_instances_running_instances_container, #leftmenu_instances_stopped_instances_container, #leftmenu_instances_destroyed_instances_container ").show();
    } 	
    else if(isUser()) {	 
		$("#leftmenu_instance_expandedbox").find("#leftmenu_instances_all_instances_container, #leftmenu_instances_running_instances_container, #leftmenu_instances_stopped_instances_container").show();
        
		/*
		$.ajax({
            cache: false,
            data: createURL("command=listInstanceGroups"),	       
            dataType: "json",
            success: function(json) {	  
                $("#leftmenu_instance_group_container").empty();          
                var instancegroups = json.listinstancegroupsresponse.instancegroup;	        	
        	    if(instancegroups!=null && instancegroups.length>0) {           
	                for(var i=0; i < instancegroups.length; i++) {		                
	                    instanceBuildSubMenu2(instancegroups[i].name, ("listVirtualMachines&groupid="+instancegroups[i].id));   
	                }
	            }
            }
        });  
        */
    }    
}

function instanceBuildSubMenu2(label, commandString) {   
    var $newSubMenu = $("#leftmenu_secondindent_template").clone();
    $newSubMenu.find("#label").text(label);    
    bindAndListMidMenuItems($newSubMenu, commandString, vmGetSearchParams, "listvirtualmachinesresponse", "virtualmachine", "jsp/instance.jsp", afterLoadInstanceJSP, vmToMidmenu, vmToRightPanel, getMidmenuId, true);
    $("#leftmenu_instance_group_container").append($newSubMenu.show());
}

var $doTemplateNo, $doTemplateCustom,$doTemplateExisting, $soTemplate;
var $selectedVmWizardTemplate;	
var osTypeMap = {};
function afterLoadInstanceJSP() {
	$("#right_panel_content").data("clearRightPanelFn", vmClearRightPanel);
			
	$doTemplateNo = $("#vm_popup_disk_offering_template_no");
	$doTemplateCustom = $("#vm_popup_disk_offering_template_custom");
	$doTemplateExisting = $("#vm_popup_disk_offering_template_existing");
	$soTemplate = $("#vm_popup_service_offering_template");		
			
	initVMWizard();
	bindStartVMButton();    
	bindStopVMButton(); 
	bindRebootVMButton();    
	bindDestroyVMButton();
			
	// switch between different tabs 
	var tabArray = [$("#tab_details"), $("#tab_nic"), $("#tab_securitygroup"), $("#tab_volume"), $("#tab_statistics")];
	var tabContentArray = [$("#tab_content_details"), $("#tab_content_nic"), $("#tab_content_securitygroup"), $("#tab_content_volume"), $("#tab_content_statistics")];
	var afterSwitchFnArray = [vmJsonToDetailsTab, vmJsonToNicTab, vmJsonToSecurityGroupTab, vmJsonToVolumeTab, vmJsonToStatisticsTab];
	switchBetweenDifferentTabs(tabArray, tabContentArray, afterSwitchFnArray);   
	
	if(getDirectAttachSecurityGroupsEnabled() == "true") {
		$("#tab_securitygroup").show();
	}		
	else {
		$("#tab_securitygroup").hide();		
	}	
	
    // dialogs
    initDialog("dialog_detach_iso_from_vm");       	
   	initDialog("dialog_attach_iso");  
    initDialog("dialog_change_service_offering", 600);     
    initDialog("dialog_create_template_from_vm", 400); 
    initDialog("dialog_create_template_from_volume", 400);      
	initDialog("dialog_migrate_instance", 600);
	initDialog("dialog_confirmation_stop_vm"); 
	
	var $dialogStopVm = $("#dialog_confirmation_stop_vm");
	if(isAdmin()) {		
		$dialogStopVm.find("#force_stop_instance_container").show();		   
	}
		
    $.ajax({
	    data: createURL("command=listOsTypes"),
		dataType: "json",
		async: false,
		success: function(json) {		    
			types = json.listostypesresponse.ostype;
			var osTypeDropdown1 = $("#right_panel_content").find("#tab_content_details").find("#ostypename_edit").empty(); 
			var osTypeDropdown2 = $("#dialog_create_template_from_vm #create_template_os_type").empty();
			var osTypeDropdown3 = $("#dialog_create_template_from_volume #create_template_os_type").empty();
			if (types != null && types.length > 0) {				
				for (var i = 0; i < types.length; i++) {
				    osTypeMap[types[i].id] = fromdb(types[i].description);		
				    var html = "<option value='" + types[i].id + "'>" + fromdb(types[i].description) + "</option>";									
					osTypeDropdown1.append(html);	
					osTypeDropdown2.append(html);	
					osTypeDropdown3.append(html);	
				}
			}					
		}
	});		
}

function bindStartVMButton() {    
    $("#start_vm_button").bind("click", function(event) {            
        var itemCounts = 0;
        for(var id in selectedItemsInMidMenu) {
            itemCounts ++;
        }
        if(itemCounts == 0) {
            $("#dialog_info_please_select_one_item_in_middle_menu").dialog("open");		
            return false;
        }        
                
        $("#dialog_confirmation")	
        .text(dictionary["message.action.start.instance"])
	    .dialog('option', 'buttons', { 						
		    "Confirm": function() { 
			    $(this).dialog("close"); 			
			    
			    var apiInfo = {
                    label: "label.action.start.instance",
                    isAsyncJob: true,
                    inProcessText: "label.action.start.instance.processing",
                    asyncJobResponse: "startvirtualmachineresponse",                  
                    afterActionSeccessFn: function(json, $midmenuItem1, id) {                    
                        var jsonObj = json.queryasyncjobresultresponse.jobresult.virtualmachine;                         
                        vmToMidmenu(jsonObj, $midmenuItem1);                                             
                        if(jsonObj.id.toString() == $("#right_panel_content #tab_content_details").find("#id").text())
                            vmToRightPanel($midmenuItem1);                              
                    }
                }          
			                    
                for(var id in selectedItemsInMidMenu) {	
                    var apiCommand = "command=startVirtualMachine&id="+id;                                                
                    doActionToMidMenu(id, apiInfo, apiCommand); 	
                }  
                
                selectedItemsInMidMenu = {}; //clear selected items for action	                      					    
		    }, 
		    "Cancel": function() { 
			    $(this).dialog("close"); 
    			
		    } 
	    }).dialog("open");
                                                 
        return false;        
    }); 	
}

function bindStopVMButton() {       
    $("#stop_vm_button").bind("click", function(event) {            
        var itemCounts = 0;
        for(var id in selectedItemsInMidMenu) {
            itemCounts ++;
        }
        if(itemCounts == 0) {
            $("#dialog_info_please_select_one_item_in_middle_menu").dialog("open");		
            return false;
        }        
                   
        $("#dialog_confirmation_stop_vm")	
	    .dialog('option', 'buttons', { 						
		    "Confirm": function() { 
			    $(this).dialog("close"); 			
			    
			    var apiInfo = {
                    label: "label.action.stop.instance",
                    isAsyncJob: true,
                    inProcessText: "label.action.stop.instance.processing",
                    asyncJobResponse: "stopvirtualmachineresponse",                 
                    afterActionSeccessFn: function(json, $midmenuItem1, id) {                         
                        var jsonObj = json.queryasyncjobresultresponse.jobresult.virtualmachine;  
                        vmToMidmenu(jsonObj, $midmenuItem1);   
                        if(jsonObj.id.toString() == $("#right_panel_content #tab_content_details").find("#id").text())
                            vmToRightPanel($midmenuItem1);                                             
                    }
                }                      
			     			    
			    var isForced = $("#dialog_confirmation_stop_vm").find("#force_stop_instance").attr("checked").toString();
			    
                for(var id in selectedItemsInMidMenu) {	
                    var apiCommand = "command=stopVirtualMachine&id="+id+"&forced="+isForced;                                    
                    doActionToMidMenu(id, apiInfo, apiCommand); 	
                }  
                
                selectedItemsInMidMenu = {}; //clear selected items for action	                      					    
		    }, 
		    "Cancel": function() { 
			    $(this).dialog("close"); 
    			
		    } 
	    }).dialog("open");                        	                   
         
        return false;        
    }); 	
}

function bindRebootVMButton() {   
    $("#reboot_vm_button").bind("click", function(event) {            
        var itemCounts = 0;
        for(var id in selectedItemsInMidMenu) {
            itemCounts ++;
        }
        if(itemCounts == 0) {
            $("#dialog_info_please_select_one_item_in_middle_menu").dialog("open");		
            return false;
        }        
               
        $("#dialog_confirmation")	
        .text(dictionary["message.action.reboot.instance"])
	    .dialog('option', 'buttons', { 						
		    "Confirm": function() { 
			    $(this).dialog("close"); 			
			    
			    var apiInfo = {
                    label: "label.action.reboot.instance",
                    isAsyncJob: true,
                    inProcessText: "label.action.reboot.instance.processing",
                    asyncJobResponse: "rebootvirtualmachineresponse",                  
                    afterActionSeccessFn: function(json, $midmenuItem1, id) {  
                        var jsonObj = json.queryasyncjobresultresponse.jobresult.virtualmachine;  
                        vmToMidmenu(jsonObj, $midmenuItem1);    
                        if(jsonObj.id.toString() == $("#right_panel_content #tab_content_details").find("#id").text())
                            vmToRightPanel($midmenuItem1);                                             
                    }
                }                       
			                    
                for(var id in selectedItemsInMidMenu) {	
                    var apiCommand = "command=rebootVirtualMachine&id="+id;                                               
                    doActionToMidMenu(id, apiInfo, apiCommand); 	
                }  
                
                selectedItemsInMidMenu = {}; //clear selected items for action	                      					    
		    }, 
		    "Cancel": function() { 
			    $(this).dialog("close"); 
    			
		    } 
	    }).dialog("open");
                                          
        return false;        
    }); 
}

function bindDestroyVMButton() {    
    $("#destroy_vm_button").bind("click", function(event) {            
        var itemCounts = 0;
        for(var id in selectedItemsInMidMenu) {
            itemCounts ++;
        }
        if(itemCounts == 0) {
            $("#dialog_info_please_select_one_item_in_middle_menu").dialog("open");		
            return false;
        }        
                
        $("#dialog_confirmation")
        .text(dictionary["message.action.destroy.instance"])	
	    .dialog('option', 'buttons', { 						
		    "Confirm": function() { 
			    $(this).dialog("close"); 			
			    
			    var apiInfo = {
                    label: "label.action.destroy.instance",
                    isAsyncJob: true,
                    inProcessText: "label.action.destroy.instance.processing",
                    asyncJobResponse: "destroyvirtualmachineresponse",                 
                    afterActionSeccessFn: function(json, $midmenuItem1, id) {  
                        var jsonObj = json.queryasyncjobresultresponse.jobresult.virtualmachine; 
                        vmToMidmenu(jsonObj, $midmenuItem1);  
                        if(jsonObj.id.toString() == $("#right_panel_content #tab_content_details").find("#id").text())
                            vmToRightPanel($midmenuItem1);                                              
                    }
                }                            
			                    
                for(var id in selectedItemsInMidMenu) {	
                    var apiCommand = "command=destroyVirtualMachine&id="+id;                                       
                    doActionToMidMenu(id, apiInfo, apiCommand); 	
                }  
                
                selectedItemsInMidMenu = {}; //clear selected items for action	                      					    
		    }, 
		    "Cancel": function() { 
			    $(this).dialog("close"); 
    			
		    } 
	    }).dialog("open");                 
             
        return false;        
    }); 	
}

var currentPageInTemplateGridInVmPopup =1;
var selectedTemplateTypeInVmPopup;  //selectedTemplateTypeInVmPopup will be set to "featured" when new VM dialog box opens
var vmPopupTemplatePageSize = 6; //max number of templates in VM wizard
var currentStepInVmPopup = 1;
function initVMWizard() {
    $vmPopup = $("#vm_popup");  
    //$vmPopup.draggable();

    if (isAdmin() || (getUserPublicTemplateEnabled() == "true")) {
        $vmPopup.find("#wiz_community").show();   
    } 
    else {
        $vmPopup.find("#wiz_community").hide();   
    } 
    
    $("#add_vm_button").unbind("click").bind("click", function(event) {
        vmWizardOpen();			
	    $.ajax({
		    data: createURL("command=listZones&available=true"),
		    dataType: "json",
		    success: function(json) {
			    var zones = json.listzonesresponse.zone;					
			    var $zoneSelect = $vmPopup.find("#wizard_zone").empty();					
			    if (zones != null && zones.length > 0) {
				    for (var i = 0; i < zones.length; i++) {
						$zone = $("<option value='" + zones[i].id + "'>" + fromdb(zones[i].name) + "</option>");
						$zone.data("zoneObj", zones[i]);
					    $zoneSelect.append($zone); 
				    }
			    }	
			    $zoneSelect.change();			
			    listTemplatesInVmPopup();	
		    }
	    });
		
		$.ajax({					
		    data: createURL("command=listSecurityGroups"+"&domainid="+g_domainid+"&account="+g_account),		
			dataType: "json",
			success: function(json) {			    		
				var items = json.listsecuritygroupsresponse.securitygroup;					
				var $securityGroupDropdown = $vmPopup.find("#security_group_dropdown").empty();	
				if (items != null && items.length > 0) {
					for (var i = 0; i < items.length; i++) {
					    if(items[i].name != "default") //exclude default security group because it is always applied
						    $securityGroupDropdown.append("<option value='" + fromdb(items[i].id) + "'>" + fromdb(items[i].name) + "</option>"); 
					}
				}					    
			}
		});		
		
	    $.ajax({
		    data: createURL("command=listServiceOfferings&issystem=false"),
		    dataType: "json",
		    async: false,
		    success: function(json) {
			    var offerings = json.listserviceofferingsresponse.serviceoffering;
			    var $container = $vmPopup.find("#service_offering_container");
			    $container.empty();					    
			    if (offerings != null && offerings.length > 0) {						    
				    for (var i = 0; i < offerings.length; i++) {	
					    var $t = $soTemplate.clone();  						  
					    $t.find("input:radio[name=service_offering_radio]").val(offerings[i].id); 
					    $t.find("#name").text(fromdb(offerings[i].name));
					    $t.find("#description").text(fromdb(offerings[i].displaytext)); 						    
					    if (i > 0)
					        $t.find("input:radio[name=service_offering_radio]").removeAttr("checked");							
					    $container.append($t.html());	
				    }
			    }
		    }
		});
	
	    $.ajax({
		    data: createURL("command=listDiskOfferings"),
		    dataType: "json",
		    async: false,
		    success: function(json) {
			    var offerings = json.listdiskofferingsresponse.diskoffering;			
			    var $dataDiskOfferingContainer = $vmPopup.find("#data_disk_offering_container").empty();
		        var $rootDiskOfferingContainer = $vmPopup.find("#root_disk_offering_container").empty();
		        
		        //***** data disk offering: "no, thanks", "custom", existing disk offerings in database (begin) ****************************************************
		        //"no, thanks" radio button (default radio button in data disk offering)
	            $dataDiskOfferingContainer.append($doTemplateNo.clone().html()); 
		        		        
		        //disk offerings from database
		        if (offerings != null && offerings.length > 0) {						    
			        for (var i = 0; i < offerings.length; i++) {	
			            var $t;
			            if(offerings[i].iscustomized == true) 			                       
		                    $t = $doTemplateCustom.clone();  			            
			            else 
				            $t = $doTemplateExisting.clone(); 	
				        
				        $t.data("jsonObj", offerings[i]);				        
				        $t.find("input:radio[name=data_disk_offering_radio]").removeAttr("checked").val(fromdb(offerings[i].id));	
			            $t.find("#name").text(fromdb(offerings[i].name));
			            $t.find("#description").text(fromdb(offerings[i].displaytext)); 
			            $dataDiskOfferingContainer.append($t.html());	
			        }
		        }
		        //***** data disk offering: "no, thanks", "custom", existing disk offerings in database (end) *******************************************************
		        		        	
		        //***** root disk offering: "custom", existing disk offerings in database (begin) *******************************************************************
		        //disk offerings from database
		        if (offerings != null && offerings.length > 0) {						    
			        for (var i = 0; i < offerings.length; i++) {	
			            var $t;
			            if(offerings[i].iscustomized == true) 
			                $t = $doTemplateCustom.clone();  
			            else 
				            $t = $doTemplateExisting.clone(); 	
				        
				        $t.data("jsonObj", offerings[i]).attr("id", "do"+offerings[i].id);	
				        var $offering = $t.find("input:radio").val(offerings[i].id);	 
				        if(i > 0) {
				            $offering.removeAttr("checked");	
						}
				        $t.find("#name").text(fromdb(offerings[i].name));
				        $t.find("#description").text(fromdb(offerings[i].displaytext)); 	 
				        $rootDiskOfferingContainer.append($t.html());	
			        }
		        }
			    //***** root disk offering: "custom", existing disk offerings in database (end) *********************************************************************	
		    }
	    });	
	    
	    $vmPopup.find("#wizard_service_offering").click();	      
        return false;
    });
        
    function vmWizardCleanup() {
        currentStepInVmPopup = 1;			
	    $vmPopup.find("#step1").show().nextAll().hide();		   
	    $vmPopup.find("#wizard_message").hide();
	    selectedTemplateTypeInVmPopup = "featured";				
	    $("#wiz_featured").removeClass().addClass("rev_wizmid_selectedtempbut");
	    $("#wiz_my, #wiz_community, #wiz_blank").removeClass().addClass("rev_wizmid_nonselectedtempbut");	
	    currentPageInTemplateGridInVmPopup = 1;	 	
    }	
	
    function vmWizardOpen() {
        $("#overlay_black").show();
        $vmPopup.show();        
        vmWizardCleanup();	
    }     
            
    function vmWizardClose() {			
	    $vmPopup.hide();
	    $("#overlay_black").hide();					
    }
		    	
    $vmPopup.find("#close_button").bind("click", function(event) {
	    vmWizardClose();
	    return false;
    });
			
    $vmPopup.find("#step1 #wiz_message_continue").bind("click", function(event) {			    
	    $vmPopup.find("#step1 #wiz_message").hide();
	    return false;
    });
		
    $vmPopup.find("#step2 #wiz_message_continue").bind("click", function(event) {			    
	    $vmPopup.find("#step2 #wiz_message").hide();
	    return false;
    });
	
	$vmPopup.find("#step3 #wiz_message_continue").bind("click", function(event) {			    
	    $vmPopup.find("#step3 #wiz_message").hide();
	    return false;
    });
	
	$vmPopup.find("#step4 #wiz_message_continue").bind("click", function(event) {			    
	    $vmPopup.find("#step4 #wiz_message").hide();
	    return false;
    });
	
    function getIconForOS(osType) {
	    if (osType == null || osType.length == 0) {
		    return "";
	    } else {
		    if (osType.match("^CentOS") != null) {
			    return "rev_wiztemo_centosicons";
		    } else if (osType.match("^Windows") != null) {
			    return "rev_wiztemo_windowsicons";
		    } else {
			    return "rev_wiztemo_linuxicons";
		    }
	    }
    }
	
    //vm wizard search and pagination
    $vmPopup.find("#step1").find("#search_button").bind("click", function(event) {	              
        currentPageInTemplateGridInVmPopup = 1;           	        	
        listTemplatesInVmPopup();  
        return false;   //event.preventDefault() + event.stopPropagation() 
    });
	
    $vmPopup.find("#step1").find("#search_input").bind("keypress", function(event) {		        
        if(event.keyCode == keycode_Enter) {                	        
            $vmPopup.find("#step1").find("#search_button").click();	
            return false;   //event.preventDefault() + event.stopPropagation() 		     
        }		    
    });   
			
    $vmPopup.find("#step1").find("#next_page").bind("click", function(event){	            
        currentPageInTemplateGridInVmPopup++;        
        listTemplatesInVmPopup(); 
        return false;   //event.preventDefault() + event.stopPropagation() 
    });		
    
    $vmPopup.find("#step1").find("#prev_page").bind("click", function(event){	                 
        currentPageInTemplateGridInVmPopup--;	              	    
        listTemplatesInVmPopup(); 
        return false;   //event.preventDefault() + event.stopPropagation() 
    });	
						
    //var vmPopupTemplatePageSize = 6; //max number of templates in VM wizard 
    function listTemplatesInVmPopup() {		
        var zoneId = $vmPopup.find("#wizard_zone").val();
        if(zoneId == null || zoneId.length == 0)
            return;
	
        var container = $vmPopup.find("#template_container");	 		    	
		   
        var commandString, templateType;    		  	   
        var searchInput = $vmPopup.find("#step1").find("#search_input").val();  

        if (selectedTemplateTypeInVmPopup != "blank") {  //*** template ***  
            templateType = "template";
            if (searchInput != null && searchInput.length > 0)                 
                commandString = "command=listTemplates&templatefilter="+selectedTemplateTypeInVmPopup+"&zoneid="+zoneId+"&keyword="+searchInput; 
            else
                commandString = "command=listTemplates&templatefilter="+selectedTemplateTypeInVmPopup+"&zoneid="+zoneId;           		    		
	    } 
	    else {  //*** ISO ***
	        templateType = "ISO";
	        if (searchInput != null && searchInput.length > 0)                 
                commandString = "command=listIsos&isReady=true&bootable=true&isofilter=executable&zoneid="+zoneId+"&keyword="+searchInput;  
            else
                commandString = "command=listIsos&isReady=true&bootable=true&isofilter=executable&zoneid="+zoneId;  
	    }
	  
		commandString += "&pagesize="+vmPopupTemplatePageSize+"&page="+currentPageInTemplateGridInVmPopup;
		   		
	    var loading = $vmPopup.find("#wiz_template_loading").show();	
	    if(currentPageInTemplateGridInVmPopup==1)
            $vmPopup.find("#step1").find("#prev_page").hide();
        else 
            $vmPopup.find("#step1").find("#prev_page").show();  		
		
	    $.ajax({
		    data: createURL(commandString),
		    dataType: "json",
		    async: false,
		    success: function(json) {
		        var items, $vmTemplateInWizard;		
		        if (templateType == "template") {
			        items = json.listtemplatesresponse.template;
			        $vmTemplateInWizard = $("#vmtemplate_in_vmwizard");
			    }
			    else if (templateType == "ISO") {
			        items = json.listisosresponse.iso;
			        $vmTemplateInWizard = $("#vmiso_in_vmwizard");
			    }
			        
			    loading.hide();
			    container.empty(); 
			    if (items != null && items.length > 0) {	
				    for (var i = 0; i < items.length; i++) {
				        var $newTemplate = $vmTemplateInWizard.clone();				        
				        vmWizardTemplateJsonToTemplate(items[i], $newTemplate, templateType, i);
				        container.append($newTemplate.show());				       
				    }						
				    
				    if(items.length < vmPopupTemplatePageSize)
	                    $vmPopup.find("#step1").find("#next_page").hide();
	                else
	                    $vmPopup.find("#step1").find("#next_page").show();	 
			    } 
			    else {
			        var msg;
			        if (selectedTemplateTypeInVmPopup != "blank")
			            msg = "No templates available";
			        else
			            msg = "No ISOs available";					    
				    var html = '<div class="rev_wiztemplistbox" id="-2">'
							      +'<div></div>'
							      +'<div class="rev_wiztemp_listtext">'+msg+'</div>'
						      +'</div>';
				    container.append(html);						
				    $vmPopup.find("#step1").find("#next_page").hide();
			    }
		    }
	    });
    }
		
	//var $selectedVmWizardTemplate;		
	function vmWizardTemplateJsonToTemplate(jsonObj, $template, templateType, i) {	 
	    $template.attr("id", ("vmWizardTemplate_"+jsonObj.id));
	    $template.data("templateId", jsonObj.id);
	    $template.data("templateType", templateType);
	    $template.data("templateName", fromdb(jsonObj.displaytext));
		$template.data("hypervisor", jsonObj.hypervisor);
	
        $template.find("#icon").removeClass().addClass(getIconForOS(jsonObj.ostypename));
        $template.find("#name").text(fromdb(jsonObj.displaytext));	
        
        if(templateType == "template") {
            $template.find("#hypervisor_text").text(fromdb(jsonObj.hypervisor));	
            //$template.find("#hypervisor_text").text("XenServer");  //This line is for testing only. Comment this line and uncomment the line above before checkin.
        }
        				    
        $template.find("#submitted_by").text(fromdb(jsonObj.account));				      
                
        if(i == 0) { //select the 1st one
            $selectedVmWizardTemplate = $template;
            $template.addClass("rev_wiztemplistbox_selected");
        }
        else {
            $template.addClass("rev_wiztemplistbox");
        }
            
        $template.bind("click", function(event) {
            if($selectedVmWizardTemplate != null)
                $selectedVmWizardTemplate.removeClass("rev_wiztemplistbox_selected").addClass("rev_wiztemplistbox");          
             
            $(this).removeClass("rev_wiztemplistbox").addClass("rev_wiztemplistbox_selected");  
            $selectedVmWizardTemplate = $(this);
            return false;
        });
	}		
	             
    $vmPopup.find("#wizard_zone").bind("change", function(event) {       
        var selectedZone = $(this).val();   
        if(selectedZone != null && selectedZone.length > 0) {            
            $.ajax({
               data: createURL("command=listHypervisors&zoneid="+selectedZone),
               dataType: "json",
               async: false,
               success: function(json) {            
                   var items = json.listhypervisorsresponse.hypervisor;
                   var $hypervisorDropdown = $("#vmiso_in_vmwizard").find("#hypervisor_select").empty();
                   var $hypervisorSpan = $("#vmiso_in_vmwizard").find("#hypervisor_span");
                   if(items != null && items.length > 0) {    
                       if(items.length == 1) {
                           $hypervisorSpan.text(fromdb(items[0].name)).show();
                           $hypervisorDropdown.hide();
                       }
                       else {   
                           $hypervisorDropdown.show();
                           $hypervisorSpan.text("").hide();
                           for(var i=0; i<items.length; i++) {                    
                               $hypervisorDropdown.append("<option value='"+fromdb(items[i].name)+"'>"+fromdb(items[i].name)+"</option>");
                           }
                       }
                   }
               }    
            }); 
            
            listTemplatesInVmPopup();              	            
        }     
        return false;
    });
            
    function displayDiskOffering(type) {
        if(type=="data") {
            $vmPopup.find("#wizard_data_disk_offering_title").show();
		    $vmPopup.find("#wizard_data_disk_offering").show();
		    $vmPopup.find("#wizard_root_disk_offering_title").hide();
		    $vmPopup.find("#wizard_root_disk_offering").hide();
        }
        else if(type=="root") {
            $vmPopup.find("#wizard_root_disk_offering_title").show();
		    $vmPopup.find("#wizard_root_disk_offering").show();
		    $vmPopup.find("#wizard_data_disk_offering_title").hide();	
		    $vmPopup.find("#wizard_data_disk_offering").hide();	
        }
    }
    displayDiskOffering("data");  //because default value of "#wiz_template_filter" is "wiz_featured"
  
    
    // Setup the left template filters	  	
    $vmPopup.find("#wiz_template_filter").unbind("click").bind("click", function(event) {		 	    
	    var $container = $(this);
	    var target = $(event.target);
	    var targetId = target.attr("id");
	    selectedTemplateTypeInVmPopup = "featured";		
		
	    switch (targetId) {
		    case "wiz_featured":
		        $vmPopup.find("#search_input").val("");  
		        currentPageInTemplateGridInVmPopup = 1;
			    selectedTemplateTypeInVmPopup = "featured";
			    $container.find("#wiz_featured").removeClass().addClass("rev_wizmid_selectedtempbut");
			    $container.find("#wiz_my, #wiz_community, #wiz_blank").removeClass().addClass("rev_wizmid_nonselectedtempbut");
			    displayDiskOffering("data");
			    break;
		    case "wiz_my":
		        $vmPopup.find("#search_input").val("");  
		        currentPageInTemplateGridInVmPopup = 1;
			    $container.find("#wiz_my").removeClass().addClass("rev_wizmid_selectedtempbut");
			    $container.find("#wiz_featured, #wiz_community, #wiz_blank").removeClass().addClass("rev_wizmid_nonselectedtempbut");
			    selectedTemplateTypeInVmPopup = "selfexecutable";
			    displayDiskOffering("data");
			    break;	
		    case "wiz_community":
		        $vmPopup.find("#search_input").val("");  
		        currentPageInTemplateGridInVmPopup = 1;
			    $container.find("#wiz_community").removeClass().addClass("rev_wizmid_selectedtempbut");
			    $container.find("#wiz_my, #wiz_featured, #wiz_blank").removeClass().addClass("rev_wizmid_nonselectedtempbut");
			    selectedTemplateTypeInVmPopup = "community";					
			    displayDiskOffering("data");
			    break;
		    case "wiz_blank":
		        $vmPopup.find("#search_input").val("");  
		        currentPageInTemplateGridInVmPopup = 1;
			    $container.find("#wiz_blank").removeClass().addClass("rev_wizmid_selectedtempbut");
			    $container.find("#wiz_my, #wiz_community, #wiz_featured").removeClass().addClass("rev_wizmid_nonselectedtempbut");
			    selectedTemplateTypeInVmPopup = "blank";
			    displayDiskOffering("root");
			    break;
	    }
	    listTemplatesInVmPopup();
	    return false;
    });  
		
	function vmWizardShowNetworkContainer($thisPopup) {	        
	    $thisPopup.find("#step4").find("#network_container").show();
	    $thisPopup.find("#step4").find("#securitygroup_container").hide();	
		$thisPopup.find("#step4").find("#for_no_network_support").hide();
	    
	    var zoneObj = $thisPopup.find("#wizard_zone option:selected").data("zoneObj");
	    		    
		$.ajax({
			data: createURL("command=listNetworks&domainid="+g_domainid+"&account="+g_account+"&zoneId="+$thisPopup.find("#wizard_zone").val()),
			dataType: "json",
			async: false,
			success: function(json) {
				var networks = json.listnetworksresponse.network;
								
				// Setup Virtual Network
				if(zoneObj.securitygroupsenabled == false) {
				    var virtualNetwork = null;
				    if (networks != null && networks.length > 0) {
					    for (var i = 0; i < networks.length; i++) {
						    if (networks[i].type == 'Virtual') {
							    virtualNetwork = networks[i];
						    }
					    }
				    }
				    var $networkVirtualContainer = $thisPopup.find("#network_virtual_container");				
				    var requiredVirtual = false;
				    var defaultNetworkAdded = false;
				    var availableSecondary = false;
				    if (virtualNetwork == null) {
					    $.ajax({
						    data: createURL("command=listNetworkOfferings&guestiptype=Virtual"),
						    dataType: "json",
						    async: false,
						    success: function(json) {
							    var networkOfferings = json.listnetworkofferingsresponse.networkoffering;
							    if (networkOfferings != null && networkOfferings.length > 0) {
								    for (var i = 0; i < networkOfferings.length; i++) {
									    if (networkOfferings[i].isdefault == true && networkOfferings[i].availability != "Unavailable") {
										    // Create a virtual network
										    var networkName = "Virtual Network";
		                                    var networkDesc = "A dedicated virtualized network for your account.  The broadcast domain is contained within a VLAN and all public network access is routed out by a virtual router.";				
										    $.ajax({
											    data: createURL("command=createNetwork&networkOfferingId="+networkOfferings[i].id+"&name="+todb(networkName)+"&displayText="+todb(networkDesc)+"&zoneId="+$thisPopup.find("#wizard_zone").val()),
											    dataType: "json",
											    async: false,
											    success: function(json) {
												    var network = json.createnetworkresponse.network;														
												    $networkVirtualContainer.show();
												    if (network.networkofferingavailability == 'Required') {
													    requiredVirtual = true;
													    $networkVirtualContainer.find("#network_virtual").attr('disabled', true);
												    }
												    defaultNetworkAdded = true;
												    $networkVirtualContainer.find("#network_virtual").data("id", network.id).data("jsonObj", network);														 
											    }
										    });
									    }
								    }
							    }
						    }
					    });
				    } 
				    else {
					    if (virtualNetwork.networkofferingavailability != 'Unavailable') {
						    $networkVirtualContainer.show();
						    if (virtualNetwork.networkofferingavailability == 'Required') {
							    requiredVirtual = true;
							    $networkVirtualContainer.find("#network_virtual").attr('disabled', true);
						    }
						    defaultNetworkAdded = true;
						    $networkVirtualContainer.data("id", virtualNetwork.id);
						    $networkVirtualContainer.find("#network_virtual").data("id", virtualNetwork.id).data("jsonObj", virtualNetwork);
					    } else {
						    $networkVirtualContainer.hide();
					    }
				    }
				}
				
				// Setup Direct Networks
				var $networkDirectTemplate = $("#wizard_network_direct_template");
				var $networkSecondaryDirectTemplate = $("#wizard_network_direct_secondary_template");
				var $networkDirectContainer = $("#network_direct_container").empty();
				var $networkDirectSecondaryContainer = $("#network_direct_secondary_container").empty();
				
				if (networks != null && networks.length > 0) {
					for (var i = 0; i < networks.length; i++) {
						//if zoneObj.securitygroupsenabled is true and users still choose to select network instead of security group, then UI won't show networks whose securitygroupenabled is true.
						if(zoneObj.securitygroupsenabled == true && networks[i].securitygroupenabled == true) {
						    continue;
						}
						
						if (networks[i].type != 'Direct') {
							continue;
						}
						var $directNetworkElement = null;
						if (networks[i].isdefault) {
							if (requiredVirtual) {
								continue;
							}
							$directNetworkElement = $networkDirectTemplate.clone().attr("id", "direct"+networks[i].id);
							if (defaultNetworkAdded || i > 0) {
								// Only check the first default network
								$directNetworkElement.find("#network_direct_checkbox").removeAttr("checked");
							}
							defaultNetworkAdded = true;
						} else {
							$directNetworkElement = $networkSecondaryDirectTemplate.clone().attr("id", "direct"+networks[i].id);
						}
						$directNetworkElement.find("#network_direct_checkbox").data("jsonObj", networks[i]);
						$directNetworkElement.find("#network_direct_name").text(networks[i].name);
						$directNetworkElement.find("#network_direct_desc").text(networks[i].displaytext);
						if (networks[i].isdefault) {
							$networkDirectContainer.append($directNetworkElement.show());
						} else {
							availableSecondary = true;
							$networkDirectSecondaryContainer.append($directNetworkElement.show());
						}
					}
				}
						
				if (availableSecondary) {
					$("#secondary_network_title, #secondary_network_desc").show();
				}
			}
		});
	}
	
	
	function vmWizardShowSecurityGroupContainer($thisPopup) {	        
        $thisPopup.find("#step4").find("#network_container").hide();	
        if($selectedVmWizardTemplate.data("hypervisor") != "VMware" && getDirectAttachSecurityGroupsEnabled() == "true") {		
		    $thisPopup.find("#step4").find("#securitygroup_container").show();
		    $thisPopup.find("#step4").find("#for_no_network_support").hide();
            $thisPopup.find("#step4").find("#security_group_section").show();			        
	        $thisPopup.find("#step5").find("#wizard_review_network").text("Basic Network");
	    }
	    else {
		    $thisPopup.find("#step4").find("#securitygroup_container").hide();
			
		    $thisPopup.find("#step4").find("#for_no_network_support").show();	
		    if($selectedVmWizardTemplate.data("hypervisor") == "VMware") {
		        $thisPopup.find("#step4").find("#for_no_network_support").find("#not_available_message_1").show();	
		        $thisPopup.find("#step4").find("#for_no_network_support").find("#not_available_message_2").hide();
		    }		
		    else if(getDirectAttachSecurityGroupsEnabled() != "true") {
		        $thisPopup.find("#step4").find("#for_no_network_support").find("#not_available_message_1").hide();
		        $thisPopup.find("#step4").find("#for_no_network_support").find("#not_available_message_2").show();	
		    }    
			    			
            $thisPopup.find("#step5").find("#wizard_review_network").text("Basic Network");			   
	    }		   
    }		    
		
    $vmPopup.find("#next_step").bind("click", function(event) {
	    event.preventDefault();
	    event.stopPropagation();	
	    var $thisPopup = $vmPopup;		    		
		var $reviewNetworkTemplate = $("#wizard_network_direct_review_template");
	    if (currentStepInVmPopup == 1) { //select a template/ISO		    		
	        // prevent a person from moving on if no templates are selected	  
	        if($thisPopup.find("#step1 #template_container .rev_wiztemplistbox_selected").length == 0) {			        
	            $thisPopup.find("#step1 #wiz_message").show();
	            return false;
	        }
               	 
		    if ($thisPopup.find("#wiz_blank").hasClass("rev_wizmid_selectedtempbut")) {  //ISO
		        $thisPopup.find("#step3_label").text(dictionary["label.root.disk.offering"]);
		        $thisPopup.find("#root_disk_offering_container").show();
		        $thisPopup.find("#data_disk_offering_container").hide();			       
		    } 
		    else {  //template
		        $thisPopup.find("#step3_label").text(dictionary["label.data.disk.offering"]);
		        $thisPopup.find("#data_disk_offering_container").show();
		        $thisPopup.find("#root_disk_offering_container").hide();			       
		    }	
			
			$thisPopup.find("#wizard_review_zone").text($thisPopup.find("#wizard_zone option:selected").text());    
			
			// This is taking from the selected template but need to change this to the dropdown that supports ISO.		
			if($selectedVmWizardTemplate.data("templateType") == "template") {
			    $selectedVmWizardTemplate.data("hypervisor", $selectedVmWizardTemplate.find("#hypervisor_text").text());
			}
			else {
			    if($selectedVmWizardTemplate.find("#hypervisor_select").css("display") != "none")
			        $selectedVmWizardTemplate.data("hypervisor", $selectedVmWizardTemplate.find("#hypervisor_select").val());
			    else //if($selectedVmWizardTemplate.find("#hypervisor_span").css("display") != "none")
			        $selectedVmWizardTemplate.data("hypervisor", $selectedVmWizardTemplate.find("#hypervisor_span").text());
			}			
			$thisPopup.find("#wizard_review_hypervisor").text($selectedVmWizardTemplate.data("hypervisor"));   	
						
			$thisPopup.find("#wizard_review_template").text($selectedVmWizardTemplate.data("templateName")); 
	    }			
		
	    if (currentStepInVmPopup == 2) { //service offering
	        // prevent a person from moving on if no service offering is selected
	        if($thisPopup.find("input:radio[name=service_offering_radio]:checked").length == 0) {
	            $thisPopup.find("#step2 #wiz_message #wiz_message_text").text("Please select a service offering to continue");
	            $thisPopup.find("#step2 #wiz_message").show();
		        return false;
		    }               
            $thisPopup.find("#wizard_review_service_offering").text($thisPopup.find("input:radio[name=service_offering_radio]:checked").next().text());
	    }			
		
	    if(currentStepInVmPopup ==3) { //disk offering	 	        
	        if($selectedVmWizardTemplate.data("templateType") == "template") {	//*** template ***            
	            $thisPopup.find("#wizard_review_disk_offering_label").text(dictionary["label.data.disk.offering"]  + ":");
	            var checkedRadioButton = $thisPopup.find("#data_disk_offering_container input[name=data_disk_offering_radio]:checked");	
	        }
	        else {  //*** ISO ***	            
	            // prevent a person from moving on if no disk offering is selected
	            if($thisPopup.find("input:radio[name=data_disk_offering_radio]:checked").length == 0) {
	                $thisPopup.find("#step3 #wiz_message #wiz_message_text").text("Please select a disk offering to continue");
	                $thisPopup.find("#step3 #wiz_message").show();
		            return false;
		        }   
	            $thisPopup.find("#wizard_review_disk_offering_label").text(dictionary["label.root.disk.offering"] + ":");
	            var checkedRadioButton = $thisPopup.find("#root_disk_offering_container input[name=data_disk_offering_radio]:checked");	
		    }
		          		        
	        var $diskOfferingElement = checkedRadioButton.parent();	        	    
	        
	        var isValid = true;		
	        if($diskOfferingElement.find("#custom_disk_size").length > 0) 	    
	            isValid &= validateInteger("Disk Size", $diskOfferingElement.find("#custom_disk_size"), $diskOfferingElement.find("#custom_disk_size_errormsg"), null, null, false);	//required	
	        else
	            isValid &= validateInteger("Disk Size", $diskOfferingElement.find("#custom_disk_size"), $diskOfferingElement.find("#custom_disk_size_errormsg"), null, null, true);	//optional		    		
	        if (!isValid) 
	            return;        
	        
	        var diskOfferingName = $diskOfferingElement.find("#name").text();
	        if(checkedRadioButton.parent().attr("id") == "vm_popup_disk_offering_template_custom")
	            diskOfferingName += (" (Disk Size: " + $diskOfferingElement.find("#custom_disk_size").val() + " MB)");
	        $thisPopup.find("#wizard_review_disk_offering").text(diskOfferingName);  
			
			
			var zoneObj = $thisPopup.find("#wizard_zone option:selected").data("zoneObj");
						
			if (zoneObj.securitygroupsenabled == false) {  //show network container				
			    vmWizardShowNetworkContainer($thisPopup);	 
			} 
			else if (zoneObj.securitygroupsenabled == true) {  // if security group is enabled			    
			    var hasDedicatedDirectTaggedDefaultNetwork = false;
			    $.ajax({
					data: createURL("command=listNetworks&type=Direct&domainid="+g_domainid+"&account="+g_account+"&zoneId="+$thisPopup.find("#wizard_zone").val()),
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
				    $("#dialog_confirmation")
                    .text(dictionary["message.launch.vm.on.private.network"])
                    .dialog("option", "buttons", {	                    
                         "Yes": function() {
                             //present the current UI we have today	
                             vmWizardShowNetworkContainer($thisPopup);  
                             $(this).dialog("close");
                         },
                         "No": function() {	                         
                             //present security groups for user to select
                             vmWizardShowSecurityGroupContainer($thisPopup);	
                             $(this).dialog("close");	
                         }
                    }).dialog("open");     
                }					    
			    else {
			        vmWizardShowSecurityGroupContainer($thisPopup);				        
				}
			}
	    }	
	    	
	    if (currentStepInVmPopup == 4) { //network			
			if ($thisPopup.find("#step4").find("#network_container").css("display") != "none") {
				var $selectedSecondaryNetworks = $thisPopup.find("input:checkbox[name=secondary_network]:checked");
				
				var $selectedPrimaryNetworks;	
				if($thisPopup.find("#network_virtual_container").css("display") == "none") 				
				    $selectedPrimaryNetworks = $thisPopup.find("#network_direct_container").find("input:radio[name=primary_network]:checked");
				else 
				    $selectedPrimaryNetworks = $thisPopup.find("input:radio[name=primary_network]:checked");		        
				
				// prevent a person from moving on if no network has been selected
				if($selectedPrimaryNetworks.length == 0) {
					$thisPopup.find("#step4 #wiz_message").show();
					return false;
				}      
				
				var modResult = 0;
				$thisPopup.find("#step5").find("#wizard_review_network").text($selectedPrimaryNetworks.data("jsonObj").name);
				$thisPopup.find("#wizard_review_primary_network_container").show();
				modResult = 0;
			
				var $reviewNetworkContainer = $("#wizard_review_secondary_network_container").empty();
				if ($selectedSecondaryNetworks.length != 0) {
					var networkIds = [];
					
					$selectedSecondaryNetworks.each(function(i) {
						var json = $(this).data("jsonObj");
						if (i == 0) {
							networkIds.push(json.id);
						} else {
							networkIds.push(","+json.id);
						}
						$reviewNetworkElement = $reviewNetworkTemplate.clone().attr("id", "network"+json.id);
						if (i % 2 == modResult) {
							$reviewNetworkElement.addClass("odd");
						} else {
							$reviewNetworkElement.addClass("even");
						}
						$reviewNetworkElement.find("#wizard_review_network_label").text("Network " + (i+2-modResult) + ":");
						$reviewNetworkElement.find("#wizard_review_network_selected").text(json.name);
						$reviewNetworkContainer.append($reviewNetworkElement.show());
					});
					$reviewNetworkContainer.data("directNetworkIds", networkIds.join(""));
				} else {
					$reviewNetworkContainer.data("directNetworkIds", null);
				}
			} 
			else if ($thisPopup.find("#step4").find("#securitygroup_container").css("display") != "none") { 
							
			}
	    }	
	    
	    if (currentStepInVmPopup == 5) { //last step		        
	        // validate values							
		    var isValid = true;									
		    isValid &= validateString("Name", $thisPopup.find("#wizard_vm_name"), $thisPopup.find("#wizard_vm_name_errormsg"), true);	 //optional	
		    isValid &= validateString("Group", $thisPopup.find("#wizard_vm_group"), $thisPopup.find("#wizard_vm_group_errormsg"), true); //optional					
		    if (!isValid) 
		        return;		    
	        vmWizardClose();
	        
		    // Create a new VM!!!!
		    var moreCriteria = [];								
		    moreCriteria.push("&zoneId="+$thisPopup.find("#wizard_zone").val());			    
			moreCriteria.push("&hypervisor="+$selectedVmWizardTemplate.data("hypervisor"));	    								
		    moreCriteria.push("&templateId="+$selectedVmWizardTemplate.data("templateId"));    							
		    moreCriteria.push("&serviceOfferingId="+$thisPopup.find("input:radio[name=service_offering_radio]:checked").val());
						
			if ($thisPopup.find("#step4").find("#network_container").css("display") != "none") {		
				var $selectedPrimaryNetworks;	
				if($thisPopup.find("#network_virtual_container").css("display") == "none") 				
				    $selectedPrimaryNetworks = $thisPopup.find("#network_direct_container").find("input:radio[name=primary_network]:checked");
				else 
				    $selectedPrimaryNetworks = $thisPopup.find("input:radio[name=primary_network]:checked");					
				
				var networkIds = $selectedPrimaryNetworks.data("jsonObj").id;

				var directNetworkIds = $thisPopup.find("#wizard_review_secondary_network_container").data("directNetworkIds");
				if (directNetworkIds != null) {
					if (networkIds != null) {
						networkIds = networkIds+","+directNetworkIds;
					} else {
						networkIds = directNetworkIds;
					}
				}
				moreCriteria.push("&networkIds="+networkIds);
			} 
			else if ($thisPopup.find("#step4").find("#securitygroup_container").css("display") != "none") {  
			    if($thisPopup.find("#step4").find("#security_group_section").css("display") != "none") {
				    if($thisPopup.find("#security_group_dropdown").val() != null && $thisPopup.find("#security_group_dropdown").val().length > 0) {
			            var securityGroupList = $thisPopup.find("#security_group_dropdown").val().join(",");
			            moreCriteria.push("&securitygroupids="+securityGroupList);	
			        }		
			    }				
			}
			
			var diskOfferingId, $diskOfferingElement;    						
		    if ($thisPopup.find("#wiz_blank").hasClass("rev_wizmid_selectedtempbut")) {  //ISO
		        diskOfferingId = $thisPopup.find("#root_disk_offering_container input[name=data_disk_offering_radio]:checked").val();	
		        $diskOfferingElement = $thisPopup.find("#root_disk_offering_container input[name=data_disk_offering_radio]:checked").parent();
		    }
		    else { //template
		        diskOfferingId = $thisPopup.find("#data_disk_offering_container input[name=data_disk_offering_radio]:checked").val();	
		        $diskOfferingElement = $thisPopup.find("#data_disk_offering_container input[name=data_disk_offering_radio]:checked").parent();
		    }
	        if(diskOfferingId != null && diskOfferingId != "" && diskOfferingId != "no")
		        moreCriteria.push("&diskOfferingId="+diskOfferingId);						 
								
			if($diskOfferingElement.find("#custom_disk_size").length > 0) {    			
			    var customDiskSize = $diskOfferingElement.find("#custom_disk_size").val(); //unit is MB
			    if(customDiskSize != null && customDiskSize.length > 0)
			        moreCriteria.push("&size="+customDiskSize);	    
			}
			
			var name = trim($thisPopup.find("#wizard_vm_name").val());
		    if (name != null && name.length > 0) 
			    moreCriteria.push("&displayname="+todb(name));	
			
		    var group = trim($thisPopup.find("#wizard_vm_group").val());
		    if (group != null && group.length > 0) 
			    moreCriteria.push("&group="+todb(group));	
			    		
			var $midmenuItem1 = beforeAddingMidMenuItem() ;
			$("#midmenu_container #midmenu_container_no_items_available").hide();
			    			
		    $.ajax({
			    data: createURL("command=deployVirtualMachine"+moreCriteria.join("")),
			    dataType: "json",
			    success: function(json) {
				    var jobId = json.deployvirtualmachineresponse.jobid;					   
				    var timerKey = "vmNew"+jobId;
					
				    // Process the async job
				    $("body").everyTime(
					    10000,
					    timerKey,
					    function() {
						    $.ajax({
							    data: createURL("command=queryAsyncJobResult&jobId="+jobId),
							    dataType: "json",
							    success: function(json) {
								    var result = json.queryasyncjobresultresponse;
								    if (result.jobstatus == 0) {
									    return; //Job has not completed
								    } else {
									    $("body").stopTime(timerKey);										    
									    if (result.jobstatus == 1) {
										    // Succeeded	
										    var item = result.jobresult.virtualmachine;					                        
				                            vmToMidmenu(item, $midmenuItem1);
				                            bindClickToMidMenu($midmenuItem1, vmToRightPanel, getMidmenuId);  
				                            
				                            if (item.passwordenabled == true) {							                                									        
										        var secondRowText = dictionary["label.new.password"] + ": " + item.password;
										        afterAddingMidMenuItem($midmenuItem1, true, secondRowText);
										        $midmenuItem1.data("afterActionInfo", secondRowText); 
										        /*
										        var afterActionInfo = "Instance " + getVmName(item.name, item.displayname) + " has been created successfully.  New password is " + item.password;
										        $midmenuItem1.data("afterActionInfo", afterActionInfo); 
										        
										        $("#dialog_info")
                                                .text(afterActionInfo)    
	                                            .dialog('option', 'buttons', { 	
		                                            "OK": function() { 
			                                            $(this).dialog("close"); 
		                                            } 
	                                            }).dialog("open");	
	                                            */										        
									        } 	
									        else {
									            afterAddingMidMenuItem($midmenuItem1, true, null);
									        }							                        
									    } else if (result.jobstatus == 2) {
										    // Failed										    
										    afterAddingMidMenuItem($midmenuItem1, false, fromdb(result.jobresult.errortext));		
									    }
								    }
							    },
							    error: function(XMLHttpResponse) {
								    $("body").stopTime(timerKey);
									handleError(XMLHttpResponse, function() {
										afterAddingMidMenuItem($midmenuItem1, false, parseXMLHttpResponse(XMLHttpResponse));
									});
							    }
						    });
					    },
					    0
				    );
			    },
			    error: function(XMLHttpResponse) {	
					handleError(XMLHttpResponse, function() {
						afterAddingMidMenuItem($midmenuItem1, false, parseXMLHttpResponse(XMLHttpResponse));
					});
			    }					
		    });
	    } 		
		
	    //since no error, move to next step		    
	    $vmPopup.find("#step" + currentStepInVmPopup).hide().next().show();  //hide current step, show next step		    
	    currentStepInVmPopup++;					
    });
	
    $vmPopup.find("#prev_step").bind("click", function(event) {		
	    var $prevStep = $vmPopup.find("#step" + currentStepInVmPopup).hide().prev().show(); //hide current step, show previous step
	    currentStepInVmPopup--;
	    return false; //event.preventDefault() + event.stopPropagation()
    });
}


//***** VM Detail (begin) ******************************************************************************
      
var vmActionMap = {    
    "label.action.start.instance": {        
        isAsyncJob: true,
        asyncJobResponse: "startvirtualmachineresponse",
        inProcessText: "label.action.start.instance.processing",
        dialogBeforeActionFn : doStartVM,
        afterActionSeccessFn: function(json, $midmenuItem1, id) { 
            var jsonObj = json.queryasyncjobresultresponse.jobresult.virtualmachine;      
            vmToMidmenu(jsonObj, $midmenuItem1);            
        }
    },
    "label.action.stop.instance": {             
        isAsyncJob: true,
        asyncJobResponse: "stopvirtualmachineresponse",
        inProcessText: "label.action.stop.instance.processing",
        dialogBeforeActionFn : doStopVM,
        afterActionSeccessFn: function(json, $midmenuItem1, id) { 
            var jsonObj = json.queryasyncjobresultresponse.jobresult.virtualmachine;            
            vmToMidmenu(jsonObj, $midmenuItem1);            
        }
    },
    "label.action.reboot.instance": {        
        isAsyncJob: true,
        asyncJobResponse: "rebootvirtualmachineresponse",
        inProcessText: "label.action.reboot.instance.processing",
        dialogBeforeActionFn : doRebootVM,
        afterActionSeccessFn: function(json, $midmenuItem1, id) { 
            var jsonObj = json.queryasyncjobresultresponse.jobresult.virtualmachine;       
            vmToMidmenu(jsonObj, $midmenuItem1);            
        }
    },
    "label.action.destroy.instance": {        
        isAsyncJob: true,
        asyncJobResponse: "destroyvirtualmachineresponse",
        inProcessText: "label.action.destroy.instance.processing",
        dialogBeforeActionFn : doDestroyVM,
        afterActionSeccessFn: function(json, $midmenuItem1, id) {             
            var jsonObj = json.queryasyncjobresultresponse.jobresult.virtualmachine; 
            vmToMidmenu(jsonObj, $midmenuItem1);            
        }
    },
    "label.action.restore.instance": {          
        isAsyncJob: false,
        inProcessText: "label.action.restore.instance.processing",
        dialogBeforeActionFn : doRestoreVM,
        afterActionSeccessFn: function(json, $midmenuItem1, id) { 
            var jsonObj = json.recovervirtualmachineresponse.virtualmachine;
            vmToMidmenu(jsonObj, $midmenuItem1);            
        }
    },
    "label.action.edit.instance": {
        dialogBeforeActionFn: doEditVM  
    },
    "label.action.attach.iso": {
        isAsyncJob: true,
        asyncJobResponse: "attachisoresponse",    
        inProcessText: "label.action.attach.iso.processing",        
        dialogBeforeActionFn : doAttachISO,
        afterActionSeccessFn: function(json, $midmenuItem1, id) {   
            var jsonObj = json.queryasyncjobresultresponse.jobresult.virtualmachine;           
            vmToMidmenu(jsonObj, $midmenuItem1);              
        }   
    },
    "label.action.detach.iso": {
        isAsyncJob: true,
        asyncJobResponse: "detachisoresponse",     
        inProcessText: "label.action.detach.iso.processing",       
        dialogBeforeActionFn : doDetachISO,
        afterActionSeccessFn: function(json, $midmenuItem1, id) { 
            var jsonObj = json.queryasyncjobresultresponse.jobresult.virtualmachine;    
            vmToMidmenu(jsonObj, $midmenuItem1);                 
        }   
    },
    "label.action.reset.password": {                
        isAsyncJob: true,  
        asyncJobResponse: "resetpasswordforvirtualmachineresponse", 
        inProcessText: "label.action.reset.password.processing",  
        dialogBeforeActionFn : doResetPassword,
        afterActionSeccessFn: function(json, $midmenuItem1, id) {      
            var jsonObj = json.queryasyncjobresultresponse.jobresult.virtualmachine;   
            vmToMidmenu(jsonObj, $midmenuItem1);      
            
            /*
            $("#dialog_info")
            .text("New password of instance " + getVmName(jsonObj.name, jsonObj.displayname) + " is " + fromdb(jsonObj.password))    
	        .dialog('option', 'buttons', { 	
		        "OK": function() { 
			        $(this).dialog("close"); 
		        } 
	        }).dialog("open");	
	        */
	        
	        return dictionary["label.new.password"] + ": " + fromdb(jsonObj.password);
        }
    },       
    "label.action.change.service": {
        isAsyncJob: false,        
        inProcessText: "label.action.change.service",
        dialogBeforeActionFn : doChangeService,
        afterActionSeccessFn: function(json, $midmenuItem1, id) {                 
            var jsonObj = json.changeserviceforvirtualmachineresponse.virtualmachine;       
            vmToMidmenu(jsonObj, $midmenuItem1);           
        }
    },    
    "label.action.create.template": {
        isAsyncJob: true,
        asyncJobResponse: "createtemplateresponse",            
        dialogBeforeActionFn : doCreateTemplateFromVM,
        inProcessText: "label.action.create.template.processing",
        afterActionSeccessFn: function(json, $midmenuItem1, id) {}   
    },    
    "label.action.migrate.instance": {
        isAsyncJob: true,        
		asyncJobResponse: "migratevirtualmachineresponse", 
        inProcessText: "label.action.migrate.instance.processing",
        dialogBeforeActionFn : doMigrateInstance,
        afterActionSeccessFn: function(json, $midmenuItem1, id) {                 
            var jsonObj = json.queryasyncjobresultresponse.jobresult.virtualmachine;       
            vmToMidmenu(jsonObj, $midmenuItem1);           
        }
    }        
}                      
     
function doStartVM($actionLink, $detailsTab, $midmenuItem1) {       
    $("#dialog_confirmation")	
    .text(dictionary["message.action.start.instance"])
    .dialog('option', 'buttons', { 						
	    "Confirm": function() { 
		    $(this).dialog("close"); 			
		    
		    var jsonObj = $midmenuItem1.data("jsonObj");
		    var id = jsonObj.id;
		    var apiCommand = "command=startVirtualMachine&id="+id;  
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);				   	                         					    
	    }, 
	    "Cancel": function() { 
		    $(this).dialog("close"); 
			
	    } 
    }).dialog("open");
}   

function doStopVM($actionLink, $detailsTab, $midmenuItem1) {   
    $("#dialog_confirmation_stop_vm")	  
    .dialog('option', 'buttons', { 						
	    "Confirm": function() { 
		    $(this).dialog("close"); 			
		    
		    var jsonObj = $midmenuItem1.data("jsonObj");
		    var id = jsonObj.id;
		    var isForced = $("#dialog_confirmation_stop_vm").find("#force_stop_instance").attr("checked").toString();
		    var apiCommand = "command=stopVirtualMachine&id="+id+"&forced="+isForced;       
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);				   	                         					    
	    }, 
	    "Cancel": function() { 
		    $(this).dialog("close"); 
			
	    } 
    }).dialog("open");
}   
   
function doRebootVM($actionLink, $detailsTab, $midmenuItem1) {   
    $("#dialog_confirmation")	
    .text(dictionary["message.action.reboot.instance"])
    .dialog('option', 'buttons', { 						
	    "Confirm": function() { 
		    $(this).dialog("close"); 			
		    
		    var jsonObj = $midmenuItem1.data("jsonObj");
		    var id = jsonObj.id;
		    var apiCommand = "command=rebootVirtualMachine&id="+id;  
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);				   	                         					    
	    }, 
	    "Cancel": function() { 
		    $(this).dialog("close"); 
			
	    } 
    }).dialog("open");
}   
  
function doDestroyVM($actionLink, $detailsTab, $midmenuItem1) {   
    $("#dialog_confirmation")	
    .text(dictionary["message.action.destroy.instance"])
    .dialog('option', 'buttons', { 						
	    "Confirm": function() { 
		    $(this).dialog("close"); 			
		    
		    var jsonObj = $midmenuItem1.data("jsonObj");
		    var id = jsonObj.id;
		    var apiCommand = "command=destroyVirtualMachine&id="+id;  
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);				   	                         					    
	    }, 
	    "Cancel": function() { 
		    $(this).dialog("close"); 
			
	    } 
    }).dialog("open");
}   
  
function doRestoreVM($actionLink, $detailsTab, $midmenuItem1) {   
    $("#dialog_confirmation")	
    .text(dictionary["message.action.restore.instance"])
    .dialog('option', 'buttons', { 						
	    "Confirm": function() { 
		    $(this).dialog("close"); 			
		    
		    var jsonObj = $midmenuItem1.data("jsonObj");
		    var id = jsonObj.id;
		    var apiCommand = "command=recoverVirtualMachine&id="+id;  
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);				   	                         					    
	    }, 
	    "Cancel": function() { 
		    $(this).dialog("close"); 
			
	    } 
    }).dialog("open");
}   
 
function doEditVM($actionLink, $detailsTab, $midmenuItem1) { 
	var vmObj = $midmenuItem1.data("jsonObj");	
    $.ajax({
        data: createURL("command=listServiceOfferings&id="+vmObj.serviceofferingid),
        dataType: "json",
        async: false,
        success: function(json) {	        
	        if(json.listserviceofferingsresponse.serviceoffering != null && json.listserviceofferingsresponse.serviceoffering[0].offerha == true) {	            
	            $readonlyFields  = $("#tab_content_details").find("#vmname, #group, #haenable, #ostypename");
                $editFields = $("#tab_content_details").find("#vmname_edit, #group_edit, #haenable_edit, #ostypename_edit"); 	            
	        }
	        else {
	            $readonlyFields  = $("#tab_content_details").find("#vmname, #group, #ostypename");
                $editFields = $("#tab_content_details").find("#vmname_edit, #group_edit, #ostypename_edit"); 	            
	        }	        
        }
    });
	 
    $readonlyFields.hide();
    $editFields.show();  
    $detailsTab.find("#cancel_button, #save_button").show();
    
    $detailsTab.find("#cancel_button").unbind("click").bind("click", function(event){    
        cancelEditMode($detailsTab);    
        return false;
    });
    $detailsTab.find("#save_button").unbind("click").bind("click", function(event){        
        doEditVM2($actionLink, $detailsTab, $midmenuItem1, $readonlyFields, $editFields);   
        return false;
    });   
}

function doEditVM2($actionLink, $detailsTab, $midmenuItem1, $readonlyFields, $editFields) {   
    // validate values
    var isValid = true;					
    isValid &= validateString("Name", $detailsTab.find("#name_edit"), $detailsTab.find("#name_edit_errormsg"), true);  //optional
    isValid &= validateString("Display Text", $detailsTab.find("#group_edit"), $detailsTab.find("#group_edit_errormsg"), true);	//optional	
    if (!isValid) 
        return;
       
    var jsonObj = $midmenuItem1.data("jsonObj"); 
	var id = jsonObj.id;	
	
	var array1 = [];						
	var name = trim($detailsTab.find("#vmname_edit").val());
	array1.push("&displayName="+todb(name));	
	
	var group = trim($detailsTab.find("#group_edit").val());
	array1.push("&group="+todb(group));
	
	var haenable = $detailsTab.find("#haenable_edit").val();     
	array1.push("&haenable="+haenable);   
	
	var ostypeid = $detailsTab.find("#ostypename_edit").val();     
	array1.push("&ostypeid="+ostypeid);   
	
	$.ajax({
	    data: createURL("command=updateVirtualMachine&id="+id+array1.join("")),
		dataType: "json",
		success: function(json) {
		    var jsonObj = json.updatevirtualmachineresponse.virtualmachine;		
            vmToMidmenu(jsonObj, $midmenuItem1);
            vmToRightPanel($midmenuItem1);	
            
            $editFields.hide();      
            $readonlyFields.show();       
            $("#save_button, #cancel_button").hide();          					
		}
	});
} 
         
function doAttachISO($actionLink, $detailsTab, $midmenuItem1) {   
    $.ajax({
	    data: createURL("command=listIsos&isReady=true&isofilter=executable"),
		dataType: "json",
		async: false,
		success: function(json) {
			var isos = json.listisosresponse.iso;
			var isoSelect = $("#dialog_attach_iso #attach_iso_select");
			if (isos != null && isos.length > 0) {
				isoSelect.empty();
				for (var i = 0; i < isos.length; i++) {
					isoSelect.append("<option value='"+isos[i].id+"'>"+fromdb(isos[i].displaytext)+"</option>");;
				}
			}
		}
	});
	
	$("#dialog_attach_iso")
	.dialog('option', 'buttons', { 						
		"OK": function() { 	
		    var $thisDialog = $(this);
		    				
			var isValid = true;				
			isValid &= validateDropDownBox("ISO", $thisDialog.find("#attach_iso_select"), $thisDialog.find("#attach_iso_select_errormsg"));	
			if (!isValid) 
			    return;
			    
			$thisDialog.dialog("close");		
				
			var isoId = $("#dialog_attach_iso #attach_iso_select").val();	
			
			var jsonObj = $midmenuItem1.data("jsonObj");
			var id = jsonObj.id;
			var apiCommand = "command=attachIso&virtualmachineid="+id+"&id="+isoId;
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);						
		}, 
		"Cancel": function() { 
			$(this).dialog("close"); 			
		} 
	}).dialog("open");
}

function doDetachISO($actionLink, $detailsTab, $midmenuItem1) {  
    $("#dialog_detach_iso_from_vm")	
	.dialog('option', 'buttons', { 						
		"OK": function() { 
			$(this).dialog("close");	
			
			var jsonObj = $midmenuItem1.data("jsonObj");
			var id = jsonObj.id;
			var apiCommand = "command=detachIso&virtualmachineid="+id;
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);							
		}, 
		"Cancel": function() { 
			$(this).dialog("close"); 			
		} 
	}).dialog("open");
}

function doResetPassword($actionLink, $detailsTab, $midmenuItem1) { 
	var jsonObj = $midmenuItem1.data("jsonObj");
	
	if (jsonObj.passwordenabled == false) {
		$("#dialog_info")
			.text(dictionary["message.action.reset.password.off"])    
			.dialog('option', 'buttons', { 	
			"OK": function() { 
				$(this).dialog("close"); 
			}	 
		}).dialog("open");
		return;
	} else if (jsonObj.state != 'Stopped') {
		$("#dialog_info")
			.text(dictionary["message.action.reset.password.warning"])    
			.dialog('option', 'buttons', { 	
			"OK": function() { 
				$(this).dialog("close"); 
			}	 
		}).dialog("open");
		return;
	}
  		
	$("#dialog_confirmation")
	.text(dictionary["message.action.instance.reset.password"])	
	.dialog('option', 'buttons', { 						
		"Yes": function() { 
			$(this).dialog("close"); 
						
			if(jsonObj.passwordenabled != true) {
			    var $afterActionInfoContainer = $("#right_panel_content #after_action_info_container_on_top");
			    $afterActionInfoContainer.find("#after_action_info").text("Reset password failed. Reason: This instance is not using a template that has the password reset feature enabled.  If you have forgotten your root password, please contact support.");  
			    $afterActionInfoContainer.addClass("errorbox").show();
			    return;
			}			
			
			var id = jsonObj.id;
			var apiCommand = "command=resetPasswordForVirtualMachine&id="+id;    
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);				
		}, 
		"No": function() { 
			$(this).dialog("close"); 			
		} 
	}).dialog("open");
}

function doChangeService($actionLink, $detailsTab, $midmenuItem1) {    
    var jsonObj = $midmenuItem1.data("jsonObj");
	var id = jsonObj.id;
	
	if (jsonObj.state != 'Stopped') {
		$("#dialog_info")
			.text(dictionary['message.action.change.service.warning.for.instance'])    
			.dialog('option', 'buttons', { 	
			"OK": function() { 
				$(this).dialog("close"); 
			}	 
		}).dialog("open");
		return;
	}
	
	$.ajax({	   
	    data: createURL("command=listServiceOfferings&VirtualMachineId="+id), 
		dataType: "json",
		async: false,
		success: function(json) {
			var offerings = json.listserviceofferingsresponse.serviceoffering;
			var offeringSelect = $("#dialog_change_service_offering #change_service_offerings").empty();
			
			if (offerings != null && offerings.length > 0) {
				for (var i = 0; i < offerings.length; i++) {
					var option = $("<option value='" + offerings[i].id + "'>" + fromdb(offerings[i].displaytext) + "</option>").data("name", fromdb(offerings[i].name));
					offeringSelect.append(option); 
				}
			} 
		}
	});
	
	$("#dialog_change_service_offering")
	.dialog('option', 'buttons', { 						
		"OK": function() { 
		    var $thisDialog = $(this);
		    		   
		    var isValid = true;				
			isValid &= validateDropDownBox("Service Offering", $thisDialog.find("#change_service_offerings"), $thisDialog.find("#change_service_offerings_errormsg"));	
			if (!isValid) 
			    return;
		    
			$thisDialog.dialog("close"); 
			var serviceOfferingId = $thisDialog.find("#change_service_offerings").val();
						
			if(jsonObj.state != "Stopped") {				    
		        $midmenuItem1.find("#info_icon").addClass("error").show();
                $midmenuItem1.data("afterActionInfo", ($actionLink.data("label") + " action failed. Reason: virtual instance needs to be stopped before you can change its service."));  
	        }
            var apiCommand = "command=changeServiceForVirtualMachine&id="+id+"&serviceOfferingId="+serviceOfferingId;	     
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);				
		}, 
		"Cancel": function() { 
			$(this).dialog("close"); 			
		} 
	}).dialog("open");
}

function doMigrateInstance($actionLink, $detailsTab, $midmenuItem1) {    
    var jsonObj = $midmenuItem1.data("jsonObj");
	var id = jsonObj.id;
	
	$.ajax({	   
	    data: createURL("command=listHosts&VirtualMachineId="+id), 
		dataType: "json",
		async: false,
		success: function(json) {
			var hosts = json.listhostsresponse.host;
			var hostSelect = $("#dialog_migrate_instance #migrate_instance_hosts").empty();
			
			if (hosts != null && hosts.length > 0) {
				for (var i = 0; i < hosts.length; i++) {
					var option = $("<option value='" + hosts[i].id + "'>" + fromdb(hosts[i].name) + ": " +((hosts[i].hasEnoughCapacity) ? dictionary["label.available"] : dictionary["label.full"]) + "</option>").data("name", fromdb(hosts[i].name));
					hostSelect.append(option); 
				}
			} 
		},
		error: function(XMLHttpResponse) {
			handleError(XMLHttpResponse, function() {
				$("#dialog_migrate_instance #migrate_instance_hosts").empty();
			});
		}
});
	
	$("#dialog_migrate_instance")
	.dialog('option', 'buttons', { 						
		"OK": function() { 
		    var $thisDialog = $(this);
		    		   
		    var isValid = true;				
			isValid &= validateDropDownBox("Host", $thisDialog.find("#migrate_instance_hosts"), $thisDialog.find("#migrate_instance_errormsg"));	
			if (!isValid) 
			    return;
		    
			$thisDialog.dialog("close"); 
			var hostId = $thisDialog.find("#migrate_instance_hosts").val();
			/*		
			if(jsonObj.state != "Stopped") {				    
		        $midmenuItem1.find("#info_icon").addClass("error").show();
                $midmenuItem1.data("afterActionInfo", ($actionLink.data("label") + " action failed. Reason: virtual instance needs to be stopped before you can change its service."));  
	        }
			*/
            var apiCommand = "command=migrateVirtualMachine&hostid="+hostId+"&virtualmachineid="+id;	     
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);				
		}, 
		"Cancel": function() { 
			$(this).dialog("close"); 			
		} 
	}).dialog("open");
}

function vmToMidmenu(jsonObj, $midmenuItem1) {  
    $midmenuItem1.data("jsonObj", jsonObj);
    $midmenuItem1.attr("id", getMidmenuId(jsonObj));   
      
    var firstRowText = getVmName(jsonObj.name, jsonObj.displayname);
    $midmenuItem1.find("#first_row").text(clippedText(firstRowText, midMenuFirstRowLength));    
    $midmenuItem1.find("#first_row_container").attr("title", firstRowText);   
    
    var secondRowText = fromdb(jsonObj.templatename);
    $midmenuItem1.find("#second_row").text(clippedText(secondRowText, midMenuSecondRowLength));
    $midmenuItem1.find("#second_row_container").attr("title", secondRowText); 
    
    updateVmStateInMidMenu(jsonObj, $midmenuItem1);     
    
    $midmenuItem1.data("toRightPanelFn", vmToRightPanel);   
    countTopButtonMapFn = vmCountTopButtonMap;
    uncountTopButtonMapFn = vmUncountTopButtonMap;
    grayoutTopButtonsFn = vmGrayoutTopButtons;
    resetTopButtonMapFn = vmResetTopButtonMap;
}

function vmToRightPanel($midmenuItem1) {
    var jsonObj = $midmenuItem1.data("jsonObj");          
    
    var vmName = getVmName(jsonObj.name, jsonObj.displayname);        
    $("right_panel_header").find("#vm_name").text(vmName);	
     
    copyActionInfoFromMidMenuToRightPanel($midmenuItem1); 
     
    $("#right_panel_content").data("$midmenuItem1", $midmenuItem1);
    $("#tab_details").click();   
}
  
function vmJsonToDetailsTab(){  
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
	if ($midmenuItem1 == null) {
	    vmJsonClearDetailsTab();   
	    return;	 
	}
	    
	var jsonObj = $midmenuItem1.data("jsonObj");    
	if(jsonObj == null) {
	    vmJsonClearDetailsTab();   
	    return;
	}
	
	var $thisTab = $("#right_panel_content").find("#tab_content_details");     
	$thisTab.find("#tab_container").hide(); 
	$thisTab.find("#tab_spinning_wheel").show();    
		
	var id = jsonObj.id;		  
	
	//comment out the following AJAX call because it caused problem of multiple-selection middle menu
	/*
	$.ajax({
		data: createURL("command=listVirtualMachines&id="+id),
		dataType: "json",
		async: false,
		success: function(json) {  
			var items = json.listvirtualmachinesresponse.virtualmachine;
			if(items != null && items.length > 0) {
				jsonObj = items[0]; //override jsonObj declared above				
				$midmenuItem1.data("jsonObj", jsonObj); 
				updateVmStateInMidMenu(jsonObj, $midmenuItem1);    				
	        }   
		}
	});  	  
    */
	
	resetViewConsoleAction(jsonObj, $thisTab);      
	setVmStateInRightPanel(jsonObj.state, $thisTab.find("#state"));		
	
	
	//refresh status every 2 seconds until status is not Starting/Stopping any more 
	var timerKey = "refreshInstanceStatus";
	$("body").stopTime(timerKey);  //stop timer used by another middle menu item (i.e. stop timer when clicking on a different middle menu item)		
	if($midmenuItem1.find("#spinning_wheel").css("display") == "none") {
	    if(jsonObj.state in vmChangableStatus) {	       
	        $("body").everyTime(
                5000,
                timerKey,
                function() {              
                    $.ajax({
		                data: createURL("command=listVirtualMachines&id="+id),
		                dataType: "json",
		                async: false,
		                success: function(json) {  
			                var items = json.listvirtualmachinesresponse.virtualmachine;
			                if(items != null && items.length > 0) {
				                jsonObj = items[0]; //override jsonObj declared above				
				                $midmenuItem1.data("jsonObj", jsonObj); 	
				                if(!(jsonObj.state in vmChangableStatus)) {
				                    $("body").stopTime(timerKey);	
				                    updateVmStateInMidMenu(jsonObj, $midmenuItem1); 				                    				                    
				                    if(jsonObj.id.toString() == $("#right_panel_content").find("#tab_content_details").find("#id").text()) {
				                        setVmStateInRightPanel(jsonObj.state, $thisTab.find("#state"));	
				                        vmBuildActionMenu(jsonObj, $thisTab, $midmenuItem1);	
				                    }					                    
				                }				               	
	                        }   
		                }
	                });                       	
                }
            );
	    }
	}
		
		
	$thisTab.find("#ipAddress").text(fromdb(jsonObj.ipaddress));	
	$thisTab.find("#id").text(fromdb(jsonObj.id));
	$thisTab.find("#zoneName").text(fromdb(jsonObj.zonename));
		   
	var vmName = getVmName(jsonObj.name, jsonObj.displayname);        
	$thisTab.find("#title").text(vmName);
	
	$thisTab.find("#vmname").text(vmName);
	$thisTab.find("#vmname_edit").val(fromdb(jsonObj.displayname));
	
	$thisTab.find("#ipaddress").text(fromdb(jsonObj.ipaddress));
	
	$thisTab.find("#hypervisor").text(fromdb(jsonObj.hypervisor));	
	$thisTab.find("#templateName").text(fromdb(jsonObj.templatename));

	$thisTab.find("#ostypename").text(osTypeMap[fromdb(jsonObj.guestosid)]);
    $thisTab.find("#ostypename_edit").val(fromdb(jsonObj.guestosid));   
	
	$thisTab.find("#serviceOfferingName").text(fromdb(jsonObj.serviceofferingname));	
	$thisTab.find("#account").text(fromdb(jsonObj.account));
	$thisTab.find("#domain").text(fromdb(jsonObj.domain));
	$thisTab.find("#hostName").text(fromdb(jsonObj.hostname));
	
	$thisTab.find("#group").text(fromdb(jsonObj.group));	
	$thisTab.find("#group_edit").val(fromdb(jsonObj.group));	
	
	setDateField(jsonObj.created, $thisTab.find("#created"));	 		
	
	setBooleanReadField(jsonObj.haenable, $thisTab.find("#haenable"));
	setBooleanEditField(jsonObj.haenable, $thisTab.find("#haenable_edit"));
		
	setBooleanReadField((jsonObj.isoid != null), $thisTab.find("#iso"));	
	  
	//actions ***
    vmBuildActionMenu(jsonObj, $thisTab, $midmenuItem1);
	
	$thisTab.find("#tab_spinning_wheel").hide();    
	$thisTab.find("#tab_container").show();  	
}

var vmChangableStatus = {
    "Starting": 1,
    "Stopping": 1
}

function vmBuildActionMenu(jsonObj, $thisTab, $midmenuItem1) {    
	var $actionMenu = $thisTab.find("#action_link #action_menu");
	$actionMenu.find("#action_list").empty();              
	var noAvailableActions = true; 
	
	if (jsonObj.state == 'Destroyed') {
	    if(isAdmin() || isDomainAdmin()) {
		    buildActionLinkForTab("label.action.restore.instance", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);
		    noAvailableActions = false;		
		}	
	} 
	else if (jsonObj.state == 'Running') {		      
	    buildActionLinkForTab("label.action.edit.instance", vmActionMap, $actionMenu, $midmenuItem1, $thisTab); 			
		buildActionLinkForTab("label.action.stop.instance", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);
		buildActionLinkForTab("label.action.reboot.instance", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);
		buildActionLinkForTab("label.action.destroy.instance", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);
		if (isAdmin() 
			&& (jsonObj.rootdevicetype == 'NetworkFilesystem' || jsonObj.rootdevicetype == 'IscsiLUN' || jsonObj.rootdevicetype == 'PreSetup' || jsonObj.rootdevicetype == 'OCFS2')
			//&& (jsonObj.hypervisor == 'XenServer' || jsonObj.hypervisor == 'VMware' || jsonObj.hypervisor == 'KVM')
			) 
		{
			buildActionLinkForTab("label.action.migrate.instance", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);
		}
		
		if (jsonObj.isoid == null)	
			buildActionLinkForTab("label.action.attach.iso", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);
		else 		
			buildActionLinkForTab("label.action.detach.iso", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);	
			
		buildActionLinkForTab("label.action.reset.password", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);
		buildActionLinkForTab("label.action.change.service", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);	
		
		if(jsonObj.hypervisor == "BareMetal")
		    buildActionLinkForTab("label.action.create.template", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);
		    
		noAvailableActions = false;	
	} 
	else if (jsonObj.state == 'Stopped') {	    
	    buildActionLinkForTab("label.action.edit.instance", vmActionMap, $actionMenu, $midmenuItem1, $thisTab); 
		buildActionLinkForTab("label.action.start.instance", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);		    
		buildActionLinkForTab("label.action.destroy.instance", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);
		
		if (jsonObj.isoid == null)	
			buildActionLinkForTab("label.action.attach.iso", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);
		else 		
		   buildActionLinkForTab("label.action.detach.iso", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);				    
		
		buildActionLinkForTab("label.action.reset.password", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);
		buildActionLinkForTab("label.action.change.service", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);
		
		if(jsonObj.hypervisor == "BareMetal")
		    buildActionLinkForTab("label.action.create.template", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);
		
		noAvailableActions = false;			    					
	}
	else if (jsonObj.state == 'Starting') {	
		buildActionLinkForTab("label.action.stop.instance", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);
	    noAvailableActions = false;	
	}
	else if (jsonObj.state == 'Error') {	
	    buildActionLinkForTab("label.action.destroy.instance", vmActionMap, $actionMenu, $midmenuItem1, $thisTab);
	    noAvailableActions = false;	
	}
			
	// no available actions 
	if(noAvailableActions == true) {
	    $actionMenu.find("#action_list").append($("#no_available_actions").clone().show());
	}	
}

var vmTopButtonMap = {
    "start_vm_button": 0,
    "stop_vm_button": 0,
    "reboot_vm_button": 0,
    "destroy_vm_button": 0
};

function vmCountTopButtonMap(jsonObj) {       
    if(jsonObj == null)
        return;
        
	if (jsonObj.state == 'Running') {			
		vmTopButtonMap["stop_vm_button"] += 1;	
		vmTopButtonMap["reboot_vm_button"] += 1;
		vmTopButtonMap["destroy_vm_button"] += 1;	
	} 
	else if (jsonObj.state == 'Stopped') {	
		vmTopButtonMap["start_vm_button"] += 1;
		vmTopButtonMap["destroy_vm_button"] += 1;		  					
	}
	else if (jsonObj.state == 'Error') {		    
	    vmTopButtonMap["destroy_vm_button"] += 1;	    
	}
}

function vmUncountTopButtonMap(jsonObj) {       
    if(jsonObj == null)
        return;
        
	if (jsonObj.state == 'Running') {			
		vmTopButtonMap["stop_vm_button"] -= 1;	
		vmTopButtonMap["reboot_vm_button"] -= 1;
		vmTopButtonMap["destroy_vm_button"] -= 1;	
	} 
	else if (jsonObj.state == 'Stopped') {	
		vmTopButtonMap["start_vm_button"] -= 1;
		vmTopButtonMap["destroy_vm_button"] -= 1;		  					
	}
	else if (jsonObj.state == 'Error') {		    
	    vmTopButtonMap["destroy_vm_button"] -= 1;	    
	}
}

function vmGrayoutTopButtons() {    
    var itemCounts = 0;
    for(var id in selectedItemsInMidMenu) {
        itemCounts ++;
    }
      
    for(var buttonElementId in vmTopButtonMap) {
        if(vmTopButtonMap[buttonElementId] < itemCounts) {   
            $("#"+buttonElementId).hide();       
            //$("#"+buttonElementId).find("#button_content").removeClass("actionpanel_button").addClass("actionpanel_button_hidden");
        }
        else {  
            $("#"+buttonElementId).show();               
            //$("#"+buttonElementId).find("#button_content").removeClass("actionpanel_button_hidden").addClass("actionpanel_button"); 
        }        
    }
}

function vmResetTopButtonMap() {
    vmTopButtonMap = {
        "start_vm_button": 0,
        "stop_vm_button": 0,
        "reboot_vm_button": 0,
        "destroy_vm_button": 0
    };
}

function vmJsonToNicTab() {  
	var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");	
	if ($midmenuItem1 == null)  {
	    vmJsonClearNicTab();
	    return;
	}
		
	var jsonObj = $midmenuItem1.data("jsonObj");	
    if(jsonObj == null) {
        vmJsonClearNicTab();
        return;	
    }
	
	var $thisTab = $("#right_panel_content").find("#tab_content_nic");  		
	
	var nics = jsonObj.nic;
	var template = $("#nic_tab_template");
	var $container = $thisTab.find("#tab_container").empty();
	if(nics != null && nics.length > 0) {
	    for (var i = 0; i < nics.length; i++) {
		    var newTemplate = template.clone(true);
		    vmNicJSONToTemplate(nics[i], newTemplate, i+1); 
		    $container.append(newTemplate.show());
	    }
	}
}

function vmJsonClearNicTab() { 
    var $thisTab = $("#right_panel_content").find("#tab_content_nic");  
    $thisTab.find("#tab_container").empty();		
}

function vmJsonToSecurityGroupTab() {  
	var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");	
	if ($midmenuItem1 == null)  {
		vmJsonClearSecurityGroupTab();
	    return;
	}
		
	var jsonObj = $midmenuItem1.data("jsonObj");	
    if(jsonObj == null) {
  	    vmJsonClearSecurityGroupTab();
        return;	
    }
	
	var $thisTab = $("#right_panel_content").find("#tab_content_securitygroup");  		
	
	var items = jsonObj.securitygroup;
	var template = $("#securitygroup_tab_template");	
	var $container = $thisTab.find("#tab_container").find("#grid_content").empty();
	if(items != null && items.length > 0) {
	    for (var i = 0; i < items.length; i++) {
		    var newTemplate = template.clone(true);
		    if(i % 2 == 0)
		    	newTemplate.addClass("even");
		    else
		    	newTemplate.addClass("odd");
		    vmSecurityGroupJSONToTemplate(items[i], newTemplate); 
		    $container.append(newTemplate.show());
	    }
	}
}

function vmJsonClearSecurityGroupTab() { 
  var $thisTab = $("#right_panel_content").find("#tab_content_securitygroup");  
  $thisTab.find("#tab_container").empty();		
}

function vmJsonToVolumeTab() {  
	var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");	
	if ($midmenuItem1 == null) {
	    vmJsonClearVolumeTab();
	    return;
	}
		
	var jsonObj = $midmenuItem1.data("jsonObj");	
    if(jsonObj == null) {
        vmJsonClearVolumeTab();
        return;	
    }
	
	var $thisTab = $("#right_panel_content").find("#tab_content_volume");  		
	$thisTab.find("#tab_container").hide(); 
	$thisTab.find("#tab_spinning_wheel").show();   
	
	$.ajax({
		cache: false,
		data: createURL("command=listVolumes&virtualMachineId="+jsonObj.id),
		dataType: "json",
		success: function(json) {			    
			var items = json.listvolumesresponse.volume;
			var $container = $thisTab.find("#tab_container").empty();
			if (items != null && items.length > 0) {				
				var template = $("#volume_tab_template");				
				for (var i = 0; i < items.length; i++) {
					var newTemplate = template.clone(true);
					vmVolumeJSONToTemplate(items[i], newTemplate); 
					$container.append(newTemplate.show());	
				}
			}	
			$thisTab.find("#tab_spinning_wheel").hide();    
			$thisTab.find("#tab_container").show();    						
		}
	});      
	
}
 
function vmJsonClearVolumeTab() {  
    var $thisTab = $("#right_panel_content").find("#tab_content_volume");  	
    $thisTab.find("#tab_container").empty();	
} 
 
function vmJsonToStatisticsTab() {    
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
	if ($midmenuItem1 == null) {
	    vmJsonClearStatisticsTab();
	    return;	
	}
	
	var jsonObj = $midmenuItem1.data("jsonObj");
	if(jsonObj == null) {
	    vmJsonClearStatisticsTab();
	    return;
	}

    var $thisTab = $("#right_panel_content #tab_content_statistics");  	
	    
    $thisTab.find("#cpunumber").text(fromdb(jsonObj.cpunumber));
    $thisTab.find("#cpuspeed").text(convertHz(jsonObj.cpuspeed));
    
    $thisTab.find("#percentused").text(jsonObj.cpuused); 
    	
	if(jsonObj.networkkbsread == null || jsonObj.networkkbsread == 0)
		$thisTab.find("#networkkbsread").text("N/A");
	else
	    $thisTab.find("#networkkbsread").text(convertBytes(jsonObj.networkkbsread * 1024));
		
	if(jsonObj.networkkbswrite == null || jsonObj.networkkbswrite == 0)
		$thisTab.find("#networkkbswrite").text("N/A");
	else	
	    $thisTab.find("#networkkbswrite").text(convertBytes(jsonObj.networkkbswrite * 1024));	
}

function vmJsonClearStatisticsTab() {       
    var $thisTab = $("#right_panel_content #tab_content_statistics");  	
	var $barChartContainer = $thisTab.find("#cpu_barchart");
	$barChartContainer.find("#cpunumber").text("");	
	$barChartContainer.find("#cpuspeed").text("");	
	$barChartContainer.find("#bar_chart").removeClass().addClass("db_barbox").css("width", "0%");    
	$barChartContainer.find("#percentused").text("");   
	drawBarChart($barChartContainer, null);			
	$thisTab.find("#networkkbsread").text("");	
	$thisTab.find("#networkkbswrite").text("");	
}

function vmClearRightPanel(jsonObj) {      
    vmJsonClearDetailsTab();   
    vmJsonClearNicTab();
    vmJsonClearSecurityGroupTab();
    vmJsonClearVolumeTab();
    vmJsonClearStatisticsTab();     
    $("#tab_details").click();  
}  

function vmJsonClearDetailsTab(){    
	var $thisTab = $("#right_panel_content").find("#tab_content_details");       
	resetViewConsoleAction(null, $thisTab);      
	setVmStateInRightPanel(null, $thisTab.find("#state"));		
	$thisTab.find("#ipAddress").text("");	
	$thisTab.find("#id").text("");
	$thisTab.find("#zoneName").text("");	       
	$thisTab.find("#title").text("");	
	$thisTab.find("#vmname").text("");
	$thisTab.find("#vmname_edit").val("");	
	$thisTab.find("#ipaddress").text("");	
	$thisTab.find("#hypervisor").text("");
	$thisTab.find("#templateName").text("");
	$thisTab.find("#ostypename").text("");
    $thisTab.find("#ostypename_edit").val(""); 	
	$thisTab.find("#serviceOfferingName").text("");	
	$thisTab.find("#account").text("");
	$thisTab.find("#domain").text("");
	$thisTab.find("#hostName").text("");	
	$thisTab.find("#group").text("");	
	$thisTab.find("#group_edit").val("");		
	$thisTab.find("#created").text("");		
	$thisTab.find("#haenable").text("");		
	$thisTab.find("#iso").text("");	
		  
	//actions ***
	var $actionMenu = $("#right_panel_content #tab_content_details #action_link #action_menu");
	$actionMenu.find("#action_list").empty();              
	$actionMenu.find("#action_list").append($("#no_available_actions").clone().show());
}

//***** declaration for volume tab (begin) *********************************************************
var vmVolumeActionMap = {  
    "label.action.detach.disk": {
        api: "detachVolume",            
        isAsyncJob: true,
        asyncJobResponse: "detachvolumeresponse",
        inProcessText: "label.action.detach.disk.processing",
        afterActionSeccessFn: function(json, id, $subgridItem) {         
            $subgridItem.slideUp("slow", function(){                   
                $(this).remove();
            });
        }
    },
    "label.action.create.template": {
        isAsyncJob: true,
        asyncJobResponse: "createtemplateresponse",            
        dialogBeforeActionFn : doCreateTemplateFromVmVolume,
        inProcessText: "label.action.create.template.processing",
        afterActionSeccessFn: function(json, id, $subgridItem) {}         
    },
    "label.action.take.snapshot": { 
        isAsyncJob: true,
        asyncJobResponse: "createsnapshotresponse",            
        dialogBeforeActionFn : doTakeSnapshotFromVmVolume,
        inProcessText: "label.action.take.snapshot.processing",
        afterActionSeccessFn: function(json, id, $subgridItem) {}         
    }
}     

function vmNicJSONToTemplate(json, $template, index) {
	$template.attr("id","vm_nic_"+fromdb(json.id));
	$template.find("#title").text(fromdb("NIC " + index));
	$template.find("#ip").text(fromdb(json.ipaddress));
	$template.find("#type").text(fromdb(json.type)); 
	$template.find("#gateway").text(fromdb(json.gateway)); 
	$template.find("#netmask").text(fromdb(json.netmask)); 
}

function vmSecurityGroupJSONToTemplate(json, $template) {
	$template.attr("id","vm_securitygroup_"+fromdb(json.id));
	$template.find("#id").text(fromdb(json.id));	
	$template.find("#name").text(fromdb(json.name)); 	
	$template.find("#description").text(fromdb(json.description));
		
	$template.find("#show_ingressrule_link").unbind("click").bind("click", function(event){	        
		var $managementArea = $template.find("#management_area");
        var $ingressruleSubgrid = $managementArea.find("#subgrid_content").empty();           
              
        $.ajax({
    		cache: false,		
    		data: createURL("command=listSecurityGroups&id="+json.id),
    		dataType: "json",
    		async: false,
    		success: function(json) {	
    		    var securityGroupObj = json.listsecuritygroupsresponse.securitygroup[0];		    				    
    			var items = securityGroupObj.ingressrule;        																					
    			if (items != null && items.length > 0) {			    
    				var template = $("#ingressrule_template");				
    				for (var i = 0; i < items.length; i++) {
    					var newTemplate = template.clone(true);	               
    	                securityGroupIngressRuleJSONToTemplate(items[i], newTemplate); 
    	                $ingressruleSubgrid.append(newTemplate.show());	
    				}			
    			}	    					
    		}
    	});     
        
        $managementArea.show();		           
        $template.find("#show_ingressrule_link").hide();
        $template.find("#hide_ingressrule_link").show();        
        return false;
    });
	
	$template.find("#hide_ingressrule_link").unbind("click").bind("click", function(event){	            
        $template.find("#management_area").hide();   
        $template.find("#hide_ingressrule_link").hide();
        $template.find("#show_ingressrule_link").show();
        return false;
    });	
}

function securityGroupIngressRuleJSONToTemplate(jsonObj, $template) {
    $template.data("jsonObj", jsonObj);     
    $template.attr("id", "securitygroup_ingressRule_"+fromdb(jsonObj.ruleid));   
       		   
    $template.find("#id").text(fromdb(jsonObj.ruleid));       
    $template.find("#protocol").text(jsonObj.protocol);
    			    		    
    var endpoint;		    
    if(jsonObj.protocol == "icmp")
        endpoint = "ICMP Type=" + ((jsonObj.icmptype!=null)?jsonObj.icmptype:"") + ", code=" + ((jsonObj.icmpcode!=null)?jsonObj.icmpcode:"");		        
    else //tcp, udp
        endpoint = "Port Range " + ((jsonObj.startport!=null)?jsonObj.startport:"") + "-" + ((jsonObj.endport!=null)?jsonObj.endport:"");		    
    $template.find("#endpoint").text(endpoint);	
    
    var cidrOrGroup;
    if(jsonObj.cidr != null && jsonObj.cidr.length > 0)
        cidrOrGroup = jsonObj.cidr;
    else if (jsonObj.account != null && jsonObj.account.length > 0 &&  jsonObj.securitygroupname != null && jsonObj.securitygroupname.length > 0)
        cidrOrGroup = jsonObj.account + "/" + jsonObj.securitygroupname;		    
    $template.find("#cidr").text(cidrOrGroup);	 
} 

function vmVolumeJSONToTemplate(json, $template) {
    $template.attr("id","vm_volume_"+fromdb(json.id));	        
    $template.data("jsonObj", json);    
    $template.find("#title").text(fromdb(json.name));    
	$template.find("#id").text(fromdb(json.id));	
	$template.find("#name").text(fromdb(json.name));
	if (json.storagetype == "shared") 
		$template.find("#type").text(fromdb(json.type) + " (shared storage)");
	else 
		$template.find("#type").text(fromdb(json.type) + " (local storage)");
			
	$template.find("#size").text((json.size == "0") ? "" : convertBytes(json.size));										
	setDateField(json.created, $template.find("#created"));
	
	//***** actions (begin) *****
	var $actionLink = $template.find("#action_link");		
	bindActionLink($actionLink);
		
	var $actionMenu = $actionLink.find("#action_menu");
    $actionMenu.find("#action_list").empty();
    var noAvailableActions = true;
    	
	if(json.hypervisor != "Ovm") {   
		buildActionLinkForSubgridItem("label.action.take.snapshot", vmVolumeActionMap, $actionMenu, $template);	 
		noAvailableActions = false;		
	}
     
	if(json.type=="ROOT") { //"label.action.create.template" is allowed(when stopped), "label.action.detach.disk" is disallowed.
		if (json.vmstate == "Stopped") {
		    buildActionLinkForSubgridItem("label.action.create.template", vmVolumeActionMap, $actionMenu, $template);	
		    noAvailableActions = false;		
		}
	} 
	else { //json.type=="DATADISK": "label.action.detach.disk" is allowed, "label.action.create.template" is disallowed.			
		buildActionLinkForSubgridItem("label.action.detach.disk", vmVolumeActionMap, $actionMenu, $template);		
		noAvailableActions = false;				
	}	
	
	// no available actions 
	if(noAvailableActions == true) {	    
	    $actionMenu.find("#action_list").append($("#no_available_actions").clone().show());
	}	  
	//***** actions (end) *****		
}
	
//***** declaration for volume tab (end) *********************************************************

function appendInstanceGroup(groupId, groupName) {
    var $leftmenuSubmenuTemplate = $("#leftmenu_submenu_template").clone().show();			        	    
    $leftmenuSubmenuTemplate.attr("id", ("leftmenu_instance_group_"+groupId));		
    $leftmenuSubmenuTemplate.data("groupId", groupId)	        	            	
    $leftmenuSubmenuTemplate.find("#submenu_name").text(groupName);
    $leftmenuSubmenuTemplate.find("#icon").attr("src", "images/instance_leftmenuicon.png").show();
     		                			                
    $leftmenuSubmenuTemplate.bind("click", function(event) { 
        $("#midmenu_container").empty();
        selectedItemsInMidMenu = {};
                                    
        var groupId = $(this).data("groupId");                                   
        $.ajax({
            cache: false,
            data: createURL("command=listVirtualMachines&groupid="+groupId+"&pagesize="+midmenuItemCount+"&page=1"),
            dataType: "json",
            success: function(json) {		                                                             
                var instances = json.listvirtualmachinesresponse.virtualmachine;    
                if (instances != null && instances.length > 0) {
                    var $template = $("#midmenu_item"); 	                           
                    for(var i=0; i<instances.length;i++) {  
                        var $midmenuItem1 = $template.clone();                                                                                                                                              
                        vmToMidmenu(instances[i], $midmenuItem1); 
                        bindClickToMidMenu($midmenuItem1, vmToRightPanel, getMidmenuId);  
                        $("#midmenu_container").append($midmenuItem1.show()); 
                        if(i == 0)  //click the 1st item in middle menu as default  
                            clickItemInMultipleSelectionMidmenu($midmenuItem1); 
                    }  
                }  
            }
        });                            
        return false;
    });	
    $("#leftmenu_instance_group_container").append($leftmenuSubmenuTemplate);
}	

function doCreateTemplateFromVmVolume($actionLink, $subgridItem) {        
    var $dialogCreateTemplate = $("#dialog_create_template_from_volume");    
       
    var jsonObj = $subgridItem.data("jsonObj");
    
    $dialogCreateTemplate
	.dialog('option', 'buttons', { 						
		"OK": function() { 		
		    var thisDialog = $(this);		    
									
			// validate values
	        var isValid = true;					
	        isValid &= validateString("Name", thisDialog.find("#create_template_name"), thisDialog.find("#create_template_name_errormsg"));
			isValid &= validateString("Display Text", thisDialog.find("#create_template_desc"), thisDialog.find("#create_template_desc_errormsg"));			
	        if (!isValid) 
	            return;		
	        
	        thisDialog.dialog("close"); 
	        
	        var name = trim(thisDialog.find("#create_template_name").val());
			var desc = trim(thisDialog.find("#create_template_desc").val());
			var osType = thisDialog.find("#create_template_os_type").val();					
			var isPublic = thisDialog.find("#create_template_public").val();
            var password = thisDialog.find("#create_template_password").val();				
			
			var id = $subgridItem.data("jsonObj").id;			
			var apiCommand = "command=createTemplate&volumeId="+id+"&name="+todb(name)+"&displayText="+todb(desc)+"&osTypeId="+osType+"&isPublic="+isPublic+"&passwordEnabled="+password;
	    	doActionToSubgridItem(id, $actionLink, apiCommand, $subgridItem);					
		}, 
		"Cancel": function() { 
			$(this).dialog("close"); 
		} 
	}).dialog("open");
}   

function doCreateTemplateFromVM($actionLink, $detailsTab, $midmenuItem1) {  
	var $dialogCreateTemplate = $("#dialog_create_template_from_vm");
	
	if (getUserPublicTemplateEnabled() == "true" || isAdmin()) {
		$dialogCreateTemplate.find("#create_template_public_container").show();	
	}	
   
    var jsonObj = $midmenuItem1.data("jsonObj");     
    
    $dialogCreateTemplate
	.dialog('option', 'buttons', { 						
		"Create": function() { 		   
		    var $thisDialog = $(this);
		    									
			// validate values
	        var isValid = true;					
	        isValid &= validateString("Name", $thisDialog.find("#create_template_name"), $thisDialog.find("#create_template_name_errormsg"));
			isValid &= validateString("Display Text", $thisDialog.find("#create_template_desc"), $thisDialog.find("#create_template_desc_errormsg"));	
	        isValid &= validateString("Image Directory", $thisDialog.find("#image_directory"), $thisDialog.find("#image_directory_errormsg"), false); //image directory is required when creating template from VM whose hypervisor is BareMetal
			if (!isValid) 
	        	return;		
	        
			$thisDialog.dialog("close"); 
			
			var array1 = [];
						
	        var name = $thisDialog.find("#create_template_name").val();
	        array1.push("&name="+todb(name));
	        
			var desc = $thisDialog.find("#create_template_desc").val();
			array1.push("&displayText="+todb(desc));
			
			var osType = $thisDialog.find("#create_template_os_type").val();	
			array1.push("&osTypeId="+osType);
			
			var isPublic = $thisDialog.find("#create_template_public").val();
			array1.push("&isPublic="+isPublic);
			           
            var imageDirectory = $thisDialog.find("#image_directory").val();
	        array1.push("&url="+todb(imageDirectory));
			
	        var id = $midmenuItem1.data("jsonObj").id;		
			var apiCommand = "command=createTemplate&virtualmachineid="+id+array1.join("");
	    	doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);					
		}, 
		"Cancel": function() { 
			$(this).dialog("close"); 
		} 
	}).dialog("open");
}   

function doTakeSnapshotFromVmVolume($actionLink, $subgridItem) {  
    $("#dialog_confirmation")
    .text(dictionary["message.action.take.snapshot"])					
    .dialog('option', 'buttons', { 					    
	    "Confirm": function() { 	
	        $(this).dialog("close");	
	    	
            var id = $subgridItem.data("jsonObj").id;	
			var apiCommand = "command=createSnapshot&volumeid="+id;
	    	doActionToSubgridItem(id, $actionLink, apiCommand, $subgridItem);
	    },
	    "Cancel": function() { 					        
		    $(this).dialog("close"); 
	    } 
    }).dialog("open");	  
}		
