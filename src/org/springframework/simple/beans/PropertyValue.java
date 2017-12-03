package org.springframework.simple.beans;

import java.io.Serializable;

import org.springframework.core.AttributeAccessorSupport;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

@SuppressWarnings("serial")
public class PropertyValue extends AttributeAccessorSupport implements BeanMetadataElement, Serializable {

	private final String name;

	private final Object value;

	private Object source;


	/**
	 * Create a new PropertyValue instance.
	 * @param name name of the property
	 * @param value value of the property (possibly before type conversion)
	 */
	public PropertyValue(String name, Object value) {
		if (name == null) {
			throw new IllegalArgumentException("Property name cannot be null");
		}
		this.name = name;
		this.value = value;
	}

	/**
	 * Copy constructor.
	 * @param source the PropertyValue to copy
	 */
	public PropertyValue(PropertyValue source) {
		Assert.notNull(source, "Source must not be null");
		this.name = source.getName();
		this.value = source.getValue();
		copyAttributesFrom(source);
	}


	/**
	 * Return the name of the property.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the value of the property.
	 * <p>Note that type conversion will <i>not</i> have occurred here.
	 * It is the responsibility of the BeanWrapper implementation to
	 * perform type conversion.
	 */
	public Object getValue() {
		return value;
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


	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PropertyValue)) {
			return false;
		}
		PropertyValue otherPv = (PropertyValue) other;
		return (this.name.equals(otherPv.name) &&
				ObjectUtils.nullSafeEquals(this.value, otherPv.value) &&
				ObjectUtils.nullSafeEquals(this.source, otherPv.source));
	}

	public int hashCode() {
		return this.name.hashCode() * 29 + (this.value == null ? 0 : this.value.hashCode());
	}

	public String toString() {
		return "PropertyValue: name='" + this.name + "', value=[" + this.value + "]";
	}

}
