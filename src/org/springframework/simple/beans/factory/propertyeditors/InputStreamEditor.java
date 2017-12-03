package org.springframework.simple.beans.factory.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.util.Assert;

public class InputStreamEditor extends PropertyEditorSupport {

	private final ResourceEditor resourceEditor;

	public InputStreamEditor() {
		this.resourceEditor = new ResourceEditor();
	}

	public InputStreamEditor(ResourceEditor resourceEditor) {
		Assert.notNull(resourceEditor, "resourceEditor不能为空。");
		this.resourceEditor = new ResourceEditor();
	}

	public void setAsText(String text) throws IllegalStateException {
		this.resourceEditor.setAsText(text);
		Resource resource = (Resource) this.getValue();
		try {
			setValue(resource != null ? resource.getInputStream() : null);
		} catch (IOException e) {
			throw new IllegalStateException("处理失败。");
		}
	}

	public String getAsText() {
		return null;
	}
}
