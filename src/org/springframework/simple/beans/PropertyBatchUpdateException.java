package org.springframework.simple.beans;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.springframework.util.Assert;

public class PropertyBatchUpdateException extends BeansException{

	/** List of PropertyAccessException objects */
	private PropertyAccessException[] propertyAccessExceptions;


	/**
	 * Create a new PropertyBatchUpdateException.
	 * @param propertyAccessExceptions the List of PropertyAccessExceptions
	 */
	public PropertyBatchUpdateException(PropertyAccessException[] propertyAccessExceptions) {
		super(null);
		Assert.notEmpty(propertyAccessExceptions, "At least 1 PropertyAccessException required");
		this.propertyAccessExceptions = propertyAccessExceptions;
	}


	/**
	 * If this returns 0, no errors were encountered during binding.
	 */
	public final int getExceptionCount() {
		return this.propertyAccessExceptions.length;
	}

	/**
	 * Return an array of the propertyAccessExceptions stored in this object.
	 * Will return the empty array (not null) if there were no errors.
	 */
	public final PropertyAccessException[] getPropertyAccessExceptions() {
		return this.propertyAccessExceptions;
	}

	/**
	 * Return the exception for this field, or <code>null</code> if there isn't one.
	 */
	public PropertyAccessException getPropertyAccessException(String propertyName) {
		for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
			PropertyAccessException pae = this.propertyAccessExceptions[i];
			if (propertyName.equals(pae.getPropertyChangeEvent().getPropertyName())) {
				return pae;
			}
		}
		return null;
	}


	public String getMessage() {
		StringBuffer sb = new StringBuffer("Failed properties: ");
		for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
			sb.append(this.propertyAccessExceptions[i].getMessage());
			if (i < this.propertyAccessExceptions.length - 1) {
				sb.append("; ");
			}
		}
		return sb.toString();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName()).append("; nested PropertyAccessExceptions (");
		sb.append(getExceptionCount()).append(") are:");
		for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
			sb.append('\n').append("PropertyAccessException ").append(i + 1).append(": ");
			sb.append(this.propertyAccessExceptions[i]);
		}
		return sb.toString();
	}

	public void printStackTrace(PrintStream ps) {
		ps.println(getClass().getName() + "; nested PropertyAccessException details (" +
				getExceptionCount() + ") are:");
		for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
			ps.println("PropertyAccessException " + (i + 1) + ":");
			this.propertyAccessExceptions[i].printStackTrace(ps);
		}
	}

	public void printStackTrace(PrintWriter pw) {
		pw.println(getClass().getName() + "; nested PropertyAccessException details (" +
				getExceptionCount() + ") are:");
		for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
			pw.println("PropertyAccessException " + (i + 1) + ":");
			this.propertyAccessExceptions[i].printStackTrace(pw);
		}
	}

	public boolean contains(Class exClass) {
		if (exClass == null) {
			return false;
		}
		if (exClass.isInstance(this)) {
			return true;
		}
		for (int i = 0; i < this.propertyAccessExceptions.length; i++) {
			PropertyAccessException pae = this.propertyAccessExceptions[i];
			if (pae.contains(exClass)) {
				return true;
			}
		}
		return false;
	}
}
