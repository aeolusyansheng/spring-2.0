package org.springframework.simple.beans.factory.config;

import org.springframework.simple.beans.BeansException;

public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {

	void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException;
}
