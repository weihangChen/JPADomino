package persistence.event;

import javax.persistence.PersistenceException;

/**
 * 
 * associated with Event Listener, not used
 * 
 */

public abstract interface CallbackMethod {
	public abstract void invoke(Object paramObject) throws PersistenceException;
}
