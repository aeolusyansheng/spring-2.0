package org.springframework.simple.beans.factory.xml;

import org.springframework.core.Conventions;
import org.springframework.simple.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public abstract class AbstractSimpleBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	protected final void doParse(Element el, BeanDefinitionBuilder builder) {
		NamedNodeMap attributes = el.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Attr attribute = (Attr) attributes.item(i);
			String attrName = attribute.getLocalName();
			if ("id".equals(attrName)) {
				continue;
			}
			String propertyName = extractPropertyName(attrName);
			if (!StringUtils.hasText(propertyName)) {
				throw new IllegalStateException("属性名称不能为空。");
			}
			String propertyValue = attribute.getValue();
			builder.addPropertyValue(propertyName, propertyValue);
		}
		postProcess(el, builder);
	}

	protected String extractPropertyName(String attributeName) {
		return Conventions.attributeNameToPropertyName(attributeName);
	}

	protected void postProcess(Element el, BeanDefinitionBuilder builder) {
		// 子类可扩展。
	}
}
