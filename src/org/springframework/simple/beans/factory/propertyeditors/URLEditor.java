package org.springframework.simple.beans.factory.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.net.URL;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;

public class URLEditor extends PropertyEditorSupport {

	private final ResourceEditor resourceEditor;

	public URLEditor() {
		this.resourceEditor = new ResourceEditor();
	}

	public URLEditor(ResourceEditor resourceEditor) {
		this.resourceEditor = resourceEditor;
	}

	public void setAsText(String text) {
		this.resourceEditor.setAsText(text);
		Resource resource = (Resource) this.resourceEditor.getValue();
		try {
			setValue(resource != null ? resource.getURL() : null);
		} catch (IOException e) {
			throw new IllegalStateException("解析资源失败。");
		}
	}

	public String getAsText() {
		URL url = (URL) getValue();
		return (url != null ? url.toExternalForm() : "");
	}
}
