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
var ERROR_ACCESS_DENIED_DUE_TO_UNAUTHORIZED = 401;
var ERROR_INTERNET_NAME_NOT_RESOLVED = 12007;
var ERROR_INTERNET_CANNOT_CONNECT = 12029;
var ERROR_VMOPS_ACCOUNT_ERROR = 531;

var g_loginResponse = null;
var g_mySession = null;
$.urlParam = function(name){ var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href); if (!results) { return 0; } return results[1] || 0;}
 
function logout() {
	window.location='/client/cloudkit/login.jsp';
	g_loginResponse = null;
	g_mySession = null
	$.cookie('loginResponse', null);
	return true;
}

function login(json) {
	g_loginResponse = json.loginresponse;
	g_mySession = $.cookie("JSESSIONID");
	$.cookie('loginResponse', JSON.stringify(g_loginResponse), { expires: 1});
	window.location='/client/cloudkit/cloudkit.jsp';
}

function createURL(url) {
    return url +"&response=json&sessionkey=" + encodeURIComponent(g_loginResponse.sessionkey);
}

function handleError(XMLHttpResponse, handleErrorCallback) {
	// User Not authenticated
	if (XMLHttpResponse.status == ERROR_ACCESS_DENIED_DUE_TO_UNAUTHORIZED) {
		logout();
		//$("#dialog_session_expired").dialog("open");
	} 	
	else if (XMLHttpResponse.status == ERROR_INTERNET_NAME_NOT_RESOLVED) {
		alert("Unable to resolve the DNS name");
		//$("#dialog_error_internet_not_resolved").dialog("open");
	} 
	else if (XMLHttpResponse.status == ERROR_INTERNET_CANNOT_CONNECT) {
		alert("Unable to reach the internet");
		//$("#dialog_error_management_server_not_accessible").dialog("open");
	} 
	else if (XMLHttpResponse.status == ERROR_VMOPS_ACCOUNT_ERROR && handleErrorCallback != undefined) {
		handleErrorCallback();
	} 
	else if (handleErrorCallback != undefined) {
		handleErrorCallback();
	}
	else {
		var errorMsg = parseXMLHttpResponse(XMLHttpResponse);				
		alert(errorMsg);
		//$("#dialog_error").text(fromdb(errorMsg)).dialog("open");
	}
}

$(document).ready(function() {
	$.ajaxSetup({
		url: "/client/api",
		type: "GET",
		dataType: "json",
		async: false,
		cache: false,
		error: function(XMLHttpResponse) {
			handleError(XMLHttpResponse);
		},
		beforeSend: function(XMLHttpRequest) {
			if (g_mySession == $.cookie("JSESSIONID")) {
				return true;
			} else {
				alert(g_mySession + " == " + $.cookie("JSESSIONID"));
				logout();
				return false;
			}
		}		
	});
	
	// Setup tab clicks
	$("#tab_hosts").bind("click", function(event) {
		$(this).removeClass("off").addClass("on");
		$("#tab_docs").removeClass("on").addClass("off");
		$("#tab_docs_content").hide();
		$("#tab_hosts_content").show();
		return false;
	});
	$("#tab_docs").bind("click", function(event) {
		$(this).removeClass("off").addClass("on");
		$("#tab_hosts").removeClass("on").addClass("off");
		$("#tab_hosts_content").hide();
		$("#tab_docs_content").show();
		return false;
	});

	if (g_loginResponse == null) {
		g_loginResponse = JSON.parse($.cookie('loginResponse'));
	}
	if (g_loginResponse != null) {
		$("#header_username").text(g_loginResponse.username);
	}
	g_mySession = $.cookie("JSESSIONID");
	
	$("#header_logout").bind("click", function(event) {
		logout();
		return false;
	});
});


