package org.springframework.simple.beans.factory.parsing;

import org.springframework.simple.beans.BeanMetadataElement;
import org.springframework.util.Assert;

public class ImportDefiniton implements BeanMetadataElement {

	private final String importedResource;
	private final Object source;

	public ImportDefiniton(String importedResource) {
		this(importedResource, null);
	}

	public ImportDefiniton(String importedResource, Object source) {
		Assert.hasText(importedResource, "importedResource不能为空。");
		this.importedResource = importedResource;
		this.source = source;
	}

	@Override
	public Object getSource() {
		// TODO Auto-generated method stub
		return this.source;
	}

	public String getimportedResource() {
		return this.importedResource;
	}

}
