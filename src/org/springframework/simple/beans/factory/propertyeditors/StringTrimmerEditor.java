package org.springframework.simple.beans.factory.propertyeditors;

import java.beans.PropertyEditorSupport;

import org.springframework.util.StringUtils;

public class StringTrimmerEditor extends PropertyEditorSupport {

	private final String charsToDelete;
	private final boolean EmptyAsNull;

	public StringTrimmerEditor(boolean EmptyAsNull) {
		this.charsToDelete = "";
		this.EmptyAsNull = EmptyAsNull;
	}

	public StringTrimmerEditor(String charsToDelete, boolean EmptyAsNull) {
		this.charsToDelete = charsToDelete;
		this.EmptyAsNull = EmptyAsNull;
	}

	public void setAsText(String text) {
		if (text == null) {
			setValue(null);
		} else {
			String value = text.trim();
			if (this.charsToDelete != null) {
				// 删除对象字符。
				value = StringUtils.deleteAny(value, this.charsToDelete);
			}
			if (this.EmptyAsNull && "".equals(value)) {
				setValue(null);
			} else {
				setValue(value);
			}
		}
	}

	public String getAsText() {
		Object value = getValue();
		return (value != null ? value.toString() : "");
	}
}
