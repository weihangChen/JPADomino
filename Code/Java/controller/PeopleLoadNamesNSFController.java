package controller;

import java.util.Vector;

import model.Person;
import dao.DaoFactory;
import dao.PersonDao;

/**
 * Controller class for View PeopleLoadNamesNSF.xsp, main purpose is to show the
 * feature fetching Person objects from another database and make them
 * persistent, in this case names.nsf
 * 
 * 
 * @author weihang chen
 * 
 */
public class PeopleLoadNamesNSFController {

	protected Vector<Person> peopleVec;

	public PeopleLoadNamesNSFController() {
		peopleVec = new Vector<Person>();
	}
	/**
	 * get the corresponding DaoImpl - PersonDao from DaoFactory, invoke
	 * personDao.getPeople() to retrieve a collection of Person entities.
	 * PeopleLoadNamesNSFController as the controller class do not know anything about the
	 * interaction with the database, its the Dao Implementation's responsibility
	 * 
	 * @return Collection of Location entities
	 */
	public Vector<Person> getPeople() {
		if (peopleVec != null && peopleVec.size() > 0)
			return peopleVec;
		PersonDao personDao = DaoFactory.getPersonDao();
		Vector<Person> people = new Vector<Person>();
		try {
			people = personDao.getPeople();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		peopleVec = people;
		return peopleVec;
	}
}
