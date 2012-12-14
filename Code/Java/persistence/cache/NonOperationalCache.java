package persistence.cache;

/**
 * associate with secondary cache, not used
 * 
 * @author weihang chen
 * 
 */
public class NonOperationalCache implements Cache, javax.persistence.Cache {
	public int size() {
		return 0;
	}

	public void put(Object key, Object value) {
	}

	public Object get(Object key) {
		return null;
	}

	@SuppressWarnings("unchecked")
	public boolean contains(Class paramClass, Object paramObject) {
		return false;
	}

	@SuppressWarnings("unchecked")
	public void evict(Class paramClass, Object paramObject) {
	}

	@SuppressWarnings("unchecked")
	public void evict(Class paramClass) {
	}

	public void evictAll() {
	}
}
