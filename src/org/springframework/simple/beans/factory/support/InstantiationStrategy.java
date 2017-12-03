package org.springframework.simple.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.springframework.simple.beans.BeansException;
import org.springframework.simple.beans.factory.BeanFactory;

public interface InstantiationStrategy {

	Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner) throws BeansException;

	Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner, Constructor ctor,
			Object[] args) throws BeansException;

	Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner, Object factoryBean,
			Method factoryMethod, Object[] args) throws BeansException;
}
