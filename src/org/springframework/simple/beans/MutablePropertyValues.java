package org.springframework.simple.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

@SuppressWarnings("serial")
public class MutablePropertyValues implements PropertyValues,Serializable {

	/** List of PropertyValue objects */
	@SuppressWarnings("rawtypes")
	private final List propertyValueList;


	/**
	 * Creates a new empty MutablePropertyValues object.
	 * Property values can be added with the addPropertyValue methods.
	 * @see #addPropertyValue(PropertyValue)
	 * @see #addPropertyValue(String, Object)
	 */
	public MutablePropertyValues() {
		this.propertyValueList = new ArrayList();
	}

	/**
	 * Deep copy constructor. Guarantees PropertyValue references
	 * are independent, although it can't deep copy objects currently
	 * referenced by individual PropertyValue objects.
	 * @param source the PropertyValues to copy
	 * @see #addPropertyValues(PropertyValues)
	 */
	public MutablePropertyValues(PropertyValues source) {
		// We can optimize this because it's all new:
		// There is no replacement of existing property values.
		if (source != null) {
			PropertyValue[] pvs = source.getPropertyValues();
			this.propertyValueList = new ArrayList(pvs.length);
			for (int i = 0; i < pvs.length; i++) {
				PropertyValue newPv = new PropertyValue(pvs[i]);
				this.propertyValueList.add(newPv);
			}
		}
		else {
			this.propertyValueList = new ArrayList(0);
		}
	}

