package persistence.event;

import persistence.metadata.model.EntityMetadata;
import java.util.List;
import javax.persistence.EntityManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * associated with Event Listener, not used
 * 
 */
public class EntityEventDispatcher {
	private static final Log log = LogFactory.getLog(EntityManager.class);

	@SuppressWarnings("unchecked")
	public void fireEventListeners(EntityMetadata metadata, Object entity,
			Class<?> event) {
		List<CallbackMethod> callBackMethods = (List<CallbackMethod>) metadata
				.getCallbackMethods(event);

		if ((null == callBackMethods) || (callBackMethods.isEmpty()))
			return;
		log.debug("Callback >> " + event.getSimpleName() + " on "
				+ metadata.getEntityClazz().getName());
		for (CallbackMethod callback : callBackMethods) {
			log.debug("Firing >> " + callback);

			callback.invoke(entity);
		}
	}
}
