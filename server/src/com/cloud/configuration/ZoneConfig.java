/**
 *  Copyright (C) 2011 Cloud.com.  All rights reserved.
 *
 * This software is licensed under the GNU General Public License v3 or later. 
 *
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later
version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.cloud.configuration;

public enum ZoneConfig {
    EnableSecStorageVm( Boolean.class, "enable.secstorage.vm", "true", "Enables secondary storage vm service", null),
    EnableConsoleProxyVm( Boolean.class, "enable.consoleproxy.vm", "true", "Enables console proxy vm service", null);

   

    private final Class<?> _type;
    private final String _name;
    private final String _defaultValue;
    private final String _description;
    private final String _range;
    
    private ZoneConfig( Class<?> type, String name, String defaultValue, String description, String range) {

        _type = type;
        _name = name;
        _defaultValue = defaultValue;
        _description = description;
        _range = range;
    }
    
    public Class<?> getType() {
        return _type;
    }

    public String getName() {
        return _name;
    }

    public String getDefaultValue() {
        return _defaultValue;
    }

    public String getDescription() {
        return _description;
    }

    public String getRange() {
        return _range;
    }
    
    public String key() {
        return _name;
    }
}
