package org.springframework.simple.beans.factory.parsing;

import org.springframework.util.Assert;

public class ConstructorArgumentEntry implements ParseState.Entry {

	private final int index;

	public ConstructorArgumentEntry() {
		this.index = Integer.MIN_VALUE;
	}

	public ConstructorArgumentEntry(int index) {
		Assert.isTrue(index >= 0, "index必须大于等于0.");
		this.index = index;
	}

	public String toString() {
		return "Constructor-arg" + (this.index > Integer.MIN_VALUE ? ": #" + this.index : "");
	}
}
