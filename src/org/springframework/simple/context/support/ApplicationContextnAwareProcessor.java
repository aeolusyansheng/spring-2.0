package org.springframework.simple.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.simple.beans.BeansException;
import org.springframework.simple.beans.factory.config.BeanPostProcessor;
import org.springframework.simple.context.ApplicationContext;
import org.springframework.simple.context.ApplicationContextAware;
import org.springframework.simple.context.ApplicationEventPublisherAware;
import org.springframework.simple.context.MessageSourceAware;
import org.springframework.simple.context.ResourceLoaderAware;

public class ApplicationContextnAwareProcessor implements BeanPostProcessor {
	// 本身是一个BeanPostProcessor，用于加载AppcationContext的各个Aware

	protected final Log loggor = LogFactory.getLog(getClass());

	private final ApplicationContext applicationContext;

	public ApplicationContextnAwareProcessor(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		// 依次加载以下aware
		// ResourceLoaderAware
		// ApplicationEventPublisherAware
		// MessageSourceAware
		// ApplicationContextAware

		if (bean instanceof ResourceLoaderAware) {
			((ResourceLoaderAware) bean).setResourceLoader(this.applicationContext);
		}

		if (bean instanceof ApplicationEventPublisherAware) {
			((ApplicationEventPublisherAware) bean).setApplicationEventPublisher(this.applicationContext);
		}

		if (bean instanceof MessageSourceAware) {
			((MessageSourceAware) bean).setMessageSource(this.applicationContext);
		}

		if (bean instanceof ApplicationContextAware) {
			((ApplicationContextAware) bean).setApplicationContext(this.applicationContext);
		}

		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
