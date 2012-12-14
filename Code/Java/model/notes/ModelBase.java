package model.notes;

import java.util.*;

import javax.persistence.PersistenceException;

import persistence.annotation.support.DominoEntityHelper;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.model.domino.DominoUtils;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;
import com.ibm.xsp.model.domino.wrapped.DominoDocument.FieldValueHolder;

import util.Assert;
import util.JSFUtil;
import util.ResourceUtil;

import lotus.domino.*;
import lotus.domino.local.EmbeddedObject;

/**
 * This is the parent class that all Domino entity Pojo class should extend,
 * providing some important functionalities
 * 
 * @author weihang chen
 */
public class ModelBase {
	private static final long serialVersionUID = -126355568954326940L;
	protected static final String ITEM_FORM_NAME = "Form";
	@SuppressWarnings("unused")
	private String unid;
	public DominoDocument doc;

	/**
	 * empty constructor will invoke another constructor instead, passing null
	 * as argument
	 */
	protected ModelBase() {
		this(null);
	}

	/**
	 * this is the real functional constructor in use to create a new java
	 * entity wrapping a DominoDocument
	 * 
	 * @param doc
	 */

	protected ModelBase(Object doc) {
		this.doc = initDoc(doc);
	}

	/**
	 * if docObj is null, create a new DominoDocument and assign it to dominoDoc
	 * if docObj is not null, assign it to dominoDoc
	 * 
	 * @param docObj
	 * @return
	 */
	protected DominoDocument initDoc(Object docObj) {
		DominoDocument dominoDoc = null;
		String formName = DominoEntityHelper.getFormName(this.getClass());
		if (StringUtil.isEmpty(formName) && !(docObj instanceof DominoDocument))
			throw new NullPointerException(
					"Java entity fails to be initialized, neither formName or wrapped dominoDocument can be found");
		try {
			if (!(docObj instanceof DominoDocument)) {
				String dbName = DominoEntityHelper.getDBName(this.getClass());
				Database targetDB = null;
				if (StringUtil.isEmpty(dbName)) {
					targetDB = DominoUtils.getCurrentDatabase();
				} else {
					targetDB = JSFUtil.doOpenDatabase(dbName);
					// targetDB = DominoUtils.openDatabaseByName(dbName);
				}
				String relativeDBPath = targetDB.getFilePath();
				dominoDoc = DominoDocument.wrap(relativeDBPath, targetDB, "",
						formName, "both", "force", false, null, null);

			} else
				dominoDoc = (DominoDocument) docObj;

			this.unid = dominoDoc.getDocument().getUniversalID();
			// System.out.println("create object instance with id: " + unid);

		} catch (Exception ne) {
			handleException(ne);
		}
		Assert.notNull(dominoDoc, "wrapped dominoDocument may not be null");
		return dominoDoc;
	}

	/**
	 * return the document UniversalID return unid; is not going to work due to
	 * the attach/detach behavior
	 * 
	 * @return
	 */
	public String getUnid() {
		String id = "";
		try {
			checkState();
			id = doc.getDocument().getUniversalID();
		} catch (Exception ne) {
			handleException(ne);
		}
		return id;
	}

	public void setUnid(String unid) {
		this.unid = unid;
	}

	public DominoDocument getDoc() {
		return doc;
	}

	public void setDoc(DominoDocument doc) {
		this.doc = doc;
	}

	// all exception being thrown should be log here
	protected static void handleException(Exception ne) {
		ne.printStackTrace();
	}

	// CRUD - R
	@SuppressWarnings("unchecked")
	protected Vector<?> readValues(String itemName) {
		Vector<?> retval = new Vector();
		try {
			checkState();
			retval = (Vector<?>) doc.getItemValue(itemName);
		} catch (Exception ne) {
			handleException(ne);
		}
		return retval;
	}

	protected String readString(String itemName) {
		String retval = "";
		try {
			checkState();
			retval = doc.getItemValueString(itemName);
		} catch (Exception ne) {
			handleException(ne);
		}
		return retval;
	}

	protected double readDouble(String itemName) {
		double retval = 0.0;
		try {
			checkState();
			retval = doc.getItemValueDouble(itemName);
		} catch (Exception ne) {
			handleException(ne);
		}
		return retval;
	}

