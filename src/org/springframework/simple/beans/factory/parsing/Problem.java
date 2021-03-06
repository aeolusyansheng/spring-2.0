package org.springframework.simple.beans.factory.parsing;

import org.springframework.util.Assert;

public class Problem {
	private final String message;

	private final Location location;

	private final ParseState parseState;

	private final Throwable rootCause;


	
	public Problem(String message, Location location) {
		this(message, location, null, null);
	}

	
	public Problem(String message, Location location, ParseState parseState) {
		this(message, location, parseState, null);
	}

	
	public Problem(String message, Location location, ParseState parseState, Throwable rootCause) {
		Assert.notNull(message, "Message must not be null");
		Assert.notNull(location, "Location must not be null");
		this.message = message;
		this.location = location;
		this.parseState = parseState;
		this.rootCause = rootCause;
	}


	/**
	 * Get the message detailing the problem.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Get the location within a bean configuration source that triggered the error.
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Get the description of the bean configuration source that triggered the error,
	 * as contained within this Problem's Location object.
	 * @see #getLocation()
	 */
	public String getResourceDescription() {
		return getLocation().getResource().getDescription();
	}

	/**
	 * Get the {@link ParseState} at the time of the error (may be <code>null</code>).
	 */
	public ParseState getParseState() {
		return this.parseState;
	}

	/**
	 * Get the underlying expection that caused the error (may be <code>null</code>).
	 */
	public Throwable getRootCause() {
		return rootCause;
	}


	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Configuration problem: ");
		sb.append(getMessage());
		sb.append("\nOffending resource: ").append(getResourceDescription());
		if (getParseState() != null) {
			sb.append('\n').append(getParseState());
		}
		return sb.toString();
	}
}
