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
 
 function systemVmGetSearchParams() {
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
	} 	
	
	return moreCriteria.join("");          
}
 
function afterLoadSystemVmJSP($midmenuItem1) {  
  
}

function systemvmToMidmenu(jsonObj, $midmenuItem1) {
    $midmenuItem1.attr("id", getMidmenuId(jsonObj));  
    $midmenuItem1.data("jsonObj", jsonObj); 
    
    var $iconContainer = $midmenuItem1.find("#icon_container").show();   
    $iconContainer.find("#icon").attr("src", "images/midmenuicon_resource_systemvm.png");		
       
    var firstRowText = fromdb(jsonObj.name);
    $midmenuItem1.find("#first_row").text(clippedText(firstRowText, midMenuFirstRowLength));     
    $midmenuItem1.find("#first_row_container").attr("title", firstRowText);   
    
    var secondRowText = fromdb(jsonObj.publicip);
    $midmenuItem1.find("#second_row").text(clippedText(secondRowText, midMenuSecondRowLength));
    $midmenuItem1.find("#second_row_container").attr("title", secondRowText); 
    
    updateVmStateInMidMenu(jsonObj, $midmenuItem1);      
}

function systemvmToRightPanel($midmenuItem1) {    
    copyActionInfoFromMidMenuToRightPanel($midmenuItem1);
    $("#right_panel_content").data("$midmenuItem1", $midmenuItem1);
    systemvmJsonToDetailsTab();
}

function systemvmJsonToDetailsTab() {
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
    if($midmenuItem1 == null) {
        systemvmClearDetailsTab();
        return;
    }
    
    var jsonObj = $midmenuItem1.data("jsonObj");
    if(jsonObj == null) {
        systemvmClearDetailsTab();
        return;
    }
     
    var $thisTab = $("#right_panel_content").find("#tab_content_details");  
    $thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();   
       
    $.ajax({
        data: createURL("command=listSystemVms&id="+jsonObj.id),
        dataType: "json",
        async: false,
        success: function(json) {  
            var items = json.listsystemvmsresponse.systemvm;                   
            if(items != null && items.length > 0) {
                jsonObj = items[0];
                $midmenuItem1.data("jsonObj", jsonObj);  
                updateVmStateInMidMenu(jsonObj, $midmenuItem1);  
            }
        }
    });     
       
    $thisTab.find("#grid_header_title").text(fromdb(jsonObj.name));       
    resetViewConsoleAction(jsonObj, $thisTab);         
    setVmStateInRightPanel(fromdb(jsonObj.state), $thisTab.find("#state"));	
    
    
    //refresh status every 2 seconds until status is not Starting/Stopping any more 
	var timerKey = "refreshSystemvmStatus";
	$("body").stopTime(timerKey);  //stop timer used by another middle menu item (i.e. stop timer when clicking on a different middle menu item)		
	if($midmenuItem1.find("#spinning_wheel").css("display") == "none") {
	    if(jsonObj.state in vmChangableStatus) {	    
	        $("body").everyTime(
                5000,
                timerKey,
                function() {              
                    $.ajax({
		                data: createURL("command=listSystemVms&id="+jsonObj.id),
		                dataType: "json",
		                async: false,
		                success: function(json) {  
			                var items = json.listsystemvmsresponse.systemvm; 
			                if(items != null && items.length > 0) {
				                jsonObj = items[0]; //override jsonObj declared above				
				                $midmenuItem1.data("jsonObj", jsonObj); 				                            
				                if(!(jsonObj.state in vmChangableStatus)) {
				                    $("body").stopTime(timerKey);					                    
				                    updateVmStateInMidMenu(jsonObj, $midmenuItem1); 				                    
				                    if(jsonObj.id.toString() == $("#right_panel_content").find("#tab_content_details").find("#id").text()) {
				                        setVmStateInRightPanel(jsonObj.state, $thisTab.find("#state"));	
				                        systemvmBuildActionMenu(jsonObj, $thisTab, $midmenuItem1);	
				                    }					                    
				                }               
	                        }   
		                }
	                });                       	
                }
            );
	    }
	}
    	
    	
    $thisTab.find("#ipAddress").text(fromdb(jsonObj.publicip));        
    $thisTab.find("#state").text(fromdb(jsonObj.state));     
    $thisTab.find("#systemvmtype").text(toSystemVMTypeText(jsonObj.systemvmtype));    
    $thisTab.find("#zonename").text(fromdb(jsonObj.zonename)); 
    $thisTab.find("#id").text(fromdb(jsonObj.id));  
    $thisTab.find("#name").text(fromdb(jsonObj.name));     
    $thisTab.find("#publicip").text(fromdb(jsonObj.publicip)); 
    $thisTab.find("#privateip").text(fromdb(jsonObj.privateip));
	$thisTab.find("#linklocalip").text(fromdb(jsonObj.linklocalip));
    $thisTab.find("#hostname").text(fromdb(jsonObj.hostname));
    $thisTab.find("#gateway").text(fromdb(jsonObj.gateway)); 
    $thisTab.find("#created").text(fromdb(jsonObj.created));   
    
    if(jsonObj.systemvmtype == "consoleproxy") {
        $thisTab.find("#activeviewersessions").text(fromdb(jsonObj.activeviewersessions)); 
        $thisTab.find("#activeviewersessions_container").show();
    }
    else {  //jsonObj.systemvmtype == "secondarystoragevm"
        $thisTab.find("#activeviewersessions").text(""); 
        $thisTab.find("#activeviewersessions_container").hide();
    }    
        
    //actions
    systemvmBuildActionMenu(jsonObj, $thisTab, $midmenuItem1);
	
	$thisTab.find("#tab_spinning_wheel").hide();    
    $thisTab.find("#tab_container").show();      
}

