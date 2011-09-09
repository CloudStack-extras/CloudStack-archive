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

function afterLoadDashboardJSP() {
    var $alertTemplate = $("#alert_template");
    
    if (isAdmin()) {
        var $thisTab = showDashboard("dashboard_admin");
              
        $thisTab.find("#general_alerts").find("#more_icon").unbind("click").bind("click", function(event) {					
		    $("#leftmenu_events").click();
            $("#leftmenu_alert").click();
		    return false;
		});
        
		var sessionExpired = false;
		var zones = null;
		var noZones = false;
		var noPods = true;
		//$("#menutab_dashboard_root, #menutab_vm, #menutab_networking_old, #menutab_networking, #menutab_templates, #menutab_events, #menutab_hosts, #menutab_storage, #menutab_accounts, #menutab_domain").hide();							
   
        $.ajax({
		    data: createURL("command=listZones&available=true"),
			dataType: "json",
			async: false,
			success: function(json) {
				zones = json.listzonesresponse.zone;
				var zoneSelect = $thisTab.find("#capacity_zone_select").empty();	
				if (zones != null && zones.length > 0) {
					for (var i = 0; i < zones.length; i++) {
						zoneSelect.append("<option value='" + zones[i].id + "'>" + fromdb(zones[i].name) + "</option>"); 								
						if(noPods == true) {
						    $.ajax({
						        data: createURL("command=listPods&zoneId="+zones[i].id),
				                dataType: "json",
				                async: false,
				                success: function(json) {
					                var pods = json.listpodsresponse.pod;						
					                if (pods != null && pods.length > 0) {
    							        noPods = false;
    							        //$("#menutab_dashboard_root, #menutab_vm, #menutab_networking_old, #menutab_networking, #menutab_templates, #menutab_events, #menutab_hosts, #menutab_storage, #menutab_accounts, #menutab_domain").show();							
					                }							
				                }
			                });
						}
					}
				} else {							
					noZones = true;
				}
			}
		});
						
		if (sessionExpired) 
		    return false;
		    
		if (noZones || noPods) {
//			$("#tab_dashboard_user").hide();
//			$("#menutab_role_user").hide();
//			$("#menutab_role_root").show();
//			$("#menutab_configuration").click();
			return false;
		}
		
		var capacities = null;
		$.ajax({
			cache: false,
			async: false,
			data: createURL("command=listCapacity"),
			dataType: "json",
			success: function(json) {
				capacities = json.listcapacityresponse.capacity;
			}
		});
		
		$thisTab.find("#capacity_pod_select").bind("change", function(event) {		    
		    event.stopPropagation();		    
		    var selectedZone = $thisTab.find("#capacity_zone_select option:selected").text();
			var selectedPod = $thisTab.find("#capacity_pod_select").val();
			
			// Reset to Defaults			
			var $capacityContainer = $thisTab.find("#system_wide_capacity_container");
			$capacityContainer.find("#capacityused").text("N");
		    $capacityContainer.find("#capacitytotal").text("A");
		    $capacityContainer.find("#percentused").text("");		
		    $capacityContainer.find("#bar_chart").removeClass().addClass("db_barbox").css("width", "0%");    
						
			if (capacities != null && capacities.length > 0) {
				for (var i = 0; i < capacities.length; i++) {
					var capacity = capacities[i];
					if (capacity.zonename == selectedZone) {										
						// ***** Public IPs Addresses *****
						if (capacity.type == "4") {
						    var $c = $capacityContainer.find("#public_ip_address");
						    $c.find("#capacityused").text(capacity.capacityused);
						    $c.find("#capacitytotal").text(capacity.capacitytotal);						    
						    capacityBarChart($c, capacity.percentused);							
						} 						
						
						// ***** Secondary Storage Used *****
						else if (capacity.type == "6") {
						    var $c = $capacityContainer.find("#secondary_storage_used");
						    $c.find("#capacityused").text(convertBytes(parseInt(capacity.capacityused)));
						    $c.find("#capacitytotal").text(convertBytes(parseInt(capacity.capacitytotal)));						    
						    capacityBarChart($c, capacity.percentused);						    
						} 
						
						else {						    
							if (capacity.podname == selectedPod) {							    
								// ***** Memory Allocated *****
								if (capacity.type == "0") {
								    var $c = $capacityContainer.find("#memory_allocated");
						            $c.find("#capacityused").text(convertBytes(parseInt(capacity.capacityused)));
						            $c.find("#capacitytotal").text(convertBytes(parseInt(capacity.capacitytotal)));						            
								    capacityBarChart($c, capacity.percentused);								    
								} 
																
								// ***** CPU *****
								else if (capacity.type == "1") {
								    var $c = $capacityContainer.find("#cpu");
						            $c.find("#capacityused").text(convertHz(parseInt(capacity.capacityused)));
						            $c.find("#capacitytotal").text(convertHz(parseInt(capacity.capacitytotal)));						            
								    capacityBarChart($c, capacity.percentused);								    						
								} 
																
								// ***** Primary Storage Used *****
								else if (capacity.type == "2") {
								    var $c = $capacityContainer.find("#primary_storage_used");
						            $c.find("#capacityused").text(convertBytes(parseInt(capacity.capacityused)));
						            $c.find("#capacitytotal").text(convertBytes(parseInt(capacity.capacitytotal)));						            
						            capacityBarChart($c, capacity.percentused);								   
								} 
																
								// ***** Primary Storage Allocated *****
								else if (capacity.type == "3") {
								    var $c = $capacityContainer.find("#primary_storage_allocated");
						            $c.find("#capacityused").text(convertBytes(parseInt(capacity.capacityused)));
						            $c.find("#capacitytotal").text(convertBytes(parseInt(capacity.capacitytotal)));						            
						            capacityBarChart($c, capacity.percentused);								   
								} 
																
								// ***** Private IP Addresses *****
								else if (capacity.type == "5") {								
								    var $c = $capacityContainer.find("#private_ip_address");
						            $c.find("#capacityused").text(capacity.capacityused);
						            $c.find("#capacitytotal").text(capacity.capacitytotal);						            
								    capacityBarChart($c, capacity.percentused);								    							
								}	
							}
						}
					}
				}
			}
		});	
				
        $thisTab.find("#capacity_zone_select").bind("change", function(event) {
			var zoneId = $(this).val();
			$.ajax({
			    data: createURL("command=listPods&zoneId="+zoneId),
				dataType: "json",
				async: false,
				success: function(json) {
					var pods = json.listpodsresponse.pod;
					var podSelect = $thisTab.find("#capacity_pod_select").empty();	
					if (pods != null && pods.length > 0) {
						podSelect.append("<option value='All'>All pods</option>"); 
					    for (var i = 0; i < pods.length; i++) {
						    podSelect.append("<option value='" + pods[i].name + "'>" + fromdb(pods[i].name) + "</option>"); 
					    }
					}
					$thisTab.find("#capacity_pod_select").change();
				}
			});
		});
		$thisTab.find("#capacity_zone_select").change();
		
		// General Alerts			
		$.ajax({
		    data: createURL("command=listAlerts"),
			dataType: "json",
			success: function(json) {
				var alerts = json.listalertsresponse.alert;
				if (alerts != null && alerts.length > 0) {
					var alertGrid = $thisTab.find("#alert_grid_content").empty();
					var length = (alerts.length>=5) ? 5 : alerts.length;					
					for (var i = 0; i < length; i++) {
						var template = $alertTemplate.clone(true).attr("id", "alert_template_clone");
						template.find("#type").text(toAlertType(alerts[i].type));
						template.find("#description").append(fromdb(alerts[i].description));											
						setDateField(alerts[i].sent, template.find("#date"));															
						alertGrid.append(template.show());
					}
				}
			}
		});
						
		// Hosts Alerts
		$.ajax({
		    data: createURL("command=listHosts&state=Alert"),
			dataType: "json",
			success: function(json) {
				var alerts = json.listhostsresponse.host;
				if (alerts != null && alerts.length > 0) {
					var alertGrid = $thisTab.find("#host_alert_grid_content").empty();
					var length = (alerts.length>=4) ? 4 : alerts.length;
					for (var i = 0; i < length; i++) {
						var template = $alertTemplate.clone(true).attr("id", "alert_template_clone");
						template.find("#type").text("Host - Alert State");
						template.find("#description").append("Host - <b>" + fromdb(alerts[i].name) + "</b> has been detected in Alert state.");								
						setDateField(alerts[i].disconnected, template.find("#date"));											
						alertGrid.append(template.show());
					}
				}
			}
		});		
		
	} 
	else if (isDomainAdmin()) {
	    var $thisTab = showDashboard("dashboard_domainadmin");
	      
	    $.ajax({
		    data: createURL("command=listVirtualMachines"),
			dataType: "json",
			success: function(json) {
				if (json.listvirtualmachinesresponse.virtualmachine != undefined)
					$thisTab.find("#instance_total").text(json.listvirtualmachinesresponse.virtualmachine.length);
			}
		});
		
		$.ajax({
		    data: createURL("command=listVolumes"),
			dataType: "json",
			success: function(json) {
				if (json.listvolumesresponse.volume)
					$thisTab.find("#volume_total").text(json.listvolumesresponse.volume.length);
			}
		});
		
		$.ajax({
		    data: createURL("command=listSnapshots"),
			dataType: "json",
			success: function(json) {
				if (json.listsnapshotsresponse.snapshot)
					$thisTab.find("#snapshot_total").text(json.listsnapshotsresponse.snapshot.length);
			}
		});
		
		$.ajax({
		    data: createURL("command=listAccounts"),
			dataType: "json",
			success: function(json) {
				if (json.listaccountsresponse.account)
					$thisTab.find("#account_total").text(json.listaccountsresponse.account.length);
			}
		}); 
				
		// Recent Errors
		$.ajax({
		    data: createURL("command=listEvents&level=ERROR"),
			dataType: "json",
			success: function(json) {
				var items = json.listeventsresponse.event;
				if (items != null && items.length > 0) {
					var $grid = $thisTab.find("#alert_grid_content").empty();
					var length = (items.length>=3) ? 3 : items.length;
					for (var i = 0; i < length; i++) {							    
					    var $template = $alertTemplate.clone(true).attr("id", "alert_template_clone");
				        $template.find("#type").text(items[i].type);
				        $template.find("#description").append(fromdb(items[i].description));											
				        setDateField(items[i].created, $template.find("#date"));															
				        $grid.append($template.show());							    
					}
				}
			}
		});
	} 
	else if(isUser()) {	
	    var $thisTab = showDashboard("dashboard_user");
	    	    	    
	    //$thisTab.find("#launch_test").hide();
		$.ajax({
			cache: false,
			data: createURL("command=listAccounts"),
			dataType: "json",
			success: function(json) {
			    var accounts = json.listaccountsresponse.account;						
				if (accounts != null && accounts.length > 0) {
				    var statJSON = accounts[0];
				    /*
				    var sent = parseInt(statJSON.sentbytes);
				    var rec = parseInt(statJSON.receivedbytes);
					
					if(sent==0 && rec==0)
					    $thisTab.find("#network_bandwidth_panel").hide();
					else
					    $thisTab.find("#network_bandwidth_panel").show();
					
				    $("#menutab_role_user").show();
				    $("#menutab_role_root").hide();
					$("#menutab_role_domain").hide();
				    $("#tab_dashboard_user").show();
				    $("#tab_dashboard_root, #tab_dashboard_domain, #loading_gridtable").hide();
					
				    // This is in bytes, so let's change to KB
				    sent = Math.round(sent / 1024);
				    rec = Math.round(rec / 1024);
				    $thisTab.find("#db_sent").text(sent + "KB");
				    $thisTab.find("#db_received").text(rec + "KB");
				    */
				    $thisTab.find("#db_available_public_ips").text(statJSON.ipavailable);
				    $thisTab.find("#db_owned_public_ips").text(statJSON.iptotal);				    
				    $thisTab.find("#db_running_vms").text(statJSON.vmrunning);
				    $thisTab.find("#db_stopped_vms").text(statJSON.vmstopped);
				    $thisTab.find("#db_total_vms").text(statJSON.vmtotal);
				    //$thisTab.find("#db_avail_vms").text(statJSON.vmavailable);						   
				    $thisTab.find("#db_account_id").text(statJSON.id);
				    $thisTab.find("#db_account").text(statJSON.name);						    
				    $thisTab.find("#db_type").text(toRole(statJSON.accounttype));
				    $thisTab.find("#db_domain").text(statJSON.domain);						    			   
				}
				
				// Recent Errors
				$.ajax({
				    data: createURL("command=listEvents&level=ERROR"),
					dataType: "json",
					success: function(json) {
						var items = json.listeventsresponse.event;
						if (items != null && items.length > 0) {
							var $grid = $thisTab.find("#alert_grid_content").empty();
							var length = (items.length>=3) ? 3 : items.length;
							for (var i = 0; i < length; i++) {							    
							    var $template = $alertTemplate.clone(true).attr("id", "alert_template_clone");
						        $template.find("#type").text(items[i].type);
						        $template.find("#description").append(fromdb(items[i].description));											
						        setDateField(items[i].created, $template.find("#date"));															
						        $grid.append($template.show());							    
							}
						}
					}
				});
			},					
			beforeSend: function(XMLHttpRequest) {
				return true;
			}	
		});	      
	} 
	else { //no role 
	    logout(false);	    
	}
}

