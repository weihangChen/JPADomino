package dao;

import java.util.Vector;

import model.Location;
import model.notes.Key;
import model.resource.ViewsEnum;

/**
 * 
 * @author weihang chen
 * 
 */
public class LocationDAOImpl extends DaoBase implements LocationDAO {

	private static final long serialVersionUID = -3261621128324816509L;

	@SuppressWarnings("unchecked")
	public Vector<Location> getLocations() throws Exception {
		Vector<Location> locations = (Vector<Location>) findAllByKey(null,
				null, "Location", Location.class);
		return locations;
	}

	public void saveLocations(Vector<Location> locations) throws Exception {
		for (Location location : locations) {
			location.persist();
		}
	}

	public Location getLocationByUniqueId(String uniqueId) throws Exception {
		Key key = new Key();
		key.appendEntry(uniqueId);
		Location location = (Location) findOneByKey(key, null,
				ViewsEnum.Location.name(), Location.class);
		return location;
	}

}
