package org.springframework.simple.beans.factory.config;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

public class TypedStringValue {

	private String value;

	private Object targetType;


	/**
	 * Create a new {@link TypedStringValue} for the given String
	 * value and target type.
	 * @param value the String value
	 * @param targetType the type to convert to
	 */
	public TypedStringValue(String value, Class targetType) {
		setValue(value);
		setTargetType(targetType);
	}

	/**
	 * Create a new {@link TypedStringValue} for the given String
	 * value and target type.
	 * @param value the String value
	 * @param targetTypeName the type to convert to
	 */
	public TypedStringValue(String value, String targetTypeName) {
		setValue(value);
		setTargetTypeName(targetTypeName);
	}


	/**
	 * Set the String value.
	 * Only necessary for manipulating a registered value,
	 * for example in BeanFactoryPostProcessors.
	 * @see PropertyPlaceholderConfigurer
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Return the String value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Return whether this typed String value carries a target type .
	 */
	public boolean hasTargetType() {
		return (this.targetType instanceof Class);
	}

	/**
	 * Set the type to convert to.
	 * Only necessary for manipulating a registered value,
	 * for example in BeanFactoryPostProcessors.
	 * @see PropertyPlaceholderConfigurer
	 */
	public void setTargetType(Class targetType) {
		Assert.notNull(targetType, "targetType is required");
		this.targetType = targetType;
	}

	/**
	 * Return the type to convert to.
	 */
	public Class getTargetType() {
		if (!(this.targetType instanceof Class)) {
			throw new IllegalStateException("Typed String value does not carry a resolved target type");
		}
		return (Class) this.targetType;
	}

	/**
	 * Specify the type to convert to.
	 */
	public void setTargetTypeName(String targetTypeName) {
		Assert.notNull(targetTypeName, "targetTypeName is required");
		this.targetType = targetTypeName;
	}

	/**
	 * Return the type to convert to.
	 */
	public String getTargetTypeName() {
		if (this.targetType instanceof Class) {
			return ((Class) this.targetType).getName();
		}
		else {
			return (String) this.targetType;
		}
	}

	/**
	 * Determine the type to convert to, resolving it from a specified class name
	 * if necessary. Will also reload a specified Class from its name when called
	 * with the target type already resolved.
	 * @param classLoader the ClassLoader to use for resolving a (potential) class name
	 * @return the resolved type to convert to
	 */
	public Class resolveTargetType(ClassLoader classLoader) throws ClassNotFoundException {
		if (this.targetType == null) {
			return null;
		}
		Class resolvedClass = ClassUtils.forName(getTargetTypeName(), classLoader);
		this.targetType = resolvedClass;
		return resolvedClass;
	}
	
	
}
