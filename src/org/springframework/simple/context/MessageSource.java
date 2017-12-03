package org.springframework.simple.context;

import java.util.Locale;

public interface MessageSource {

	String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

	String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException;

	String getMessage(MessgaeSourceResolvable messgaeSourceResolvable, Locale locale) throws NoSuchMessageException;
}
