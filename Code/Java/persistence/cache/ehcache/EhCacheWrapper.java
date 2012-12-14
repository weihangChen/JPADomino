package persistence.cache.ehcache;

import com.ibm.commons.util.NotImplementedException;

import net.sf.ehcache.Element;

/**
 * associate with secondary cache, not used
 * 
 * @author weihang chen
 * 
 */
public class EhCacheWrapper implements persistence.cache.Cache {
	private net.sf.ehcache.Cache ehcache;

	public EhCacheWrapper(net.sf.ehcache.Cache ehcache) {
		this.ehcache = ehcache;
	}

	public Object get(Object key) {
		Element element = this.ehcache.get(key);
		return ((element == null) ? null : element.getObjectValue());
	}

	public void put(Object key, Object value) {
		this.ehcache.put(new Element(key, value));
	}

	public int size() {
		return this.ehcache.getSize();
	}

	@SuppressWarnings("unchecked")
	public boolean contains(Class arg0, Object arg1) {
		return (this.ehcache.get(arg1) != null);
	}

	@SuppressWarnings("unchecked")
	public void evict(Class arg0) {
		throw new NotImplementedException("TODO");
	}

	@SuppressWarnings("unchecked")
	public void evict(Class arg0, Object arg1) {
		this.ehcache.remove(arg1);
	}

	public void evictAll() {
		this.ehcache.removeAll();
	}

}
