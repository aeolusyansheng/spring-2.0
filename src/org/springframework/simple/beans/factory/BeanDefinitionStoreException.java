package org.springframework.simple.beans.factory;

import org.springframework.simple.beans.FatalBeanException;

@SuppressWarnings("serial")
public class BeanDefinitionStoreException extends FatalBeanException {

	private String resourceDecription;
	private String beanName;

	public String getBeanName() {
		return this.beanName;
	}

	public String getResourceDescription() {
		return this.resourceDecription;
	}

	public BeanDefinitionStoreException(String message) {
		super(message);
	}

	public BeanDefinitionStoreException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public BeanDefinitionStoreException(String resourceDescription, String msg, Throwable cause) {
		super(msg, cause);
		this.resourceDecription = resourceDescription;
	}

	public BeanDefinitionStoreException(String message, String beanName, String resourceDecription) {
		super("Error registering bean with name '" + beanName + "' is definied in " + resourceDecription + " "
				+ message);
		this.beanName = beanName;
		this.resourceDecription = resourceDecription;
	}

	public BeanDefinitionStoreException(String message, String beanName, String resourceDecription, Throwable cause) {
		super("Error registering bean with name '" + beanName + "' is definied in " + resourceDecription + " "
				+ message, cause);
		this.beanName = beanName;
		this.resourceDecription = resourceDecription;
	}
}
