package org.springframework.simple.beans.support;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourceArrayPropertyEditor;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.simple.beans.PropertyEditorRegistrar;
import org.springframework.simple.beans.PropertyEditorRegistry;
import org.springframework.simple.beans.factory.propertyeditors.ClassEditor;
import org.springframework.simple.beans.factory.propertyeditors.FileEditor;
import org.springframework.simple.beans.factory.propertyeditors.InputStreamEditor;
import org.springframework.simple.beans.factory.propertyeditors.URLEditor;

//属性编辑注册器管理
public class ResourceEditorRegistrar implements PropertyEditorRegistrar {

	private final ResourceLoader resourceLoader;

	public ResourceEditorRegistrar(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void registerCustomEdiotrs(PropertyEditorRegistry registry) {
		// 注册spring自定义属性编辑器
		ResourceEditor baseEditor = new ResourceEditor(this.resourceLoader);
		registry.registerCustomEditor(Resource.class, baseEditor);
		registry.registerCustomEditor(URL.class, new URLEditor(baseEditor));
		registry.registerCustomEditor(File.class, new FileEditor(baseEditor));
		registry.registerCustomEditor(InputStream.class, new InputStreamEditor(baseEditor));

		if (this.resourceLoader instanceof DefaultResourceLoader) {
			ClassLoader classloader = ((DefaultResourceLoader) this.resourceLoader).getClassLoader();
			registry.registerCustomEditor(Class.class, new ClassEditor(classloader));
		}

		if (this.resourceLoader instanceof ResourcePatternResolver) {
			registry.registerCustomEditor(Resource[].class,
					new ResourceArrayPropertyEditor((ResourcePatternResolver) this.resourceLoader));
		}
	}

}
