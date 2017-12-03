package org.springframework.simple.beans;

public interface Mergeable {

	boolean isMergeEnabled();

	Object merge(Object parent);
}
