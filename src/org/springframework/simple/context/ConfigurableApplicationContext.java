package org.springframework.simple.context;

import org.springframework.simple.beans.BeansException;
import org.springframework.simple.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.simple.beans.factory.config.ConfigurableListableBeanFactory;

public interface ConfigurableApplicationContext extends ApplicationContext ,Lifecycle{

	void close();

	ConfigurableListableBeanFactory getBeanFactory();

	void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor);

	void refresh() throws BeansException;

	void setparent(ApplicationContext parent);
	void registerShutdownHook();
	boolean isActive();

}
