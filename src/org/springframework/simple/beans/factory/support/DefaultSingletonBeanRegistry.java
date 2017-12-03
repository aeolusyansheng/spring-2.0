package org.springframework.simple.beans.factory.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.CollectionFactory;
import org.springframework.simple.beans.factory.BeanCreationNotAllowedException;
import org.springframework.simple.beans.factory.DisposableBean;
import org.springframework.simple.beans.factory.ObjectFactory;
import org.springframework.simple.beans.factory.config.SingletonBeanRegistry;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {

	protected final Log logger = LogFactory.getLog(getClass());

	// 保存BeanFactory所有的单例Bean
	@SuppressWarnings("rawtypes")
	private final Map singleTonCathe = CollectionFactory.createLinkedMapIfPossible(16);

	// 标志位，记录所有的单例Bean是否处于注销中
	private boolean singleTonsCurrentlyInDestruction = false;

	// 保存正处于创建中的bean的名字
	private Set singletonCurrentlyInCreation = Collections.synchronizedSet(new HashSet());

	// 保存所有实现了DisposableBean的单例Bean
	@SuppressWarnings("rawtypes")
	private final Map disposableBeans = CollectionFactory.createIdentityMapIfPossible(16);

	// 保存依赖Bean
	@SuppressWarnings("rawtypes")
	private final Map dependentBeanMap = new HashMap();

	public void destroySingleTons() {

		if (logger.isInfoEnabled()) {
			logger.info("开始注销所有的Bean in [" + this + "]");
		}
		synchronized (this.singleTonCathe) {
			this.singleTonsCurrentlyInDestruction = true;
		}
		// 先处理所有的disposable Bean
		synchronized (this.disposableBeans) {
			String[] disposableNames = StringUtils.toStringArray(this.disposableBeans.keySet());
			for (int i = disposableNames.length - 1; i >= 0; i--) {
				destroySingleTon(disposableNames[i]);
			}
		}
		// 直接清空单例MAP
		synchronized (this.singleTonCathe) {
			this.singleTonCathe.clear();
			this.singleTonsCurrentlyInDestruction = false;
		}

	}

	// 根据名称注销单例Bean
	public void destroySingleTon(String beanName) {

		// 删除单例Bean
		removeSingleTon(beanName);

		// 处理Disposal
		DisposableBean disposableBean = null;
		synchronized (this.disposableBeans) {
			disposableBean = (DisposableBean) this.disposableBeans.remove(beanName);
		}
		destroyBean(beanName, disposableBean);
	}

	protected void removeSingleTon(String beanName) {
		Assert.hasText(beanName, "bean name 不能为空。");
		synchronized (this.singleTonCathe) {
			this.singleTonCathe.remove(beanName);
		}
	}

	@SuppressWarnings("rawtypes")
	protected void destroyBean(String beanName, DisposableBean disposableBean) {

		// 删除依赖Bean
		Set dependencies = null;
		synchronized (this.dependentBeanMap) {
			dependencies = (Set) this.dependentBeanMap.remove(beanName);
		}
		if (dependencies != null) {
			for (Iterator it = dependencies.iterator(); it.hasNext();) {
				destroySingleTon((String) it.next());
			}
		}

		// 删除DisposalBean
		if (disposableBean != null) {
			try {
				disposableBean.destroy();
			} catch (Throwable ex) {
				logger.error("执行Disposable Bean " + disposableBean + "的Destroy方法时出错。" + ex);
			}
		}

	}

	public final boolean isSingletonCurrentlyInCreation(String beanName) {
		return this.singletonCurrentlyInCreation.contains(beanName);
	}

	@Override
	public void registerSingleton(String beanName, Object singletonObject) {

		Assert.hasText(beanName, "beanName不能为空。");
		Assert.notNull(singletonObject, "singletonObject不能为空。");
		synchronized (this.singleTonCathe) {
			Object oldInstance = this.singleTonCathe.get(beanName);
			if (oldInstance != null) {
				throw new IllegalStateException("不能注册单例bean" + beanName + "，因为已经存在注册对象。");
			}
			addSingleton(beanName, singletonObject);
		}
	}

	@Override
	public boolean containsSingleton(String beanName) {
		Assert.hasText(beanName, "beanName不能为空。");
		synchronized (this.singleTonCathe) {
			return this.singleTonCathe.containsKey(beanName);
		}
	}

	@Override
	public Object getSingleton(String beanName) {
		synchronized (this.singleTonCathe) {
			return this.singleTonCathe.get(beanName);
		}
	}

	public Object getSingleton(String beanName, ObjectFactory singletonFactory) {
		synchronized (this.singleTonCathe) {
			Object sharedBean = this.singleTonCathe.get(beanName);
			if (sharedBean == null) {
				if (this.singleTonsCurrentlyInDestruction) {
					throw new BeanCreationNotAllowedException(beanName, "bean工厂正在注销中，不能获取单例bean。");
				}
				if (logger.isDebugEnabled()) {
					logger.debug("开始创建单例bean" + beanName);
				}
				try {
					beforeSingletonCreation(beanName);
					sharedBean = singletonFactory.getObject();
				} finally {
					afterSingletonCreation(beanName);
				}
				addSingleton(beanName, sharedBean);
			}
			return sharedBean;
		}
	}

	protected void addSingleton(String beanName, Object sharedBean) {
		Assert.hasText(beanName, "beanName不能为空。");
		Assert.notNull(sharedBean, "sharedBean不能为空。");
		synchronized (this.singleTonCathe) {
			this.singleTonCathe.put(beanName, sharedBean);
		}
	}

	protected void beforeSingletonCreation(String beanName) {
		if (!this.singletonCurrentlyInCreation.add(beanName)) {
			throw new IllegalStateException("单例bean" + beanName + "正在创建中。");
		}
	}

	protected void afterSingletonCreation(String beanName) {
		if (!this.singletonCurrentlyInCreation.remove(beanName)) {
			throw new IllegalStateException("单例bean" + beanName + "不在创建中。");
		}
	}

	@Override
	public int getSingletonCount() {
		synchronized (this.singleTonCathe) {
			return this.singleTonCathe.size();
		}
	}

	@Override
	public String[] getSingletonNames() {
		synchronized (this.singleTonCathe) {
			return StringUtils.toStringArray(this.singleTonCathe.keySet());
		}
	}

	public void registerDependentBean(String beanName, String dependentBeanName) {
		synchronized (this.dependentBeanMap) {
			Set dependencies = (Set) this.dependentBeanMap.get(beanName);
			if (dependencies == null) {
				dependencies = CollectionFactory.createLinkedSetIfPossible(8);
				this.dependentBeanMap.put(beanName, dependencies);
			}
			dependencies.add(dependentBeanName);
		}
	}

	protected boolean hasDependentBean(String beanName) {
		synchronized (this.dependentBeanMap) {
			return this.dependentBeanMap.containsKey(beanName);
		}
	}

	@SuppressWarnings("unchecked")
	protected Set getDependentBeans(String beanName) {
		synchronized (this.dependentBeanMap) {
			return Collections.unmodifiableSet((Set) this.dependentBeanMap.get(beanName));
		}
	}

	public void registerDisposableBean(String beanName, DisposableBean bean) {
		synchronized (this.disposableBeans) {
			this.disposableBeans.put(beanName, bean);
		}
	}

}
