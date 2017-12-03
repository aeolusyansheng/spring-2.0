package org.springframework.simple.beans.factory.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;

import org.springframework.util.StringUtils;

public class PropertiesEditor extends PropertyEditorSupport {

	public void setAsText(String text) throws IllegalArgumentException {
		if (!StringUtils.hasText(text)) {
			throw new IllegalStateException("不能为空。");
		}

		Properties props = new Properties();
		try {
			props.load(new ByteArrayInputStream(text.getBytes("ISO-8859-1")));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Failed to parse [" + text + "] into Properties: " + e.getMessage());
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to parse [" + text + "] into Properties: " + e.getMessage());
		}

		this.setValue(props);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setValue(Object value) {
		if (!(value instanceof Properties) && (value instanceof Map)) {
			Map map = (Map) value;
			Properties props = new Properties();
			props.putAll(map);
			super.setValue(props);
		} else {
			super.setValue(value);
		}
	}
}
