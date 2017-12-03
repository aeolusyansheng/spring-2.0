package org.springframework.simple.beans.factory.parsing;

import org.springframework.simple.beans.BeanMetadataElement;
import org.springframework.util.Assert;

public class AliasDefinition implements BeanMetadataElement {

	private final String beanName;
	private final String alias;
	private final Object source;

	public AliasDefinition(String beanName, String alias) {
		this(beanName, alias, null);
	}

	public AliasDefinition(String beanName, String alias, Object source) {
		Assert.hasText(beanName, "beanName不能为空。");
		Assert.hasText(alias, "alias不能为空。");
		this.beanName = beanName;
		this.alias = alias;
		this.source = source;
	}

	@Override
	public Object getSource() {
		return this.source;
	}
	
	public String getBeanName() {
		return this.beanName;
	}
	
	public String getAlias() {
		return this.alias;
	}

}
