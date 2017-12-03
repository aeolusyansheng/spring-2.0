package org.springframework.simple.beans.factory.support;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.CollectionFactory;
import org.springframework.simple.beans.BeanWrapper;
import org.springframework.simple.beans.BeansException;
import org.springframework.simple.beans.SimpleTypeConverter;
import org.springframework.simple.beans.factory.BeanCreationException;
import org.springframework.simple.beans.factory.BeanDefinitionStoreException;
import org.springframework.simple.beans.factory.config.BeanDefinition;
import org.springframework.simple.beans.factory.config.BeanDefinitionHolder;
import org.springframework.simple.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.simple.beans.factory.config.RuntimeBeanReference;
import org.springframework.simple.beans.factory.config.TypedStringValue;

class BeanDefinitionValueResolver {

	public static final String GENERATED_BEAN_NAME_SEPARATOR = BeanDefinitionReaderUtils.GENERATED_BEAN_NAME_SEPARATOR;
	protected final Log logger = LogFactory.getLog(getClass());

	private final AbstractBeanFactory factory;
	private final String beanName;
	private final BeanDefinition beanDefinition;
	private final SimpleTypeConverter typeConverter = new SimpleTypeConverter();

	public BeanDefinitionValueResolver(AbstractBeanFactory factory, String beanName, BeanDefinition beanDefinition) {
		this.factory = factory;
		this.beanDefinition = beanDefinition;
		this.beanName = beanName;
		factory.registerCustomEditors(typeConverter);
	}

	public Object resolveValueIfNecessary(String argName, Object value) {

		if (value instanceof BeanDefinitionHolder) {
			BeanDefinitionHolder beanHolder = (BeanDefinitionHolder) value;
			return resolveInnerBeanDefinition(argName, beanHolder.getBeanName(), beanHolder.getBeanDefinition());
		} else if (value instanceof BeanDefinition) {
			BeanDefinition bd = (BeanDefinition) value;
			return resolveInnerBeanDefinition(argName, "(inner bean)", bd);
		} else if (value instanceof RuntimeBeanNameReference) {
			String ref = ((RuntimeBeanNameReference) value).getBeanName();
			if (!this.factory.containsBean(ref)) {
				throw new BeanDefinitionStoreException("无效的bean名称：" + ref + "参数" + argName);
			}
			return ref;
		} else if (value instanceof RuntimeBeanReference) {
			RuntimeBeanReference ref = (RuntimeBeanReference) value;
			return resolveReference(argName, ref);
		} else if (value instanceof ManagedList) {
			return resolveManagedList(argName, (List) value);
		} else if (value instanceof ManagedSet) {
			return resolveManagedSet(argName, (Set) value);
		} else if (value instanceof ManagedMap) {
			return resolveManagedMap(argName, (Map) value);
		} else if (value instanceof ManagedProperties) {
			// Properties 不用转换
			Properties copy = new Properties();
			copy.putAll((Properties) value);
			return copy;
		} else if (value instanceof TypedStringValue) {
			// 将String转换成相应的方法
			TypedStringValue typedStringValue = (TypedStringValue) value;
			try {
				Class targetClass = resolveTargetType(typedStringValue);
				return this.factory.doTypeConvertionIfNecessary(this.typeConverter, typedStringValue.getValue(),
						targetClass, null);
			} catch (Throwable ex) {
				throw new BeanCreationException(this.beanDefinition.getResourceDescription(), this.beanName,
						"String转换对象类型时失败。", ex);
			}
		} else {
			// 未知的类型，不转换
			return value;
		}
	}

	protected Class resolveTargetType(TypedStringValue value) throws ClassNotFoundException {
		if (value.hasTargetType()) {
			return value.getTargetType();
		}
		return value.resolveTargetType(this.factory.getBeanClassLoader());
	}

