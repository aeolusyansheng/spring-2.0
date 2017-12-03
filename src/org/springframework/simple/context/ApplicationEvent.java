package org.springframework.simple.context;

import java.util.EventObject;

@SuppressWarnings("serial")
public abstract class ApplicationEvent extends EventObject {

	public ApplicationEvent(Object source) {
		super(source);
		// TODO Auto-generated constructor stub
	}

}
