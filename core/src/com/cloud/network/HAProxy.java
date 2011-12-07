/**
 * Copyright (C) 2011 Citrix Systems, Inc. All rights reserved
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

package com.cloud.network;

import java.util.ArrayList;
import java.util.List;

import com.cloud.network.lb.LoadBalancingRule.LbStickinessPolicy;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.network.lb.LoadBalancingRule;
import com.cloud.network.rules.LbStickinessMethod;
import com.cloud.network.rules.LbStickinessMethod.StickinessMethodType;
import com.cloud.utils.Pair;
import com.google.gson.Gson;


public class HAProxy {
    
    public static String getStickinessCapability() {
        LbStickinessMethod method;
        List <LbStickinessMethod> methodList = new ArrayList<LbStickinessMethod>(1);
        
        method = new LbStickinessMethod(StickinessMethodType.LBCookieBased,"This is loadbalancer cookie based stickiness method."); 
        method.addParam("name", false,  "Cookie name passed in http header by the LB to the client.",false);
        method.addParam("mode", false,  "valid value : insert ,rewrite/prefix . default value: insert,  In the insert mode cookie will be created by the LB . in other mode cookie will be created by the server and LB modifies it.",false);     
        method.addParam("nocache", false, "This option is recommended in conjunction with the insert mode when there is a cache between the client and HAProxy, as it ensures that a cacheable response will be tagged non-cacheable if  a cookie needs to be inserted. This is important because if all persistence cookies are added on a cacheable home page for instance, then all customers will then fetch the page from an outer cache and will all share the same persistence cookie, leading to one server receiving much more traffic than others. See also the insert and postonly options. ",true); 
        method.addParam("indirect", false, "When this option is specified in insert mode, cookies will only be added when the server was not reached after a direct access, which means that only when a server is elected after applying a load-balancing algorithm, or after a redispatch, then the cookie  will be inserted. If the client has all the required information to connect to the same server next time, no further cookie will be inserted. In all cases, when the indirect option is used in insert mode, the cookie is always removed from the requests transmitted to the server. The persistence mechanism then becomes totally transparent from the application point of view.",true);    
        method.addParam("postonly",false,"This option ensures that cookie insertion will only be performed on responses to POST requests. It is an alternative to the nocache option, because POST responses are not cacheable, so this ensures that the persistence cookie will never get cached.Since most sites do not need any sort of persistence before the first POST which generally is a login request, this is a very efficient method to optimize caching without risking to find a persistence cookie in the cache. See also the insert and nocache options.",true);
        method.addParam("domain",false,"This option allows to specify the domain at which a cookie is inserted. It requires exactly one parameter: a valid domain name. If the domain begins with a dot, the browser is allowed to use it for any host ending with that name. It is also possible to specify several domain names by invoking this option multiple times. Some browsers might have small limits on the number of domains, so be careful when doing that. For the record, sending 10 domains to MSIE 6 or Firefox 2 works as expected.",false);
        methodList.add(method);
        
        method = new LbStickinessMethod(StickinessMethodType.AppCookieBased,"This is app session based sticky method,Define session stickiness on an existing application cookie. it can be used only for a specific http traffic");
        method.addParam("name", true,  "This is the name of the cookie used by the application and which LB will have to learn for each new session",false);
        method.addParam("length", true,  "This is the max number of characters that will be memorized and checked in each cookie value",false);  
        method.addParam("holdtime", true, "This is the time after which the cookie will be removed from memory if unused. The value should be in the format Example : 20s or 30m  or 4h or 5d . only seconds(s), minutes(m) hours(h) and days(d) are valid , cannot use th combinations like 20h30m. ",false);
        method.addParam("request-learn", false, "If this option is specified, then haproxy will be able to learn the cookie found in the request in case the server does not specify any in response. This is typically what happens with PHPSESSID cookies, or when haproxy's session expires before the application's session and the correct server is selected. It is recommended to specify this option to improve reliability",true);
        method.addParam("prefix", false,"When this option is specified, haproxy will match on the cookie prefix (or URL parameter prefix). The appsession value is the data following this prefix. Example : appsession ASPSESSIONID len 64 timeout 3h prefix  This will match the cookie ASPSESSIONIDXXXX=XXXXX, the appsession value will be XXXX=XXXXX.",true);
        method.addParam("mode", false,"This option allows to change the URL parser mode. 2 modes are currently supported : - path-parameters : The parser looks for the appsession in the path parameters part (each parameter is separated by a semi-colon), which is convenient for JSESSIONID for example.This is the default mode if the option is not set. - query-string : In this mode, the parser will look for the appsession in the query string.",false);
        methodList.add(method);
        
        method = new LbStickinessMethod(StickinessMethodType.SourceBased,"This is source based Stickiness method, it can be used for any type of protocol.");
        method.addParam("tablesize", false, "Size of table to store source ip addresses. example: tablesize=200k or 300m or 400g",false);
        method.addParam("expire", false, "Entry in source ip table will expire after expire duration. units can be s,m,h,d . example: expire=30m 20s 50h 4d , combinations is not valid",false);
        methodList.add(method);
        
        Gson gson = new Gson();
        String capability = gson.toJson(methodList);
        return capability;
    }
    
    public static class HAProxyValidator implements LoadBalancerValidator {
        /*
         * This function detects numbers like 12 ,32h ,42m .. etc,. 1) plain
         * number like 12 2) time or tablesize like 12h, 34m, 45k, 54m , here
         * last character is non-digit but from known characters .
         */
        private boolean containsOnlyNumbers(String str, String endChar) {
            if (str == null)
                return false;

            String number = str;
            if (endChar != null) {
                boolean matchedEndChar = false;
                if (str.length() < 2)
                    return false; // atleast one numeric and one char. example:
                                  // 3h
                char strEnd = str.toCharArray()[str.length() - 1];
                for (char c : endChar.toCharArray()) {
                    if (strEnd == c) {
                        number = str.substring(0, str.length() - 1);
                        matchedEndChar = true;
                        break;
                    }
                }
                if (!matchedEndChar)
                    return false;
            }
            try {
                int i = Integer.parseInt(number);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }

        public boolean validateLBRule(LoadBalancingRule rule)  {
            String timeEndChar = "dhms";

            for (LbStickinessPolicy stickinessPolicy : rule.getStickinessPolicies()) {
                List<Pair<String, String>> paramsList = stickinessPolicy.getParams();

                if (StickinessMethodType.LBCookieBased.getName().equalsIgnoreCase(stickinessPolicy.getMethodName())) {

                } else if (StickinessMethodType.SourceBased.getName().equalsIgnoreCase(stickinessPolicy.getMethodName())) {
                    String tablesize = "200k"; // optional
                    String expire = "30m"; // optional

                    /* overwrite default values with the stick parameters */
                    for (Pair<String, String> paramKV : paramsList) {
                        String key = paramKV.first();
                        String value = paramKV.second();
                        if ("tablesize".equalsIgnoreCase(key))
                            tablesize = value;
                        if ("expire".equalsIgnoreCase(key))
                            expire = value;
                    }
                    if ((expire != null) && !containsOnlyNumbers(expire, timeEndChar)) {
                        throw new InvalidParameterValueException("Failed LB in validation rule id: " + rule.getId() + " Cause: expire is not in timeformat:" + expire);
                    }
                    if ((tablesize != null) && !containsOnlyNumbers(tablesize, "kmg")) {
                        throw new InvalidParameterValueException("Failed LB in validation rule id: " + rule.getId() + " Cause: tablesize is not in size format:" + tablesize);

                    }
                } else if (StickinessMethodType.AppCookieBased.getName().equalsIgnoreCase(stickinessPolicy.getMethodName())) {
                    /*
                     * FORMAT : appsession <cookie> len <length> timeout
                     * <holdtime> [request-learn] [prefix] [mode
                     * <path-parameters|query-string>]
                     */
                    /* example: appsession JSESSIONID len 52 timeout 3h */
                    String name = null; // required
                    String length = null; // required
                    String holdtime = null; // required

                    for (Pair<String, String> paramKV : paramsList) {
                        String key = paramKV.first();
                        String value = paramKV.second();
                        if ("name".equalsIgnoreCase(key))
                            name = value;
                        if ("length".equalsIgnoreCase(key))
                            length = value;
                        if ("holdtime".equalsIgnoreCase(key))
                            holdtime = value;
                    }
                    if ((name == null) || (length == null) || (holdtime == null)) {
                        throw new InvalidParameterValueException("Failed LB in validation rule id: " + rule.getId() + " Cause: length,holdtime or name is null ");
                    }
                    if (!containsOnlyNumbers(length, null)) {
                        throw new InvalidParameterValueException("Failed LB in validation rule id: " + rule.getId() + " Cause: length is not a number: " + length);
                    }
                    if (!containsOnlyNumbers(holdtime, timeEndChar)) {
                        throw new InvalidParameterValueException("Failed LB in validation rule id: " + rule.getId() + " Cause: holdtime is not in timeformat: " + holdtime);
                    }
                }
            }
            return true;
        }
    }
}
