package event;

import com.ibm.xsp.application.ApplicationEx;

public class CustomApplicationListener implements
		com.ibm.xsp.application.events.ApplicationListener {

	public void applicationCreated(ApplicationEx arg0) {
		// com.ibm.xsp.application.DesignerApplicationEx
		System.out
				.println("Application started!!!!!!CREATE INSTANCE OF ENTITYMANAGERFACTORY");
		persistence.core.DominoPersistenceProvider persistenceProvider = new persistence.core.DominoPersistenceProvider();
		persistenceProvider.createContainerEntityManagerFactory(null, null);
		System.out.println("CREATE INSTANCE OF ENTITYMANAGERFACTORY FINISHED");
	}

	public void applicationDestroyed(ApplicationEx arg0) {
		System.out.println("Application destroyed!!!!!!");
	}

}
