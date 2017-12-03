package org.springframework.simple.beans;

import java.beans.PropertyChangeEvent;

import org.springframework.core.ErrorCoded;

public abstract class PropertyAccessException extends BeansException implements ErrorCoded {
	private PropertyChangeEvent propertyChangeEvent;

	/**
	 * Create a new PropertyAccessException.
	 * 
	 * @param propertyChangeEvent
	 *            the PropertyChangeEvent that resulted in the problem
	 * @param msg
	 *            the detail message
	 * @param cause
	 *            the root cause
	 */
	public PropertyAccessException(PropertyChangeEvent propertyChangeEvent, String msg, Throwable cause) {
		super(msg, cause);
		this.propertyChangeEvent = propertyChangeEvent;
	}

	/**
	 * Create a new PropertyAccessException without PropertyChangeEvent.
	 * 
	 * @param msg
	 *            the detail message
	 * @param cause
	 *            the root cause
	 */
	public PropertyAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Return the PropertyChangeEvent that resulted in the problem. Only available
	 * if an actual bean property was affected.
	 */
	public PropertyChangeEvent getPropertyChangeEvent() {
		return propertyChangeEvent;
	}
}
