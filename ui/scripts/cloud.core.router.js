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

function routerGetSearchParams() {
    var moreCriteria = [];	

	var searchInput = $("#basic_search").find("#search_input").val();	 
    if (searchInput != null && searchInput.length > 0) {	           
        moreCriteria.push("&keyword="+todb(searchInput));	       
    }     

	var $advancedSearchPopup = getAdvancedSearchPopupInSearchContainer();
	if ($advancedSearchPopup.length > 0 && $advancedSearchPopup.css("display") != "none" ) {    
		var state = $advancedSearchPopup.find("#adv_search_state").val();
		if (state!=null && state.length > 0) 
			moreCriteria.push("&state="+todb(state));		
				
		var zone = $advancedSearchPopup.find("#adv_search_zone").val();	
	    if (zone!=null && zone.length > 0) 
			moreCriteria.push("&zoneId="+zone);	
		
		if ($advancedSearchPopup.find("#adv_search_pod_li").css("display") != "none") {	
		    var pod = $advancedSearchPopup.find("#adv_search_pod").val();		
	        if (pod!=null && pod.length > 0) 
			    moreCriteria.push("&podId="+pod);
        }
        
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
		    if (account!=null && account.length > 0) 
			    moreCriteria.push("&account="+account);		
		}	
	} 	
	
	return moreCriteria.join("");          
}

function afterLoadRouterJSP() {
	// dialogs    
    initDialog("dialog_change_system_service_offering", 600);  
}

function routerToMidmenu(jsonObj, $midmenuItem1) {
    $midmenuItem1.attr("id", getMidmenuId(jsonObj));  
    $midmenuItem1.data("jsonObj", jsonObj); 
    
    var firstRowText = fromdb(jsonObj.name);
    $midmenuItem1.find("#first_row").text(clippedText(firstRowText, midMenuFirstRowLength));     
    $midmenuItem1.find("#first_row_container").attr("title", firstRowText);   
    
    var secondRowText = fromdb(jsonObj.publicip);
    $midmenuItem1.find("#second_row").text(clippedText(secondRowText, midMenuSecondRowLength));
    $midmenuItem1.find("#second_row_container").attr("title", secondRowText); 
    
    updateVmStateInMidMenu(jsonObj, $midmenuItem1);       
}

function routerToRightPanel($midmenuItem1) { 
    copyActionInfoFromMidMenuToRightPanel($midmenuItem1);
    $("#right_panel_content").data("$midmenuItem1", $midmenuItem1);
    routerJsonToDetailsTab();   
}

function routerJsonToDetailsTab() {   
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
    if($midmenuItem1 == null) {
        routerClearDetailsTab();
        return;
    }
    
    var jsonObj = $midmenuItem1.data("jsonObj");
    if(jsonObj == null) {
        routerClearDetailsTab();
        return;       
    }
    
    var $thisTab = $("#right_panel_content").find("#tab_content_details");  
    $thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();   
         
    $.ajax({
        data: createURL("command=listRouters&id="+jsonObj.id),
        dataType: "json",
        async: false,
        success: function(json) {  
            var items = json.listroutersresponse.router;                   
            if(items != null && items.length > 0) {
                jsonObj = items[0];
                $midmenuItem1.data("jsonObj", jsonObj);  
                updateVmStateInMidMenu(jsonObj, $midmenuItem1);   
            }
        }
    });     
           
    $thisTab.find("#grid_header_title").text(fromdb(jsonObj.name));            
    setVmStateInRightPanel(fromdb(jsonObj.state), $thisTab.find("#state"));  
    
    
    //refresh status every 2 seconds until status is not Starting/Stopping any more 
	var timerKey = "refreshRouterStatus";
	$("body").stopTime(timerKey);  //stop timer used by another middle menu item (i.e. stop timer when clicking on a different middle menu item)		
	if($midmenuItem1.find("#spinning_wheel").css("display") == "none") {
	    if(jsonObj.state in vmChangableStatus) {	    
	        $("body").everyTime(
                5000,
                timerKey,
                function() {              
                    $.ajax({
		                data: createURL("command=listRouters&id="+jsonObj.id),
		                dataType: "json",
		                async: false,
		                success: function(json) {  
			                var items = json.listroutersresponse.router;
			                if(items != null && items.length > 0) {
				                jsonObj = items[0]; //override jsonObj declared above				
				                $midmenuItem1.data("jsonObj", jsonObj); 				                            
				                if(!(jsonObj.state in vmChangableStatus)) {
				                    $("body").stopTime(timerKey);					                    
				                    updateVmStateInMidMenu(jsonObj, $midmenuItem1); 				                    
				                    if(jsonObj.id.toString() == $("#right_panel_content").find("#tab_content_details").find("#id").text()) {
				                        setVmStateInRightPanel(jsonObj.state, $thisTab.find("#state"));	
				                        routerBuildActionMenu(jsonObj, $thisTab, $midmenuItem1);	
				                    }					                    
				                }               
	                        }   
		                }
	                });                       	
                }
            );
	    }
	}
	
    
    $thisTab.find("#id").text(fromdb(jsonObj.id));  
    $thisTab.find("#ipAddress").text(fromdb(jsonObj.publicip));
    $thisTab.find("#zonename").text(fromdb(jsonObj.zonename));
    $thisTab.find("#name").text(fromdb(jsonObj.name));
    $thisTab.find("#publicip").text(fromdb(jsonObj.publicip));
    $thisTab.find("#privateip").text(fromdb(jsonObj.linklocalip));
    $thisTab.find("#guestipaddress").text(fromdb(jsonObj.guestipaddress));
    $thisTab.find("#hostname").text(fromdb(jsonObj.hostname));
    $thisTab.find("#serviceOfferingName").text(fromdb(jsonObj.serviceofferingname));	
    $thisTab.find("#networkdomain").text(fromdb(jsonObj.networkdomain));
    $thisTab.find("#domain").text(fromdb(jsonObj.domain));  
    $thisTab.find("#account").text(fromdb(jsonObj.account));  
    setDateField(jsonObj.created, $thisTab.find("#created"));	
        
    setBooleanReadField(jsonObj.isredundantrouter, $thisTab.find("#isredundantrouter"));
    if(jsonObj.isredundantrouter == true) {
    	var t = $thisTab.find("#isredundantrouter").text()+ " (" + fromdb(jsonObj.redundantstate) + ")";
    	$thisTab.find("#isredundantrouter").text(t);
    }   
    
    resetViewConsoleAction(jsonObj, $thisTab);   
    
    // actions
    routerBuildActionMenu(jsonObj, $thisTab, $midmenuItem1);
    
    $thisTab.find("#tab_spinning_wheel").hide();    
    $thisTab.find("#tab_container").show();     		    
}        

