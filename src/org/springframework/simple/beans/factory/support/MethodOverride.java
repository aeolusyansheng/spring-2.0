package org.springframework.simple.beans.factory.support;

import java.lang.reflect.Method;

import org.springframework.simple.beans.BeanMetadataElement;
import org.springframework.util.ObjectUtils;

public abstract class MethodOverride implements BeanMetadataElement{
	private final String methodName;

	private boolean overloaded = true;

	private Object source;


	/**
	 * Construct a new override for the given method.
	 * @param methodName the name of the method to override
	 */
	protected MethodOverride(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * Return the name of the method to be overridden.
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * Set whether the overridden method has to be considered as overloaded
	 * (that is, whether arg type matching has to happen).
	 * Default is "true"; can be switched to false to optimize runtime performance.
	 */
	protected void setOverloaded(boolean overloaded) {
		this.overloaded = overloaded;
	}

	/**
	 * Return whether the overridden method has to be considered as overloaded
	 * (that is, whether arg type matching has to happen).
	 */
	protected boolean isOverloaded() {
		return overloaded;
	}

	/**
	 * Set the configuration source <code>Object</code> for this metadata element.
	 * <p>The exact type of the object will depend on the configuration mechanism used.
	 */
	public void setSource(Object source) {
		this.source = source;
	}

	public Object getSource() {
		return source;
	}


	/**
	 * Subclasses must override this to indicate whether they match
	 * the given method. This allows for argument list checking
	 * as well as method name checking.
	 * @param method the method to check
	 * @return whether this override matches the given method
	 */
	public abstract boolean matches(Method method);


	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MethodOverride that = (MethodOverride) o;

		if (overloaded != that.overloaded) return false;
		if (!ObjectUtils.nullSafeEquals(this.methodName, that.methodName)) return false;
		if (!ObjectUtils.nullSafeEquals(this.source, that.source)) return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = ObjectUtils.nullSafeHashCode(this.methodName);
		result = ObjectUtils.nullSafeHashCode(this.source);
		result = 29 * result + (overloaded ? 1 : 0);
		return result;
	}
}
