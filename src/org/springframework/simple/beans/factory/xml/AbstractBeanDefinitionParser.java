package org.springframework.simple.beans.factory.xml;

import org.springframework.simple.beans.factory.config.BeanDefinition;
import org.springframework.simple.beans.factory.config.BeanDefinitionHolder;
import org.springframework.simple.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.simple.beans.factory.support.AbstractBeanDefinition;
import org.springframework.simple.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.simple.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

public abstract class AbstractBeanDefinitionParser implements BeanDefinitionParser {

	private static final String ID_ATTRIBUTE = "id";

	@Override
	public BeanDefinition parse(Element el, ParserContext parserContext) {
		// 内部解析标签
		AbstractBeanDefinition beanDefinition = parseInternal(el, parserContext);
		// 编辑id属性
		String id = resolveId(el, beanDefinition, parserContext);
		// id为空时返回异常
		if (!StringUtils.hasText(id) && !parserContext.isNested()) {
			throw new IllegalStateException("标签" + el.getLocalName() + "需要一个id属性");
		}
		BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, id);
		// 注册bean到beanFactory
		registerBeanDefinition(holder, parserContext.getRegistry(), parserContext.isNested());
		// 触发相关事件
		if (shouldFireEvents()) {
			BeanComponentDefinition componentDefinition = new BeanComponentDefinition(holder);
			postProcessComponentDefiniton(componentDefinition);
			parserContext.getReaderContext().fireComponentRegistered(componentDefinition);
		}

		return beanDefinition;

	}

	protected boolean shouldFireEvents() {
		return true;
	}

	protected void registerBeanDefinition(BeanDefinitionHolder holder, BeanDefinitionRegistry registry,
			boolean isNested) {
		if (!isNested) {
			BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
		}
	}

	// 子类实现
	protected abstract AbstractBeanDefinition parseInternal(Element el, ParserContext parserContext);

	protected String resolveId(Element el, AbstractBeanDefinition definition, ParserContext parserContext) {
		if (shouldGenerateId()) {
			return BeanDefinitionReaderUtils.generateBeanName(definition, parserContext.getRegistry(),
					parserContext.isNested());
		} else {
			return el.getAttribute(ID_ATTRIBUTE);
		}
	}

	protected boolean shouldGenerateId() {
		return false;
	}

	protected void postProcessComponentDefiniton(BeanComponentDefinition componentDefinition) {

	}

}
