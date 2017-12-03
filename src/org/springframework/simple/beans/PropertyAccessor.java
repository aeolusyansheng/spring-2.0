package org.springframework.simple.beans;

import java.util.Map;

public interface PropertyAccessor {

	String NESTED_PROPERTY_SEPARATOR = ".";
	char NESTED_PROPERTY_SEPARATOR_CHAR = '.';

	String PROPERTY_KEY_PREFIX = "[";
	String PROPERTY_KEY_SUFFIX = "]";
	char PROPERTY_KEY_PREFIX_CHAR = '[';
	char PROPERTY_KEY_SUFFIX_CHAR = ']';

	boolean isReadableProperty(String propertyName) throws BeansException;

	boolean isWritableproperty(String propertyName) throws BeansException;

	Class getPropertyType(String propertyName) throws BeansException;

	Object getPropertyValue(String propertyName) throws BeansException;

	void setPropertyValue(String propertyName, Object value) throws BeansException;

	void setPropertyValue(PropertyValue pv) throws BeansException;

	void setPropertyValues(Map map) throws BeansException;

	void setPropertyValues(PropertyValues pvs) throws BeansException;

	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown) throws BeansException;

	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid) throws BeansException;

}
