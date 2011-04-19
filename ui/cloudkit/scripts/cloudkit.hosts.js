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
$(document).ready(function() {
	if (g_loginResponse == null) {
		logout();
		return;
	}
	
	if (g_loginResponse.registered == "false") {
		$("#registration_complete_link").attr("href","https://my.rightscale.com/cloud_registrations/cloudkit/new?callback_url="+encodeURIComponent("http://localhost:8080/client/cloudkit/complete?token="+g_loginResponse.registrationtoken));
		$("#registration_complete_container").show();
	} else {
		$("#registration_complete_container").hide();
	}
	
	var hostTemplate = $("#host_template");
	var hostContainer = $("#host_container").empty();
	$.ajax({
		data: createURL("command=listHosts"),				
		success: function(json) {	
			var hosts = json.listhostsresponse.host;
			if (hosts != null && hosts.length >0) {
				for (var i = 0; i < hosts.length; i++) {
					var host = hosts[i];
					var template = hostTemplate.clone(true);
					template.find("#hostname").text(host.name);
					template.find("#ip").text(host.ipaddress);
					template.find("#version").text(host.version);
					template.find("#disconnected").text(host.disconnected);
					template.find("#state").text(host.state);
					if (host.state != 'Up') {
						template.find("#state").removeClass("green").addClass("red");
					}
					hostContainer.append(template.show());
				}
			}
		}
	});	
	
	$("#main").show();
});


