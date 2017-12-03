package org.springframework.simple.beans;

import java.beans.PropertyEditor;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;

import org.springframework.core.CollectionFactory;
import org.springframework.simple.beans.factory.propertyeditors.ByteArrayPropertyEditor;
import org.springframework.simple.beans.factory.propertyeditors.CharArrayPropertyEditor;
import org.springframework.simple.beans.factory.propertyeditors.CharacterEditor;
import org.springframework.simple.beans.factory.propertyeditors.ClassArrayEditor;
import org.springframework.simple.beans.factory.propertyeditors.ClassEditor;
import org.springframework.simple.beans.factory.propertyeditors.CustomBooleanEditor;
import org.springframework.simple.beans.factory.propertyeditors.CustomCollectionEditor;
import org.springframework.simple.beans.factory.propertyeditors.CustomNumberEditor;
import org.springframework.simple.beans.factory.propertyeditors.FileEditor;
import org.springframework.simple.beans.factory.propertyeditors.InputStreamEditor;
import org.springframework.simple.beans.factory.propertyeditors.LocaleEditor;
import org.springframework.simple.beans.factory.propertyeditors.PropertiesEditor;
import org.springframework.simple.beans.factory.propertyeditors.URLEditor;
import org.springframework.util.ClassUtils;

public class PropertyEditorRegistrySupport implements PropertyEditorRegistry {

