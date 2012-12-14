package dao;

import java.util.Vector;

import com.ibm.xsp.model.domino.DominoUtils;

import lotus.domino.Database;
import model.Person;

/**
 * 
 * @author weihang chen
 * 
 */
public class PersonDaoImpl extends DaoBase implements PersonDao {

	private static final long serialVersionUID = -3180284664017555290L;

	@SuppressWarnings("unchecked")
	public Vector<Person> getPeople() throws Exception {
		// hard code the db
		Database personDb = DominoUtils.openDatabaseByName("names.nsf");
		Vector<Person> people = (Vector<Person>) findAllByKey(null, personDb,
				"People", Person.class);
		return people;
	}

}
