package persistence.metadata.processor;

import persistence.metadata.MetadataProcessor;
import persistence.metadata.model.EntityMetadata;
import persistence.event.CallbackMethod;
import persistence.event.ExternalCallbackMethod;
import persistence.event.InternalCallbackMethod;
import util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityListeners;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * not implemented
 * 
 * @author weihang chen
 * 
 */
public class EntityListenersProcessor implements MetadataProcessor {
	private static Log log = LogFactory.getLog(EntityListenersProcessor.class);

	@SuppressWarnings("unchecked")
	private static final List<Class> JPAListenersAnnotations = Arrays
			.asList(new Class[] { PrePersist.class, PostPersist.class,
					PreUpdate.class, PostUpdate.class, PreRemove.class,
					PostRemove.class, PostLoad.class });

	@SuppressWarnings("unchecked")
	public final void process(Class<?> entityClass, EntityMetadata metadata) {
		EntityListeners entityListeners = entityClass
				.getAnnotation(EntityListeners.class);
		if (entityListeners != null) {
			Class[] entityListenerClasses = entityListeners.value();
			if (entityListenerClasses != null) {
				for (Class entityListener : entityListenerClasses) {
					try {
						entityListener.getConstructor(new Class[0]);
					} catch (NoSuchMethodException nsme) {
						throw new RuntimeException(
								"Skipped method("
										+ entityListener.getName()
										+ ") must have a default no-argument constructor.");
					}

					for (Method method : entityListener.getDeclaredMethods()) {
						List<Class> jpaAnnotations = getValidJPAAnnotationsFromMethod(
								entityListener, method, 1);

						for (Class jpaAnnotation : jpaAnnotations) {
							CallbackMethod callBackMethod = new ExternalCallbackMethod(
									entityListener, method);
							addCallBackMethod(metadata, jpaAnnotation,
									callBackMethod);
						}
					}
				}
			}
		}
		for (Method method : entityClass.getDeclaredMethods()) {
			List<Class> jpaAnnotations = getValidJPAAnnotationsFromMethod(
					entityClass, method, 0);

			for (Class jpaAnnotation : jpaAnnotations) {
				CallbackMethod callbackMethod = new InternalCallbackMethod(
						metadata, method);
				addCallBackMethod(metadata, jpaAnnotation, callbackMethod);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void addCallBackMethod(EntityMetadata metadata,
			Class<?> jpaAnnotation, CallbackMethod callbackMethod) {
		Map<Class, List> callBackMethodsMap = metadata.getCallbackMethodsMap();
		List<CallbackMethod> list = callBackMethodsMap.get(jpaAnnotation);
		if (null == list) {
			list = new ArrayList<CallbackMethod>();
			callBackMethodsMap.put(jpaAnnotation, list);
		}
		list.add(callbackMethod);
	}

	@SuppressWarnings("unchecked")
	private List<Class> getValidJPAAnnotationsFromMethod(Class<?> clazz,
			Method method, int numberOfParams) {
		List<Class> annotations = new ArrayList<Class>();

		for (Annotation methodAnnotation : method.getAnnotations()) {
			Class methodAnnotationType = methodAnnotation.annotationType();

			if (!(isValidJPAEntityListenerAnnotation(methodAnnotationType))) {
				continue;
			}

			boolean hasUncheckedExceptions = false;
			for (Class exception : method.getExceptionTypes()) {
				if (ReflectionUtils.hasSuperClass(RuntimeException.class,
						exception))
					continue;
				hasUncheckedExceptions = true;
				break;
			}

			if (hasUncheckedExceptions) {
				log.info("Skipped method(" + clazz.getName() + "."
						+ method.getName()
						+ ") Must not throw unchecked exceptions.");
			} else if (!(method.getReturnType().getSimpleName().equals("void"))) {
				log.info("Skipped method(" + clazz.getName() + "."
						+ method.getName()
						+ ") Must have \"void\" return type.");
			} else {
				Class[] paramTypes = method.getParameterTypes();
				if (paramTypes.length != numberOfParams) {
					log.info("Skipped method(" + clazz.getName() + "."
							+ method.getName() + ") Must have "
							+ numberOfParams + " parameter.");
				} else if (numberOfParams == 1) {
					Class parameter = paramTypes[0];
					if (!(parameter.getName().equals("java.lang.Object"))) {
						log
								.info("Skipped method("
										+ clazz.getName()
										+ "."
										+ method.getName()
										+ ") Must have only 1 \"Object\" type parameter.");
					}

				} else {
					annotations.add(methodAnnotationType);
				}
			}
		}
		return annotations;
	}

	private boolean isValidJPAEntityListenerAnnotation(Class<?> annotation) {
		return JPAListenersAnnotations.contains(annotation);
	}
}
