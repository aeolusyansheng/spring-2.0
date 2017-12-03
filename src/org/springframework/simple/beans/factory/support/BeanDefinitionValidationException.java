package org.springframework.simple.beans.factory.support;

import org.springframework.simple.beans.FatalBeanException;

@SuppressWarnings("serial")
public class BeanDefinitionValidationException extends FatalBeanException {
	
	
	public BeanDefinitionValidationException(String msg) {
		super(msg);
	}

	public BeanDefinitionValidationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
