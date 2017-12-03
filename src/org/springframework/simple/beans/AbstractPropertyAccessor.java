package org.springframework.simple.beans;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractPropertyAccessor extends PropertyEditorRegistrySupport
		implements ConfigurablePropertyAccessor {

	private boolean extractOldValueForEditor = false;

	public Class getPropertyType(String propertyPath) {
		return null;
	}

	@Override
	public boolean isExtractOldValueForEditor() {
		return this.extractOldValueForEditor;
	}

	@Override
	public void setExtractOldValueForEditor(boolean extractOldValueForEditor) {
		this.extractOldValueForEditor = extractOldValueForEditor;
	}

	@Override
	public abstract Object getPropertyValue(String propertyName) throws BeansException;

	@Override
	public abstract void setPropertyValue(String propertyName, Object value) throws BeansException;

	@Override
	public void setPropertyValue(PropertyValue pv) throws BeansException {
		setPropertyValue(pv.getName(), pv.getValue());
	}

	@Override
	public void setPropertyValues(Map map) throws BeansException {
		setPropertyValues(new MutablePropertyValues(map));
	}

	@Override
	public void setPropertyValues(PropertyValues pvs) throws BeansException {
		setPropertyValues(pvs, false, false);
	}

	@Override
	public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown) throws BeansException {
		setPropertyValues(pvs, ignoreUnknown, false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid)
			throws BeansException {

		// 把全部异常保存在一起。
		List propertyAccessExceptions = new LinkedList();
		PropertyValue[] pvArray = pvs.getPropertyValues();
		for (int i = 0; i < pvArray.length; i++) {
			try {
				setPropertyValue(pvArray[i]);
			} catch (NotWritablePropertyException ex) {
				if (!ignoreUnknown) {
					throw ex;
				}
			} catch (NullValueInNestedPathException ex) {
				if (!ignoreInvalid) {
					throw ex;
				}
			} catch (PropertyAccessException ex) {
				propertyAccessExceptions.add(ex);
			}
		}

		if (!propertyAccessExceptions.isEmpty()) {
			PropertyAccessException[] errArr = (PropertyAccessException[]) propertyAccessExceptions
					.toArray(new PropertyAccessException[propertyAccessExceptions.size()]);
			throw new PropertyBatchUpdateException(errArr);
		}
	}

}
