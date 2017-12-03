package org.springframework.simple.beans.factory.xml;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.simple.beans.factory.BeanDefinitionStoreException;
import org.springframework.simple.beans.factory.config.BeanDefinitionHolder;
import org.springframework.simple.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.simple.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {

	private static final String BEAN_ELEMENT = "bean";
	private static final String ALIAS_ELEMENT = "alias";
	private static final String NAME_ATTRIBUTE = "name";
	private static final String ALIAS_ATTRIBUTE = "alias";
	private static final String IMPORT_ELEMENT = "import";
	private static final String RESOURCE_ATTRIBUTE = "resource";

	protected final Log logger = LogFactory.getLog(getClass());

	private XmlReaderContext readerContext;

	@Override
	public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext)
			throws BeanDefinitionStoreException {
		this.readerContext = readerContext;
		if (logger.isDebugEnabled()) {
			logger.debug("开始解析XML。");
		}
		BeanDefinitionParserDelegate helper = new BeanDefinitionParserDelegate(readerContext);

		// helper 初始化
		Element root = doc.getDocumentElement();
		helper.initDefaults(root);

		preProcessXml(root);
		// 解析XML
		parserBeanDefinitions(root, helper);
		postProcessXml(root);

	}

	protected void parserBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
		if (delegate.isDefaultNamespace(root.getNamespaceURI())) {
			// 根元素为默认名称空间
			NodeList nodes = root.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node instanceof Element) {
					// 只解析标签
					Element el = (Element) node;
					if (delegate.isDefaultNamespace(el.getNamespaceURI())) {
						// 子节点为bean名称空间
						parseDefaultElement(el, delegate);
					} else {
						// 子节点为bean以外（如context等）名称空间
						delegate.parseCustomElement(el);
					}
				}
			}
		} else {
			// 根元素为自定义名称空间
			delegate.parseCustomElement(root);
		}
	}

	private void parseDefaultElement(Element el, BeanDefinitionParserDelegate delegate) {
		// 解析import,alias,bean三种元素
		if (DomUtils.nodeNameEquals(el, IMPORT_ELEMENT)) {
			importBeanDefinitionResource(el);
		} else if (DomUtils.nodeNameEquals(el, ALIAS_ELEMENT)) {
			String name = el.getAttribute(NAME_ATTRIBUTE);
			String alias = el.getAttribute(ALIAS_ATTRIBUTE);
			// 注册别名
			getReaderContext().getRegistry().registerAlias(name, alias);
			getReaderContext().fireAliasRegistered(name, alias, extractSource(el));
		} else if (DomUtils.nodeNameEquals(el, BEAN_ELEMENT)) {
			// 解析
			BeanDefinitionHolder beanHolder = delegate.parseBeanDefinitionElement(el);
			if (beanHolder != null) {
				// 包装
				beanHolder = delegate.decoratorBeanDefinitionIfRequired(el, beanHolder);
				// 注册
				BeanDefinitionReaderUtils.registerBeanDefinition(beanHolder, getReaderContext().getRegistry());
				getReaderContext().fireComponentRegistered(new BeanComponentDefinition(beanHolder));
			}
		}
	}

	protected void importBeanDefinitionResource(Element el) {
		String location = el.getAttribute(RESOURCE_ATTRIBUTE);
		location = SystemPropertyUtils.resolvePlaceholders(location);
		if (ResourcePatternUtils.isUrl(location)) {
			int loadCount = getReaderContext().getBeanDefinitionReader().loadBeanDefinitions(location);
			if (logger.isDebugEnabled()) {
				logger.debug("从" + location + "文件里读取了" + loadCount + "个Bean定义.");
			}
		} else {
			// 从当前资源的相对路劲查找
			Resource resource = getReaderContext().getResource();
			try {
				Resource relativeResource = resource.createRelative(location);
				int loadCount = getReaderContext().getBeanDefinitionReader().loadBeanDefinitions(relativeResource);
				if (logger.isDebugEnabled()) {
					logger.debug("从" + relativeResource + "文件里读取了" + loadCount + "个Bean定义.");
				}
			} catch (IOException e) {
				getReaderContext().error("从" + location + "文件里读取Bean失败。", el);
			}
		}

		getReaderContext().fireImportProcessed(location, extractSource(el));
	}

	public Object extractSource(Element el) {
		return getReaderContext().extractSource(el);
	}

	protected void preProcessXml(Element root) {

	}

	protected void postProcessXml(Element root) {

	}

	public XmlReaderContext getReaderContext() {
		return this.readerContext;
	}

}
