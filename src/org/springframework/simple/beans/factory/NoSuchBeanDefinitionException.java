package org.springframework.simple.beans.factory;

import org.springframework.simple.beans.BeansException;

@SuppressWarnings("serial")
public class NoSuchBeanDefinitionException extends BeansException {

	private String name;
	private Class<?> type;

	public NoSuchBeanDefinitionException(String name) {
		super("no such bean named " + name + " is definied.");
		this.name = name;
	}

	public NoSuchBeanDefinitionException(String name, String message) {
		super("no such bean named " + name + " is definied." + message);
		this.name = name;
	}

	public NoSuchBeanDefinitionException(Class<?> type, String message) {
		super("no bean of type " + type.getName() + " " + message);
		this.type = type;
	}

	public String getName() {
		return this.name;
	}

	public Class<?> getType() {
		return this.type;
	}
}
