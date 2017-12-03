package org.springframework.simple.beans.factory.support;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.simple.beans.BeanUtils;
import org.springframework.simple.beans.BeanWrapper;
import org.springframework.simple.beans.BeanWrapperImpl;
import org.springframework.simple.beans.BeansException;
import org.springframework.simple.beans.MutablePropertyValues;
import org.springframework.simple.beans.PropertyValue;
import org.springframework.simple.beans.PropertyValues;
import org.springframework.simple.beans.factory.BeanClassLoaderAware;
import org.springframework.simple.beans.factory.BeanCreationException;
import org.springframework.simple.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.simple.beans.factory.BeanDefinitionStoreException;
import org.springframework.simple.beans.factory.BeanFactory;
import org.springframework.simple.beans.factory.BeanFactoryAware;
import org.springframework.simple.beans.factory.BeanNameAware;
import org.springframework.simple.beans.factory.InitializingBean;
import org.springframework.simple.beans.factory.UnsatisfiedDependencyException;
import org.springframework.simple.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.simple.beans.factory.config.BeanPostProcessor;
import org.springframework.simple.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
		implements AutowireCapableBeanFactory {

	// 未完成的FactoryBean实例的缓存：FactoryBean name - > BeanWrapper
	private final Map factoryBeanInstanceCatch = new HashMap();

	private final Set ignoredDependencyInterfaces = new HashSet();
	private final Set ignoredDependencyTypes = new HashSet();

	// 没有使用CglibSubclassingInstantiationStrategy
	private InstantiationStrategy instantiationStrategy = new SimpleInstantiationStrategy();

	// 是否允许循环参照
	private boolean allowRawInjectionDespiteWrapping = false;

	private boolean allowCircularReferences = true;

	public AbstractAutowireCapableBeanFactory() {
		super();
	}

	public AbstractAutowireCapableBeanFactory(BeanFactory parent) {
		this();
		setParentBeanFactory(parent);
	}

	public Object createBean(Class beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
		RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode, dependencyCheck);
		bd.setSingleton(false);
		return createBean(beanClass.getName(), bd, null);
	}

	protected Object createBean(String beanName, RootBeanDefinition mergedBeanDefinition, Object[] args)
			throws BeanCreationException {

		// 如果有依赖bean，则先创建依赖bean
		String[] dependencies = mergedBeanDefinition.getDependsOn();
		if (dependencies != null) {
			for (int i = 0; i < dependencies.length; i++) {
				getBean(dependencies[i]);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("用bean定义" + mergedBeanDefinition + "创建bean " + beanName);
		}
		// 确保此时bean一定有beanClass
		Class beanClass = resolveBeanClass(mergedBeanDefinition, beanName);
		try {
			// 准备方法重写
			mergedBeanDefinition.prepareMethodOverrides();
		} catch (BeanDefinitionValidationException ex) {
			throw new BeanDefinitionStoreException(mergedBeanDefinition.getResourceDescription(), beanName, "方法覆盖校验失败。",
					ex);
		}

		String errorMessage = null;

		try {
			// 实例化bean（开始）
			errorMessage = "bean实例化前加载后处理器失败。";
			// 让后处理器处理是否需要返回代理bean（AOP？）
			if (beanClass != null && !mergedBeanDefinition.isSynthetic()) {
				Object bean = applyBeanPostProcessorsBeforeInstantiation(beanClass, beanName);
				if (bean != null) {
					bean = this.applyBeanPostProcessorsAfterInitialization(bean, beanName);
					return bean;
				}
			}
			// 创建bean实例
			errorMessage = "bean实例化失败。";

			BeanWrapper instanceWrapper = null;
			synchronized (this.factoryBeanInstanceCatch) {
				instanceWrapper = (BeanWrapper) this.factoryBeanInstanceCatch.remove(beanName);
			}
			if (instanceWrapper == null) {
				instanceWrapper = createBeanInstance(beanName, mergedBeanDefinition, args);
			}
			Object bean = instanceWrapper.getWrappedInstance();
			// 提前暴露创建好的实例，防止循环参照
			if (this.allowCircularReferences && isSingletonCurrentlyInCreation(beanName)) {
				if (logger.isDebugEnabled()) {
					logger.debug("为了处理循环参照，提前暴露beanName：" + beanName + "实例到缓存中。");
				}
				addSingleton(beanName, bean);
			}

			errorMessage = "bean初始化失败。";

			// 填充bean属性前给InstantiationAwareBeanPostProcessor后处理器机会修改bean的状态
			boolean continueWithPropertyPopulation = true;
			if (!mergedBeanDefinition.isSynthetic()) {
				for (Iterator it = getBeanPostProcessors().iterator(); it.hasNext();) {
					BeanPostProcessor beanPostProcessor = (BeanPostProcessor) it.next();
					if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
						InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) beanPostProcessor;
						if (!ibp.postProcessAfterInstantiation(beanPostProcessor, beanName)) {
							// 如果有一个后处理器返回false，不执行填充bean。
							continueWithPropertyPopulation = false;
							break;
						}
					}
				}
			}
			// 填充bean属性
			if (continueWithPropertyPopulation) {
				populateBean(beanName, mergedBeanDefinition, instanceWrapper);
			}
			Object originalBean = bean;
			// 初始化bean
			bean = this.initializeBean(beanName, bean, mergedBeanDefinition);

			if (!this.allowRawInjectionDespiteWrapping && originalBean != bean && hasDependentBean(beanName)) {
				throw new BeanCurrentlyInCreationException(beanName,
						"Bean with name '" + beanName + "' has been injected into other beans "
								+ getDependentBeans(beanName) + " in its raw version as part of a circular reference, "
								+ "but has eventually been wrapped (for example as part of auto-proxy creation). "
								+ "This means that said other beans do not use the final version of the bean. "
								+ "This is often the result of over-eager type matching - consider using "
								+ "'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example.");
			}

			// 如果需要注册DisposableBean，则注册
			registerDisposableBeanIfNeccesary(beanName, bean, mergedBeanDefinition);

			return bean;

		} catch (BeanCreationException ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new BeanCreationException(mergedBeanDefinition.getResourceDescription(), beanName, errorMessage, ex);
		}
	}

	public Object initializeBean(String beanName, Object bean) throws BeansException {
		return initializeBean(beanName, bean, null);
	}

	protected Object initializeBean(String beanName, Object bean, RootBeanDefinition mergedBeanDefinition)
			throws BeansException {

		if (bean instanceof BeanNameAware) {
			if (logger.isDebugEnabled()) {
				logger.debug("执行bean：" + beanName + "的BeanNameAware bean");
			}
			((BeanNameAware) bean).setBeanName(beanName);
		}

		if (bean instanceof BeanClassLoaderAware) {
			if (logger.isDebugEnabled()) {
				logger.debug("执行bean：" + beanName + "的BeanClassLoaderAware bean");
			}
			((BeanClassLoaderAware) bean).setBeanClassLoader(getBeanClassLoader());
		}

		if (bean instanceof BeanFactoryAware) {
			((BeanFactoryAware) bean).setBeanFactory(this);
		}

		Object WrappedBean = bean;
		// 初始化前的后处理
		if (mergedBeanDefinition == null || !mergedBeanDefinition.isSynthetic()) {
			WrappedBean = applyBeanPostProcessorBeforeInitialization(WrappedBean, beanName);
		}
		// 初始化处理
		try {
			invokeInitMethod(beanName, WrappedBean, mergedBeanDefinition);
		} catch (Throwable ex) {
			throw new BeanCreationException(
					(mergedBeanDefinition != null ? mergedBeanDefinition.getResourceDescription() : null), beanName,
					"初始化方法执行异常。", ex);
		}
		// 初始化后的后处理
		if (mergedBeanDefinition == null || !mergedBeanDefinition.isSynthetic()) {
			WrappedBean = applyBeanPostProcessorAfterInitialization(WrappedBean, beanName);
		}
		return WrappedBean;
	}

	protected void invokeInitMethod(String beanName, Object bean, RootBeanDefinition mergedBeanDefinition)
			throws Throwable {
		if (bean instanceof InitializingBean) {
			((InitializingBean) bean).afterPropertiesSet();
		}
		if (mergedBeanDefinition != null && mergedBeanDefinition.getInitMethodName() != null) {
			invokeCustomInitMethod(beanName, bean, mergedBeanDefinition.getInitMethodName(),
					mergedBeanDefinition.isEnforceInitMethod());
		}
	}

	protected void invokeCustomInitMethod(String beanName, Object bean, String initMethodName,
			boolean enforceInitMethod) throws Throwable {
		if (logger.isDebugEnabled()) {
			logger.debug("调用自定义的InitMethod。bean：" + beanName);
		}
		Method initMethod = BeanUtils.findMethod(bean.getClass(), initMethodName, null);
		if (initMethod == null) {
			if (!enforceInitMethod) {
				throw new NoSuchMethodException("方法不存在。bean：" + beanName + "方法名：" + initMethodName);
			} else {
				return;
			}
		}
		if (!Modifier.isPublic(initMethod.getModifiers())) {
			initMethod.setAccessible(true);
		}
		try {
			initMethod.invoke(bean, (Object[]) null);
		} catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}

	public Object applyBeanPostProcessorBeforeInitialization(Object existingBean, String beanName)
			throws BeansException {
		if (logger.isDebugEnabled()) {
			logger.debug("执行初始化前的后处理。beanName：" + beanName);
		}
		Object result = existingBean;
		for (Iterator it = getBeanPostProcessors().iterator(); it.hasNext();) {
			BeanPostProcessor beanPostProcessor = (BeanPostProcessor) it.next();
			result = beanPostProcessor.postProcessBeforeInitialization(result, beanName);
		}
		return result;
	}

	public Object applyBeanPostProcessorAfterInitialization(Object existingBean, String beanName)
			throws BeansException {
		if (logger.isDebugEnabled()) {
			logger.debug("执行初始化后的后处理。beanName：" + beanName);
		}
		Object result = existingBean;
		for (Iterator it = getBeanPostProcessors().iterator(); it.hasNext();) {
			BeanPostProcessor beanPostProcessor = (BeanPostProcessor) it.next();
			result = beanPostProcessor.postProcessAfterInitialization(result, beanName);
		}
		return result;
	}

	protected void populateBean(String beanName, RootBeanDefinition mergedBeanDefinition, BeanWrapper bw)
			throws BeansException {

		// 获取所有的属性
		PropertyValues pvs = mergedBeanDefinition.getPropertyValues();

		// 解析自动注入属性
		if (mergedBeanDefinition.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME
				|| mergedBeanDefinition.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
			MutablePropertyValues newPvs = new MutablePropertyValues(pvs);

			// 自动注入，处理ByName
			if (mergedBeanDefinition.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME) {
				autoWireByName(beanName, mergedBeanDefinition, bw, newPvs);
			}

			// 自动注入，处理ByType
			if (mergedBeanDefinition.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
				autoWireByType(beanName, mergedBeanDefinition, bw, newPvs);
			}

			pvs = newPvs;
		}
		PropertyDescriptor[] filterdPds = filterPropertyDescriptorsForDependencyCheck(bw);
		// 填充前执行bean后处理
		for (Iterator it = getBeanPostProcessors().iterator(); it.hasNext();) {
			BeanPostProcessor beanPostProcessor = (BeanPostProcessor) it.next();
			if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
				InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) beanPostProcessor;
				pvs = ibp.postProcessPropertyValues(pvs, filterdPds, bw.getWrappedInstance(), beanName);
				if (pvs == null) {
					return;
				}
			}
		}
		// 校验依赖
		checkDependencies(beanName, mergedBeanDefinition, filterdPds, pvs);
		// 执行属性填充
		applyPropertyvalues(beanName, mergedBeanDefinition, bw, pvs);
	}

	private void applyPropertyvalues(String beanName, RootBeanDefinition mergedBeanDefinition, BeanWrapper bw,
			PropertyValues pvs) throws BeansException {
		if (pvs == null) {
			return;
		}
		BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this, beanName,
				mergedBeanDefinition);
		MutablePropertyValues deepCopy = new MutablePropertyValues();
		PropertyValue[] pvArray = pvs.getPropertyValues();
		// 处理每一个属性
		for (int i = 0; i < pvArray.length; i++) {
			PropertyValue pv = pvArray[i];
			Object resolvedValue = valueResolver.resolveValueIfNecessary("bean property '" + pv.getName(),
					pv.getValue());
			deepCopy.addPropertyValue(pvArray[i].getName(), resolvedValue);
		}

		try {
			if (!getCustomEditors().isEmpty()) {
				synchronized (this) {
					bw.setPropertyValues(deepCopy);
				}
			} else {
				bw.setPropertyValues(deepCopy);
			}
		} catch (BeansException ex) {
			throw new BeanCreationException(mergedBeanDefinition.getResourceDescription(), beanName, "属性填充失败。", ex);
		}
	}

	protected void checkDependencies(String beanName, RootBeanDefinition mergedBeanDefinition, PropertyDescriptor[] pds,
			PropertyValues pvs) throws UnsatisfiedDependencyException {
		int dependencyCheck = mergedBeanDefinition.getDependencyCheck();
		if (dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_NONE) {
			return;
		}
		// 循环判断每一个pd
		for (int i = 0; i < pds.length; i++) {
			if (pds[i].getWriteMethod() != null && !pvs.contains(pds[i].getName())) {
				boolean isSimple = BeanUtils.isSimpleProperty(pds[i].getPropertyType());
				boolean unSatisfied = (dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_ALL
						|| (dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_SIMPLE && isSimple)
						|| (dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS && !isSimple));
				if (unSatisfied) {
					throw new UnsatisfiedDependencyException(mergedBeanDefinition.getResourceDescription(), beanName,
							pds[i].getName(), "需要给属性设值或者将依赖check设置为禁止。");
				}
			}
		}
	}

	protected PropertyDescriptor[] filterPropertyDescriptorsForDependencyCheck(BeanWrapper bw) {
		List pds = new LinkedList(Arrays.asList(bw.getPropertyDescriptors()));
		for (Iterator it = pds.iterator(); it.hasNext();) {
			PropertyDescriptor pd = (PropertyDescriptor) it.next();
			if (isExcludedFromDependencyCheck(pd)) {
				it.remove();
			}
		}
		return (PropertyDescriptor[]) pds.toArray(new PropertyDescriptor[pds.size()]);
	}

	protected abstract Map findMatchingBeans(Class requiredType) throws BeansException;

	private void filterMatchingBeans(Map matchingBeans, String beanName) {
		// 从matchingBeans中过滤掉自身和非自动注入bean
		for (Iterator it = matchingBeans.keySet().iterator(); it.hasNext();) {
			String name = (String) it.next();
			if (containsBeanDefinition(name)) {
				RootBeanDefinition beanDefinition = getMergedBeanDefinition(name);
				if (ObjectUtils.nullSafeEquals(beanName, name) || !beanDefinition.isAutowireCandidate()) {
					it.remove();
				}
			}
		}
	}

	protected void autoWireByType(String beanName, RootBeanDefinition mergedBeanDefinition, BeanWrapper bw,
			MutablePropertyValues pvs) throws BeansException {

		String[] propertyNames = unsatisfiedNonSimpleProperties(mergedBeanDefinition, bw);
		for (int i = 0; i < propertyNames.length; i++) {
			String propertyName = propertyNames[i];
			// 查找匹配的类型
			// 属性的类型
			Class requiredType = bw.getPropertyDescriptor(propertyName).getPropertyType();
			Map matchingBeans = findMatchingBeans(requiredType);
			filterMatchingBeans(matchingBeans, propertyName);
			// 找到一个OK
			if (matchingBeans != null && matchingBeans.size() == 1) {
				String autowiredBeanName = (String) matchingBeans.keySet().iterator().next();
				Object autowireBean = matchingBeans.values().iterator().next();
				pvs.addPropertyValue(propertyName, autowireBean);
				if (mergedBeanDefinition.isSingleton()) {
					registerDependentBean(autowiredBeanName, beanName);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("ByType设定成功。属性：" + propertyName + "bean:" + beanName);
				}
			}
			// 找到多个NG
			else if (matchingBeans != null && matchingBeans.size() > 1) {
				throw new UnsatisfiedDependencyException(mergedBeanDefinition.getResourceDescription(), beanName,
						propertyName, "ByType查找到多个匹配的bean，请使用ByName。属性：" + propertyName);
			}
			// 找不到，写LOG
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("ByType查找失败。属性：" + propertyName + "bean:" + beanName);
				}
			}
		}
	}

	protected void autoWireByName(String beanName, RootBeanDefinition mergedBeanDefinition, BeanWrapper bw,
			MutablePropertyValues pvs) throws BeansException {

		String[] propertyNames = unsatisfiedNonSimpleProperties(mergedBeanDefinition, bw);
		for (int i = 0; i < propertyNames.length; i++) {
			String propertyName = propertyNames[i];
			// 用属性名在缓存中查找bean是否存在
			if (containsBean(propertyName)) {
				Object bean = getBean(propertyName);
				pvs.addPropertyValue(propertyName, bean);
				if (mergedBeanDefinition.isSingleton()) {
					registerDependentBean(propertyName, beanName);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("ByName自动注入成功。属性：" + propertyName + "bean：" + beanName);
				}
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug("ByName自动注入属性失败。属性：" + propertyName + "bean:" + beanName);
				}
			}

		}
	}

	//  查找非简单属性,不支持对bean中的基本数据类型和String类型的属性进行自动装配
	protected String[] unsatisfiedNonSimpleProperties(RootBeanDefinition mergedBeanDefinition, BeanWrapper bw) {
		Set result = new TreeSet();
		PropertyValues pvs = mergedBeanDefinition.getPropertyValues();
		PropertyDescriptor[] pds = bw.getPropertyDescriptors();
		for (int i = 0; i < pds.length; i++) {
			if (pds[i].getWriteMethod() != null && !isExcludedFromDependencyCheck(pds[i])
					&& !pvs.contains(pds[i].getName()) && !BeanUtils.isSimpleProperty(pds[i].getPropertyType())) {
				result.add(pds[i].getName());
			}
		}
		return StringUtils.toStringArray(result);
	}

	protected boolean isExcludedFromDependencyCheck(PropertyDescriptor pd) {
		return (AutowireUtils.isExcludedFromDependencyCheck(pd)
				|| this.ignoredDependencyTypes.contains(pd.getPropertyType())
				|| AutowireUtils.isSetterDefinedInInterface(pd, this.ignoredDependencyInterfaces));
	}

	protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mergedBeanDefinition, Object[] args)
			throws BeansException {
		BeanWrapper instanceWrapper = null;
		if (mergedBeanDefinition.getFactoryMethodName() != null) {
			instanceWrapper = instantiateUsingFactoryMethod(beanName, mergedBeanDefinition, args);
		} else if (mergedBeanDefinition.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR
				|| mergedBeanDefinition.hasConstructorArgumentValues()) {
			instanceWrapper = autowireConstructor(beanName, mergedBeanDefinition);
		} else {
			// 采用无参数的构造函数
			instanceWrapper = intantiateBean(beanName, mergedBeanDefinition);
		}
		return instanceWrapper;
	}

	protected BeanWrapper intantiateBean(String beanName, RootBeanDefinition mergedBeanDefinition)
			throws BeansException {
		Object beanInstance = getInstantiationStrategy().instantiate(mergedBeanDefinition, beanName, this);
		BeanWrapper bw = new BeanWrapperImpl(beanInstance);
		initBeanWrapper(bw);
		return bw;
	}

	public InstantiationStrategy getInstantiationStrategy() {
		return this.instantiationStrategy;
	}

	protected BeanWrapper instantiateUsingFactoryMethod(String beanName, RootBeanDefinition mergedBeanDefinition,
			Object[] args) throws BeansException {
		// TODO
		return null;
	}

	protected BeanWrapper autowireConstructor(String beanName, RootBeanDefinition mergedBeanDefinition)
			throws BeansException {
		// TODO
		return null;
	}

	protected Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) {
		if (logger.isDebugEnabled()) {
			logger.debug("对bean：" + beanName + "执行初始化完了得后处理。");
		}
		Object result = existingBean;
		for (Iterator it = getBeanPostProcessors().iterator(); it.hasNext();) {
			BeanPostProcessor beanPostProcessor = (BeanPostProcessor) it.next();
			// 循环覆盖前面处理的结果
			result = beanPostProcessor.postProcessBeforeInitialization(result, beanName);
		}
		return result;
	}

	protected Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) {
		if (logger.isDebugEnabled()) {
			logger.debug("对bean：" + beanName + "执行初始化完了得后处理。");
		}
		Object result = existingBean;
		for (Iterator it = getBeanPostProcessors().iterator(); it.hasNext();) {
			BeanPostProcessor beanPostProcessor = (BeanPostProcessor) it.next();
			// 循环覆盖前面处理的结果
			result = beanPostProcessor.postProcessAfterInitialization(result, beanName);
		}
		return result;
	}

	protected Object applyBeanPostProcessorsBeforeInstantiation(Class beanClass, String beanName) {
		if (logger.isDebugEnabled()) {
			logger.debug("执行bean实例化前的bean后处理。");
		}
		for (Iterator it = getBeanPostProcessors().iterator(); it.hasNext();) {
			BeanPostProcessor beanPostProcessor = (BeanPostProcessor) it.next();
			if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
				InstantiationAwareBeanPostProcessor ibpp = (InstantiationAwareBeanPostProcessor) beanPostProcessor;
				Object result = ibpp.postProcessBeforeInstantiation(beanClass, beanName);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public void ignoreDependencyInterface(Class ignore) {
		this.ignoredDependencyInterfaces.add(ignore);
	}

	protected void removeSingleTon(String beanName) {
		super.removeSingleTon(beanName);
		synchronized (this.factoryBeanInstanceCatch) {
			this.factoryBeanInstanceCatch.remove(beanName);
		}
	}

	public void setAllowRawInjectionDespiteWrapping(boolean allowRawInjectionDespiteWrapping) {
		this.allowRawInjectionDespiteWrapping = allowRawInjectionDespiteWrapping;
	}
}
