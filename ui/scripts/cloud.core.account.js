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

var rootDomainId = 1;

var systemAccountId = 1;
var adminAccountId = 2;

var systemUserId = 1;
var adminUserId = 2;

function accountGetSearchParams() {
    var moreCriteria = [];	

	var searchInput = $("#basic_search").find("#search_input").val();	 
    if (searchInput != null && searchInput.length > 0) {	           
        moreCriteria.push("&keyword="+todb(searchInput));	       
    }     

	var $advancedSearchPopup = getAdvancedSearchPopupInSearchContainer();
	if ($advancedSearchPopup.length > 0 && $advancedSearchPopup.css("display") != "none" ) {	
		var role = $advancedSearchPopup.find("#adv_search_role").val();	
		if (role != null && role.length > 0) 
				moreCriteria.push("&accounttype="+role);	
	} 
		
	return moreCriteria.join("");          
}

function afterLoadAccountJSP() {
    if(isAdmin()) {
        initDialog("dialog_resource_limits");            
        initDialog("dialog_edit_user", 450);    
        initDialog("dialog_change_password", 450);    
        initDialog("dialog_add_user", 450);
        
        $("#top_buttons").find("#add_account_button").show();        
        bindAddAccountButton();
        bindAddUserButton();   
    }
    
    // switch between different tabs 
    var tabArray = [$("#tab_details"), $("#tab_user")];
    var tabContentArray = [$("#tab_content_details"), $("#tab_content_user")];
    var afterSwitchFnArray = [accountJsonToDetailsTab, accountJsonToUserTab];
    switchBetweenDifferentTabs(tabArray, tabContentArray, afterSwitchFnArray);       
     
    $readonlyFields  = $("#tab_content_details").find("#name");
    $editFields = $("#tab_content_details").find("#name_edit"); 
            
    initTimezonesObj();     
}

function initTimezonesObj() {
    var timezones = new Object();    
    timezones['Etc/GMT+12']='[UTC-12:00] GMT-12:00';
    timezones['Etc/GMT+11']='[UTC-11:00] GMT-11:00';
    timezones['Pacific/Samoa']='[UTC-11:00] Samoa Standard Time';
    timezones['Pacific/Honolulu']='[UTC-10:00] Hawaii Standard Time';
    timezones['US/Alaska']='[UTC-09:00] Alaska Standard Time';
    timezones['America/Los_Angeles']='[UTC-08:00] Pacific Standard Time';
    timezones['Mexico/BajaNorte']='[UTC-08:00] Baja California';
    timezones['US/Arizona']='[UTC-07:00] Arizona';
    timezones['US/Mountain']='[UTC-07:00] Mountain Standard Time';
    timezones['America/Chihuahua']='[UTC-07:00] Chihuahua, La Paz';
    timezones['America/Chicago']='[UTC-06:00] Central Standard Time';
    timezones['America/Costa_Rica']='[UTC-06:00] Central America';
    timezones['America/Mexico_City']='[UTC-06:00] Mexico City, Monterrey';
    timezones['Canada/Saskatchewan']='[UTC-06:00] Saskatchewan';
    timezones['America/Bogota']='[UTC-05:00] Bogota, Lima';
    timezones['America/New_York']='[UTC-05:00] Eastern Standard Time';
    timezones['America/Caracas']='[UTC-04:00] Venezuela Time';
    timezones['America/Asuncion']='[UTC-04:00] Paraguay Time';
    timezones['America/Cuiaba']='[UTC-04:00] Amazon Time';
    timezones['America/Halifax']='[UTC-04:00] Atlantic Standard Time';
    timezones['America/La_Paz']='[UTC-04:00] Bolivia Time';
    timezones['America/Santiago']='[UTC-04:00] Chile Time';
    timezones['America/St_Johns']='[UTC-03:30] Newfoundland Standard Time';
    timezones['America/Araguaina']='[UTC-03:00] Brasilia Time';
    timezones['America/Argentina/Buenos_Aires']='[UTC-03:00] Argentine Time';
    timezones['America/Cayenne']='[UTC-03:00] French Guiana Time';
    timezones['America/Godthab']='[UTC-03:00] Greenland Time';
    timezones['America/Montevideo']='[UTC-03:00] Uruguay Time]';
    timezones['Etc/GMT+2']='[UTC-02:00] GMT-02:00';
    timezones['Atlantic/Azores']='[UTC-01:00] Azores Time';
    timezones['Atlantic/Cape_Verde']='[UTC-01:00] Cape Verde Time';
    timezones['Africa/Casablanca']='[UTC] Casablanca';
    timezones['Etc/UTC']='[UTC] Coordinated Universal Time';
    timezones['Atlantic/Reykjavik']='[UTC] Reykjavik';
    timezones['Europe/London']='[UTC] Western European Time';
    timezones['CET']='[UTC+01:00] Central European Time';
    timezones['Europe/Bucharest']='[UTC+02:00] Eastern European Time';
    timezones['Africa/Johannesburg']='[UTC+02:00] South Africa Standard Time';
    timezones['Asia/Beirut']='[UTC+02:00] Beirut';
    timezones['Africa/Cairo']='[UTC+02:00] Cairo';
    timezones['Asia/Jerusalem']='[UTC+02:00] Israel Standard Time';
    timezones['Europe/Minsk']='[UTC+02:00] Minsk';
    timezones['Europe/Moscow']='[UTC+03:00] Moscow Standard Time';
    timezones['Africa/Nairobi']='[UTC+03:00] Eastern African Time';
    timezones['Asia/Karachi']='[UTC+05:00] Pakistan Time';
    timezones['Asia/Kolkata']='[UTC+05:30] India Standard Time';
    timezones['Asia/Bangkok']='[UTC+05:30] Indochina Time';
    timezones['Asia/Shanghai']='[UTC+08:00] China Standard Time';
    timezones['Asia/Kuala_Lumpur']='[UTC+08:00] Malaysia Time';
    timezones['Australia/Perth']='[UTC+08:00] Western Standard Time (Australia)';
    timezones['Asia/Taipei']='[UTC+08:00] Taiwan';
    timezones['Asia/Tokyo']='[UTC+09:00] Japan Standard Time';
    timezones['Asia/Seoul']='[UTC+09:00] Korea Standard Time';
    timezones['Australia/Adelaide']='[UTC+09:30] Central Standard Time (South Australia)';
    timezones['Australia/Darwin']='[UTC+09:30] Central Standard Time (Northern Territory)';
    timezones['Australia/Brisbane']='[UTC+10:00] Eastern Standard Time (Queensland)';
    timezones['Australia/Canberra']='[UTC+10:00] Eastern Standard Time (New South Wales)';
    timezones['Pacific/Guam']='[UTC+10:00] Chamorro Standard Time';
    timezones['Pacific/Auckland']='[UTC+12:00] New Zealand Standard Time';
}

