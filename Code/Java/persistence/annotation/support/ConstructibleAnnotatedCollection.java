package persistence.annotation.support;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * wrapper class wrapping some information to initialise concrete Collection
 * instance. More detail on how to create an instance of this class please read
 * AbstractEntityReader.recursivelyFindEntities() 
 * <code>
 * ConstructibleAnnotatedCollection constructibleField = new ConstructibleAnnotatedCollection(
 *								collectionField, constructor, returnType);
 * </code>
 * 
 * 
 * @author weihang chen
 */
public class ConstructibleAnnotatedCollection implements Serializable {

	private static final long serialVersionUID = 8067256514903780284L;
	private final Constructor<Collection<Object>> constructor;
	private final Field field;
	private final Class<?> returnType;

	public ConstructibleAnnotatedCollection(Field field,
			Constructor<Collection<Object>> ctor, Class<?> returnType) {
		this.field = field;
		this.constructor = ctor;
		this.returnType = returnType;
	}

	public Constructor<Collection<Object>> getConstructor() {
		return constructor;
	}

	public Field getField() {
		return field;
	}

	public Class<?> getReturnType() {
		return returnType;
	}

	@Override
	public String toString() {
		return field.getName();
	}

}
