package persistence.core;

import persistence.cache.Cache;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * associate with second level cache, not implemented
 * 
 * @author weihang chen
 * 
 */
public class EntityManagerSession {
	private static final Log LOG = LogFactory
			.getLog(EntityManagerSession.class);
	private Map<Object, Object> sessionCache;
	private Cache l2Cache;

	@SuppressWarnings("unchecked")
	public EntityManagerSession(Cache cache) {
		this.sessionCache = new ConcurrentHashMap();
		setL2Cache(cache);
	}

	@SuppressWarnings("unchecked")
	protected <T> T lookup(Class<T> entityClass, Object id) {
		String key = cacheKey(entityClass, id);
		LOG.debug("Reading from L1 >> " + key);
		Object o = this.sessionCache.get(key);

		if (o == null) {
			LOG.debug("Reading from L2 >> " + key);
			Cache c = getL2Cache();
			if (c != null) {
				o = c.get(key);
				if (o != null) {
					LOG.debug("Found item in second level cache!");
				}
			}
		}
		return (T) o;
	}

	protected void store(Object id, Object entity) {
		store(id, entity, Boolean.TRUE.booleanValue());
	}

	protected void store(Object id, Object entity, boolean spillOverToL2) {
		String key = cacheKey(entity.getClass(), id);
		LOG.debug("Writing to L1 >> " + key);
		this.sessionCache.put(key, entity);

		if (!(spillOverToL2))
			return;
		LOG.debug("Writing to L2 >>" + key);

		Cache c = getL2Cache();
		if (c == null)
			return;
		c.put(key, entity);
	}

	protected <T> void remove(Class<T> entityClass, Object id) {
		remove(entityClass, id, Boolean.TRUE.booleanValue());
	}

	protected <T> void remove(Class<T> entityClass, Object id,
			boolean spillOverToL2) {
		String key = cacheKey(entityClass, id);
		LOG.debug("Removing from L1 >> " + key);
		@SuppressWarnings("unused")
		Object o = this.sessionCache.remove(key);

		if (!(spillOverToL2))
			return;
		LOG.debug("Removing from L2 >> " + key);
		Cache c = getL2Cache();
		if (c == null)
			return;
		c.evict(entityClass, key);
	}

	private String cacheKey(Class<?> clazz, Object id) {
		return clazz.getName() + "_" + id;
	}

	@SuppressWarnings("unchecked")
	public final void clear() {
		this.sessionCache = new ConcurrentHashMap();

		if (getL2Cache() == null)
			return;
		getL2Cache().evictAll();
	}

	public Cache getL2Cache() {
		return this.l2Cache;
	}

	public void setL2Cache(Cache l2Cache) {
		this.l2Cache = l2Cache;
	}
}
