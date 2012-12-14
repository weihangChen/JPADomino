package persistence.cache;

import java.util.Map;
import javax.persistence.Cache;

/**
 * cache provider which should be extended by concrete secondary cache provider
 * such as EhCacheProvider not used
 * 
 * @author weihang chen
 * 
 */
public abstract interface CacheProvider {
	public abstract void init(Map<?, ?> paramMap);

	public abstract void init(String paramString);

	public abstract Cache createCache(String paramString);

	public abstract Cache getCache(String paramString);

	public abstract void shutdown();
}
