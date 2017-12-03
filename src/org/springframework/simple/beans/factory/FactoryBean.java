package org.springframework.simple.beans.factory;

public interface FactoryBean {

	Object getObject() throws Exception;

	Class getOjectType();

	boolean isSingleTon();
}
