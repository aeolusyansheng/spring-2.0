package org.springframework.simple.beans.factory.xml;

import org.springframework.simple.beans.factory.BeanDefinitionStoreException;
import org.w3c.dom.Document;

public interface BeanDefinitionDocumentReader {

	void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) throws BeanDefinitionStoreException;
}
