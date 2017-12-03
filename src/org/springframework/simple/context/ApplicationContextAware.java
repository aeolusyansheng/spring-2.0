package org.springframework.simple.context;

import org.springframework.simple.beans.BeansException;

public interface ApplicationContextAware {

	void setApplicationContext(ApplicationContext applicationContext) throws BeansException;

}
