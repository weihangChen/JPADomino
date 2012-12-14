package dao;

import java.util.Vector;

import model.Location;

public interface LocationDAO {
	public Vector<Location> getLocations() throws Exception;

	public void saveLocations(Vector<Location> locations) throws Exception;

	public Location getLocationByUniqueId(String uniqueId) throws Exception;

}
