package org.springframework.simple.beans.factory.xml;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DelegatingEntityResolver implements EntityResolver {

	public static final String DTD_SUFFIX = ".dtd";
	public static final String XSD_SUFFIX = ".xsd";

	protected final Log logger = LogFactory.getLog(getClass());

	private final EntityResolver dtdResolver;
	private final EntityResolver schemaResolver;

	public DelegatingEntityResolver(ClassLoader classLoader) {
		this.dtdResolver = new BeanDtdResolver();
		if (classLoader == null) {
			classLoader = ClassUtils.getDefaultClassLoader();
		}
		this.schemaResolver = new PluggableSchemaResolver(classLoader);
		
		
	}

	public DelegatingEntityResolver(EntityResolver dtdResolver, EntityResolver schemaResolver) {
		Assert.notNull(dtdResolver);
		Assert.notNull(schemaResolver);
		this.dtdResolver = dtdResolver;
		this.schemaResolver = schemaResolver;
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

		if (systemId.endsWith(DTD_SUFFIX)) {
			return innerResolveEntity(publicId, systemId, this.dtdResolver, "DTD");
		} else if (systemId.endsWith(XSD_SUFFIX)) {
			return innerResolveEntity(publicId, systemId, this.schemaResolver, "XML Schema");
		}

		return null;
	}

	private InputSource innerResolveEntity(String publicId, String systemId, EntityResolver entityResolver, String type)
			throws SAXException, IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Attempting to resolve " + type + " [" + systemId + "] using ["
					+ entityResolver.getClass().getName() + "]");
		}

		return entityResolver.resolveEntity(publicId, systemId);
	}

}
