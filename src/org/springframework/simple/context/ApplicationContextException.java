package org.springframework.simple.context;

import org.springframework.simple.beans.FatalBeanException;

@SuppressWarnings("serial")
public class ApplicationContextException extends FatalBeanException {
	public ApplicationContextException(String msg) {
		super(msg);
	}

	public ApplicationContextException(String msg, Throwable ex) {
		super(msg, ex);
	}
}