function showDashboard(dashboardToShow) {
    var $thisTab;
    var allDashboards = ["dashboard_admin", "dashboard_domainadmin", "dashboard_user"];
    for(var i=0; i < allDashboards.length; i++) {
        dashboard = allDashboards[i];
        if(dashboard == dashboardToShow)
            $thisTab = $("#"+dashboard).show();
        else
            $("#"+dashboard).hide();        
    }    
    return $thisTab;
}

//*** dashboard admin (begin) ***
function capacityBarChart($capacity, percentused) { // e.g. percentused == "51.27" (no % inside)
    
    var percentusedFloat = parseFloat(percentused); // e.g. percentusedFloat == 51.27  
    var percentusedRound = Math.round(percentusedFloat); //e.g. percentusedRound == 51
    
    var percentused2 = (percentusedRound.toString() + "%"); //e.g. percentused2 == "51%"
    $capacity.find("#percentused").text(percentused2);
     
    //e.g. percentusedRound == 51    
    if (percentusedRound <= 60)
        $capacity.find("#bar_chart").removeClass().addClass("db_barbox low").css("width", percentused2); 
    else if (percentusedRound > 60 && percentusedRound <= 80 )
        $capacity.find("#bar_chart").removeClass().addClass("db_barbox mid").css("width", percentused2);
    else if (percentusedRound > 80 )
        $capacity.find("#bar_chart").removeClass().addClass("db_barbox high").css("width", percentused2);
}
//*** dashboard admin (end) ***

