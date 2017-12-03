package org.springframework.simple.beans.factory.xml;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

public class DefaultNamespaceHandlerResolver implements NamespaceHandlerResolver {

	private final static String SRING_HANDLER_MAPPINGS_LOCATION = "META-INF/simple-spring.handlers";
	protected final Log logger = LogFactory.getLog(getClass());
	@SuppressWarnings("rawtypes")
	private Map handleMapping;

	public DefaultNamespaceHandlerResolver() {
		this(null, SRING_HANDLER_MAPPINGS_LOCATION);
	}

	public DefaultNamespaceHandlerResolver(ClassLoader classLoader) {
		this(classLoader, SRING_HANDLER_MAPPINGS_LOCATION);
	}

	@SuppressWarnings("rawtypes")
	public DefaultNamespaceHandlerResolver(ClassLoader classLoader, String handlerMappingsLocation) {
		Assert.hasText(handlerMappingsLocation, "handlerMappingsLocation不能为空。");
		ClassLoader cLassLoaderToUse = classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader();
		this.handleMapping = new HashMap();
		initMappingHandles(cLassLoaderToUse, handlerMappingsLocation);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initMappingHandles(ClassLoader classLoader, String handlerMappingsLocation) {
		Properties properties = loadMapping(classLoader, handlerMappingsLocation);
		for (Enumeration en = properties.propertyNames(); en.hasMoreElements();) {
			String namespaceUrl = (String) en.nextElement();
			String className = properties.getProperty(namespaceUrl);
			// className->class
			try {
				Class handlerClazz = ClassUtils.forName(className, classLoader);
				if (!NamespaceHandler.class.isAssignableFrom(handlerClazz)) {
					throw new IllegalArgumentException(
							"Class [" + className + "] does not implement the NamespaceHandler interface");
				}
				// class->instance
				NamespaceHandler namespaceHandler = (NamespaceHandler) BeanUtils.instantiateClass(handlerClazz);
				namespaceHandler.init();
				this.handleMapping.put(namespaceUrl, namespaceHandler);

			} catch (ClassNotFoundException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Ignoring handler [" + className + "]: class not found", e);
				}
			} catch (LinkageError e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Ignoring handler [" + className + "]: problem with class file or dependent class", e);
				}
			}

		}

	}

	private Properties loadMapping(ClassLoader classLoader, String handlerMappingsLocation) {

		try {
			return PropertiesLoaderUtils.loadAllProperties(handlerMappingsLocation, classLoader);
		} catch (IOException e) {
			throw new IllegalStateException("无法导入 [" + handlerMappingsLocation + "]. 原因: " + e);
		}
	}

	@Override
	public NamespaceHandler resolve(String namespaceUrl) {
		// TODO Auto-generated method stub
		return (NamespaceHandler) this.handleMapping.get(namespaceUrl);
	}

}
