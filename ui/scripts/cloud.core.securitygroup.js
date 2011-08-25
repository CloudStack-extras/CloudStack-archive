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

function securityGroupGetSearchParams() {
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
        
	} 	
	
	return moreCriteria.join("");          
}

function afterLoadSecurityGroupJSP() {    
    initAddSecurityGroupDialog();  
    initAddIngressRuleDialog();
    
    // switch between different tabs 
    var tabArray = [$("#tab_details"), $("#tab_ingressrule")];
    var tabContentArray = [$("#tab_content_details"), $("#tab_content_ingressrule")];
    var afterSwitchFnArray = [securityGroupJsonToDetailsTab, securityGroupJsonToIngressRuleTab];
    switchBetweenDifferentTabs(tabArray, tabContentArray, afterSwitchFnArray);           
}

function initAddSecurityGroupDialog() {
    initDialog("dialog_add_security_group");   

    var $dialogAddSecurityGroup = $("#dialog_add_security_group");
    var $detailsTab = $("#right_panel_content").find("#tab_content_details");             
     
    //add button ***        
    $("#add_securitygroup_button").unbind("click").bind("click", function(event) {    
        $dialogAddSecurityGroup
	    .dialog('option', 'buttons', {
	        "Add": function() {		            	          
	            var $thisDialog = $(this);	
							
				// validate values
				var isValid = true;
				isValid &= validateString("Name", $thisDialog.find("#name"), $thisDialog.find("#name_errormsg"), false);  //required
				isValid &= validateString("Description", $thisDialog.find("#description"), $thisDialog.find("#description_errormsg"), true);	//optional				
				if (!isValid) 
				    return;	
				
				var $midmenuItem1 = beforeAddingMidMenuItem();				   					
				
				var name = trim($thisDialog.find("#name").val());
				var desc = trim($thisDialog.find("#description").val());
				
				$thisDialog.dialog("close");
							
				$.ajax({						
				    data: createURL("command=createSecurityGroup&name="+todb(name)+"&description="+todb(desc)),
					dataType: "json",
					success: function(json) {						    			   
						var item = json.createsecuritygroupresponse.securitygroup;																		   
					    securityGroupToMidmenu(item, $midmenuItem1);
	                    bindClickToMidMenu($midmenuItem1, securityGroupToRightPanel, getMidmenuId);  
	                    afterAddingMidMenuItem($midmenuItem1, true);	 									
					}, 								
                    error: function(XMLHttpResponse) {	 
						handleError(XMLHttpResponse, function() {
							afterAddingMidMenuItem($midmenuItem1, false, parseXMLHttpResponse(XMLHttpResponse));	
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

function initAddIngressRuleDialog() {
    initDialog("dialog_add_ingress_rule", 400);   
    
    var $dialogAddIngressRule = $("#dialog_add_ingress_rule");
				
	$dialogAddIngressRule.find("#add_more_cidr").unbind("click").bind("click", function(event){		    
        $dialogAddIngressRule.find("#cidr_container").append($("#cidr_template").clone().show());
        return false;
    });	
			
	$dialogAddIngressRule.find("#add_more_account_securitygroup").unbind("click").bind("click", function(event){		    
        $dialogAddIngressRule.find("#account_securitygroup_container").append($("#account_securitygroup_template").clone().show());
        return false;
    });		
       				
	$dialogAddIngressRule.find("input[name='ingress_rule_type']").unbind("change").bind("change", function(){		    
	    if($dialogAddIngressRule.find("input[name='ingress_rule_type']:checked").val() == "cidr") {	
	        //enable CIDR	        
	        $dialogAddIngressRule.find(".cidr_template, #add_more_cidr").removeAttr("disabled");	 
	        	        
	        //disable Account/Network Group, clear up error fields 	        
	        $dialogAddIngressRule.find(".account_securitygroup_template, #add_more_account_securitygroup").attr("disabled", "disabled"); 
	        cleanErrMsg($dialogAddIngressRule.find(".account_securitygroup_template").find("#account"), $dialogAddIngressRule.find(".account_securitygroup_template").find("#account_securitygroup_template_errormsg")); 
    		cleanErrMsg($dialogAddIngressRule.find(".account_securitygroup_template").find("#securitygroup"), $dialogAddIngressRule.find(".account_securitygroup_template").find("#account_securitygroup_template_errormsg")); 	        	        
	    }
	    else if($dialogAddIngressRule.find("input[name='ingress_rule_type']:checked").val() == "account_securitygroup") {
	        //enable Account/Network Group
	        $dialogAddIngressRule.find(".account_securitygroup_template, #add_more_account_securitygroup").removeAttr("disabled");	
	        
	        //disable CIDR, clear up error fields
	        $dialogAddIngressRule.find(".cidr_template, #add_more_cidr").attr("disabled", "disabled");   
	        cleanErrMsg($dialogAddIngressRule.find(".cidr_template").find("#cidr"), $dialogAddIngressRule.find(".cidr_template").find("#cidr_errormsg")); 		              
	    }
	});
		
	$dialogAddIngressRule.find("#protocol").unbind("change").bind("change", function(event){		    
	    var thisDropDown = $(this);		   
	    if(thisDropDown.val() == "ICMP") {		        
	        $dialogAddIngressRule.find("#icmp_type_container, #icmp_code_container").show();
	        $dialogAddIngressRule.find("#icmp_type, #icmp_code").val("-1");
	        
	        $dialogAddIngressRule.find("#start_port_container, #end_port_container").hide();		        
	        $dialogAddIngressRule.find("#start_port, #end_port").val("");	
	        cleanErrMsg($dialogAddIngressRule.find("#start_port"), $dialogAddIngressRule.find("#start_port_errormsg"));	  
	        cleanErrMsg($dialogAddIngressRule.find("#end_port"), $dialogAddIngressRule.find("#end_port_errormsg"));	                
	    }
	    else {  //TCP, UDP
	        $dialogAddIngressRule.find("#start_port_container, #end_port_container").show();	 
	        
	        $dialogAddIngressRule.find("#icmp_type_container, #icmp_code_container").hide();
	        $dialogAddIngressRule.find("#icmp_type, #icmp_code").val("");
	        cleanErrMsg($dialogAddIngressRule.find("#icmp_type"),$dialogAddIngressRule.find("#icmp_type_errormsg"));	     
	        cleanErrMsg($dialogAddIngressRule.find("#icmp_code"),$dialogAddIngressRule.find("#icmp_code_errormsg"));	
	    }		
	    return false;
	});	   
	
	//add button ***        
    $("#add_ingressrule_button").unbind("click").bind("click", function(event) {   
        if($("#tab_ingressrule").hasClass("off"))
            $("#tab_ingressrule").click();   
        
        $dialogAddIngressRule.find("#spinning_wheel").hide();
	    $dialogAddIngressRule.find("#info_container").hide();
         
        $dialogAddIngressRule.find("#protocol").val("TCP");
        $dialogAddIngressRule.find("#protocol").change();		        
        
        $dialogAddIngressRule.find("#start_port").val("");
        cleanErrMsg($dialogAddIngressRule.find("#start_port"), $dialogAddIngressRule.find("#start_port_errormsg"));
        
        $dialogAddIngressRule.find("#end_port").val("");
        cleanErrMsg($dialogAddIngressRule.find("#end_port"), $dialogAddIngressRule.find("#end_port_errormsg"));
       
        $dialogAddIngressRule.find("input[name='ingress_rule_type']").change();
        
        $dialogAddIngressRule.find("#cidr_container").empty();
        $dialogAddIngressRule.find("#add_more_cidr").click();
        		        
        $dialogAddIngressRule.find("#account_securitygroup_container").empty();
        $dialogAddIngressRule.find("#add_more_account_securitygroup").click();
        		        
        		        
        $("#dialog_add_ingress_rule")
        .dialog('option', 'buttons', { 			    
            "Add": function() { 
                var $thisDialog = $(this);		  
                $thisDialog.find("#info_container").hide();  	
		    	var protocol = $thisDialog.find("#protocol").val();
		    	          										
                // validate values 					
	            var isValid = true;		
	            		
	            if(protocol == "ICMP") {					
	                isValid &= validateInteger("Type", $thisDialog.find("#icmp_type"), $thisDialog.find("#icmp_type_errormsg"), -1, 40, false);	//required	
	                isValid &= validateInteger("Code", $thisDialog.find("#icmp_code"), $thisDialog.find("#icmp_code_errormsg"), -1 , 15, false);	//required
	            }	
	            else {  //TCP, UDP
	                isValid &= validateInteger("Start Port", $thisDialog.find("#start_port"), $thisDialog.find("#start_port_errormsg"), 1, 65535, false);	//required	
	                isValid &= validateInteger("End Port", $thisDialog.find("#end_port"), $thisDialog.find("#end_port_errormsg"), 1, 65535, false);	//required
	            }
	            				          	
	            if($thisDialog.find("input[name='ingress_rule_type']:checked").val() == "cidr") {					                
	                isValid &= validateCIDR("CIDR", $thisDialog.find(".cidr_template").eq(0).find("#cidr"), $thisDialog.find(".cidr_template").eq(0).find("#cidr_errormsg"), false); //required                
                    for(var i=1; i<$thisDialog.find(".cidr_template").length; i++)
                        isValid &= validateCIDR("CIDR", $thisDialog.find(".cidr_template").eq(i).find("#cidr"), $thisDialog.find(".cidr_template").eq(0).find("#cidr_errormsg"), true); //optional        
                }
                else if($thisDialog.find("input[name='ingress_rule_type']:checked").val() == "account_securitygroup") {			                        
                    isValid &= validateString("Account", $thisDialog.find(".account_securitygroup_template").eq(0).find("#account"), $thisDialog.find(".account_securitygroup_template").eq(0).find("#account_securitygroup_template_errormsg"), false);	 //required                
                    isValid &= validateString("Network Group", $thisDialog.find(".account_securitygroup_template").eq(0).find("#securitygroup"), $thisDialog.find(".account_securitygroup_template").eq(0).find("#account_securitygroup_template_errormsg"), false);	 //required             
                    for(var i=1; i<$thisDialog.find(".account_securitygroup_template").length; i++) {
                        isValid &= validateString("Account", $thisDialog.find(".account_securitygroup_template").eq(i).find("#account"), $thisDialog.find(".account_securitygroup_template").eq(0).find("#account_securitygroup_template_errormsg"), true);	 //optional          
                        isValid &= validateString("Network Group", $thisDialog.find(".account_securitygroup_template").eq(i).find("#securitygroup"), $thisDialog.find(".account_securitygroup_template").eq(0).find("#account_securitygroup_template_errormsg"), true);	 //optional     
                    }
                }					            
	            if (!isValid) 
	                return;					
				
				$thisDialog.find("#spinning_wheel").show();    
							
		    	var securityGroupObj = $currentMidmenuItem.data("jsonObj");	
		    	var securityGroupId = securityGroupObj.id;
		    	var domainId = securityGroupObj.domainid;
		    	var account = securityGroupObj.account;    	
		    	var securityGroupName = securityGroupObj.name;	
		    	
		    	var moreCriteria = [];			    	    	
		    	moreCriteria.push("&domainid=" + domainId);
                moreCriteria.push("&account=" + account);
                //moreCriteria.push("&securitygroupname=" + securityGroupName);   
                moreCriteria.push("&securitygroupid=" + securityGroupId);   
                  	    	
	            if (protocol!=null && protocol.length > 0) 
		            moreCriteria.push("&protocol="+encodeURIComponent(protocol));	
		            	  				          							
				if(protocol == "ICMP") {        					    
				    var icmpType = $thisDialog.find("#icmp_type").val();	
				    if (icmpType!=null && icmpType.length > 0)         					        
		                moreCriteria.push("&icmptype="+encodeURIComponent(icmpType));						            
		            var icmpCode = $thisDialog.find("#icmp_code").val();
	                if (icmpCode!=null && icmpCode.length > 0) 				                    
		                moreCriteria.push("&icmpcode="+encodeURIComponent(icmpCode)); 					              					    
				}
				else {  //TCP, UDP
				    var startPort = $thisDialog.find("#start_port").val();	
				    if (startPort!=null && startPort.length > 0) 
		                moreCriteria.push("&startport="+encodeURIComponent(startPort));	
		            var endPort = $thisDialog.find("#end_port").val();
	                if (endPort!=null && endPort.length > 0) 
		                moreCriteria.push("&endport="+encodeURIComponent(endPort));	
				}        					            
	            				            				                                      
                if($dialogAddIngressRule.find("input[name='ingress_rule_type']:checked").val() == "cidr") {	
                    var array1 = [];	        
                    var cidrElementArray = $dialogAddIngressRule.find(".cidr_template").find("#cidr");			                        
                    for(var i=0; i<cidrElementArray.length; i++) {
                        if(cidrElementArray[i].value.length >  0)
                            array1.push(cidrElementArray[i].value);
                    }
                    if(array1.length > 0)
	                    moreCriteria.push("&cidrlist="+encodeURIComponent(array1.join(",")));	
                }	    
                else if($dialogAddIngressRule.find("input[name='ingress_rule_type']:checked").val() == "account_securitygroup") {			                          
                    var accountElementArray = $dialogAddIngressRule.find(".account_securitygroup_template").find("#account");	
                    var securitygroupElementArray = $dialogAddIngressRule.find(".account_securitygroup_template").find("#securitygroup");			                        
                    for(var i=0; i<accountElementArray.length; i++)	{	  
                        if(securitygroupElementArray[i].value.length > 0 && accountElementArray[i].value.length > 0)                        
                            moreCriteria.push("&usersecuritygrouplist["+i+"].account="+accountElementArray[i].value+"&usersecuritygrouplist["+i+"].group="+securitygroupElementArray[i].value);
                    }
                }	  		           
	             
                $.ajax({
	                data: createURL("command=authorizeSecurityGroupIngress"+moreCriteria.join("")),
			        dataType: "json",
			        success: function(json) {					            		            				
			            var jobId = json.authorizesecuritygroupingress.jobid; 						            	                   
	                    var timerKey = "ingressRuleJob_"+jobId;	                    					        
				        $("body").everyTime(
					        5000,
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
									        if (result.jobstatus == 1) { // Succeeded	
									            $thisDialog.find("#spinning_wheel").hide();				        
				                                $thisDialog.dialog("close");									        
									            var items = result.jobresult.securitygroup.ingressrule;	
									                                					
					                            var $subgridItem = $("#ingressrule_tab_template").clone(true);	 
					                            securityGroupIngressRuleJSONToTemplate(items[0], $subgridItem).data("parentObj", securityGroupObj); 
									            $subgridItem.find("#after_action_info").text(g_dictionary["label.adding.succeeded"]);
                                                $subgridItem.find("#after_action_info_container").removeClass("error").addClass("success").show();  
                                                $("#right_panel_content").find("#tab_content_ingressrule").find("#tab_container").prepend($subgridItem.show());  
									             
									            if(items.length > 1) {                               
                                                    for(var i=1; i<items.length; i++) {                                                                                           
                                                        var $subgridItem = $("#ingressrule_tab_template").clone(true);	 
					                                    securityGroupIngressRuleJSONToTemplate(items[i], $subgridItem).data("parentObj", securityGroupObj);													            											            
									                    $subgridItem.find("#after_action_info").text(g_dictionary["label.adding.succeeded"]);
                                                        $subgridItem.find("#after_action_info_container").removeClass("error").addClass("success").show();  
                                                        $("#right_panel_content").find("#tab_content_ingressrule").find("#tab_container").prepend($subgridItem.show());                                                                                                                       
                                                    }                                    
                                                } 								                                                                                                               												
									        } else if (result.jobstatus == 2) { // Failed									            
									            //var errorMsg = g_dictionary["label.failed"] + " - " + g_dictionary["label.error.code"] + " " + fromdb(result.jobresult.errorcode);
									            var errorMsg = g_dictionary["label.failed"] + " - " + fromdb(result.jobresult.errortext);	
									            handleErrorInDialog2(errorMsg, $thisDialog);									            
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
					        0
				        );				        
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
    });	 
}

function doDeleteSecurityGroup($actionLink, $detailsTab, $midmenuItem1) {       
    var jsonObj = $midmenuItem1.data("jsonObj");
	var id = jsonObj.id;
		
	$("#dialog_confirmation")
	.text(dictionary["message.action.delete.security.group"])
	.dialog('option', 'buttons', { 					
		"Confirm": function() { 			
			$(this).dialog("close");			
			var apiCommand = "command=deleteSecurityGroup&id="+id;
            doActionToTab(id, $actionLink, apiCommand, $midmenuItem1, $detailsTab);	
		}, 
		"Cancel": function() { 
			$(this).dialog("close"); 
		}
	}).dialog("open");
}

function securityGroupToMidmenu(jsonObj, $midmenuItem1) {  
    $midmenuItem1.attr("id", getMidmenuId(jsonObj));  
    $midmenuItem1.data("jsonObj", jsonObj); 
       
    var $iconContainer = $midmenuItem1.find("#icon_container").show();   
    $iconContainer.find("#icon").attr("src", "images/midmenuicon_securityGroup.png");	
    
    var firstRowText = fromdb(jsonObj.name);
    $midmenuItem1.find("#first_row").text(firstRowText.substring(0,midMenuFirstRowLength));     
    $midmenuItem1.find("#first_row_container").attr("title", firstRowText);   
    
    var secondRowText = fromdb(jsonObj.account);
    $midmenuItem1.find("#second_row").text(secondRowText.substring(0,midMenuSecondRowLength));
    $midmenuItem1.find("#second_row_container").attr("title", secondRowText);  
}

function securityGroupToRightPanel($midmenuItem1) {
    copyActionInfoFromMidMenuToRightPanel($midmenuItem1);
    $("#right_panel_content").data("$midmenuItem1", $midmenuItem1);
    $("#tab_details").click();   
}

function securityGroupJsonToDetailsTab() {     
    var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");
    if($midmenuItem1 == null) {
        securityGroupClearDetailsTab();
        return;
    }
    
    var jsonObj = $midmenuItem1.data("jsonObj");
    if(jsonObj == null) {
        securityGroupClearDetailsTab();
        return;
    }
     
    var $thisTab = $("#right_panel_content #tab_content_details");  
    $thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();   
            
    $.ajax({
        data: createURL("command=listSecurityGroups&id="+jsonObj.id),
        dataType: "json",
        async: false,
        success: function(json) {  
            var items = json.listsecuritygroupsresponse.securitygroup;                   
            if(items != null && items.length > 0) {
                jsonObj = items[0];
                $midmenuItem1.data("jsonObj", jsonObj);  
            }
        }
    });   
    
    $thisTab.find("#id").text(fromdb(jsonObj.id));     
    $thisTab.find("#grid_header_title").text(fromdb(jsonObj.name));
    $thisTab.find("#name").text(fromdb(jsonObj.name)); 
    $thisTab.find("#displaytext").text(fromdb(jsonObj.description));       
    $thisTab.find("#domain").text(fromdb(jsonObj.domain));   
    $thisTab.find("#account").text(fromdb(jsonObj.account));   
        
    //actions ***
    var $actionMenu = $("#right_panel_content #tab_content_details #action_link #action_menu");
    $actionMenu.find("#action_list").empty();  
    buildActionLinkForTab("label.action.delete.security.group", securityGroupActionMap, $actionMenu, $midmenuItem1, $thisTab);	  
    
    $thisTab.find("#tab_spinning_wheel").hide();    
    $thisTab.find("#tab_container").show();         
}

function securityGroupJsonToIngressRuleTab() {       		
	var $midmenuItem1 = $("#right_panel_content").data("$midmenuItem1");	
	if($midmenuItem1 == null) {
	    securityGroupClearIngressRuleTab();
	    return;
	}
	
	var securityGroupObj = $midmenuItem1.data("jsonObj");	
	if(securityGroupObj == null) {
	    securityGroupClearIngressRuleTab();
	    return;
	}
	
	var $thisTab = $("#right_panel_content").find("#tab_content_ingressrule");	    
	$thisTab.find("#tab_container").hide(); 
    $thisTab.find("#tab_spinning_wheel").show();   
  
    $.ajax({
		cache: false,		
		data: createURL("command=listSecurityGroups&id="+securityGroupObj.id),
		dataType: "json",
		success: function(json) {	
		    var securityGroupObj = json.listsecuritygroupsresponse.securitygroup[0];		    				    
			var items = securityGroupObj.ingressrule;    
			var $container = $thisTab.find("#tab_container").empty();     																			
			if (items != null && items.length > 0) {			    
				var template = $("#ingressrule_tab_template");				
				for (var i = 0; i < items.length; i++) {
					var newTemplate = template.clone(true);	               
	                securityGroupIngressRuleJSONToTemplate(items[i], newTemplate).data("parentObj", securityGroupObj); 
	                $container.append(newTemplate.show());	
				}			
			}	
			$thisTab.find("#tab_spinning_wheel").hide();    
            $thisTab.find("#tab_container").show();    			
		}
	});
} 

function securityGroupClearIngressRuleTab() {   	
	var $thisTab = $("#right_panel_content").find("#tab_content_ingressrule");	    
	$thisTab.find("#tab_container").empty();  
} 

function securityGroupIngressRuleJSONToTemplate(jsonObj, $template) {
    $template.data("jsonObj", jsonObj);     
    $template.attr("id", "securitygroup_ingressRule_"+fromdb(jsonObj.id));   
     
    $template.find("#grid_header_title").text(fromdb(jsonObj.ruleid));			   
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
    
    // actions	
	var $actionLink = $template.find("#action_link");
	bindActionLink($actionLink);
		
	var $actionMenu = $actionLink.find("#action_menu");
    $actionMenu.find("#action_list").empty();	
        
    buildActionLinkForSubgridItem("label.action.delete.ingress.rule", securityGroupIngressRuleActionMap, $actionMenu, $template);	
    
    return $template;   
} 

var securityGroupIngressRuleActionMap = {      
    "label.action.delete.ingress.rule": {      
        isAsyncJob: true,
        asyncJobResponse: "revokesecuritygroupingress",
		dialogBeforeActionFn : doDeleteIngressRule,
        inProcessText: "label.action.delete.ingress.rule.processing",
        afterActionSeccessFn: function(json, id, $subgridItem) {                 
            $subgridItem.slideUp("slow", function() {
                $(this).remove();
            });
        }
    }    
}  

function doDeleteIngressRule($actionLink, $subgridItem) {
	$("#dialog_info")	
	.text(dictionary["message.action.delete.ingress.rule"])
    .dialog('option', 'buttons', { 						
	    "Confirm": function() { 
		    $(this).dialog("close"); 	
			var securityGroupObj = $subgridItem.data("parentObj");
			var ingressRuleObj = $subgridItem.data("jsonObj");
            var id = ingressRuleObj.ruleid;
                       
            var moreCriteria = [];		 
	        moreCriteria.push("&domainid="+securityGroupObj.domainid);    	    	        
	        moreCriteria.push("&account="+securityGroupObj.account);    	    		    	        
	        moreCriteria.push("&id="+id);    
	      						
			var apiCommand = "command=revokeSecurityGroupIngress"+moreCriteria.join("");                  
            doActionToSubgridItem(id, $actionLink, apiCommand, $subgridItem); 
	    }, 
	    "Cancel": function() { 
		    $(this).dialog("close"); 
			
	    } 
    }).dialog("open");
}

function securityGroupClearRightPanel() {
    securityGroupClearDetailsTab();
}

function securityGroupClearDetailsTab() {
    var $thisTab = $("#right_panel_content").find("#tab_content_details");     
    $thisTab.find("#id").text("");    
    $thisTab.find("#grid_header_title").text("");
    $thisTab.find("#name").text("");
    $thisTab.find("#name_edit").val("");    
    $thisTab.find("#displaytext").text("");
    $thisTab.find("#displaytext_edit").val("");    
    $thisTab.find("#disksize").text("");
    $thisTab.find("#tags").text("");   
    $thisTab.find("#domain").text("");  
    $thisTab.find("#account").text("");   
    
    var $actionMenu = $("#right_panel_content #tab_content_details #action_link #action_menu");
    $actionMenu.find("#action_list").empty(); 
    $actionMenu.find("#action_list").append($("#no_available_actions").clone().show());
}

var securityGroupActionMap = {       
    "label.action.delete.security.group": {   
        isAsyncJob: false,  
        dialogBeforeActionFn : doDeleteSecurityGroup,               
        inProcessText: "Deleting Security Group....",
        afterActionSeccessFn: function(json, $midmenuItem1, id) {  
			$midmenuItem1.remove(); 
            if(id.toString() == $("#right_panel_content").find("#tab_content_details").find("#id").text()) {
                clearRightPanel();
                securityGroupClearRightPanel();
            }                           
        }
    }    
}  