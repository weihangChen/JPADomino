package dao;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import javax.persistence.PersistenceException;

import persistence.annotation.support.DominoEntityHelper;
import persistence.annotation.support.JavaBeanFactory;

import com.ibm.xsp.model.domino.DominoUtils;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;
import lotus.domino.*;
import model.notes.Key;
import util.ResourceUtil;

/**
 * this class provides all the interaction with the database, all other Dao
 * should extend this class. This class is associated with LocationCRUD.xsp, not
 * with the persistence context part
 * <p>
 * all the methods responsibilities are self-explained, therefore no extra
 * description is written for individual methods
 * 
 * @author weihang chen
 * 
 */

public class DaoBase implements Serializable {

	private static final long serialVersionUID = -5542911989121450439L;

	protected void removeEntityById(Key key, String viewName) {
		try {
			View lup = ResourceUtil.getViewByName(viewName);
			if (lup != null) {
				Document doc = lup.getDocumentByKey(key.getEntries(), true);
				if (doc == null) {
					PersistenceException ex0 = new PersistenceException(
							"Business Object with ID " + key + " not found!");
					throw ex0;
				}
				boolean b = doc.remove(true);
				if (b == false) {
					PersistenceException ex1 = new PersistenceException(
							"Business Object with ID " + key
									+ " can not be deleted");
					throw ex1;
				}
			}
		} catch (Exception ne) {
			handleException(ne);
		}
	}

	@SuppressWarnings("unchecked")
	public Vector viewEntryCollectionToJava(ViewEntryCollection vc, Class clazz)
			throws NotesException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Vector<Object> col = new Vector<Object>();
		ViewEntry ve = vc.getFirstEntry();
		ViewEntry tmp = null;
		String dbName = DominoEntityHelper.getDBName(clazz);
		while (ve != null) {
			Document doc = ve.getDocument();
			DominoDocument dominoDoc = DominoDocument.wrap(dbName, doc, "both",
					"force", true, "", "");
			// 0716
			Object obj = JavaBeanFactory.getInstance(clazz, dominoDoc);

			// Constructor con = clazz
			// .getConstructor(new Class[] { Object.class });
			// Object obj = con.newInstance(new Object[] { dominoDoc });
			col.add(obj);
			tmp = vc.getNextEntry();
			ve.recycle();
			ve = tmp;
		}
		return col;
	}

	@SuppressWarnings("unchecked")
	public Vector documentCollectionToJava(DocumentCollection dc, Class clazz)
			throws NotesException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Vector<Object> col = new Vector<Object>();
		Document doc = dc.getFirstDocument();
		String dbName = DominoEntityHelper.getDBName(clazz);
		while (doc != null) {
			DominoDocument dominoDoc = DominoDocument.wrap(dbName, doc, "both",
					"force", true, "", "");
			// 0716
			Object obj = JavaBeanFactory.getInstance(clazz, dominoDoc);

			// Constructor con = clazz
			// .getConstructor(new Class[] { Object.class });
			// Object obj = con.newInstance(new Object[] { dominoDoc });
			col.add(obj);
			Document tmp = dc.getNextDocument();
			doc.recycle();
			doc = tmp;
		}
		return col;
	}

	@SuppressWarnings("unchecked")
	public Object documentToJava(String dbName, Document doc, Class clazz)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			PersistenceException {
		Object obj = null;
		DominoDocument dominoDoc = DominoDocument.wrap(dbName, doc, "both",
				"force", true, "", "");
		// 0716
		obj = JavaBeanFactory.getInstance(clazz, dominoDoc);
		// Constructor con = clazz.getConstructor(new Class[] { Object.class });
		// obj = con.newInstance(new Object[] { dominoDoc });
		if (obj == null)
			throw new PersistenceException(
					"document fails to be converted to Java object: documentToJava(Document doc, Class clazz)");
		return obj;
	}

	public Object findOneByKey(Key key, Database db, String viewName,
			Class<?> clazz) throws PersistenceException, PersistenceException,
			SecurityException, IllegalArgumentException, PersistenceException,
			NoSuchMethodException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			PersistenceException, NotesException {
		if (!(key instanceof Key) || key.getEntries().size() == 0)
			throw new PersistenceException("empty key");
		View lup = ResourceUtil.getViewByName1(db, viewName);
		Document doc = lup.getDocumentByKey(key.getEntries(), true);
		if (doc == null) {
			PersistenceException idex = new PersistenceException(
					"Business Object with ID " + key + " not found!");
			throw idex;
		}
		String dbName = "";
		if (db == null)
			dbName = DominoUtils.getCurrentDatabase().getTitle();
		else
			dbName = db.getTitle();
		Object obj = documentToJava(dbName, doc, clazz);
		return obj;
	}

	public Vector<?> findAllByKey(Key key, Database db, String viewName,
			Class<?> clazz) throws PersistenceException, PersistenceException,
			SecurityException, IllegalArgumentException, NoSuchMethodException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NotesException {
		View lup = ResourceUtil.getViewByName1(db, viewName);
		// return all documents in view
		if (key == null) {
			ViewEntryCollection vc = lup.getAllEntries();
			if (vc == null)
				return null;
			return viewEntryCollectionToJava(vc, clazz);
		} else if (key instanceof Key) {
			DocumentCollection dc = lup.getAllDocumentsByKey(key.getEntries(),
					true);
			return documentCollectionToJava(dc, clazz);
		}
		return null;
	}

	protected static void handleException(Exception ne) {
		ne.printStackTrace();
	}

}
