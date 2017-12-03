package org.springframework.simple.beans;

public interface PropertyValues {

	boolean contains(String propertyName);

	PropertyValue[] getPropertyValues();

	PropertyValue getPropertyValue(String propertyName);

	boolean isEmpty();

	PropertyValues changesSince(PropertyValues old);
}
