package org.springframework.simple.beans.factory.propertyeditors;

import java.beans.PropertyEditorSupport;

import org.springframework.util.StringUtils;

public class LocaleEditor extends PropertyEditorSupport {

	public void setAsText(String text) {
		setValue(StringUtils.parseLocaleString(text));
	}

	public String getAsText() {
		Object value = getValue();
		return (value != null ? value.toString() : "");
	}
}
