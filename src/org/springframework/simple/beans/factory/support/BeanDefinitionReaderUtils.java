package org.springframework.simple.beans.factory.support;

import org.springframework.simple.beans.factory.BeanDefinitionStoreException;
import org.springframework.simple.beans.factory.config.BeanDefinitionHolder;
import org.springframework.util.ClassUtils;

public class BeanDefinitionReaderUtils {
	
	public static final String GENERATED_BEAN_NAME_SEPARATOR = "#";
	
	public static AbstractBeanDefinition CreateBeanDefinition(String parent, String className, ClassLoader classloader)
			throws ClassNotFoundException {
		AbstractBeanDefinition bd = null;

		if (parent != null) {
			bd = new ChildBeanDefinition(parent);
		} else {
			bd = new RootBeanDefinition();
		}

		if (className != null) {
			if (classloader != null) {
				bd.setBeanClass(ClassUtils.forName(className, classloader));
			} else {
				bd.setBeanClassName(className);
			}
		}
		return bd;
	}

	public static void registerBeanDefinition(BeanDefinitionHolder holder, BeanDefinitionRegistry beanFactory) {

		String beanName = holder.getBeanName();
		beanFactory.registerBeanDefinition(beanName, holder.getBeanDefinition());
		String[] alias = holder.getAlias();
		if (alias != null) {
			for (int i = 0; i < alias.length; i++) {
				beanFactory.registerAlias(beanName, alias[i]);
			}
		}

	}

	public static String generateBeanName(AbstractBeanDefinition beanDefinition, BeanDefinitionRegistry beanFactory,
			boolean isInnerBean) throws BeanDefinitionStoreException {

		return "";
	}
}
