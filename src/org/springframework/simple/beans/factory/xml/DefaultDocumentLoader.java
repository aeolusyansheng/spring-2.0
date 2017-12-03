package org.springframework.simple.beans.factory.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

public class DefaultDocumentLoader implements DocumentLoader {

	/**
	 * JAXP attribute used to configure the schema language for validation.
	 */
	private static final String SCHEMA_LANGUAGE_ATTRIBUTE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

	/**
	 * JAXP attribute value indicating the XSD schema language.
	 */
	private static final String XSD_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

	protected final Log logger = LogFactory.getLog(getClass());

	public Document loadDocument(InputSource inputSource, EntityResolver entityResolver, ErrorHandler errorHandler,
			int validationMode, boolean namespaceAware) throws Exception {
		DocumentBuilderFactory fatory = createDocumentBuilderFactory(validationMode, namespaceAware);
		DocumentBuilder builder = createDocumentBuilder(fatory, entityResolver, errorHandler);
		return builder.parse(inputSource);
	}

	protected DocumentBuilderFactory createDocumentBuilderFactory(int validationMode, boolean namespaceAware)
			throws ParserConfigurationException {

		// DocumentBuilderFactory需要设置的属性：namespaceAware，validation，attribute
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(namespaceAware);

		if (validationMode != XmlBeanDefinitionReader.VALIDATION_NONE) {
			factory.setValidating(true);

			if (validationMode == XmlBeanDefinitionReader.VALIDATION_XSD) {
				factory.setNamespaceAware(true);
				factory.setAttribute(SCHEMA_LANGUAGE_ATTRIBUTE, XSD_SCHEMA_LANGUAGE);
			}
		} else {
			factory.setValidating(false);
		}

		return factory;

	}

	protected DocumentBuilder createDocumentBuilder(DocumentBuilderFactory factory, EntityResolver entityResolver,
			ErrorHandler errorHandler) throws ParserConfigurationException {

		// DocumentBuilder需要设置的属性：errorHandle,entityResolver
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		if (entityResolver != null) {
			documentBuilder.setEntityResolver(entityResolver);
		}
		if (errorHandler != null) {
			documentBuilder.setErrorHandler(errorHandler);
		}
		return documentBuilder;
	}

}
