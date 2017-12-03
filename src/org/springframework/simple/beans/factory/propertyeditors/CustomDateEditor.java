package org.springframework.simple.beans.factory.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.springframework.util.StringUtils;

public class CustomDateEditor extends PropertyEditorSupport {

	private final DateFormat dateFormat;
	private final boolean allowEmpty;
	private final int exactLenght;

	public CustomDateEditor(DateFormat dateFormat, boolean allowEmpty) {
		this.dateFormat = dateFormat;
		this.allowEmpty = allowEmpty;
		this.exactLenght = -1;
	}

	public CustomDateEditor(DateFormat dateFormat, boolean allowEmpty, int exactLenght) {
		this.dateFormat = dateFormat;
		this.allowEmpty = allowEmpty;
		this.exactLenght = exactLenght;
	}

	public void setAsText(String text) {
		if (this.allowEmpty && (!StringUtils.hasText(text))) {
			setValue(null);
		} else if (StringUtils.hasText(text) && this.exactLenght >= 0 && this.exactLenght != text.length()) {
			throw new IllegalStateException("无法解析成Date型。");
		} else {
			try {
				setValue(this.dateFormat.parse(text));
			} catch (ParseException e) {
				throw new IllegalStateException("无法解析成Date型。" + e.getMessage());
			}
		}
	}

	public String getAsText() {
		Date value = (Date) getValue();
		return (value != null ? this.dateFormat.format(value) : "");
	}

}
