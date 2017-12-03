package pojo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

public class JunitTest {

	private final Log logger = LogFactory.getLog(getClass());

	@Test
	public void testBeanReal() {
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(factory);
		xmlReader.loadBeanDefinitions("test.xml");
		Bean1 b1 = (Bean1) factory.getBean("bean1");
		System.out.println("name:" + b1.getName() + ",age:" + b1.getAge());
		System.out.println("name:" + b1.getBean().getName() + ",age:" + b1.getBean().getAge());
	}

	@Test
	public void testBeanSimple() {
		org.springframework.simple.beans.factory.support.DefaultListableBeanFactory factory = new org.springframework.simple.beans.factory.support.DefaultListableBeanFactory();
		org.springframework.simple.beans.factory.xml.XmlBeanDefinitionReader reader = new org.springframework.simple.beans.factory.xml.XmlBeanDefinitionReader(
				factory);
		reader.loadBeanDefinitions("test.xml");
		Bean1 b1 = (Bean1) factory.getBean("bean1");
		// Bean2 b2 = (Bean2)factory.getBean("bean2");
		System.out.println("name:" + b1.getName() + ",age:" + b1.getAge());
		System.out.println("name:" + b1.getBean().getName() + ",age:" + b1.getBean().getAge());
	}
}