function systemvmBuildActionMenu(jsonObj, $thisTab, $midmenuItem1) {  
    var $actionLink = $thisTab.find("#action_link"); 
    bindActionLink($actionLink);
   
    var $actionMenu = $actionLink.find("#action_menu");
    $actionMenu.find("#action_list").empty();   
	var noAvailableActions = true;
	
	if (jsonObj.state == 'Running') {	
	    buildActionLinkForTab("label.action.stop.systemvm", systemVmActionMap, $actionMenu, $midmenuItem1, $thisTab);     
        buildActionLinkForTab("label.action.reboot.systemvm", systemVmActionMap, $actionMenu, $midmenuItem1, $thisTab);           
        buildActionLinkForTab("label.action.destroy.systemvm", systemVmActionMap, $actionMenu, $midmenuItem1, $thisTab); 
        noAvailableActions = false;	  
	} 
	else if (jsonObj.state == 'Stopped') { 
	    buildActionLinkForTab("label.action.start.systemvm", systemVmActionMap, $actionMenu, $midmenuItem1, $thisTab); 
	    buildActionLinkForTab("label.action.destroy.systemvm", systemVmActionMap, $actionMenu, $midmenuItem1, $thisTab);  
	    noAvailableActions = false;	 
	} 
	else if (jsonObj.state == 'Error') {
	    buildActionLinkForTab("label.action.destroy.systemvm", systemVmActionMap, $actionMenu, $midmenuItem1, $thisTab);   
	    noAvailableActions = false;	
	} 
	
	// no available actions 
	if(noAvailableActions == true) {
	    $actionMenu.find("#action_list").append($("#no_available_actions").clone().show());
	}	 
}

function systemvmClearDetailsTab() {    
    var $thisTab = $("#right_panel_content").find("#tab_content_details");        
    $thisTab.find("#grid_header_title").text(fromdb(jsonObj.name));       
    resetViewConsoleAction(null, $thisTab);         
    setVmStateInRightPanel(null, $thisTab.find("#state"));		
    $thisTab.find("#ipAddress").text(fromdb(jsonObj.publicip));        
    $thisTab.find("#state").text(fromdb(jsonObj.state));     
    $thisTab.find("#systemvmtype").text(toSystemVMTypeText(jsonObj.systemvmtype));    
    $thisTab.find("#zonename").text(fromdb(jsonObj.zonename)); 
    $thisTab.find("#id").text(fromdb(jsonObj.id));  
    $thisTab.find("#name").text(fromdb(jsonObj.name));     
    $thisTab.find("#publicip").text(fromdb(jsonObj.publicip)); 
    $thisTab.find("#privateip").text(fromdb(jsonObj.privateip));
	$thisTab.find("#linklocalip").text(fromdb(jsonObj.linklocalip));	
    $thisTab.find("#hostname").text(fromdb(jsonObj.hostname));
    $thisTab.find("#gateway").text(fromdb(jsonObj.gateway)); 
    $thisTab.find("#created").text(fromdb(jsonObj.created));   
    $thisTab.find("#activeviewersessions").text(""); 
    
    var $actionMenu = $("#right_panel_content #tab_content_details #action_link #action_menu");
    $actionMenu.find("#action_list").empty();
    $actionMenu.find("#action_list").append($("#no_available_actions").clone().show());   		    
}

function toSystemVMTypeText(value) {
    var text = "";
    if(value == "consoleproxy")
        text = "Console Proxy VM";
    else if(value == "secondarystoragevm")
        text = "Secondary Storage VM";
    return text;        
}

