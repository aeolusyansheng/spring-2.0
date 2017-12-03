package org.springframework.simple.beans.factory;

import org.springframework.simple.beans.BeansException;

public interface BeanFactoryAware {

	void setBeanFactory(BeanFactory factory) throws BeansException;
}
