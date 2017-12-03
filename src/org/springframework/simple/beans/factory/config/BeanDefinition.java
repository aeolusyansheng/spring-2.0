package org.springframework.simple.beans.factory.config;

import org.springframework.core.AttributeAccessor;
import org.springframework.simple.beans.BeanMetadataElement;
import org.springframework.simple.beans.MutablePropertyValues;

public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {

	String SCOPE_SINGLETON = ConfigurableBeanFactory.SCOPE_SINGLETON;
	String SCOPE_PROTOTYPE = ConfigurableBeanFactory.SCOPE_PROTOTYPE;
	int ROLE_APPLICATION = 0;
	int ROLE_SUPPORT = 1;
	int ROLE_INFRASTRUCTURE = 2;

	String getBeanClassName();

	void setBeanClassName(String beanClassName);

	String getResourceDescription();

	int getRole();

	String getScope();

	void setScope(String scope);

	boolean isSingleton();

	boolean isAbstract();

	boolean isLazyInit();

	ConstructorArgumentValues getConstructorArgumentValues();

	MutablePropertyValues getPropertyValues();

}
