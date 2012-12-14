package persistence.annotation.support;

import java.lang.reflect.Constructor;

import net.sf.cglib.proxy.Enhancer;
import model.notes.ModelBase;

/**
 * all new instances of Domino entity need to be created using this class, so
 * the result entities become enhanced, that all the methods become intercepted
 * 
 * @author weihang chen
 */

public class JavaBeanFactory {

	public static <T> T getProxy(Class<T> clazz) {
		return getInstance(clazz, null);
	}

	/**
	 * create a new ModelBase entity using reflection, constructor in use as following
	 * <p>
	 * <code>protected ModelBase(Object doc) {
	 * this.doc = initDoc(doc); }</code>
	 * <p>
	 * then use CGLib <code>net.sf.cglib.proxy.Enhancer</code> to create a enhanced copy of original entity, assign the correct unid to the copy and return the copy 
	 * 
	 * <a href="http://agapple.iteye.com/blog/799827">CGLib Tutorial</a>,
	 * <a href="http://www.techavalanche.com/2011/08/24/understanding-java-dynamic-proxy/">Dynamic Proxy1</a>,
	 * <a href="http://www.ibm.com/developerworks/java/library/j-jtp08305/index.html">Dynamic Proxy2</a>
	 * 
	 * @param <T> generic type
	 * @param clazz Class
	 * @param dominoDoc DominoDocument instance or null
	 * @return enhanced Domino entity
	 */
	
	@SuppressWarnings("unchecked")
	public static <T> T getInstance(Class<T> clazz, Object dominoDoc) {
		try {
			Constructor con = clazz
					.getConstructor(new Class[] { Object.class });
			T object1 = (T) con.newInstance(new Object[] { dominoDoc });
			if (!(object1 instanceof ModelBase))
				return null;

			Object result = null;
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(clazz);
			JavaBeanInterceptor interceptor = new JavaBeanInterceptor();
			interceptor.setTarget(object1);
			enhancer.setCallback(interceptor);

			// !!IMPORTANT CGLIB WILL CREATE A SHADOW INSTANCE
			// AND UNID GETS RECALCULATED, need to reset the id after
			// creating an interceptor
			String originalUNID = ((ModelBase) object1).getUnid();
			result = enhancer.create();
			((ModelBase) result).setUnid(originalUNID);

			return (T) result;

		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}
}
