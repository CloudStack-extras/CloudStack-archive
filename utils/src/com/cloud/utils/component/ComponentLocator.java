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
package com.cloud.utils.component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Local;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cloud.utils.Pair;
import com.cloud.utils.PropertiesUtil;
import com.cloud.utils.Ternary;
import com.cloud.utils.db.DatabaseCallback;
import com.cloud.utils.db.DatabaseCallbackFilter;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.mgmt.JmxUtil;
import com.cloud.utils.mgmt.ManagementBean;

/**
 * ComponentLocator manages all of the adapters within a system. It operates on
 * top of an components.xml and uses reflection to instantiate all of the
 * adapters. It also supports rereading of all of the adapters.
 * 
 **/
@SuppressWarnings("unchecked")
public class ComponentLocator implements ComponentLocatorMBean {
    protected static final Logger                      s_logger     = Logger.getLogger(ComponentLocator.class);
    
    protected static final ThreadLocal<ComponentLocator> s_tl = new ThreadLocal<ComponentLocator>();
    protected static final ConcurrentHashMap<Class<?>, Singleton> s_singletons = new ConcurrentHashMap<Class<?>, Singleton>(111);
    protected static final HashMap<String, ComponentLocator> s_locators = new HashMap<String, ComponentLocator>();
    protected static final HashMap<Class<?>, InjectInfo> s_factories = new HashMap<Class<?>, InjectInfo>();
    protected static Boolean s_once = false;
    protected static Boolean _hasCheckerRun = false;
    protected static Callback[] s_callbacks = new Callback[] { NoOp.INSTANCE, new DatabaseCallback()};
    protected static CallbackFilter s_callbackFilter = new DatabaseCallbackFilter();
    protected static final List<AnnotationInterceptor<?>> s_interceptors = new ArrayList<AnnotationInterceptor<?>>();
    protected static CleanupThread s_janitor = null;
    
    protected HashMap<String, Adapters<? extends Adapter>>              _adapterMap;
    protected HashMap<String, ComponentInfo<Manager>>                   _managerMap;
    protected HashMap<String, ComponentInfo<SystemIntegrityChecker>>     _checkerMap;
    protected LinkedHashMap<String, ComponentInfo<GenericDao<?, ? extends Serializable>>>    _daoMap;
    protected String                                                    _serverName;
    protected Object                                                    _component;
    protected HashMap<Class<?>, Class<?>>                               _factories;
    
    static {
        if (s_janitor == null) {
            s_janitor = new CleanupThread();
            Runtime.getRuntime().addShutdownHook(new CleanupThread());
        }
    }

    public ComponentLocator(String server) {
        _serverName = server;
        if (s_janitor == null) {
            s_janitor = new CleanupThread();
            Runtime.getRuntime().addShutdownHook(new CleanupThread());
        }
    }

    public String getLocatorName() {
        return _serverName;
    }
    
    @Override
    public String getName() {
        return getLocatorName();
    }

    protected Pair<XmlHandler, HashMap<String, List<ComponentInfo<Adapter>>>> parse2(String filename) {
        try {
            SAXParserFactory spfactory = SAXParserFactory.newInstance();
            SAXParser saxParser = spfactory.newSAXParser();
            _daoMap = new LinkedHashMap<String, ComponentInfo<GenericDao<?, ? extends Serializable>>>();
            _managerMap = new LinkedHashMap<String, ComponentInfo<Manager>>();
            _checkerMap = new HashMap<String, ComponentInfo<SystemIntegrityChecker>>();
            _adapterMap = new HashMap<String, Adapters<? extends Adapter>>();
            _factories = new HashMap<Class<?>, Class<?>>();
            File file = PropertiesUtil.findConfigFile(filename);
            if (file == null) {
                s_logger.info("Unable to find " + filename);
                return null;
            }
            s_logger.info("Config file found at " + file.getAbsolutePath() + ".  Configuring " + _serverName);
            XmlHandler handler = new XmlHandler(_serverName);
            saxParser.parse(file, handler);

            HashMap<String, List<ComponentInfo<Adapter>>> adapters = new HashMap<String, List<ComponentInfo<Adapter>>>();
            if (handler.parent != null) {
                String[] tokens = handler.parent.split(":");
                String parentFile = filename;
                String parentName = handler.parent;
                if (tokens.length > 1) {
                    parentFile = tokens[0];
                    parentName = tokens[1];
                }
                ComponentLocator parentLocator = new ComponentLocator(parentName);
                adapters.putAll(parentLocator.parse2(parentFile).second());
                _daoMap.putAll(parentLocator._daoMap);
                _managerMap.putAll(parentLocator._managerMap);
                _factories.putAll(parentLocator._factories);
            }

            ComponentLibrary library = null;
            if (handler.library != null) {
                Class<?> clazz = Class.forName(handler.library);
                library = (ComponentLibrary)clazz.newInstance();
                _daoMap.putAll(library.getDaos());
                _managerMap.putAll(library.getManagers());
                adapters.putAll(library.getAdapters());
                _factories.putAll(library.getFactories());
            }

            _daoMap.putAll(handler.daos);
            _managerMap.putAll(handler.managers);
            _checkerMap.putAll(handler.checkers);
            adapters.putAll(handler.adapters);
            
            return new Pair<XmlHandler, HashMap<String, List<ComponentInfo<Adapter>>>>(handler, adapters);
        } catch (ParserConfigurationException e) {
            s_logger.error("Unable to load " + _serverName + " due to errors while parsing " + filename, e);
            System.exit(1);
        } catch (SAXException e) {
            s_logger.error("Unable to load " + _serverName + " due to errors while parsing " + filename, e);
            System.exit(1);
        } catch (IOException e) {
            s_logger.error("Unable to load " + _serverName + " due to errors while reading from " + filename, e);
            System.exit(1);
        } catch (CloudRuntimeException e) {
            s_logger.error("Unable to load configuration for " + _serverName + " from " + filename, e);
            System.exit(1);
        } catch (Exception e) {
            s_logger.error("Unable to load configuration for " + _serverName + " from " + filename, e);
            System.exit(1);
        }
        return null;
    }

