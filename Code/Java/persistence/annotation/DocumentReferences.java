package persistence.annotation;

import java.lang.annotation.*;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;

/**
 * This annotation is used to define one-to-many relation between relation owner
 * object and the relation targeting object collection. view name is used for
 * look up, once objects are retrieved assign it to the relation owner object
 * using reflection. if its eager fetch, when relation object is loaded,
 * relation targeting collection is populated as well, else if its lazy,
 * collection is only populated when its getter is invoked
 * 
 * @author weihang chen
 * 
 */
@Target( { ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DocumentReferences {

	/**
	 * Controls when referenced Domino entity collection should be loaded. in
	 * case of LAZY, collection is not loaded until collection getter is
	 * invoked, in case of EAGER, collection is loaded when owner object is
	 * loaded
	 */
	public FetchType fetch() default FetchType.LAZY;

	/**
	 * descending , ascending - not implemented
	 */
	public boolean descendingSortOrder() default false;

	/**
	 * collection order field - not implemented
	 */
	public String orderBy() default "";

	/**
	 * Define how operation issued on relation owner object should cascade on
	 * collection objects
	 * 
	 * @return array of CascadeType
	 */
	public CascadeType[] cascade() default { CascadeType.ALL };

	/**
	 * unid as default
	 */
	public String foreignKey();

	/**
	 * define the viewname to look for the collection objects
	 * 
	 * @return viewname
	 */

	public String viewName();

}
