package org.springframework.simple.beans.factory.parsing;

import org.springframework.simple.beans.BeanMetadataElement;
import org.springframework.simple.beans.factory.config.BeanDefinition;
import org.springframework.simple.beans.factory.config.BeanReference;

public interface ComponentDefinition extends BeanMetadataElement {

	String getName();

	String getDescription();

	BeanDefinition[] getBeanDefinitions();

	BeanDefinition[] getInnerBeanDefinitions();

	BeanReference[] getBeanReferences();
}
