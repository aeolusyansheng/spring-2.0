package org.springframework.simple.beans.factory.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.text.NumberFormat;

import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

public class CustomNumberEditor extends PropertyEditorSupport {

	@SuppressWarnings("rawtypes")
	private final Class numberClass;
	private final boolean allowEmpty;
	private final NumberFormat numberFormat;

	@SuppressWarnings("rawtypes")
	public CustomNumberEditor(Class numberClass, boolean allowEmpty) {
		this(numberClass, allowEmpty, null);
	}

	@SuppressWarnings("rawtypes")
	public CustomNumberEditor(Class numberClass, boolean allowEmpty, NumberFormat numberFormat)
			throws IllegalArgumentException {
		if (numberClass == null || !Number.class.isAssignableFrom(numberClass)) {
			throw new IllegalStateException("必须是Number的子类才能解析。");
		}
		this.numberClass = numberClass;
		this.allowEmpty = allowEmpty;
		this.numberFormat = numberFormat;
	}

	public void setAsText(String text) {
		if (!StringUtils.hasText(text)) {
			setValue(null);
		} else if (this.numberFormat != null) {
			setValue(NumberUtils.parseNumber(text, this.numberClass, this.numberFormat));
		} else {
			setValue(NumberUtils.parseNumber(text, this.numberClass));
		}
	}

	public String getAsText() {
		Object value = getValue();
		if (value == null) {
			return "";
		} else if (this.numberFormat != null) {
			return this.numberFormat.format(value);
		} else {
			return value.toString();
		}
	}
}
