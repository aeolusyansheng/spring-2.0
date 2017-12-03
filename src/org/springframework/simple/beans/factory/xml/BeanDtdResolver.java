package org.springframework.simple.beans.factory.xml;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class BeanDtdResolver implements EntityResolver {

	private static final String DTD_EXTENSION = ".dtd";

	private static final String[] DTD_NAMES = { "spring-beans-2.0", "spring-beans" };

	private static final Log logger = LogFactory.getLog(BeanDtdResolver.class);

	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		if (logger.isDebugEnabled()) {
			logger.debug(
					"Trying to resolve XML entity with public ID [" + publicId + "] and system ID [" + systemId + "]");
		}
		if (systemId != null && systemId.endsWith(DTD_EXTENSION)) {
			int lastPathSeparator = systemId.lastIndexOf("/");
			for (int i = 0; i < DTD_NAMES.length; ++i) {
				int dtdNameStart = systemId.indexOf(DTD_NAMES[i]);
				if (dtdNameStart > lastPathSeparator) {
					String dtdFile = systemId.substring(dtdNameStart);
					if (logger.isDebugEnabled()) {
						logger.debug("Trying to locate [" + dtdFile + "] in Spring jar");
					}
					try {
						Resource resource = new ClassPathResource(dtdFile, getClass());
						InputSource source = new InputSource(resource.getInputStream());
						source.setPublicId(publicId);
						source.setSystemId(systemId);
						if (logger.isDebugEnabled()) {
							logger.debug("Found beans DTD [" + systemId + "] in classpath");
						}
						return source;
					} catch (IOException ex) {
						if (logger.isDebugEnabled()) {
							logger.debug("Could not resolve beans DTD [" + systemId + "]: not found in class path", ex);
						}
					}

				}
			}
		}

		// Use the default behavior -> download from website or wherever.
		return null;
	}

}
