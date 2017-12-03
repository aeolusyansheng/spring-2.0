package org.springframework.simple.beans.factory.propertyeditors;

import java.beans.PropertyEditorSupport;

import org.springframework.util.StringUtils;

public class StringArrayPropertyEditor extends PropertyEditorSupport {

	private static final String DEFAULT_SEPARATOR = ",";

	private final String separator;

	public StringArrayPropertyEditor() {
		this.separator = DEFAULT_SEPARATOR;
	}

	public StringArrayPropertyEditor(String separator) {
		this.separator = separator;
	}

	public void setAsText(String text) {
		String[] values = StringUtils.delimitedListToStringArray(text, this.separator);
		setValue(values);
	}

	public String getAsText() {
		String[] value = (String[]) getValue();
		return StringUtils.arrayToDelimitedString(value, this.separator);
	}
}
