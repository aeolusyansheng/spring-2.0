package org.springframework.simple.beans;

import org.springframework.core.NestedRuntimeException;

@SuppressWarnings("serial")
public abstract class BeansException extends NestedRuntimeException {

	public BeansException(String message) {
		super(message);
	}

	public BeansException(String message,Throwable cause) {
		super(message,cause);
	}
}
