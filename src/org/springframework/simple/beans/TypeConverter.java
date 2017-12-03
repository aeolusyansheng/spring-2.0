package org.springframework.simple.beans;

import org.springframework.core.MethodParameter;

public interface TypeConverter {

	Object convertIfNecessory(Object value, Class reqiredType) throws TypeMismatchException;

	Object convertIfNecessory(Object value, Class reqiredType, MethodParameter methodParameter)
			throws TypeMismatchException;
}
