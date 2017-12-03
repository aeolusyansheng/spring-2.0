package org.springframework.simple.beans.factory.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.AttributeAccessor;
import org.springframework.simple.beans.PropertyValue;
import org.springframework.simple.beans.factory.config.BeanDefinition;
import org.springframework.simple.beans.factory.config.BeanDefinitionHolder;
import org.springframework.simple.beans.factory.config.ConstructorArgumentValues;
import org.springframework.simple.beans.factory.config.RuntimeBeanReference;
import org.springframework.simple.beans.factory.config.TypedStringValue;
import org.springframework.simple.beans.factory.parsing.BeanEntry;
import org.springframework.simple.beans.factory.parsing.ConstructorArgumentEntry;
import org.springframework.simple.beans.factory.parsing.ParseState;
import org.springframework.simple.beans.factory.parsing.PropertyEntry;
import org.springframework.simple.beans.factory.support.AbstractBeanDefinition;
import org.springframework.simple.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.simple.beans.factory.support.LookupOverride;
import org.springframework.simple.beans.factory.support.ManagedList;
import org.springframework.simple.beans.factory.support.ManagedMap;
import org.springframework.simple.beans.factory.support.ManagedProperties;
import org.springframework.simple.beans.factory.support.ManagedSet;
import org.springframework.simple.beans.factory.support.MethodOverrides;
import org.springframework.simple.beans.factory.support.ReplaceOverride;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BeanDefinitionParserDelegate {

	// 根节点属性常量
	public static final String DEFAULT_LAZY_INIT_ATTRIBUTE = "default-lazy-init";
	public static final String DEFAULT_AUTOWIRE_ATTRIBUTE = "default-autowire";
	public static final String DEFAULT_DEPENDENCY_CHECK_ATTRIBUTE = "default-dependency-check";
	public static final String DEFAULT_INIT_METHOD_ATTRIBUTE = "default-init-method";
	public static final String DEFAULT_DESTROY_METHOD_ATTRIBUTE = "default-destroy-method";
	public static final String DEFAULT_MERGE_ATTRIBUTE = "default-merge";

	// 属性常量
	public static final String ID_ATTRIBUTE = "id";
	public static final String BEAN_NAME_DELIMITERS = ",; ";
	public static final String TRUE_VALUE = "true";

	public static final String DEFAULT_VALUE = "default";

	public static final String DESCRIPTION_ELEMENT = "description";

	public static final String AUTOWIRE_BY_NAME_VALUE = "byName";

	public static final String AUTOWIRE_BY_TYPE_VALUE = "byType";

	public static final String AUTOWIRE_CONSTRUCTOR_VALUE = "constructor";

	public static final String AUTOWIRE_AUTODETECT_VALUE = "autodetect";

	public static final String DEPENDENCY_CHECK_ALL_ATTRIBUTE_VALUE = "all";

	public static final String DEPENDENCY_CHECK_SIMPLE_ATTRIBUTE_VALUE = "simple";

	public static final String DEPENDENCY_CHECK_OBJECTS_ATTRIBUTE_VALUE = "objects";

	public static final String NAME_ATTRIBUTE = "name";

	public static final String BEAN_ELEMENT = "bean";

	public static final String META_ELEMENT = "meta";

	public static final String PARENT_ATTRIBUTE = "parent";

	public static final String CLASS_ATTRIBUTE = "class";

	public static final String ABSTRACT_ATTRIBUTE = "abstract";

	public static final String SCOPE_ATTRIBUTE = "scope";

	public static final String SINGLETON_ATTRIBUTE = "singleton";

	public static final String LAZY_INIT_ATTRIBUTE = "lazy-init";

	public static final String AUTOWIRE_ATTRIBUTE = "autowire";

	public static final String AUTOWIRE_CANDIDATE_ATTRIBUTE = "autowire-candidate";

	public static final String DEPENDENCY_CHECK_ATTRIBUTE = "dependency-check";

	public static final String DEPENDS_ON_ATTRIBUTE = "depends-on";

	public static final String INIT_METHOD_ATTRIBUTE = "init-method";

	public static final String DESTROY_METHOD_ATTRIBUTE = "destroy-method";

	public static final String FACTORY_METHOD_ATTRIBUTE = "factory-method";

	public static final String FACTORY_BEAN_ATTRIBUTE = "factory-bean";

	public static final String CONSTRUCTOR_ARG_ELEMENT = "constructor-arg";

	public static final String INDEX_ATTRIBUTE = "index";

	public static final String TYPE_ATTRIBUTE = "type";

	public static final String VALUE_TYPE_ATTRIBUTE = "value-type";

	public static final String KEY_TYPE_ATTRIBUTE = "key-type";

	public static final String PROPERTY_ELEMENT = "property";

	public static final String REF_ATTRIBUTE = "ref";

	public static final String VALUE_ATTRIBUTE = "value";

	public static final String LOOKUP_METHOD_ELEMENT = "lookup-method";

	public static final String REPLACED_METHOD_ELEMENT = "replaced-method";

	public static final String REPLACER_ATTRIBUTE = "replacer";

	public static final String ARG_TYPE_ELEMENT = "arg-type";

	public static final String ARG_TYPE_MATCH_ATTRIBUTE = "match";

	public static final String REF_ELEMENT = "ref";

	public static final String IDREF_ELEMENT = "idref";

	public static final String BEAN_REF_ATTRIBUTE = "bean";

	public static final String LOCAL_REF_ATTRIBUTE = "local";

	public static final String PARENT_REF_ATTRIBUTE = "parent";

	public static final String VALUE_ELEMENT = "value";

	public static final String NULL_ELEMENT = "null";

	public static final String LIST_ELEMENT = "list";

	public static final String SET_ELEMENT = "set";

	public static final String MAP_ELEMENT = "map";

	public static final String ENTRY_ELEMENT = "entry";

	public static final String KEY_ELEMENT = "key";

	public static final String KEY_ATTRIBUTE = "key";

	public static final String KEY_REF_ATTRIBUTE = "key-ref";

	public static final String VALUE_REF_ATTRIBUTE = "value-ref";

	public static final String PROPS_ELEMENT = "props";

	public static final String PROP_ELEMENT = "prop";

	public static final String MERGE_ATTRIBUTE = "merge";

	// 默认名称空间
	public static final String BEANS_NAMESPACE_URI = "http://www.springframework.org/schema/beans";

	private XmlReaderContext readerContext;
	private ParseState parseState = new ParseState();

	protected final Log logger = LogFactory.getLog(getClass());
	@SuppressWarnings("rawtypes")
	private final Set usedName = new HashSet();

	// 根节点
	private String defaultLazyInit;
	private String defaultAutowire;
	private String defaultDependencyCheck;
	private String defaultInitMethod;
	private String defaultDestroyMethod;
	private String defaultMerge;

	public BeanDefinitionParserDelegate(XmlReaderContext readerContext) {
		Assert.notNull(readerContext, "readerContext不能为空。");
		this.readerContext = readerContext;
	}

	public BeanDefinitionHolder parseBeanDefinitionElement(Element el) {
		return parseBeanDefinitionElement(el, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public BeanDefinitionHolder parseBeanDefinitionElement(Element el, BeanDefinition containBean) {

		String id = el.getAttribute(ID_ATTRIBUTE);
		String nameAttr = el.getAttribute(NAME_ATTRIBUTE);

		// 处理name属性
		List alias = new ArrayList();
		if (StringUtils.hasLength(nameAttr)) {
			String[] nameArray = StringUtils.tokenizeToStringArray(nameAttr, BEAN_NAME_DELIMITERS);
			alias.addAll(Arrays.asList(nameArray));
		}

		// 如果指定了ID，就用ID作为beanName
		// 没有指定ID，就用Name属性。Name属性可用，；指定多个，用指定的第一个name作为beanName
		String beanName = id;
		if ((!StringUtils.hasText(beanName)) && (!alias.isEmpty())) {
			beanName = (String) alias.remove(0);
			if (logger.isDebugEnabled()) {
				logger.debug("ID属性未指定，使用Name属性作为beanName");
			}
		}

		// check beanName是否已经使用过
		if (containBean == null) {
			checkNameUniqUsed(beanName, alias, el);
		}

		// 解析bean元素
		AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(el, beanName, containBean);
		if (beanDefinition != null) {
			if (!StringUtils.hasText(beanName)) {
				// 如果ID和Name都为空，则内部生成一个beanName
				beanName = BeanDefinitionReaderUtils.generateBeanName(beanDefinition, getReaderContext().getRegistry(),
						(containBean != null));
				if (logger.isDebugEnabled()) {
					logger.debug("ID和Name都为空，采用内部生成beanName。");
				}
			}
			String[] aliasArray = StringUtils.toStringArray(alias);
			// 返回一个beanDefinitionHolder
			return new BeanDefinitionHolder(beanDefinition, beanName, aliasArray);
		}

		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void checkNameUniqUsed(String beanName, List alias, Element el) {
		String foundName = null;
		if (StringUtils.hasText(beanName) && this.usedName.contains(beanName)) {
			foundName = beanName;
		}
		this.usedName.add(beanName);

		if (!StringUtils.hasText(foundName)) {
			foundName = (String) CollectionUtils.findFirstMatch(this.usedName, alias);
		}

		if (StringUtils.hasText(foundName)) {
			getReaderContext().error("name属性已经存在。", el);
		}
		this.usedName.addAll(alias);
	}

	protected void error(String message, Element source) {
		getReaderContext().error(message, source, this.parseState.snapshot());
	}

	protected void error(String message, Element source, Throwable cause) {
		getReaderContext().error(message, source, this.parseState.snapshot(), cause);
	}

	public AbstractBeanDefinition parseBeanDefinitionElement(Element el, String beanName, BeanDefinition containBean) {
		String className = null;
		if (el.hasAttribute(CLASS_ATTRIBUTE)) {
			className = el.getAttribute(CLASS_ATTRIBUTE);
		}
		String parent = null;
		if (el.hasAttribute(PARENT_ATTRIBUTE)) {
			parent = el.getAttribute(PARENT_ATTRIBUTE);
		}

		try {
			this.parseState.push(new BeanEntry(beanName));
			AbstractBeanDefinition bd = BeanDefinitionReaderUtils.CreateBeanDefinition(parent, className,
					getReaderContext().getBeanDefinitionReader().getBeanClassLoader());
			if (el.hasAttribute(SCOPE_ATTRIBUTE)) {
				bd.setScope(el.getAttribute(SCOPE_ATTRIBUTE));
				if (el.hasAttribute(SINGLETON_ATTRIBUTE)) {
					error("同时指定了scope和singleTon属性。", el);
				}
			} else if (el.hasAttribute(SINGLETON_ATTRIBUTE)) {
				bd.setSingleton(TRUE_VALUE.equals(el.getAttribute(SINGLETON_ATTRIBUTE)));
			} else if (containBean != null) {
				bd.setSingleton(containBean.isSingleton());
			}

			if (el.hasAttribute(ABSTRACT_ATTRIBUTE)) {
				bd.setAbstract(TRUE_VALUE.equals(el.getAttribute(ABSTRACT_ATTRIBUTE)));
			}

			String lazyInit = el.getAttribute(LAZY_INIT_ATTRIBUTE);
			if (DEFAULT_VALUE.equals(lazyInit) && bd.isSingleton()) {
				lazyInit = getDefaultLazyInit();
			}
			bd.setLazyInit(TRUE_VALUE.equals(lazyInit));

			if (el.hasAttribute(AUTOWIRE_CANDIDATE_ATTRIBUTE)) {
				bd.setAutowireCandidate(TRUE_VALUE.equals(el.getAttribute(AUTOWIRE_CANDIDATE_ATTRIBUTE)));
			}

			String autowire = el.getAttribute(AUTOWIRE_ATTRIBUTE);
			if (DEFAULT_VALUE.equals(autowire)) {
				autowire = getDefaultAutowire();
			}
			bd.setAutowireMode(getAutowireMode(autowire));

			String dependencyCheck = el.getAttribute(DEPENDENCY_CHECK_ATTRIBUTE);
			if (DEFAULT_VALUE.equals(dependencyCheck)) {
				dependencyCheck = getDefaultDependencyCheck();
			}
			bd.setDependencyCheck(getDependencyCheck(dependencyCheck));

			if (el.hasAttribute(DEPENDS_ON_ATTRIBUTE)) {
				String dependsOn = el.getAttribute(DEPENDS_ON_ATTRIBUTE);
				String[] dependsOns = StringUtils.tokenizeToStringArray(dependsOn, BEAN_NAME_DELIMITERS);
				bd.setDependsOn(dependsOns);
			}

			if (el.hasAttribute(FACTORY_METHOD_ATTRIBUTE)) {
				bd.setFactoryMethodName(el.getAttribute(FACTORY_METHOD_ATTRIBUTE));
			}
			if (el.hasAttribute(FACTORY_BEAN_ATTRIBUTE)) {
				bd.setFactoryBeanName(el.getAttribute(FACTORY_BEAN_ATTRIBUTE));
			}

			if (el.hasAttribute(INIT_METHOD_ATTRIBUTE)) {
				String initMenthodName = el.getAttribute(INIT_METHOD_ATTRIBUTE);
				if (StringUtils.hasText(initMenthodName)) {
					bd.setInitMethodName(initMenthodName);
				}
			} else {
				if (StringUtils.hasText(getDefaultInitMethod())) {
					bd.setInitMethodName(getDefaultInitMethod());
					bd.setEnforceInitMethod(false);
				}
			}

			if (el.hasAttribute(DESTROY_METHOD_ATTRIBUTE)) {
				String destroyMethodName = el.getAttribute(DESTROY_METHOD_ATTRIBUTE);
				if (StringUtils.hasText(destroyMethodName)) {
					bd.setDestroyMethodName(destroyMethodName);
				}
			} else {
				if (StringUtils.hasText(getDefaultDestroyMethod())) {
					bd.setDestroyMethodName(getDefaultDestroyMethod());
					bd.setEnforceDestroyMethod(false);
				}
			}

			// meta标签
			parseMetaElement(el, bd);
			// LookUp标签
			parseLookupOverridesSubElement(el, bd.getMethodOverrides());
			// replacedMethod标签
			parseReplacedMethodSubElement(el, bd.getMethodOverrides());
			// constructor标签
			parseConstructorArgElements(el, bd);
			// property标签
			parsePropertyElements(el, bd);

			bd.setResourceDescription(getReaderContext().getResource().getDescription());
			bd.setSource(extractSource(el));

			return bd;

		} catch (ClassNotFoundException ex) {
			error("Bean class [" + className + "] not found", el, ex);
		} catch (NoClassDefFoundError err) {
			error("Class that bean class [" + className + "] depends on not found", el, err);
		} catch (Throwable ex) {
			error("Unexpected failure during bean definition parsing", el, ex);
		} finally {
			this.parseState.pop();
		}

		return null;
	}

	public void parseConstructorArgElements(Element el, BeanDefinition bd) {
		// 处理对象：bean下所有的construcotArg标签。
		NodeList nodes = el.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i) instanceof Element && DomUtils.nodeNameEquals(nodes.item(i), CONSTRUCTOR_ARG_ELEMENT)) {
				Element constructArg = (Element) nodes.item(i);
				parseConstructorArgElement(constructArg, bd);
			}
		}
	}

	public void parseConstructorArgElement(Element constructEl, BeanDefinition bd) {
		// 处理对象：单个construcotArg标签
		String indexAttr = constructEl.getAttribute(INDEX_ATTRIBUTE);
		String typeAttr = constructEl.getAttribute(TYPE_ATTRIBUTE);

		if (StringUtils.hasText(indexAttr)) {
			try {
				int index = Integer.valueOf(indexAttr);
				if (index < 0) {
					error("index不能小于0", constructEl);
				}
				try {
					this.parseState.push(new ConstructorArgumentEntry(index));
					Object value = this.parsePropertyValue(constructEl, bd, null);
					ConstructorArgumentValues.ValueHolder valueHolder = new ConstructorArgumentValues.ValueHolder(
							value);
					if (StringUtils.hasText(typeAttr)) {
						valueHolder.setType(typeAttr);
					}
					valueHolder.setSource(extractSource(constructEl));
					bd.getConstructorArgumentValues().addIndexedArgumentValue(index, valueHolder);
				} finally {
					this.parseState.pop();
				}
			} catch (NumberFormatException ex) {
				error("index 必须为数字。", constructEl);
			}
		} else {
			try {
				this.parseState.push(new ConstructorArgumentEntry());
				Object value = this.parsePropertyValue(constructEl, bd, null);
				ConstructorArgumentValues.ValueHolder valueHolder = new ConstructorArgumentValues.ValueHolder(value);
				if (StringUtils.hasText(typeAttr)) {
					valueHolder.setType(typeAttr);
				}
				valueHolder.setSource(extractSource(constructEl));
				bd.getConstructorArgumentValues().addGenericArgumentValue(valueHolder);
			} finally {
				this.parseState.pop();
			}
		}
	}

	public void parsePropertyElements(Element el, BeanDefinition bd) {
		// 处理对象：Bean标签下所有的property子标签.
		NodeList nodes = el.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof Element && DomUtils.nodeNameEquals(node, PROPERTY_ELEMENT)) {
				Element propertyElemrnt = (Element) node;
				parsePropertyElement(propertyElemrnt, bd);
			}
		}
	}

	public void parsePropertyElement(Element el, BeanDefinition bd) {

		String name = el.getAttribute(NAME_ATTRIBUTE);
		if (!StringUtils.hasLength(name)) {
			error("name属性不能为空", el);
		}

		this.parseState.push(new PropertyEntry(name));
		try {
			if (bd.getPropertyValues().contains(name)) {
				error("name" + name + "已经存在。", el);
			}

			Object value = parsePropertyValue(el, bd, name);
			PropertyValue pv = new PropertyValue(name, value);
			parseMetaElement(el, bd);
			pv.setSource(extractSource(el));
			bd.getPropertyValues().addPropertyValue(pv);
		} finally {
			this.parseState.pop();
		}

	}

	public Object parsePropertyValue(Element el, BeanDefinition bd, String propertyName) {
		// 解析单个Property标签

		// 查找子标签
		NodeList nodes = el.getChildNodes();
		Element subElement = null;
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i) instanceof Element) {
				Element subEl = (Element) nodes.item(i);
				if (DESCRIPTION_ELEMENT.equals(subEl.getTagName())) {
					// do nothing.
				} else {
					if (subElement != null && !META_ELEMENT.equals(subEl.getTagName())) {
						error("property标签下只能有一个子标签。", el);
					}
					subElement = subEl;
				}
			}
		}

		boolean hasValueAttribute = el.hasAttribute(VALUE_ATTRIBUTE);
		boolean hasRefAttribute = el.hasAttribute(REF_ATTRIBUTE);
		// 校验Property标签的整合性
		if ((hasValueAttribute && hasRefAttribute) || ((hasValueAttribute || hasRefAttribute) && subElement != null)) {
			error("value，ref，子标签整合性出错。", el);
		}

		if (hasRefAttribute) {
			String ref = el.getAttribute(REF_ATTRIBUTE);
			if (!StringUtils.hasText(ref)) {
				error("REF元素的值不能为空。", el);
			}
			RuntimeBeanReference rbr = new RuntimeBeanReference(ref);
			rbr.setSource(extractSource(el));
			return rbr;
		} else if (hasValueAttribute) {
			return el.getAttribute(VALUE_ATTRIBUTE);
		}

		if (subElement == null) {
			error("Value和Ref元素没有指定，子标签不能为空。", el);
		}

		return parsePropertySubElement(subElement, bd, null);
	}

	public Object parsePropertySubElement(Element subElement, BeanDefinition bd, String defaultTypeClassName) {

		if (DomUtils.nodeNameEquals(subElement, BEAN_ELEMENT)) {
			return this.parseBeanDefinitionElement(subElement, bd);
		} else if (DomUtils.nodeNameEquals(subElement, REF_ELEMENT)) {
			boolean toParent = false;
			String refName = subElement.getAttribute(REF_ATTRIBUTE);
			if (!StringUtils.hasLength(refName)) {
				refName = subElement.getAttribute(LOCAL_REF_ATTRIBUTE);
				if (!StringUtils.hasLength(refName)) {
					refName = subElement.getAttribute(PARENT_REF_ATTRIBUTE);
					toParent = true;
					if (!StringUtils.hasLength(refName)) {
						error("ref，local，parent属性都没有设定。", subElement);
					}
				}
			}
			if (!StringUtils.hasText(refName)) {
				error("设定的属性值里含有空格。", subElement);
			}
			RuntimeBeanReference rbr = new RuntimeBeanReference(refName, toParent);
			rbr.setSource(extractSource(subElement));
			return rbr;
		} else if (DomUtils.nodeNameEquals(subElement, VALUE_ELEMENT)) {
			String type = DomUtils.getTextValue(subElement);
			String typeClassName = subElement.getAttribute(TYPE_ATTRIBUTE);
			if (!StringUtils.hasText(typeClassName)) {
				typeClassName = defaultTypeClassName;
			}
			if (StringUtils.hasText(typeClassName)) {

				try {
					return buildTypedStringValue(type, typeClassName);
				} catch (ClassNotFoundException e) {
					error("指定的type没有找到相应的class。", subElement);
				}
			}
			return type;
		} else if (DomUtils.nodeNameEquals(subElement, LIST_ELEMENT)) {
			return parseListElement(subElement, bd);
		} else if (DomUtils.nodeNameEquals(subElement, SET_ELEMENT)) {
			return parseSetElement(subElement, bd);
		} else if (DomUtils.nodeNameEquals(subElement, PROPS_ELEMENT)) {
			return parsePropsElement(subElement);
		} else if (DomUtils.nodeNameEquals(subElement, MAP_ELEMENT)) {
			return parseMapElement(subElement, bd);
		} else if (!isDefaultNamespace(subElement.getNamespaceURI())) {
			this.parseNestedCustomElement(subElement, bd);
		}

		error("未知类型，无法解析。", subElement);
		return null;
	}

	@SuppressWarnings("rawtypes")
	protected Map parseMapElement(Element mapElement, BeanDefinition bd) {

		String defaultKeyTypeClassName = mapElement.getAttribute(KEY_TYPE_ATTRIBUTE);
		String defaultValueTypeClassName = mapElement.getAttribute(VALUE_TYPE_ATTRIBUTE);

		List entrys = DomUtils.getChildElementsByTagName(mapElement, ENTRY_ELEMENT);
		ManagedMap map = new ManagedMap(entrys.size());
		map.setSource(extractSource(mapElement));
		map.setMergeEnabled(this.parseMergeAttribute(mapElement));

		for (Iterator it = entrys.iterator(); it.hasNext();) {
			if (it.next() instanceof Element) {
				Element entry = (Element) it.next();
				NodeList entrySubElements = entry.getChildNodes();
				Element keyEle = null;
				Element valueEle = null;

				// 每个Entry最多只能有一个key和value
				for (int i = 0; i < entrySubElements.getLength(); i++) {
					if (entrySubElements.item(i) instanceof Element) {
						Element subEntry = (Element) entrySubElements.item(i);
						if (DomUtils.nodeNameEquals(subEntry, KEY_ELEMENT)) {
							if (keyEle != null) {
								error("key子标签只能指定一个。", mapElement);
							}
							keyEle = subEntry;
						} else if (DomUtils.nodeNameEquals(subEntry, VALUE_ELEMENT)) {
							if (valueEle != null) {
								error("key子标签只能指定一个。", mapElement);
							}
							valueEle = subEntry;
						}
					}
				}

				// 处理key
				Object key = null;
				boolean hasKeyAttri = entry.hasAttribute(KEY_ELEMENT);
				boolean hasKeyRefAttri = entry.hasAttribute(KEY_REF_ATTRIBUTE);
				if ((hasKeyAttri && hasKeyRefAttri) || ((hasKeyAttri || hasKeyRefAttri) && keyEle != null)) {
					error("key,key-ref,key子标签3者同时只能指定一个。", entry);
				}
				if (hasKeyAttri) {
					if (!StringUtils.hasText(entry.getAttribute(KEY_ATTRIBUTE))) {
						error("key属性值为空。", entry);
					}
					key = extractTypedStringValueIfNecessary(mapElement, entry.getAttribute(KEY_ATTRIBUTE),
							defaultKeyTypeClassName);
				} else if (hasKeyRefAttri) {
					String keyRef = entry.getAttribute(KEY_REF_ATTRIBUTE);
					if (!StringUtils.hasText(keyRef)) {
						error("key-ref属性值为空。", entry);
					}
					RuntimeBeanReference rbr = new RuntimeBeanReference(keyRef);
					rbr.setSource(extractSource(keyEle));
					key = rbr;
				} else if (keyEle != null) {
					key = parserKeyElement(keyEle, bd, defaultKeyTypeClassName);
				} else {
					error("需要指定key属性。", entry);
				}

				// 处理value
				Object value = null;
				boolean hasValueAttri = entry.hasAttribute(VALUE_ATTRIBUTE);
				boolean hasValueRefAttri = entry.hasAttribute(VALUE_REF_ATTRIBUTE);
				if ((hasValueAttri && hasValueRefAttri) || ((hasValueAttri || hasValueRefAttri) && valueEle != null)) {
					error("value,value-ref,value子标签3者同时只能指定一个。", entry);
				}
				if (hasValueAttri) {
					if (!StringUtils.hasText(entry.getAttribute(VALUE_ATTRIBUTE))) {
						error("value属性值为空。", entry);
					}
					value = extractTypedStringValueIfNecessary(mapElement, entry.getAttribute(VALUE_ATTRIBUTE),
							defaultValueTypeClassName);
				} else if (hasValueRefAttri) {
					String valueRef = entry.getAttribute(VALUE_REF_ATTRIBUTE);
					if (!StringUtils.hasText(valueRef)) {
						error("value-ref属性值为空。", entry);
					}
					RuntimeBeanReference rbr = new RuntimeBeanReference(valueRef);
					rbr.setSource(extractSource(valueEle));
					value = rbr;
				} else if (valueEle != null) {
					value = this.parsePropertySubElement(valueEle, bd, defaultValueTypeClassName);
				} else {
					error("需要指定value属性。", entry);
				}

				map.put(key, value);
			}
		}

		return map;
	}

	protected Object parserKeyElement(Element el, BeanDefinition bd, String defaultTypeClassName) {

		NodeList nodes = el.getChildNodes();
		Element subEls = null;
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i) instanceof Element) {
				if (subEls != null) {
					error("key标签下只能有一个子标签。", el);
				}
				subEls = (Element) nodes.item(i);
			}
		}
		return parsePropertySubElement(el, bd, defaultTypeClassName);

	}

	private Object extractTypedStringValueIfNecessary(Element mapElement, String attributeValue,
			String defaultTypeClassName) {

		if (!StringUtils.hasText(defaultTypeClassName)) {
			return attributeValue;
		}

		try {
			return buildTypedStringValue(attributeValue, defaultTypeClassName);
		} catch (ClassNotFoundException e) {
			error("Unable to load class '" + defaultTypeClassName + "' for Map key/value type", mapElement);
			return attributeValue;
		}
	}

	@SuppressWarnings("rawtypes")
	protected Properties parsePropsElement(Element el) {

		ManagedProperties propers = new ManagedProperties();
		propers.setSource(extractSource(el));
		propers.setMergeEnabled(parseMergeAttribute(el));
		List subPropsEle = DomUtils.getChildElementsByTagName(el, PROP_ELEMENT);
		for (Iterator it = subPropsEle.iterator(); it.hasNext();) {
			Element eleSub = (Element) it.next();
			String key = eleSub.getAttribute(KEY_ATTRIBUTE);
			String value = DomUtils.getTextValue(eleSub).trim();
			propers.setProperty(key, value);
		}
		return propers;
	}

	@SuppressWarnings("rawtypes")
	protected Set parseSetElement(Element el, BeanDefinition bd) {
		String defaultTypeClassName = el.getAttribute(VALUE_TYPE_ATTRIBUTE);
		NodeList nodes = el.getChildNodes();
		ManagedSet set = new ManagedSet(nodes.getLength());
		set.setSource(extractSource(el));
		set.setMergeEnabled(parseMergeAttribute(el));
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i) instanceof Element) {
				Element ele = (Element) nodes.item(i);
				Object objList = this.parsePropertySubElement(ele, bd, defaultTypeClassName);
				set.add(objList);
			}
		}
		return set;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected List parseListElement(Element el, BeanDefinition bd) {

		String defaultTypeClassName = el.getAttribute(VALUE_TYPE_ATTRIBUTE);
		NodeList nodes = el.getChildNodes();
		ManagedList list = new ManagedList(nodes.getLength());
		list.setSource(extractSource(el));
		list.setMergeEnabled(parseMergeAttribute(el));
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i) instanceof Element) {
				Element ele = (Element) nodes.item(i);
				Object objList = this.parsePropertySubElement(ele, bd, defaultTypeClassName);
				list.add(objList);
			}
		}
		return list;
	}

	protected boolean parseMergeAttribute(Element el) {
		String value = el.getAttribute(MERGE_ATTRIBUTE);
		if (DEFAULT_VALUE.equals(value)) {
			value = this.getDefaultMerge();
		}
		return TRUE_VALUE.equals(value);
	}

	@SuppressWarnings("rawtypes")
	protected TypedStringValue buildTypedStringValue(String type, String targetTypedName)
			throws ClassNotFoundException {
		ClassLoader classloader = getReaderContext().getBeanDefinitionReader().getBeanClassLoader();
		if (classloader != null) {
			Class clazz = ClassUtils.forName(type, classloader);
			return new TypedStringValue(type, clazz);
		}
		return new TypedStringValue(type, targetTypedName);
	}

	@SuppressWarnings("rawtypes")
	public void parseReplacedMethodSubElement(Element el, MethodOverrides overrides) {
		NodeList nodes = el.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof Element && DomUtils.nodeNameEquals(node, REPLACED_METHOD_ELEMENT)) {
				Element element = (Element) node;
				String repalcedMethodName = element.getAttribute(NAME_ATTRIBUTE);
				String replacedBean = element.getAttribute(REPLACER_ATTRIBUTE);
				ReplaceOverride Override = new ReplaceOverride(repalcedMethodName, replacedBean);
				List argList = DomUtils.getChildElementsByTagName(el, ARG_TYPE_ELEMENT);
				if (!argList.isEmpty()) {
					for (int j = 0; j < argList.size(); j++) {
						String argType = ((Element) argList.get(j)).getAttribute(ARG_TYPE_MATCH_ATTRIBUTE);
						Override.addTypeIdentifier(argType);
					}
				}
				Override.setSource(element);
				overrides.addOverride(Override);
			}
		}
	}

	public void parseLookupOverridesSubElement(Element el, MethodOverrides methodOverrides) {
		NodeList nodes = el.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof Element && DomUtils.nodeNameEquals(node, LOOKUP_METHOD_ELEMENT)) {
				Element element = (Element) node;
				String methodName = element.getAttribute(NAME_ATTRIBUTE);
				String beanRef = element.getAttribute(BEAN_ELEMENT);
				LookupOverride lookUpoverride = new LookupOverride(methodName, beanRef);
				lookUpoverride.setSource(element);
				methodOverrides.addOverride(lookUpoverride);
			}
		}
	}

	public void parseMetaElement(Element el, AttributeAccessor attributeAccessor) {
		NodeList nodes = el.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node instanceof Element && DomUtils.nodeNameEquals(node, META_ELEMENT)) {
				Element element = (Element) node;
				String key = element.getAttribute(KEY_ATTRIBUTE);
				String value = element.getAttribute(VALUE_ATTRIBUTE);
				attributeAccessor.setAttribute(key, value);
			}
		}
	}

	public int getDependencyCheck(String att) {
		int dependencyCheckCode = AbstractBeanDefinition.DEPENDENCY_CHECK_NONE;
		if (DEPENDENCY_CHECK_ALL_ATTRIBUTE_VALUE.equals(att)) {
			dependencyCheckCode = AbstractBeanDefinition.DEPENDENCY_CHECK_ALL;
		} else if (DEPENDENCY_CHECK_SIMPLE_ATTRIBUTE_VALUE.equals(att)) {
			dependencyCheckCode = AbstractBeanDefinition.DEPENDENCY_CHECK_SIMPLE;
		} else if (DEPENDENCY_CHECK_OBJECTS_ATTRIBUTE_VALUE.equals(att)) {
			dependencyCheckCode = AbstractBeanDefinition.DEPENDENCY_CHECK_OBJECTS;
		}
		// Else leave default value.
		return dependencyCheckCode;
	}

	public int getAutowireMode(String att) {
		int autowire = AbstractBeanDefinition.AUTOWIRE_NO;
		if (AUTOWIRE_BY_NAME_VALUE.equals(att)) {
			autowire = AbstractBeanDefinition.AUTOWIRE_BY_NAME;
		} else if (AUTOWIRE_BY_TYPE_VALUE.equals(att)) {
			autowire = AbstractBeanDefinition.AUTOWIRE_BY_TYPE;
		} else if (AUTOWIRE_CONSTRUCTOR_VALUE.equals(att)) {
			autowire = AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR;
		} else if (AUTOWIRE_AUTODETECT_VALUE.equals(att)) {
			autowire = AbstractBeanDefinition.AUTOWIRE_AUTODETECT;
		}
		// Else leave default value.
		return autowire;
	}

	public BeanDefinitionHolder decoratorBeanDefinitionIfRequired(Element el, BeanDefinitionHolder beanHolder) {
		BeanDefinitionHolder decoratoredHolder = beanHolder;

		// decorator 属性Node
		NamedNodeMap attrNodes = el.getAttributes();
		for (int i = 0; i < attrNodes.getLength(); i++) {
			Node attrNode = attrNodes.item(i);
			decoratoredHolder = innerDecorator(attrNode, decoratoredHolder);
		}

		// decorator 标签Node
		NodeList childNodes = el.getChildNodes();
		for (int j = 0; j < childNodes.getLength(); j++) {
			Node child = childNodes.item(j);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				decoratoredHolder = innerDecorator(child, decoratoredHolder);
			}
		}

		return decoratoredHolder;
	}

	private BeanDefinitionHolder innerDecorator(Node node, BeanDefinitionHolder tobeDecorator) {
		String url = node.getNamespaceURI();
		if (!isDefaultNamespace(url)) {
			NamespaceHandler handler = this.getReaderContext().getNamespaceHandlerResolver().resolve(url);
			tobeDecorator = handler.decorate(node, tobeDecorator, new ParserContext(this.getReaderContext(), this));
		}
		return tobeDecorator;
	}

	public void initDefaults(Element root) {
		// 对以下属性进行初始设定
		// default-lazy-init,default-autowire,default-dependency-check,default-init-method,default-destroy-method,default-merge
		this.setDefaultLazyInit(root.getAttribute(DEFAULT_LAZY_INIT_ATTRIBUTE));
		this.setDefaultAutowire(root.getAttribute(DEFAULT_AUTOWIRE_ATTRIBUTE));
		this.setDefaultDependencyCheck(root.getAttribute(DEFAULT_DEPENDENCY_CHECK_ATTRIBUTE));
		if (root.hasAttribute(DEFAULT_INIT_METHOD_ATTRIBUTE)) {
			this.setDefaultInitMethod(root.getAttribute(DEFAULT_INIT_METHOD_ATTRIBUTE));
		}
		if (root.hasAttribute(DEFAULT_DESTROY_METHOD_ATTRIBUTE)) {
			this.setDefaultDestroyMethod(root.getAttribute(DEFAULT_DESTROY_METHOD_ATTRIBUTE));
		}
		this.setDefaultMerge(root.getAttribute(DEFAULT_MERGE_ATTRIBUTE));
	}

	public boolean isDefaultNamespace(String namespaceUri) {
		return (!StringUtils.hasLength(namespaceUri)) || BEANS_NAMESPACE_URI.equals(namespaceUri);
	}

	public BeanDefinition parseCustomElement(Element el) {
		return parseCustomElement(el, null);
	}

	public BeanDefinition parseCustomElement(Element el, BeanDefinition containBd) {
		String namespaceUri = el.getNamespaceURI();
		NamespaceHandler handler = getReaderContext().getNamespaceHandlerResolver().resolve(namespaceUri);
		if (handler == null) {
			getReaderContext().error("无法获得名称空间namespace [" + namespaceUri + "]的解析器", el);
			return null;
		}
		return handler.parse(el, new ParserContext(getReaderContext(), this, containBd));
	}

	private Object parseNestedCustomElement(Element candidateEle, BeanDefinition containingBd) {
		BeanDefinition bd = this.parseCustomElement(candidateEle, containingBd);
		if (bd == null) {
			error("无法解析此自定义标签。", candidateEle);
			return null;
		}
		return bd;
	}

	public XmlReaderContext getReaderContext() {
		return readerContext;
	}

	protected Object extractSource(Element el) {
		return getReaderContext().extractSource(el);
	}

	public void setReaderContext(XmlReaderContext readerContext) {
		this.readerContext = readerContext;
	}

	public String getDefaultLazyInit() {
		return defaultLazyInit;
	}

	public void setDefaultLazyInit(String defaultLazyInit) {
		this.defaultLazyInit = defaultLazyInit;
	}

	public String getDefaultAutowire() {
		return defaultAutowire;
	}

	public void setDefaultAutowire(String defaultAutowire) {
		this.defaultAutowire = defaultAutowire;
	}

	public String getDefaultDependencyCheck() {
		return defaultDependencyCheck;
	}

	public void setDefaultDependencyCheck(String defaultDependencyCheck) {
		this.defaultDependencyCheck = defaultDependencyCheck;
	}

	public String getDefaultInitMethod() {
		return defaultInitMethod;
	}

	public void setDefaultInitMethod(String defaultInitMethod) {
		this.defaultInitMethod = defaultInitMethod;
	}

	public String getDefaultDestroyMethod() {
		return defaultDestroyMethod;
	}

	public void setDefaultDestroyMethod(String defaultDestroyMethod) {
		this.defaultDestroyMethod = defaultDestroyMethod;
	}

	public String getDefaultMerge() {
		return defaultMerge;
	}

	public void setDefaultMerge(String defaultMerge) {
		this.defaultMerge = defaultMerge;
	}

}
