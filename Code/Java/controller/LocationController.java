package controller;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import com.ibm.commons.util.StringUtil;

import persistence.annotation.support.JavaBeanFactory;

import model.Location;
import dao.DaoFactory;
import dao.LocationDAO;

/**
 * Controller class for View LocationCRUD.xsp
 * <p>
 * Business logic should be put in this class. Database CRUD operations are
 * delegated to LocationDAO
 * 
 * @author weihang chen
 * 
 */
public class LocationController implements Serializable {

	private static final long serialVersionUID = 7184365967249368209L;
	/**
	 * Collection of locations being binded to LocationCRUD.xsp
	 */
	protected Vector<Location> locations;
	/**
	 * in memory collection, store Locations which are supposed to be removed
	 * from database
	 */
	protected Vector<Location> deleteLocations;
	/**
	 * variable used to decide if Location entities should be read from database
	 * or read from JVM memory
	 */
	protected boolean isLoaded;

	public LocationController() {
		locations = new Vector<Location>();
		deleteLocations = new Vector<Location>();
		isLoaded = false;
	}

	/**
	 * Createing a new Location entity in this way Location loc=new Location()
	 * is wrong in this project at the moment, have to use
	 * JavaBeanFactory.getProxy(Location.class) to generate a new entity, so the
	 * entity is CGLib enhanced
	 * 
	 * @return Location entity
	 */
	public Location createNewLocation() {
		Location location = JavaBeanFactory.getProxy(Location.class);
		return location;
	}

	public void addLocation(Location location) {
		locations.add(location);
	}

	/**
	 * put the should-be removed Location entity to deleteLocations and remove
	 * it from locations, no database transaction is happening here
	 * 
	 * @param location
	 */
	public void deleteLocation(Location location) {
		deleteLocations.add(location);
		locations.remove(location);
		Iterator<Location> iter = locations.iterator();
		while (iter.hasNext()) {
			Location loc = iter.next();
			if (StringUtil.equals(loc.getUnid(), location.getUnid())) {
				iter.remove();
			}
		}
	}

	/**
	 * get the corresponding DaoImpl - LocationDAO from DaoFactory, invoke
	 * locationDao.getLocations() to retrieve a collection of Location entities.
	 * LocationController as the controller class do not know anything about the
	 * interaction with the database, its the Dao Implementation's responsibility
	 * 
	 * @return Collection of Location entities
	 */
	public Vector<Location> getLocations() {
		if (isLoaded)
			return locations;
		LocationDAO locationDao = DaoFactory.getLocationDao();
		try {
			locations = locationDao.getLocations();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		isLoaded = true;
		return locations;
	}

	/**
	 * all the in-memory changes to entities are submited to database go through
	 * locations and save, DominoDocument has already implemented isDirty, if a
	 * document is not modified, it will not be submitted to database
	 * <p>
	 * go through deleteLocations, delete all the should-be deleted documents if
	 * they are not new documents, clear everything by cleaning both locations
	 * and deleteLocations and finally changed the isLoaded to false, so when
	 * the next request comes in, it will fetch Locations from database instead
	 */
	public void submit() {
		for (Location location : locations) {
			try {
				location.persist();
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		locations.clear();
		isLoaded = false;
		for (Location location : deleteLocations) {
			try {
				location.checkState();
				if (!location.getDoc().getDocument().isNewNote())
					location.delete();
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		deleteLocations.clear();
	}

}
