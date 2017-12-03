package org.springframework.simple.beans;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.CollectionFactory;
import org.springframework.core.GenericCollectionTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

class TypeConverterDelegate {

	private static final Log logger = LogFactory.getLog(TypeConverterDelegate.class);

	private final PropertyEditorRegistrySupport propertyEditorRegistry;
	private final Object targetObject;

	public TypeConverterDelegate(PropertyEditorRegistrySupport propertyEditorRegistry) {
		this(propertyEditorRegistry, null);
	}

	public TypeConverterDelegate(PropertyEditorRegistrySupport propertyEditorRegistry, Object targetObject) {
		Assert.notNull(propertyEditorRegistry, "propertyEditorRegistry不能为空。");
		this.propertyEditorRegistry = propertyEditorRegistry;
		this.targetObject = targetObject;
	}

	public Object convertIfNecessary(Object newValue, Class requiredType, MethodParameter methodParameter)
			throws IllegalStateException {
		return convertIfNecessary(null, null, newValue, requiredType, null, methodParameter);
	}

	public Object convertIfNecessary(String propertyName, Object oldValue, Object newValue, Class requiredType)
			throws IllegalStateException {
		return convertIfNecessary(propertyName, oldValue, newValue, requiredType, null, null);
	}

	public Object convertIfNecessary(Object oldValue, Object newValue, PropertyDescriptor descriptor)
			throws IllegalStateException {
		Assert.notNull(descriptor, "descriptor不能为空。");
		return convertIfNecessary(descriptor.getName(), oldValue, newValue, descriptor.getPropertyType(), descriptor,
				new MethodParameter(descriptor.getWriteMethod(), 0));
	}

	protected Object convertIfNecessary(String propertyName, Object oldValue, Object newValue, Class requiredType,
			PropertyDescriptor descriptor, MethodParameter methodParameter) throws IllegalStateException {

		Object convertedValue = newValue;

		// 自定义属性编辑器
		PropertyEditor pe = this.propertyEditorRegistry.findCustomEditor(requiredType, propertyName);

		// 需要转换成spring不支持，用户自定义的转换类型
		if (pe != null || (requiredType != null && !ClassUtils.isAssignableValue(requiredType, convertedValue))) {
			if (pe == null && descriptor != null) {
				// 尝试从属性描述器创建属性编辑器
				pe = descriptor.createPropertyEditor(this.targetObject);
			}
			if (pe == null && requiredType != null) {
				// 从属性描述器创建失败，尝试从默认编辑器里找
				pe = this.propertyEditorRegistry.getDefaultEditor(requiredType);
				if (pe == null) {
					// 仍然失败，则从JDK找
					pe = PropertyEditorManager.findEditor(requiredType);
				}
			}
			convertedValue = convertValue(convertedValue, requiredType, pe, oldValue);
		}

		// spring支持的转换(经过前面的处理，已经是Spring能够转换的类型了)
		if (requiredType != null) {
			// 数组
			if (convertedValue != null && requiredType.isArray()) {
				return convertToTypedArray(convertedValue, propertyName, requiredType);
			} else if (convertedValue instanceof Collection && Collection.class.isAssignableFrom(requiredType)) {
				// 集合类型
				return convertToTypedCollection((Collection) convertedValue, propertyName, methodParameter);
			} else if (convertedValue instanceof Map && Map.class.isAssignableFrom(requiredType)) {
				// Map
				return convertToTypedMap((Map) convertedValue, propertyName, methodParameter);
			} else if (convertedValue instanceof String && !requiredType.isInstance(convertedValue)) {
				// String
				try {
					Field enumField = requiredType.getField((String) convertedValue);
					convertedValue = enumField.get(null);
				} catch (Exception ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("Field [" + convertedValue + "] isn't an enum value", ex);
					}
				}
			}

			if (!ClassUtils.isAssignableValue(requiredType, convertedValue)) {
				throw new IllegalStateException("没有找到合适的转换器。");
			}
		}

