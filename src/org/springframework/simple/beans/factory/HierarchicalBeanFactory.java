package org.springframework.simple.beans.factory;

public interface HierarchicalBeanFactory extends BeanFactory {

	BeanFactory getParentBeanFactory();
	
	boolean containLocalBean(String name);
}
