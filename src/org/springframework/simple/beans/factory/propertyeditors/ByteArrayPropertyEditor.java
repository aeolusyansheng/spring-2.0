package org.springframework.simple.beans.factory.propertyeditors;

import java.beans.PropertyEditorSupport;

public class ByteArrayPropertyEditor extends PropertyEditorSupport {

	public void setAsText(String text) {
		setValue(text != null ? text.getBytes() : null);
	}

	public String getAsText() {
		byte[] value = (byte[]) getValue();
		return (value != null ? new String(value) : "");

	}
}
