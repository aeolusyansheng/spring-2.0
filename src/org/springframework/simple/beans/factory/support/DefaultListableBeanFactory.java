package org.springframework.simple.beans.factory.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.CollectionFactory;
import org.springframework.simple.beans.BeansException;
import org.springframework.simple.beans.factory.BeanCreationException;
import org.springframework.simple.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.simple.beans.factory.BeanDefinitionStoreException;
import org.springframework.simple.beans.factory.BeanFactory;
import org.springframework.simple.beans.factory.BeanFactoryUtils;
import org.springframework.simple.beans.factory.CannotLoadBeanClassException;
import org.springframework.simple.beans.factory.FactoryBean;
import org.springframework.simple.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.simple.beans.factory.config.BeanDefinition;
import org.springframework.simple.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
		implements ConfigurableListableBeanFactory, BeanDefinitionRegistry {

	protected final Log logger = LogFactory.getLog(getClass());
	private boolean allowBeanDefinitionOverriding = true;

	private boolean allowEagerClassLoading = true;

	@SuppressWarnings("rawtypes")
	private final Map beanDefinitionMap = new HashMap();

	@SuppressWarnings("unused")
	private final List beanDefinitionNames = new ArrayList();

	public DefaultListableBeanFactory() {

	}

	public DefaultListableBeanFactory(BeanFactory parent) {
		super(parent);
	}

	@Override
	public boolean containsBeanDefinition(String name) {
		return this.beanDefinitionMap.containsKey(name);
	}

	@Override
	public int getBeanDefinitionCount() {
		return this.beanDefinitionMap.size();
	}

	protected Map findMatchingBeans(Class requiredType) {
		return BeanFactoryUtils.beansOfTypeIncludingAncestors(this, requiredType);
	}

	@Override
	public String[] getBeanDefinitionNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getBeanNamesForType(Class type) {
		return getBeanNamesForType(type, true, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String[] getBeanNamesForType(Class type, boolean includePrototypes, boolean allowEagerInit) {
		List result = new ArrayList();

		// 在所有的beanDefinition里找
		for (Iterator it = this.beanDefinitionNames.iterator(); it.hasNext();) {
			String beanName = (String) it.next();
			// 只判断非别名的名称
			if (!isAlias(beanName)) {
				// 获取bean定义
				RootBeanDefinition rbd = getMergedBeanDefinition(beanName, false);
				if (!rbd.isAbstract()
						&& (allowEagerInit || rbd.hasBeanClass() || !rbd.isLazyInit() || this.allowEagerClassLoading)) {

					try {
						Class beanClass = resolveBeanClass(rbd, beanName);

						// 处理工厂bean
						boolean isFactoryBean = (beanClass != null && FactoryBean.class.isAssignableFrom(beanClass));
						if (isFactoryBean || rbd.getFactoryBeanName() != null) {
							if (allowEagerInit && (includePrototypes || isSingleton(beanName))
									&& isBeanTypeMatch(beanName, type)) {
								result.add(beanName);
								continue;
							}
							if (!isFactoryBean) {
								continue;
							}
							beanName = FACTORY_BEAN_PREFIX + beanName;
						}

						// 非工厂bean
						if ((includePrototypes || isSingleton(beanName)) && isBeanTypeMatch(beanName, type)) {
							result.add(beanName);
						}
					} catch (CannotLoadBeanClassException ex) {
						if (rbd.isLazyInit()) {
							if (logger.isDebugEnabled()) {
								logger.debug("对应延迟加载bean，忽略加载失败:" + beanName);
							}
						} else {
							throw ex;
						}
					}
				}
			}
		}

		// 在所有的已注册的单例里找
		String[] singletonNames = getSingletonNames();
		for (int i = 0; i < singletonNames.length; i++) {
			String beanName = singletonNames[i];
			if (!containsBeanDefinition(beanName)) {
				if (isFactoryBean(beanName)) {
					if ((includePrototypes || isSingleton(beanName)) && isBeanTypeMatch(beanName, type)) {
						result.add(beanName);
						continue;
					}
					beanName = FACTORY_BEAN_PREFIX + beanName;
				}
				if (isBeanTypeMatch(beanName, type)) {
					result.add(beanName);
				}
			}
		}

		return StringUtils.toStringArray(result);
	}

	private boolean isBeanTypeMatch(String beanName, Class type) {
		if (type == null) {
			return true;
		}

		Class beanType = getType(beanName);
		return (beanType != null && type.isAssignableFrom(beanType));
	}

	@Override
	public Map getBeanOfType(Class type) throws BeansException {
		return getBeanOfType(type, true, true);
	}

	@Override
	public Map getBeanOfType(Class type, boolean includePrototypes, boolean allowEagerInit) throws BeansException {

		String[] beanNames = getBeanNamesForType(type, includePrototypes, allowEagerInit);
		Map result = CollectionFactory.createLinkedMapIfPossible(beanNames.length);
		for (int i = 0; i < beanNames.length; i++) {
			String beanName = beanNames[i];
			try {
				result.put(beanName, getBean(beanName));
			} catch (BeanCreationException ex) {
				if (ex.contains(BeanCurrentlyInCreationException.class)) {
					if (logger.isDebugEnabled()) {
						logger.debug("忽略正在创建中bean的异常。beanName：" + beanName + " message:" + ex.getMessage());
					}
				} else {
					throw ex;
				}
			}
		}
		return null;
	}

	@Override
	public String[] getAliases(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BeanDefinition getBeanDefinition(String name) throws BeansException {
		BeanDefinition bd = (BeanDefinition) this.beanDefinitionMap.get(name);
		if (bd == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("没有找到bean:" + name);
			}
			throw new NoSuchBeanDefinitionException(name);
		}
		return bd;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void registerBeanDefinition(String name, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {

		Assert.hasText(name, "bean name 不能为空。");
		Assert.notNull(beanDefinition, "bean definition 不能为空。");
		try {
			if (beanDefinition instanceof AbstractBeanDefinition) {
				((AbstractBeanDefinition) beanDefinition).validate();
			}
		} catch (BeanDefinitionValidationException ex) {
			throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), name, "校验时出错。", ex);
		}

		BeanDefinition oldBeanDefinition = (BeanDefinition) this.beanDefinitionMap.get(name);
		if (oldBeanDefinition != null) {
			if (!this.allowBeanDefinitionOverriding) {
				throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), name, "禁止覆盖bean定义。");
			} else {
				if (logger.isInfoEnabled()) {
					logger.info("bean 定义被覆盖。");
				}
			}

		} else {
			this.beanDefinitionNames.add(name);
		}
		this.beanDefinitionMap.put(name, beanDefinition);

		removeSingleTon(name);
	}

	@Override
	public void ignoreDependencyType(Class type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preInstantiateSingleTons() throws BeansException {
		// TODO Auto-generated method stub

	}

}
