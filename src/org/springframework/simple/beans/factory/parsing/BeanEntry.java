package org.springframework.simple.beans.factory.parsing;

public class BeanEntry implements ParseState.Entry {

	private String beanDefinitionName;

	public BeanEntry(String beanDefinitionName) {
		this.beanDefinitionName = beanDefinitionName;
	}

	public String toString() {
		return "Bean '" + this.beanDefinitionName + "'";
	}
}
