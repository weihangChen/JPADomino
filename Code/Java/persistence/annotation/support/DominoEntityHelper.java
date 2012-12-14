package persistence.annotation.support;

import persistence.annotation.DominoEntity;

/**
 * support class getting annotated meta data from entity - database name, form
 * name
 * 
 * @author weihang chen
 * 
 */
public class DominoEntityHelper {

	/**
	 * 
	 * @param <T>
	 * @param clazz
	 * @return databasename
	 */
	public static <T> String getDBName(Class<T> clazz) {
		DominoEntity entityClassMetaData = clazz
				.getAnnotation(DominoEntity.class);
		if (entityClassMetaData != null)
			return entityClassMetaData.DBName();
		return "";
	}

	/**
	 * 
	 * @param <T>
	 * @param clazz
	 * @return form name
	 */
	public static <T> String getFormName(Class<T> clazz) {
		DominoEntity entityClassMetaData = clazz
				.getAnnotation(DominoEntity.class);
		if (entityClassMetaData != null)
			return entityClassMetaData.formName();
		return "";
	}
}