function routerBuildActionMenu(jsonObj, $thisTab, $midmenuItem1) {  
    var $actionMenu = $("#right_panel_content #tab_content_details #action_link #action_menu");
    $actionMenu.find("#action_list").empty();
    var noAvailableActions = true;
   
    if (jsonObj.state == 'Running') {   
        buildActionLinkForTab("label.action.stop.router", routerActionMap, $actionMenu, $midmenuItem1, $thisTab);	
        buildActionLinkForTab("label.action.reboot.router", routerActionMap, $actionMenu, $midmenuItem1, $thisTab);	  
        buildActionLinkForTab("label.action.change.service", routerActionMap, $actionMenu, $midmenuItem1, $thisTab);
        noAvailableActions = false;      
    }
    else if (jsonObj.state == 'Stopped') {        
        buildActionLinkForTab("label.action.start.router", routerActionMap, $actionMenu, $midmenuItem1, $thisTab);	
        buildActionLinkForTab("label.action.change.service", routerActionMap, $actionMenu, $midmenuItem1, $thisTab);
        noAvailableActions = false;
    }  
    
    // no available actions 
	if(noAvailableActions == true) {
	    $actionMenu.find("#action_list").append($("#no_available_actions").clone().show());
	}	   
}

function routerClearDetailsTab() {     
    var $thisTab = $("#right_panel_content").find("#tab_content_details");           
    $thisTab.find("#grid_header_title").text("");            
    setVmStateInRightPanel(null, $thisTab.find("#state"));  
    $thisTab.find("#ipAddress").text("");
    $thisTab.find("#zonename").text("");
    $thisTab.find("#name").text("");
    $thisTab.find("#publicip").text("");
    $thisTab.find("#privateip").text("");
    $thisTab.find("#guestipaddress").text("");
    $thisTab.find("#hostname").text("");
    $thisTab.find("#serviceOfferingName").text("");	
    $thisTab.find("#networkdomain").text("");
    $thisTab.find("#domain").text("");  
    $thisTab.find("#account").text("");  
    $thisTab.find("#created").text("");   
    $thisTab.find("#isredundantrouter").text("");
        
    resetViewConsoleAction(null, $thisTab);       
    
    var $actionMenu = $("#right_panel_content #tab_content_details #action_link #action_menu");
    $actionMenu.find("#action_list").empty();
    $actionMenu.find("#action_list").append($("#no_available_actions").clone().show());   		    
}       
 
function doStartRouter($actionLink, $detailsTab, $midmenuItem1) {     
    $("#dialog_confirmation")
    .text(dictionary["message.action.start.router"])	
    .dialog('option', 'buttons', { 						
	    "Confirm": function() { 
		    $(this).dialog("close"); 			
		    
		    var jsonObj = $midmenuItem1.data("jsonObj");
		    var id = jsonObj.id;
		    var apiCommand = "command=startRouter&id="+id;  
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab); 			   	                         					    
	    }, 
	    "Cancel": function() { 
		    $(this).dialog("close"); 
			
	    } 
    }).dialog("open");
}  
 
