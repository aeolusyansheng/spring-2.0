package org.springframework.simple.context.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.simple.beans.BeansException;
import org.springframework.simple.beans.factory.BeanFactory;
import org.springframework.simple.beans.factory.DisposableBean;
import org.springframework.simple.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.simple.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.simple.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.simple.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.simple.beans.support.ResourceEditorRegistrar;
import org.springframework.simple.context.ApplicationContext;
import org.springframework.simple.context.ApplicationContextAware;
import org.springframework.simple.context.ApplicationContextException;
import org.springframework.simple.context.ApplicationEvent;
import org.springframework.simple.context.ApplicationEventPublisherAware;
import org.springframework.simple.context.ConfigurableApplicationContext;
import org.springframework.simple.context.MessageSourceAware;
import org.springframework.simple.context.MessgaeSourceResolvable;
import org.springframework.simple.context.NoSuchMessageException;
import org.springframework.simple.context.ResourceLoaderAware;

public abstract class AbstractApplicationContext extends DefaultResourceLoader
		implements ConfigurableApplicationContext, DisposableBean {

	protected final Log loggor = LogFactory.getLog(getClass());

	// 同步用Flag，记录容器是否处于启动中，销毁中
	private final Object startupShutdownMonitor = new Object();

	// 同步用Flag，用于件事active Flag
	private final Object avtiveMonitor = new Object();
	// 记录容器是否处于活动中
	private boolean active = false;

	// 父容器
	private ApplicationContext parent;

	// 容器使用的资源加载器
	private ResourcePatternResolver resourcePatternResolver;

	// 保存注册的BeanFactory后处理器
	private final List beanFactoryPostProcessors = new ArrayList();

	// 容器启动时间
	private long startupTime = 0;

	private String displayName = getClass().getName() + ";hashCode=" + hashCode();

	public AbstractApplicationContext() {
		this(null);
	}

	public AbstractApplicationContext(ApplicationContext parent) {
		if (parent != null) {
			this.setparent(parent);
		}
		this.resourcePatternResolver = getResourcePatternResolver();
	}

	@Override
	public void refresh() throws BeansException {
		// 刷新容器

		synchronized (this.startupShutdownMonitor) {
			this.startupTime = System.currentTimeMillis();

			synchronized (this.avtiveMonitor) {
				this.active = true;
			}

			// 以下正式开始启动容器
			// 子类刷新Factory
			refreshBeanFactory();
			// 获取
			ConfigurableListableBeanFactory beanFactory = getBeanFactory();

			// 设定bean classLoader
			beanFactory.setBeanClassLoader(getClassLoader());

			// 注册属性编辑器
			beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this));

			// 设定Bean后处理器(注册和Application相关的各个重要的Aware)
			beanFactory.addBeanPostProcesser(new ApplicationContextnAwareProcessor(this));
			// 注册的Aware不需要再依赖注入
			beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);
			beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
			beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
			beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);

			this.postProcessBeanFactory(beanFactory);

			// 上面Bean定义已经解析完毕，此处利用beanFactory后处理器将beanDefinition全部处理一遍
			for (Iterator it = this.getBeanFactoryPostProcessors().iterator(); it.hasNext();) {
				BeanFactoryPostProcessor beanFactoryPostProcessor = (BeanFactoryPostProcessor) it.next();
				beanFactoryPostProcessor.postProcessBeanFactory(beanFactory);
			}

			if (this.loggor.isInfoEnabled()) {
				if (getBeanDefinitionCount() == 0) {
					loggor.info("容器里没有bean");
				} else {
					loggor.info("容器里定义的bean有" + getBeanDefinitionCount() + "个。");
				}
			}

			try {

				// 激活beanFactory后处理器
				this.invokeBeanFactoryPostProcessors();
				// 注册bean后处理器
				this.registerBeanPostProcessors();
				// 为容器初始化消息资源
				this.initMessageSource();
				// 为容器初始化多路广播器
				this.initApplicationEventMulticasters();
				// 初始化子类指定的特定的bean
				this.onRefresh();
				// 校验并注册监听器
				this.registerListeners();
				// 实例化容器内所有的可早期暴露的单例bean
				beanFactory.preInstantiateSingleTons();
				// 广播相应的事件
				// this.publishEvent(beanFactory.);

			} catch (BeansException ex) {
				beanFactory.destroySingleTons();
				throw ex;
			}

		}

	}

	public int getBeanDefinitionCount() {
		return getBeanFactory().getBeanDefinitionCount();
	}

	protected List getBeanFactoryPostProcessors() {
		return this.beanFactoryPostProcessors;
	}

	protected abstract void refreshBeanFactory() throws BeansException, IllegalStateException;

	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

	}

	protected ResourcePatternResolver getResourcePatternResolver() {
		return new PathMatchingResourcePatternResolver(this);
	}

	@Override
	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public ApplicationContext getParent() {
		return this.parent;
	}

	protected BeanFactory getInternalParentBeanFactory() {
		if (getParent() instanceof ConfigurableApplicationContext) {
			return ((ConfigurableApplicationContext) getParent()).getBeanFactory();
		} else {
			return (BeanFactory) getParent();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void invokeBeanFactoryPostProcessors() {

		String[] factoryPostProcessorNames = getBeanNamesForType(BeanFactoryPostProcessor.class, true, true);

		// 有序BeanFactory后处理器
		List orderedFactoryProcesssors = new ArrayList();
		// 无序BeanFactory后处理器
		List nonOrderedFactoryProcessorNames = new ArrayList();

		for (int i = 0; i < factoryPostProcessorNames.length; i++) {
			if (Ordered.class.isAssignableFrom(getType(factoryPostProcessorNames[i]))) {
				orderedFactoryProcesssors.add(getBean(factoryPostProcessorNames[i]));
			} else {
				nonOrderedFactoryProcessorNames.add(factoryPostProcessorNames[i]);
			}
		}

		Collections.sort(orderedFactoryProcesssors, new OrderComparator());

		// 先执行有序后处理
		for (Iterator it = orderedFactoryProcesssors.iterator(); it.hasNext();) {
			BeanFactoryPostProcessor beanFactoryPostProcessor = (BeanFactoryPostProcessor) it.next();
			beanFactoryPostProcessor.postProcessBeanFactory(getBeanFactory());
		}

		// 后执行无序后处理
		for (Iterator it = nonOrderedFactoryProcessorNames.iterator(); it.hasNext();) {
			String beanFactoryPostProcessorName = (String) it.next();
			((BeanFactoryPostProcessor) getBean(beanFactoryPostProcessorName)).postProcessBeanFactory(getBeanFactory());
		}

	}

	private void registerBeanPostProcessors() {

	}

	private void initMessageSource() {

	}

	private void initApplicationEventMulticasters() {

	}

	private void registerListeners() throws BeansException {

	}

	public void publishEvent(ApplicationEvent event) {

	}

	protected void onRefresh() throws BeansException {

	}

	@Override
	public long getStartupDate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean containsBeanDefinition(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] getBeanDefinitionNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getBeanNamesForType(Class type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getBeanNamesForType(Class type, boolean includePrototypes, boolean allowEagerInit) {
		return getBeanFactory().getBeanNamesForType(type, includePrototypes, allowEagerInit);
	}

	@Override
	public  Map getBeanOfType(Class type) throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map getBeanOfType(Class type, boolean includePrototypes, boolean allowEagerInit)
			throws BeansException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getBean(String name) throws BeansException {
		return getBeanFactory().getBean(name);
	}

	@Override
	public Object getBean(String name, Class requiredType) throws BeansException {
		return getBeanFactory().getBean(name, requiredType);
	}

	@Override
	public boolean containsBean(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] getAlieses(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class getType(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().getType(name);
	}

	@Override
	public BeanFactory getParentBeanFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containLocalBean(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMessage(MessgaeSourceResolvable messgaeSourceResolvable, Locale locale)
			throws NoSuchMessageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource getResource(String location) {
		// TODO Auto-generated method stub
		return null;
	}

	public Resource[] getResources(String locationPattern) throws IOException {
		return this.resourcePatternResolver.getResources(locationPattern);
	}

	@Override
	public ClassLoader getClassLoader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws ApplicationContextException {
		// TODO Auto-generated method stub

	}

	@Override
	public abstract ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;

	@Override
	public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setparent(ApplicationContext parent) {
		this.parent = parent;
	}

	public void destroy() {
		close();
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void registerShutdownHook() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

}
