package org.springframework.simple.context;

public interface HierarchialMessageSource extends MessageSource {

	MessageSource getParentMessageSource();

	void setParentMessageSource(MessageSource messageSource);
}
