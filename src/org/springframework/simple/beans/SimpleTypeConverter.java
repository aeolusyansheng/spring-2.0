package org.springframework.simple.beans;

import org.springframework.core.MethodParameter;

public class SimpleTypeConverter extends PropertyEditorRegistrySupport implements TypeConverter {

	private final TypeConverterDelegate typeConverterDelegate = new TypeConverterDelegate(this);

	public SimpleTypeConverter() {
		registerDefaultEditors();
	}

	@Override
	public Object convertIfNecessory(Object value, Class reqiredType) throws TypeMismatchException {
		return convertIfNecessory(value, reqiredType, null);
	}

	@Override
	public Object convertIfNecessory(Object value, Class reqiredType, MethodParameter methodParameter)
			throws TypeMismatchException {
		try {
			return this.typeConverterDelegate.convertIfNecessary(value, reqiredType, methodParameter);
		} catch (IllegalStateException ex) {
			throw new TypeMismatchException(value, reqiredType, ex);
		}
	}

}
