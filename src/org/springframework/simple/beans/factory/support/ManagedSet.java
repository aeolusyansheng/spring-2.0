package org.springframework.simple.beans.factory.support;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.springframework.core.CollectionFactory;
import org.springframework.simple.beans.BeanMetadataElement;
import org.springframework.simple.beans.Mergeable;
import org.springframework.util.Assert;


@SuppressWarnings("rawtypes")
public class ManagedSet implements Set, Mergeable, BeanMetadataElement{
	private final Set targetSet;

	private boolean mergeEnabled;

	private Object source;


	public ManagedSet() {
		this(16);
	}

	public ManagedSet(int initialCapacity) {
		this.targetSet = CollectionFactory.createLinkedSetIfPossible(initialCapacity);
	}

	public ManagedSet(Set targetSet) {
		this.targetSet = targetSet;
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
		if (parent instanceof Set) {
			Set otherSet = (Set) parent;
			Set temp = new ManagedSet();
			temp.addAll(otherSet);
			temp.addAll(this);
			return temp;
		}
		throw new IllegalArgumentException("Cannot merge object with object of type [" + parent.getClass() + "]");
	}


	public int size() {
		return this.targetSet.size();
	}

	public boolean isEmpty() {
		return this.targetSet.isEmpty();
	}

	public boolean contains(Object obj) {
		return this.targetSet.contains(obj);
	}

	public Iterator iterator() {
		return this.targetSet.iterator();
	}

	public Object[] toArray() {
		return this.targetSet.toArray();
	}

	@SuppressWarnings("unchecked")
	public Object[] toArray(Object[] arr) {
		return this.targetSet.toArray(arr);
	}

	@SuppressWarnings("unchecked")
	public boolean add(Object obj) {
		return this.targetSet.add(obj);
	}

	public boolean remove(Object obj) {
		return this.targetSet.remove(obj);
	}

	@SuppressWarnings("unchecked")
	public boolean containsAll(Collection coll) {
		return this.targetSet.containsAll(coll);
	}

	@SuppressWarnings("unchecked")
	public boolean addAll(Collection coll) {
		return this.targetSet.addAll(coll);
	}

	@SuppressWarnings("unchecked")
	public boolean retainAll(Collection coll) {
		return this.targetSet.retainAll(coll);
	}

	@SuppressWarnings("unchecked")
	public boolean removeAll(Collection coll) {
		return this.targetSet.removeAll(coll);
	}

	public void clear() {
		this.targetSet.clear();
	}

	public int hashCode() {
		return this.targetSet.hashCode();
	}

	public boolean equals(Object obj) {
		return this.targetSet.equals(obj);
	}

	public String toString() {
		return this.targetSet.toString();
	}
}
