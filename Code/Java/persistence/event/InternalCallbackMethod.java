package persistence.event;

import persistence.metadata.model.EntityMetadata;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.PersistenceException;

/**
 * associated with Event Listener, not used
 * 
 */
public final class InternalCallbackMethod implements CallbackMethod {
	private final EntityMetadata entityMetadata;
	private Method method;

	public InternalCallbackMethod(EntityMetadata entityMetadata, Method method) {
		this.entityMetadata = entityMetadata;
		this.method = method;
	}

	public void invoke(Object entity) throws PersistenceException {
		if (!(this.method.isAccessible()))
			this.method.setAccessible(true);
		try {
			this.method.invoke(entity, new Object[0]);
		} catch (IllegalArgumentException e) {
			throw new PersistenceException(e);
		} catch (IllegalAccessException e) {
			throw new PersistenceException(e);
		} catch (InvocationTargetException e) {
			throw new PersistenceException(e);
		}
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.entityMetadata.getEntityClazz().getName() + "."
				+ this.method.getName());
		return builder.toString();
	}
}
