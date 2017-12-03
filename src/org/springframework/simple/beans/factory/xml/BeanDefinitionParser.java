package org.springframework.simple.beans.factory.xml;

import org.springframework.simple.beans.factory.config.BeanDefinition;
import org.w3c.dom.Element;

public interface BeanDefinitionParser {

	BeanDefinition parse(Element el, ParserContext parserContext);
}
