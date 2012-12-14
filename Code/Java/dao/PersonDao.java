package dao;

import java.util.Vector;

import model.Person;

/**
 * 
 * @author weihang chen
 * 
 */
public interface PersonDao {
	public Vector<Person> getPeople() throws Exception;

}
