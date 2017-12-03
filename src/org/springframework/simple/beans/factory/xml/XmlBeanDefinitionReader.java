package org.springframework.simple.beans.factory.xml;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.simple.beans.BeanUtils;
import org.springframework.simple.beans.factory.BeanDefinitionStoreException;
import org.springframework.simple.beans.factory.parsing.EmptyReaderEventListener;
import org.springframework.simple.beans.factory.parsing.FailFastProblemReporter;
import org.springframework.simple.beans.factory.parsing.NullSourceExtracter;
import org.springframework.simple.beans.factory.parsing.ProblemReporter;
import org.springframework.simple.beans.factory.parsing.ReaderEventListener;
import org.springframework.simple.beans.factory.parsing.SourceExtracter;
import org.springframework.simple.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.simple.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.xml.SimpleSaxErrorHandler;
import org.springframework.util.xml.XmlValidationModeDetector;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {

	// 不验证
	public static final int VALIDATION_NONE = 0;

	// 验证
	public static final int VALIDATION_AUTO = 1;

	// DTD模式
	public static final int VALIDATION_DTD = XmlValidationModeDetector.VALIDATION_DTD;

	// XSD模式
	public static final int VALIDATION_XSD = XmlValidationModeDetector.VALIDATION_XSD;

	private XmlValidationModeDetector xmlValidationModeDetector = new XmlValidationModeDetector();

	private EntityResolver entityResolver;

	private DocumentLoader documentLoader = new DefaultDocumentLoader();

	// 默认为VALIDATION_AUTO
	private int validateMode = VALIDATION_AUTO;

	private boolean namespaceAware ;
	private ErrorHandler errorHandler = new SimpleSaxErrorHandler(logger);

	// ReaderContext相关
	private NamespaceHandlerResolver namespaceHandlerResolver;
	private ProblemReporter problemReproter = new FailFastProblemReporter();
	private SourceExtracter sourceExtracter = new NullSourceExtracter();
	private ReaderEventListener readerEventListener = new EmptyReaderEventListener();

	public XmlBeanDefinitionReader(BeanDefinitionRegistry beanfactory) {
		super(beanfactory);

		// 设定EntityResolver
		if (getResourceLoader() != null) {
			this.entityResolver = new ResourceEntityResolver(getResourceLoader());
		} else {
			this.entityResolver = new DelegatingEntityResolver(ClassUtils.getDefaultClassLoader());
		}
	}

	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	public EntityResolver getEntityResolver() {
		return this.entityResolver;
	}

	@Override
	public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
		return loadBeanDefinitions(new EncodedResource(resource));
	}

	public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
		Assert.notNull(encodedResource, "encodedResource不能为空。");
		try {
			InputStream inputStream = encodedResource.getResource().getInputStream();
			try {
				InputSource inputSource = new InputSource(inputStream);
				if (encodedResource.getEncoding() != null) {
					// 设定编码格式
					inputSource.setEncoding(encodedResource.getEncoding());
				}
				// 解析XML
				return doLoadBeanDefinitions(inputSource, encodedResource.getResource());
			} finally {
				inputStream.close();
			}
		} catch (IOException e) {
			throw new BeanDefinitionStoreException("解析XML失败。 " + encodedResource.getResource(), e);
		}
	}

	protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource)
			throws BeanDefinitionStoreException {

		// 获取验证Mode
		this.validateMode = getValidationModeForResource(resource);
		// 获取解析XML的Document对象
		Document doc;
		try {
			doc = this.documentLoader.loadDocument(inputSource, getEntityResolver(), this.errorHandler,
					this.validateMode, this.namespaceAware);
			// 解析DOC
			return registerBeanDefinitions(doc, resource);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new BeanDefinitionStoreException("Unexpected exception parsing XML document from " + resource, e);
		}

	}

	public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {

		// 真正解析XML用的是BeanDefinitionDocumentReader
		BeanDefinitionDocumentReader documentReader = CreatBeanDefinitionDocumentReader();
		int countBefore = this.getBeanFactory().getBeanDefinitionCount();

		// 用documentReader解析XML
		documentReader.registerBeanDefinitions(doc, CreateReaderContext(resource));

		return this.getBeanFactory().getBeanDefinitionCount() - countBefore;
	}

	public void setNamespaceHandlerResolver(NamespaceHandlerResolver namespaceHandlerResolver) {
		this.namespaceHandlerResolver = namespaceHandlerResolver;
	}

	protected XmlReaderContext CreateReaderContext(Resource resource) {
		NamespaceHandlerResolver resolver = this.namespaceHandlerResolver;
		if (resolver == null) {
			resolver = new DefaultNamespaceHandlerResolver(getResourceLoader().getClassLoader());
		}
		return new XmlReaderContext(resource, this.problemReproter, this.sourceExtracter, this.readerEventListener,
				this, resolver);
	}

	protected BeanDefinitionDocumentReader CreatBeanDefinitionDocumentReader() {
		return (BeanDefinitionDocumentReader) BeanUtils.instantiateClass(DefaultBeanDefinitionDocumentReader.class);
	}

	protected int detectValidateMode(Resource resource) {

		if (resource.isOpen()) {
			throw new BeanDefinitionStoreException("资源流处于打开中，无法完成验证。");
		}

		InputStream inputStream;
		try {
			inputStream = resource.getInputStream();
		} catch (IOException e) {
			throw new BeanDefinitionStoreException("获取资源流失败。 ");
		}

		try {
			// 内部发生异常时，会关闭资源流。
			return this.xmlValidationModeDetector.detectValidationMode(inputStream);
		} catch (IOException e) {
			throw new BeanDefinitionStoreException("获取验证模式失败。 ");
		}

	}

	private int getValidationModeForResource(Resource resource) {
		return (this.validateMode != VALIDATION_AUTO ? this.validateMode : detectValidateMode(resource));
	}

}