package org.springframework.simple.beans.factory.config;

import java.beans.PropertyEditor;

import org.springframework.simple.beans.PropertyEditorRegistrar;
import org.springframework.simple.beans.factory.BeanDefinitionStoreException;
import org.springframework.simple.beans.factory.BeanFactory;
import org.springframework.simple.beans.factory.HierarchicalBeanFactory;

public interface ConfigurableBeanFactory extends HierarchicalBeanFactory,SingletonBeanRegistry {

	String SCOPE_PROTOTYPE = "prototype";
	String SCOPE_SINGLETON = "singleton";

	void addBeanPostProcesser(BeanPostProcessor beanPostProcessor);

	void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar);

	void copyConfigurationFrom(ConfigurableBeanFactory otherFactory);

	void destroyBean(String beanName, Object beanInstance);

	void destroyScopedBean(String beanName);

	void destroySingleTons();

	ClassLoader getBeanClassLoader();
	int getBeanPostProcessorCount();
	Scope getRegisteredScope(String scopeName);
	String[] getRegisteredScopeNames();
	boolean isCacheBeanMetedata();
	boolean isCurrentlyInCreation(String beanName);
	void registerAlias(String beanName,String alias) throws BeanDefinitionStoreException;
	
	void registerCustomEditor(Class requiredType,PropertyEditor propertyEditor);
	void registerScope(String scopeName,Scope scope);
	void setBeanClassLoader(ClassLoader beanClassLoader);
	void setCacheBeanMetedata(boolean cacheBeanMetedata);
	void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException;
}
