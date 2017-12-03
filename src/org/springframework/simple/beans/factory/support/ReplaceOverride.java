package org.springframework.simple.beans.factory.support;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.springframework.util.ObjectUtils;

public class ReplaceOverride extends MethodOverride {
	private final String methodReplacerBeanName;

	/**
	 * List of String. Identifying signatures.
	 */
	private List typeIdentifiers = new LinkedList();

	/**
	 * Construct a new ReplaceOverride.
	 * 
	 * @param methodName
	 *            the name of the method to override
	 * @param methodReplacerBeanName
	 *            the bean name of the MethodReplacer
	 */
	public ReplaceOverride(String methodName, String methodReplacerBeanName) {
		super(methodName);
		this.methodReplacerBeanName = methodReplacerBeanName;
	}

	/**
	 * Return the name of the bean implementing MethodReplacer.
	 */
	public String getMethodReplacerBeanName() {
		return methodReplacerBeanName;
	}

	/**
	 * Add a fragment of a class string, like "Exception" or "java.lang.Exc", to
	 * identify a parameter type
	 * 
	 * @param s
	 *            substring of class FQN
	 */
	public void addTypeIdentifier(String s) {
		this.typeIdentifiers.add(s);
	}

	public boolean matches(Method method) {
		// TODO could cache result for efficiency
		if (!method.getName().equals(getMethodName())) {
			// It can't match.
			return false;
		}

		if (!isOverloaded()) {
			// No overloaded: don't worry about arg type matching.
			return true;
		}

		// If we get to here, we need to insist on precise argument matching.
		if (this.typeIdentifiers.size() != method.getParameterTypes().length) {
			return false;
		}
		for (int i = 0; i < this.typeIdentifiers.size(); i++) {
			String identifier = (String) this.typeIdentifiers.get(i);
			if (method.getParameterTypes()[i].getName().indexOf(identifier) == -1) {
				// This parameter cannot match.
				return false;
			}
		}
		return true;
	}

	public boolean equals(Object o) {
		if (!super.equals(o)) {
			return false;
		}

		ReplaceOverride that = (ReplaceOverride) o;
		if (!ObjectUtils.nullSafeEquals(this.methodReplacerBeanName, that.methodReplacerBeanName))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.typeIdentifiers, that.typeIdentifiers))
			return false;

		return true;
	}

	public int hashCode() {
		int result = super.hashCode();
		result = 29 * result + ObjectUtils.nullSafeHashCode(this.methodReplacerBeanName);
		result = 29 * result + ObjectUtils.nullSafeHashCode(this.typeIdentifiers);
		return result;
	}

	public String toString() {
		return "Replace override for method '" + getMethodName() + "; will call bean '" + this.methodReplacerBeanName
				+ "'";
	}
}
