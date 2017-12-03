package org.springframework.simple.beans.factory.xml;

import org.springframework.simple.beans.factory.support.AbstractBeanDefinition;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import pojo.MyCustomNamespacePojo;

public class MyCustomBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

	@SuppressWarnings("rawtypes")
	protected Class getBeanClass(Element el) {
		return MyCustomNamespacePojo.class;
	}

	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
		String id = super.resolveId(element, definition, parserContext);
		if (!StringUtils.hasText(id)) {
			id = element.getAttribute("name");
		}
		return id;
	}

}
