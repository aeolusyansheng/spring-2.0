package org.springframework.simple.beans.factory.parsing;

import org.springframework.util.StringUtils;

public class PropertyEntry implements ParseState.Entry {

	private String name;

	public PropertyEntry(String name) {

		if (!StringUtils.hasText(name)) {
			throw new IllegalArgumentException("属性名name '" + name + "'非法.");
		}

		this.name = name;
	}

	public String toString() {
		return "Property '" + this.name + "'";
	}
}
