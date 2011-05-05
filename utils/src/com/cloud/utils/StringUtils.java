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

package com.cloud.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// StringUtils exists in Apache Commons Lang, but rather than import the entire JAR to our system, for now
// just implement the method needed
public class StringUtils {
    public static String join(Iterable<? extends Object> iterable, String delim) {
        StringBuilder sb = new StringBuilder();
        if (iterable != null) {
            Iterator<? extends Object> iter = iterable.iterator();
            if (iter.hasNext()) {
                Object next = iter.next();
                sb.append(next.toString());
            }
            while (iter.hasNext()) {
                Object next = iter.next();
                sb.append(delim + next.toString());
            }
        }
        return sb.toString();
    }
    
    
	/**
	 * Converts a comma separated list of tags to a List
	 * @param tags
	 * @return List of tags
	 */
    
    public static List<String> csvTagsToList(String tags) {
    	List<String> tagsList = new ArrayList<String>();
    	
    	if (tags != null) {
            String[] tokens = tags.split(",");
            for (int i = 0; i < tokens.length; i++) {
                tagsList.add(tokens[i].trim());
            }
        }
    	
    	return tagsList;
    }
    
	
    /**
	 * Converts a List of tags to a comma separated list
	 * @param tags
	 * @return String containing a comma separated list of tags
	 */
    
    public static String listToCsvTags(List<String> tagsList) {
    	String tags = "";
    	if (tagsList.size() > 0) {
    		for (int i = 0; i < tagsList.size(); i++) {
    			tags += tagsList.get(i);
    			if (i != tagsList.size() - 1) {
    				tags += ",";
    			}
    		}
    	} 
    	
    	return tags;
    }        
    
    public static String getExceptionStackInfo(Throwable e) {
    	StringBuffer sb = new StringBuffer();
    	
    	sb.append(e.toString()).append("\n");
    	StackTraceElement[] elemnents = e.getStackTrace();
    	for(StackTraceElement element : elemnents) {
    		sb.append(element.getClassName()).append(".");
    		sb.append(element.getMethodName()).append("(");
    		sb.append(element.getFileName()).append(":");
    		sb.append(element.getLineNumber()).append(")");
    		sb.append("\n");
    	}
    	
    	return sb.toString();
    }
}
