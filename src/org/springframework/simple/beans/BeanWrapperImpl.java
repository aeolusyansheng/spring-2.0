package org.springframework.simple.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.GenericCollectionTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class BeanWrapperImpl extends AbstractPropertyAccessor implements BeanWrapper, TypeConverter {

	// bean工厂启动期间会产生大量的BeanWrapperImpl对象，logger只用一个。
	private static final Log logger = LogFactory.getLog(BeanWrapperImpl.class);

	// 被包装对象
	private Object object;

	private String nestPath = "";
	private Object rootObject;

	// 类型转换器代理
	private TypeConverterDelegate typeConverterDelegate;

	// 缓存内省结果
	private CachedIntrospectionResults cachedIntrospectionResults;

	private Map nestedBeanWrappers;

	public BeanWrapperImpl() {
		this(true);
	}

	public BeanWrapperImpl(boolean registerDefaultEditors) {
		if (registerDefaultEditors) {
			registerDefaultEditors();
		}
		this.typeConverterDelegate = new TypeConverterDelegate(this);
	}

	public BeanWrapperImpl(Object object) {
		registerDefaultEditors();
		setWrappedInstance(object);
	}

	public BeanWrapperImpl(Class clazz) {
		registerDefaultEditors();
		setWrappedInstance(BeanUtils.instantiateClass(clazz));
	}

	public BeanWrapperImpl(Object object, String nestPath, Object rootObject) {
		registerDefaultEditors();
		setWrappedInstance(object, nestPath, rootObject);
	}

	private BeanWrapperImpl(Object object, String nestPath, BeanWrapperImpl superBw) {
		setWrappedInstance(object, nestPath, superBw.getWrappedInstance());
		setExtractOldValueForEditor(superBw.isExtractOldValueForEditor());
	}

	@Override
	public boolean isReadableProperty(String propertyName) throws BeansException {
		Assert.notNull(propertyName, "propertyName不能为空。");
		try {
			PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
			if (pd != null) {
				if (pd.getReadMethod() != null) {
					return true;
				}
			} else {
				getPropertyValue(propertyName);
				return true;
			}

		} catch (InvalidPropertyException ex) {
			// 不处理，一定是非可读的。
		}
		return false;
	}

	private String getFinalPath(BeanWrapper bw, String nestedPath) {
		if (bw == this) {
			return nestedPath;
		}
		return nestedPath.substring(PropertyAccessorUtils.getLastNestedPropertySeparatorIndex(nestedPath) + 1);
	}

	protected PropertyDescriptor getPropertyDescriptorInternal(String propertyName) throws BeansException {
		Assert.state(this.object != null, "BeanWrapper内没有对象实例。");
		BeanWrapperImpl nestedBw = getBeanWrapperForPropertyPath(propertyName);
		return nestedBw.cachedIntrospectionResults.getPropertyDescriptor(getFinalPath(nestedBw, propertyName));
	}

	protected BeanWrapperImpl getBeanWrapperForPropertyPath(String propertyPath) throws BeansException {
		int pos = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(propertyPath);
		if (pos > -1) {
			String nestedProperty = propertyPath.substring(0, pos);
			String nestedPath = propertyPath.substring(pos + 1);
			BeanWrapperImpl nestedBw = getNestedBeanWrapper(nestedProperty);
			// 递归查找
			return nestedBw.getBeanWrapperForPropertyPath(nestedPath);
		} else {
			return this;
		}
	}

	private BeanWrapperImpl getNestedBeanWrapper(String nestedProperty) throws BeansException {

		if (this.nestedBeanWrappers == null) {
			this.nestedBeanWrappers = new HashMap();
		}
		PropertyTokenHolder tokens = getPropertyNameTokens(nestedProperty);
		String canonicalName = tokens.canonicalName;
		Object propertyValue = getPropertyValue(tokens);
		if (propertyValue == null) {
			throw new NullValueInNestedPathException(getRootClass(), this.nestPath + canonicalName);
		}

		// 查找缓存
		BeanWrapperImpl nestedBw = (BeanWrapperImpl) this.nestedBeanWrappers.get(canonicalName);
		if (nestedBw == null || nestedBw.getWrappedInstance() != propertyValue) {
			if (logger.isDebugEnabled()) {
				logger.debug("Creating new nested BeanWrapper for property '" + canonicalName + "'");
			}
			// 缓存里没有则新建一个，并放入缓存
			nestedBw = newNestedBeanWrapper(propertyValue, this.nestPath + canonicalName + NESTED_PROPERTY_SEPARATOR);
			copyDefaultEditorTo(nestedBw);
			copyCustomEditorsTo(nestedBw, canonicalName);
			this.nestedBeanWrappers.put(canonicalName, nestedBw);

		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Using cached nested BeanWrapper for property '" + canonicalName + "'");
			}
		}

		return nestedBw;
	}

	protected BeanWrapperImpl newNestedBeanWrapper(Object object, String nestedPath) {
		return new BeanWrapperImpl(object, nestedPath, this);
	}

	private PropertyTokenHolder getPropertyNameTokens(String propertyName) {
		PropertyTokenHolder tokens = new PropertyTokenHolder();
		String actualName = null;
		List keys = new ArrayList(2);
		int searchIndex = 0;
		while (searchIndex != -1) {
			int keyStart = propertyName.indexOf(PROPERTY_KEY_PREFIX, searchIndex);
			searchIndex = -1;
			if (keyStart != -1) {
				int keyEnd = propertyName.indexOf(PROPERTY_KEY_SUFFIX, keyStart + PROPERTY_KEY_PREFIX.length());
				if (keyEnd != -1) {
					if (actualName == null) {
						actualName = propertyName.substring(0, keyStart);
					}
					String key = propertyName.substring(keyStart + PROPERTY_KEY_PREFIX.length(), keyEnd);
					if ((key.startsWith("'") && key.endsWith("'")) || (key.startsWith("\"") && key.endsWith("\""))) {
						key = key.substring(1, key.length() - 1);
					}
					keys.add(key);
					searchIndex = keyEnd + PROPERTY_KEY_SUFFIX.length();
				}
			}
		}
		tokens.actualName = (actualName != null ? actualName : propertyName);
		tokens.canonicalName = tokens.actualName;
		if (!keys.isEmpty()) {
			tokens.canonicalName += PROPERTY_KEY_PREFIX
					+ StringUtils.collectionToDelimitedString(keys, PROPERTY_KEY_SUFFIX + PROPERTY_KEY_PREFIX)
					+ PROPERTY_KEY_SUFFIX;
			tokens.keys = StringUtils.toStringArray(keys);
		}
		return tokens;
	}

	@Override
	public boolean isWritableproperty(String propertyName) throws BeansException {

		Assert.notNull(propertyName, "propertyName不能为空。");
		try {
			PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
			if (pd != null) {
				if (pd.getWriteMethod() != null) {
					return true;
				}
			} else {
				getPropertyValue(propertyName);
				return true;
			}
		} catch (InvalidPropertyException ex) {
			// do nothing
		}
		return false;
	}

	@Override
	public Object convertIfNecessory(Object value, Class reqiredType) throws TypeMismatchException {
		return convertIfNecessory(value, reqiredType, null);
	}

	@Override
	public Object convertIfNecessory(Object value, Class reqiredType, MethodParameter methodParameter)
			throws TypeMismatchException {
		try {
			return this.typeConverterDelegate.convertIfNecessary(value, reqiredType, methodParameter);
		} catch (IllegalStateException ex) {
			throw new TypeMismatchException(value, reqiredType, ex);
		}
	}

	@Override
	public void setWrappedInstance(Object object) {
		setWrappedInstance(object, "", null);
	}

	public void setWrappedInstance(Object object, String nestPath, Object rootObject) {

		Assert.notNull(object, "object不能为空。");
		this.object = object;
		this.nestPath = (nestPath != null ? nestPath : "");
		this.rootObject = (!"".equals(nestPath) ? rootObject : object);
		this.typeConverterDelegate = new TypeConverterDelegate(this, object);
		// 设置内省类
		this.setIntrospectionClass(object.getClass());
	}

	/**
	 * 设定内省类
	 */
	protected void setIntrospectionClass(Class clazz) {
		if (this.cachedIntrospectionResults == null || !this.cachedIntrospectionResults.getBeanClass().equals(clazz)) {
			this.cachedIntrospectionResults = CachedIntrospectionResults.forClass(clazz);
		}
	}

	@Override
	public Object getWrappedInstance() {
		return this.object;
	}

	@Override
	public Class getWrappedClass() {
		return this.object.getClass();
	}

	public String getNestedPath() {
		return this.nestPath;
	}

	public Object getRootInstance() {
		return this.rootObject;
	}

	public Class getRootClass() {
		return (this.rootObject != null ? this.rootObject.getClass() : null);
	}

	@Override
	public PropertyDescriptor[] getPropertyDescriptors() throws BeansException {
		return this.cachedIntrospectionResults.getBeanInfo().getPropertyDescriptors();
	}

	@Override
	public PropertyDescriptor getPropertyDescriptor(String propertyName) throws BeansException {
		Assert.notNull(propertyName, "propertyName不能为空。");
		PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
		if (pd != null) {
			return pd;
		} else {
			throw new InvalidPropertyException(getRootClass(), this.nestPath + propertyName, propertyName + "属性找不到。");
		}
	}

	@Override
	public Object getPropertyValue(String propertyName) throws BeansException {
		BeanWrapperImpl nestedBw = getBeanWrapperForPropertyPath(propertyName);
		PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestedBw, propertyName));
		return nestedBw.getPropertyValue(tokens);
	}

	private Object getPropertyValue(PropertyTokenHolder tokens) throws BeansException {

		String propertyName = tokens.canonicalName;
		String actualName = tokens.actualName;
		PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
		if (pd == null || pd.getReadMethod() == null) {
			throw new NotReadablePropertyException(getRootClass(), this.nestPath + propertyName);
		}
		Method readMethod = pd.getReadMethod();
		if (logger.isDebugEnabled()) {
			logger.debug("通过反射执行read方法。");
		}
		try {
			if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
				readMethod.setAccessible(true);
			}
			Object value = readMethod.invoke(this.object, (Object[]) null);
			if (tokens.keys != null) {
				// 处理数组或Map
				for (int i = 0; i < tokens.keys.length; i++) {
					String key = tokens.keys[i];
					if (value == null) {
						throw new NullValueInNestedPathException(getRootClass(), this.nestPath + propertyName,
								"无法访问控制的index元素。");
					}
					// 数组
					else if (value.getClass().isArray()) {
						value = Array.get(value, Integer.parseInt(key));
					}
					// list
					else if (value instanceof List) {
						List list = (List) value;
						value = list.get(Integer.parseInt(key));
					}
					// Set
					else if (value instanceof Set) {
						Set set = (Set) value;
						int index = Integer.parseInt(key);
						if (index < 0 || index > set.size()) {
							throw new InvalidPropertyException(getRootClass(), this.nestPath + propertyName,
									"下标越界无法访问。");
						}
						Iterator it = set.iterator();
						for (int j = 0; it.hasNext(); j++) {
							Object element = it.next();
							if (index == j) {
								value = element;
								break;
							}
						}
					}
					// Map
					else if (value instanceof Map) {
						Map map = (Map) value;
						Class mapKeyType = GenericCollectionTypeResolver.getMapKeyReturnType(readMethod,
								tokens.keys.length);
						Object convertedMapKey = this.typeConverterDelegate.convertIfNecessary(null, null, key,
								mapKeyType);
						value = map.get(convertedMapKey);
					}
					// 其他
					else {
						throw new InvalidPropertyException(getRootClass(), this.nestPath + propertyName, "无法处理的类型。");
					}
				}
			}
			return value;
		} catch (IllegalAccessException ex) {
			throw new InvalidPropertyException(getRootClass(), this.nestPath + propertyName, "非法访问，权限异常", ex);
		} catch (IllegalArgumentException ex) {
			throw new InvalidPropertyException(getRootClass(), this.nestPath + propertyName, "非法参数异常", ex);
		} catch (InvocationTargetException ex) {
			throw new InvalidPropertyException(getRootClass(), this.nestPath + propertyName, "不存在getter属性异常", ex);
		} catch (ArrayIndexOutOfBoundsException ex) {
			throw new InvalidPropertyException(getRootClass(), this.nestPath + propertyName, "数组越界异常", ex);
		}
	}

	@Override
	public void setPropertyValue(String propertyName, Object value) throws BeansException {
		BeanWrapperImpl nestBw = null;
		try {
			nestBw = getBeanWrapperForPropertyPath(propertyName);

		} catch (NotReadablePropertyException ex) {
			throw new NotWritablePropertyException(getRootClass(), this.nestPath + propertyName,
					propertyName + "属性不存在。", ex);
		}
		PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestBw, propertyName));
		nestBw.setPropertyValue(tokens, value);
	}

	private void setPropertyValue(PropertyTokenHolder tokens, Object newValue) throws BeansException {
		String propertyName = tokens.canonicalName;

		// 集合类或者数组时
		if (tokens.keys != null) {
			PropertyTokenHolder getterTokens = new PropertyTokenHolder();
			getterTokens.canonicalName = tokens.canonicalName;
			getterTokens.actualName = tokens.actualName;
			getterTokens.keys = new String[tokens.keys.length - 1];
			// 拷贝数组
			System.arraycopy(tokens.keys, 0, getterTokens.keys, 0, tokens.keys.length - 1);
			Object propValue = null;
			try {
				// 取倒数第二层的对象，以便于设定最后层的值
				propValue = getPropertyValue(getterTokens);
			} catch (NotReadablePropertyException ex) {
				throw new NotWritablePropertyException(getRootClass(), this.nestPath + propertyName,
						"取得倒数第二层元素的对象失败。属性名：" + propertyName, ex);
			}
			// 给最后层设值
			String key = tokens.keys[tokens.keys.length - 1];
			// 值为空，异常
			if (propValue == null) {
				throw new NullValueInNestedPathException(getRootClass(), this.nestPath + propertyName,
						"取得倒数第二层元素的对象为空。属性：" + propertyName);
			}
			// 值为数组
			else if (propValue.getClass().isArray()) {
				// 数组元素的类型
				Class requiredType = propValue.getClass().getComponentType();
				int arrayIndex = Integer.parseInt(key);
				Object oldValue = null;
				try {
					if (isExtractOldValueForEditor()) {
						oldValue = Array.get(propValue, arrayIndex);
					}
					Object convertedValue = this.typeConverterDelegate.convertIfNecessary(propertyName, oldValue,
							newValue, requiredType);
					Array.set(propValue, arrayIndex, convertedValue);
				} catch (IllegalArgumentException ex) {
					PropertyChangeEvent pce = new PropertyChangeEvent(this.rootObject, this.nestPath + propertyName,
							oldValue, newValue);
					throw new TypeMismatchException(pce, requiredType, ex);
				} catch (ArrayIndexOutOfBoundsException ex) {
					throw new InvalidPropertyException(getRootClass(), this.nestPath + propertyName, "数组越界异常", ex);
				}
			}
			// 值为List
			else if (propValue instanceof List) {
				PropertyDescriptor pd = getPropertyDescriptorInternal(tokens.actualName);
				Class requiredType = GenericCollectionTypeResolver.getCollectionReturnType(pd.getReadMethod(),
						tokens.keys.length);
				List list = (List) propValue;
				int index = Integer.parseInt(key);
				Object oldValue = null;
				if (isExtractOldValueForEditor() && index < list.size()) {
					oldValue = list.get(index);
				}
				try {
					Object convertedValue = this.typeConverterDelegate.convertIfNecessary(propertyName, oldValue,
							newValue, requiredType);
					if (index < list.size()) {
						list.set(index, convertedValue);
					} else if (index >= list.size()) {
						for (int i = list.size(); i < index; i++) {
							try {
								list.add(null);
							} catch (NullPointerException ex) {
								throw new InvalidPropertyException(getRootClass(), this.nestPath + propertyName,
										"不支持null");
							}
						}
						list.add(convertedValue);
					}

				} catch (IllegalArgumentException ex) {
					PropertyChangeEvent pce = new PropertyChangeEvent(this.rootObject, this.nestPath + propertyName,
							oldValue, newValue);
					throw new TypeMismatchException(pce, requiredType, ex);
				}
			}
			// 值为Map
			else if (propValue instanceof Map) {
				PropertyDescriptor pd = getPropertyDescriptorInternal(tokens.actualName);
				Class mapKeyType = GenericCollectionTypeResolver.getMapKeyReturnType(pd.getReadMethod(),
						tokens.keys.length);
				Class mapValueType = GenericCollectionTypeResolver.getMapValueReturnType(pd.getReadMethod(),
						tokens.keys.length);
				Map map = (Map) propValue;
				Object oldValue = null;
				if (isExtractOldValueForEditor()) {
					oldValue = map.get(key);
				}
				Object convertedMapKey = null;
				Object convertedMapValue = null;
				try {
					// key转换成mapKeyType的类型
					convertedMapKey = this.typeConverterDelegate.convertIfNecessary(null, null, key, mapKeyType);
				} catch (IllegalArgumentException ex) {
					PropertyChangeEvent pce = new PropertyChangeEvent(this.rootObject, this.nestPath + propertyName,
							oldValue, newValue);
					throw new TypeMismatchException(pce, mapKeyType, ex);
				}
				try {
					// newValue转换成mapValueType的类型
					convertedMapValue = this.typeConverterDelegate.convertIfNecessary(propertyName, oldValue, newValue,
							mapValueType);
				} catch (IllegalArgumentException ex) {
					PropertyChangeEvent pce = new PropertyChangeEvent(this.rootObject, this.nestPath + propertyName,
							oldValue, newValue);
					throw new TypeMismatchException(pce, mapValueType, ex);
				}
				map.put(convertedMapKey, convertedMapValue);
			}
			// 其他类型
			else {
				throw new InvalidPropertyException(getRootClass(), this.nestPath + propertyName, "不支持的类型。");
			}
		}
		// 单对象
		else {
			PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
			if (pd == null || pd.getWriteMethod() == null) {
				PropertyMatches matches = PropertyMatches.forProperty(propertyName, getRootClass());
				throw new NotWritablePropertyException(getRootClass(), this.nestPath + propertyName,
						matches.buildErrorMessage(), matches.getPossibleMatches());
			}
			Method readMethod = pd.getReadMethod();
			Method writeMethod = pd.getWriteMethod();
			// 获取原值
			Object oldValue = null;
			if (isExtractOldValueForEditor() && readMethod != null) {
				if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
					readMethod.setAccessible(true);
				}
				try {
					readMethod.invoke(this.object, new Object[0]);
				} catch (Exception ex) {
					// 只出力log，不抛异常
					if (logger.isDebugEnabled()) {
						logger.debug("设定前的值取得失败。属性：" + this.nestPath + propertyName);
					}
				}
			}
			try {
				// 获取目标值转换后的值
				Object convertedValue = this.typeConverterDelegate.convertIfNecessary(oldValue, newValue, pd);
				if (pd.getPropertyType().isPrimitive() && (convertedValue == null || "".equals(convertedValue))) {
					throw new IllegalStateException("目标设定值为基本类型，不能设定NULL。属性：" + this.nestPath + propertyName);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("准备运用反射调用write方法。");
				}
				if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
					writeMethod.setAccessible(true);
				}
				writeMethod.invoke(this.object, new Object[] { convertedValue });
			} catch (IllegalAccessException ex) {
				PropertyChangeEvent pce = new PropertyChangeEvent(this.rootObject, this.nestPath + propertyName,
						oldValue, newValue);
				throw new MethodInvocationException(pce, ex);
			} catch (IllegalArgumentException ex) {
				PropertyChangeEvent pce = new PropertyChangeEvent(this.rootObject, this.nestPath + propertyName,
						oldValue, newValue);
				throw new TypeMismatchException(pce, pd.getPropertyType(), ex);
			} catch (InvocationTargetException ex) {
				PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this.rootObject, propertyName,
						oldValue, newValue);
				if (ex.getTargetException() instanceof ClassCastException) {
					throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), ex.getTargetException());
				} else {
					throw new MethodInvocationException(propertyChangeEvent, ex.getTargetException());
				}
			}
		}
	}

	public Class getPropertyType(String propertyName) throws BeansException {
		try {
			PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
			if (pd != null) {
				return pd.getPropertyType();
			} else {
				Object value = getPropertyValue(propertyName);
				if (value != null) {
					return value.getClass();
				}

				// 猜测自定义属性编辑器里可能有合适的类型
				Class editorType = guessPropertyTypeFromEditors(propertyName);
				if (editorType != null) {
					return editorType;
				}
			}
		} catch (InvalidPropertyException ex) {

		}
		return null;
	}

	private static class PropertyTokenHolder {
		private String canonicalName;
		private String actualName;
		private String[] keys;
	}

}
