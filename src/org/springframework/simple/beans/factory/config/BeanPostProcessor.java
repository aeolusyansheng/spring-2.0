package org.springframework.simple.beans.factory.config;

import org.springframework.simple.beans.BeansException;

public interface BeanPostProcessor {

	Object postProcessBeforeInitialization(Object bean,String beanName) throws BeansException;
	Object postProcessAfterInitialization(Object bean,String beanName) throws BeansException;
}
