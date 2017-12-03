package org.springframework.simple.beans.factory.propertyeditors;

import java.beans.PropertyEditorSupport;

public class CharArrayPropertyEditor extends PropertyEditorSupport {

	public void setAsText(String text) {
		setValue(text != null ? text.toCharArray() : null);
	}

	public String getAsText() {
		char[] value = (char[]) getValue();
		return (value != null ? new String(value) : "");
	}

}
