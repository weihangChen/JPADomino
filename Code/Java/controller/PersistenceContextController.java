package controller;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.persistence.EntityManagerFactory;

import persistence.annotation.support.JavaBeanFactory;
import persistence.context.PersistenceCache;
import persistence.core.DominoPersistenceProvider;
import persistence.core.EntityManagerImpl;
import persistence.graph.Node;
import util.JSFUtil;

import model.Location;
import model.ToolBox;

/**
 * Controller class for View PersistenceContextCRUD.xsp, no dao is in use here
 * to simplify the code and put more focus on the persistence context instead.
 * More on dao when persistence context is in use, please read articles below,
 * (their persistence context is container managed, ours is application managed)
 * <p>
 * 1. http://blog.xebia.com/2009/03/09/jpa-implementation-patterns-data-access-
 * objects/
 * <p>
 * 2. http://henk53.wordpress.com/2012/04/15/jsf2-primefaces3-ejb3-jpa2-
 * integration-project/
 * 
 * 
 * @author weihang chen
 * 
 */
public class PersistenceContextController implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * most important Class in this project responsible for all the interaction
	 * with the persistence context
	 */
	public EntityManagerImpl entityManager;
	/**
	 * The editing Location entity from page PersistenceContextCRUD.xsp
	 */
	protected Location location;
	/**
	 * support variable holding all the cached objects within a persistence
	 * context, it is only used for display purpose
	 */
	private PersistenceCache persistenceCacheDisplay;

	/**
	 * initialise support variable persistenceCacheDisplay, kick start a
	 * persistence context
	 */
	public PersistenceContextController() {
		persistenceCacheDisplay = new PersistenceCache();
		EntityManagerFactory emf = DominoPersistenceProvider
				.getEntityManagerFactory();
		entityManager = (EntityManagerImpl) emf.createEntityManager();
		entityManager.begin();
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * there are two collections in the persistence context, one storing all the
	 * headNodes, another storing all the nodes
	 * 
	 * create two variables cacheHeadNodes, cacheAllNodes moving the
	 * nodes.toString to these two variables and put them on the Xpages
	 * requestMap, so we can view cache on PersistenceContextCRUD.xsp
	 */
	public void pushCache() {
		persistenceCacheDisplay = entityManager.getPersistenceCache();
		if (persistenceCacheDisplay != null) {
			Set<Node> headNodesSet = persistenceCacheDisplay.getMainCache()
					.getHeadNodes();
			Iterator<Node> iter = headNodesSet.iterator();
			Vector<String> tmp1 = new Vector<String>();
			while (iter.hasNext()) {
				Node node = iter.next();
				tmp1.add(node.toString());
			}
			JSFUtil.pushData(tmp1, "cacheHeadNodes");
			Collection<Node> nodes = persistenceCacheDisplay.getMainCache()
					.getAllNodes();
			Vector<String> tmp2 = new Vector<String>();
			for (Node node : nodes)
				tmp2.add(node.toString());
			JSFUtil.pushData(tmp2, "cacheAllNodes");
		}
	}

	/**
	 * if Location entity is null, find it using entityManager, if not null,
	 * return it
	 * 
	 * @param locationId
	 * @return Location entity
	 */
	public Location getLocation(String locationId) {
		if (location == null) {
			location = entityManager.find(Location.class, locationId);
		}
		return location;
	}

	/**
	 * create a new ToolBox entity using JavaBeanFactory
	 * 
	 * @return ToolBox entity
	 */
	public ToolBox createNewToolBox() {
		ToolBox toolBox = JavaBeanFactory.getProxy(ToolBox.class);
		return toolBox;
	}

	/**
	 * add the ToolBox to the in-memory collection and submit it to database
	 * 
	 * @param tb
	 */
	public void submitNewToolBox(ToolBox tb) {
		// entityManager.begin();
		addNewToolBox(tb);
		System.out.println("-----------------MERGING + FLUSH START!!!!!!");
		entityManager.merge(location);
		System.out.println("-----------------MERGING + FLUSH END!!!!!!!!");
	}

	/**
	 * persist new entity to database
	 * 
	 * @param tb
	 */
	public void persistNewToolBox(ToolBox tb) {
		addNewToolBox(tb);
		entityManager.persist(tb);
	}

	/**
	 * merge method should only be used when existing entities variables are
	 * changed or new entities are added, it should not be invoked when at
	 * operation remove. result of this method is that the current state of
	 * detached entities are transfered to the corresponding managed entities in
	 * the persistence cache, and persistence cache is synchronised with the
	 * database
	 */
	public void flush() {
		entityManager.merge(location);
	}

	/**
	 * add a in-memory ToolBox to ToolBoxEagerList
	 * 
	 * @param newToolBox
	 *            ToolBoxEagerList
	 */
	public void addNewToolBox(ToolBox newToolBox) {
		newToolBox.setLocationUNID(location.getUnid());
		location.getToolBoxEagerList().add(newToolBox);
	}

	/**
	 * remove a toolbox from in-memory collection ToolBoxEagerList remove the
	 * toolbox from persistence context and database
	 * 
	 * @param toolBox
	 */
	public void removeToolBox(ToolBox toolBox) {
		location.getToolBoxEagerList().remove(toolBox);
		entityManager.remove(toolBox);
	}

	/**
	 * remove the location itself, and cascade remove operation to its children
	 * objects ToolBoxEagerList as well
	 */
	public void removeLocation() {
		entityManager.remove(location);
	}

}
