package org.springframework.simple.beans.factory;

public interface InitializingBean {

	void afterPropertiesSet() throws Exception;
}
