package org.springframework.simple.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.simple.beans.BeanUtils;
import org.springframework.simple.beans.BeansException;
import org.springframework.simple.beans.factory.BeanDefinitionStoreException;
import org.springframework.simple.beans.factory.BeanFactory;

public class SimpleInstantiationStrategy implements InstantiationStrategy {

	protected final Log logger = LogFactory.getLog(getClass());

	@Override
	public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner)
			throws BeansException {
		if (beanDefinition.getMethodOverrides().isEmpty()) {
			return BeanUtils.instantiateClass(beanDefinition.getBeanClass());
		} else {
			return instantiateWithMethodInjection(beanDefinition, beanName, owner);
		}
	}

	/**
	 * 子类覆盖
	 * 
	 * @param beanDefinition
	 * @param beanName
	 * @param owner
	 * @return
	 */
	protected Object instantiateWithMethodInjection(RootBeanDefinition beanDefinition, String beanName,
			BeanFactory owner) {
		throw new UnsupportedOperationException("Method Injection not supported in SimpleInstantiationStrategy");
	}

	@Override
	public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner, Constructor ctor,
			Object[] args) throws BeansException {
		if (beanDefinition.getMethodOverrides().isEmpty()) {
			return BeanUtils.instantiateClass(ctor, args);
		} else {
			return instantiateWithMethodInjection(beanDefinition, beanName, owner, ctor, args);
		}
	}

	protected Object instantiateWithMethodInjection(RootBeanDefinition beanDefinition, String beanName,
			BeanFactory owner, Constructor ctor, Object[] args) {
		throw new UnsupportedOperationException("Method Injection not supported in SimpleInstantiationStrategy");
	}

	@Override
	public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner, Object factoryBean,
			Method factoryMethod, Object[] args) throws BeansException {
		try {
			if (!Modifier.isPublic(factoryMethod.getModifiers())
					|| !Modifier.isPublic(factoryMethod.getDeclaringClass().getModifiers())) {
				factoryMethod.setAccessible(true);
			}
			return factoryMethod.invoke(factoryBean, args);
		} catch (IllegalAccessException e) {
			throw new BeanDefinitionStoreException("权限异常。");
		} catch (IllegalArgumentException e) {
			throw new BeanDefinitionStoreException("参数异常。");
		} catch (InvocationTargetException e) {
			throw new BeanDefinitionStoreException("对象创建异常。");
		}
	}

}
