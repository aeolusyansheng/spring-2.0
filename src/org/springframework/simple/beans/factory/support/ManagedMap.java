package org.springframework.simple.beans.factory.support;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.springframework.core.CollectionFactory;
import org.springframework.simple.beans.BeanMetadataElement;
import org.springframework.simple.beans.Mergeable;
import org.springframework.util.Assert;

@SuppressWarnings("rawtypes")
public class ManagedMap implements Map, Mergeable, BeanMetadataElement {
	private final Map targetMap;

	private boolean mergeEnabled;

	private Object source;

	public ManagedMap() {
		this(16);
	}

	public ManagedMap(int initialCapacity) {
		this.targetMap = CollectionFactory.createLinkedMapIfPossible(initialCapacity);
	}

	public ManagedMap(Map targetMap) {
		this.targetMap = targetMap;
	}

	public void setMergeEnabled(boolean mergeEnabled) {
		this.mergeEnabled = mergeEnabled;
	}

	public boolean isMergeEnabled() {
		return mergeEnabled;
	}

	/**
	 * Set the configuration source <code>Object</code> for this metadata element.
	 * <p>
	 * The exact type of the object will depend on the configuration mechanism used.
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
		if (parent instanceof Map) {
			Map parentMap = (Map) parent;
			Map temp = new ManagedMap();
			temp.putAll(parentMap);
			temp.putAll(this);
			return temp;
		}
		throw new IllegalArgumentException("Cannot merge object with object of type [" + parent.getClass() + "]");
	}

	public int size() {
		return this.targetMap.size();
	}

	public boolean isEmpty() {
		return this.targetMap.isEmpty();
	}

	public boolean containsKey(Object key) {
		return this.targetMap.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return this.targetMap.containsValue(value);
	}

	public Object get(Object key) {
		return this.targetMap.get(key);
	}

	@SuppressWarnings("unchecked")
	public Object put(Object key, Object value) {
		return this.targetMap.put(key, value);
	}

	public Object remove(Object key) {
		return this.targetMap.remove(key);
	}

	@SuppressWarnings("unchecked")
	public void putAll(Map t) {
		this.targetMap.putAll(t);
	}

	public void clear() {
		this.targetMap.clear();
	}

	public Set keySet() {
		return this.targetMap.keySet();
	}

	public Collection values() {
		return this.targetMap.values();
	}

	public Set entrySet() {
		return this.targetMap.entrySet();
	}

	public int hashCode() {
		return this.targetMap.hashCode();
	}

	public boolean equals(Object obj) {
		return this.targetMap.equals(obj);
	}

	public String toString() {
		return this.targetMap.toString();
	}
}
