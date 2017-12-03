package org.springframework.simple.beans.factory.config;

import org.springframework.util.Assert;

public class RuntimeBeanReference implements BeanReference {

	private final String beanName;

	private final boolean toParent;

	private Object source;

	public RuntimeBeanReference(String beanName) {
		this(beanName, false);
	}

	public RuntimeBeanReference(String beanName, boolean toParent) {
		Assert.hasText(beanName, "Bean name must not be empty");
		this.beanName = beanName;
		this.toParent = toParent;
	}

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
		if (!(other instanceof RuntimeBeanReference)) {
			return false;
		}
		RuntimeBeanReference that = (RuntimeBeanReference) other;
		return (this.beanName.equals(that.beanName) && this.toParent == that.toParent);
	}

	public int hashCode() {
		int result = this.beanName.hashCode();
		result = 29 * result + (this.toParent ? 1 : 0);
		return result;
	}

	public String toString() {
		return '<' + getBeanName() + '>';
	}

	public String getBeanName() {
		return beanName;
	}

	public boolean isToParent() {
		return toParent;
	}

}
