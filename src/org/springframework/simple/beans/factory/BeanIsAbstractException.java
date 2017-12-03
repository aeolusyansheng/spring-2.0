package org.springframework.simple.beans.factory;

public class BeanIsAbstractException extends BeanCreationException {

	/**
	 * Create a new BeanIsAbstractException.
	 * 
	 * @param beanName
	 *            the name of the bean requested
	 */
	public BeanIsAbstractException(String beanName) {
		super(beanName, "Bean definition is abstract");
	}
}
