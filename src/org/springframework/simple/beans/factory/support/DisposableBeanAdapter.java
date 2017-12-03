package org.springframework.simple.beans.factory.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.simple.beans.factory.DisposableBean;
import org.springframework.util.Assert;

public class DisposableBeanAdapter implements DisposableBean, Runnable {

	private static final Log logger = LogFactory.getLog(DisposableBeanAdapter.class);
	private final Object bean;
	private final String beanName;
	private final RootBeanDefinition mergedBeanDefinition;
	private final List beanPostProcessors;

	public DisposableBeanAdapter(Object bean, String beanName, RootBeanDefinition mergedBeanDefinition,
			List beanPostProcessors) {
		Assert.notNull(bean, "bean 不能为空。");
		this.bean = bean;
		this.beanName = beanName;
		this.mergedBeanDefinition = mergedBeanDefinition;
		this.beanPostProcessors = beanPostProcessors;
	}

	@Override
	public void run() {
		destroy();
	}

	@Override
	public void destroy() {
		// 处理后处理器
		if (this.beanPostProcessors != null) {
			for (int i = beanPostProcessors.size(); i >= 0; i--) {
				Object postPostProcessor = beanPostProcessors.get(i);
				if (postPostProcessor instanceof DestructionAwareBeanPostProcessor) {
					((DestructionAwareBeanPostProcessor) postPostProcessor).postProcessBeforeDestruction(this.bean,
							this.beanName);
				}
			}
		}

		// 处理DisposalBean接口
		if (this.bean instanceof DisposableBean) {
			try {
				((DisposableBean) bean).destroy();
			} catch (Exception e) {
				logger.error("执行注销方法失败。" + this.beanName, e);
			}
		}

		// 处理Bean定义中自定义的注销方法
		invokeCustomDestroyMethod();
	}

	private void invokeCustomDestroyMethod() {

		String methodName = this.mergedBeanDefinition.getDestroyMethodName();
		if (methodName != null) {
			Method method = BeanUtils.findMethodWithMinimalParameters(this.bean.getClass(), methodName);
			if (method != null) {
				Class[] parameters = method.getParameterTypes();
				if (parameters.length > 1) {
					logger.error("不支持多个参数。");
				} else if (parameters.length == 1 && (!parameters[0].equals(boolean.class))) {
					logger.error("只支持boolean参数");
				} else {
					Object[] args = new Object[parameters.length];
					if (parameters.length == 1) {
						args[0] = Boolean.TRUE;
					}
					if (!Modifier.isPublic(method.getModifiers())) {
						method.setAccessible(true);
					}
					try {
						method.invoke(this.bean, args);
					} catch (InvocationTargetException e) {
						logger.error("注销方法执行出错。");
					} catch (Throwable ex) {
						logger.error("注销方法执行出错。");
					}
				}
			}
		}
	}

}
