/**
 * Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved
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
package com.cloud.network.rules;

import java.util.List;
import java.util.ArrayList;



public class LbStickinessMethod {
    public static class StickinessMethodType {
        private String _name;
        
        public static final StickinessMethodType LBCookieBased = new StickinessMethodType("LbCookie");
        public static final StickinessMethodType AppCookieBased = new StickinessMethodType("AppCookie");
        public static final StickinessMethodType SourceBased = new StickinessMethodType("SourceBased");
        public StickinessMethodType(String name) {
            _name = name;
        }
        
        public String getName() {
            return _name;
        }
    }
    /* FIXME: Here variable names appear in the jason object inserted into capability, so the names of variable are important */
    public class LbStickinessMethodParam {
        private String paramname;
        private Boolean required;
        private String description;

        public LbStickinessMethodParam(String name, Boolean required,
                String description) {
            this.paramname = name;
            this.required = required;
            this.description = description;
        }

        public String getParamName() {
            return paramname;
        }

        public void setParamName(String paramName) {
            this.paramname = paramName;
        }

        public Boolean getRequired() {
            return required;
        }

        public void setRequired(Boolean required) {
            this.required = required;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

    }

    private String methodname;
    private Boolean httpbased;
    private List<LbStickinessMethodParam> paramlist;
    private String description;

    public LbStickinessMethod(StickinessMethodType methodType, String description, Boolean httpbased) {
        this.methodname = methodType.getName();
        this.description = description;
        this.httpbased = httpbased;
        this.paramlist = new ArrayList<LbStickinessMethodParam>(1);
    }

    public void addParam(String name, Boolean required, String description) {
        /* FIXME : UI is breaking if the capability string length is larger , temporarily description is commented out */
       // LbStickinessMethodParam param = new LbStickinessMethodParam(name, required, description);
        LbStickinessMethodParam param = new LbStickinessMethodParam(name,required, " ");
        paramlist.add(param);
        return;
    }

    public String getMethodName() {
        return methodname;
    }
    
    public Boolean getHttpbased() {
        return httpbased;
    }

    public void setHttpbased(Boolean httpbased) {
        this.httpbased = httpbased;
    }
    public List<LbStickinessMethodParam> getParamList() {
        return paramlist;
    }

    public void setParamList(List<LbStickinessMethodParam> paramList) {
        this.paramlist = paramList;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        /* FIXME : UI is breaking if the capability string length is larger , temporarily description is commented out */
        //this.description = description;
        this.description = " ";
    }
}
