package org.springframework.simple.beans.factory;

import org.springframework.simple.beans.FatalBeanException;

public class BeanCreationException extends FatalBeanException {
	private String resourceDescription;

	private String beanName;

	/**
	 * Create a new BeanCreationException.
	 * 
	 * @param msg
	 *            the detail message
	 */
	public BeanCreationException(String msg) {
		super(msg);
	}

	/**
	 * Create a new BeanCreationException.
	 * 
	 * @param msg
	 *            the detail message
	 * @param cause
	 *            the root cause
	 */
	public BeanCreationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Create a new BeanCreationException.
	 * 
	 * @param beanName
	 *            the name of the bean requested
	 * @param msg
	 *            the detail message
	 */
	public BeanCreationException(String beanName, String msg) {
		this(beanName, msg, (Throwable) null);
	}

	/**
	 * Create a new BeanCreationException.
	 * 
	 * @param beanName
	 *            the name of the bean requested
	 * @param msg
	 *            the detail message
	 * @param cause
	 *            the root cause
	 */
	public BeanCreationException(String beanName, String msg, Throwable cause) {
		super("Error creating bean with name '" + beanName + "': " + msg, cause);
	}

	/**
	 * Create a new BeanCreationException.
	 * 
	 * @param resourceDescription
	 *            description of the resource that the bean definition came from
	 * @param beanName
	 *            the name of the bean requested
	 * @param msg
	 *            the detail message
	 */
	public BeanCreationException(String resourceDescription, String beanName, String msg) {
		this(resourceDescription, beanName, msg, null);
	}

	/**
	 * Create a new BeanCreationException.
	 * 
	 * @param resourceDescription
	 *            description of the resource that the bean definition came from
	 * @param beanName
	 *            the name of the bean requested
	 * @param msg
	 *            the detail message
	 * @param cause
	 *            the root cause
	 */
	public BeanCreationException(String resourceDescription, String beanName, String msg, Throwable cause) {
		super("Error creating bean with name '" + beanName + "'"
				+ (resourceDescription != null ? " defined in " + resourceDescription : "") + ": " + msg, cause);
		this.resourceDescription = resourceDescription;
		this.beanName = beanName;
	}

	/**
	 * Return the description of the resource that the bean definition came from, if
	 * any.
	 */
	public String getResourceDescription() {
		return resourceDescription;
	}

	/**
	 * Return the name of the bean requested, if any.
	 */
	public String getBeanName() {
		return beanName;
	}
}
