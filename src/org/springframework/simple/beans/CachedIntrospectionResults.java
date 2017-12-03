package org.springframework.simple.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

final class CachedIntrospectionResults {

	private static final Log logger = LogFactory.getLog(CachedIntrospectionResults.class);

	/**
	 * Map keyed by class containing CachedIntrospectionResults. Needs to be a
	 * WeakHashMap with WeakReferences as values to allow for proper garbage
	 * collection in case of multiple class loaders.
	 */
	private static final Map classCache = Collections.synchronizedMap(new WeakHashMap());

	/**
	 * Create CachedIntrospectionResults for the given bean class.
	 * <P>
	 * We don't want to use synchronization here. Object references are atomic, so
	 * we can live with doing the occasional unnecessary lookup at startup only.
	 * 
	 * @param beanClass
	 *            the bean class to analyze
	 */
	public static CachedIntrospectionResults forClass(Class beanClass) throws BeansException {
		CachedIntrospectionResults results = null;
		Object value = classCache.get(beanClass);
		if (value instanceof Reference) {
			Reference ref = (Reference) value;
			results = (CachedIntrospectionResults) ref.get();
		} else {
			results = (CachedIntrospectionResults) value;
		}
		if (results == null) {
			// can throw BeansException
			results = new CachedIntrospectionResults(beanClass);
			boolean cacheSafe = isCacheSafe(beanClass);
			if (logger.isDebugEnabled()) {
				logger.debug("Class [" + beanClass.getName() + "] is " + (!cacheSafe ? "not " : "") + "cache-safe");
			}
			if (cacheSafe) {
				classCache.put(beanClass, results);
			} else {
				classCache.put(beanClass, new WeakReference(results));
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Using cached introspection results for class [" + beanClass.getName() + "]");
			}
		}
		return results;
	}

	/**
	 * Check whether the given class is cache-safe, i.e. whether it is loaded by the
	 * same class loader as the CachedIntrospectionResults class or a parent of it.
	 * <p>
	 * Many thanks to Guillaume Poirier for pointing out the garbage collection
	 * issues and for suggesting this solution.
	 * 
	 * @param clazz
	 *            the class to analyze
	 * @return whether the given class is thread-safe
	 */
	private static boolean isCacheSafe(Class clazz) {
		ClassLoader cur = CachedIntrospectionResults.class.getClassLoader();
		ClassLoader target = clazz.getClassLoader();
		if (target == null || cur == target) {
			return true;
		}
		while (cur != null) {
			cur = cur.getParent();
			if (cur == target) {
				return true;
			}
		}
		return false;
	}

	private final BeanInfo beanInfo;

	/** Property descriptors keyed by property name */
	private final Map propertyDescriptorCache;

	/**
	 * Create a new CachedIntrospectionResults instance for the given class.
	 */
	private CachedIntrospectionResults(Class clazz) throws BeansException {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Getting BeanInfo for class [" + clazz.getName() + "]");
			}
			this.beanInfo = Introspector.getBeanInfo(clazz);

			// Immediately remove class from Introspector cache, to allow for proper
			// garbage collection on class loader shutdown - we cache it here anyway,
			// in a GC-friendly manner. In contrast to CachedIntrospectionResults,
			// Introspector does not use WeakReferences as values of its WeakHashMap!
			Class classToFlush = clazz;
			do {
				Introspector.flushFromCaches(classToFlush);
				classToFlush = classToFlush.getSuperclass();
			} while (classToFlush != null);

			if (logger.isDebugEnabled()) {
				logger.debug("Caching PropertyDescriptors for class [" + clazz.getName() + "]");
			}
			this.propertyDescriptorCache = new HashMap();

			// This call is slow so we do it once.
			PropertyDescriptor[] pds = this.beanInfo.getPropertyDescriptors();
			for (int i = 0; i < pds.length; i++) {
				PropertyDescriptor pd = pds[i];
				if (logger.isDebugEnabled()) {
					logger.debug("Found bean property '" + pd.getName() + "'"
							+ (pd.getPropertyType() != null ? " of type [" + pd.getPropertyType().getName() + "]" : "")
							+ (pd.getPropertyEditorClass() != null
									? "; editor [" + pd.getPropertyEditorClass().getName() + "]"
									: ""));
				}
				this.propertyDescriptorCache.put(pd.getName(), pd);
			}
		} catch (IntrospectionException ex) {
			throw new FatalBeanException("Cannot get BeanInfo for object of class [" + clazz.getName() + "]", ex);
		}
	}

	public BeanInfo getBeanInfo() {
		return this.beanInfo;
	}

	public Class getBeanClass() {
		return this.beanInfo.getBeanDescriptor().getBeanClass();
	}

	public PropertyDescriptor getPropertyDescriptor(String propertyName) {
		return (PropertyDescriptor) this.propertyDescriptorCache.get(propertyName);
	}

}
