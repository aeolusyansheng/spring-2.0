package org.springframework.simple.context;

public interface ApplicationEventPublisher {

	void publishEvent(ApplicationEvent event);
}
