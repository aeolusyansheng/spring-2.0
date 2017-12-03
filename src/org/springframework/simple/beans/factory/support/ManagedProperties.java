package org.springframework.simple.beans.factory.support;

import java.util.Properties;

import org.springframework.simple.beans.BeanMetadataElement;
import org.springframework.simple.beans.Mergeable;
import org.springframework.util.Assert;


@SuppressWarnings("serial")
public class ManagedProperties extends Properties implements Mergeable, BeanMetadataElement{
	private boolean mergeEnabled;

	private Object source;


	public void setMergeEnabled(boolean mergeEnabled) {
		this.mergeEnabled = mergeEnabled;
	}

	public boolean isMergeEnabled() {
		return mergeEnabled;
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


	public synchronized Object merge(Object parent) {
		if (!this.mergeEnabled) {
			throw new IllegalStateException("Cannot merge when the mergeEnabled property is false");
		}
		Assert.notNull(parent);
		if (parent instanceof Properties) {
			Properties temp = new ManagedProperties();
			temp.putAll((Properties) parent);
			temp.putAll(this);
			return temp;
		}
		throw new IllegalArgumentException("Cannot merge object with object of type [" + parent.getClass() + "]");
	}

}