	private Map defaultEditors;
	private Map customEditors;
	private Map customEditorCathe;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void registerDefaultEditors() {
		this.defaultEditors = new HashMap(32);

		// 类以及资源类型
		this.defaultEditors.put(Class.class, new ClassEditor());
		this.defaultEditors.put(Class[].class, new ClassArrayEditor());
		this.defaultEditors.put(File.class, new FileEditor());
		this.defaultEditors.put(InputStream.class, new InputStreamEditor());
		this.defaultEditors.put(Locale.class, new LocaleEditor());
		this.defaultEditors.put(Properties.class, new PropertiesEditor());
		this.defaultEditors.put(URL.class, new URLEditor());

		// 集合类型
		this.defaultEditors.put(Collection.class, new CustomCollectionEditor(Collection.class));
		this.defaultEditors.put(List.class, new CustomCollectionEditor(List.class));
		this.defaultEditors.put(Set.class, new CustomCollectionEditor(Set.class));
		this.defaultEditors.put(SortedSet.class, new CustomCollectionEditor(SortedSet.class));

		// 基础类型数组
		this.defaultEditors.put(char[].class, new CharArrayPropertyEditor());
		this.defaultEditors.put(Byte[].class, new ByteArrayPropertyEditor());

		// 字符型
		this.defaultEditors.put(char.class, new CharacterEditor(false));
		this.defaultEditors.put(Character.class, new CharacterEditor(true));

		// boolean
		this.defaultEditors.put(Boolean.class, new CustomBooleanEditor(true));
		this.defaultEditors.put(boolean.class, new CustomBooleanEditor(false));

		// number型
		this.defaultEditors.put(byte.class, new CustomNumberEditor(Byte.class, false));
		this.defaultEditors.put(Byte.class, new CustomNumberEditor(Byte.class, true));
		this.defaultEditors.put(short.class, new CustomNumberEditor(Short.class, false));
		this.defaultEditors.put(Short.class, new CustomNumberEditor(Short.class, true));
		this.defaultEditors.put(int.class, new CustomNumberEditor(Integer.class, false));
		this.defaultEditors.put(Integer.class, new CustomNumberEditor(Integer.class, true));
		this.defaultEditors.put(long.class, new CustomNumberEditor(Long.class, false));
		this.defaultEditors.put(Long.class, new CustomNumberEditor(Long.class, true));
		this.defaultEditors.put(float.class, new CustomNumberEditor(Float.class, false));
		this.defaultEditors.put(Float.class, new CustomNumberEditor(Float.class, true));
		this.defaultEditors.put(double.class, new CustomNumberEditor(Double.class, false));
		this.defaultEditors.put(Double.class, new CustomNumberEditor(Double.class, true));
		this.defaultEditors.put(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, true));
		this.defaultEditors.put(BigInteger.class, new CustomNumberEditor(BigInteger.class, true));

	}

	@SuppressWarnings("rawtypes")
	protected PropertyEditor getDefaultEditor(Class requiredType) {
		if (this.defaultEditors == null) {
			return null;
		}
		return (PropertyEditor) this.defaultEditors.get(requiredType);
	}

	protected void copyDefaultEditorTo(PropertyEditorRegistrySupport another) {
		another.defaultEditors = this.defaultEditors;
	}

	@Override
	public void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor) {
		registerCustomEditor(requiredType, null, propertyEditor);
	}
	protected void copyCustomEditorsTo(PropertyEditorRegistry target, String nestedProperty) {
		String actualPropertyName =
				(nestedProperty != null ? PropertyAccessorUtils.getPropertyName(nestedProperty) : null);
		if (this.customEditors != null) {
			for (Iterator it = this.customEditors.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				if (entry.getKey() instanceof Class) {
					Class requiredType = (Class) entry.getKey();
					PropertyEditor editor = (PropertyEditor) entry.getValue();
					target.registerCustomEditor(requiredType, editor);
				}
				else if (entry.getKey() instanceof String & nestedProperty != null) {
					String editorPath = (String) entry.getKey();
					int pos = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(editorPath);
					if (pos != -1) {
						String editorNestedProperty = editorPath.substring(0, pos);
						String editorNestedPath = editorPath.substring(pos + 1);
						if (editorNestedProperty.equals(nestedProperty) || editorNestedProperty.equals(actualPropertyName)) {
							CustomEditorHolder editorHolder = (CustomEditorHolder) entry.getValue();
							target.registerCustomEditor(
									editorHolder.getRegisteredType(), editorNestedPath, editorHolder.getPropertyEditor());
						}
					}
				}
			}
		}
	}

	// 主要用于给数组的元素注册属性编辑器时使用，例如：数组Foo[n].aaa,参数propertyPath应该指定为Foo.aaa
	@SuppressWarnings("unchecked")
	@Override
	public void registerCustomEditor(Class requiredType, String propertyPath, PropertyEditor propertyEditor) {
		if (requiredType == null && propertyPath == null) {
			throw new IllegalArgumentException("Either requiredType or propertyPath is required");
		}

		if (this.customEditors == null) {
			this.customEditors = CollectionFactory.createLinkedMapIfPossible(16);
		}

		if (propertyPath != null) {
			this.customEditors.put(propertyPath, new CustomEditorHolder(requiredType, propertyEditor));
		} else {
			this.customEditors.put(requiredType, propertyEditor);
			this.customEditorCathe = null;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public PropertyEditor findCustomEditor(Class requiredType, String propertyPath) {
		if (this.customEditors == null) {
			return null;
		}

		// 从CustomEditorHolder里取
		if (propertyPath != null) {
			PropertyEditor editor = getCustomEditor(propertyPath, requiredType);
			if (editor == null) {
				List strippedPaths = new LinkedList();
				// 把数组符号给过滤掉
				addStrippedPropertyPaths(strippedPaths, "", propertyPath);
				for (Iterator it = strippedPaths.iterator(); it.hasNext() && editor == null;) {
					String strippedPath = (String) it.next();
					editor = getCustomEditor(strippedPath, requiredType);
				}
			}
			if (editor != null) {
				return editor;
			} else if (requiredType == null) {
				requiredType = getPropertyType(propertyPath);
			}
		}

		// 从MAP里取
		return getCustomEditor(requiredType);
	}

	protected Class getPropertyType(String propertyPath) {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void addStrippedPropertyPaths(List strippedPaths, String nestedPath, String propertyPath) {
		int startIndex = propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR);
		if (startIndex != -1) {
			int endIndex = propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR);
			if (endIndex != -1) {
				String prefix = propertyPath.substring(0, startIndex);
				String key = propertyPath.substring(startIndex, endIndex + 1);
				String suffix = propertyPath.substring(endIndex + 1, propertyPath.length());
				strippedPaths.add(nestedPath + prefix + suffix);
				addStrippedPropertyPaths(strippedPaths, nestedPath + prefix, suffix);
				addStrippedPropertyPaths(strippedPaths, nestedPath + prefix + key, suffix);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private PropertyEditor getCustomEditor(Class requiredType) {
		if (requiredType == null) {
			return null;
		}
		PropertyEditor editor = (PropertyEditor) this.customEditors.get(requiredType);
		if (editor == null) {
			// 直接找，找不到时先在缓存里找
			if (this.customEditorCathe != null) {
				editor = (PropertyEditor) this.customEditorCathe.get(requiredType);
			}
			if (editor == null) {
				// 缓存里找不到，就在已经注册的所有编辑器里查找是都存在父类的编辑器
				for (Iterator it = this.customEditors.keySet().iterator(); it.hasNext() && editor == null;) {
					Object key = it.next();
					if ((key instanceof Class) && ((Class) key).isAssignableFrom(requiredType)) {
						editor = (PropertyEditor) this.customEditors.get(key);
						// 放到直接找不到对象的缓存里，避免下次再找
						if (this.customEditorCathe == null) {
							this.customEditorCathe = new HashMap();
						}
						this.customEditorCathe.put(requiredType, editor);
					}
				}
			}
		}

		return editor;
	}

	private PropertyEditor getCustomEditor(String propertyPath, Class requiredType) {
		CustomEditorHolder holder = (CustomEditorHolder) this.customEditors.get(propertyPath);
		return (holder != null ? holder.getPropertyEditor(requiredType) : null);
	}

	@SuppressWarnings("rawtypes")
	protected Class guessPropertyTypeFromEditors(String propertyName) {
		if (this.customEditors != null) {

			CustomEditorHolder holder = (CustomEditorHolder) this.customEditors.get(propertyName);
			if (holder == null) {
				List strippedPaths = new LinkedList();
				addStrippedPropertyPaths(strippedPaths, "", propertyName);
				for (Iterator it = strippedPaths.iterator(); it.hasNext() && holder == null;) {
					String strippedName = (String) it.next();
					holder = (CustomEditorHolder) this.customEditors.get(strippedName);
				}
			}
			if (holder != null) {
				return holder.getRegisteredType();
			}
		}
		return null;
	}
	
	

	private static class CustomEditorHolder {
		private final Class registeredType;
		private final PropertyEditor propertyEditor;

		private CustomEditorHolder(Class registeredType, PropertyEditor propertyEditor) {
			this.registeredType = registeredType;
			this.propertyEditor = propertyEditor;
		}

		public Class getRegisteredType() {
			return registeredType;
		}

		public PropertyEditor getPropertyEditor() {
			return propertyEditor;
		}

		private PropertyEditor getPropertyEditor(Class requiredType) {
			// Special case: If no required type specified, which usually only happens for
			// Collection elements, or required type is not assignable to registered type,
			// which usually only happens for generic properties of type Object -
			// then return PropertyEditor if not registered for Collection or array type.
			// (If not registered for Collection or array, it is assumed to be intended
			// for elements.)
			if (this.registeredType == null
					|| (requiredType != null && (ClassUtils.isAssignable(this.registeredType, requiredType)
							|| ClassUtils.isAssignable(requiredType, this.registeredType)))
					|| (requiredType == null && (!Collection.class.isAssignableFrom(this.registeredType)
							&& !this.registeredType.isArray()))) {
				return this.propertyEditor;
			} else {
				return null;
			}
		}

	}

}
