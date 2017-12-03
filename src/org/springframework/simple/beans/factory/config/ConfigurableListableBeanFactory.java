package org.springframework.simple.beans.factory.config;

import org.springframework.simple.beans.BeansException;
import org.springframework.simple.beans.factory.ListableBeanFactory;

public interface ConfigurableListableBeanFactory
		extends ConfigurableBeanFactory, AutowireCapableBeanFactory, ListableBeanFactory {

	void ignoreDependencyInterface(Class ignore);

	void ignoreDependencyType(Class type);

	BeanDefinition getBeanDefinition(String beanName);

	// 实例化所有非延迟加载单例bean
	void preInstantiateSingleTons() throws BeansException;

}
