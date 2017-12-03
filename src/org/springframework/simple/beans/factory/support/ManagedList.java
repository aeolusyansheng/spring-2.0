package org.springframework.simple.beans.factory.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.simple.beans.BeanMetadataElement;
import org.springframework.simple.beans.Mergeable;
import org.springframework.util.Assert;

@SuppressWarnings({ "rawtypes", "serial" })
public class ManagedList extends ArrayList implements Mergeable,BeanMetadataElement {

	private boolean mergeEnabled;

	private Object source;


	public ManagedList() {
	}

	public ManagedList(int initialCapacity) {
		super(initialCapacity);
	}


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
	

	@SuppressWarnings("unchecked")
	public synchronized Object merge(Object parent) {
		if (!this.mergeEnabled) {
			throw new IllegalStateException("Cannot merge when the mergeEnabled property is false");
		}
		Assert.notNull(parent);
		if (parent instanceof List) {
			List temp = new ManagedList();
			temp.addAll((List) parent);
			temp.addAll(this);
			return temp;
		}
		throw new IllegalArgumentException("Cannot merge object with object of type [" + parent.getClass() + "]");
	}

}
