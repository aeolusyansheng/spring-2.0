package org.springframework.simple.beans.factory.propertyeditors;

import java.beans.PropertyEditorSupport;

import org.springframework.util.StringUtils;

public class CustomBooleanEditor extends PropertyEditorSupport {

	private static final String VALUE_TRUE = "true";
	private static final String VALUE_ON = "on";
	private static final String VALUE_YES = "yes";
	private static final String VALUE_1 = "1";

	private static final String VALUE_FALSE = "false";
	private static final String VALUE_OFF = "off";
	private static final String VALUE_NO = "no";
	private static final String VALUE_0 = "0";

	private final String trueString;
	private final String falseString;
	private final boolean allowEmpty;

	public CustomBooleanEditor(boolean allowEmpty) {
		this(null, null, allowEmpty);
	}

	public CustomBooleanEditor(String trueString, String falseString, boolean allowEmpty) {
		this.trueString = trueString;
		this.falseString = falseString;
		this.allowEmpty = allowEmpty;
	}

	public void setAsText(String text) {
		if (this.allowEmpty && !StringUtils.hasText(text)) {
			setValue(null);
		} else if (this.trueString != null && this.trueString.equalsIgnoreCase(text)) {
			setValue(Boolean.TRUE);
		} else if (this.falseString != null && this.falseString.equalsIgnoreCase(text)) {
			setValue(Boolean.FALSE);
		} else if (this.trueString == null && (text.equalsIgnoreCase(VALUE_1) || text.equalsIgnoreCase(VALUE_YES)
				|| text.equalsIgnoreCase(VALUE_TRUE) || text.equalsIgnoreCase(VALUE_ON))) {
			setValue(Boolean.TRUE);
		} else if (this.falseString == null && (text.equalsIgnoreCase(VALUE_0) || text.equalsIgnoreCase(VALUE_NO)
				|| text.equalsIgnoreCase(VALUE_FALSE) || text.equalsIgnoreCase(VALUE_OFF))) {
			setValue(Boolean.FALSE);
		} else {
			throw new IllegalArgumentException("无效的布尔值 [" + text + "]");
		}
	}

	public String getAsText() {
		Boolean value = (Boolean) getValue();
		if (value == null) {
			return "";
		} else if (value.equals(Boolean.TRUE)) {
			return (this.trueString == null ? VALUE_TRUE : this.trueString);
		} else if (value.equals(Boolean.FALSE)) {
			return (this.falseString == null ? VALUE_FALSE : this.falseString);
		} else {
			throw new IllegalArgumentException("无效的布尔值 [" + value.toString() + "]");
		}
	}

}
