package org.springframework.simple.beans.factory.support;

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.CollectionFactory;
import org.springframework.core.MethodParameter;
import org.springframework.simple.beans.BeanWrapper;
import org.springframework.simple.beans.BeansException;
import org.springframework.simple.beans.PropertyEditorRegistrar;
import org.springframework.simple.beans.PropertyEditorRegistry;
import org.springframework.simple.beans.TypeConverter;
import org.springframework.simple.beans.TypeMismatchException;
import org.springframework.simple.beans.factory.BeanCreationException;
import org.springframework.simple.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.simple.beans.factory.BeanDefinitionStoreException;
import org.springframework.simple.beans.factory.BeanFactory;
import org.springframework.simple.beans.factory.BeanFactoryUtils;
import org.springframework.simple.beans.factory.BeanIsAbstractException;
import org.springframework.simple.beans.factory.BeanIsNotAFactoryException;
import org.springframework.simple.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.simple.beans.factory.CannotLoadBeanClassException;
import org.springframework.simple.beans.factory.DisposableBean;
import org.springframework.simple.beans.factory.FactoryBean;
import org.springframework.simple.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.simple.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.simple.beans.factory.ObjectFactory;
import org.springframework.simple.beans.factory.config.BeanDefinition;
import org.springframework.simple.beans.factory.config.BeanPostProcessor;
import org.springframework.simple.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.simple.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.simple.beans.factory.config.Scope;
import org.springframework.simple.beans.factory.propertyeditors.StringArrayPropertyEditor;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements ConfigurableBeanFactory {

	@SuppressWarnings("rawtypes")
	private final Map aliasMap = new HashMap();

	// 由FactoryBean的getObject方法创建的单例对象缓存：FactoryBean name - > object
	@SuppressWarnings("rawtypes")
	private final Map factoryBeanObjectCache = new HashMap();

	// 保存属性编辑器管理器
	@SuppressWarnings("rawtypes")
	private final Set propertyEditorRegistrars = CollectionFactory.createLinkedSetIfPossible(16);

	// 保存登记的Bean后处理器
	@SuppressWarnings("rawtypes")
	private final List beanPostProcessors = new ArrayList();

	// 是否含有DestructionAwareBeanPostProcessors
	private boolean hasDestructionAwareBeanPostProcessors;

	// 是否缓存bean元数据
	private boolean catheBeanMetadata = true;

	// 保存合并后的bean定义
	private final Map mergedBeanDefinitions = new HashMap();

	// 保存至少创建过一次的bean的名称
	private final Set alreadyCreated = CollectionFactory.createLinkedSetIfPossible(16);

	// 将beanname解析成beanclass时使用
	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	// 保存实例正在创建中的prototype的名称
	private final ThreadLocal prototypesCurrentlyInCreation = new ThreadLocal();

	// 保存scope
	private final Map scopes = new HashMap();

	// 保存自定义属性编辑器
	private final Map customEditors = new HashMap();

	private BeanFactory parentBeanFactory;

	public AbstractBeanFactory() {
	}

	public AbstractBeanFactory(BeanFactory parentBeanFactory) {
		this.parentBeanFactory = parentBeanFactory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException {
		Assert.hasText(beanName, "beanName不能为空。");
		Assert.hasText(alias, "alias不能为空。");
		if (!beanName.equals(alias)) {
			Object registeredName = aliasMap.get(alias);
			if (registeredName != null && !registeredName.equals(beanName)) {
				throw new BeanDefinitionStoreException(beanName + "别名已经被注册过。");
			}
			aliasMap.put(alias, beanName);
		}
	}

	protected boolean isAlias(String beanName) {
		synchronized (this.aliasMap) {
			return this.aliasMap.containsKey(beanName);
		}
	}

	public RootBeanDefinition getMergedBeanDefinition(String name) {
		return getMergedBeanDefinition(name, false);
	}

	protected RootBeanDefinition getMergedBeanDefinition(String name, boolean includingAncestors) {

		String beanName = transformBeanName(name);

		// 工厂里不存在且允许查找父工厂
		if (includingAncestors && !containsBeanDefinition(beanName)
				&& getParentBeanFactory() instanceof AbstractBeanFactory) {

			return ((AbstractBeanFactory) getParentBeanFactory()).getMergedBeanDefinition(beanName, true);
		}

		return getMergedBeanDefinition(beanName, getBeanDefinition(beanName));
	}

	protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd)
			throws BeanDefinitionStoreException {

		synchronized (this.mergedBeanDefinitions) {
			RootBeanDefinition mergedBd = (RootBeanDefinition) this.mergedBeanDefinitions.get(bd);
			// 如果已经做成，直接返回缓存中的beanDefinition，缓存找不到，进入if语句
			if (mergedBd == null) {
				// 如果是父bean，直接拷贝一个放入缓存中
				if (bd instanceof RootBeanDefinition) {
					mergedBd = new RootBeanDefinition((RootBeanDefinition) bd);
				}
				// 如果是子bean，则合并父bean后放入缓存
				else if (bd instanceof ChildBeanDefinition) {
					ChildBeanDefinition childBd = (ChildBeanDefinition) bd;
					RootBeanDefinition parentBd = null;

					try {
						// 子bean保存的父bean名称不等于beanName时，采用子bean的保存值
						if (!beanName.equals(childBd.getParentName())) {
							parentBd = getMergedBeanDefinition(childBd.getParentName(), true);
						} else {
							if (getParentBeanFactory() instanceof AbstractBeanFactory) {
								AbstractBeanFactory parentBeanFactory = (AbstractBeanFactory) getParentBeanFactory();
								parentBd = parentBeanFactory.getMergedBeanDefinition(childBd.getParentName(), true);
							} else {
								throw new NoSuchBeanDefinitionException(childBd.getParentName(), "找不到父bean定义。");
							}
						}
					} catch (NoSuchBeanDefinitionException ex) {
						throw new BeanDefinitionStoreException("找不到父bean定义。" + childBd.getParentName());
					}
					// 利用父bean的定义深拷贝一个新的bean
					mergedBd = new RootBeanDefinition(parentBd);
					// 将子bean的定义覆盖到新bean
					mergedBd.overrideFrom(childBd);
				} else {
					throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName,
							"既不是RootBeanDefinition又不是ChildBeanDefinition，无法处理。");
				}

				if (isCatheBeanMetadata() && this.alreadyCreated.contains(mergedBd)) {
					// 加入正准备创建实例或者已经创建过一次的bean的定义
					this.mergedBeanDefinitions.put(bd, mergedBd);
				}
			}
			return mergedBd;
		}

	}

	public void setCacheBeanMetadata(boolean cacheBeanMetadata) {
		this.catheBeanMetadata = cacheBeanMetadata;
	}

	public boolean isCatheBeanMetadata() {
		return this.catheBeanMetadata;
	}

	protected String transformBeanName(String name) {

		// 如果是工厂bean，则去掉前面的&
		String beanName = BeanFactoryUtils.transformedBeanName(name);
		// 如果name是别名，则返回对应的真名
		synchronized (this.aliasMap) {
			String trueName = (String) this.aliasMap.get(beanName);
			return (trueName != null ? trueName : beanName);
		}
	}

	protected abstract boolean containsBeanDefinition(String beanName);

	protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

	@Override
	public void destroyScopedBean(String beanName) {

		RootBeanDefinition mergedBeanDefinition = getMergedBeanDefinition(beanName);
		if (mergedBeanDefinition.isSingleton() || mergedBeanDefinition.isPrototype()) {
			throw new IllegalStateException(" 不能注销singleTon和prototype类型的bean。");
		}
		String scopeName = mergedBeanDefinition.getScope();
		Scope scope = (Scope) this.scopes.get(scopeName);
		if (scope != null) {
			throw new IllegalStateException(" 没有找到指定的scope。");
		}
		Object bean = scope.remove(beanName);
		if (bean != null) {
			destroyBean(beanName, bean, mergedBeanDefinition);
		}
	}

	@Override
	public BeanFactory getParentBeanFactory() {
		return this.parentBeanFactory;
	}

	@Override
	public boolean containLocalBean(String name) {
		String beanName = transformBeanName(name);
		return containsSingleton(beanName) || containsBeanDefinition(beanName);
	}

	@Override
	public Object getBean(String name) throws BeansException {
		return getBean(name, null, null);
	}

	@Override
	public Object getBean(String name, Class requiredType) throws BeansException {
		return getBean(name, requiredType, null);
	}

	public Object getBean(String name, Object[] Args) {
		return getBean(name, null, Args);
	}

	@SuppressWarnings("unchecked")
	public Object getBean(String name, Class requiredType, Object[] args) throws BeansException {

		String beanName = transformBeanName(name);
		Object bean = null;

		// 1，查找单例缓存
		Object sharedInstance = getSingleton(beanName);
		// 2，能找到
		if (sharedInstance != null) {
			// 2.1，如果bean还处于加载中的状态，出力log，说明bean的初期化还没有最后完成，主要是用于解决循环依赖
			if (isSingletonCurrentlyInCreation(beanName)) {
				if (logger.isDebugEnabled()) {
					logger.debug("返回初次暴露的bean实例" + beanName + "，这个bean还没有完全完成实例化。");
				}
			}
			// 2.2，如果bean已经加载完成，出力log
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("从缓存中返回bean " + beanName + "的实例。");
				}
			}

			// 2.3，确认最终输出对象（如果是工厂bean，则需要出力getObject的结果）
			if (containsBeanDefinition(beanName)) {
				RootBeanDefinition mbd = getMergedBeanDefinition(beanName, false);
				bean = getObjectForBeanInstance(sharedInstance, name, mbd);
			} else {
				// TODO 什么时候用到？
				bean = getObjectForBeanInstance(sharedInstance, name, null);
			}
		}
		// 3，缓存找不到
		else {
			// 3.1，如果bean还处于加载中的状态，异常
			if (isSingletonCurrentlyInCreation(beanName)) {
				throw new BeanCurrentlyInCreationException(beanName);
			}

			// 3.2，如果父工厂存在，且当前工厂没有bean定义，查找父工厂
			BeanFactory parentFactory = getParentBeanFactory();
			if (parentFactory != null && (!containsBeanDefinition(beanName))) {
				String nameToLookUp = originalBeanName(name);
				if (parentFactory instanceof AbstractBeanFactory) {
					return ((AbstractBeanFactory) parentFactory).getBean(nameToLookUp, requiredType, args);
				} else if (args == null) {
					return parentFactory.getBean(nameToLookUp, requiredType);
				} else {
					throw new NoSuchBeanDefinitionException(beanName, "无法从父容器中加载，不支持指定参数的bean加载。");
				}
			}

			this.alreadyCreated.add(beanName);

			final RootBeanDefinition mergedBeanDefinition = getMergedBeanDefinition(beanName);
			checkMergedBeanDefinition(mergedBeanDefinition, beanName, args);

			// 3.3，利用解析好的bean定义创建bean实例
			// 3.3.1，bean是单例的场合
			if (mergedBeanDefinition.isSingleton()) {
				// 3.3.1.1，创建bean（子类实现）
				sharedInstance = getSingleton(beanName, new ObjectFactory() {
					@Override
					public Object getObject() throws BeansException {
						try {
							return createBean(beanName, mergedBeanDefinition, args);
						} catch (BeansException ex) {
							// 为了解决循环依赖，有可能已经提前暴露了，需要将提前暴露的实例消除。
							destroySingleTon(beanName);
							throw ex;
						}
					}
				});
				// 3.3.1.2，确认最终输出对象（如果是工厂bean，则需要出力getObject的结果）
				bean = getObjectForBeanInstance(sharedInstance, name, mergedBeanDefinition);
			}

			// 3.3.2，bean是prototype的场合
			else if (mergedBeanDefinition.isPrototype()) {
				Object prototypeInstance = null;
				// 3.3.2.1，创建bean（子类实现）
				try {
					beforePrototypeCreation(beanName);
					prototypeInstance = this.createBean(beanName, mergedBeanDefinition, args);
				} finally {
					afterPrototypeCreation(beanName);
				}

				// 3.3.2.2，确认最终输出对象（如果是工厂bean，则需要出力getObject的结果）
				bean = getObjectForBeanInstance(prototypeInstance, name, mergedBeanDefinition);
			}

			// 3.3.3，其他scope的场合
			else {
				String scopeName = mergedBeanDefinition.getScope();
				Scope scope = (Scope) this.scopes.get(scopeName);
				if (scope == null) {
					throw new IllegalStateException("没有找到" + scopeName + "相关的scope。");
				}
				try {
					// 3.3.3.1，创建bean（子类实现）
					Object scopeInstance = scope.get(beanName, new ObjectFactory() {

						@Override
						public Object getObject() throws BeansException {
							beforePrototypeCreation(beanName);
							try {
								Object bean = createBean(beanName, mergedBeanDefinition, args);
								// 注册注销信息
								if (requiresDestruction(bean, mergedBeanDefinition)) {
									scope.registerDestructionCallBack(beanName, new DisposableBeanAdapter(bean,
											beanName, mergedBeanDefinition, getBeanPostProcessors()));
								}
								return bean;
							} finally {
								afterPrototypeCreation(beanName);
							}
						}
					});
					// 3.3.3.2，确认最终输出对象（如果是工厂bean，则需要出力getObject的结果）
					bean = this.getObjectForBeanInstance(scopeInstance, name, mergedBeanDefinition);
				} catch (IllegalStateException ex) {
					throw new BeanCreationException(beanName, "Scope" + scopeName + "加载失败。", ex);
				}
			}
		}

		// 4，返回前做ClassType检验
		if (requiredType != null && !requiredType.isAssignableFrom(bean.getClass())) {
			// 生成的bean不是指定类型或其子类时，报错。
			throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
		}

		return bean;
	}

	protected boolean requiresDestruction(Object bean, RootBeanDefinition mergedBeanDefinition) {
		return (bean instanceof DisposableBean || mergedBeanDefinition.getDestroyMethodName() != null
				|| hasDestructionAwareBeanPostProcessors());
	}

	protected boolean hasDestructionAwareBeanPostProcessors() {
		return this.hasDestructionAwareBeanPostProcessors;
	}

	protected void checkMergedBeanDefinition(RootBeanDefinition mergedBeanDefinition, String beanName, Object[] args)
			throws BeansException {

		// 不能为抽象类
		if (mergedBeanDefinition.isAbstract()) {
			throw new BeanIsAbstractException(beanName);
		}

		if (args != null) {
			if (mergedBeanDefinition.isSingleton()) {
				throw new BeanDefinitionStoreException("加载单例时不能指定参数。");
			} else if (mergedBeanDefinition.getFactoryMethodName() == null) {
				throw new BeanDefinitionStoreException("只能为工厂方法指定参数。");
			}
		}
	}

	protected Object getObjectForBeanInstance(Object beanInstance, String name, RootBeanDefinition mbd) {

		String beanName = transformBeanName(name);

		if (BeanFactoryUtils.isFactoryDereference(name) && !(beanInstance instanceof FactoryBean)) {
			throw new BeanIsNotAFactoryException(beanName, beanInstance.getClass());
		}

		boolean shared = (mbd == null || mbd.isSingleton());
		Object object = beanInstance;

		// 如果不是工厂bean，直接返回
		// 如果是工厂bean，返回工厂bean创建的对象
		if (beanInstance instanceof FactoryBean) {
			if (!BeanFactoryUtils.isFactoryDereference(name)) {
				FactoryBean factory = (FactoryBean) beanInstance;
				if (logger.isDebugEnabled()) {
					logger.debug("名称为" + beanName + "是一个工厂bean");
				}
				// 如果工厂bean为单例，则加到缓存里
				if (shared && factory.isSingleTon()) {
					synchronized (this.factoryBeanObjectCache) {
						object = this.factoryBeanObjectCache.get(beanName);
						if (object == null) {
							object = getObjectFromFactoryBean(factory, beanName, mbd);
							// 保存getObject方法创造的对象
							this.factoryBeanObjectCache.put(beanName, object);
						}
					}
				}
				// 如果工厂bean不是单例，则不缓存，直接创建
				else {
					object = getObjectFromFactoryBean(factory, beanName, mbd);
				}
			}

			else {
				// 希望返回工厂bean本身的实例（&XXXX）
				if (logger.isDebugEnabled()) {
					logger.debug("Calling code asked for FactoryBean instance for name '" + beanName + "'");
				}
			}
		}

		return object;

	}

	private Object getObjectFromFactoryBean(FactoryBean factory, String beanName, RootBeanDefinition mbd)
			throws BeanCreationException {

		Object object;

		try {
			object = factory.getObject();
		} catch (FactoryBeanNotInitializedException ex) {
			throw new BeanCurrentlyInCreationException(beanName, ex.toString());
		} catch (Throwable ex) {
			throw new BeanCreationException(beanName, "工厂bean创建对象时出异常。");
		}

		// 创建结果为空且bean正还没有完全创建完毕
		if (object == null && isSingletonCurrentlyInCreation(beanName)) {
			throw new BeanCurrentlyInCreationException(beanName, "工厂bean正处于创建中，getObject返回了空值。");
		}

		if (object != null && ((mbd == null) || mbd.isSynthetic())) {
			object = postProcessObjectFromFactoryBean(object, beanName);
		}

		return object;

	}

	protected Object postProcessObjectFromFactoryBean(Object object, String beanName) {
		// 特殊需要时，子类完成。
		return object;
	}

	@Override
	public boolean containsBean(String name) {
		if (containsSingleton(name)) {
			return true;
		}
		// 找不到，就找父容器
		BeanFactory parentBeanFactory = getParentBeanFactory();
		if (parentBeanFactory != null) {
			return parentBeanFactory.containsBean(originalBeanName(name));
		}
		return false;
	}

	@Override
	public String[] getAlieses(String name) {

		String beanName = transformBeanName(name);
		List alias = new ArrayList();
		boolean factoryPrefix = name.startsWith(FACTORY_BEAN_PREFIX);
		String fullBeanName = beanName;
		if (factoryPrefix) {
			fullBeanName = FACTORY_BEAN_PREFIX + beanName;
		}
		if (!fullBeanName.equals(name)) {
			// 说明name是别名，将真名放在List的第一个
			alias.add(fullBeanName);
		}
		synchronized (this.aliasMap) {
			// 循环查找别名集合
			for (Iterator it = this.aliasMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String realName = (String) entry.getValue();
				if (realName.equals(beanName)) {
					String key = (factoryPrefix ? FACTORY_BEAN_PREFIX : "") + entry.getKey();
					if (!key.equals(name)) {
						// 除参数以外
						alias.add(key);
					}
				}
			}
		}
		if (!containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
			BeanFactory parentFactory = getParentBeanFactory();
			if (parentFactory != null) {
				alias.addAll(Arrays.asList(parentFactory.getAlieses(fullBeanName)));
			}
		}
		return StringUtils.toStringArray(alias);
	}

	@Override
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {

		String beanName = transformBeanName(name);
		// 在单例bean的实例缓存里查找
		Object singleInstace = getSingleton(beanName);
		if (singleInstace != null) {
			// 工厂bean
			if (singleInstace instanceof FactoryBean && !BeanFactoryUtils.isFactoryDereference(name)) {
				return ((FactoryBean) singleInstace).isSingleTon();
			} else {
				// 单例实例找到了，直接返回真。
				return true;
			}
		}
		// 找不到时，查找beanDefinition
		else {
			// 查找父工厂
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				// 父工厂不为空且本工厂无此bean
				return parentBeanFactory.isSingleton(originalBeanName(beanName));
			}

			// 查找本工厂的bean定义
			RootBeanDefinition mbd = getMergedBeanDefinition(beanName, false);

			boolean isSingleTon = mbd.isSingleton();
			// 根据找到的定义找出对应的bean Class，是否为工厂bean，如果是，再判断是否为单例
			if (isSingleTon && !BeanFactoryUtils.isFactoryDereference(name)) {
				Class beanClass = resolveBeanClass(mbd, beanName);
				if (beanClass != null && FactoryBean.class.isAssignableFrom(beanClass)) {
					FactoryBean factoryBean = (FactoryBean) getBean(BeanFactory.FACTORY_BEAN_PREFIX + beanName);
					isSingleTon = factoryBean.isSingleTon();
				}
			}
			return isSingleTon;
		}
	}

	protected String originalBeanName(String name) {
		String beanName = transformBeanName(name);
		if (name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
			beanName = BeanFactory.FACTORY_BEAN_PREFIX + beanName;
		}
		return beanName;
	}

	@Override
	public Class getType(String name) throws NoSuchBeanDefinitionException {

		String beanName = transformBeanName(name);
		// 查找单例
		Object singleTonInstance = getSingleton(beanName);
		if (singleTonInstance != null) {
			// 工厂bean
			if (singleTonInstance instanceof FactoryBean && !BeanFactoryUtils.isFactoryDereference(name)) {
				return ((FactoryBean) singleTonInstance).getOjectType();
			} else {
				return singleTonInstance.getClass();
			}
		}

		// 查找bean定义
		else {
			// 查找父工厂
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				return parentBeanFactory.getType(originalBeanName(name));
			}

			RootBeanDefinition mbd = getMergedBeanDefinition(beanName, false);

			// 返回值
			Class beanClass = null;

			// 处理工厂方法
			if (mbd.getFactoryMethodName() != null) {
				beanClass = getTypeForFactoryMethod(beanName, mbd);
			} else {
				beanClass = resolveBeanClass(mbd, beanName);
			}

			// 处理工厂bean
			if (beanClass != null && FactoryBean.class.isAssignableFrom(beanClass)
					&& !BeanFactoryUtils.isFactoryDereference(name)) {
				beanClass = getTypeForFactoryBean(beanName, mbd);
			}

			return beanClass;
		}
	}

	public boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformBeanName(name);
		Object instance = getSingleton(beanName);

		if (instance != null) {
			return (instance instanceof FactoryBean);
		}

		if (!containsBeanDefinition(beanName) && (getParentBeanFactory() instanceof AbstractBeanFactory)) {
			return ((AbstractBeanFactory) getParentBeanFactory()).isFactoryBean(beanName);
		}

		RootBeanDefinition mbd = getMergedBeanDefinition(beanName, false);
		Class beanClass = resolveBeanClass(mbd, beanName);
		return (FactoryBean.class.equals(beanClass));

	}

	protected Class getTypeForFactoryBean(String beanName, RootBeanDefinition mbd) {
		FactoryBean factoryBean = null;
		try {
			factoryBean = (FactoryBean) getBean(FACTORY_BEAN_PREFIX + beanName);
		} catch (BeanCreationException ex) {
			return null;
		}
		try {
			return factoryBean.getOjectType();
		} catch (Throwable ex) {
			return null;
		}
	}

	protected Class getTypeForFactoryMethod(String beanName, RootBeanDefinition mbd) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addBeanPostProcesser(BeanPostProcessor beanPostProcessor) {

		Assert.notNull(beanPostProcessor, "beanPostProcessor不能为空。");
		this.beanPostProcessors.add(beanPostProcessor);
		if (beanPostProcessor instanceof DestructionAwareBeanPostProcessor) {
			this.hasDestructionAwareBeanPostProcessors = true;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar) {
		Assert.notNull(registrar, "属性编辑注册器管理器不能为空。");
		this.propertyEditorRegistrars.add(registrar);
	}

	protected Class resolveBeanClass(RootBeanDefinition mbd, String beanName) {
		if (mbd.hasBeanClass()) {
			return mbd.getBeanClass();
		}
		try {
			return mbd.resolveBeanClass(getBeanClassLoader());
		} catch (ClassNotFoundException e) {
			throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), e);
		}

	}

	@Override
	public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
		// 拷贝另一个工厂的配置。
		Assert.notNull(otherFactory, "otherFactory不能为空。");
		setBeanClassLoader(otherFactory.getBeanClassLoader());
		setCacheBeanMetedata(otherFactory.isCacheBeanMetedata());
		if (otherFactory instanceof AbstractBeanFactory) {
			AbstractBeanFactory otherAbstractFactory = (AbstractBeanFactory) otherFactory;
			this.customEditors.putAll(otherAbstractFactory.customEditors);
			this.propertyEditorRegistrars.addAll(otherAbstractFactory.propertyEditorRegistrars);
			this.beanPostProcessors.addAll(otherAbstractFactory.beanPostProcessors);
			this.hasDestructionAwareBeanPostProcessors = this.hasDestructionAwareBeanPostProcessors
					|| otherAbstractFactory.hasDestructionAwareBeanPostProcessors;
			this.scopes.putAll(otherAbstractFactory.scopes);
		}
	}

	@Override
	public void destroyBean(String beanName, Object beanInstance) {
		destroyBean(beanName, beanInstance, getMergedBeanDefinition(beanName));
	}

	protected void destroyBean(String beanName, Object beanInstance, RootBeanDefinition mergedBeanDefinition) {
		new DisposableBeanAdapter(beanInstance, beanName, mergedBeanDefinition, getBeanPostProcessors()).destroy();
	}

	@Override
	public ClassLoader getBeanClassLoader() {
		return this.beanClassLoader;
	}

	@Override
	public int getBeanPostProcessorCount() {
		return this.beanPostProcessors.size();
	}

	@Override
	public Scope getRegisteredScope(String scopeName) {
		Assert.notNull(scopeName, "scopeName不能为空。");
		return (Scope) this.scopes.get(scopeName);
	}

	@Override
	public String[] getRegisteredScopeNames() {
		return StringUtils.toStringArray(this.scopes.keySet());
	}

	@Override
	public boolean isCacheBeanMetedata() {
		return this.catheBeanMetadata;
	}

	@Override
	public boolean isCurrentlyInCreation(String beanName) {
		return (isSingletonCurrentlyInCreation(beanName) || isPrototypeCurrentlyCreation(beanName));
	}

	@Override
	public void registerCustomEditor(Class type, PropertyEditor propertyEditor) {
		Assert.notNull(type, "type 不能为空。");
		Assert.notNull(propertyEditor, "propertyEditor 不能为空。");
		this.customEditors.put(type, propertyEditor);
	}

	public Map getCustomEditors() {
		return this.customEditors;
	}

	@Override
	public void registerScope(String scopeName, Scope scope) {
		Assert.notNull(scopeName, "scopeName不能为空。");
		Assert.notNull(scope, "scope不能为空。");
		if (SCOPE_SINGLETON.equals(scopeName) || SCOPE_PROTOTYPE.equals(scopeName)) {
			throw new IllegalStateException("不能使用singleton和prototype关键字。");
		}
		this.scopes.put(scopeName, scope);
	}

	@Override
	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = (beanClassLoader != null ? beanClassLoader : ClassUtils.getDefaultClassLoader());
	}

	@Override
	public void setCacheBeanMetedata(boolean cacheBeanMetedata) {
		this.catheBeanMetadata = cacheBeanMetedata;
	}

	@Override
	public void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException {

		if (this.parentBeanFactory != null && this.parentBeanFactory != parentBeanFactory) {
			throw new IllegalStateException("已经关联了一个parent factory.");
		}
		this.parentBeanFactory = parentBeanFactory;
	}

	protected void removeSingleTon(String beanName) {
		super.removeSingleTon(beanName);
		synchronized (this.factoryBeanObjectCache) {
			this.factoryBeanObjectCache.remove(beanName);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void beforePrototypeCreation(String beanName) {
		Set prototypeNames = (Set) this.prototypesCurrentlyInCreation.get();
		if (prototypeNames == null) {
			prototypeNames = new HashSet();
			this.prototypesCurrentlyInCreation.set(prototypeNames);
		}
		prototypeNames.add(beanName);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void afterPrototypeCreation(String beanName) {
		Set prototypeNames = (Set) this.prototypesCurrentlyInCreation.get();
		if (prototypeNames != null) {
			if (prototypeNames.contains(beanName)) {
				prototypeNames.remove(beanName);
			}
			if (prototypeNames.isEmpty()) {
				this.prototypesCurrentlyInCreation.set(null);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	protected final boolean isPrototypeCurrentlyCreation(String beanName) {
		Set prototypeNames = (Set) this.prototypesCurrentlyInCreation.get();
		if (prototypeNames != null) {
			return prototypeNames.contains(beanName);
		}
		return false;
	}

	protected boolean isBeanNameUsed(String beanName) {
		return containLocalBean(beanName) || hasDependentBean(beanName);
	}

	public List getBeanPostProcessors() {
		return this.beanPostProcessors;
	}

	protected void initBeanWrapper(BeanWrapper bw) {
		this.registerCustomEditors(bw);
	}

	protected void registerCustomEditors(PropertyEditorRegistry registry) {
		// 注册String数组的属性编辑器
		registry.registerCustomEditor(String[].class, new StringArrayPropertyEditor());
		for (Iterator it = getPropertyEditorRegistrars().iterator(); it.hasNext();) {
			PropertyEditorRegistrar registrar = (PropertyEditorRegistrar) it.next();
			registrar.registerCustomEdiotrs(registry);
		}
		for (Iterator it = getCustomEditors().entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			Class clazz = (Class) entry.getKey();
			PropertyEditor editor = (PropertyEditor) entry.getValue();
			registry.registerCustomEditor(clazz, editor);
		}
	}

	public Set getPropertyEditorRegistrars() {
		return this.propertyEditorRegistrars;
	}

	protected void registerDisposableBeanIfNeccesary(String beanName, Object bean,
			RootBeanDefinition mergedBeanDefinition) {
		// 注册注销beann
		if (mergedBeanDefinition.isSingleton() && requiresDestruction(bean, mergedBeanDefinition)) {
			registerDisposableBean(beanName,
					new DisposableBeanAdapter(bean, beanName, mergedBeanDefinition, getBeanPostProcessors()));
		}
		// 注册依赖bean
		String[] dependsOn = mergedBeanDefinition.getDependsOn();
		if (dependsOn != null) {
			for (int i = 0; i < dependsOn.length; i++) {
				registerDependentBean(dependsOn[i], beanName);
			}
		}
	}

	protected Object doTypeConvertionIfNecessary(TypeConverter converter, Object originalValue, Class targetType,
			MethodParameter methodParam) throws TypeMismatchException {
		if (!getCustomEditors().isEmpty()) {
			synchronized (getCustomEditors()) {
				return converter.convertIfNecessory(originalValue, targetType, methodParam);
			}
		} else {
			return converter.convertIfNecessory(originalValue, targetType, methodParam);
		}
	}

	protected abstract Object createBean(String beanName, RootBeanDefinition mergedBeanDefinition, Object[] args)
			throws BeanCreationException;

}
