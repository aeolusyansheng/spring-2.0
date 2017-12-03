package org.springframework.simple.beans.factory;

public interface DisposableBean {

	void destroy() throws Exception;
}
