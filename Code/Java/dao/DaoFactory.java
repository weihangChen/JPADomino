package dao;

/**
 * this class provides access to other detail Dao implementations, this class
 * has nothing to do with persistence context
 * 
 * @author weihang chen
 * 
 */
public class DaoFactory {

	
	public static PersonDao getPersonDao() {
		return new PersonDaoImpl();
	}

	public static LocationDAO getLocationDao() {
		return new LocationDAOImpl();
	}

}
