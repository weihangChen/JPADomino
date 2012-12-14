package model.notes;

import java.util.*;

/**
 * GetAllDocumentsByKey/GetAllEntriesByKey can take a vector of string as
 * parameter, this class simply wrap over such a Vector object
 * 
 * @author weihang chen
 * 
 */
public class Key {
	private Vector<String> entries = new Vector<String>();

	public Vector<String> getEntries() {
		return entries;
	}

	public void appendEntry(String entry) {
		entries.addElement(entry);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("[");
		Enumeration<String> e1 = entries.elements();
		while (e1.hasMoreElements()) {
			sb.append((String) e1.nextElement());
			sb.append(' ');
		}
		return (sb.toString() + "]");
	}

}
