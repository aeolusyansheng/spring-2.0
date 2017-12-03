package org.springframework.simple.beans.factory.support;

import org.springframework.core.io.Resource;

public interface BeanDefinitionReader {

	ClassLoader getBeanClassLoader();

	BeanDefinitionRegistry getBeanFactory();

	int loadBeanDefinitions(Resource resource);
}