function doStopRouter($actionLink, $detailsTab, $midmenuItem1) {     
    $("#dialog_confirmation")
    .text(dictionary["message.action.stop.router"])	
    .dialog('option', 'buttons', { 						
	    "Confirm": function() { 
		    $(this).dialog("close"); 			
		    
		    var jsonObj = $midmenuItem1.data("jsonObj");
		    var id = jsonObj.id;
		    var apiCommand = "command=stopRouter&id="+id;  
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab); 			   	                         					    
	    }, 
	    "Cancel": function() { 
		    $(this).dialog("close"); 
			
	    } 
    }).dialog("open");
}   
   
function doRebootRouter($actionLink, $detailsTab, $midmenuItem1) {     
    $("#dialog_confirmation")
    .text(dictionary["message.action.reboot.router"])	
    .dialog('option', 'buttons', { 						
	    "Confirm": function() { 
		    $(this).dialog("close"); 			
		    
		    var jsonObj = $midmenuItem1.data("jsonObj");
		    var id = jsonObj.id;
		    var apiCommand = "command=rebootRouter&id="+id;  
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab); 			   	                         					    
	    }, 
	    "Cancel": function() { 
		    $(this).dialog("close"); 
			
	    } 
    }).dialog("open");
}     
  
function doChangeSystemServiceOffering($actionLink, $detailsTab, $midmenuItem1) {    
    var jsonObj = $midmenuItem1.data("jsonObj");
	var id = jsonObj.id;
	
	if (jsonObj.state != 'Stopped') {
		$("#dialog_info")
			.text(dictionary['message.action.change.service.warning.for.router'])    
			.dialog('option', 'buttons', { 	
			"OK": function() { 
				$(this).dialog("close"); 
			}	 
		}).dialog("open");
		return;
	}
	
	$.ajax({	   
	    data: createURL("command=listServiceOfferings&issystem=true&systemvmtype=domainrouter"), 
		dataType: "json",
		async: false,
		success: function(json) {
			var offerings = json.listserviceofferingsresponse.serviceoffering;
			var offeringSelect = $("#dialog_change_system_service_offering #change_service_offerings").empty();
			
			if (offerings != null && offerings.length > 0) {
				for (var i = 0; i < offerings.length; i++) {
					if(offerings[i].id != jsonObj.serviceofferingid) {
						var option = $("<option value='" + offerings[i].id + "'>" + fromdb(offerings[i].name) + "</option>").data("name", fromdb(offerings[i].name));
						offeringSelect.append(option); 
					}
				}
			} 
		}
	});
	
	$("#dialog_change_system_service_offering")
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
            var apiCommand = "command=changeServiceForRouter&id="+id+"&serviceofferingid="+serviceOfferingId;	     
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);				
		}, 
		"Cancel": function() { 
			$(this).dialog("close"); 			
		} 
	}).dialog("open");
}

var routerActionMap = {      
    "label.action.start.router": {        
        isAsyncJob: true,
        asyncJobResponse: "startrouterresponse",
        inProcessText: "label.action.start.router.processing",
        dialogBeforeActionFn : doStartRouter,
        afterActionSeccessFn: function(json, $midmenuItem1, id) {
            var item = json.queryasyncjobresultresponse.jobresult.domainrouter;    
            routerToMidmenu(item, $midmenuItem1);             
        }
    },
    "label.action.stop.router": {          
        isAsyncJob: true,
        asyncJobResponse: "stoprouterresponse",
        inProcessText: "label.action.stop.router.processing",
        dialogBeforeActionFn : doStopRouter,
        afterActionSeccessFn: function(json, $midmenuItem1, id) {
            var item = json.queryasyncjobresultresponse.jobresult.domainrouter;    
            routerToMidmenu(item, $midmenuItem1);              
        }
    },
    "label.action.reboot.router": {           
        isAsyncJob: true,
        asyncJobResponse: "rebootrouterresponse",
        inProcessText: "label.action.reboot.router.processing",
        dialogBeforeActionFn : doRebootRouter,
        afterActionSeccessFn: function(json, $midmenuItem1, id) {
            var item = json.queryasyncjobresultresponse.jobresult.domainrouter;    
            routerToMidmenu(item, $midmenuItem1);              
        }
    },
    "label.action.change.service": {
        isAsyncJob: false,        
        inProcessText: "label.action.change.service",
        dialogBeforeActionFn : doChangeSystemServiceOffering,
        afterActionSeccessFn: function(json, $midmenuItem1, id) { 
            var jsonObj = json.changeserviceforrouterresponse.domainrouter;       
            vmToMidmenu(jsonObj, $midmenuItem1);           
        }
    }
}   