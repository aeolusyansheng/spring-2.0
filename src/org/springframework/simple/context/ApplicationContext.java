package org.springframework.simple.context;

import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.simple.beans.factory.HierarchicalBeanFactory;
import org.springframework.simple.beans.factory.ListableBeanFactory;
import org.springframework.simple.beans.factory.config.AutowireCapableBeanFactory;

public interface ApplicationContext extends ListableBeanFactory, ApplicationEventPublisher, HierarchicalBeanFactory,
		MessageSource, ResourcePatternResolver {

	String getDisplayName();

	ApplicationContext getParent();

	AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;

	long getStartupDate();

}
