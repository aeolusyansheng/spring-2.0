package org.springframework.simple.beans.factory.xml;

import java.util.HashMap;
import java.util.Map;

import org.springframework.simple.beans.factory.config.BeanDefinition;
import org.springframework.simple.beans.factory.config.BeanDefinitionHolder;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class NamespaceHandlerSupport implements NamespaceHandler {

	@SuppressWarnings("rawtypes")
	private final Map parser = new HashMap();
	@SuppressWarnings("rawtypes")
	private final Map decorator = new HashMap();
	@SuppressWarnings("rawtypes")
	private final Map attributeDecorator = new HashMap();

	@SuppressWarnings("unchecked")
	protected void registerBeanDefinitionParser(String elementName, BeanDefinitionParser parser) {
		this.parser.put(elementName, parser);
	}

	@SuppressWarnings("unchecked")
	protected void registerBeanDefinitionDecorator(String elementName, BeanDefinitionDecorator decorator) {
		this.decorator.put(elementName, decorator);
	}

	@SuppressWarnings("unchecked")
	protected void registerBeanDefinitionDecoratorForAttribute(String attributeName,
			BeanDefinitionDecorator decorator) {
		this.attributeDecorator.put(attributeName, decorator);
	}

	protected BeanDefinitionParser findBeanDefinitionParser(Element el) {
		BeanDefinitionParser parser = (BeanDefinitionParser) this.parser.get(el.getLocalName());
		if (parser == null) {
			throw new IllegalArgumentException("找不到元素 [" + el.getLocalName() + "]的解析器.");
		}
		return parser;

	}

	protected BeanDefinitionDecorator findBeanDefinitionDecorator(Node node) {
		BeanDefinitionDecorator decorator;
		if (node instanceof Element) {
			decorator = (BeanDefinitionDecorator) this.decorator.get(node.getLocalName());
		} else if (node instanceof Attr) {
			decorator = (BeanDefinitionDecorator) this.attributeDecorator.get(node.getLocalName());
		} else {
			throw new IllegalArgumentException(
					"Cannot decorate based on Nodes of type '" + node.getClass().getName() + "'");
		}

		if (decorator == null) {
			throw new IllegalArgumentException("找不到元素 " + (node instanceof Element ? "element" : "attribute") + " ["
					+ node.getLocalName() + "]的装饰器.");
		}

		return decorator;
	}

	@Override
	public BeanDefinition parse(Element el, ParserContext parserContext) {
		return findBeanDefinitionParser(el).parse(el, parserContext);
	}

	@Override
	public BeanDefinitionHolder decorate(Node el, BeanDefinitionHolder definition, ParserContext parserContext) {
		return findBeanDefinitionDecorator(el).decorate(el, definition, parserContext);
	}

}
