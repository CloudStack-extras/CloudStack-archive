/*
 * Copyright (C) 2011 Citrix Systems, Inc.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloud.bridge.persist;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import com.cloud.bridge.util.DateHelper;

/**
 * @author Kelven Yang, John Zucker
 * The purpose of this Hibernate user type is to customize the mappings from a MySQL datetime
 * column to a Java date so as to resemble the AWS datetime datype.  
 * For this purpose dates are held in UTC-relative form
 * referenced to the GMT timezone.  To provide accurate encodings all date strings supplied by
 * the Hibernate mappings (as .hbm.xml files in the persistence model) are encoded through 
 * conversions using cloud.bridge.util.ISO8601DateFormat.
 * GMTDateTimeUserType implements a Hibernate user type, it deals with GMT date/time conversion
 * between Java Date/Calendar and MySQL DATE types
 * The business methods nullSafeGet and nullSafeSet define the getters and setters for the datetime
 * column entries in the cloud.bridge.model ORM.
 */
public class GMTDateTimeUserType implements UserType {
	
	private static final int[] SQL_TYPES = { Types.VARBINARY };
	
	public Class<?> returnedClass() { return Date.class; }

	public boolean equals(Object x, Object y) {
		if (x == y) 
			return true;
		
		if (x == null || y == null) 
			return false;
		
		return x.equals(y);
	}
	
	public int hashCode(Object x) {
		if(x != null)
			return x.hashCode();
		
		return 0;
	}
	
	public Object deepCopy(Object value) {
		if(value != null)
			return ((Date)value).clone();
		return null;
	}
	
	public boolean isMutable() { 
		return true; 
	}
	
	public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
	throws HibernateException, SQLException {
	
	String dateString = resultSet.getString(names[0]);
	if(dateString != null)
		return DateHelper.parseDateString(DateHelper.GMT_TIMEZONE, dateString);
	return null;
}
	
	/* public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
		throws HibernateException, SQLException {
		String dateString = resultSet.getString(names[0]);
		if (null == dateString)
			 return null;
		else return DateHelper.parseISO8601DateString(dateString);
			// return DateHelper.parseDateString(DateHelper.GMT_TIMEZONE, dateString);
	} */
	
	public void nullSafeSet(PreparedStatement statement, Object value, int index)
		throws HibernateException, SQLException {
		if (value == null) {
			statement.setNull(index, Types.TIMESTAMP);
		} else {
			Date dt = (Date)value;
			statement.setString(index, DateHelper.getDateDisplayString(DateHelper.GMT_TIMEZONE, dt));
		}
	}
	
	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return DateHelper.parseDateString(DateHelper.GMT_TIMEZONE, (String)cached);
	}
	
	public Serializable disassemble(Object value) throws HibernateException {
		return DateHelper.getDateDisplayString(DateHelper.GMT_TIMEZONE, (Date)value);
	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return ((Date)original).clone();
	}

	public int[] sqlTypes() {
		return SQL_TYPES;
	}
}
