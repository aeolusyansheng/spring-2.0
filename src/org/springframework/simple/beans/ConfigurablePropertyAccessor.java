package org.springframework.simple.beans;

public interface ConfigurablePropertyAccessor extends PropertyAccessor, PropertyEditorRegistry {

	boolean isExtractOldValueForEditor();

	void setExtractOldValueForEditor(boolean extractOldValueForEditor);
}
