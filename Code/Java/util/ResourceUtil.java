package util;

import javax.persistence.PersistenceException;

import com.ibm.xsp.model.domino.DominoUtils;

import lotus.domino.*;

/**
 * DominoUtil functions
 * 
 * @author weihang chen
 * 
 */
public class ResourceUtil {
	/**
	 * get view from current database
	 * 
	 * @param viewName
	 * @return
	 * @throws PersistenceException
	 * @throws NotesException
	 */
	public static View getViewByName(String viewName)
			throws PersistenceException, NotesException {
		View view = null;
		Database db = DominoUtils.getCurrentDatabase();
		if (db.isOpen())
			view = db.getView(viewName);
		if (view == null)
			throw new PersistenceException("view can not be found");
		if (db.isFTIndexed())
			db.updateFTIndex(true);
		return view;
	}

	/**
	 * get view from a database
	 * 
	 * @param db
	 * @param viewName
	 * @return
	 * @throws PersistenceException
	 * @throws NotesException
	 */
	public static View getViewByName1(Database db, String viewName)
			throws PersistenceException, NotesException {
		if (db == null)
			return getViewByName(viewName);
		View view = null;
		if (!db.isOpen())
			db.open();
		if (!db.isOpen())
			Assert.notNull(null, "database can't be openned");
		if (db.isOpen()) {
			view = db.getView(viewName);
			if (view == null)
				throw new PersistenceException("view can not be found");
			if (db.isFTIndexed())
				db.updateFTIndex(true);
		}
		return view;
	}

}
