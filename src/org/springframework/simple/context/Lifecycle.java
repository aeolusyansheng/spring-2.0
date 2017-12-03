package org.springframework.simple.context;

public interface Lifecycle {
	
	void start();
	void stop();
	boolean isRunning();
}
