package org.springframework.simple.beans.factory;

import org.springframework.simple.beans.FatalBeanException;

public class FactoryBeanNotInitializedException extends FatalBeanException {
	/**
	 * Create a new FactoryBeanNotInitializedException with the default message.
	 */
	public FactoryBeanNotInitializedException() {
		super("FactoryBean is not fully initialized yet");
	}

	/**
	 * Create a new FactoryBeanNotInitializedException with the given message.
	 * 
	 * @param msg
	 *            the detail message
	 */
	public FactoryBeanNotInitializedException(String msg) {
		super(msg);
	}
}
