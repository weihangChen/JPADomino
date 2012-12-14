package persistence.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.PersistenceException;

/**
 * associated with Event Listener, not used
 * 
 */
public final class ExternalCallbackMethod implements CallbackMethod {
	private Class<?> clazz;
	private Method method;

	public ExternalCallbackMethod(Class<?> clazz, Method method) {
		this.clazz = clazz;
		this.method = method;
	}

	public void invoke(Object entity) throws PersistenceException {
		if (!(this.method.isAccessible()))
			this.method.setAccessible(true);
		try {
			this.method.invoke(this.clazz.newInstance(),
					new Object[] { entity });
		} catch (IllegalArgumentException e) {
			throw new PersistenceException(e);
		} catch (IllegalAccessException e) {
			throw new PersistenceException(e);
		} catch (InvocationTargetException e) {
			throw new PersistenceException(e);
		} catch (InstantiationException e) {
			throw new PersistenceException(e);
		}
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.clazz.getName() + "." + this.method.getName());
		return builder.toString();
	}
}
