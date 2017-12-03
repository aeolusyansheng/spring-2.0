package org.springframework.simple.beans.factory.parsing;

import org.springframework.simple.beans.factory.config.BeanDefinition;
import org.springframework.simple.beans.factory.config.BeanReference;

public abstract class AbstractComponentDefinition implements ComponentDefinition {


	@Override
	public String getDescription() {
		return getName();
	}


	@Override
	public BeanDefinition[] getInnerBeanDefinitions() {
		return new BeanDefinition[0];
	}

	@Override
	public BeanReference[] getBeanReferences() {
		return new BeanReference[0];
	}
	
	public String toString() {
		return getDescription();
	}

}
