package org.springframework.simple.beans.factory.parsing;

import java.util.Stack;

public final class ParseState {

	private static final char TAB = '\t';

	@SuppressWarnings("rawtypes")
	private final Stack state;

	@SuppressWarnings("rawtypes")
	public ParseState() {
		this.state = new Stack();
	}

	@SuppressWarnings("rawtypes")
	public ParseState(ParseState other) {
		this.state = (Stack) other.state.clone();
	}

	public void pop() {
		this.state.pop();
	}

	@SuppressWarnings("unchecked")
	public void push(Entry entry) {
		this.state.push(entry);
	}

	public Entry peek() {
		return (this.state.empty() ? null : (Entry) this.state.peek());
	}

	public ParseState snapshot() {
		return new ParseState(this);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int x = 0; x < this.state.size(); x++) {
			if (x > 0) {
				sb.append('\n');
				for (int y = 0; y < x; y++) {
					sb.append(TAB);
				}
				sb.append("-> ");
			}
			sb.append(this.state.get(x));
		}
		return sb.toString();
	}

	// 内部接口
	public interface Entry {

	}
}
