package org.springframework.simple.beans.factory.parsing;

import org.springframework.simple.beans.factory.BeanDefinitionStoreException;

@SuppressWarnings("serial")
public class BeanDefinitionParsingException extends BeanDefinitionStoreException {

	public BeanDefinitionParsingException(Problem problem) {
		super(problem.getResourceDescription(),problem.toString(),problem.getRootCause());
	}

}