	private Object resolveManagedMap(String argName, Map map) throws BeansException {
		Map resolved = CollectionFactory.createLinkedMapIfPossible(map.size());
		for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			// key和value都需要转换
			Object resolvedKey = this.resolveValueIfNecessary(argName, entry.getKey());
			Object resolvedValue = this.resolveValueIfNecessary(argName + "with key " + BeanWrapper.PROPERTY_KEY_PREFIX
					+ entry.getKey() + BeanWrapper.PROPERTY_KEY_SUFFIX, entry.getValue());
			resolved.put(resolvedKey, resolvedValue);
		}
		return resolved;
	}

	private Object resolveManagedSet(String argName, Set set) throws BeansException {
		Set resolved = CollectionFactory.createLinkedSetIfPossible(set.size());
		int i = 0;
		for (Iterator it = set.iterator(); it.hasNext();) {
			Object resolvedItem = this.resolveValueIfNecessary(
					argName + "with key " + BeanWrapper.PROPERTY_KEY_PREFIX + i + BeanWrapper.PROPERTY_KEY_SUFFIX,
					it.next());
			resolved.add(resolvedItem);
			i++;
		}
		return resolved;
	}

	private Object resolveManagedList(String argName, List list) throws BeansException {
		List resolved = new ArrayList(list.size());
		for (int i = 0; i < list.size(); i++) {
			Object resolvedItem = this.resolveValueIfNecessary(
					argName + "with key " + BeanWrapper.PROPERTY_KEY_PREFIX + i + BeanWrapper.PROPERTY_KEY_SUFFIX,
					list.get(i));
			resolved.add(resolvedItem);
		}
		return resolved;
	}

	private Object resolveReference(String argName, RuntimeBeanReference ref) throws BeansException {
		if (logger.isDebugEnabled()) {
			logger.debug("解析参照对象，属性：" + argName);
		}
		try {
			if (ref.isToParent()) {
				if (this.factory.getParentBeanFactory() == null) {
					throw new BeanCreationException(this.beanDefinition.getResourceDescription(), this.beanName,
							"解析参照类型失败：" + ref.getBeanName() + "没有找到父工厂");
				}
				return this.factory.getParentBeanFactory().getBean(ref.getBeanName());
			} else {
				Object bean = this.factory.getBean(ref.getBeanName());
				if (this.beanDefinition.isSingleton()) {
					this.factory.registerDependentBean(ref.getBeanName(), this.beanName);
				}
				return bean;
			}
		} catch (BeansException ex) {
			throw new BeanCreationException(this.beanDefinition.getResourceDescription(), this.beanName,
					"解析引用bean失败。名称：" + ref.getBeanName() + "参数" + argName);
		}
	}

	private Object resolveInnerBeanDefinition(String argName, String innerBeanName, BeanDefinition innerbd)
			throws BeansException {

		if (logger.isDebugEnabled()) {
			logger.debug("开始解析bean定义。innerBeanName:" + innerBeanName);
		}
		try {
			RootBeanDefinition mergedInnerBd = this.factory.getMergedBeanDefinition(innerBeanName, innerbd);
			String actualInnerBeanName = innerBeanName;
			if (mergedInnerBd.isSingleton()) {
				if (!this.beanDefinition.isSingleton()) {
					throw new BeanDefinitionStoreException("Inner bean definition '" + innerBeanName + "' for "
							+ argName + " has scope 'singleton' but containing bean definition '" + this.beanName
							+ "' does not. Mark the inner bean definition with scope 'prototype' instead.");
				}
				actualInnerBeanName = adaptInnerBeanName(innerBeanName);
			}
			Object innerBean = this.factory.createBean(actualInnerBeanName, mergedInnerBd, null);
			if (mergedInnerBd.isSingleton()) {
				this.factory.registerDependentBean(actualInnerBeanName, this.beanName);
			}
			return this.factory.getObjectForBeanInstance(innerBean, actualInnerBeanName, mergedInnerBd);
		} catch (BeansException ex) {
			throw new BeanCreationException(this.beanDefinition.getResourceDescription(), this.beanName,
					"无法创建内置bean：" + innerBeanName + " 参数：" + argName);
		}
	}

	private String adaptInnerBeanName(String innerBeanName) {
		String actualInnerBeanName = innerBeanName;
		int counter = 0;
		while (this.factory.isBeanNameUsed(actualInnerBeanName)) {
			counter++;
			actualInnerBeanName = innerBeanName + GENERATED_BEAN_NAME_SEPARATOR + counter;
		}
		return actualInnerBeanName;
	}
}
