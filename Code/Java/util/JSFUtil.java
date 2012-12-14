package util;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import net.sf.cglib.proxy.Enhancer;

import com.ibm.xsp.domino.context.DominoFacesContext;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.domino.DominoUtils;
import com.ibm.xsp.util.DataPublisher;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

public class JSFUtil {
	public static Object getVariableValue(String varName) {
		FacesContext context = FacesContext.getCurrentInstance();
		return context.getApplication().getVariableResolver().resolveVariable(
				context, varName);
	}

	public static Object getBindingValue(String ref) {
		FacesContext context = FacesContext.getCurrentInstance();
		Application application = context.getApplication();
		return application.createValueBinding(ref).getValue(context);
	}

	/**
	 * use SignerWithFullAccess to open restricted database
	 * 
	 * @param dbName
	 * @return
	 */
	public static Database doOpenDatabase(String dbName) {
		Session strongSession = ExtLibUtil
				.getCurrentSessionAsSignerWithFullAccess();
		try {
			return strongSession.getDatabase(DominoUtils.getCurrentDatabase()
					.getServer(), dbName);
		} catch (NotesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * enhanced classes are the child class to the POJO class, need to call
	 * getSuperclass to get the original POJO class
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Class getRealClass(Class clazz) {
		if (Enhancer.isEnhanced(clazz))
			clazz = clazz.getSuperclass();
		return clazz;
	}

	/**
	 * check if a class is CG enhanced
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean isEnhanced(Class clazz) {
		return Enhancer.isEnhanced(clazz);
	}

	public static String getRelativeDBPath(Database db) {
		String currentPath = "";
		try {
			if (db == null)
				currentPath = DominoUtils.getCurrentDatabase().getFilePath();
			else
				db.getFilePath();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return currentPath;
	}

	public static String getPath1() {
		String currentPath = "";
		Session session = (Session) JSFUtil.getVariableValue("session");
		try {
			Object o = FacesContext.getCurrentInstance().getExternalContext()
					.getRequest();
			HttpServletRequest request = (HttpServletRequest) o;
			currentPath = request.getServerName() + "/"
					+ session.getCurrentDatabase().getFilePath();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return currentPath;
	}

	// push data to requestscope map, used in our project to show cache objects
	// and graph
	public static void pushData(Object obj, String name) {
		DataPublisher dataPublisher = ((DominoFacesContext) FacesContext
				.getCurrentInstance()).getDataPublisher();
		dataPublisher.pushObject(dataPublisher.createShadowedList(), name, obj);
	}
}
