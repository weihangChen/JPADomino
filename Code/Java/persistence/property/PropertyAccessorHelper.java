package persistence.property;

import util.ReflectionUtils;

import java.lang.reflect.Field;

import java.lang.reflect.Type;
import java.util.Collection;

import persistence.metadata.model.EntityMetadata;
import persistence.proxy.EnhancedEntity;

/**
 * some reflection methods.this class should be merged with ReflectionUtils.java
 * 
 * @author weihang chen
 * 
 */
public class PropertyAccessorHelper {

	public static void set(Object target, Field field, Object value) {
		if (!(field.isAccessible())) {
			field.setAccessible(true);
		}
		try {
			field.set(target, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static Class<?> getGenericClass(Field collectionField) {
		Class genericClass = null;
		if (collectionField == null) {
			return genericClass;
		}
		if (isCollection(collectionField.getType())) {
			Type[] parameters = ReflectionUtils
					.getTypeArguments(collectionField);
			if (parameters != null) {
				if (parameters.length == 1) {
					genericClass = (Class) parameters[0];
				} else {
					throw new RuntimeException(
							"Can't determine generic class from a field that has two parameters.");
				}
			}
		}
		return ((genericClass != null) ? genericClass : collectionField
				.getType());
	}

	public static Field[] getDeclaredFields(Field relationalField) {
		Field[] fields;
		if (isCollection(relationalField.getType())) {
			fields = getGenericClass(relationalField).getDeclaredFields();
		} else {
			fields = relationalField.getType().getDeclaredFields();
		}
		return fields;
	}

	public static final boolean isCollection(Class<?> clazz) {
		return Collection.class.isAssignableFrom(clazz);
	}

	public static String getId(Object entity, EntityMetadata metadata) {
		if (entity instanceof EnhancedEntity) {
			return ((EnhancedEntity) entity).getId();
		}
		return "id can not be found";
	}

}
