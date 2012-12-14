package persistence.annotation.support;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Map;

import persistence.annotation.DominoProperty;

import util.CommonUtil;
import util.Predicate;
import util.ReflectionUtils;

import com.ibm.commons.util.StringUtil;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * this intercepter only intercept Fields' (annotated with @DominoProperty)
 * getter/setter to hide read/write action to document, make a cleaner entity
 * class
 * 
 * <code>@DominoProperty </code>
 * 
 * annotated Fields getter/setter are cached in intercepter instance, then
 * according to the method return type it will find the corresponding read/write
 * method for the DominoDocument, and invoke the read/write method instead of
 * invoking the original getter/setter method.
 * 
 * <code>@DocumentReference </code>annotated getter methods are cached,
 * restoreDocument before returning the objects. This only applies to children
 * objects, where parent object will be restored directly within the cache from
 * persistenDelegator
 * 
 * @author weihang chen
 */
public class JavaBeanInterceptor implements MethodInterceptor {

	/**
	 * Original entity that is sent in and enhanced
	 */
	private Object target;
	private ArrayList<Field> cachedDominoPropertyFields;
	/**
	 * store all java bean getter method for a Field which is annotated by
	 * DominoProperty
	 */
	private HashMap<String, String> getterMap;
	/**
	 * store all java bean setter method for a Field which is annotated by
	 * DominoProperty
	 */
	private HashMap<String, String> setterMap;

	public JavaBeanInterceptor() {
		super();
		cachedDominoPropertyFields = new ArrayList<Field>();
		getterMap = new HashMap<String, String>();
		setterMap = new HashMap<String, String>();
	}

	public void setTarget(Object target) {

		this.target = target;
		//go through all fields and put them in a collection if they are annotated with DominoProperty
		cachedDominoPropertyFields = (ArrayList<Field>) ReflectionUtils
				.eachField(target.getClass(), new Predicate<Field>() {
					public boolean apply(Field input) {
						if (ReflectionUtils.hasAnnotation(input,
								DominoProperty.class)) {
							return true;
						}
						return false;
					}
				});
		//go through the DominoProperty Field collection and put the getter/setter methods in getter/setter map
		for (Field field : cachedDominoPropertyFields) {
			String getterName = "get"
					+ CommonUtil.firstCharToUpperCase(field.getName());
			String setterName = "set"
					+ CommonUtil.firstCharToUpperCase(field.getName());
			String documentItemName = field.getAnnotation(DominoProperty.class)
					.itemName();
			getterMap.put(getterName, documentItemName);
			setterMap.put(setterName, documentItemName);
		}

	}

	/**
	 * static variable mapping the returntype with the document read operation
	 */
	final static Map<String, String> _javaDocumentPropertyMap = new HashMap<String, String>();
	static {
		_javaDocumentPropertyMap.put("java.lang.String", "readString");
		_javaDocumentPropertyMap.put("int", "readInteger");
	}

	public Object intercept(Object obj, Method method, Object[] args,
			MethodProxy proxy) throws Throwable {

		// System.out.println("intercept starts: " + method.getName());
		String methodName = method.getName();
		if (!getterMap.containsKey(methodName)
				&& !setterMap.containsKey(methodName)) {
			// System.out
			// .println("Method"
			// + methodName
			// +
			// " is NOT registred getter/setter method for @DominoProperty annotated Field, PROCEED AS USUAL ");
			method.setAccessible(true);
			return method.invoke(target, args);
		}
		// System.out
		// .println("Method"
		// + methodName
		// +
		// " is registred getter/setter method for @DominoProperty annotated Field, PROCEED read/write method from/to document instead ");
		// ex. getThemeName()/setThemeName()

		Method javaMethod = ReflectionUtils.findMethod(obj.getClass(),
				methodName);

		// process getter methods ex. getThemeName()
		Object[] mArgs = null;
		String realMethodStr = "";
		if (getterMap.containsKey(methodName)) {
			mArgs = new String[1];
			mArgs[0] = getterMap.get(methodName);
			// ex. String is returnType of getThemeName()
			// Class<?> returnType = javaMethodName.getReturnType();
			Class<?> returnType = ReflectionUtils.resolveReturnType(javaMethod);

			// ex. getThemeName() corresponds to readString("ThemeName");
			realMethodStr = _javaDocumentPropertyMap.get(returnType.getName());

		} else if (setterMap.containsKey(methodName)) {
			if (args.length != 1) {
				method.setAccessible(true);
				return method.invoke(target, args);
			}
			mArgs = new Object[2];
			// document item name
			mArgs[0] = setterMap.get(methodName);
			// set value
			mArgs[1] = args[0];
			realMethodStr = "writeValue";
		}
		if (StringUtil.isEmpty(realMethodStr)) {
			method.setAccessible(true);
			return method.invoke(target, args);
		}
		Method realMethod = ReflectionUtils.findMethod(obj.getClass(),
				realMethodStr);
		realMethod.setAccessible(true);
		return realMethod.invoke(obj, mArgs);
	}
}