	protected int readInteger(String itemName) {
		int retval = 0;
		try {
			checkState();
			retval = doc.getItemValueInteger(itemName);
		} catch (Exception ne) {
			handleException(ne);
		}
		return retval;
	}

	// CRUD - R/U
	protected void writeValue(String itemName, Object value) {
		try {
			checkState();
			if (value == null) {
				doc.removeItem(itemName);
			}
			doc.replaceItemValue(itemName, value);
		} catch (Exception ne) {
			handleException(ne);
		}
	}

	public void deleteAttachments(String richTextItemName) {
		Document document = doc.getDocument();
		try {
			RichTextItem body = (RichTextItem) document
					.getFirstItem(richTextItemName);
			if (body == null)
				return;
			RichTextNavigator rtnav = body.createNavigator();
			RichTextRange range = body.createRange();
			ArrayList<String> deleteAttachments = new ArrayList<String>();
			if (rtnav.findFirstElement(RichTextItem.RTELEM_TYPE_FILEATTACHMENT)) {
				do {
					Base attachment = rtnav.getElement();
					String attachmentName = attachment.toString();
					deleteAttachments.add(attachmentName);
					range.setBegin(rtnav);
					range.setEnd(rtnav);
					range.remove();
				} while (rtnav
						.findNextElement(RichTextItem.RTELEM_TYPE_FILEATTACHMENT));
			}
			deleteAllAttachments(document, deleteAttachments);
		} catch (Exception e) {
			handleException(e);
		}
	}

	protected void deleteAllAttachments(Document doc,
			ArrayList<String> deleteAttachments) throws NotesException {
		for (String attachmentName : deleteAttachments) {
			lotus.domino.EmbeddedObject e = doc.getAttachment(attachmentName);
			if (e != null)
				e.remove();
		}
	}

	public void createAttachment(String richTextItemName,
			String attachmentName, String filePath) {
		Document document = doc.getDocument();
		try {
			RichTextItem body = (RichTextItem) document
					.getFirstItem(richTextItemName);
			if (body == null)
				body = document.createRichTextItem(richTextItemName);
			body.embedObject(EmbeddedObject.EMBED_ATTACHMENT, null, filePath,
					attachmentName);
		} catch (Exception e) {
			handleException(e);
		}
	}

	/**
	 * compute the document and perform save
	 */
	public void persist() {
		try {
			checkState();
			doc.getDocument().computeWithForm(true, true);
			doc.save();
			// persistRefDocuments();
		} catch (Exception ne) {
			handleException(ne);
		}
	}

	protected long getEntityCount(Key key, String viewName)
			throws PersistenceException {
		long n = 0;
		try {
			View lup = ResourceUtil.getViewByName(viewName);
			if (lup != null) {
				DocumentCollection dc = lup.getAllDocumentsByKey(key
						.getEntries(), true);
				n = dc.getCount();
				dc.recycle();
			}
		} catch (NotesException ne) {
			handleException(ne);
		}
		return n;
	}

	// CRUD - D
	public boolean delete() throws PersistenceException, NotesException {
		this.checkState();
		boolean ret = false;
		try {
			synchronized (this) {
				ret = doc.getDocument().remove(true);
				doc = null;
			}
		} catch (NotesException ne) {
			handleException(ne);
		}
		return ret;
	}

	/**
	 * Notes Documents are recycled between requests, restore them if they are
	 * found being deleted
	 * 
	 * @throws PersistenceException
	 * @throws NotesException
	 */
	public void checkState() throws PersistenceException, NotesException {
		if (doc.getDocument().isDeleted())
			doc.restoreWrappedDocument();
		if (doc.getDocument() == null || doc.getDocument().isDeleted())
			throw new PersistenceException("Business Object with id ["
					+ this.getUnid() + "] is in an invalid state");
	}

	public int getChangedFieldSize() {
		return doc.getChangedFields().size();
	}

	public HashMap<String, FieldValueHolder> getChangedFields() {
		return (HashMap<String, FieldValueHolder>) doc.getChangedFields();
	}

}