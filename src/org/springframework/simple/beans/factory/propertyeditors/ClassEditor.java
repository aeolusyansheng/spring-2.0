package org.springframework.simple.beans.factory.propertyeditors;

import java.beans.PropertyEditorSupport;

import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public class ClassEditor extends PropertyEditorSupport {

	private final ClassLoader classloader;

	public ClassEditor() {
		this(null);
	}

	public ClassEditor(ClassLoader classloader) {
		this.classloader = (classloader != null ? classloader : ClassUtils.getDefaultClassLoader());
	}

	@SuppressWarnings("rawtypes")
	public void setAsText(String text) {
		if (StringUtils.hasText(text)) {
			Class clazz = ClassUtils.resolveClassName(text.trim(), this.classloader);
			setValue(clazz);
		} else {
			setValue(null);
		}
	}

	@SuppressWarnings("rawtypes")
	public String getAsText() {
		Class clazz = (Class) getValue();
		if (clazz == null) {
			return "";
		}
		return ClassUtils.getQualifiedName(clazz);
	}
}