    protected void parse(String filename) {
        Pair<XmlHandler, HashMap<String, List<ComponentInfo<Adapter>>>> result = parse2(filename);
        if (result == null) {
            s_logger.info("Skipping configuration using " + filename);
            return;
        }
        
        XmlHandler handler = result.first();
        HashMap<String, List<ComponentInfo<Adapter>>> adapters = result.second();
        try {
            runCheckers();
            startDaos();    // daos should not be using managers and adapters.
            instantiateAdapters(adapters);
            instantiateManagers();
            if (handler.componentClass != null) {
                _component = createInstance(handler.componentClass, true, true);
            }
            configureManagers();
            configureAdapters();
            startManagers();
            startAdapters();
        } catch (CloudRuntimeException e) {
            s_logger.error("Unable to load configuration for " + _serverName + " from " + filename, e);
            System.exit(1);
        } catch (Exception e) {
        	s_logger.error("Unable to load configuration for " + _serverName + " from " + filename, e);
        	System.exit(1);
        }
    }

    protected void runCheckers() {
        Set<Map.Entry<String, ComponentInfo<SystemIntegrityChecker>>> entries = _checkerMap.entrySet();
        for (Map.Entry<String, ComponentInfo<SystemIntegrityChecker>> entry : entries) {
            ComponentInfo<SystemIntegrityChecker> info = entry.getValue();
            try {
                info.instance = (SystemIntegrityChecker)createInstance(info.clazz, false, info.singleton);
                info.instance.check();
            } catch (Exception e) {
                s_logger.error("Problems with running checker:" + info.name, e);
                System.exit(1);
            }
        }
    }
    /**
     * Daos should not refer to any other components so it is safe to start them
     * here.
     */
    protected void startDaos()  {
        Set<Map.Entry<String, ComponentInfo<GenericDao<?, ? extends Serializable>>>> entries = _daoMap.entrySet();

        for (Map.Entry<String, ComponentInfo<GenericDao<?, ?>>> entry : entries) {
            ComponentInfo<GenericDao<?, ?>> info = entry.getValue();
            try {
                info.instance = (GenericDao<?, ?>)createInstance(info.clazz, true, info.singleton);
                if (info.singleton) {
                    s_logger.info("Starting singleton DAO: " + info.name);
                    Singleton singleton = s_singletons.get(info.clazz);
                    if (singleton.state == Singleton.State.Instantiated) {
                        inject(info.clazz, info.instance);
                        singleton.state = Singleton.State.Injected;
                    }
                    if (singleton.state == Singleton.State.Injected) {
                        if (!info.instance.configure(info.name, info.params)) {
                            s_logger.error("Unable to configure DAO: " + info.name);
                            System.exit(1);
                        }
                        singleton.state = Singleton.State.Started;
                    }
                } else {
                    s_logger.info("Starting DAO: " + info.name);
                    inject(info.clazz, info.instance);
                    if (!info.instance.configure(info.name, info.params)) {
                        s_logger.error("Unable to configure DAO: " + info.name);
                        System.exit(1);
                    }
                }
            } catch (ConfigurationException e) {
                s_logger.error("Unable to configure DAO: " + info.name, e);
                System.exit(1);
            } catch (Exception e) {
                s_logger.error("Problems while configuring DAO: " + info.name, e);
                System.exit(1);
            }
            if (info.instance instanceof ManagementBean) {
                registerMBean((ManagementBean) info.instance);
            }
        }
    }
    
