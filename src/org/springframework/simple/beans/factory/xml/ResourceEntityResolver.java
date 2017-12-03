package org.springframework.simple.beans.factory.xml;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ResourceEntityResolver extends DelegatingEntityResolver {

	private final ResourceLoader resourceLoader;

	/**
	 * Create a ResourceEntityResolver for the specified ResourceLoader (usually, an
	 * ApplicationContext).
	 * 
	 * @param resourceLoader
	 *            the ResourceLoader (or ApplicationContext) to load XML entity
	 *            includes with
	 */
	public ResourceEntityResolver(ResourceLoader resourceLoader) {
		super(resourceLoader.getClassLoader());
		this.resourceLoader = resourceLoader;
	}

	@SuppressWarnings("deprecation")
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		InputSource source = super.resolveEntity(publicId, systemId);
		if (source == null && systemId != null) {
			String resourcePath = null;
			try {
				String decodedSystemId = URLDecoder.decode(systemId);
				String givenUrl = new URL(decodedSystemId).toString();
				String systemRootUrl = new File("").toURL().toString();
				// try relative to resource base if currently in system root
				if (givenUrl.startsWith(systemRootUrl)) {
					resourcePath = givenUrl.substring(systemRootUrl.length());
				}
			} catch (MalformedURLException ex) {
				// no URL -> try relative to resource base
				resourcePath = systemId;
			}
			if (resourcePath != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Trying to locate XML entity [" + systemId + "] as resource [" + resourcePath + "]");
				}
				Resource resource = this.resourceLoader.getResource(resourcePath);
				if (logger.isDebugEnabled()) {
					logger.debug("Found XML entity [" + systemId + "] as resource [" + resourcePath + "]");
				}
				source = new InputSource(resource.getInputStream());
				source.setPublicId(publicId);
				source.setSystemId(systemId);
			}
		}
		return source;
	}

}