	/**
	 * Construct a new PropertyValues object from a Map.
	 * @param source Map with property values keyed by property name,
	 * which must be a String
	 * @see #addPropertyValues(Map)
	 */
	public MutablePropertyValues(Map source) {
		// We can optimize this because it's all new:
		// There is no replacement of existing property values.
		if (source != null) {
			this.propertyValueList = new ArrayList(source.size());
			Iterator it = source.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				PropertyValue newPv = new PropertyValue((String) entry.getKey(), entry.getValue());
				this.propertyValueList.add(newPv);
			}
		}
		else {
			this.propertyValueList = new ArrayList(0);
		}
	}


	/**
	 * Copy all given PropertyValues into this object. Guarantees PropertyValue
	 * references are independent, although it can't deep copy objects currently
	 * referenced by individual PropertyValue objects.
	 * @param source the PropertyValues to copy
	 * @return this object to allow creating objects, adding multiple PropertyValues
	 * in a single statement
	 */
	public MutablePropertyValues addPropertyValues(PropertyValues source) {
		if (source != null) {
			PropertyValue[] pvs = source.getPropertyValues();
			for (int i = 0; i < pvs.length; i++) {
				PropertyValue newPv = new PropertyValue(pvs[i].getName(), pvs[i].getValue());
				addPropertyValue(newPv);
			}
		}
		return this;
	}

	/**
	 * Add all property values from the given Map.
	 * @param source Map with property values keyed by property name,
	 * which must be a String
	 * @return this object to allow creating objects, adding multiple
	 * PropertyValues in a single statement
	 */
	public MutablePropertyValues addPropertyValues(Map source) {
		if (source != null) {
			Iterator it = source.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				PropertyValue newPv = new PropertyValue((String) entry.getKey(), entry.getValue());
				addPropertyValue(newPv);
			}
		}
		return this;
	}

	/**
	 * Add a PropertyValue object, replacing any existing one
	 * for the corresponding property.
	 * @param pv PropertyValue object to add
	 * @return this object to allow creating objects, adding multiple
	 * PropertyValues in a single statement
	 */
	public MutablePropertyValues addPropertyValue(PropertyValue pv) {
		for (int i = 0; i < this.propertyValueList.size(); i++) {
			PropertyValue currentPv = (PropertyValue) this.propertyValueList.get(i);
			if (currentPv.getName().equals(pv.getName())) {
				pv = mergeIfRequired(pv, currentPv);
				setPropertyValueAt(pv, i);
				return this;
			}
		}
		this.propertyValueList.add(pv);
		return this;
	}

	/**
	 * Overloaded version of <code>addPropertyValue</code> that takes
	 * a property name and a property value.
	 * @param propertyName name of the property
	 * @param propertyValue value of the property
	 * @see #addPropertyValue(PropertyValue)
	 */
	public void addPropertyValue(String propertyName, Object propertyValue) {
		addPropertyValue(new PropertyValue(propertyName, propertyValue));
	}

	/**
	 * Modify a PropertyValue object held in this object.
	 * Indexed from 0.
	 */
	public void setPropertyValueAt(PropertyValue pv, int i) {
		this.propertyValueList.set(i, pv);
	}

	/**
	 * Merges the value of the supplied 'new' {@link PropertyValue} with that of
	 * the current {@link PropertyValue} if merging is supported and enabled.
	 * @see Mergeable
	 */
	private PropertyValue mergeIfRequired(PropertyValue newPv, PropertyValue currentPv) {
		Object value = newPv.getValue();
		if (value instanceof Mergeable) {
			Mergeable mergeable = (Mergeable) value;
			if (mergeable.isMergeEnabled()) {
				Object merged = mergeable.merge(currentPv.getValue());
				return new PropertyValue(newPv.getName(), merged);
			}
		}
		return newPv;
	}

	/**
	 * Overloaded version of <code>removePropertyValue</code> that takes a property name.
	 * @param propertyName name of the property
	 * @see #removePropertyValue(PropertyValue)
	 */
	public void removePropertyValue(String propertyName) {
		removePropertyValue(getPropertyValue(propertyName));
	}

	/**
	 * Remove the given PropertyValue, if contained.
	 * @param pv the PropertyValue to remove
	 */
	public void removePropertyValue(PropertyValue pv) {
		this.propertyValueList.remove(pv);
	}

	/**
	 * Clear this holder, removing all PropertyValues.
	 */
	public void clear() {
		this.propertyValueList.clear();
	}


	public PropertyValue[] getPropertyValues() {
		return (PropertyValue[])
				this.propertyValueList.toArray(new PropertyValue[this.propertyValueList.size()]);
	}

	public PropertyValue getPropertyValue(String propertyName) {
		for (int i = 0; i < this.propertyValueList.size(); i++) {
			PropertyValue pv = (PropertyValue) propertyValueList.get(i);
			if (pv.getName().equals(propertyName)) {
				return pv;
			}
		}
		return null;
	}

	public boolean contains(String propertyName) {
		return (getPropertyValue(propertyName) != null);
	}

	public boolean isEmpty() {
		return this.propertyValueList.isEmpty();
	}
	
	@Override
	public PropertyValues changesSince(PropertyValues old) {
		MutablePropertyValues changes = new MutablePropertyValues();
		if (old == this) {
			return changes;
		}

		// for each property value in the new set
		for (Iterator it = this.propertyValueList.iterator(); it.hasNext();) {
			PropertyValue newPv = (PropertyValue) it.next();
			// if there wasn't an old one, add it
			PropertyValue pvOld = old.getPropertyValue(newPv.getName());
			if (pvOld == null) {
				changes.addPropertyValue(newPv);
			}
			else if (!pvOld.equals(newPv)) {
				// it's changed
				changes.addPropertyValue(newPv);
			}
		}
		return changes;
	}


	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MutablePropertyValues)) {
			return false;
		}
		MutablePropertyValues that = (MutablePropertyValues) other;
		return this.propertyValueList.equals(that.propertyValueList);
	}

	public int hashCode() {
		return this.propertyValueList.hashCode();
	}

	public String toString() {
		PropertyValue[] pvs = getPropertyValues();
		StringBuffer sb = new StringBuffer("PropertyValues: length=" + pvs.length + "; ");
		sb.append(StringUtils.arrayToDelimitedString(pvs, "; "));
		return sb.toString();
	}


}
