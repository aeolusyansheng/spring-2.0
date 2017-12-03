package org.springframework.simple.beans.factory.support;

import org.springframework.simple.beans.PropertyValue;

public class BeanDefinitionBuilder {

	private AbstractBeanDefinition beanDefinition;

	private BeanDefinitionBuilder() {

	}

	@SuppressWarnings("rawtypes")
	public static BeanDefinitionBuilder rootBeanDefinition(Class beanClass) {
		return rootBeanDefinition(beanClass, null);
	}

	@SuppressWarnings("rawtypes")
	public static BeanDefinitionBuilder rootBeanDefinition(Class beanClass, String factoryMethod) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new RootBeanDefinition();
		builder.beanDefinition.setBeanClass(beanClass);
		builder.beanDefinition.setFactoryMethodName(factoryMethod);
		return builder;
	}

	public AbstractBeanDefinition getBeanDefinition() {
		this.beanDefinition.validate();
		return this.beanDefinition;
	}

	public BeanDefinitionBuilder setSource(Object source) {
		this.beanDefinition.setSource(source);
		return this;
	}

	public BeanDefinitionBuilder setSingleton(boolean singleton) {
		this.beanDefinition.setSingleton(singleton);
		return this;
	}

	public BeanDefinitionBuilder addPropertyValue(String name, Object value) {
		this.beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue(name, value));
		return this;
	}

}