    private static Object createInstance(Class<?> clazz, boolean inject, boolean singleton, Object... args) {
        Factory factory = null;
        Singleton entity = null;
        synchronized(s_factories) {
            if (singleton) {
                entity = s_singletons.get(clazz);
                if (entity != null) {
                    s_logger.debug("Found singleton instantiation for " + clazz.toString());
                    return entity.singleton;
                }
            }
            InjectInfo info = s_factories.get(clazz);
            if (info == null) {
                Enhancer enhancer = new Enhancer();
                enhancer.setSuperclass(clazz);
                enhancer.setCallbackFilter(s_callbackFilter);
                enhancer.setCallbacks(s_callbacks);
                factory = (Factory)enhancer.create();
                info = new InjectInfo(enhancer, factory);
                s_factories.put(clazz, info);
            } else {
                factory = info.factory;
            }
        }
        
        
        Class<?>[] argTypes = null;
        if (args != null && args.length > 0) {
            Constructor<?>[] constructors = clazz.getConstructors();
            for (Constructor<?> constructor : constructors) {
                Class<?>[] paramTypes = constructor.getParameterTypes();
                if (paramTypes.length == args.length) {
                    boolean found = true;
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (!paramTypes[i].isAssignableFrom(args[i].getClass()) && !paramTypes[i].isPrimitive()) {
                            found = false;
                            break;
                        }
                    }
                    if (found) {
                        argTypes = paramTypes;
                        break;
                    }
                }
            }
            
            if (argTypes == null) {
                throw new CloudRuntimeException("Unable to find constructor to match parameters given: " + clazz.getName());
            }
        
            entity = new Singleton(factory.newInstance(argTypes, args, s_callbacks));
        } else {
            entity = new Singleton(factory.newInstance(s_callbacks));
        }
        
        if (inject) {
            inject(clazz, entity.singleton);
            entity.state = Singleton.State.Injected;
        }
        
        if (singleton) {
            synchronized(s_factories) {
                s_singletons.put(clazz, entity);
            }
        }
        
