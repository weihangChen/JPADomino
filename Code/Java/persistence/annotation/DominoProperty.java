package persistence.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for Domino Pojo class variable, Runtime use
 * 
 * @author weihang chen
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DominoProperty {
	/**
	 * 
	 * @return corresponding item name from a document
	 */
	public String itemName();
}
