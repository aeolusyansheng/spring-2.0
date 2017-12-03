package org.springframework.simple.beans.factory.propertyeditors;

import java.beans.PropertyEditorSupport;

import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public class ClassArrayEditor extends PropertyEditorSupport {

	private final ClassLoader classloader;

	public ClassArrayEditor() {
		this(null);
	}

	public ClassArrayEditor(ClassLoader classloader) {
		if (classloader == null) {
			this.classloader = ClassUtils.getDefaultClassLoader();
		} else {
			this.classloader = classloader;
		}
	}

	@SuppressWarnings("rawtypes")
	public void setAsText(String text) {
		if (StringUtils.hasText(text)) {
			String[] classNames = StringUtils.commaDelimitedListToStringArray(text);
			Class[] classes = new Class[classNames.length];
			for (int i = 0; i < classNames.length; i++) {
				String className = classNames[i].trim();
				classes[i] = ClassUtils.resolveClassName(className, this.classloader);
			}
			setValue(classes);
		} else {
			setValue(null);
		}
	}

	@SuppressWarnings("rawtypes")
	public String getAsText() {
		Class[] classes = (Class[]) getValue();
		if (classes == null) {
			return "";
		}
		return StringUtils.arrayToCommaDelimitedString(classes);
	}
}
