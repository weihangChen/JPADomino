package util;

/**
 * interface used to evaluate an object to see if it meets certain criteria
 * 
 * @author weihang chen
 * 
 * @param <T>
 */
public interface Predicate<T> {

	boolean apply(T input);

}
