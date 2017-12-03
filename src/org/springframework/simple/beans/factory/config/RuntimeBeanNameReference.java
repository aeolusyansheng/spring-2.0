package org.springframework.simple.beans.factory.config;

import org.springframework.util.Assert;

public class RuntimeBeanNameReference implements BeanReference {

	private final String beanName;

	private Object source;

	/**
	 * Create a new RuntimeBeanNameReference to the given bean name.
	 * 
	 * @param beanName
	 *            name of the target bean
	 */
	public RuntimeBeanNameReference(String beanName) {
		Assert.hasText(beanName, "Bean name must not be empty");
		this.beanName = beanName;
	}

	public String getBeanName() {
		return beanName;
	}

	/**
	 * Set the configuration source <code>Object</code> for this metadata element.
	 * <p>
	 * The exact type of the object will depend on the configuration mechanism used.
	 */
	public void setSource(Object source) {
		this.source = source;
	}

	public Object getSource() {
		return source;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof RuntimeBeanNameReference)) {
			return false;
		}
		RuntimeBeanNameReference that = (RuntimeBeanNameReference) other;
		return this.beanName.equals(that.beanName);
	}

	public int hashCode() {
		return this.beanName.hashCode();
	}

	public String toString() {
		return '<' + getBeanName() + '>';
	}

}
