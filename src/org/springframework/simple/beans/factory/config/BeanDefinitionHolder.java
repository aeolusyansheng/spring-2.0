package org.springframework.simple.beans.factory.config;

public class BeanDefinitionHolder {

	private BeanDefinition beanDefinition;
	private String beanName;
	private String[] alias;

	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName) {
		this(beanDefinition, beanName, null);
	}

	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName, String[] alias) {
		this.beanDefinition = beanDefinition;
		this.beanName = beanName;
		this.alias = alias;
	}

	public String getBeanName() {
		return this.beanName;
	}

	public String[] getAlias() {
		return this.alias;
	}

	public BeanDefinition getBeanDefinition() {
		return this.beanDefinition;
	}
	
	public String toString() {
		return "Bean definition with name '" + this.beanName + "': " + this.beanDefinition;
	}
}
