package org.springframework.simple.beans.factory.xml;

import org.springframework.simple.beans.factory.support.AbstractBeanDefinition;
import org.springframework.simple.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.Assert;
import org.w3c.dom.Element;

public abstract class AbstractSingleBeanDefinitionParser extends AbstractBeanDefinitionParser {

	@SuppressWarnings("rawtypes")
	protected final AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {

		Class beanClass = getBeanClass(element);
		Assert.state(beanClass!=null,"bean class 不能为空。");
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(beanClass);
		builder.setSource(parserContext.extractSource(element));
		if(parserContext.isNested()) {
			builder.setSingleton(parserContext.getContainingBeanDefinition().isSingleton());
		}
		doParse(element,builder);
		return builder.getBeanDefinition();
	}
	
	@SuppressWarnings("rawtypes")
	protected abstract Class getBeanClass(Element element);
		
	protected void doParse(Element el,BeanDefinitionBuilder builder) {
		// 空方法，子类实现。
	}
}
