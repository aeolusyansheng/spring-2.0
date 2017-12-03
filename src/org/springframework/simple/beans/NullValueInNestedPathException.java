package org.springframework.simple.beans;

public class NullValueInNestedPathException extends InvalidPropertyException {
	/**
	 * Create a new NullValueInNestedPathException.
	 * 
	 * @param beanClass
	 *            the offending bean class
	 * @param propertyName
	 *            the offending property
	 */
	public NullValueInNestedPathException(Class beanClass, String propertyName) {
		super(beanClass, propertyName, "Value of nested property '" + propertyName + "' is null");
	}

	/**
	 * Create a new NullValueInNestedPathException.
	 * 
	 * @param beanClass
	 *            the offending bean class
	 * @param propertyName
	 *            the offending property
	 * @param msg
	 *            the detail message
	 */
	public NullValueInNestedPathException(Class beanClass, String propertyName, String msg) {
		super(beanClass, propertyName, msg);
	}
}
