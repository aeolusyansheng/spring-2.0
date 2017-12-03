package org.springframework.simple.context;

public interface MessgaeSourceResolvable {

	Object[] getArguments();

	String[] getCodes();

	String getDefaultMessage();
}
