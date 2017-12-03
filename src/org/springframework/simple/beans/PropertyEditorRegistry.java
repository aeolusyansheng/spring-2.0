package org.springframework.simple.beans;

import java.beans.PropertyEditor;

public interface PropertyEditorRegistry {

	@SuppressWarnings("rawtypes")
	void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor);

	@SuppressWarnings("rawtypes")
	void registerCustomEditor(Class requiredType, String propertyPath, PropertyEditor propertyEditor);

	@SuppressWarnings("rawtypes")
	PropertyEditor findCustomEditor(Class requiredType, String propertyPath);
}
