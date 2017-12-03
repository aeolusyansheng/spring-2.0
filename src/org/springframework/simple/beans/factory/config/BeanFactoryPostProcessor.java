package org.springframework.simple.beans.factory.config;

import org.springframework.simple.beans.BeansException;

public interface BeanFactoryPostProcessor {

	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;
}
