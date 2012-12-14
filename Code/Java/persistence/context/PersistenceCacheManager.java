package persistence.context;

import persistence.graph.Node;

/**
 * utility class managing cache
 * 
 * @author SWECWI
 * 
 */
public class PersistenceCacheManager {
	private PersistenceCache persistenceCache;

	public PersistenceCacheManager(PersistenceCache pc) {
		this.persistenceCache = pc;
	}

	public void clearPersistenceCache() {
		this.persistenceCache.clean();
	}

	@SuppressWarnings("unused")
	private void cleanIndividualCache(CacheBase cache) {
		for (Node node : cache.getAllNodes()) {
			node.clear();
		}
	}

	/**
	 * this method is invoked before visiting cache nodes before nodes are added
	 * to flush stack, if a node is setTraversed(true) then it will not be
	 * visited again while building flush stack
	 */
	public void markAllNodesNotTraversed() {
		for (Node node : this.persistenceCache.getMainCache().getAllNodes()) {
			node.setTraversed(false);
		}

	}
}
