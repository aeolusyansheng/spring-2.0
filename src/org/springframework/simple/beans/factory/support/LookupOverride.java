package org.springframework.simple.beans.factory.support;

import java.lang.reflect.Method;

import org.springframework.util.ObjectUtils;

public class LookupOverride extends MethodOverride  {
	private final String beanName;


	/**
	 * Construct a new LookupOverride.
	 * @param methodName the name of the method to override. This
	 * method must have no arguments.
	 * @param beanName name of the bean in the current BeanFactory
	 * or ApplicationContext that the overriden method should return
	 */
	public LookupOverride(String methodName, String beanName) {
		super(methodName);
		this.beanName = beanName;
	}

	/**
	 * Return the name of the bean that should be returned
	 * by this method.
	 */
	public String getBeanName() {
		return beanName;
	}


	/**
	 * Match method of the given name, with no parameters.
	 */
	public boolean matches(Method method) {
		return (method.getName().equals(getMethodName()) && method.getParameterTypes().length == 0);
	}


	public String toString() {
		return "LookupOverride for method '" + getMethodName() + "'; will return bean '" + beanName + "'";
	}

	public boolean equals(Object o) {
		if(!super.equals(o)) {
			return false;
		}
		return ObjectUtils.nullSafeEquals(this.beanName, ((LookupOverride)o).beanName);
	}

	public int hashCode() {
		return 29 * super.hashCode() + ObjectUtils.nullSafeHashCode(this.beanName);
	}
}
