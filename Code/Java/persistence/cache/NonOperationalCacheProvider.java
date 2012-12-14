package persistence.cache;

import java.util.Map;
import javax.persistence.Cache;
import javax.persistence.PersistenceException;

/**
 * associate with secondary cache, not used
 * 
 * @author weihang chen
 * 
 */
public class NonOperationalCacheProvider implements CacheProvider {
	private Cache cache = new NonOperationalCache();

	public void init(Map<?, ?> properties) {
	}

	public Cache createCache(String name) {
		return this.cache;
	}

	public Cache getCache(String name) throws PersistenceException {
		return null;
	}

	public void shutdown() {
	}

	public void init(String cacheResourceName) throws PersistenceException {
	}
}
