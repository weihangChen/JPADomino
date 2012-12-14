package persistence.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * used to index a domino entity field (lucene), not implemented yet
 * 
 * @author weihang chen
 * 
 */
@Target( { java.lang.annotation.ElementType.TYPE,
		java.lang.annotation.ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Index {
	public abstract boolean index();

	public abstract String name();

	public abstract String[] columns();
}
