package org.springframework.simple.beans.factory.config;

import java.beans.PropertyDescriptor;

import org.springframework.simple.beans.BeansException;
import org.springframework.simple.beans.PropertyValues;

public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

	Object postProcessBeforeInstantiation(Class beanClass, String beanName) throws BeansException;

	boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException;

	PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName)
			throws BeansException;

}