function bindAddAccountButton() {     
    initDialog("dialog_add_account", 450);
                   
    var $dialogAddAccount = $("#dialog_add_account");
            
    applyAutoCompleteToDomainField($dialogAddAccount.find("#domain"));   
                
    $("#add_account_button").unbind("click").bind("click", function(event) {    		
		$dialogAddAccount
		.dialog('option', 'buttons', { 					
			"Add": function() { 	
			    var $thisDialog = $(this);	
			    			    			
				// validate values
				var isValid = true;					
				isValid &= validateString("User name", $thisDialog.find("#add_user_username"), $thisDialog.find("#add_user_username_errormsg"), false);    //required
				isValid &= validateString("Password", $thisDialog.find("#add_user_password"), $thisDialog.find("#add_user_password_errormsg"), false);     //required	
				isValid &= validateEmail("Email", $thisDialog.find("#add_user_email"), $thisDialog.find("#add_user_email_errormsg"), false);              //required	
				isValid &= validateString("First name", $thisDialog.find("#add_user_firstname"), $thisDialog.find("#add_user_firstname_errormsg"), false); //required	
				isValid &= validateString("Last name", $thisDialog.find("#add_user_lastname"), $thisDialog.find("#add_user_lastname_errormsg"), false);    //required	
				isValid &= validateString("Account", $thisDialog.find("#add_user_account"), $thisDialog.find("#add_user_account_errormsg"), true);         //optional
				
				isValid &= validateString("Domain", $thisDialog.find("#domain"), $thisDialog.find("#domain_errormsg"), false);                             //required	
				var domainName = $thisDialog.find("#domain").val();
				var domainId;
				if(domainName != null && domainName.length > 0) { 				    
				    if(autoCompleteDomains != null && autoCompleteDomains.length > 0) {									
					    for(var i=0; i < autoCompleteDomains.length; i++) {					        
					      if(fromdb(autoCompleteDomains[i].name).toLowerCase() == domainName.toLowerCase()) {
					          domainId = autoCompleteDomains[i].id;
					          break;	
					      }
				        } 					   			    
				    }					    				    
				    if(domainId == null) {
				        showError(false, $thisDialog.find("#domain"), $thisDialog.find("#domain_errormsg"), g_dictionary["label.not.found"]);
				        isValid &= false;
				    }				    
				}				
				
				if (!isValid) 
				    return;
				
				var $midmenuItem1 = beforeAddingMidMenuItem() ;
								
				var array1 = [];																		
				var username = $thisDialog.find("#add_user_username").val();
				array1.push("&username="+todb(username));
				
				var password = $thisDialog.find("#add_user_password").val();
				if (md5Hashed) {
			        password = $.md5(password);
		        } 				
				array1.push("&password="+password);
				
				var email = $thisDialog.find("#add_user_email").val();				
				array1.push("&email="+todb(email));
					
				var firstname = $thisDialog.find("#add_user_firstname").val();				
				array1.push("&firstname="+todb(firstname));
					
				var lastname = $thisDialog.find("#add_user_lastname").val();
			    array1.push("&lastname="+todb(lastname));
					
				var account = $thisDialog.find("#add_user_account").val();					
				if(account == "")
					account = username;
			    array1.push("&account="+todb(account));
					
				var accountType = $thisDialog.find("#add_user_account_type").val();	
																
				if (parseInt(domainId) != rootDomainId && accountType == "1") {
					accountType = "2"; // Change to domain admin 
				}
				array1.push("&accounttype="+accountType);	
				
				//var domainId = $thisDialog.find("#domain_dropdown").val();				
				array1.push("&domainid="+domainId);
								
				var timezone = $thisDialog.find("#add_user_timezone").val();	
				if(timezone != null && timezone.length > 0)
	                array1.push("&timezone="+todb(timezone));	
	        						
				$thisDialog.dialog("close");					
									
				$.ajax({
					type: "POST",
				    data: createURL("command=createAccount"+array1.join("")),
					dataType: "json",
					async: false,
					success: function(json) {						    
					    if($("#leftmenu_account_all_accounts").hasClass("selected") == false) { //for fixing Bug 7452 ("Adding an account under My Account will result in a duplicate") 
					        $("#leftmenu_account_all_accounts").click();
					    }
					    else {							       
					        var item = json.createaccountresponse.account;					    			    		
						    accountToMidmenu(item, $midmenuItem1);
	                        bindClickToMidMenu($midmenuItem1, accountToRightPanel, getMidmenuId);  
	                        afterAddingMidMenuItem($midmenuItem1, true);	                       
	                    }							
					},			
                    error: function(XMLHttpResponse) {	                        
                        afterAddingMidMenuItem($midmenuItem1, false, parseXMLHttpResponse(XMLHttpResponse));        
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

function bindAddUserButton() {                 
    var $dialogAddUser = $("#dialog_add_user");
         
    $("#add_user_button").show().unbind("click").bind("click", function(event) {   
        var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
        if($midmenuItem1 == null)
            return;
        
        var accountObj = $midmenuItem1.data("jsonObj");
        if(accountObj == null)
            return;    
         
        if($("#tab_user").hasClass("off"))
            $("#tab_user").click();     
             
        $dialogAddUser.find("#account_name").text(accountObj.name);
        $dialogAddUser.find("#info_container").hide(); 
            	
		$dialogAddUser
		.dialog('option', 'buttons', { 					
			"Add": function() { 	
			    var $thisDialog = $(this);		
			    $thisDialog.find("#info_container").hide();
			    		    			
				// validate values
				var isValid = true;					
				isValid &= validateString("User name", $thisDialog.find("#add_user_username"), $thisDialog.find("#add_user_username_errormsg"), false);    //required
				isValid &= validateString("Password", $thisDialog.find("#add_user_password"), $thisDialog.find("#add_user_password_errormsg"), false);     //required	
				isValid &= validateEmail("Email", $thisDialog.find("#add_user_email"), $thisDialog.find("#add_user_email_errormsg"), false);              //required
				isValid &= validateString("First name", $thisDialog.find("#add_user_firstname"), $thisDialog.find("#add_user_firstname_errormsg"), false); //required
				isValid &= validateString("Last name", $thisDialog.find("#add_user_lastname"), $thisDialog.find("#add_user_lastname_errormsg"), false);    //required				
				if (!isValid) 
				    return;
				
				$thisDialog.find("#spinning_wheel").show();
								
				var array1 = [];																		
				var username = $thisDialog.find("#add_user_username").val();
				array1.push("&username="+todb(username));
				
				var password = $thisDialog.find("#add_user_password").val();
				if (md5Hashed) {
			        password = $.md5(password);
		        } 
				array1.push("&password="+password);
				
				var email = $thisDialog.find("#add_user_email").val();
				array1.push("&email="+todb(email));
					
				var firstname = $thisDialog.find("#add_user_firstname").val();
				array1.push("&firstname="+todb(firstname));
					
				var lastname = $thisDialog.find("#add_user_lastname").val();
			    array1.push("&lastname="+todb(lastname));
									
			    array1.push("&domainid="+accountObj.domainid);
			    array1.push("&account="+accountObj.name);							
				array1.push("&accounttype="+accountObj.accounttype);	
								
				var timezone = $thisDialog.find("#add_user_timezone").val();	
				if(timezone != null && timezone.length > 0)
	                array1.push("&timezone="+todb(timezone));		        											
									
				$.ajax({
					type: "POST",
				    data: createURL("command=createUser"+array1.join("")),
					dataType: "json",
					async: false,
					success: function(json) {						    
					    $thisDialog.find("#spinning_wheel").hide();				        
				        $thisDialog.dialog("close");
									    
					    var item = json.createuserresponse.user;
					    					    
					    var $subgridItem = $("#user_tab_template").clone(true);	                        
				        accountUserJSONToTemplate(item, $subgridItem);	
	                    $subgridItem.find("#after_action_info").text(g_dictionary["label.adding.succeeded"]);
	                    $subgridItem.find("#after_action_info_container").removeClass("error").addClass("success").show();  
                        $("#tab_content_user").find("#tab_container").append($subgridItem.show());  					    
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

function accountToMidmenu(jsonObj, $midmenuItem1) {  
    $midmenuItem1.attr("id", getMidmenuId(jsonObj));  
    $midmenuItem1.data("jsonObj", jsonObj); 
    
    var $iconContainer = $midmenuItem1.find("#icon_container").show();   
    if (jsonObj.accounttype == roleTypeUser) 
        $iconContainer.find("#icon").attr("src", "images/midmenuicon_account_user.png");		
	else if (jsonObj.accounttype == roleTypeAdmin) 
	    $iconContainer.find("#icon").attr("src", "images/midmenuicon_account_admin.png");		
	else if (jsonObj.accounttype == roleTypeDomainAdmin) 
	    $iconContainer.find("#icon").attr("src", "images/midmenuicon_account_domain.png");	
    
    var firstRowText = fromdb(jsonObj.name);
    $midmenuItem1.find("#first_row").text(firstRowText.substring(0,midMenuFirstRowLength));     
    $midmenuItem1.find("#first_row_container").attr("title", firstRowText);   
    
    var secondRowText = fromdb(jsonObj.domain);
    $midmenuItem1.find("#second_row").text(secondRowText.substring(0,midMenuSecondRowLength));
    $midmenuItem1.find("#second_row_container").attr("title", secondRowText); 
}

function accountToRightPanel($midmenuItem1) { 
    copyActionInfoFromMidMenuToRightPanel($midmenuItem1);  
    $("#right_panel_content").data("$midmenuItem1", $midmenuItem1);  
    $("#tab_details").click();   
}

function accountClearRightPanel() { 
    accountClearDetailsTab();
    accountClearUserTab();
}

function accountJsonToDetailsTab() {  
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
    if($midmenuItem1 == null) {
        accountClearDetailsTab();
        return;
    }
    
    var jsonObj = $midmenuItem1.data("jsonObj");
    if(jsonObj == null) {
        accountClearDetailsTab();
        return;
    }

    $.ajax({
        data: createURL("command=listAccounts&id="+jsonObj.id),
        dataType: "json",
        async: false,
        success: function(json) {  
            var items = json.listaccountsresponse.account;                   
            if(items != null && items.length > 0) {
                jsonObj = items[0];
                $midmenuItem1.data("jsonObj", jsonObj);  
            }
        }
    });  
   
    var $detailsTab = $("#right_panel_content").find("#tab_content_details");           
    $detailsTab.find("#grid_header_title").text(fromdb(jsonObj.name));
    $detailsTab.find("#id").text(fromdb(jsonObj.id));
    $detailsTab.find("#role").text(toRole(jsonObj.accounttype));
    $detailsTab.find("#name").text(fromdb(jsonObj.name));
    $detailsTab.find("#name_edit").val(fromdb(jsonObj.name));
    $detailsTab.find("#domain").text(fromdb(jsonObj.domain));
    $detailsTab.find("#vm_total").text(fromdb(jsonObj.vmtotal));
    $detailsTab.find("#ip_total").text(fromdb(jsonObj.iptotal));
    $detailsTab.find("#bytes_received").text(convertBytes(jsonObj.receivedbytes));
    $detailsTab.find("#bytes_sent").text(convertBytes(jsonObj.sentbytes));
    $detailsTab.find("#state").text(fromdb(jsonObj.state));
    
    //actions ***
    var $actionMenu = $("#right_panel_content #tab_content_details #action_link #action_menu");
    $actionMenu.find("#action_list").empty(); 
    var noAvailableActions = true;
  
    if(isAdmin()) {
        if(jsonObj.id != systemAccountId && jsonObj.id != adminAccountId) {   
            buildActionLinkForTab("label.action.edit.account", accountActionMap, $actionMenu, $midmenuItem1, $detailsTab);  
             
            if (jsonObj.accounttype == roleTypeUser || jsonObj.accounttype == roleTypeDomainAdmin) {
                buildActionLinkForTab("label.action.resource.limits", accountActionMap, $actionMenu, $midmenuItem1, $detailsTab);	                
            }
             
            if(jsonObj.state == "enabled") {
                buildActionLinkForTab("label.action.disable.account", accountActionMap, $actionMenu, $midmenuItem1, $detailsTab);  
                buildActionLinkForTab("label.action.lock.account", accountActionMap, $actionMenu, $midmenuItem1, $detailsTab);                
            }          	        
            else if(jsonObj.state == "disabled" || jsonObj.state == "locked") {
                buildActionLinkForTab("label.action.enable.account", accountActionMap, $actionMenu, $midmenuItem1, $detailsTab);                   
            }   
            
            buildActionLinkForTab("label.action.delete.account", accountActionMap, $actionMenu, $midmenuItem1, $detailsTab);  
            noAvailableActions = false;	            
        }  
    }
    buildActionLinkForTab("label.action.update.resource.count", accountActionMap, $actionMenu, $midmenuItem1, $detailsTab);  
    noAvailableActions = false;	    
    
    // no available actions 
	if(noAvailableActions == true) {
	    $actionMenu.find("#action_list").append($("#no_available_actions").clone().show());
	}	  
}

function accountClearDetailsTab() {      
    var $detailsTab = $("#right_panel_content").find("#tab_content_details");           
    $detailsTab.find("#grid_header_title").text("");
    $detailsTab.find("#id").text("");
    $detailsTab.find("#role").text("");
    $detailsTab.find("#name").text("");
    $detailsTab.find("#domain").text("");
    $detailsTab.find("#vm_total").text("");
    $detailsTab.find("#ip_total").text("");
    $detailsTab.find("#bytes_received").text("");
    $detailsTab.find("#bytes_sent").text("");
    $detailsTab.find("#state").text("");
    
    //actions ***
    var $actionMenu = $("#right_panel_content #tab_content_details #action_link #action_menu");
    $actionMenu.find("#action_list").empty();    
	$actionMenu.find("#action_list").append($("#no_available_actions").clone().show());		  
}

function accountJsonToUserTab() {       	
	var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");	
	if($midmenuItem1 == null) {
	    accountClearUserTab();
	    return;
	}
	
	var jsonObj = $midmenuItem1.data("jsonObj");	
	if(jsonObj == null) {
	    accountClearUserTab();
	    return;     
	}       
	
	var $thisTab = $("#right_panel_content").find("#tab_content_user");	    
	$thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();   
        
    $.ajax({
		cache: false,
		data: createURL("command=listUsers&domainid="+fromdb(jsonObj.domainid)+"&account="+todb(fromdb(jsonObj.name))),
		dataType: "json",
		success: function(json) {						    
			var items = json.listusersresponse.user;	
			var $container = $thisTab.find("#tab_container").empty();																					
			if (items != null && items.length > 0) {			    
				var $template = $("#user_tab_template");				
				for (var i = 0; i < items.length; i++) {
					var $newTemplate = $template.clone(true);	               
	                accountUserJSONToTemplate(items[i], $newTemplate); 
	                $container.append($newTemplate.show());	
				}			
			}	
			$thisTab.find("#tab_spinning_wheel").hide();    
            $thisTab.find("#tab_container").show();    			
		}
	});
} 

function accountClearUserTab() {     
	var $thisTab = $("#right_panel_content").find("#tab_content_user");	    
	$thisTab.find("#tab_container").empty();
} 

function accountUserJSONToTemplate(jsonObj, $template) {
    $template.data("jsonObj", jsonObj);     
    $template.attr("id", "account_user_"+fromdb(jsonObj.id)).data("accountUserId", fromdb(jsonObj.id));    
    $template.find("#grid_header_title").text(fromdb(jsonObj.username));			   
    $template.find("#id").text(fromdb(jsonObj.id));
    $template.find("#username").text(fromdb(jsonObj.username));	    
    $template.find("#state").text(fromdb(jsonObj.state));	
    $template.find("#apikey").text(fromdb(jsonObj.apikey));
    $template.find("#secretkey").text(fromdb(jsonObj.secretkey));    
    $template.find("#account").text(fromdb(jsonObj.account));	
    $template.find("#role").text(toRole(fromdb(jsonObj.accounttype)));	    
    $template.find("#domain").text(fromdb(jsonObj.domain));	
    $template.find("#email").text(fromdb(jsonObj.email));	
    $template.find("#firstname").text(fromdb(jsonObj.firstname));	
    $template.find("#lastname").text(fromdb(jsonObj.lastname));	
    $template.find("#timezone").text(timezones[fromdb(jsonObj.timezone)]);	
    $template.data("timezone", jsonObj.timezone); 
      
    //actions    
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
    var noAvailableActions = true;
    
    if(isAdmin()) {
        buildActionLinkForSubgridItem("label.action.edit.user", accountUserActionMap, $actionMenu, $template);	 
        buildActionLinkForSubgridItem("label.action.change.password", accountUserActionMap, $actionMenu, $template);	  
        buildActionLinkForSubgridItem("label.action.generate.keys", accountUserActionMap, $actionMenu, $template);	    
        noAvailableActions = false;
        
        if(jsonObj.id != systemUserId && jsonObj.id != adminUserId) {
            if(jsonObj.state == "enabled") 
                buildActionLinkForSubgridItem("label.action.disable.user", accountUserActionMap, $actionMenu, $template);	  
            if(jsonObj.state == "disabled")
                buildActionLinkForSubgridItem("label.action.enable.user", accountUserActionMap, $actionMenu, $template);	  
            buildActionLinkForSubgridItem("label.action.delete.user", accountUserActionMap, $actionMenu, $template);	  
        }
	} 	
	
	if(noAvailableActions == true) {
	    $actionMenu.find("#action_list").append($("#no_available_actions").clone().show());
	}	
} 

var accountActionMap = {  
    "label.action.edit.account": {
        dialogBeforeActionFn: doEditAccount  
    }, 
    "label.action.update.resource.count": {   
    	isAsyncJob: false,   
    	dialogBeforeActionFn : doUpdateResourceCountForAccount,                     
        inProcessText: "label.action.update.resource.count.processing",
        afterActionSeccessFn: function(json, $midmenuItem1, id){}           
    },   
    "label.action.resource.limits": {                 
        dialogBeforeActionFn : doResourceLimitsForAccount 
    } 
    ,
    "label.action.disable.account": {              
        isAsyncJob: true,
        asyncJobResponse: "disableaccountresponse",
        dialogBeforeActionFn : doDisableAccount,
        inProcessText: "label.action.disable.account.processing",
        afterActionSeccessFn: function(json, $midmenuItem1, id) {             
            var item = json.queryasyncjobresultresponse.jobresult.account;
            accountToMidmenu(item, $midmenuItem1);  
        }
    }    
    ,
    "label.action.lock.account": {              
        isAsyncJob: true,     
        asyncJobResponse: "disableaccountresponse",  
        dialogBeforeActionFn : doLockAccount,
        inProcessText: "label.action.lock.account.processing",
        afterActionSeccessFn: function(json, $midmenuItem1, id) {  
            var item = json.queryasyncjobresultresponse.jobresult.account;
            accountToMidmenu(item, $midmenuItem1);   
        }
    }    
    ,
    "label.action.enable.account": {              
        isAsyncJob: false,       
        dialogBeforeActionFn : doEnableAccount,
        inProcessText: "label.action.enable.account.processing",
        afterActionSeccessFn: function(json, $midmenuItem1, id) {   
            var item = json.enableaccountresponse.account;                  
            accountToMidmenu(item, $midmenuItem1);    
        }
    } 
    ,
    "label.action.delete.account": {              
        isAsyncJob: true,
        asyncJobResponse: "deleteaccountresponse",
        dialogBeforeActionFn : doDeleteAccount,
        inProcessText: "label.action.delete.account.processing",
        afterActionSeccessFn: function(json, $midmenuItem1, id) {  
    	    $midmenuItem1.remove();
            if(id.toString() == $("#right_panel_content").find("#tab_content_details").find("#id").text()) {
                clearRightPanel();
                accountClearRightPanel();
            }   
        }
    }          
}; 

function doEditAccount($actionLink, $detailsTab, $midmenuItem1) {               
    $readonlyFields.hide();
    $editFields.show();  
    $detailsTab.find("#cancel_button, #save_button").show();
    
    $detailsTab.find("#cancel_button").unbind("click").bind("click", function(event){    
        cancelEditMode($detailsTab);        
        return false;
    });
    $detailsTab.find("#save_button").unbind("click").bind("click", function(event){        
        doEditAccount2($actionLink, $detailsTab, $midmenuItem1, $readonlyFields, $editFields);   
        return false;
    });   
}

function doEditAccount2($actionLink, $detailsTab, $midmenuItem1, $readonlyFields, $editFields) {     
    // validate values
    var isValid = true;					
    isValid &= validateString("Name", $detailsTab.find("#name_edit"), $detailsTab.find("#name_edit_errormsg"));    		
    if (!isValid) 
        return;
       
    var jsonObj = $midmenuItem1.data("jsonObj"); 
	var id = jsonObj.id;
	
	var array1 = [];
	array1.push("&id="+id);
							
	var name = $detailsTab.find("#name_edit").val();
	array1.push("&newname="+todb(name));
		
	$.ajax({
	    data: createURL("command=updateAccount&domainid="+jsonObj.domainid+"&account="+jsonObj.name+array1.join("")),
		dataType: "json",		
		success: function(json) {	
		    var item = json.updateaccountresponse.account;		
		    accountToMidmenu(item, $midmenuItem1);           
            accountJsonToDetailsTab();
            		   		    
		    $editFields.hide();      
            $readonlyFields.show();       
            $("#save_button, #cancel_button").hide();       	  		
		}
	});
}

function doUpdateResourceCountForAccount($actionLink, $detailsTab, $midmenuItem1) {      
	var jsonObj = $midmenuItem1.data("jsonObj");
    var id = jsonObj.id;
	var domainid=jsonObj.domainid;
	var account = jsonObj.name;			
	var apiCommand = "command=updateResourceCount&domainid="+domainid+"&account="+account;
	doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);			
}

function updateResourceLimitForAccount(domainId, account, type, max) {
	$.ajax({
	    data: createURL("command=updateResourceLimit&domainid="+domainId+"&account="+account+"&resourceType="+type+"&max="+max),
		dataType: "json",
		success: function(json) {								    												
		}
	});
}

function doResourceLimitsForAccount($actionLink, $detailsTab, $midmenuItem1) {
    var $detailsTab = $("#right_panel_content #tab_content_details");  
	var jsonObj = $midmenuItem1.data("jsonObj");
	var domainId = jsonObj.domainid;
	var account = jsonObj.name;
	$.ajax({
		cache: false,				
		data: createURL("command=listResourceLimits&domainid="+domainId+"&account="+account),
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
							$("#dialog_resource_limits #limits_vm").val(limit.max);
							break;
						case "1":
							preIpLimit = limit.max;
							$("#dialog_resource_limits #limits_ip").val(limit.max);
							break;
						case "2":
							preDiskLimit = limit.max;
							$("#dialog_resource_limits #limits_volume").val(limit.max);
							break;
						case "3":
							preSnapshotLimit = limit.max;
							$("#dialog_resource_limits #limits_snapshot").val(limit.max);
							break;
						case "4":
							preTemplateLimit = limit.max;
							$("#dialog_resource_limits #limits_template").val(limit.max);
							break;
					}
				}
			}	
			$("#dialog_resource_limits")
			.dialog('option', 'buttons', { 								
				"Save": function() { 	
					// validate values
					var isValid = true;					
					isValid &= validateInteger("Instance Limit", $("#dialog_resource_limits #limits_vm"), $("#dialog_resource_limits #limits_vm_errormsg"), -1, 32000, false);
					isValid &= validateInteger("Public IP Limit", $("#dialog_resource_limits #limits_ip"), $("#dialog_resource_limits #limits_ip_errormsg"), -1, 32000, false);
					isValid &= validateInteger("Disk Volume Limit", $("#dialog_resource_limits #limits_volume"), $("#dialog_resource_limits #limits_volume_errormsg"), -1, 32000, false);
					isValid &= validateInteger("Snapshot Limit", $("#dialog_resource_limits #limits_snapshot"), $("#dialog_resource_limits #limits_snapshot_errormsg"), -1, 32000, false);
					isValid &= validateInteger("Template Limit", $("#dialog_resource_limits #limits_template"), $("#dialog_resource_limits #limits_template_errormsg"), -1, 32000, false);
					if (!isValid) return;
												
					var instanceLimit = trim($("#dialog_resource_limits #limits_vm").val());
					var ipLimit = trim($("#dialog_resource_limits #limits_ip").val());
					var diskLimit = trim($("#dialog_resource_limits #limits_volume").val());
					var snapshotLimit = trim($("#dialog_resource_limits #limits_snapshot").val());
					var templateLimit = trim($("#dialog_resource_limits #limits_template").val());
											
					$(this).dialog("close"); 
					if (instanceLimit != preInstanceLimit) {
						updateResourceLimitForAccount(domainId, account, 0, instanceLimit);
					}
					if (ipLimit != preIpLimit) {
						updateResourceLimitForAccount(domainId, account, 1, ipLimit);
					}
					if (diskLimit != preDiskLimit) {
						updateResourceLimitForAccount(domainId, account, 2, diskLimit);
					}
					if (snapshotLimit != preSnapshotLimit) {
						updateResourceLimitForAccount(domainId, account, 3, snapshotLimit);
					}
					if (templateLimit != preTemplateLimit) {
						updateResourceLimitForAccount(domainId, account, 4, templateLimit);
					}
				}, 
				"Cancel": function() { 
					$(this).dialog("close"); 
				} 
			}).dialog("open");
		}
	});	
}

function doDisableAccount($actionLink, $detailsTab, $midmenuItem1) {       
    var jsonObj = $midmenuItem1.data("jsonObj");    
    var id = jsonObj.id;
    
    $("#dialog_confirmation")  
    .text(dictionary["message.disable.account"])  
    .dialog('option', 'buttons', {                    
        "Yes": function() { 		                    
            $(this).dialog("close");	
			var apiCommand = "command=disableAccount&lock=false&account="+jsonObj.name+"&domainId="+jsonObj.domainid;	    	
	    	doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab) ;         		                    	     
        },
        "Cancel": function() {
            $(this).dialog("close");		     
        }
    }).dialog("open");  
}

function doLockAccount($actionLink, $detailsTab, $midmenuItem1) {       
    var jsonObj = $midmenuItem1.data("jsonObj");    
    
    $("#dialog_confirmation")
    .text(dictionary["message.lock.account"])    
    .dialog('option', 'buttons', {                    
        "Yes": function() { 		                    
            $(this).dialog("close");			
			var apiCommand = "command=disableAccount&lock=true&account="+jsonObj.name+"&domainId="+jsonObj.domainid;
	    	doActionToTab(jsonObj.id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);	         		                    	     
        },
        "Cancel": function() {
            $(this).dialog("close");		     
        }
    }).dialog("open");  
}

function doEnableAccount($actionLink, $detailsTab, $midmenuItem1) {       
    var jsonObj = $midmenuItem1.data("jsonObj");    
    
    $("#dialog_confirmation")  
    .text(dictionary["message.enable.account"])  
    .dialog('option', 'buttons', {                    
        "Yes": function() { 		                    
            $(this).dialog("close");	
			var apiCommand = "command=enableAccount&account="+jsonObj.name+"&domainId="+jsonObj.domainid;
	    	doActionToTab(jsonObj.id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);	         		                    	     
        },
        "Cancel": function() {
            $(this).dialog("close");		     
        }
    }).dialog("open");  
}

function doDeleteAccount($actionLink, $detailsTab, $midmenuItem1) {       
    var jsonObj = $midmenuItem1.data("jsonObj");    
    var id = jsonObj.id;
    
    $("#dialog_confirmation")    
    .text(dictionary["message.delete.account"])
    .dialog('option', 'buttons', {                    
        "Yes": function() { 		                    
            $(this).dialog("close");	
			var apiCommand = "command=deleteAccount&id="+jsonObj.id;	    	
	    	doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab) ;         		                    	     
        },
        "Cancel": function() {
            $(this).dialog("close");		     
        }
    }).dialog("open");  
}

var accountUserActionMap = {
    "label.action.edit.user": {
        dialogBeforeActionFn : doEditUser
    },
    "label.action.change.password": {
        dialogBeforeActionFn : doChangePassword
    },
    "label.action.generate.keys": {  
        api: "registerUserKeys",            
        isAsyncJob: false,
        inProcessText: "label.action.generate.keys.processing",
        afterActionSeccessFn: function(json, id, $subgridItem) {
            var jsonObj = json.registeruserkeysresponse.userkeys;
            $subgridItem.find("#apikey").text(fromdb(jsonObj.apikey));    
            $subgridItem.find("#secretkey").text(fromdb(jsonObj.secretkey));	
        }            
    },
    "label.action.disable.user": {              
        api: "disableUser",     
        isAsyncJob: true,
        asyncJobResponse: "disableuserresponse",		
        inProcessText: "label.action.disable.user.processing",
        afterActionSeccessFn: function(json, id, $subgridItem) {                 
            var item = json.queryasyncjobresultresponse.jobresult.user;    
            accountUserJSONToTemplate(item, $subgridItem); 
        }
    } ,
    "label.action.enable.user": {              
        api: "enableUser",     
        isAsyncJob: false,        
        inProcessText: "label.action.enable.user.processing",
        afterActionSeccessFn: function(json, id, $subgridItem) {    
            var item = json.enableuserresponse.user;    
            accountUserJSONToTemplate(item, $subgridItem); 
        }
    } ,
    "label.action.delete.user": {
        api: "deleteUser",            
        isAsyncJob: false,
        inProcessText: "label.action.delete.user.processing",
        afterActionSeccessFn: function(json, id, $subgridItem) {
            $subgridItem.slideUp("slow", function() {
                $(this).remove();
            });
        }        
    }    
}

function doEditUser($actionLink, $subgridItem) {   
    var jsonObj = $subgridItem.data("jsonObj");
    var id = jsonObj.id;

    var $dialogEditUser = $("#dialog_edit_user");		           
						
	$dialogEditUser.find("#edit_user_username").val($subgridItem.find("#username").text());   
	if(id==systemUserId || id==adminUserId)
		$dialogEditUser.find("#edit_user_username").attr("disabled", true);
	else
		$dialogEditUser.find("#edit_user_username").attr("disabled", false);    
						
	$dialogEditUser.find("#edit_user_email").val($subgridItem.find("#email").text());
	$dialogEditUser.find("#edit_user_firstname").val($subgridItem.find("#firstname").text());
	$dialogEditUser.find("#edit_user_lastname").val($subgridItem.find("#lastname").text());						
	$dialogEditUser.find("#edit_user_timezone").val($subgridItem.data("timezone"));
	
	$dialogEditUser
	.dialog('option', 'buttons', { 							
		"Save": function() { 	
		    var $thisDialog = $(this);
			
			// validate values						   
			var isValid = true;					
			isValid &= validateString("User name", $thisDialog.find("#edit_user_username"), $thisDialog.find("#edit_user_username_errormsg"), false);	  //required					      
			isValid &= validateString("Email", $thisDialog.find("#edit_user_email"), $thisDialog.find("#edit_user_email_errormsg"), true);	          //optional
			isValid &= validateString("First name", $thisDialog.find("#edit_user_firstname"), $thisDialog.find("#edit_user_firstname_errormsg"), true); //optional
			isValid &= validateString("Last name", $thisDialog.find("#edit_user_lastname"), $thisDialog.find("#edit_user_lastname_errormsg"), true);	  //optional	   	
			if (!isValid) 
			    return;
										
			var username = $thisDialog.find("#edit_user_username").val();							  
			var email = $thisDialog.find("#edit_user_email").val();
			var firstname = $thisDialog.find("#edit_user_firstname").val();
			var lastname = $thisDialog.find("#edit_user_lastname").val(); 	
			var timezone = $thisDialog.find("#edit_user_timezone").val(); 							
											
			$thisDialog.dialog("close");
			
			$.ajax({
			    data: createURL("command=updateUser&id="+id+"&username="+todb(username)+"&email="+todb(email)+"&firstname="+todb(firstname)+"&lastname="+todb(lastname)+"&timezone="+todb(timezone)),
				dataType: "json",
				success: function(json) {	
				    $subgridItem.find("#after_action_info").text("Edit User action succeeded.");
                    $subgridItem.find("#after_action_info_container").removeClass("error").addClass("success").show(); 
											      						    					
					$subgridItem.find("#username").text(username);
					$subgridItem.find("#email").text(email);
					$subgridItem.find("#firstname").text(firstname);
					$subgridItem.find("#lastname").text(lastname);		
					$subgridItem.find("#timezone").text(timezones[timezone]);		
					$subgridItem.data("timezone", timezone);
				}
			});
		},
		"Cancel": function() { 
			$(this).dialog("close"); 
		} 
	}).dialog("open");
}

function doChangePassword($actionLink, $subgridItem) {   
    var jsonObj = $subgridItem.data("jsonObj");
    var id = jsonObj.id;

    var $dialogChangePassword = $("#dialog_change_password");			             
	$dialogChangePassword.find("#change_password_password1").val("");         
	
	$dialogChangePassword
	.dialog('option', 'buttons', { 							
		"Save": function() { 	
		    var thisDialog = $(this);
		    					
			// validate values						   
			var isValid = true;					      	
			isValid &= validateString("Password", thisDialog.find("#change_password_password1"), thisDialog.find("#change_password_password1_errormsg"), false); //required						      		   	
			if (!isValid) return;
																	
			var password = thisDialog.find("#change_password_password1").val();	
			if (md5Hashed) {
		        password = $.md5(password);
	        } 					   					
											
			thisDialog.dialog("close");
			$.ajax({
			    data: createURL("command=updateUser&id="+id+"&password="+password),
				dataType: "json",
				success: function(json) {					    
				    var label = $actionLink.data("label");	
                    var label2;
                    if(label in dictionary)
                        label2 = dictionary[label];   
                    else
                        label2 = label;				    	    
				    $subgridItem.find("#after_action_info").text(label2 + " - " + g_dictionary["label.succeeded"]);				    
                    $subgridItem.find("#after_action_info_container").removeClass("error").addClass("success").show(); 				    				       				
				}
			});
		},
		"Cancel": function() { 
			$(this).dialog("close"); 
		} 
	}).dialog("open");    
}    