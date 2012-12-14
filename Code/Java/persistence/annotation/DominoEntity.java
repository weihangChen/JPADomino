package persistence.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * used to annotated a pojo entity as Domino entity, Runtime use
 * 
 * @author weihang chen
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DominoEntity {
	// FormName value required
	public String formName();

	// ViewName value required
	public String viewName();

	// Database name value not required
	public String DBName() default "";
}
