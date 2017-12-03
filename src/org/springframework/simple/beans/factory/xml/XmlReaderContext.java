package org.springframework.simple.beans.factory.xml;

import org.springframework.core.io.Resource;
import org.springframework.simple.beans.factory.parsing.ProblemReporter;
import org.springframework.simple.beans.factory.parsing.ReaderContext;
import org.springframework.simple.beans.factory.parsing.ReaderEventListener;
import org.springframework.simple.beans.factory.parsing.SourceExtracter;
import org.springframework.simple.beans.factory.support.BeanDefinitionRegistry;

public class XmlReaderContext extends ReaderContext {

	private XmlBeanDefinitionReader reader;
	private NamespaceHandlerResolver namespaceHandlerResolver;

	public XmlReaderContext(Resource resource, ProblemReporter problemReporter, SourceExtracter sourceExtractor,
			ReaderEventListener readerEventListener, XmlBeanDefinitionReader reader, NamespaceHandlerResolver resolver) {
		super(resource, problemReporter, sourceExtractor, readerEventListener);
		this.namespaceHandlerResolver = resolver;
		this.reader = reader;
	}

	public XmlBeanDefinitionReader getBeanDefinitionReader() {
		return this.reader;
	}

	public BeanDefinitionRegistry getRegistry() {
		return this.reader.getBeanFactory();
	}

	public NamespaceHandlerResolver getNamespaceHandlerResolver() {
		return this.namespaceHandlerResolver;
	}

}
