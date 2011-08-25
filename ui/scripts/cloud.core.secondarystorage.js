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

function secondaryStorageGetSearchParams() {
    var moreCriteria = [];	
   
    /*
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
	*/
    
	return moreCriteria.join("");          
}

function afterLoadSecondaryStorageJSP($midmenuItem1) {    
	initDialog("dialog_add_secondarystorage");   
    secondaryStorageRefreshDataBinding();    	
}

function secondaryStorageRefreshDataBinding() {      
    var $secondaryStorageNode = $selectedSubMenu; 
    bindAddSecondaryStorageButton($secondaryStorageNode.data("zoneObj"));  
}

function secondaryStorageToMidmenu(jsonObj, $midmenuItem1) {    
    $midmenuItem1.attr("id", getMidmenuId(jsonObj));  
    $midmenuItem1.data("jsonObj", jsonObj);      
    
    var $iconContainer = $midmenuItem1.find("#icon_container").show(); 
    $iconContainer.find("#icon").attr("src", "images/midmenuicon_host.png");   
          
	var firstRowText = fromdb(jsonObj.name);
    $midmenuItem1.find("#first_row").text(firstRowText.substring(0,midMenuFirstRowLength));     
    $midmenuItem1.find("#first_row_container").attr("title", firstRowText);   
    
    var secondRowText = fromdb(jsonObj.id).toString();
    $midmenuItem1.find("#second_row").text(secondRowText.substring(0,midMenuSecondRowLength));
    $midmenuItem1.find("#second_row_container").attr("title", secondRowText); 
}

function secondaryStorageToRightPanel($midmenuItem1) {	
    copyActionInfoFromMidMenuToRightPanel($midmenuItem1);  
    $("#right_panel_content").data("$midmenuItem1", $midmenuItem1);        
    secondaryStorageToDetailsTab();     
}

function secondaryStorageToDetailsTab() {	    
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
    if($midmenuItem1 == null) {
        secondaryStorageClearDetailsTab();      
        return;
    }
    
    var jsonObj = $midmenuItem1.data("jsonObj");
    if(jsonObj == null) {
    	secondaryStorageClearDetailsTab();  
        return;    
    }
       
    var $thisTab = $("#right_panel_content").find("#tab_content_details");  
    $thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();                   
         
    $.ajax({
        data: createURL("command=listHosts&id="+jsonObj.id),
        dataType: "json",
        async: false,
        success: function(json) {              
            var items = json.listhostsresponse.host;		
			if(items != null && items.length > 0) {
                jsonObj = items[0];
                $midmenuItem1.data("jsonObj", jsonObj);                  
            }
        }
    });    
    
    if(jsonObj == null) {
        secondaryStorageClearRightPanel();  
        $thisTab.find("#tab_spinning_wheel").hide();    
        $thisTab.find("#tab_container").show();      
        return;
    }
    
    $thisTab.find("#id").text(fromdb(jsonObj.id));
    $thisTab.find("#grid_header_title").text(fromdb(jsonObj.name));    
    $thisTab.find("#name").text(fromdb(jsonObj.name));
   
    $thisTab.find("#zonename").text(fromdb(jsonObj.zonename));	
	$thisTab.find("#type").text(jsonObj.type);	
    $thisTab.find("#ipaddress").text(jsonObj.ipaddress);
       
    //setHostStateInRightPanel(fromdb(jsonObj.state), $thisTab.find("#state"))
        
    setDateField(jsonObj.disconnected, $thisTab.find("#disconnected"));
       
    //actions ***   
    var $actionLink = $thisTab.find("#action_link"); 
    bindActionLink($actionLink);
        
    var $actionMenu = $actionLink.find("#action_menu");
    $actionMenu.find("#action_list").empty();          
    buildActionLinkForTab("label.action.delete.secondary.storage", secondaryStorageActionMap, $actionMenu, $midmenuItem1, $thisTab);   
    
    $thisTab.find("#tab_spinning_wheel").hide();    
    $thisTab.find("#tab_container").show();      
}	  

