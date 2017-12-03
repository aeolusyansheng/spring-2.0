package org.springframework.simple.beans.factory;

import java.util.Map;

import org.springframework.simple.beans.BeansException;

public interface ListableBeanFactory extends BeanFactory {

	boolean containsBeanDefinition(String name);

	int getBeanDefinitionCount();

	String[] getBeanDefinitionNames();

	String[] getBeanNamesForType(Class type);

	String[] getBeanNamesForType(Class type, boolean includePrototypes, boolean allowEagerInit);

	Map getBeanOfType(Class type) throws BeansException;

	Map getBeanOfType(Class type, boolean includePrototypes, boolean allowEagerInit)
			throws BeansException;
}
