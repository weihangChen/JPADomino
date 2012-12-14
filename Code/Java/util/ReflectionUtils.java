package util;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import javax.persistence.PersistenceException;

import net.sf.cglib.proxy.Enhancer;

/**
 * reflection utility methods
 * 
 * @author weihang chen
 * 
 */
public class ReflectionUtils {

	public static <T extends Annotation> T findAnnotation(Class<?> clazz,
			Class<T> annotationClass, Predicate<Field> p) {
		for (Field f : clazz.getDeclaredFields()) {
			T a = f.getAnnotation(annotationClass);
			if (a != null & p.apply(f)) {
				return a;
			}
		}

		if (clazz.getSuperclass() != null) {
			return findAnnotation(clazz.getSuperclass(), annotationClass, p);
		}
		return null;
	}

	public static Collection<Field> eachField(Class<?> clazz, Predicate<Field> p) {
		List<Field> result = new ArrayList<Field>();
		for (Field f : clazz.getDeclaredFields()) {
			if (p.apply(f)) {
				result.add(f);
			}
		}

		if (clazz.getSuperclass() != null) {
			result.addAll(eachField(clazz.getSuperclass(), p));
		}
		return result;
	}

	public static Collection<Method> eachMethod(Class<?> clazz,
			Predicate<Method> p) {
		List<Method> result = new ArrayList<Method>();
		for (Method f : clazz.getDeclaredMethods()) {
			if (p.apply(f)) {
				result.add(f);
			}
		}

		if (clazz.getSuperclass() != null) {
			result.addAll(eachMethod(clazz.getSuperclass(), p));
		}
		return result;
	}

	public static <T extends Annotation> void eachAnnotation(Class<?> clazz,
			Class<T> annotationClass, Predicate<T> p) {
		T a = clazz.getAnnotation(annotationClass);
		if (a != null) {
			p.apply(a);
		}
		for (Method me : clazz.getDeclaredMethods()) {
			a = me.getAnnotation(annotationClass);
			if (a != null) {
				p.apply(a);
			}
		}

		if (clazz.getSuperclass() != null) {
			eachAnnotation(clazz.getSuperclass(), annotationClass, p);
		}

	}

	public static Method findMethod(Class<?> clazz, String name) {
		for (Method me : clazz.getDeclaredMethods()) {
			if (me.getName().equalsIgnoreCase(name)) {
				return me;
			}
		}
		if (clazz.getSuperclass() != null) {
			return findMethod(clazz.getSuperclass(), name);
		}
		return null;
	}

	public static boolean hasAnnotation(AnnotatedElement e,
			Class<? extends Annotation> annotationClass) {
		return e.getAnnotation(annotationClass) != null;
	}

	public interface AnnotationPredicate {
		boolean equals(Method m, Annotation a);
	}

