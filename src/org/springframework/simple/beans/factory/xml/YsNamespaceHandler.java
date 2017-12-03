package org.springframework.simple.beans.factory.xml;

public class YsNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("test", new MyCustomBeanDefinitionParser());
	}

}
