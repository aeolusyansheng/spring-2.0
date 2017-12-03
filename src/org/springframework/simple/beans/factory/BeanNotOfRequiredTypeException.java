package org.springframework.simple.beans.factory;

import org.springframework.simple.beans.BeansException;

public class BeanNotOfRequiredTypeException extends BeansException {

	/** The name of the instance that was of the wrong type */
	private String beanName;

	/** The required type */
	private Class requiredType;

	/** The offending type */
	private Class actualType;


	/**
	 * Create a new BeanNotOfRequiredTypeException.
	 * @param beanName the name of the bean requested
	 * @param requiredType the required type
	 * @param actualType the actual type returned, which did not match
	 * the expected type
	 */
	public BeanNotOfRequiredTypeException(String beanName, Class requiredType, Class actualType) {
		super("Bean named '" + beanName + "' must be of type [" + requiredType.getName() +
				"], but was actually of type [" + actualType.getName() + "]");
		this.beanName = beanName;
		this.requiredType = requiredType;
		this.actualType = actualType;
	}


	/**
	 * Return the name of the instance that was of the wrong type.
	 */
	public String getBeanName() {
		return beanName;
	}

	/**
	 * Return the required type for the bean.
	 */
	public Class getRequiredType() {
		return requiredType;
	}

	/**
	 * Return the actual type of the instance found.
	 */
	public Class getActualType() {
		return actualType;
	}
}
