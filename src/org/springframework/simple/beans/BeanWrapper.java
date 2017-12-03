package org.springframework.simple.beans;

import java.beans.PropertyDescriptor;

public interface BeanWrapper extends ConfigurablePropertyAccessor {

	void setWrappedInstance(Object object);

	Object getWrappedInstance();

	Class getWrappedClass();

	PropertyDescriptor[] getPropertyDescriptors() throws BeansException;

	PropertyDescriptor getPropertyDescriptor(String propertyName) throws BeansException;

}
