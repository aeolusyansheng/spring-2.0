package org.springframework.simple.beans.factory.xml;

import org.springframework.simple.beans.factory.config.BeanDefinition;
import org.springframework.simple.beans.factory.config.BeanDefinitionHolder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface NamespaceHandler {

	void init();

	BeanDefinition parse(Element el, ParserContext parserContext);

	BeanDefinitionHolder decorate(Node el, BeanDefinitionHolder definition, ParserContext parserContext);
}
