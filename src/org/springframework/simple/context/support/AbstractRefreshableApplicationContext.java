package org.springframework.simple.context.support;

import java.io.IOException;

import org.springframework.simple.beans.BeansException;
import org.springframework.simple.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.simple.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.simple.context.ApplicationContext;
import org.springframework.simple.context.ApplicationContextException;

public abstract class AbstractRefreshableApplicationContext extends AbstractApplicationContext {

	// 内部BeanFactory的监视器
	private final Object beanFactoryMonitor = new Object();

	// BeanFactory
	private DefaultListableBeanFactory beanFactory;

	public AbstractRefreshableApplicationContext() {

	}

	public AbstractRefreshableApplicationContext(ApplicationContext parent) {
		super(parent);
	}

	protected final void refreshBeanFactory() {

		// 停止现行Beanfactory
		synchronized (this.beanFactoryMonitor) {
			if (this.beanFactory != null) {
				this.beanFactory.destroySingleTons();
				this.beanFactory = null;
			}
		}

		// 启动新的BeanFactory
		try {
			// 创建BeanFactory实例
			DefaultListableBeanFactory beanFactory = createBeanFactory();
			// 加载XML数据
			loadBeanDefinitions(beanFactory);
			synchronized (this.beanFactoryMonitor) {
				this.beanFactory = beanFactory;
			}
			if (loggor.isInfoEnabled()) {
				loggor.info("容器的名称" + getDisplayName());
			}

		} catch (IOException ex) {
			throw new ApplicationContextException("解析XML时出错" + ex);
		}
	}

	public final ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException {

		synchronized (this.beanFactoryMonitor) {
			if (this.beanFactory == null) {
				throw new IllegalStateException("Bean 工厂没有被初始化或者处理关闭中。需要先执行refresh处理。");
			}
		}
		return this.beanFactory;
	}

	protected abstract void loadBeanDefinitions(DefaultListableBeanFactory defaultListableBeanFactory)
			throws IOException, BeansException;

	protected DefaultListableBeanFactory createBeanFactory() {
		return new DefaultListableBeanFactory(getInternalParentBeanFactory());
	}
}