		return convertedValue;
	}

	protected Object convertToTypedMap(Map original, String propertyName, MethodParameter methodParam) {
		Class keyType = null;
		Class valueType = null;
		if (methodParam != null) {
			keyType = GenericCollectionTypeResolver.getMapKeyParameterType(methodParam);
			valueType = GenericCollectionTypeResolver.getMapValueParameterType(methodParam);
		}
		Map convertedCopy = null;
		Iterator it = null;
		try {
			convertedCopy = CollectionFactory.createApproximateMap(original.getClass(), original.size());
			it = original.entrySet().iterator();
		} catch (Throwable ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("访问Map属性失败。");
			}
			return original;
		}
		boolean actuallyConverted = false;
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			String keyedPropertyName = buildKeyedPropertyName(propertyName, key);
			Object convertedKey = convertIfNecessary(keyedPropertyName, null, key, keyType);
			Object convertedValue = convertIfNecessary(keyedPropertyName, null, value, valueType);
			convertedCopy.put(convertedKey, convertedValue);
			actuallyConverted = actuallyConverted || (key != convertedKey) || (value != convertedValue);
		}
		return (actuallyConverted ? convertedCopy : original);
	}

	protected Object convertToTypedCollection(Collection original, String propertyName, MethodParameter methodParam) {
		Class elmentType = null;
		if (methodParam != null) {
			// 获取Collection元素类型
			elmentType = GenericCollectionTypeResolver.getCollectionParameterType(methodParam);
		}
		Collection convertedCopy = null;
		Iterator it = null;
		try {
			convertedCopy = CollectionFactory.createApproximateCollection(original.getClass(), original.size());
			it = original.iterator();
		} catch (Throwable ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("无法便利集合的元素。");
			}
			return original;
		}
		// 是否转换成功Flg
		boolean actuallyConverted = false;
		for (int i = 0; it.hasNext(); i++) {
			Object element = it.next();
			String indexPropertyName = buildIndexedPropertyName(propertyName, i);
			Object convertedItem = this.convertIfNecessary(indexPropertyName, null, element, elmentType);
			convertedCopy.add(convertedItem);
			actuallyConverted = actuallyConverted || (convertedItem != element);
		}
		return (actuallyConverted ? convertedCopy : original);
	}

	protected Object convertToTypedArray(Object input, String propertyName, Class componentType) {
		if (input instanceof Collection) {
			// 集合类型
			Collection coll = (Collection) input;
			Object result = Array.newInstance(componentType, coll.size());
			int i = 0;
			for (Iterator it = coll.iterator(); it.hasNext(); i++) {
				Object convertedItem = convertIfNecessary(buildIndexedPropertyName(propertyName, i), null, it.next(),
						componentType);
				Array.set(result, i, convertedItem);
			}
			return result;
		} else if (input.getClass().isArray()) {
			// 数组
			int arraylen = Array.getLength(input);
			Object result = Array.newInstance(componentType, arraylen);
			for (int i = 0; i < arraylen; i++) {
				Object convertedItem = convertIfNecessary(buildIndexedPropertyName(propertyName, i), null,
						Array.get(input, i), componentType);
				Array.set(result, i, convertedItem);
			}
			return result;
		} else {
			// 单类型
			// 单个转数组
			Object result = Array.newInstance(componentType, 1);
			Object convertedItem = convertIfNecessary(buildIndexedPropertyName(propertyName, 0), null, input,
					componentType);
			Array.set(result, 0, convertedItem);
			return result;
		}
	}

	private String buildIndexedPropertyName(String propertyName, int index) {
		return (propertyName != null
				? propertyName + PropertyAccessor.PROPERTY_KEY_PREFIX + index + PropertyAccessor.PROPERTY_KEY_SUFFIX
				: null);
	}

	private String buildKeyedPropertyName(String propertyName, Object key) {
		return (propertyName != null
				? propertyName + PropertyAccessor.PROPERTY_KEY_PREFIX + key + PropertyAccessor.PROPERTY_KEY_SUFFIX
				: null);
	}

	protected Object convertValue(Object newValue, Class requiredType, PropertyEditor pe, Object oldValue) {
		Object convertedValue = newValue;

		if (pe != null && !(convertedValue instanceof String)) {
			pe.setValue(convertedValue);
			Object newConvertedValue = pe.getValue();
			if (newConvertedValue != convertedValue) {
				convertedValue = newConvertedValue;
				// 防止后面的分支继续用
				pe = null;
			}
		}

		// String数组
		if (requiredType != null && !requiredType.isArray() && convertedValue instanceof String[]) {
			convertedValue = StringUtils.arrayToCommaDelimitedString((String[]) convertedValue);
		}

		// 继续处理
		if (pe != null && convertedValue instanceof String) {
			pe.setValue(oldValue);
			pe.setAsText((String) convertedValue);
			convertedValue = pe.getValue();
		}

		return convertedValue;
	}

}
