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
package com.cloud.network.rules;

import java.util.List;
import java.util.ArrayList;


public class LbStickinessMethod {
	public class LbStickinessMethodParam {
		private String paramName;
		private Boolean required;
		private String description;
		
		public LbStickinessMethodParam(String name, Boolean required, String description)
		{
			this.paramName = name;
			this.required = required;
			this.description = description;
		}

		public String getParamName() {
			return paramName;
		}

		public void setParamName(String paramName) {
			this.paramName = paramName;
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
	
	private String methodName;
	private List <LbStickinessMethodParam> paramList;
	private String description;
	
	public LbStickinessMethod(String methodName,String description)
	{
		this.methodName = methodName;
		this.description = description;
		this.paramList = new ArrayList<LbStickinessMethodParam>(1);
	}
	public void addParam(String name, Boolean required,  String description)
	{
		LbStickinessMethodParam param = new LbStickinessMethodParam(name,required, description);
		paramList.add(param);
		return;
	}
	
    public String getMethodName() {
        return methodName;
    }
	public List<LbStickinessMethodParam> getParamList() {
		return paramList;
	}
	public void setParamList(List<LbStickinessMethodParam> paramList) {
		this.paramList = paramList;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
}
