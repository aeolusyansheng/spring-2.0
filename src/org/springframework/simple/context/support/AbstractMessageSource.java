package org.springframework.simple.context.support;

import java.util.Locale;

import org.springframework.simple.context.HierarchialMessageSource;
import org.springframework.simple.context.MessageSource;
import org.springframework.simple.context.MessgaeSourceResolvable;
import org.springframework.simple.context.NoSuchMessageException;

public abstract class AbstractMessageSource implements HierarchialMessageSource {

	@Override
	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMessage(MessgaeSourceResolvable messgaeSourceResolvable, Locale locale)
			throws NoSuchMessageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageSource getParentMessageSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParentMessageSource(MessageSource messageSource) {
		// TODO Auto-generated method stub
		
	}

}
