package persistence.metadata.model;

import persistence.annotation.support.JavaBeanFactory;
import persistence.proxy.EntityEnhancerFactory;

/**
 * @author weihang chen
 */
public class CoreMetadata {
	/**
	 * not used
	 */
	private EntityEnhancerFactory enhancedProxyFactory;
	/**
	 * main class to create instance of Domino entity
	 */
	private JavaBeanFactory javaBeanFactory;

	public EntityEnhancerFactory getEnhancedProxyFactory() {
		return this.enhancedProxyFactory;
	}

	public void setEnhancedProxyFactory(
			EntityEnhancerFactory enhancedProxyFactory) {
		this.enhancedProxyFactory = enhancedProxyFactory;
	}

	public JavaBeanFactory getJavaBeanFactory() {
		return javaBeanFactory;
	}

	public void setJavaBeanFactory(JavaBeanFactory javaBeanFactory) {
		this.javaBeanFactory = javaBeanFactory;
	}

}