        return entity.singleton;
    }
    

    protected ComponentInfo<GenericDao<?, ?>> getDao(String name) {
        ComponentInfo<GenericDao<?, ?>> info = _daoMap.get(name);
        if (info == null) {
            throw new CloudRuntimeException("Unable to find DAO " + name);
        }
        
        return info;
    }

    public static synchronized Object getComponent(String componentName) {
    	synchronized(_hasCheckerRun) {
    		/* System Integrity checker will run before all components really loaded */
	    	if (!_hasCheckerRun && !componentName.equalsIgnoreCase(SystemIntegrityChecker.Name)) {
	    	    ComponentLocator.getComponent(SystemIntegrityChecker.Name);
	    	    _hasCheckerRun = true;
	    	}
    	}
    	
        ComponentLocator locator = s_locators.get(componentName);
        if (locator == null) {
            locator = ComponentLocator.getLocator(componentName);
        }
        return locator._component;
    }

    public <T extends GenericDao<?, ? extends Serializable>> T getDao(Class<T> clazz) {
        ComponentInfo<GenericDao<?, ?>> info = getDao(clazz.getName());
        return info != null ? (T)info.instance : null;
    }

    protected void instantiateManagers() {
        Set<Map.Entry<String, ComponentInfo<Manager>>> entries = _managerMap.entrySet();
        for (Map.Entry<String, ComponentInfo<Manager>> entry : entries) {
            ComponentInfo<Manager> info = entry.getValue();
            if (info.instance == null) {
                s_logger.info("Instantiating Manager: " + info.name);
                info.instance = (Manager)createInstance(info.clazz, false, info.singleton);
            }
        }
    }

    protected void configureManagers() {
        Set<Map.Entry<String, ComponentInfo<Manager>>> entries = _managerMap.entrySet();
        for (Map.Entry<String, ComponentInfo<Manager>> entry : entries) {
            ComponentInfo<Manager> info = entry.getValue();
            if (info.singleton) {
                Singleton s = s_singletons.get(info.clazz);
                if (s.state == Singleton.State.Instantiated) {
                    s_logger.debug("Injecting singleton Manager: " + info.name);
                    inject(info.clazz, info.instance);
                    s.state = Singleton.State.Injected;
                }
            } else {
                s_logger.info("Injecting Manager: " + info.name);
                inject(info.clazz, info.instance);
            }
        }
        for (Map.Entry<String, ComponentInfo<Manager>> entry : entries) {
            ComponentInfo<Manager> info = entry.getValue();
            if (info.singleton) {
                Singleton s = s_singletons.get(info.clazz);
                if (s.state == Singleton.State.Injected) {
                    s_logger.info("Configuring singleton Manager: " + info.name);
                    try {
                        info.instance.configure(info.name, info.params);
                    } catch (ConfigurationException e) {
                        s_logger.error("Unable to configure manager: " + info.name, e);
                        System.exit(1);
                    }
                    s.state = Singleton.State.Configured;
                }
            } else {
                s_logger.info("Configuring Manager: " + info.name);
                try {
                    info.instance.configure(info.name, info.params);
                } catch (ConfigurationException e) {
                    s_logger.error("Unable to configure manager: " + info.name, e);
                    System.exit(1);
                }
            }
        }
    }
    
    protected static void inject(Class<?> clazz, Object entity) {
        ComponentLocator locator = ComponentLocator.getCurrentLocator();
        
        do {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                Inject inject = field.getAnnotation(Inject.class);
                if (inject == null) {
                    continue;
                }
                Class<?> fc = field.getType();
                Object instance = null;
                if (Manager.class.isAssignableFrom(fc)) {
                    s_logger.trace("Manager: " + fc.getName());
                    instance = locator.getManager(fc);
                } else if (GenericDao.class.isAssignableFrom(fc)) {
                    s_logger.trace("Dao:" + fc.getName());
                    instance = locator.getDao((Class<? extends GenericDao<?, ? extends Serializable>>)fc);
                } else if (Adapters.class.isAssignableFrom(fc)) {
                    s_logger.trace("Adapter" + fc.getName());
                    instance = locator.getAdapters(inject.adapter());
                } else {
                    s_logger.trace("Other:" + fc.getName());
                    instance = locator.getManager(fc);
                }
        
                if (instance == null) {
                    throw new CloudRuntimeException("Unable to inject " + fc.getSimpleName() + " in " + clazz.getSimpleName());
                }
                
                try {
                    field.setAccessible(true);
                    field.set(entity, instance);
                } catch (IllegalArgumentException e) {
                    throw new CloudRuntimeException("hmmm....is it really illegal?", e);
                } catch (IllegalAccessException e) {
                    throw new CloudRuntimeException("what! what ! what!", e);
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class && clazz != null);
    }

    protected void startManagers() {
        Set<Map.Entry<String, ComponentInfo<Manager>>> entries = _managerMap.entrySet();
        for (Map.Entry<String, ComponentInfo<Manager>> entry : entries) {
            ComponentInfo<Manager> info = entry.getValue();
            if (info.singleton) {
                Singleton s = s_singletons.get(info.clazz);
                if (s.state == Singleton.State.Configured) {
                    s_logger.info("Starting singleton Manager: " + info.name);
                    if (!info.instance.start()) {
                        throw new CloudRuntimeException("Incorrect Configuration: " + info.name);
                    }
                    if (info.instance instanceof ManagementBean) {
                        registerMBean((ManagementBean) info.instance);
                    }
                    s_logger.info("Started Manager: " + info.name);
                    s.state = Singleton.State.Started;
                }
            } else {
                s_logger.info("Starting Manager: " + info.name);
                if (!info.instance.start()) {
                    throw new CloudRuntimeException("Incorrect Configuration: " + info.name);
                }
                if (info.instance instanceof ManagementBean) {
                    registerMBean((ManagementBean) info.instance);
                }
                s_logger.info("Started Manager: " + info.name);
            }
        }
    }

    protected void registerMBean(ManagementBean mbean) {
        try {
            JmxUtil.registerMBean(mbean);
        } catch (MalformedObjectNameException e) {
            s_logger.warn("Unable to register MBean: " + mbean.getName(), e);
        } catch (InstanceAlreadyExistsException e) {
            s_logger.warn("Unable to register MBean: " + mbean.getName(), e);
        } catch (MBeanRegistrationException e) {
            s_logger.warn("Unable to register MBean: " + mbean.getName(), e);
        } catch (NotCompliantMBeanException e) {
            s_logger.warn("Unable to register MBean: " + mbean.getName(), e);
        }
        s_logger.info("Registered MBean: " + mbean.getName());
    }

    protected ComponentInfo<Manager> getManager(String name) {
        ComponentInfo<Manager> mgr = _managerMap.get(name);
        return mgr;
    }

    public <T> T getManager(Class<T> clazz) {
        ComponentInfo<Manager> info = getManager(clazz.getName());
        if (info == null) {
            return null;
        }
        if (info.instance == null) {
            info.instance = (Manager)createInstance(info.clazz, false, info.singleton);
        }
        return (T)info.instance;
    }
    
    protected void configureAdapters() {
        for (Adapters<? extends Adapter> adapters : _adapterMap.values()) {
            List<ComponentInfo<Adapter>> infos = adapters._infos;
            for (ComponentInfo<Adapter> info : infos) {
                try {
                    if (info.singleton) {
                        Singleton singleton = s_singletons.get(info.clazz);
                        if (singleton.state == Singleton.State.Instantiated) {
                            s_logger.info("Injecting singleton Adapter: " + info.getName());
                            inject(info.clazz, info.instance);
                            singleton.state = Singleton.State.Injected;
                        }
                        if (singleton.state == Singleton.State.Injected) {
                            s_logger.info("Configuring singleton Adapter: " + info.getName());
                            if (!info.instance.configure(info.name, info.params)) {
                                s_logger.error("Unable to configure adapter: " + info.name);
                                System.exit(1);
                            }
                            singleton.state = Singleton.State.Configured;
                        }
                    } else {
                        s_logger.info("Injecting Adapter: " + info.getName());
                        inject(info.clazz, info.instance);
                        s_logger.info("Configuring singleton Adapter: " + info.getName());
                        if (!info.instance.configure(info.name, info.params)) {
                            s_logger.error("Unable to configure adapter: " + info.name);
                            System.exit(1);
                        }
                    }
                } catch (ConfigurationException e) {
                    s_logger.error("Unable to configure adapter: " + info.name, e);
                    System.exit(1);
                } catch (Exception e) {
                    s_logger.error("Unable to configure adapter: " + info.name, e);
                    System.exit(1);
                }
            }
        }
    }

    protected void populateAdapters(Map<String, List<ComponentInfo<Adapter>>> map) {
        Set<Map.Entry<String, List<ComponentInfo<Adapter>>>> entries = map.entrySet();
        for (Map.Entry<String, List<ComponentInfo<Adapter>>> entry : entries) {
            for (ComponentInfo<Adapter> info : entry.getValue()) {
                s_logger.info("Instantiating Adapter: " + info.name);
                info.instance = (Adapter)createInstance(info.clazz, false, info.singleton);
            }
            Adapters<Adapter> adapters = new Adapters<Adapter>(entry.getKey(), entry.getValue());
            _adapterMap.put(entry.getKey(), adapters);
        }
    }
    
    protected void instantiateAdapters(Map<String, List<ComponentInfo<Adapter>>> map) {
        Set<Map.Entry<String, List<ComponentInfo<Adapter>>>> entries = map.entrySet();
        for (Map.Entry<String, List<ComponentInfo<Adapter>>> entry : entries) {
            for (ComponentInfo<Adapter> info : entry.getValue()) {
                s_logger.info("Instantiating Adapter: " + info.name);
                info.instance = (Adapter)createInstance(info.clazz, false, info.singleton);
            }
            Adapters<Adapter> adapters = new Adapters<Adapter>(entry.getKey(), entry.getValue());
            _adapterMap.put(entry.getKey(), adapters);
        }
    }

    protected void startAdapters() {
        for (Map.Entry<String, Adapters<? extends Adapter>> entry : _adapterMap.entrySet()) {
            for (ComponentInfo<Adapter> adapter : entry.getValue()._infos) {
                if (adapter.singleton) {
                    Singleton s = s_singletons.get(adapter.clazz);
                    if (s.state == Singleton.State.Configured) {
                        s_logger.info("Starting singleton Adapter: " + adapter.getName());
                        if (!adapter.instance.start()) {
                            throw new CloudRuntimeException("Unable to start adapter: " + adapter.getName());
                        }
                        if (adapter.instance instanceof ManagementBean) {
                            registerMBean((ManagementBean)adapter.instance);
                        }
                        s_logger.info("Started Adapter: " + adapter.instance.getName());
                    }
                    s.state = Singleton.State.Started;
                } else {
                    s_logger.info("Starting Adapter: " + adapter.getName());
                    if (!adapter.instance.start()) {
                        throw new CloudRuntimeException("Unable to start adapter: " + adapter.getName());
                    }
                    if (adapter.instance instanceof ManagementBean) {
                        registerMBean((ManagementBean)adapter.instance);
                    }
                    s_logger.info("Started Adapter: " + adapter.instance.getName());
                }
            }
        }
    }

    public static <T> T inject(Class<T> clazz) {
        return (T)createInstance(clazz, true, false);
    }
    
    public <T> T createInstance(Class<T> clazz) {
        Class<? extends T> impl = (Class<? extends T>)_factories.get(clazz);
        if (impl == null) {
            throw new CloudRuntimeException("Unable to find a factory for " + clazz);
        }
        return inject(impl);
    }
    
    public static <T> T inject(Class<T> clazz, Object... args) {
        return (T)createInstance(clazz, true, false, args);
    }
    
    @Override
    public Map<String, List<String>> getAdapterNames() {
        HashMap<String, List<String>> result = new HashMap<String, List<String>>();
        for (Map.Entry<String, Adapters<? extends Adapter>> entry : _adapterMap.entrySet()) {
            Adapters<? extends Adapter> adapters = entry.getValue();
            Enumeration<? extends Adapter> en = adapters.enumeration();
            List<String> lst = new ArrayList<String>();
            while (en.hasMoreElements()) {
                Adapter adapter = en.nextElement();
                lst.add(adapter.getName() + "-" + adapter.getClass().getName());
            }
            result.put(entry.getKey(), lst);
        }
        return result;
    }

    public Map<String, List<String>> getAllAccessibleAdapters() {
        Map<String, List<String>> parentResults = new HashMap<String, List<String>>();
        Map<String, List<String>> results = getAdapterNames();
        parentResults.putAll(results);
        return parentResults;
    }

    @Override
    public Collection<String> getManagerNames() {
        Collection<String> names = new HashSet<String>();
        for (Map.Entry<String, ComponentInfo<Manager>> entry : _managerMap.entrySet()) {
            names.add(entry.getValue().name);
        }
        return names;
    }

    @Override
    public Collection<String> getDaoNames() {
        Collection<String> names = new HashSet<String>();
        for (Map.Entry<String, ComponentInfo<GenericDao<?, ?>>> entry : _daoMap.entrySet()) {
            names.add(entry.getValue().name);
        }
        return names;
    }

    public <T extends Adapter> Adapters<T> getAdapters(Class<T> clazz) {
        return (Adapters<T>)getAdapters(clazz.getName());
    }

    public Adapters<? extends Adapter> getAdapters(String key) {
        Adapters<? extends Adapter> adapters = _adapterMap.get(key);
        if (adapters != null) {
            return adapters;
        }
        return new Adapters<Adapter>(key, new ArrayList<ComponentInfo<Adapter>>());
    }
    
    protected void resetInterceptors(InterceptorLibrary library) {
        library.addInterceptors(s_interceptors);
        if (s_interceptors.size() > 0) {
            s_callbacks = new Callback[s_interceptors.size() + 2];
            int i = 0;
            s_callbacks[i++] = NoOp.INSTANCE;
            s_callbacks[i++] = new InterceptorDispatcher();
            for (AnnotationInterceptor<?> interceptor : s_interceptors) {
                s_callbacks[i++] = interceptor.getCallback();
            }
            s_callbackFilter = new InterceptorFilter();
        }
    }

    protected static ComponentLocator getLocatorInternal(String server, boolean setInThreadLocal, String configFileName, String log4jFilename) {
        synchronized(s_once) {
            if (!s_once) {
                File file = PropertiesUtil.findConfigFile(log4jFilename + ".xml");
                if (file != null) {
                    s_logger.info("log4j configuration found at " + file.getAbsolutePath());
                    DOMConfigurator.configureAndWatch(file.getAbsolutePath());
                } else {
                    file = PropertiesUtil.findConfigFile(log4jFilename + ".properties");
                    if (file != null) {
                        s_logger.info("log4j configuration found at " + file.getAbsolutePath());
                        PropertyConfigurator.configureAndWatch(file.getAbsolutePath());
                    }
                }
                s_once = true;
            }
        }
        
        ComponentLocator locator;
        synchronized (s_locators) {
            locator = s_locators.get(server);
            if (locator == null) {
                locator = new ComponentLocator(server);
                s_locators.put(server, locator);
                if (setInThreadLocal) {
                    s_tl.set(locator);
                }
                locator.parse(configFileName);
            } else {
                if (setInThreadLocal) {
                    s_tl.set(locator);
                }
            }
        }

        return locator;
    }

    public static ComponentLocator getLocator(String server, String configFileName, String log4jFilename) {
        return getLocatorInternal(server, true, configFileName, log4jFilename);
    }

    public static ComponentLocator getLocator(String server) {
        String configFile = null;
        try {
            final File propsFile = PropertiesUtil.findConfigFile("environment.properties");
            if (propsFile == null) {
                s_logger.debug("environment.properties could not be opened");
            } else {
                final FileInputStream finputstream = new FileInputStream(propsFile);
                final Properties props = new Properties();
                props.load(finputstream);
                finputstream.close();
                configFile = props.getProperty("cloud-stack-components-specification");
            }
        } catch (IOException e) {
            s_logger.debug("environment.properties could not be loaded:" + e.toString());
        }
        
        if (configFile == null || PropertiesUtil.findConfigFile(configFile) == null) {
            configFile = "components-premium.xml";
            if (PropertiesUtil.findConfigFile(configFile) == null){
                configFile = "components.xml";
            }
        }
        return getLocatorInternal(server, true, configFile, "log4j-cloud");
    }

    public static ComponentLocator getCurrentLocator() {
        return s_tl.get();
    }

    public static class ComponentInfo<T> {
        Class<?>                clazz;
        HashMap<String, Object> params = new HashMap<String, Object>();
        String                  name;
        List<String>            keys = new ArrayList<String>();
        T                       instance;
        boolean                 singleton = true;
        
        protected ComponentInfo() {
        }
        
        public List<String> getKeys() {
            return keys;
        }
        
        public String getName() {
            return name;
        }
        
        public ComponentInfo(String name, Class<? extends T> clazz) {
            this(name, clazz, new ArrayList<Pair<String, Object>>(0));
        }
        
        public ComponentInfo(String name, Class<? extends T> clazz, T instance) {
            this(name, clazz);
            this.instance = instance;
        }
        
        public ComponentInfo(String name, Class<? extends T> clazz, List<Pair<String, Object>> params) {
            this(name, clazz, params, true);
        }
        
        public ComponentInfo(String name, Class<? extends T> clazz, List<Pair<String, Object>> params, boolean singleton) {
            this.name = name;
            this.clazz = clazz;
            this.singleton = singleton;
            for (Pair<String, Object> param : params) {
                this.params.put(param.first(), param.second());
            }
            fillInfo();
        }
        
        protected void fillInfo() {
            String clazzName = clazz.getName();
            
            Local local = clazz.getAnnotation(Local.class);
            if (local == null) {
                throw new CloudRuntimeException("Unable to find Local annotation for class " + clazzName);
            }

            // Verify that all interfaces specified in the Local annotation is implemented by the class.
            Class<?>[] classes = local.value();
            for (int i = 0; i < classes.length; i++) {
                if (!classes[i].isInterface()) {
                    throw new CloudRuntimeException(classes[i].getName() + " is not an interface");
                }
                if (classes[i].isAssignableFrom(clazz)) {
                    keys.add(classes[i].getName());
                    s_logger.info("Found component: " + classes[i].getName() + " in " + clazzName + " - " + name);
                } else {
                    throw new CloudRuntimeException(classes[i].getName() + " is not implemented by " + clazzName);
                }
            }
        }
        
        public void addParameter(String name, String value) {
            params.put(name, value);
        }
    }

    /**
     * XmlHandler is used by AdapterManager to handle the SAX parser callbacks.
     * It builds a hash map of lists of adapters and a hash map of managers.
     **/
    protected class XmlHandler extends DefaultHandler {
        public HashMap<String, List<ComponentInfo<Adapter>>>    adapters;
        public HashMap<String, ComponentInfo<Manager>>          managers;
        public HashMap<String, ComponentInfo<SystemIntegrityChecker>> checkers;
        public LinkedHashMap<String, ComponentInfo<GenericDao<?, ?>>> daos;
        public String                                  parent;
        public String                                  library;

        List<ComponentInfo<Adapter>>                            lst;
        String                                         paramName;
        StringBuilder                                  value;
        String                                         serverName;
        boolean                                        parse;
        ComponentInfo<?>                               currentInfo;
        Class<?>                                       componentClass;

        public XmlHandler(String serverName) {
            this.serverName = serverName;
            parse = false;
            adapters = new HashMap<String, List<ComponentInfo<Adapter>>>();
            managers = new HashMap<String, ComponentInfo<Manager>>();
            checkers = new HashMap<String, ComponentInfo<SystemIntegrityChecker>>();
            daos = new LinkedHashMap<String, ComponentInfo<GenericDao<?, ?>>>();
            value = null;
            parent = null;
        }

        protected void fillInfo(Attributes atts, Class<?> interphace, ComponentInfo<?> info) {
            String clazzName = getAttribute(atts, "class");
            if (clazzName == null) {
                throw new CloudRuntimeException("Missing class attribute for " + interphace.getName());
            }
            info.name = getAttribute(atts, "name");
            if (info.name == null) {
                throw new CloudRuntimeException("Missing name attribute for " + interphace.getName());
            }
            s_logger.debug("Looking for class " + clazzName);
            try {
                info.clazz = Class.forName(clazzName);
            } catch (ClassNotFoundException e) {
                throw new CloudRuntimeException("Unable to find class: " + clazzName);
            } catch (Throwable e) {
                throw new CloudRuntimeException("Caught throwable: ", e);
            }

            if (!interphace.isAssignableFrom(info.clazz)) {
                throw new CloudRuntimeException("Class " + info.clazz.toString() + " does not implment " + interphace);
            }
            String singleton = getAttribute(atts, "singleton");
            if (singleton != null) {
                info.singleton = Boolean.parseBoolean(singleton);
            }
            
            info.fillInfo();
        }
        
        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
        throws SAXException {
            if (qName.equals("interceptor") && s_interceptors.size() == 0) {
                synchronized(s_interceptors){
                    if (s_interceptors.size() == 0) {
                        String libraryName = getAttribute(atts, "library");
                        try {
                            Class<?> libraryClazz = Class.forName(libraryName);
                            InterceptorLibrary library = (InterceptorLibrary)libraryClazz.newInstance();
                            resetInterceptors(library);
                        } catch (ClassNotFoundException e) {
                            throw new CloudRuntimeException("Unable to find " + libraryName, e);
                        } catch (InstantiationException e) {
                            throw new CloudRuntimeException("Unable to instantiate " + libraryName, e);
                        } catch (IllegalAccessException e) {
                            throw new CloudRuntimeException("Illegal access " + libraryName, e);
                        }
                    }
                }
            }
            if (!parse) {
                if (qName.equals(_serverName)) {
                    parse = true;
                    parent = getAttribute(atts, "extends");
                    String implementationClass = getAttribute(atts, "class");
                    if (implementationClass != null) {
                        try {
                            componentClass = Class.forName(implementationClass);
                        } catch (ClassNotFoundException e) {
                            throw new CloudRuntimeException("Unable to find " + implementationClass, e);
                        }
                    }
                    
                    library = getAttribute(atts, "library");
                }
            } else if (qName.equals("adapters")) {
                lst = new ArrayList<ComponentInfo<Adapter>>();
                String key = getAttribute(atts, "key");
                if (key == null) {
                    throw new CloudRuntimeException("Missing key attribute for adapters");
                }
                adapters.put(key, lst);
            } else if (qName.equals("adapter")) {
                ComponentInfo<Adapter> info = new ComponentInfo<Adapter>();
                fillInfo(atts, Adapter.class, info);
                lst.add(info);
                currentInfo = info;
            } else if (qName.equals("manager")) {
                ComponentInfo<Manager> info = new ComponentInfo<Manager>();
                fillInfo(atts, Manager.class, info);
                s_logger.info("Adding Manager: " + info.name);
                for (String key : info.keys) {
                    s_logger.info("Linking " + key + " to " + info.name);
                    managers.put(key, info);
                }
                currentInfo = info;
            } else if (qName.equals("param")) {
                paramName = getAttribute(atts, "name");
                value = new StringBuilder();
            } else if (qName.equals("dao")) {
                ComponentInfo<GenericDao<?, ?>> info = new ComponentInfo<GenericDao<?, ?>>();
                fillInfo(atts, GenericDao.class, info);
                for (String key : info.keys) {
                    daos.put(key, info);
                }
                currentInfo = info;
            } else if (qName.equals("checker")) {
                ComponentInfo<SystemIntegrityChecker> info = new ComponentInfo<SystemIntegrityChecker>();
                fillInfo(atts, SystemIntegrityChecker.class, info);
                checkers.put(info.name, info);
                s_logger.info("Adding system integrity checker: " + info.name);
                currentInfo = info;
            } else {
                // ignore
            }
        }

        protected String getAttribute(Attributes atts, String name) {
            for (int att = 0; att < atts.getLength(); att++) {
                String attName = atts.getQName(att);
                if (attName.equals(name)) {
                    return atts.getValue(att);
                }
            }
            return null;
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            if (!parse) {
                return;
            }

            if (qName.equals(_serverName)) {
                parse = false;
            } else if (qName.equals("adapters")) {
            } else if (qName.equals("adapter")) {
            } else if (qName.equals("manager")) {
            } else if (qName.equals("dao")) {
            } else if (qName.equals("param")) {
                currentInfo.params.put(paramName, value.toString());
                paramName = null;
                value = null;
            } else {
                // ignore
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (parse && value != null) {
                value.append(ch, start, length);
            }
        }
    }
    
    protected static class InjectInfo {
        public Factory factory;
        public Enhancer enhancer;
        
        public InjectInfo(Enhancer enhancer, Factory factory) {
            this.factory = factory;
            this.enhancer = enhancer;
        }
    }
    
    protected static class CleanupThread extends Thread {
        @Override
        public void run() {
            synchronized (CleanupThread.class) {
                for (ComponentLocator locator : s_locators.values()) {
                    Iterator<Adapters<? extends Adapter>> itAdapters = locator._adapterMap.values().iterator();
                    while (itAdapters.hasNext()) {
                        Adapters<? extends Adapter> adapters = itAdapters.next();
                        itAdapters.remove();
                        for (ComponentInfo<Adapter> adapter : adapters._infos) {
                            if (adapter.singleton) {
                                Singleton singleton = s_singletons.get(adapter.clazz);
                                if (singleton.state == Singleton.State.Started) {
                                    s_logger.info("Asking " + adapter.getName() + " to shutdown.");
                                    adapter.instance.stop();
                                    singleton.state = Singleton.State.Stopped;
                                } else {
                                    s_logger.debug("Skippng " + adapter.getName() + " because it has already stopped");
                                }
                            } else {
                                s_logger.info("Asking " + adapter.getName() + " to shutdown.");
                                adapter.instance.stop();
                            }
                        }
                    }
                }
                
                for (ComponentLocator locator : s_locators.values()) {
                    Iterator<ComponentInfo<Manager>> itManagers = locator._managerMap.values().iterator();
                    while (itManagers.hasNext()) {
                        ComponentInfo<Manager> manager = itManagers.next();
                        itManagers.remove();
                        if (manager.singleton == true) {
                            Singleton singleton = s_singletons.get(manager.clazz);
                            if (singleton != null && singleton.state == Singleton.State.Started) {
                                s_logger.info("Asking Manager " + manager.getName() + " to shutdown.");
                                manager.instance.stop();
                                singleton.state = Singleton.State.Stopped;
                            } else {
                                s_logger.info("Skipping Manager " + manager.getName() + " because it is not in a state to shutdown.");
                            }
                        }
                    }
                }
            }
        }
    }
    
    static class Singleton {
        public enum State {
            Instantiated,
            Injected,
            Configured,
            Started,
            Stopped
        }
        
        public Object singleton;
        public State state;
        
        public Singleton(Object singleton) {
            this.singleton = singleton;
            this.state = State.Instantiated;
        }
    }
    
    protected class InterceptorDispatcher implements MethodInterceptor {

        @Override
        public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            ArrayList<Pair<AnnotationInterceptor<Object>, Object>> interceptors = new ArrayList<Pair<AnnotationInterceptor<Object>, Object>>();
            for (AnnotationInterceptor<?> interceptor : s_interceptors) {
                if (interceptor.needToIntercept(method)) {
                    Object obj = interceptor.interceptStart(method);
                    interceptors.add(new Pair<AnnotationInterceptor<Object>, Object>((AnnotationInterceptor<Object>)interceptor, obj));
                }
            }
            boolean success = false;
            try {
                Object obj = methodProxy.invokeSuper(object, args);
                success = true;
                return obj;
            } finally {
                for (Pair<AnnotationInterceptor<Object>, Object> interceptor : interceptors) {
                    if (success) {
                        interceptor.first().interceptComplete(method, interceptor.second());
                    } else {
                        interceptor.first().interceptException(method, interceptor.second());
                    }
                }
            }
        }
    }
    
    protected static class InterceptorFilter implements CallbackFilter {
        @Override
        public int accept(Method method) {
            int index = 0;
            for (int i = 2; i < s_callbacks.length; i++) {
                AnnotationInterceptor<?> interceptor = (AnnotationInterceptor<?>)s_callbacks[i];
                if (interceptor.needToIntercept(method)) {
                    if (index == 0) {
                        index = i;
                    } else {
                        return 1;
                    }
                }
            }
            
            return index;
        }
    }
}
