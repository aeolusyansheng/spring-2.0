package org.springframework.simple.beans.factory.parsing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.simple.beans.PropertyValue;
import org.springframework.simple.beans.PropertyValues;
import org.springframework.simple.beans.factory.config.BeanDefinition;
import org.springframework.simple.beans.factory.config.BeanDefinitionHolder;
import org.springframework.simple.beans.factory.config.BeanReference;
import org.springframework.util.Assert;

public class BeanComponentDefinition extends AbstractComponentDefinition {

	private String beanName;
	private BeanDefinition beanDefinition;
	private BeanDefinition[] innerBeanDefinitions;
	private BeanReference[] beanReferences;
	private String description;

	public BeanComponentDefinition(BeanDefinition beanDefinition, String beanName) {
		Assert.notNull(beanDefinition, "beanDefinition不能为空。");
		Assert.notNull(beanName, "beanName不能为空。");
		this.beanName = beanName;
		this.beanDefinition = beanDefinition;
		this.description = buildDescription(beanDefinition);
		// innerBeanDefinitions,beanReferences做成
		findInnerBeanDefinitionsAndBeanReferences();
	}

	public BeanComponentDefinition(BeanDefinitionHolder beanDefinitionHolder) {
		this(beanDefinitionHolder.getBeanDefinition(), beanDefinitionHolder.getBeanName());
	}

	private String buildDescription(BeanDefinition beanDefinition) {
		StringBuffer sb = new StringBuffer();
		sb.append("Bean '").append(getName()).append("'");
		String beanType = beanDefinition.getBeanClassName();
		if (beanType != null) {
			sb.append(" of type [" + beanType + "]");
		}
		return sb.toString();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void findInnerBeanDefinitionsAndBeanReferences() {
		List innerBeans = new ArrayList();
		List references = new ArrayList();
		PropertyValues propertyValues = this.beanDefinition.getPropertyValues();
		for (int i = 0; i < propertyValues.getPropertyValues().length; i++) {
			PropertyValue propertyValue = propertyValues.getPropertyValues()[i];
			Object value = propertyValue.getValue();
			if (value instanceof BeanDefinitionHolder) {
				innerBeans.add(((BeanDefinitionHolder) value).getBeanDefinition());
			} else if (value instanceof BeanDefinition) {
				innerBeans.add(value);
			} else if (value instanceof BeanReference) {
				references.add(value);
			}
		}
		this.innerBeanDefinitions = (BeanDefinition[]) innerBeans.toArray(new BeanDefinition[innerBeans.size()]);
		this.beanReferences = (BeanReference[]) references.toArray(new BeanReference[references.size()]);
	}

	@Override
	public String getName() {
		return beanName;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public BeanDefinition[] getBeanDefinitions() {
		return new BeanDefinition[] { this.beanDefinition };
	}

	public BeanDefinition[] getInnerBeanDefinitions() {
		return innerBeanDefinitions;
	}

	public BeanReference[] getBeanReferences() {
		return beanReferences;
	}

	@Override
	public Object getSource() {
		return this.beanDefinition.getSource();
	}

}
