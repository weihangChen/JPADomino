package util;

/**
 * Utility methods
 * 
 * @author weihang chen
 */

public class CommonUtil {

	/**
	 * change the first char from a String object to uppercase
	 * 
	 * @param name
	 * @return
	 */
	public static String firstCharToUpperCase(String name) {
		return Character.toString(Character.toUpperCase(name.charAt(0)))
				+ name.substring(1);
	}

	/**
	 * get the current running method name
	 * 
	 * @param className
	 * @return
	 */
	public static String getMethodName(String className) {
		final StackTraceElement[] stacktrace = Thread.currentThread()
				.getStackTrace();
		StackTraceElement e = stacktrace[3];
		String methodName = e.getMethodName();
		String str = methodName + " from " + className;
		return str;
	}

}