function secondaryStorageClearRightPanel() {
    secondaryStorageClearDetailsTab();      
}

function secondaryStorageClearDetailsTab() {	    
    var $thisTab = $("#right_panel_content").find("#tab_content_details");          
    $thisTab.find("#id").text("");
    $thisTab.find("#grid_header_title").text("");    
    $thisTab.find("#name").text("");   
    $thisTab.find("#zonename").text("");	
	$thisTab.find("#type").text("");	
    $thisTab.find("#ipaddress").text("");
    $thisTab.find("#state").text("");  
    $thisTab.find("#version").text("");    
    $thisTab.find("#disconnected").text("");        
        
    //actions ***   
    var $actionMenu = $thisTab.find("#action_link #action_menu");
    $actionMenu.find("#action_list").empty();   
	$actionMenu.find("#action_list").append($("#no_available_actions").clone().show());		
}	

var secondaryStorageActionMap = {
    "label.action.delete.secondary.storage": {   
        isAsyncJob: false,   
        dialogBeforeActionFn: doDeleteSecondaryStorage,       
        inProcessText: "label.action.delete.secondary.storage.processing",
        afterActionSeccessFn: function(json, $midmenuItem1, id) {  
			$midmenuItem1.remove();		                   
	        if(id.toString() == $("#right_panel_content").find("#tab_content_details").find("#id").text()) {
	            clearRightPanel();
	            secondaryStorageClearRightPanel();
	        }		    
        }
    } 
}

function doDeleteSecondaryStorage($actionLink, $detailsTab, $midmenuItem1) {     
    var jsonObj = $midmenuItem1.data("jsonObj");    
       
    $("#dialog_confirmation")	
    .text(dictionary["message.action.delete.secondary.storage"])
	.dialog('option', 'buttons', { 						
		"Confirm": function() { 
		    var $thisDialog = $(this);	
			$thisDialog.dialog("close");       	                                             
         
            var id = jsonObj.id;
            var apiCommand = "command=deleteHost&id="+id;  
    	    doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);						
		}, 
		"Cancel": function() { 
			$(this).dialog("close"); 
		} 
	}).dialog("open");
}

function bindAddSecondaryStorageButton(zoneObj) {        
   $("#add_secondarystorage_button").unbind("click").bind("click", function(event) {   
       $("#dialog_add_secondarystorage").find("#zone_name").text(fromdb(zoneObj.name));   
       $("#dialog_add_secondarystorage").find("#info_container").hide();		    
  
       $("#dialog_add_secondarystorage")
	    .dialog('option', 'buttons', { 				    
		    "Add": function() { 
		        var $thisDialog = $(this);	
	            $thisDialog.find("#info_container").hide(); 
	            
			    // validate values					
			    var isValid = true;							    
			    isValid &= validateString("NFS Server", $thisDialog.find("#nfs_server"), $thisDialog.find("#nfs_server_errormsg"));	
			    isValid &= validatePath("Path", $thisDialog.find("#path"), $thisDialog.find("#path_errormsg"));					
			    if (!isValid) 
			        return;
			    
				$thisDialog.find("#spinning_wheel").show();
								     					  								            				
			    var zoneId = zoneObj.id;		
			    var nfs_server = trim($thisDialog.find("#nfs_server").val());		
			    var path = trim($thisDialog.find("#path").val());	    					    				    					   					
				var url = nfsURL(nfs_server, path);  
			    				  
			    $.ajax({
				    data: createURL("command=addSecondaryStorage&zoneId="+zoneId+"&url="+todb(url)),
				    dataType: "json",
				    success: function(json) {	
				        $thisDialog.find("#spinning_wheel").hide();				        
				        $thisDialog.dialog("close");										    
				        $("#zone_"+zoneId+"_secondarystorage").click();				        					    
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