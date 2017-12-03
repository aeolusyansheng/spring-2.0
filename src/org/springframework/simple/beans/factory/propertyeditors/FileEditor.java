package org.springframework.simple.beans.factory.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

public class FileEditor extends PropertyEditorSupport {

	private final ResourceEditor resourceEditor;

	public FileEditor() {
		this.resourceEditor = new ResourceEditor();
	}

	public FileEditor(ResourceEditor resourceEditor) {
		Assert.notNull(resourceEditor, "resourceEditor不能为空。");
		this.resourceEditor = resourceEditor;
	}

	public void setAsText(String text) throws IllegalArgumentException {
		// 如果不是classpath开头，就用File
		if (StringUtils.hasText(text) && !ResourceUtils.isUrl(text)) {
			File f = new File(text);
			// 只处理绝对路径的文件
			if (f.isAbsolute()) {
				setValue(f);
				return;
			}
		}

		// 如果是classpath，利用spring的资源解析
		this.resourceEditor.setAsText(text);
		Resource resource = (Resource) this.resourceEditor.getValue();
		try {
			setValue(resource != null ? resource.getFile() : null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new IllegalArgumentException("无法处理文件 " + resource + ": " + e.getMessage());
		}
	}

	public String getAsText() {
		File f = (File) getValue();
		if (f == null) {
			return "";
		} else {
			return f.getPath();
		}
	}
}
