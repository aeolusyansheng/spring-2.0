package org.springframework.simple.context.support;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.simple.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.simple.beans.factory.xml.ResourceEntityResolver;
import org.springframework.simple.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.simple.context.ApplicationContext;

public abstract class AbstractXmlApplicationContext extends AbstractRefreshableApplicationContext {

	// with parent
	public AbstractXmlApplicationContext(ApplicationContext parent) {
		super(parent);
	}

	// without parent
	public AbstractXmlApplicationContext() {

	}

	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException {

		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
		//beanDefinitionReader.setResourceLoader(this);
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));
		initBeanDefinitionReader(beanDefinitionReader);
		loadBeanDefinitions(beanDefinitionReader);
	}

	protected void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
		// 空方法，让子类添加动作
	}

	protected void loadBeanDefinitions(XmlBeanDefinitionReader beanDefinitionReader)
			throws BeansException, IOException {

		String[] configLocations = getConfigLocations();
		if (configLocations != null) {
			// 委托Reader完成加载Bean定义
			beanDefinitionReader.loadBeanDefinitions(configLocations);
		}
	}

	protected String[] getConfigLocations() {
		// 具体实现留给子类
		return null;
	}

}
