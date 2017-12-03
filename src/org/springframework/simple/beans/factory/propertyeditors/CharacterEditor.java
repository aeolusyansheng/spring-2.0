package org.springframework.simple.beans.factory.propertyeditors;

import java.beans.PropertyEditorSupport;

import org.springframework.util.StringUtils;

public class CharacterEditor extends PropertyEditorSupport {

	// Unicode字符前缀
	private static final String UNICODE_PREFIX = "¥¥u";
	private static final int UNICODE_LENGTH = 6;

	private boolean allowEmpty;

	public CharacterEditor(boolean allowEmpty) {
		this.allowEmpty = allowEmpty;
	}

	public void setAsText(String text) {

		if (this.allowEmpty && (!StringUtils.hasText(text))) {
			setValue(null);
		} else if (text == null) {
			throw new IllegalStateException("空字符串不能转换成字符型。");
		} else if (isUnicodeCharacterSequence(text)) {
			setUnicode(text);
		} else if (text.length() != 1) {
			throw new IllegalStateException("字符串长度大于1，不能传唤成字符型。");
		} else {
			setValue(text.charAt(0));
		}
	}

	public void setUnicode(String text) {
		int chatInt = Integer.parseInt(text.substring(UNICODE_PREFIX.length()), 16);
		setValue(new Character((char) chatInt));
	}

	private boolean isUnicodeCharacterSequence(String text) {
		return text.startsWith(UNICODE_PREFIX) && text.length() == UNICODE_LENGTH;
	}

	public String getAsText() {
		Object value = getValue();
		return (value != null ? value.toString() : "");
	}

}
