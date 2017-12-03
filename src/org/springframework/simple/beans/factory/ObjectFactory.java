package org.springframework.simple.beans.factory;

import org.springframework.simple.beans.BeansException;

public interface ObjectFactory {

	Object getObject() throws BeansException;
}