	public static Object invokeGetterMethod(Object methodOwner,
			Method getterMethod) {
		if (methodOwner == null || getterMethod == null)
			return null;
		Object[] args = {};
		try {
			return getterMethod.invoke(methodOwner, args);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Method getFieldGetterMethod(Field field) {
		if (field == null)
			return null;
		return findMethod(field.getDeclaringClass(), "get"
				+ CommonUtil.firstCharToUpperCase(field.getName()));
	}

	public static Method getFieldSetterMethod(Field field) {
		if (field == null)
			return null;
		return findMethod(field.getDeclaringClass(), "set"
				+ CommonUtil.firstCharToUpperCase(field.getName()));
	}

	public static Class<?> resolveReturnType(Method method) {
		Type returnType = method.getGenericReturnType();
		if (returnType instanceof ParameterizedType) {
			ParameterizedType type = (ParameterizedType) returnType;
			Type[] typeArguments = type.getActualTypeArguments();
			for (Type typeArgument : typeArguments) {
				if (typeArgument instanceof Class<?>) {
					return (Class<?>) typeArgument;
				}
			}
			return null;
		}
		return (Class<?>) returnType;
	}

	// do not use field.get(from), the enhance entity will make this fail
	// https://forums.oracle.com/forums/thread.jspa?messageID=4704203
	// use method getter/setter instead
	public static Object getFieldObject(Object parentObj, Field field) {
		Method m = getFieldGetterMethod(field);
		try {
			Object[] arguments = {};
			return m.invoke(parentObj, arguments);
		} catch (Exception e) {
			// log this
			e.printStackTrace();
		}
		return null;
	}

	// the assignedvalue might be incompatable collection type, need to specify
	// concrete collection object to be assigned to the field
	@SuppressWarnings("unchecked")
	public static void setFieldObject(Object parentObj, Field assignedField,
			Object assignedValue) {
		if (parentObj == null || assignedField == null || assignedValue == null)
			return;
		Method m = getFieldSetterMethod(assignedField);
		if (!m.isAccessible())
			m.setAccessible(true);
		try {
			if (isCollection(assignedValue.getClass())) {
				Constructor<Collection<Object>> constructor = findCtor(assignedField);
				Collection concreteCollection = constructor.newInstance();
				concreteCollection.addAll(((Collection) assignedValue));
				m.invoke(parentObj, concreteCollection);

			} else
				m.invoke(parentObj, assignedValue);
		} catch (Exception e) {
			// log this
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	final static Map<String, Class<? extends Collection>> _collectionFallbacks = new HashMap<String, Class<? extends Collection>>();
	static {
		_collectionFallbacks.put(Collection.class.getName(), ArrayList.class);
		_collectionFallbacks.put(List.class.getName(), ArrayList.class);
		// _collectionFallbacks.put(Set.class.getName(), LinkedHashSet.class);
		_collectionFallbacks.put(Set.class.getName(), HashSet.class);
		_collectionFallbacks.put(SortedSet.class.getName(), TreeSet.class);
		_collectionFallbacks.put(Queue.class.getName(), LinkedList.class);
		_collectionFallbacks.put("java.util.Deque", LinkedList.class);
		_collectionFallbacks.put("java.util.NavigableSet", TreeSet.class);
	}

	@SuppressWarnings("unchecked")
	public static Constructor<Collection<Object>> findCtor(Field field)
			throws SecurityException, NoSuchMethodException {
		Class<?> collectionClass = field.getType();
		Constructor<Collection<Object>> ctor = null;
		if (collectionClass.isInterface()) {
			Class<? extends Collection> fallback = _collectionFallbacks
					.get(collectionClass.getName());
			if (fallback == null) {
				throw new IllegalArgumentException(
						"Can not find a deserializer for non-concrete Collection type "
								+ collectionClass.getName());
			}
			ctor = (Constructor<Collection<Object>>) fallback.getConstructor();
		} else {
			ctor = (Constructor<Collection<Object>>) collectionClass
					.getConstructor();
		}
		return ctor;
	}

	public static boolean isCollection(Class<?> clazz) {
		return Collection.class.isAssignableFrom(clazz);
	}

	@SuppressWarnings("unchecked")
	public static Class getRealClass(Class clazz) {
		if (Enhancer.isEnhanced(clazz))
			clazz = clazz.getSuperclass();
		return clazz;
	}

	@SuppressWarnings("unchecked")
	public static boolean hasInterface(Class<?> has, Class<?> in) {
		if (has.equals(in)) {
			return true;
		}
		boolean match = false;
		for (Class intrface : in.getInterfaces()) {
			if (intrface.getInterfaces().length > 0) {
				match = hasInterface(has, intrface);
			} else {
				match = intrface.equals(has);
			}

			if (match) {
				return true;
			}
		}
		return false;
	}

	public static Type[] getTypeArguments(Field property) {
		Type type = property.getGenericType();
		if (type instanceof ParameterizedType) {
			return ((ParameterizedType) type).getActualTypeArguments();
		}
		return null;
	}

	public static boolean hasSuperClass(Class<?> has, Class<?> in) {
		if (in.equals(has)) {
			return true;
		}
		boolean match = false;

		if (in.getSuperclass().equals(Object.class)) {
			return match;
		}
		match = hasSuperClass(has, in.getSuperclass());
		return match;
	}

	@SuppressWarnings("unchecked")
	public static Class<?> classForName(String className,
			ClassLoader classLoader) {
		try {
			Class c = null;
			try {
				c = Class.forName(className, true, Thread.currentThread()
						.getContextClassLoader());
			} catch (ClassNotFoundException e) {
				try {
					c = Class.forName(className);
				} catch (ClassNotFoundException e1) {
					if (classLoader == null) {
						throw e1;
					}

					c = classLoader.loadClass(className);
				}
			}

			return c;
		} catch (ClassNotFoundException e) {
			throw new PersistenceException(e);
		}
	}

	public static Class<?> stripEnhancerClass(Class<?> c) {
		String className = c.getName();

		int enhancedIndex = className.indexOf("$$EnhancerByCGLIB");
		if (enhancedIndex != -1) {
			className = className.substring(0, enhancedIndex);
		}

		if (className.equals(c.getName())) {
			return c;
		}

		c = classForName(className, c.getClassLoader());

		return c;
	}

}
