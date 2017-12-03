package org.springframework.simple.beans.factory.xml;

import org.springframework.simple.beans.factory.config.BeanDefinitionHolder;
import org.w3c.dom.Node;

public interface BeanDefinitionDecorator {

	BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder holder, ParserContext parserContext);
}
