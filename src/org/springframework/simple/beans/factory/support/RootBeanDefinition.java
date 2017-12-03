package org.springframework.simple.beans.factory.support;

import org.springframework.simple.beans.MutablePropertyValues;
import org.springframework.simple.beans.factory.config.ConstructorArgumentValues;

public class RootBeanDefinition extends AbstractBeanDefinition {

	/**
	 * Create a new RootBeanDefinition, to be configured through its bean properties
	 * and configuration methods.
	 * 
	 * @see #setBeanClass
	 * @see #setBeanClassName
	 * @see #setSingleton
	 * @see #setAutowireMode
	 * @see #setDependencyCheck
	 * @see #setConstructorArgumentValues
	 * @see #setPropertyValues
	 */
	public RootBeanDefinition() {
		super();
	}

	/**
	 * Create a new RootBeanDefinition for a singleton.
	 * 
	 * @param beanClass
	 *            the class of the bean to instantiate
	 */
	public RootBeanDefinition(Class beanClass) {
		super();
		setBeanClass(beanClass);
	}

	/**
	 * Create a new RootBeanDefinition with the given singleton status.
	 * 
	 * @param beanClass
	 *            the class of the bean to instantiate
	 * @param singleton
	 *            the singleton status of the bean
	 */
	public RootBeanDefinition(Class beanClass, boolean singleton) {
		super();
		setBeanClass(beanClass);
		setSingleton(singleton);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton, using the given autowire
	 * mode.
	 * 
	 * @param beanClass
	 *            the class of the bean to instantiate
	 * @param autowireMode
	 *            by name or type, using the constants in this interface
	 */
	public RootBeanDefinition(Class beanClass, int autowireMode) {
		super();
		setBeanClass(beanClass);
		setAutowireMode(autowireMode);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton, using the given autowire
	 * mode.
	 * 
	 * @param beanClass
	 *            the class of the bean to instantiate
	 * @param autowireMode
	 *            by name or type, using the constants in this interface
	 * @param dependencyCheck
	 *            whether to perform a dependency check for objects (not applicable
	 *            to autowiring a constructor, thus ignored there)
	 */
	public RootBeanDefinition(Class beanClass, int autowireMode, boolean dependencyCheck) {
		super();
		setBeanClass(beanClass);
		setAutowireMode(autowireMode);
		if (dependencyCheck && getResolvedAutowireMode() != AUTOWIRE_CONSTRUCTOR) {
			setDependencyCheck(RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
		}
	}

	/**
	 * Create a new RootBeanDefinition for a singleton, providing property values.
	 * 
	 * @param beanClass
	 *            the class of the bean to instantiate
	 * @param pvs
	 *            the property values to apply
	 */
	public RootBeanDefinition(Class beanClass, MutablePropertyValues pvs) {
		super(null, pvs);
		setBeanClass(beanClass);
	}

	/**
	 * Create a new RootBeanDefinition with the given singleton status, providing
	 * property values.
	 * 
	 * @param beanClass
	 *            the class of the bean to instantiate
	 * @param pvs
	 *            the property values to apply
	 * @param singleton
	 *            the singleton status of the bean
	 */
	public RootBeanDefinition(Class beanClass, MutablePropertyValues pvs, boolean singleton) {
		super(null, pvs);
		setBeanClass(beanClass);
		setSingleton(singleton);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton, providing constructor
	 * arguments and property values.
	 * 
	 * @param beanClass
	 *            the class of the bean to instantiate
	 * @param cargs
	 *            the constructor argument values to apply
	 * @param pvs
	 *            the property values to apply
	 */
	public RootBeanDefinition(Class beanClass, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		setBeanClass(beanClass);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton, providing constructor
	 * arguments and property values.
	 * <p>
	 * Takes a bean class name to avoid eager loading of the bean class.
	 * 
	 * @param beanClassName
	 *            the name of the class to instantiate
	 * @param cargs
	 *            the constructor argument values to apply
	 * @param pvs
	 *            the property values to apply
	 */
	public RootBeanDefinition(String beanClassName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		setBeanClassName(beanClassName);
	}

	/**
	 * Create a new RootBeanDefinition as deep copy of the given bean definition.
	 * 
	 * @param original
	 *            the original bean definition to copy from
	 */
	public RootBeanDefinition(RootBeanDefinition original) {
		super(original);
	}

	public boolean equals(Object other) {
		return (other instanceof RootBeanDefinition && super.equals(other));
	}

	public String toString() {
		return "Root bean: " + super.toString();
	}

}
