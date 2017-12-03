package org.springframework.simple.beans.factory.config;

import org.springframework.simple.beans.BeanMetadataElement;

public interface BeanReference extends BeanMetadataElement {
	String getBeanName();
}
