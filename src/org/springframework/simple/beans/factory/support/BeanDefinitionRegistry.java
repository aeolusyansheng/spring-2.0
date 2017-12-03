package org.springframework.simple.beans.factory.support;

import org.springframework.simple.beans.factory.config.BeanDefinition;

public interface BeanDefinitionRegistry {

	boolean containsBeanDefinition(String name);

	String[] getAliases(String name);

	BeanDefinition getBeanDefinition(String name);

	int getBeanDefinitionCount();

	String[] getBeanDefinitionNames();

	void registerBeanDefinition(String name, BeanDefinition beanDefinition);

	void registerAlias(String beanName,String alias);
}
