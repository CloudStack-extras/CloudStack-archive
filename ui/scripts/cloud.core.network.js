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

function networkGetSearchParams() {
    var moreCriteria = [];	
    
	var searchInput = $("#basic_search").find("#search_input").val();	 
    if (searchInput != null && searchInput.length > 0) {	           
        moreCriteria.push("&keyword="+todb(searchInput));	       
    }     

	var $advancedSearchPopup = getAdvancedSearchPopupInSearchContainer();
	if ($advancedSearchPopup.length > 0 && $advancedSearchPopup.css("display") != "none" ) {			
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

var zoneObj;  
function afterLoadNetworkJSP($leftmenuItem1) {      
    var $topButtonContainer = clearButtonsOnTop();			    	       
	$("#top_buttons").appendTo($("#top_button_container").empty()); 
        
    //switch between different tabs - Public Network page
    var $publicNetworkPage = $("#public_network_page");
    var tabArray = [$publicNetworkPage.find("#tab_details"), $publicNetworkPage.find("#tab_ipallocation"), $publicNetworkPage.find("#tab_firewall"), $publicNetworkPage.find("#tab_loadbalancer")];
    var tabContentArray = [$publicNetworkPage.find("#tab_content_details"), $publicNetworkPage.find("#tab_content_ipallocation"), $publicNetworkPage.find("#tab_content_firewall"), $publicNetworkPage.find("#tab_content_loadbalancer")];
    var afterSwitchFnArray = [publicNetworkJsonToDetailsTab, publicNetworkJsonToIpAllocationTab, publicNetworkJsonToFirewallTab, publicNetworkJsonToLoadBalancerTab];
    switchBetweenDifferentTabs(tabArray, tabContentArray, afterSwitchFnArray);  
    
    //switch between different tabs - Direct Network page
    var $directNetworkPage = $("#direct_network_page");
    var tabArray = [$directNetworkPage.find("#tab_details"), $directNetworkPage.find("#tab_ipallocation")];
    var tabContentArray = [$directNetworkPage.find("#tab_content_details"), $directNetworkPage.find("#tab_content_ipallocation")];
    var afterSwitchFnArray = [directNetworkJsonToDetailsTab, directNetworkJsonToIpAllocationTab];
    switchBetweenDifferentTabs(tabArray, tabContentArray, afterSwitchFnArray);    
    
    //initialize dialog
    initDialog("dialog_add_iprange_to_publicnetwork");        
    initDialog("dialog_add_external_firewall");        
    initDialog("dialog_add_load_balancer");        
    initDialog("dialog_add_network_for_zone");
    initDialog("dialog_add_iprange_to_directnetwork");    
     
    networkPopulateMiddleMenu($leftmenuItem1);  
    bindAddNetworkButton();         
}

function networkPopulateMiddleMenu($leftmenuItem1) {
    zoneObj = $leftmenuItem1.data("jsonObj");    
    if(zoneObj == null) 
	    return;	  
         
    //populate items into middle menu  
    var $midmenuContainer = $("#midmenu_container").empty();   
    var showPublicNetwork = true;
    
    if(zoneObj.networktype == "Basic") {   
    	$("#add_network_button").hide();	
    	$.ajax({
            data: createURL("command=listExternalFirewalls&zoneid="+zoneObj.id),
            dataType: "json",
            async: false,
            success: function(json) {            
                var items = json.listexternalfirewallsresponse.externalfirewall;    		   
    		    if(items != null && items.length > 0) {
    		    	showPublicNetwork = true;
    		    	$("#add_iprange_button,#tab_ipallocation").show();
    		    }
    		    else {
    		    	showPublicNetwork = false;  
    		    	$("#add_iprange_button,#tab_ipallocation").hide();	    		    	  
    		    }
            }
        });       	
    }
    else { // "Advanced"  
    	showPublicNetwork = true;
    	$("#add_network_button,#add_iprange_button,#tab_ipallocation").show();	
        listMidMenuItems2(("listNetworks&type=Direct&zoneId="+zoneObj.id), networkGetSearchParams, "listnetworksresponse", "network", directNetworkToMidmenu, directNetworkToRightPanel, directNetworkGetMidmenuId, false, 1);
    }
    
	if(showPublicNetwork == true && zoneObj.securitygroupsenabled == false) { //public network           
	    $midmenuContainer.find("#midmenu_container_no_items_available").remove();  //There is always at least one item (i.e. public network) in middle menu. So, "no items available" shouldn't be in middle menu even there is zero direct network item in middle menu.   
	    $.ajax({
	        data: createURL("command=listNetworks&trafficType=Public&isSystem=true&zoneId="+zoneObj.id),
	        dataType: "json",
	        async: false,
	        success: function(json) {       
	            var items = json.listnetworksresponse.network;       
	            if(items != null && items.length > 0) {
	                var item = items[0];
	                var $midmenuItem1 = $("#midmenu_item").clone();                      
	                $midmenuItem1.data("toRightPanelFn", publicNetworkToRightPanel);                             
	                publicNetworkToMidmenu(item, $midmenuItem1);    
	                bindClickToMidMenu($midmenuItem1, publicNetworkToRightPanel, publicNetworkGetMidmenuId);   
	                $midmenuContainer.prepend($midmenuItem1.show());    //prepend public network on the top of middle menu
	                $midmenuItem1.click();  
	            }
	        }
	    });  
	}
        else if (showPublicNetwork == true && zoneObj.securitygroupsenabled == true){
                 $midmenuContainer.find("#midmenu_container_no_items_available").remove();  //There is always at least one item (i.e. public network) in middle menu. So, "no items available" shouldn't be in middle menu even there is zero direct network item in middle menu.   
	    $.ajax({
	        data: createURL("command=listNetworks&type=Direct&trafficType=Guest&isSystem=true&zoneId="+zoneObj.id),
	        dataType: "json",
	        async: false,
	        success: function(json) {       
	            var items = json.listnetworksresponse.network;       
	            if(items != null && items.length > 0) {
	                var item = items[0];
	                var $midmenuItem1 = $("#midmenu_item").clone();                      
	                $midmenuItem1.data("toRightPanelFn", publicNetworkToRightPanel);                             
	                publicNetworkToMidmenu(item, $midmenuItem1);    
	                bindClickToMidMenu($midmenuItem1, publicNetworkToRightPanel, publicNetworkGetMidmenuId);   
	                $midmenuContainer.prepend($midmenuItem1.show());    //prepend public network on the top of middle menu
	                $midmenuItem1.click();  
	            }
	        }
	    });  
        }
	else {
		publicNetworkToRightPanel(null);	
	}




}

//***** Public Network (begin) ******************************************************************************************************
function publicNetworkGetMidmenuId(jsonObj) {    
    return "midmenuItem_publicnetework_" + jsonObj.id;
}

function publicNetworkToMidmenu(jsonObj, $midmenuItem1) {  
    $midmenuItem1.attr("id", publicNetworkGetMidmenuId(jsonObj)); 
    $midmenuItem1.data("jsonObj", jsonObj); 
    
    /*
    var $iconContainer = $midmenuItem1.find("#icon_container").show();   
    $iconContainer.find("#icon").attr("src", "images/midmenuicon_storage_snapshots.png");		
    */
    
    var firstRowText = "Public Network";
    $midmenuItem1.find("#first_row").text(firstRowText.substring(0,midMenuFirstRowLength));     
    $midmenuItem1.find("#first_row_container").attr("title", firstRowText);   
    
    var secondRowText = "VLAN: Multiple";
    $midmenuItem1.find("#second_row").text(secondRowText.substring(0,midMenuSecondRowLength));
    $midmenuItem1.find("#second_row_container").attr("title", secondRowText); 
}

function publicNetworkToRightPanel($midmenuItem1) {     
	$("#public_network_page").show(); 
	$("#direct_network_page").hide();
	
	if($midmenuItem1 == null) {
		$("#public_network_page").find("#tab_details").hide();
		$("#public_network_page").find("#tab_firewall").click();  
    }          
	else {
		$("#public_network_page").find("#tab_details").show();
		copyActionInfoFromMidMenuToRightPanel($midmenuItem1);  
	    $("#right_panel_content").data("$midmenuItem1", $midmenuItem1);  
	    $("#public_network_page").find("#tab_details").click();   
	}
       
    bindAddIpRangeToPublicNetworkButton();
    bindAddExternalFirewallButton();
    bindAddLoadBalancerButton();
}
	
function publicNetworkJsonToDetailsTab() {	 
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
    if($midmenuItem1 == null)
        return;
    
    var jsonObj = $midmenuItem1.data("jsonObj");    
    if(jsonObj == null) 
	    return;	  
	
	var $thisTab = $("#right_panel_content #public_network_page #tab_content_details");      
    $thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();   
		
	$.ajax({
        data: createURL("command=listNetworks&trafficType=Public&isSystem=true&id="+jsonObj.id),
        dataType: "json",
        async: false,
        success: function(json) {       
            var items = json.listnetworksresponse.network;             
            if(items != null && items.length > 0) {
                jsonObj = items[0];  
                $midmenuItem1.data("jsonObj", jsonObj);
            }
        }
    });           	
		
	$thisTab.find("#grid_header_title").text(fromdb(jsonObj.networkofferingdisplaytext));			
	$thisTab.find("#id").text(fromdb(jsonObj.id));		
	$thisTab.find("#state").text(fromdb(jsonObj.state));		
	$thisTab.find("#traffictype").text(fromdb(jsonObj.traffictype));	
	$thisTab.find("#broadcastdomaintype").text(fromdb(jsonObj.broadcastdomaintype));	
	setBooleanReadField(jsonObj.isshared, $thisTab.find("#isshared"));
	setBooleanReadField(jsonObj.issystem, $thisTab.find("#issystem"));
	$thisTab.find("#networkofferingname").text(fromdb(jsonObj.networkofferingname));	
	$thisTab.find("#networkofferingdisplaytext").text(fromdb(jsonObj.networkofferingdisplaytext));	
	$thisTab.find("#networkofferingid").text(fromdb(jsonObj.networkofferingid));	
	$thisTab.find("#related").text(fromdb(jsonObj.related));	
	$thisTab.find("#zoneid").text(fromdb(jsonObj.zoneid));	
	$thisTab.find("#dns1").text(fromdb(jsonObj.dns1));	
	$thisTab.find("#dns2").text(fromdb(jsonObj.dns2));	
	$thisTab.find("#domainid").text(fromdb(jsonObj.domainid));	
	$thisTab.find("#account").text(fromdb(jsonObj.account));	
	
	//actions ***   
    var $actionLink = $thisTab.find("#action_link"); 
    bindActionLink($actionLink);
             
    var $actionMenu = $thisTab.find("#action_link #action_menu");
    $actionMenu.find("#action_list").empty();   
	$actionMenu.find("#action_list").append($("#no_available_actions").clone().show());	   
			        
    $thisTab.find("#tab_container").show(); 
    $thisTab.find("#tab_spinning_wheel").hide();   
}

function publicNetworkJsonToIpAllocationTab() {  
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
    if($midmenuItem1 == null)
        return;
    
    var jsonObj = $midmenuItem1.data("jsonObj");    
    if(jsonObj == null) 
	    return;	  
	    
	var $thisTab = $("#right_panel_content #public_network_page #tab_content_ipallocation");      
    $thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();   
         
    $.ajax({
                data: createURL("command=listVlanIpRanges&zoneid="+ jsonObj.zoneid+"&networkId=" + jsonObj.id),     
		dataType: "json",		
		success: function(json) {		    
		    var items = json.listvlaniprangesresponse.vlaniprange;		    
		    var $container = $thisTab.find("#tab_container").empty();
		    var $template = $("#public_iprange_template");
		    if(items != null && items.length > 0) {		        
		        for(var i=0; i<items.length; i++) {
		            var $newTemplate = $template.clone();
		            publicNetworkIprangeJsonToTemplate(items[i], $newTemplate);
		            $container.append($newTemplate.show());
		        }
		    }		    
		    $thisTab.find("#tab_container").show(); 
            $thisTab.find("#tab_spinning_wheel").hide();    
		}
    });  
}

function publicNetworkIprangeJsonToTemplate(jsonObj, $template) {    
    $template.data("jsonObj", jsonObj);
    $template.attr("id", "publicNetworkIprange_" + jsonObj.id);
    
    var ipRange = getIpRange(fromdb(jsonObj.startip), fromdb(jsonObj.endip));
    $template.find("#grid_header_title").text(ipRange);
    
    $template.find("#id").text(jsonObj.id);
    $template.find("#vlan").text(jsonObj.vlan);    
    $template.find("#gateway").text(jsonObj.gateway);
    $template.find("#netmask").text(jsonObj.netmask);    
    $template.find("#iprange").text(ipRange);     
    $template.find("#domain").text(jsonObj.domain);
    $template.find("#account").text(jsonObj.account);
   
    var $actionLink = $template.find("#action_link");	
    bindActionLink($actionLink);
    	
	var $actionMenu = $actionLink.find("#action_menu");
    $actionMenu.find("#action_list").empty();	
       
    buildActionLinkForSubgridItem("label.action.delete.IP.range", publicNetworkIpRangeActionMap, $actionMenu, $template);	
}

function publicNetworkJsonToFirewallTab() {  
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
    if($midmenuItem1 == null)
        return;
    
    var jsonObj = $midmenuItem1.data("jsonObj");    
    if(jsonObj == null) 
	    return;	  
	    
	var $thisTab = $("#right_panel_content #public_network_page #tab_content_firewall");      
    $thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();   
        
    $.ajax({
        data: createURL("command=listExternalFirewalls&zoneid="+zoneObj.id),
        dataType: "json",
        success: function(json) {            
            var items = json.listexternalfirewallsresponse.externalfirewall;
		    var $container = $thisTab.find("#tab_container").empty();
		    var $template = $("#externalfirewall_template");
		    if(items != null && items.length > 0) {		        
		        for(var i=0; i<items.length; i++) {
		            var $newTemplate = $template.clone();
		            publicNetworkFirewallJsonToTemplate(items[i], $newTemplate);
		            $container.append($newTemplate.show());
		        }
		    }		    
		    $thisTab.find("#tab_container").show(); 
            $thisTab.find("#tab_spinning_wheel").hide();   
        }
    });       
}

/*
function publicNetworkParseUrl(url, $template) {
    if(url == null) 
        return;
    
    var array1 = url.split("?");
    if(array1.length >= 2) {
        var $subTemplate = $("#grid_row_template");  
                 
        var ip = array1[0];    
        $template.find("#grid_header_title").text(ip);            
        var $newSubTemplate = $subTemplate.clone();              
        $newSubTemplate.attr("id", ("grid_row_template_ip")).addClass("even");
        $newSubTemplate.find("#label").text("IP: ");
        $newSubTemplate.find("#value").text(ip);                        
        $template.append($newSubTemplate.show());
                   
        var parameters = array1[1];
        if(parameters != null) {
            var array2 = parameters.split("&");                    
            for(var i=0; i < array2.length; i++) {                
                var array3 = array2[i].split("=");     
                if(array3.length >= 2) {
                    var $newSubTemplate = $subTemplate.clone();              
                    $newSubTemplate.attr("id", ("grid_row_template_"+i));
                    if(i%2 == 0)
                        $newSubTemplate.addClass("odd");
                    else
                        $newSubTemplate.addClass("even");
                    $newSubTemplate.find("#label").text(array3[0] + ": ");
                    $newSubTemplate.find("#value").text(array3[1]);                        
                    $template.append($newSubTemplate.show());
                }               
            }                         
        }
    }    
}
*/

function publicNetworkFirewallJsonToTemplate(jsonObj, $template) {    
    $template.data("jsonObj", jsonObj);
    $template.attr("id", "publicNetworkFirewall_" + jsonObj.id);  
    $template.find("#id").text(fromdb(jsonObj.id));          
    //publicNetworkParseUrl(jsonObj.url, $template);    
    $template.find("#ip").text(fromdb(jsonObj.ipaddress));        
    $template.find("#username").text(fromdb(jsonObj.username));      
    $template.find("#publicinterface").text(fromdb(jsonObj.publicinterface));      
    $template.find("#privateinterface").text(fromdb(jsonObj.privateinterface));      
    $template.find("#usageinterface").text(fromdb(jsonObj.usageinterface));      
    $template.find("#publiczone").text(fromdb(jsonObj.publiczone));      
    $template.find("#privatezone").text(fromdb(jsonObj.privatezone));         
    $template.find("#numretries").text(fromdb(jsonObj.numretries));
    $template.find("#timeout").text(fromdb(jsonObj.timeout));
          
    var $actionLink = $template.find("#action_link");	
    bindActionLink($actionLink);
   
	var $actionMenu = $actionLink.find("#action_menu");
    $actionMenu.find("#action_list").empty();	
       
    buildActionLinkForSubgridItem("label.action.delete.firewall", publicNetworkFirewallActionMap, $actionMenu, $template);	
}

var publicNetworkFirewallActionMap = {     
    "label.action.delete.firewall": {             
        isAsyncJob: false,    
        dialogBeforeActionFn : doDeleteExternalFirewall,        
        inProcessText: "label.action.delete.firewall.processing",
        afterActionSeccessFn: function(json, id, $subgridItem) {                 
            $subgridItem.slideUp("slow", function() {
                $(this).remove();
            });
            networkPopulateMiddleMenu($selectedSubMenu); //refresh middle menu (check if public network should be removed) and top buttons(check if Add IP Range button should be hidden)
        }
    }     
}  

function doDeleteExternalFirewall($actionLink, $subgridItem) {   
    var jsonObj = $subgridItem.data("jsonObj");
	var id = jsonObj.id;
		
	$("#dialog_confirmation")
	.text(dictionary["message.action.delete.external.firewall"])
	.dialog('option', 'buttons', { 					
		"Confirm": function() { 			
			$(this).dialog("close");					
			var apiCommand = "command=deleteExternalFirewall&id="+id;            
            doActionToSubgridItem(id, $actionLink, apiCommand, $subgridItem);		
		}, 
		"Cancel": function() { 
			$(this).dialog("close"); 
		}
	}).dialog("open");
}

function publicNetworkJsonToLoadBalancerTab() {  
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
    if($midmenuItem1 == null)
        return;
    
    var jsonObj = $midmenuItem1.data("jsonObj");    
    if(jsonObj == null) 
	    return;	  
	    
	var $thisTab = $("#right_panel_content #public_network_page #tab_content_loadbalancer");      
    $thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();   
        
    $.ajax({
        data: createURL("command=listExternalLoadBalancers&zoneid="+zoneObj.id),
        dataType: "json",
        success: function(json) {                 
            var items = json.listexternalloadbalancersresponse.externalloadbalancer;
		    var $container = $thisTab.find("#tab_container").empty();
		    var $template = $("#loadbalancer_template");
		    if(items != null && items.length > 0) {		        
		        for(var i=0; i<items.length; i++) {
		            var $newTemplate = $template.clone();
		            publicNetworkLoadBalancerJsonToTemplate(items[i], $newTemplate);
		            $container.append($newTemplate.show());
		        }
		    }		    
		    $thisTab.find("#tab_container").show(); 
            $thisTab.find("#tab_spinning_wheel").hide();   
        }
    });       
}

function publicNetworkLoadBalancerJsonToTemplate(jsonObj, $template) {    
    $template.data("jsonObj", jsonObj);
    $template.attr("id", "publicNetworkLoadBalancer_" + jsonObj.id);
        
    $template.find("#grid_header_title").text(fromdb(jsonObj.url));    
    $template.find("#id").text(fromdb(jsonObj.id));   
    //publicNetworkParseUrl(jsonObj.url, $template);      
    $template.find("#ip").text(fromdb(jsonObj.ipaddress));        
    $template.find("#username").text(fromdb(jsonObj.username));      
    $template.find("#publicinterface").text(fromdb(jsonObj.publicinterface));      
    $template.find("#privateinterface").text(fromdb(jsonObj.privateinterface));      
    $template.find("#numretries").text(fromdb(jsonObj.numretries));
      
    var $actionLink = $template.find("#action_link");	
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
    buildActionLinkForSubgridItem("label.action.delete.load.balancer", publicNetworkLoadBalancerActionMap, $actionMenu, $template);	
}

var publicNetworkLoadBalancerActionMap = {     
    "label.action.delete.load.balancer": {   
        isAsyncJob: false,   
        dialogBeforeActionFn: doDeleteExternalLoadBalancer, 
        inProcessText: "label.action.delete.load.balancer.processing",
        afterActionSeccessFn: function(json, id, $subgridItem) {                 
            $subgridItem.slideUp("slow", function() {
                $(this).remove();
            });
        }
    }     
}  

function doDeleteExternalLoadBalancer($actionLink, $subgridItem) {   
    var jsonObj = $subgridItem.data("jsonObj");
	var id = jsonObj.id;
		
	$("#dialog_confirmation")
	.text(dictionary["message.action.delete.external.load.balancer"])
	.dialog('option', 'buttons', { 					
		"Confirm": function() { 			
			$(this).dialog("close");					
			var apiCommand = "command=deleteExternalLoadBalancer&id="+id;            
            doActionToSubgridItem(id, $actionLink, apiCommand, $subgridItem);		
		}, 
		"Cancel": function() { 
			$(this).dialog("close"); 
		}
	}).dialog("open");
}

function bindAddIpRangeToPublicNetworkButton() {   
    //***** binding Event Handler (begin) ******  		
    var $dialogAddIpRangeToPublicNetwork = $("#dialog_add_iprange_to_publicnetwork"); 
    $dialogAddIpRangeToPublicNetwork.find("#add_publicip_vlan_tagged").change(function(event) {	
		if ($(this).val() == "tagged") {
			$dialogAddIpRangeToPublicNetwork.find("#add_publicip_vlan_vlan_container").show();				
			$dialogAddIpRangeToPublicNetwork.find("#add_publicip_vlan_scope").empty().append('<option value="zone-wide">zone-wide</option>').append('<option value="account-specific">account-specific</option>');
		} 
		else if($(this).val() == "untagged") {  
			$dialogAddIpRangeToPublicNetwork.find("#add_publicip_vlan_vlan_container").hide();
			$dialogAddIpRangeToPublicNetwork.find("#add_publicip_vlan_scope").empty().append('<option value="zone-wide">zone-wide</option>');				
		}			
		
		// default value of "#add_publicip_vlan_scope" is "zone-wide". Calling change() will hide "#domain_container", "#add_publicip_vlan_account_container". 
		$dialogAddIpRangeToPublicNetwork.find("#add_publicip_vlan_scope").change(); 	
		
		return false;
	});    
   
	$dialogAddIpRangeToPublicNetwork.find("#add_publicip_vlan_scope").change(function(event) {	   
	    if($(this).val() == "zone-wide") {
	        $dialogAddIpRangeToPublicNetwork.find("#domain_container").hide();
			$dialogAddIpRangeToPublicNetwork.find("#add_publicip_vlan_account_container").hide();    
	    } 
	    else if($(this).val() == "account-specific") { 
	        $dialogAddIpRangeToPublicNetwork.find("#domain_container").show();
			$dialogAddIpRangeToPublicNetwork.find("#add_publicip_vlan_account_container").show();    
	    }		    
	    return false;
	});
	//***** binding Event Handler (end) ******   
	
	$("#add_iprange_button").unbind("click").bind("click", function(event) {  
        if($("#public_network_page").find("#tab_content_ipallocation").css("display") == "none")         
            $("#public_network_page").find("#tab_ipallocation").click();
       
		$dialogAddIpRangeToPublicNetwork.find("#add_publicip_vlan_tagged").change();            		
	    $dialogAddIpRangeToPublicNetwork.find("#add_publicip_vlan_scope").change(); // default value of "#add_publicip_vlan_scope" is "zone-wide". Calling change() will hide "#domain_container", "#add_publicip_vlan_account_container". 	
       		
		if(zoneObj.domainid != null) { //list only domains under zoneObj.domainid
		    applyAutoCompleteToDomainChildrenField($dialogAddIpRangeToPublicNetwork.find("#domain"), zoneObj.domainid);		
        }
        else { //list all domains            
             applyAutoCompleteToDomainField($dialogAddIpRangeToPublicNetwork.find("#domain"));            
        }   
		
		$dialogAddIpRangeToPublicNetwork
		.dialog('option', 'buttons', { 	
			"Add": function() { 	
			    var $thisDialog = $(this);		
			    					
				// validate values
				var isValid = true;					
				var isTagged = $thisDialog.find("#add_publicip_vlan_tagged").val() == "tagged";
				
				
				isValid &= validateString("Account", $thisDialog.find("#add_publicip_vlan_account"), $thisDialog.find("#add_publicip_vlan_account_errormsg"), true); //optional
				
				if (isTagged) {
					isValid &= validateNumber("VLAN", $thisDialog.find("#add_publicip_vlan_vlan"), $thisDialog.find("#add_publicip_vlan_vlan_errormsg"), 1, 4095);
				}
				
				isValid &= validateIp("Gateway", $thisDialog.find("#add_publicip_vlan_gateway"), $thisDialog.find("#add_publicip_vlan_gateway_errormsg"), false); //required
				isValid &= validateIp("Netmask", $thisDialog.find("#add_publicip_vlan_netmask"), $thisDialog.find("#add_publicip_vlan_netmask_errormsg"), false); //required
				isValid &= validateIp("Start IP Range", $thisDialog.find("#add_publicip_vlan_startip"), $thisDialog.find("#add_publicip_vlan_startip_errormsg"), false); //required
				isValid &= validateIp("End IP Range", $thisDialog.find("#add_publicip_vlan_endip"), $thisDialog.find("#add_publicip_vlan_endip_errormsg"), true); //optional
				
				if($thisDialog.find("#domain_container").css("display") != "none") {
				    isValid &= validateString("Domain", $thisDialog.find("#domain"), $thisDialog.find("#domain_errormsg"), false);                             //required	
				    var domainName = $thisDialog.find("#domain").val();
				    var domainId;
				    if(domainName != null && domainName.length > 0) { 		
				        var items;
				        if(zoneObj.domainid != null)
				            items = autoCompleteDomains;
				        else
				            items = autoCompleteDomains;
				    		    
				        if(items != null && items.length > 0) {									
					        for(var i=0; i < items.length; i++) {					        
					          if(fromdb(items[i].name).toLowerCase() == domainName.toLowerCase()) {
					              domainId = items[i].id;
					              break;	
					          }
				            } 					   			    
				        }					    				    
				        if(domainId == null) {
				            showError(false, $thisDialog.find("#domain"), $thisDialog.find("#domain_errormsg"), g_dictionary["label.not.found"]);
				            isValid &= false;
				        }				    
				    }	
				}			
				
				if (!isValid) 
				    return;		
				 
				var isDirect = false;
				    						
				$thisDialog.find("#spinning_wheel").show()
				
				var vlan = trim($thisDialog.find("#add_publicip_vlan_vlan").val());
				if (isTagged) {
					vlan = "&vlan="+vlan;
				} else {
					vlan = "&vlan=untagged";
				}
								
				var scopeParams = "";
				if($thisDialog.find("#domain_container").css("display") != "none") {
				    scopeParams = "&domainId="+domainId+"&account="+trim($thisDialog.find("#add_publicip_vlan_account").val());  
				} else if (isDirect) {
					scopeParams = "&isshared=true";
				}
				
				var array1 = [];						
				var gateway = $thisDialog.find("#add_publicip_vlan_gateway").val();
				array1.push("&gateway="+todb(gateway));
				
				var netmask = $thisDialog.find("#add_publicip_vlan_netmask").val();
				array1.push("&netmask="+todb(netmask));
				
				var startip = $thisDialog.find("#add_publicip_vlan_startip").val();
				array1.push("&startip="+todb(startip));
				
				var endip = $thisDialog.find("#add_publicip_vlan_endip").val();	//optional field (might be empty)
				if(endip != null && endip.length > 0)
				    array1.push("&endip="+todb(endip));			
				
				//zoneObj.networktype == "Advanced", only advanced zone has option to Add IP Range (in network node)
				if(zoneObj.securitygroupsenabled == false)   
                    array1.push("&forVirtualNetwork=true");
				else
					array1.push("&forVirtualNetwork=false");
				
				// Add IP Range to public network
				$.ajax({
					data: createURL("command=createVlanIpRange&zoneId="+zoneObj.id+vlan+scopeParams+array1.join("")),
					dataType: "json",
					success: function(json) {	
						$thisDialog.find("#spinning_wheel").hide();
						$thisDialog.dialog("close");
					
					    var item = json.createvlaniprangeresponse.vlan;						    
					    var $newTemplate = $("#public_iprange_template").clone();
	                    publicNetworkIprangeJsonToTemplate(item, $newTemplate);
	                    $("#public_network_page").find("#tab_content_ipallocation").find("#tab_container").prepend($newTemplate.show());						   
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

function bindAddExternalFirewallButton() {         
    var $dialogAddExternalFirewall = $("#dialog_add_external_firewall"); 
         
    $("#add_external_firewall_button").show().unbind("click").bind("click", function(event) {         
        if($("#public_network_page").find("#tab_content_firewall").css("display") == "none")         
            $("#public_network_page").find("#tab_firewall").click();
                          
        $dialogAddExternalFirewall.find("#info_container").hide();
        $dialogAddExternalFirewall.find("#zone_name").text(fromdb(zoneObj.name));         
					
		$dialogAddExternalFirewall
		.dialog('option', 'buttons', { 	
			"Add": function() { 	
			    var $thisDialog = $(this);		
			    $thisDialog.find("#info_container").hide(); 
			    			
				// validate values
				var isValid = true;		
				isValid &= validateString("IP", $thisDialog.find("#ip"), $thisDialog.find("#ip_errormsg"), false); //required
				isValid &= validateString("User Name", $thisDialog.find("#username"), $thisDialog.find("#username_errormsg"), false); //required
				isValid &= validateString("Password", $thisDialog.find("#password"), $thisDialog.find("#password_errormsg"), false);  //required				
				isValid &= validateString("Public Interface", $thisDialog.find("#public_interface"), $thisDialog.find("#public_interface_errormsg"), true);  //optinal
				isValid &= validateString("Private Interface", $thisDialog.find("#private_interface"), $thisDialog.find("#private_interface_errormsg"), true);  //optinal				
				isValid &= validateString("Usage Interface", $thisDialog.find("#usage_interface"), $thisDialog.find("#usage_interface_errormsg"), true);  //optinal				
				isValid &= validateString("Public Zone", $thisDialog.find("#public_zone"), $thisDialog.find("#public_zone_errormsg"), true);  //optinal
				isValid &= validateString("Private Zone", $thisDialog.find("#private_zone"), $thisDialog.find("#private_zone_errormsg"), true);  //optinal				
				isValid &= validateInteger("Number of Retries", $thisDialog.find("#numretries"), $thisDialog.find("#numretries_errormsg"), null, null, true);  //optinal
				isValid &= validateInteger(" Timeout(seconds)", $thisDialog.find("#timeout"), $thisDialog.find("#timeout_errormsg"), null, null, true);  //optinal
				if (!isValid) 
				    return;		
				 			    						
				$thisDialog.find("#spinning_wheel").show()
				
				var array1 = [];
			
				array1.push("&zoneid=" + todb(zoneObj.id));
											
				var username = $thisDialog.find("#username").val();
				array1.push("&username=" + todb(username));
				
				var password = $thisDialog.find("#password").val();
				array1.push("&password=" + todb(password));
				
				//*** construct URL (begin)	***	
				var url = [];
				
				var ip = $thisDialog.find("#ip").val();
		        if(ip.indexOf("http://")==-1)
		            url.push("http://"+ip);		            
		        else
		            url.push(ip);		                   
				
				var isQuestionMarkAdded = false;
				
				var publicInterface = $thisDialog.find("#public_interface").val();
				if(publicInterface != null && publicInterface.length > 0) {
				    if(isQuestionMarkAdded == false) {
				        url.push("?");
				        isQuestionMarkAdded = true;
				    }
				    else {
				        url.push("&");
				    }  				    
				    url.push("publicInterface="+publicInterface); 
				}
				    
				var privateInterface = $thisDialog.find("#private_interface").val();
				if(privateInterface != null && privateInterface.length > 0) {
				    if(isQuestionMarkAdded == false) {
				        url.push("?");
				        isQuestionMarkAdded = true;
				    }
				    else {
				        url.push("&");
				    }  		
				    url.push("privateInterface="+privateInterface); 
				}
				 
				var usageInterface = $thisDialog.find("#usage_interface").val();
				if(usageInterface != null && usageInterface.length > 0) {
				    if(isQuestionMarkAdded == false) {
				        url.push("?");
				        isQuestionMarkAdded = true;
				    }
				    else {
				        url.push("&");
				    }  				    
				    url.push("usageInterface="+usageInterface); 
				} 
				    
				var publicZone = $thisDialog.find("#public_zone").val();
				if(publicZone != null && publicZone.length > 0) {
				    if(isQuestionMarkAdded == false) {
				        url.push("?");
				        isQuestionMarkAdded = true;
				    }
				    else {
				        url.push("&");
				    }  		
				    url.push("publicZone="+publicZone); 
				}
				
				var privateZone = $thisDialog.find("#private_zone").val();
				if(privateZone != null && privateZone.length > 0) {
				    if(isQuestionMarkAdded == false) {
				        url.push("?");
				        isQuestionMarkAdded = true;
				    }
				    else {
				        url.push("&");
				    }  		
				    url.push("privateZone="+privateZone); 	
				}			
				
				var numretries = $thisDialog.find("#numretries").val();
				if(numretries != null && numretries.length > 0) {
				    if(isQuestionMarkAdded == false) {
				        url.push("?");
				        isQuestionMarkAdded = true;
				    }
				    else {
				        url.push("&");
				    }  		
				    url.push("numretries="+numretries); 	
				}		
				
				var timeout = $thisDialog.find("#timeout").val();
				if(timeout != null && timeout.length > 0) {
				    if(isQuestionMarkAdded == false) {
				        url.push("?");
				        isQuestionMarkAdded = true;
				    }
				    else {
				        url.push("&");
				    }  		
				    url.push("timeout="+timeout); 	
				}		
				
				array1.push("&url="+todb(url.join("")));		
				//*** construct URL (end)	***					
										
				$.ajax({
					data: createURL("command=addExternalFirewall"+array1.join("")),
					dataType: "json",
					success: function(json) {	
						$thisDialog.find("#spinning_wheel").hide();
						$thisDialog.dialog("close");
					    					    
					    var item = json.addexternalfirewallresponse.externalfirewall;
					    var $newTemplate = $("#externalfirewall_template").clone();
		                publicNetworkFirewallJsonToTemplate(item, $newTemplate);
		                $("#right_panel_content #public_network_page #tab_content_firewall").find("#tab_container").append($newTemplate.show());
		                
		                networkPopulateMiddleMenu($selectedSubMenu); //refresh middle menu (add public network) and top buttons(show Add IP Range button)
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

var publicNetworkIpRangeActionMap = {     
    "label.action.delete.IP.range": {              
        api: "deleteVlanIpRange",     
        isAsyncJob: false,   
        inProcessText: "label.action.delete.IP.range.processing",
        afterActionSeccessFn: function(json, id, $subgridItem) {                 
            $subgridItem.slideUp("slow", function() {
                $(this).remove();
            });
        }
    }    
}  


function bindAddLoadBalancerButton() {         
    var $dialogAddLoadBalancer = $("#dialog_add_load_balancer"); 
              
    $("#add_load_balancer_button").show().unbind("click").bind("click", function(event) {         
        if($("#public_network_page").find("#tab_content_loadbalancer").css("display") == "none")         
            $("#public_network_page").find("#tab_loadbalancer").click();
                          
        $dialogAddLoadBalancer.find("#info_container").hide();
        $dialogAddLoadBalancer.find("#zone_name").text(fromdb(zoneObj.name));         
					
		$dialogAddLoadBalancer
		.dialog('option', 'buttons', { 	
			"Add": function() { 	
			    var $thisDialog = $(this);		
			    $thisDialog.find("#info_container").hide(); 
			    			
				// validate values
				var isValid = true;		
				isValid &= validateString("IP", $thisDialog.find("#ip"), $thisDialog.find("#ip_errormsg"), false); //required
				isValid &= validateString("User Name", $thisDialog.find("#username"), $thisDialog.find("#username_errormsg"), false); //required
				isValid &= validateString("Password", $thisDialog.find("#password"), $thisDialog.find("#password_errormsg"), false);  //required				
				isValid &= validateString("Public Interface", $thisDialog.find("#public_interface"), $thisDialog.find("#public_interface_errormsg"), true);  //optinal
				isValid &= validateString("Private Interface", $thisDialog.find("#private_interface"), $thisDialog.find("#private_interface_errormsg"), true);  //optinal
				isValid &= validateInteger("Number of Retries", $thisDialog.find("#numretries"), $thisDialog.find("#numretries_errormsg"), null, null, true);  //optinal
				if (!isValid) 
				    return;		
				 			    						
				$thisDialog.find("#spinning_wheel").show()
				
				var array1 = [];
			
				array1.push("&zoneid=" + todb(zoneObj.id));
											
				var username = $thisDialog.find("#username").val();
				array1.push("&username=" + todb(username));
				
				var password = $thisDialog.find("#password").val();
				array1.push("&password=" + todb(password));
				
				//*** construct URL (begin)	***	
				var url = [];
				
				var ip = $thisDialog.find("#ip").val();
		        if(ip.indexOf("http://")==-1)
		            url.push("http://"+ip);		            
		        else
		            url.push(ip);		                   
				
				var isQuestionMarkAdded = false;
				
				var publicInterface = $thisDialog.find("#public_interface").val();
				if(publicInterface != null && publicInterface.length > 0) {
				    if(isQuestionMarkAdded == false) {
				        url.push("?");
				        isQuestionMarkAdded = true;
				    }
				    else {
				        url.push("&");
				    }  				    
				    url.push("publicInterface="+publicInterface); 
				}
				    
				var privateInterface = $thisDialog.find("#private_interface").val();
				if(privateInterface != null && privateInterface.length > 0) {
				    if(isQuestionMarkAdded == false) {
				        url.push("?");
				        isQuestionMarkAdded = true;
				    }
				    else {
				        url.push("&");
				    }  		
				    url.push("privateInterface="+privateInterface); 
				}
				
				var numretries = $thisDialog.find("#numretries").val();
				if(numretries != null && numretries.length > 0) {
				    if(isQuestionMarkAdded == false) {
				        url.push("?");
				        isQuestionMarkAdded = true;
				    }
				    else {
				        url.push("&");
				    }  		
				    url.push("numretries="+numretries); 	
				}		
				  				
				array1.push("&url="+todb(url.join("")));		
				//*** construct URL (end)	***					
								
				$.ajax({
					data: createURL("command=addExternalLoadBalancer"+array1.join("")),
					dataType: "json",
					success: function(json) {	
						$thisDialog.find("#spinning_wheel").hide();
						$thisDialog.dialog("close");
					   	
					   	var item = json.addexternalloadbalancerresponse.externalloadbalancer;			    
					    var $newTemplate = $("#loadbalancer_template").clone();
		                publicNetworkLoadBalancerJsonToTemplate(item, $newTemplate);
		                $("#right_panel_content #public_network_page #tab_content_loadbalancer").find("#tab_container").append($newTemplate.show());
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

//***** Public Network (end) ******************************************************************************************************


//***** Direct Network (begin) ******************************************************************************************************
function directNetworkGetMidmenuId(jsonObj) {
    return "midmenuItem_directnetework_" + jsonObj.id;
}

function directNetworkToMidmenu(jsonObj, $midmenuItem1) {  
    $midmenuItem1.attr("id", directNetworkGetMidmenuId(jsonObj)); 
    $midmenuItem1.data("jsonObj", jsonObj); 
    
    /*
    var $iconContainer = $midmenuItem1.find("#icon_container").show();   
    $iconContainer.find("#icon").attr("src", "images/midmenuicon_storage_snapshots.png");		
    */
    
    var firstRowText = fromdb(jsonObj.name);
    $midmenuItem1.find("#first_row").text(firstRowText.substring(0,midMenuFirstRowLength));     
    $midmenuItem1.find("#first_row_container").attr("title", firstRowText);   
    
    var secondRowText = "VLAN : " + fromdb(jsonObj.vlan);
    $midmenuItem1.find("#second_row").text(secondRowText.substring(0,midMenuSecondRowLength));
    $midmenuItem1.find("#second_row_container").attr("title", secondRowText);  
}

function directNetworkToRightPanel($midmenuItem1) {    
    copyActionInfoFromMidMenuToRightPanel($midmenuItem1);  
    $("#right_panel_content").data("$midmenuItem1", $midmenuItem1);  
            
    $("#direct_network_page").show();
    bindAddIpRangeToDirectNetworkButton($midmenuItem1);
    $("#add_external_firewall_button").unbind("click").hide(); 
    $("#add_load_balancer_button").unbind("click").hide(); 
    
    $("#public_network_page").hide();
    
    $("#direct_network_page").find("#tab_details").click();     
}
	
function directNetworkClearRightPanel() {
    directNetworkJsonClearDetailsTab();
    directNetworkJsonClearIpAllocationTab();
}
	
function directNetworkJsonToDetailsTab() {	    
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
    if($midmenuItem1 == null)
        return;
    
    var jsonObj = $midmenuItem1.data("jsonObj");    
    if(jsonObj == null) 
	    return;	  
	
	var $thisTab = $("#right_panel_content #direct_network_page #tab_content_details");      
    $thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();   
			
	$.ajax({
        data: createURL("command=listNetworks&type=Direct&id="+jsonObj.id),
        dataType: "json",
        async: false,
        success: function(json) {       
            var items = json.listnetworksresponse.network;                     
            if(items != null && items.length > 0) {
                jsonObj = items[0];  
                $midmenuItem1.data("jsonObj", jsonObj);
            }
        }
    });  	
		
	$thisTab.find("#grid_header_title").text(fromdb(jsonObj.name));			
	$thisTab.find("#id").text(fromdb(jsonObj.id));				
	$thisTab.find("#name").text(fromdb(jsonObj.name));	
	$thisTab.find("#name_edit").val(fromdb(jsonObj.name));	
	$thisTab.find("#displaytext").text(fromdb(jsonObj.displaytext));
	$thisTab.find("#displaytext_edit").val(fromdb(jsonObj.displaytext));
	$thisTab.find("#default").text((jsonObj.isdefault) ? "Yes" : "No"); 	
    $thisTab.find("#vlan").text(fromdb(jsonObj.vlan));
    $thisTab.find("#gateway").text(fromdb(jsonObj.gateway));
    $thisTab.find("#netmask").text(fromdb(jsonObj.netmask));
    $thisTab.find("#networkdomain").text(fromdb(jsonObj.networkdomain));
	$thisTab.find("#networkdomain_edit").val(fromdb(jsonObj.networkdomain));       
    $thisTab.find("#tags").text(fromdb(jsonObj.tags));
	$thisTab.find("#tags_edit").val(fromdb(jsonObj.tags));    
    $thisTab.find("#domain").text(fromdb(jsonObj.domain));      //might be null
    $thisTab.find("#account").text(fromdb(jsonObj.account));    //might be null
        
    //actions ***   
    var $actionLink = $thisTab.find("#action_link"); 
    bindActionLink($actionLink);
        
    var $actionMenu = $actionLink.find("#action_menu");
    $actionMenu.find("#action_list").empty();   

    buildActionLinkForTab("label.action.edit.network", directNetworkActionMap, $actionMenu, $midmenuItem1, $thisTab);	   
    buildActionLinkForTab("label.action.delete.network", directNetworkActionMap, $actionMenu, $midmenuItem1, $thisTab);	      
        
    $thisTab.find("#tab_container").show(); 
    $thisTab.find("#tab_spinning_wheel").hide();   
}

function directNetworkJsonClearDetailsTab() {
    var $thisTab = $("#right_panel_content #direct_network_page #tab_content_details");    
	$thisTab.find("#grid_header_title").text("");			
	$thisTab.find("#id").text("");				
	$thisTab.find("#name").text("");	
	$thisTab.find("#name_edit").val("");
	$thisTab.find("#displaytext").text("");	 
	$thisTab.find("#displaytext_edit").val("");	 
	$thisTab.find("#default").text(""); 	 	
    $thisTab.find("#vlan").text("");
    $thisTab.find("#gateway").text("");
    $thisTab.find("#netmask").text("");  
    $thisTab.find("#networkdomain").text("");
	$thisTab.find("#networkdomain_edit").val("");      
    $thisTab.find("#tags").text("");	
	$thisTab.find("#tags_edit").val("");    
    $thisTab.find("#domain").text("");      
    $thisTab.find("#account").text("");       
       
    //actions ***  
    var $actionMenu = $thisTab.find("#action_link #action_menu");
    $actionMenu.find("#action_list").empty();   
	$actionMenu.find("#action_list").append($("#no_available_actions").clone().show());	   
}

function directNetworkJsonToIpAllocationTab() {	    
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
    if($midmenuItem1 == null)
        return;
    
    var jsonObj = $midmenuItem1.data("jsonObj");    
    if(jsonObj == null) 
	    return;	  
	    
	var $thisTab = $("#right_panel_content #direct_network_page #tab_content_ipallocation");      
    $thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();   
         
    $.ajax({
		data: createURL("command=listVlanIpRanges&zoneid="+ jsonObj.zoneid + "&networkid="+jsonObj.id),
		dataType: "json",		
		success: function(json) {
		    var items = json.listvlaniprangesresponse.vlaniprange;		    
		    var $container = $thisTab.find("#tab_container").empty();
		    var $template = $("#direct_iprange_template");
		    if(items != null && items.length > 0) {		        
		        for(var i=0; i<items.length; i++) {
		            var $newTemplate = $template.clone();
		            directNetworkIprangeJsonToTemplate(items[i], $newTemplate);
		            $container.append($newTemplate.show());
		        }
		    }		    
		    $thisTab.find("#tab_container").show(); 
            $thisTab.find("#tab_spinning_wheel").hide();    
		}
    });  
}

function directNetworkJsonClearIpAllocationTab() {
    var $thisTab = $("#right_panel_content #direct_network_page #tab_content_ipallocation"); 
    $thisTab.find("#tab_container").empty();     
}

function directNetworkIprangeJsonToTemplate(jsonObj, $template) {    
    $template.data("jsonObj", jsonObj);
    $template.attr("id", "directNetworkIprange_" + jsonObj.id);
    
    var ipRange = getIpRange(fromdb(jsonObj.startip), fromdb(jsonObj.endip));
    $template.find("#grid_header_title").text(ipRange);
    
    $template.find("#id").text(jsonObj.id)
    $template.find("#vlan").text(jsonObj.vlan);
    $template.find("#iprange").text(ipRange);
        
    var $actionLink = $template.find("#action_link");	
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
       
    buildActionLinkForSubgridItem("label.action.delete.IP.range", directNetworkIpRangeActionMap, $actionMenu, $template);
}

var directNetworkIpRangeActionMap = {     
    "label.action.delete.IP.range": {              
        api: "deleteVlanIpRange",     
        isAsyncJob: false,   
        inProcessText: "label.action.delete.IP.range.processing",
        afterActionSeccessFn: function(json, id, $subgridItem) {                 
            $subgridItem.slideUp("slow", function() {
                $(this).remove();
            });
        }
    }    
}  

function bindAddNetworkButton() {   
    if(zoneObj == null)
        return;
    
    var $dialogAddNetworkForZone = $("#dialog_add_network_for_zone"); 
           
    if(zoneObj.securitygroupsenabled)
        $dialogAddNetworkForZone.find("#add_publicip_vlan_scope").empty().append('<option value="account-specific">account-specific</option>');
	else		
        $dialogAddNetworkForZone.find("#add_publicip_vlan_scope").empty().append('<option value="zone-wide">zone-wide</option>')
			.append('<option value="domain-specific">domain-specific</option>')
			.append('<option value="account-specific">account-specific</option>');
         
	$dialogAddNetworkForZone.find("#add_publicip_vlan_scope").change(function(event) {		    
	    if($(this).val() == "zone-wide") {
	        $dialogAddNetworkForZone.find("#domain_container").hide();
			$dialogAddNetworkForZone.find("#add_publicip_vlan_account_container").hide();    
	    }
		else if($(this).val() == "domain-specific") { 
	        $dialogAddNetworkForZone.find("#domain_container").show();
			$dialogAddNetworkForZone.find("#add_publicip_vlan_account_container").hide();    
	    }
	    else if($(this).val() == "account-specific") { 
	        $dialogAddNetworkForZone.find("#domain_container").show();
			$dialogAddNetworkForZone.find("#add_publicip_vlan_account_container").show();    
	    }		    
	    return false;
	});
	    
    $("#add_network_button").unbind("click").bind("click", function(event) {   
	    $dialogAddNetworkForZone.find("#info_container").hide();
        $dialogAddNetworkForZone.find("#zone_name").text(fromdb(zoneObj.name));  
		$dialogAddNetworkForZone.find("#add_publicip_vlan_vlan, #add_publicip_vlan_gateway, #add_publicip_vlan_netmask, #add_publicip_vlan_startip, #add_publicip_vlan_endip, #domain, #add_publicip_vlan_account").val("");
		$dialogAddNetworkForZone.find("#add_publicip_vlan_scope").change();  	                    
        				
		if (zoneObj.networktype == 'Basic') {
			
		} 
		else {				
			if(zoneObj.domainid != null) { //list only domains under zoneObj.domainid
			    applyAutoCompleteToDomainChildrenField($dialogAddNetworkForZone.find("#domain"), zoneObj.domainid);				    
            }
            else { //list all domains     
                applyAutoCompleteToDomainField($dialogAddNetworkForZone.find("#domain"));               
            }   
		}

		$dialogAddNetworkForZone
		.dialog('option', 'buttons', { 	
			"Add": function() { 	
			    var $thisDialog = $(this);		
			    $thisDialog.find("#info_container").hide(); 
			    					
				// validate values
				var isValid = true;					
				var isTagged = true;
				var isDirect = true;
				
				isValid &= validateString("Account", $thisDialog.find("#add_publicip_vlan_account"), $thisDialog.find("#add_publicip_vlan_account_errormsg"), true); //optional
				isValid &= validateInteger("VLAN", $thisDialog.find("#add_publicip_vlan_vlan"), $thisDialog.find("#add_publicip_vlan_vlan_errormsg"), 1, 4095);
				isValid &= validateString("Network Name", $thisDialog.find("#add_publicip_vlan_network_name"), $thisDialog.find("#add_publicip_vlan_network_name_errormsg"));
				isValid &= validateString("Network Description", $thisDialog.find("#add_publicip_vlan_network_desc"), $thisDialog.find("#add_publicip_vlan_network_desc_errormsg"));			
				isValid &= validateIp("Gateway", $thisDialog.find("#add_publicip_vlan_gateway"), $thisDialog.find("#add_publicip_vlan_gateway_errormsg"));
				isValid &= validateIp("Netmask", $thisDialog.find("#add_publicip_vlan_netmask"), $thisDialog.find("#add_publicip_vlan_netmask_errormsg"));
				isValid &= validateIp("Start IP Range", $thisDialog.find("#add_publicip_vlan_startip"), $thisDialog.find("#add_publicip_vlan_startip_errormsg"));   //required
				isValid &= validateIp("End IP Range", $thisDialog.find("#add_publicip_vlan_endip"), $thisDialog.find("#add_publicip_vlan_endip_errormsg"), true);  //optional
				isValid &= validateString("Network Domain", $thisDialog.find("#networkdomain"), $thisDialog.find("#networkdomain_errormsg"), true); //optional
				isValid &= validateString("Tags", $thisDialog.find("#tags"), $thisDialog.find("#tags_errormsg"), true); //optional
							
				if($thisDialog.find("#domain_container").css("display") != "none") {
				    isValid &= validateString("Domain", $thisDialog.find("#domain"), $thisDialog.find("#domain_errormsg"), false);                             //required	
				    var domainName = $thisDialog.find("#domain").val();
				    var domainId;
				    if(domainName != null && domainName.length > 0) { 				    
				        var items;
				        if(zoneObj.domainid != null)
				            items = autoCompleteDomains;
				        else
				            items = autoCompleteDomains;
				        
				        if(items != null && items.length > 0) {									
					        for(var i=0; i < items.length; i++) {					        
					          if(fromdb(items[i].name).toLowerCase() == domainName.toLowerCase()) {
					              domainId = items[i].id;
					              break;	
					          }
				            } 					   			    
				        }					    				    
				        if(domainId == null) {
				            showError(false, $thisDialog.find("#domain"), $thisDialog.find("#domain_errormsg"), g_dictionary["label.not.found"]);
				            isValid &= false;
				        }				    
				    }						
				}
				
				if (!isValid) 
				    return;		
				    				
				$thisDialog.find("#spinning_wheel").show()
				
				var array1 = [];
				array1.push("&zoneId="+zoneObj.id);
				
				var name = todb($thisDialog.find("#add_publicip_vlan_network_name").val());
				array1.push("&name="+name);
				
				var desc = todb($thisDialog.find("#add_publicip_vlan_network_desc").val());
				array1.push("&displayText="+desc);
								
				var vlan = trim($thisDialog.find("#add_publicip_vlan_vlan").val());
				if (isTagged) {
					vlan = "&vlan="+vlan;
				} else {
					vlan = "&vlan=untagged";
				}
				array1.push(vlan);
				
				var scopeParams = "";
				if($thisDialog.find("#domain_container").css("display") != "none") {
					if ($thisDialog.find("#add_publicip_vlan_account_container").css("display") != "none") {
						scopeParams = "&domainId="+domainId+"&account="+trim($thisDialog.find("#add_publicip_vlan_account").val()); 
					} else {
						scopeParams = "&domainId="+domainId+"&isshared=true"; 
					}
				} else if (isDirect) {
					scopeParams = "&isshared=true";
				}				
				array1.push(scopeParams);
				
				var isDefault = $thisDialog.find("#add_publicip_vlan_default").val();
				array1.push("&isDefault="+isDefault);
								
				var gateway = $thisDialog.find("#add_publicip_vlan_gateway").val();
				array1.push("&gateway="+todb(gateway));
				
				var netmask = $thisDialog.find("#add_publicip_vlan_netmask").val();
				array1.push("&netmask="+todb(netmask));
				
				var startip = $thisDialog.find("#add_publicip_vlan_startip").val();
				array1.push("&startip="+todb(startip));
				
				var endip = $thisDialog.find("#add_publicip_vlan_endip").val();
				array1.push("&endip="+todb(endip));
								
				var networkdomain = $thisDialog.find("#networkdomain").val();
				if(networkdomain != null && networkdomain.length > 0)
				    array1.push("&networkdomain="+todb(networkdomain));
								
				var tags = $thisDialog.find("#tags").val();
				if(tags != null && tags.length > 0)
				    array1.push("&tags="+todb(tags));
				
				// Creating network for the direct networking				
				$.ajax({
					data: createURL("command=listNetworkOfferings&guestiptype=Direct"),
					dataType: "json",
					async: false,
					success: function(json) {
						var networkOfferings = json.listnetworkofferingsresponse.networkoffering;
						if (networkOfferings != null && networkOfferings.length > 0) {
							for (var i = 0; i < networkOfferings.length; i++) {
								if (networkOfferings[i].isdefault) {
									array1.push("&networkOfferingId="+networkOfferings[i].id);
									
									// Create a network from this.
									$.ajax({
										data: createURL("command=createNetwork"+array1.join("")),
										dataType: "json",
										success: function(json) {	
											$thisDialog.find("#spinning_wheel").hide();
											$thisDialog.dialog("close");
										
										    var item = json.createnetworkresponse.network;
										    var $midmenuItem1 = $("#midmenu_item").clone();                      
                                            $midmenuItem1.data("toRightPanelFn", directNetworkToRightPanel);                             
                                            directNetworkToMidmenu(item, $midmenuItem1);    
                                            bindClickToMidMenu($midmenuItem1, directNetworkToRightPanel, directNetworkGetMidmenuId);   
                                            $("#midmenu_container").append($midmenuItem1.show());  											    
										},
										error: function(XMLHttpResponse) {
											handleError(XMLHttpResponse, function() {
												handleErrorInDialog(XMLHttpResponse, $thisDialog);	
											});
										}
									});
								}
							}
						}
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

function bindAddIpRangeToDirectNetworkButton($midmenuItem1) {   
    var jsonObj = $midmenuItem1.data("jsonObj");    
    
    $("#add_iprange_button").unbind("click").bind("click", function(event) {    
        if($("#direct_network_page").find("#tab_content_ipallocation").css("display") == "none")       
            $("#direct_network_page").find("#tab_ipallocation").click();    
                       
        var $dialogAddIpRangeToDirectNetwork = $("#dialog_add_iprange_to_directnetwork");  
        $dialogAddIpRangeToDirectNetwork.find("#directnetwork_name").text(fromdb(jsonObj.name));
        $dialogAddIpRangeToDirectNetwork.find("#zone_name").text(fromdb(zoneObj.name));
        
        if(zoneObj.securitygroupsenabled) 
            $dialogAddIpRangeToDirectNetwork.find("#vlan_id_container, #gateway_container, #netmask_container").show();
        else
            $dialogAddIpRangeToDirectNetwork.find("#vlan_id_container, #gateway_container, #netmask_container").hide();    
               
		$dialogAddIpRangeToDirectNetwork
		.dialog('option', 'buttons', { 	
			"Add": function() { 	
			    var $thisDialog = $(this);		
			    					
				// validate values
				var isValid = true;		
				if($thisDialog.find("#vlan_id_container").css("display") != "none")
				    isValid &= validateNumber("VLAN", $thisDialog.find("#vlan_id"), $thisDialog.find("#vlan_id_errormsg"), 1, 4095);				    
				if($thisDialog.find("#gateway_container").css("display") != "none")
				    isValid &= validateIp("Gateway", $thisDialog.find("#gateway"), $thisDialog.find("#gateway_errormsg"), false); //required				    
				if($thisDialog.find("#netmask_container").css("display") != "none")
				    isValid &= validateIp("Netmask", $thisDialog.find("#netmask"), $thisDialog.find("#netmask_errormsg"), false); //required				    
				isValid &= validateIp("Start IP Range", $thisDialog.find("#add_publicip_vlan_startip"), $thisDialog.find("#add_publicip_vlan_startip_errormsg"), false);   //required
				isValid &= validateIp("End IP Range", $thisDialog.find("#add_publicip_vlan_endip"), $thisDialog.find("#add_publicip_vlan_endip_errormsg"), true);  //optional
				if (!isValid) 
				    return;						    
				
				$thisDialog.find("#spinning_wheel").show()
						
				var array1 = [];
								
				if($thisDialog.find("#vlan_id_container").css("display") != "none") {
				    var vlanId = $thisDialog.find("#vlan_id").val();
				    array1.push("&vlan="+todb(vlanId)); 
				}
				//else {   //Bug 8950 (don't have to specify "vlan" parameter when Adding Ip Range to Direct Network)
				//    array1.push("&vlan=untagged");
				//}
				 
				    			    
				if($thisDialog.find("#gateway_container").css("display") != "none") {
				    var gateway = $thisDialog.find("#gateway").val();
				    array1.push("&gateway="+todb(gateway));
				}
				        
				if($thisDialog.find("#netmask_container").css("display") != "none") {
				    var netmask = $thisDialog.find("#netmask").val();
				    array1.push("&netmask="+todb(netmask));
				}
															
				var startip = $thisDialog.find("#add_publicip_vlan_startip").val();
				array1.push("&startip="+todb(startip));
				
				var endip = $thisDialog.find("#add_publicip_vlan_endip").val();	
				if(endip != null && endip.length > 0)
				    array1.push("&endip="+todb(endip));								
						
				$.ajax({
					data: createURL("command=createVlanIpRange&forVirtualNetwork=false&networkid="+todb(jsonObj.id)+array1.join("")),
					dataType: "json",
					success: function(json) {	
						$thisDialog.find("#spinning_wheel").hide();
						$thisDialog.dialog("close");
					
					    var item = json.createvlaniprangeresponse.vlan;	
					    var $newTemplate = $("#direct_iprange_template").clone();
		                directNetworkIprangeJsonToTemplate(item, $newTemplate);
		                $("#right_panel_content #direct_network_page #tab_content_ipallocation").find("#tab_container").prepend($newTemplate.show());					    			   
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

var directNetworkActionMap = {  
    "label.action.edit.network": {    
	    dialogBeforeActionFn : doEditDirectNetwork 
	}    
    ,
    "label.action.delete.network": {              
        isAsyncJob: true,    
        asyncJobResponse: "deletenetworkresponse", 
        dialogBeforeActionFn : doDeleteDirectNetwork,        
        inProcessText: "label.action.delete.network.processing",
        afterActionSeccessFn: function(json, $midmenuItem1, id) {   
            $midmenuItem1.remove();                             
            if(id.toString() == $("#right_panel_content").find("#direct_network_page").find("#tab_content_details").find("#id").text()) {
                clearRightPanel();
                directNetworkClearRightPanel();
            }           
        }
    }    
}  

function doEditDirectNetwork($actionLink, $detailsTab, $midmenuItem1) {     
	var networkObj = $midmenuItem1.data("jsonObj");
	
	$readonlyFields  = $("#direct_network_page").find("#tab_content_details").find("#name, #displaytext, #tags");
    $editFields = $("#direct_network_page").find("#tab_content_details").find("#name_edit, #displaytext_edit, #tags_edit");    
	
    var serviceObj = ipFindNetworkServiceByName("Dns", networkObj);
    if(serviceObj != null) {
        var capabilityObj = ipFindCapabilityByName("AllowDnsSuffixModification", serviceObj);
        if(capabilityObj != null) {
            if(capabilityObj.value == "true") {
            	$readonlyFields  = $("#direct_network_page").find("#tab_content_details").find("#name, #displaytext, #tags, #networkdomain");
                $editFields = $("#direct_network_page").find("#tab_content_details").find("#name_edit, #displaytext_edit, #tags_edit, #networkdomain_edit");    		                
            }
        }
    }	
 	
    $readonlyFields.hide();
    $editFields.show();  
    $detailsTab.find("#cancel_button, #save_button").show();
    
    $detailsTab.find("#cancel_button").unbind("click").bind("click", function(event){    
        cancelEditMode($detailsTab);       
        return false;
    });
    $detailsTab.find("#save_button").unbind("click").bind("click", function(event){        
        doEditDirectNetwork2($actionLink, $detailsTab, $midmenuItem1, $readonlyFields, $editFields);   
        return false;
    });   
}

function doEditDirectNetwork2($actionLink, $detailsTab, $midmenuItem1, $readonlyFields, $editFields) {     
    var jsonObj = $midmenuItem1.data("jsonObj");
    var id = jsonObj.id;
    
    // validate values   
    var isValid = true;					
    isValid &= validateString("Name", $detailsTab.find("#name_edit"), $detailsTab.find("#name_edit_errormsg"), true);		
    isValid &= validateString("Display Text", $detailsTab.find("#displaytext_edit"), $detailsTab.find("#displaytext_edit_errormsg"), true);				
    if (!isValid) 
        return;	
           
    var label = "label.action.edit.network";	
    var label2;
    if(label in dictionary)
        label2 = dictionary[label];
    else
        label2 = label;    
           
    var inProcessText = "label.action.edit.network.processing";	
    var inProcessText2;
    if(inProcessText in dictionary)
        inProcessText2 = dictionary[inProcessText];   
    else
        inProcessText2 = inProcessText;        	          
       
    var $spinningWheel = $detailsTab.find("#spinning_wheel");
    $spinningWheel.find("#description").text(inProcessText2);       
    $spinningWheel.show();      

    var $afterActionInfoContainer = $("#right_panel_content #after_action_info_container_on_top");   
    $afterActionInfoContainer.removeClass("errorbox").hide(); 
    
    var array1 = [];    
    var name = $detailsTab.find("#name_edit").val();
    array1.push("&name="+todb(name));
    
    var displaytext = $detailsTab.find("#displaytext_edit").val();
    array1.push("&displayText="+todb(displaytext));
	    
    var networkdomain = $detailsTab.find("#networkdomain_edit").val();
    array1.push("&networkdomain="+todb(networkdomain));
        
    var tags = $detailsTab.find("#tags_edit").val();
    array1.push("&tags="+todb(tags));
    
	$.ajax({
	    data: createURL("command=updateNetwork&id="+id+array1.join("")),
		dataType: "json",
		success: function(json) {	
		    var jobId = json.updatenetworkresponse.jobid;				        
	        var timerKey = "updatenetworkJob_"+jobId;
	        g_nonCompleteAsyncJob[jobId] = label2;
	        
	        $("body").everyTime(2000, timerKey, function() {
			    $.ajax({
				    data: createURL("command=queryAsyncJobResult&jobId="+jobId),
				    dataType: "json",
				    success: function(json) {										       						   
					    var result = json.queryasyncjobresultresponse;
					    if (result.jobstatus == 0) {
						    return; //Job has not completed
					    } else {											    
						    $("body").stopTime(timerKey);
						    delete g_nonCompleteAsyncJob[jobId];
	                        $spinningWheel.hide(); 
						    
						    if (result.jobstatus == 1) {
							    // Succeeded		    							    	
						    	var jsonObj = result.jobresult.network; 
							    directNetworkToMidmenu(jsonObj, $midmenuItem1);
							    directNetworkToRightPanel($midmenuItem1);	
						    	
							    $editFields.hide();      
					            $readonlyFields.show();       
					            $("#save_button, #cancel_button").hide();     
						    } else if (result.jobstatus == 2) {						    					        
						    	var errorMsg = label2+ " - " + g_dictionary["label.failed"] + " - " + fromdb(result.jobresult.errortext);
	                            if($("#middle_menu").css("display") != "none")
	                                handleMidMenuItemAfterDetailsTabAction($midmenuItem1, false, errorMsg);		
	                            else
	                                showAfterActionInfoOnTop(false, errorMsg);	  
						    }
					    }
				    },
				    error: function(XMLHttpResponse) {
					    $("body").stopTime(timerKey);
						handleError(XMLHttpResponse, function() {													
							handleErrorInDetailsTab(XMLHttpResponse, $detailsTab, label2, $afterActionInfoContainer, $midmenuItem1); 		
						});
				    }
			    });
		    }, 0);		    
		}
	});
}

function doDeleteDirectNetwork($actionLink, $detailsTab, $midmenuItem1) {       
    var jsonObj = $midmenuItem1.data("jsonObj");
	var id = jsonObj.id;
		
	$("#dialog_confirmation")
	.text(dictionary["message.action.delete.network"])
	.dialog('option', 'buttons', { 					
		"Confirm": function() { 			
			$(this).dialog("close");			
			var apiCommand = "command=deleteNetwork&id="+id;
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);	
		}, 
		"Cancel": function() { 
			$(this).dialog("close"); 
		}
	}).dialog("open");
}
//***** Direct Network (end) ******************************************************************************************************

