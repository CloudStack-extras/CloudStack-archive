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
    public class LbStickinessMethodParam {
        private String _paramName;
        private Boolean _required;
        private String _description;

        public LbStickinessMethodParam(String name, Boolean required,
                String description) {
            this._paramName = name;
            this._required = required;
            this._description = description;
        }

        public String getParamName() {
            return _paramName;
        }

        public void setParamName(String paramName) {
            this._paramName = paramName;
        }

        public Boolean getRequired() {
            return _required;
        }

        public void setRequired(Boolean required) {
            this._required = required;
        }

        public String getDescription() {
            return _description;
        }

        public void setDescription(String description) {
            this._description = description;
        }

    }

    private String _methodName;
    private List<LbStickinessMethodParam> _paramList;
    private String _description;

    public LbStickinessMethod(String methodName, String description) {
        this._methodName = methodName;
        this._description = description;
        this._paramList = new ArrayList<LbStickinessMethodParam>(1);
    }

    public void addParam(String name, Boolean required, String description) {
        LbStickinessMethodParam param = new LbStickinessMethodParam(name,
                required, description);
        _paramList.add(param);
        return;
    }

    public String getMethodName() {
        return _methodName;
    }

    public List<LbStickinessMethodParam> getParamList() {
        return _paramList;
    }

    public void setParamList(List<LbStickinessMethodParam> paramList) {
        this._paramList = paramList;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        this._description = description;
    }

    public void setMethodName(String methodName) {
        this._methodName = methodName;
    }
}