var systemVmActionMap = {      
    "label.action.start.systemvm": {             
        isAsyncJob: true,
        asyncJobResponse: "startsystemvmresponse",
        inProcessText: "label.action.start.systemvm.processing",
        dialogBeforeActionFn : doStartSystemVM,
        afterActionSeccessFn: function(json, $midmenuItem1, id) {            
            var jsonObj = json.queryasyncjobresultresponse.jobresult.systemvm;  
            systemvmToMidmenu(jsonObj, $midmenuItem1);                   
        }
    },
    "label.action.stop.systemvm": {            
        isAsyncJob: true,
        asyncJobResponse: "stopsystemvmresponse",
        inProcessText: "label.action.stop.systemvm.processing",
        dialogBeforeActionFn : doStopSystemVM,
        afterActionSeccessFn: function(json, $midmenuItem1, id) {           
            var jsonObj = json.queryasyncjobresultresponse.jobresult.systemvm;                  	
            systemvmToMidmenu(jsonObj, $midmenuItem1);           
        }
    },
    "label.action.reboot.systemvm": {        
        isAsyncJob: true,
        asyncJobResponse: "rebootsystemvmresponse",
        inProcessText: "label.action.reboot.systemvm.processing",
        dialogBeforeActionFn : doRebootSystemVM,
        afterActionSeccessFn: function(json, $midmenuItem1, id) {            
            var jsonObj = json.queryasyncjobresultresponse.jobresult.systemvm;              
            systemvmToMidmenu(jsonObj, $midmenuItem1);                
        }
    },
     "label.action.destroy.systemvm": {                
        isAsyncJob: true,  
        asyncJobResponse: "destroysystemvmresponse",
        dialogBeforeActionFn : doDestroySystemVM,      
        inProcessText: "label.action.destroy.systemvm.processing",
        afterActionSeccessFn: function(json, $midmenuItem1, id) {  
            $midmenuItem1.slideUp("slow", function() {
                   
            });          
        }
    }
}   

function doStartSystemVM($actionLink, $detailsTab, $midmenuItem1) {   
    $("#dialog_confirmation")
    .text(dictionary["message.action.start.systemvm"])	
    .dialog('option', 'buttons', { 						
	    "Confirm": function() { 
		    $(this).dialog("close"); 			
		    
		    var jsonObj = $midmenuItem1.data("jsonObj");
		    var id = jsonObj.id;
		    var apiCommand = "command=startSystemVm&id="+id;              
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab); 			   			   	                         					    
	    }, 
	    "Cancel": function() { 
		    $(this).dialog("close"); 
			
	    } 
    }).dialog("open");
}   

function doStopSystemVM($actionLink, $detailsTab, $midmenuItem1) {     
    $("#dialog_confirmation")	
    .text(dictionary["message.action.stop.systemvm"])
    .dialog('option', 'buttons', { 						
	    "Confirm": function() { 
		    $(this).dialog("close"); 			
		    
		    var jsonObj = $midmenuItem1.data("jsonObj");
		    var id = jsonObj.id;
		    var apiCommand = "command=stopSystemVm&id="+id;  
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab); 			   	                         					    
	    }, 
	    "Cancel": function() { 
		    $(this).dialog("close"); 
			
	    } 
    }).dialog("open");
}   
   
function doRebootSystemVM($actionLink, $detailsTab, $midmenuItem1) {   
    $("#dialog_confirmation")	
    .text(dictionary["message.action.reboot.systemvm"])
    .dialog('option', 'buttons', { 						
	    "Confirm": function() { 
		    $(this).dialog("close"); 			
		    
		    var jsonObj = $midmenuItem1.data("jsonObj");
		    var id = jsonObj.id;
		    var apiCommand = "command=rebootSystemVm&id="+id;              
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab); 		   			   	                         					    
	    }, 
	    "Cancel": function() { 
		    $(this).dialog("close"); 
			
	    } 
    }).dialog("open");
}   

function doDestroySystemVM($actionLink, $detailsTab, $midmenuItem1) {       
    var jsonObj = $midmenuItem1.data("jsonObj");
	var id = jsonObj.id;
		
	$("#dialog_confirmation")
	.text(dictionary["message.action.destroy.systemvm"])
	.dialog('option', 'buttons', { 					
		"Confirm": function() { 			
			$(this).dialog("close");			
			var apiCommand = "command=destroySystemVm&id="+id;
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);	
		}, 
		"Cancel": function() { 
			$(this).dialog("close"); 
		}
	}).dialog("open");
}
