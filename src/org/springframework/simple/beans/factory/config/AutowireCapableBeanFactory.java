package org.springframework.simple.beans.factory.config;

import org.springframework.simple.beans.factory.BeanFactory;

public interface AutowireCapableBeanFactory extends BeanFactory {

	int AUTOWIRE_NO=0;
	int AUTOWIRE_BY_NAME=1;
	int AUTOWIRE_BY_TYPE=2;
	int AUTOWIRE_BY_CONSTRUCTOR=3;
	int AUTOWIRE_AUTODETECT=4;
	
	
}
