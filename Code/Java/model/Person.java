package model;

import persistence.annotation.DominoEntity;
import persistence.annotation.DominoProperty;
import model.notes.ModelBase;

/**
 * annotated pojo class
 * 
 * @author weihang chen
 * 
 */
@DominoEntity(formName = "Person", viewName = "People", DBName = "names.nsf")
public class Person extends ModelBase {

	private static final long serialVersionUID = -2434472633779410296L;

	@DominoProperty(itemName = "FullName")
	private String fullName;
	@DominoProperty(itemName = "InternetAddress")
	private String internetAddress;

	public Person() {
	}

	public Person(Object doc) {
		super(doc);
	}

	public String getInternetAddress() {
		return internetAddress;
	}

	public void setInternetAddress(String internetAddress) {
		this.internetAddress = internetAddress;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

}
