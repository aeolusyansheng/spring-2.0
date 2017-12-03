package org.springframework.simple.beans.factory.xml;

public class UtilNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("constant", new ConstantBeanDefinitionParser());
		registerBeanDefinitionParser("property-path", new PropertyPathBeanDefinitionParser());
		registerBeanDefinitionParser("list", new ListBeanDefinitionParser());
		registerBeanDefinitionParser("set", new SetBeanDefinitionParser());
		registerBeanDefinitionParser("map", new MapBeanDefinitionParser());
		registerBeanDefinitionParser("properties", new PropertiesBeanDefinitionParser());
	}

}
