package event;

import javax.servlet.http.HttpSessionEvent;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.events.SessionListener;

public class CustomSessionListener implements SessionListener {

	public void sessionCreated(ApplicationEx arg0, HttpSessionEvent arg1) {
		System.out.println("Session created!!!!!!!!!!!");

	}

	public void sessionDestroyed(ApplicationEx arg0, HttpSessionEvent arg1) {
		System.out.println("Session destroyed!!!!!!");
	}

}
