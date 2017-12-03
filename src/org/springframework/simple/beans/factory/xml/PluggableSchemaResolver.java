package org.springframework.simple.beans.factory.xml;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.Assert;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PluggableSchemaResolver implements EntityResolver {

	private static final String DEFAULT_SCHEMA_MAPPINGS_LOCATION = "META-INF/spring.schemas";

	private final Log logger = LogFactory.getLog(getClass());

	private ClassLoader classLoader;

	private Properties schemaMappings;

	public PluggableSchemaResolver(ClassLoader cLassLoader) {
		this(cLassLoader, DEFAULT_SCHEMA_MAPPINGS_LOCATION);
	}

	public PluggableSchemaResolver(ClassLoader cLassLoader, String schemaMappingsLocation) {
		Assert.notNull(cLassLoader, "cLassLoader 不能为空");
		Assert.hasText(schemaMappingsLocation, "schemaMappingsLocation不能为空");
		this.classLoader = cLassLoader;
		if (logger.isDebugEnabled()) {
			logger.debug("Loading schema mappings from [" + schemaMappingsLocation + "].");
		}
		try {
			this.schemaMappings = PropertiesLoaderUtils.loadAllProperties(schemaMappingsLocation, cLassLoader);
			if (logger.isDebugEnabled()) {
				logger.debug("Loaded schema mappings: " + this.schemaMappings);
			}
		} catch (IOException e) {
			throw new FatalBeanException(
					"Unable to load schema mappings from location [" + schemaMappingsLocation + "].", e);
		}
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

		if (systemId != null) {
			String resourceLocation = this.schemaMappings.getProperty(systemId);
			Resource resource = new ClassPathResource(resourceLocation, this.classLoader);
			InputSource source = new InputSource(resource.getInputStream());
			source.setSystemId(systemId);
			source.setPublicId(publicId);
			return source;
		}
		return null;
	}

}
