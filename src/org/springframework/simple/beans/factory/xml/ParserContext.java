package org.springframework.simple.beans.factory.xml;

import org.springframework.simple.beans.factory.config.BeanDefinition;
import org.springframework.simple.beans.factory.support.BeanDefinitionRegistry;

public final class ParserContext {

	private XmlReaderContext readerContext;
	private BeanDefinitionParserDelegate delegate;
	private BeanDefinition containingBeanDefinition;
	
	public ParserContext(XmlReaderContext readerContext, BeanDefinitionParserDelegate delegate) {
		this(readerContext, delegate, null);
	}

	public ParserContext(XmlReaderContext readerContext, BeanDefinitionParserDelegate delegate,
			BeanDefinition containingBeanDefinition) {

		this.readerContext = readerContext;
		this.delegate = delegate;
		this.containingBeanDefinition = containingBeanDefinition;
	}


	public XmlReaderContext getReaderContext() {
		return this.readerContext;
	}

	public BeanDefinitionRegistry getRegistry() {
		return getReaderContext().getRegistry();
	}

	public BeanDefinitionParserDelegate getDelegate() {
		return this.delegate;
	}

	public BeanDefinition getContainingBeanDefinition() {
		return this.containingBeanDefinition;
	}

	public boolean isNested() {
		return (getContainingBeanDefinition() != null);
	}

	public Object extractSource(Object sourceCandidate) {
		return getReaderContext().extractSource(sourceCandidate);
	}
	
}
