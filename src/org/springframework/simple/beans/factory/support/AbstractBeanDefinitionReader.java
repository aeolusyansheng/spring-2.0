package org.springframework.simple.beans.factory.support;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;

public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader {

	protected final Log logger = LogFactory.getLog(getClass());
	private BeanDefinitionRegistry beanFactory;
	private ResourceLoader resourceLoader;
	private ClassLoader beanClassLoader;

	public AbstractBeanDefinitionReader(BeanDefinitionRegistry beanFactory) {
		Assert.notNull(beanFactory, "Bean Factory 不能为空");
		this.beanFactory = beanFactory;

		// 资源加载器
		if (this.beanFactory instanceof ResourceLoader) {
			this.resourceLoader = (ResourceLoader) this.beanFactory;
		} else {
			this.resourceLoader = new PathMatchingResourcePatternResolver();
		}
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	@Override
	public ClassLoader getBeanClassLoader() {
		return this.beanClassLoader;
	}

	public void setBeanClassLoader(ClassLoader cLassLoader) {
		this.beanClassLoader = cLassLoader;
	}

	@Override
	public BeanDefinitionRegistry getBeanFactory() {
		return this.beanFactory;
	}

	public int loadBeanDefinitions(Resource[] resources) {
		Assert.notNull(resources, "resources不能为空。");
		int count = 0;
		for (int i = 0; i < resources.length; i++) {
			count += loadBeanDefinitions(resources[i]);
		}

		return count;
	}

	public int loadBeanDefinitions(String configLocation) {

		// 获取资源加载器
		ResourceLoader resourceLoader = getResourceLoader();
		if (resourceLoader == null) {
			throw new BeanDefinitionStoreException("没有资源加载器，Bean读取失败。");
		}

		// 如果为Pattern
		if (resourceLoader instanceof ResourcePatternResolver) {
			try {
				
				Resource[] resources = ((ResourcePatternResolver) resourceLoader).getResources(configLocation);
				int count = loadBeanDefinitions(resources);
				if (logger.isDebugEnabled()) {
					logger.debug("从路径(pattern)" + configLocation + "加载完毕 " + count + " 个bean definitions");
				}
				return count;
			} catch (IOException e) {
				throw new BeanDefinitionStoreException("没有资源加载器，Bean读取失败。");
			}

		} else {
			// 如果为resourceLoader
			Resource resource = resourceLoader.getResource(configLocation);
			int count = loadBeanDefinitions(resource);
			if (logger.isDebugEnabled()) {
				logger.debug("从路径" + configLocation + "加载完毕 " + count + " 个bean definitions");
			}
			return count;
		}
	}

	public int loadBeanDefinitions(String[] configLocations) {
		Assert.notNull(configLocations, "configLocations不能为空。");
		int count = 0;
		for (int i = 0; i < configLocations.length; i++) {
			count += loadBeanDefinitions(configLocations[i]);
		}
		return count;
	}

}
