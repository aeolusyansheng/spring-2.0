package org.springframework.simple.beans.factory.config;

public interface SingletonBeanRegistry {

	boolean containsSingleton(String beanName);

	Object getSingleton(String beanName);

	int getSingletonCount();

	String[] getSingletonNames();

	void registerSingleton(String beanName, Object singletonObject);
}
