package org.springframework.simple.beans.factory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.simple.beans.BeansException;
import org.springframework.util.Assert;

public abstract class BeanFactoryUtils {

	public static String transformedBeanName(String beanName) {
		Assert.notNull(beanName, "beanName 不能为空。");
		String result = beanName;
		if (result.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
			result = result.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
		}
		return result;
	}

	public static boolean isFactoryDereference(String name) {
		return name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX);
	}

	public static Map beansOfTypeIncludingAncestors(ListableBeanFactory lbf, Class type) throws BeansException {
		Assert.notNull(lbf, "ListableBeanFactory不能为空。");
		Map result = new HashMap();
		result.putAll(lbf.getBeanOfType(type));
		// 继续查找父工厂
		if (lbf instanceof HierarchicalBeanFactory) {
			HierarchicalBeanFactory hbf = (HierarchicalBeanFactory) lbf;
			if (hbf.getParentBeanFactory() instanceof ListableBeanFactory) {
				ListableBeanFactory parentFactory = (ListableBeanFactory) hbf.getParentBeanFactory();
				// 递归
				Map parentResult = beansOfTypeIncludingAncestors(parentFactory, type);
				// 去重
				for (Iterator it = parentResult.entrySet().iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry) it.next();
					String beanName = (String) entry.getKey();
					if (!result.containsKey(beanName) && !hbf.containLocalBean(beanName)) {
						result.put(beanName, entry.getValue());
					}
				}
			}
		}
		return result;
	}
}
