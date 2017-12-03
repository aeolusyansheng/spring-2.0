package org.springframework.simple.context.support;

import org.springframework.simple.context.ApplicationContext;

public class ClassPathXmlApplicationContext extends AbstractXmlApplicationContext {

	private String[] configLocations;

	public ClassPathXmlApplicationContext(String configLocation) {
		this(new String[] { configLocation });
	}

	public ClassPathXmlApplicationContext(String[] configLocations) {
		this(configLocations, null);
	}

	public ClassPathXmlApplicationContext(String[] configLocations, ApplicationContext parent) {
		super(parent);
		this.configLocations = configLocations;
		refresh();
	}

	public String[] getConfigLocations() {
		return this.configLocations;
	}

}
