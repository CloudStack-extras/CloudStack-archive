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

package com.cloud.consoleproxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cloud.console.Logger;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ConsoleProxyAjaxHandler implements HttpHandler {
	private static final Logger s_logger = Logger.getLogger(ConsoleProxyAjaxHandler.class);
	
	public ConsoleProxyAjaxHandler() {
	}
	
	public void handle(HttpExchange t) throws IOException {
		try {
	        if(s_logger.isTraceEnabled())
	        	s_logger.trace("AjaxHandler " + t.getRequestURI());
	        
	        long startTick = System.currentTimeMillis();
	        
	        doHandle(t);
	        
	        if(s_logger.isTraceEnabled())
	        	s_logger.trace(t.getRequestURI() + " process time " + (System.currentTimeMillis() - startTick) + " ms");
		} catch (IOException e) {
			throw e;
		} catch (IllegalArgumentException e) {
			s_logger.warn("Exception, ", e);
			t.sendResponseHeaders(400, -1);		// bad request
		} catch(Throwable e) {
			s_logger.error("Unexpected exception, ", e);
			t.sendResponseHeaders(500, -1);		// server error
		} finally {
			t.close();
		}
	}
	
	private void doHandle(HttpExchange t) throws Exception, IllegalArgumentException {
		String queries = t.getRequestURI().getQuery();
		if(s_logger.isTraceEnabled())
			s_logger.trace("Handle AJAX request: " + queries);
		
		Map<String, String> queryMap = getQueryMap(queries);
		
		String host = queryMap.get("host");
		String portStr = queryMap.get("port");
		String sid = queryMap.get("sid");
		String tag = queryMap.get("tag");
		String ticket = queryMap.get("ticket");
		String ajaxSessionIdStr = queryMap.get("sess");
		String eventStr = queryMap.get("event");
		
		if(tag == null)
			tag = "";
		
		long ajaxSessionId = 0;
		int event = 0;
		
		int port;

		if(host == null || portStr == null || sid == null)
			throw new IllegalArgumentException();
		
		try {
			port = Integer.parseInt(portStr);
		} catch (NumberFormatException e) {
			s_logger.warn("Invalid number parameter in query string: " + portStr);
			throw new IllegalArgumentException(e);
		}

		if(ajaxSessionIdStr != null) {
			try {
				ajaxSessionId = Long.parseLong(ajaxSessionIdStr);
			} catch (NumberFormatException e) {
				s_logger.warn("Invalid number parameter in query string: " + ajaxSessionIdStr);
				throw new IllegalArgumentException(e);
			}
		}

		if(eventStr != null) {
			try {
				event = Integer.parseInt(eventStr);
			} catch (NumberFormatException e) {
				s_logger.warn("Invalid number parameter in query string: " + eventStr);
				throw new IllegalArgumentException(e);
			}
		}
		
		ConsoleProxyViewer viewer = null;
		try {
			viewer = ConsoleProxy.getAjaxVncViewer(host, port, sid, tag, ticket, ajaxSessionIdStr);
		} catch(Exception e) {

/*
			StringWriter writer = new StringWriter();
			try {
				ConsoleProxy.processTemplate("viewer-bad-sid.ftl", null, writer);
			} catch (IOException ex) {
				s_logger.warn("Unexpected exception in processing template.", e);
			} catch (TemplateException ex) {
				s_logger.warn("Unexpected exception in processing template.", e);
			}
			StringBuffer sb = writer.getBuffer();
*/
			s_logger.warn("Failed to create viwer due to " + e.getMessage(), e);

			String[] content = new String[] {
				"<html><head></head><body>",
				"<div id=\"main_panel\" tabindex=\"1\">",
				"<p>Access is denied for the console session. Please close the window and retry again</p>",
				"</div></body></html>"
			};
			
			StringBuffer sb = new StringBuffer();
			for(int i = 0; i < content.length; i++)
				sb.append(content[i]);
			
			sendResponse(t, "text/html", sb.toString());
			return;
		}
		
		if(event != 0) {
			if(ajaxSessionId != 0 && ajaxSessionId == viewer.getAjaxSessionId()) {
				if(event == 7) {
					// client send over an event bag
					InputStream is = t.getRequestBody();
					handleClientEventBag(viewer, convertStreamToString(is, true));
				} else {
					handleClientEvent(viewer, event, queryMap);
				}
				sendResponse(t, "text/html", "OK");
			} else {
				if(s_logger.isDebugEnabled())
					s_logger.debug("Ajax request comes from a different session, id in request: " + ajaxSessionId + ", id in viewer: " + viewer.getAjaxSessionId());
				
				sendResponse(t, "text/html", "Invalid ajax client session id");
			}
		} else {
			if(ajaxSessionId != 0 && ajaxSessionId != viewer.getAjaxSessionId()) {
				if(s_logger.isDebugEnabled())
					s_logger.debug("Ajax request comes from a different session, id in request: " + ajaxSessionId + ", id in viewer: " + viewer.getAjaxSessionId());
				handleClientKickoff(t, viewer);
			} else if(ajaxSessionId == 0) {
				if(s_logger.isDebugEnabled())
					s_logger.debug("Ajax request indicates a fresh client start");
		
				String title = queryMap.get("t");
				String guest = queryMap.get("guest");
				handleClientStart(t, viewer, title != null ? title : "", guest);
			} else {
				
				if(s_logger.isTraceEnabled())
					s_logger.trace("Ajax request indicates client update");
				
				handleClientUpdate(t, viewer);
			}
		}
	}
	
	public static Map<String, String> getQueryMap(String query) {
		String[] params = query.split("&");
		Map<String, String> map = new HashMap<String, String>();
		for (String param : params) {
			String[] paramTokens = param.split("=");
			if(paramTokens != null && paramTokens.length == 2) {
				String name = param.split("=")[0];
				String value = param.split("=")[1];
				map.put(name, value);
			} else {
				if(s_logger.isDebugEnabled())
					s_logger.debug("Invalid paramemter in URL found. param: " + param);
			}
		}
		return map;
	}
	
	private static String convertStreamToString(InputStream is, boolean closeStreamAfterRead) { 
		BufferedReader reader = new BufferedReader(new InputStreamReader(is)); 
		StringBuilder sb = new StringBuilder(); 
		String line = null; 
		try { 
			while ((line = reader.readLine()) != null) { 
				sb.append(line + "\n"); 
			} 
		} catch (IOException e) {
			s_logger.warn("Exception while reading request body: ", e);
		} finally {
			if(closeStreamAfterRead) {
				try { 
					is.close(); 
				} catch (IOException e) { 
				} 
			}
		} 
		return sb.toString(); 
	} 	
	
	private void sendResponse(HttpExchange t, String contentType, String response) throws IOException {
		Headers hds = t.getResponseHeaders();
		hds.set("Content-Type", contentType);
	
		t.sendResponseHeaders(200, response.length());
		OutputStream os = t.getResponseBody();
		try {
			os.write(response.getBytes());
		} finally {
			os.close();
		}
	}
	
	private void handleClientEventBag(ConsoleProxyViewer viewer, String requestData) {
		if(s_logger.isTraceEnabled())
			s_logger.trace("Handle event bag, event bag: " + requestData);
		
		int start = requestData.indexOf("=");
		if(start < 0)
			start = 0;
		else if(start > 0)
			start++;
		String data = URLDecoder.decode(requestData.substring(start));
		String[] tokens = data.split("\\|");
		if(tokens != null && tokens.length > 0) {
			int count = 0;
			try {
				count = Integer.parseInt(tokens[0]);
				int parsePos = 1;
				int type, event, x, y, code, modifiers;
				for(int i = 0; i < count; i++) {
					type = Integer.parseInt(tokens[parsePos++]);
					if(type == 1) 	{
						// mouse event
						event = Integer.parseInt(tokens[parsePos++]);
						x = Integer.parseInt(tokens[parsePos++]);
						y = Integer.parseInt(tokens[parsePos++]);
						code = Integer.parseInt(tokens[parsePos++]);
						modifiers = Integer.parseInt(tokens[parsePos++]);
						
						Map<String, String> queryMap = new HashMap<String, String>();
						queryMap.put("event", String.valueOf(event));
						queryMap.put("x", String.valueOf(x));
						queryMap.put("y", String.valueOf(y));
						queryMap.put("code", String.valueOf(code));
						queryMap.put("modifier", String.valueOf(modifiers));
						handleClientEvent(viewer, event, queryMap);
					} else {
						// keyboard event
						event = Integer.parseInt(tokens[parsePos++]);
						code = Integer.parseInt(tokens[parsePos++]);
						modifiers = Integer.parseInt(tokens[parsePos++]);
						
						Map<String, String> queryMap = new HashMap<String, String>();
						queryMap.put("event", String.valueOf(event));
						queryMap.put("code", String.valueOf(code));
						queryMap.put("modifier", String.valueOf(modifiers));
						handleClientEvent(viewer, event, queryMap);
					}
				}
			} catch(NumberFormatException e) {
				s_logger.warn("Exception in handle client event bag: " + data + ", ", e);
			} catch(Exception e) {
				s_logger.warn("Exception in handle client event bag: " + data + ", ", e);
			}
		}
	}
	
	private void handleClientEvent(ConsoleProxyViewer viewer, int event, Map<String, String> queryMap) {
		int code = 0;
		int x = 0, y = 0;
		int modifiers = 0;
		
		String str;
		switch(event) {
		case 1:		// mouse move
		case 2:		// mouse down
		case 3:		// mouse up
		case 8:		// mouse double click
			str = queryMap.get("x");
			if(str != null) {
				try {
					x = Integer.parseInt(str);
				} catch (NumberFormatException e) {
					s_logger.warn("Invalid number parameter in query string: " + str);
					throw new IllegalArgumentException(e);
				}
			}
			str = queryMap.get("y");
			if(str != null) {
				try {
					y = Integer.parseInt(str);
				} catch (NumberFormatException e) {
					s_logger.warn("Invalid number parameter in query string: " + str);
					throw new IllegalArgumentException(e);
				}
			}
			
			if(event != 1) {
				str = queryMap.get("code");
				try {
					code = Integer.parseInt(str);
				} catch (NumberFormatException e) {
					s_logger.warn("Invalid number parameter in query string: " + str);
					throw new IllegalArgumentException(e);
				}
				
				str = queryMap.get("modifier");
				try {
					modifiers = Integer.parseInt(str);
				} catch (NumberFormatException e) {
					s_logger.warn("Invalid number parameter in query string: " + str);
					throw new IllegalArgumentException(e);
				}
				
				if(s_logger.isTraceEnabled())
					s_logger.trace("Handle client mouse event. event: " + event + ", x: " + x + ", y: " + y + ", button: " + code + ", modifier: " + modifiers);
			} else {
				if(s_logger.isTraceEnabled())
					s_logger.trace("Handle client mouse move event. x: " + x + ", y: " + y);
			}
			viewer.sendClientMouseEvent(event, x, y, code, modifiers);
			break;
			
		case 4:		// key press
		case 5:		// key down
		case 6:		// key up
			str = queryMap.get("code");
			try {
				code = Integer.parseInt(str);
			} catch (NumberFormatException e) {
				s_logger.warn("Invalid number parameter in query string: " + str);
				throw new IllegalArgumentException(e);
			}
			
			str = queryMap.get("modifier");
			try {
				modifiers = Integer.parseInt(str);
			} catch (NumberFormatException e) {
				s_logger.warn("Invalid number parameter in query string: " + str);
				throw new IllegalArgumentException(e);
			}
			
			if(s_logger.isDebugEnabled())
				s_logger.debug("Handle client keyboard event. event: " + event + ", code: " + code + ", modifier: " + modifiers);
			viewer.sendClientRawKeyboardEvent(event, code, modifiers);
			break;
			
		default :
			break;
		}
	}
	
	private void handleClientKickoff(HttpExchange t, ConsoleProxyViewer viewer) throws IOException {
		String response = viewer.onAjaxClientKickoff();
		t.sendResponseHeaders(200, response.length());
		OutputStream os = t.getResponseBody();
		try {
			os.write(response.getBytes());
		} finally {
			os.close();
		}
	}
	
	private void handleClientStart(HttpExchange t, ConsoleProxyViewer viewer, String title, String guest) throws IOException {
		List<String> languages = t.getRequestHeaders().get("Accept-Language");
		String response = viewer.onAjaxClientStart(title, languages, guest);
		
		Headers hds = t.getResponseHeaders();
		hds.set("Content-Type", "text/html");
		hds.set("Cache-Control", "no-cache");
		hds.set("Cache-Control", "no-store");
		t.sendResponseHeaders(200, response.length());
		
		OutputStream os = t.getResponseBody();
		try {
			os.write(response.getBytes());
		} finally {
			os.close();
		}
	}
	
	private void handleClientUpdate(HttpExchange t, ConsoleProxyViewer viewer) throws IOException {
		String response = viewer.onAjaxClientUpdate();
		
		Headers hds = t.getResponseHeaders();
		hds.set("Content-Type", "text/javascript");
		t.sendResponseHeaders(200, response.length());
		
		OutputStream os = t.getResponseBody();
		try {
			os.write(response.getBytes());
		} finally {
			os.close();
		}
	}
}
