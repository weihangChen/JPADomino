package persistence.cache.ehcache;

import persistence.cache.CacheProvider;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map; //import java.util.Set;

import javax.persistence.PersistenceException;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.event.CacheEventListener; //import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.util.ClassLoaderUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * associate with secondary cache, not used
 * 
 * @author weihang chen
 * 
 */
public class EhCacheProvider implements CacheProvider {
	private static final Log log = LogFactory.getLog(EhCacheProvider.class);
	private CacheManager manager;
	private javax.persistence.Cache cache;
	@SuppressWarnings("unused")
	private static final String NET_SF_EHCACHE_CONFIGURATION_RESOURCE_NAME = "net.sf.ehcache.configurationResourceName";
	@SuppressWarnings("unused")
	private boolean initializing;
	private List<CacheEventListener> listeners;

	@SuppressWarnings("unchecked")
	public EhCacheProvider() {
		this.listeners = new ArrayList();
	}

	public void init(String cacheResourceName) {
		if (this.manager != null) {
			log
					.warn("Attempt to restart an already started CacheFactory. Using previously created EhCacheFactory.");
			return;
		}
		this.initializing = true;
		try {
			String configurationResourceName = cacheResourceName;
			if ((configurationResourceName == null)
					|| (configurationResourceName.length() == 0)) {
				this.manager = new CacheManager();
			} else {
				if (!(configurationResourceName.startsWith("/"))) {
					configurationResourceName = "/" + configurationResourceName;
					log.info("prepending / to " + configurationResourceName
							+ ". It should be placed in the root"
							+ "of the classpath rather than in a package.");
				}

				URL url = loadResource(configurationResourceName);
				this.manager = new CacheManager(url);
			}
		} catch (net.sf.ehcache.CacheException e) {
			if (e
					.getMessage()
					.startsWith(
							"Cannot parseConfiguration CacheManager. Attempt to create a new instance of CacheManager using the diskStorePath"))
				;
			throw e;
		} finally {
			this.initializing = false;
		}
	}

	public synchronized void init(Map<?, ?> properties) {
		if (this.manager != null) {
			log
					.warn("Attempt to restart an already started CacheFactory. Using previously created EhCacheFactory.");
			return;
		}
		this.initializing = true;
		try {
			String configurationResourceName = null;
			if (properties != null) {
				configurationResourceName = (String) properties
						.get("net.sf.ehcache.configurationResourceName");
			}
			if ((configurationResourceName == null)
					|| (configurationResourceName.length() == 0)) {
				this.manager = new CacheManager();
			} else {
				if (!(configurationResourceName.startsWith("/"))) {
					configurationResourceName = "/" + configurationResourceName;
					log.info("prepending / to " + configurationResourceName
							+ ". It should be placed in the root"
							+ "of the classpath rather than in a package.");
				}

				URL url = loadResource(configurationResourceName);
				this.manager = new CacheManager(url);
			}
		} catch (net.sf.ehcache.CacheException e) {
			if (e
					.getMessage()
					.startsWith(
							"Cannot parseConfiguration CacheManager. Attempt to create a new instance of CacheManager using the diskStorePath"))
				;
			throw new PersistenceException(e);
		} finally {
			this.initializing = false;
		}
	}

	private URL loadResource(String configurationResourceName) {
		ClassLoader standardClassloader = ClassLoaderUtil
				.getStandardClassLoader();
		URL url = null;
		if (standardClassloader != null) {
			url = standardClassloader.getResource(configurationResourceName);
		}
		if (url == null) {
			url = super.getClass().getResource(configurationResourceName);
		}
		log.info("Creating EhCacheFactory from a specified resource: "
				+ configurationResourceName + " Resolved to URL: " + url);

		if (url == null) {
			log
					.warn("A configurationResourceName was set to "
							+ configurationResourceName
							+ " but the resource could not be loaded from the classpath."
							+ "Ehcache will configure itself using defaults.");
		}

		return url;
	}

	public javax.persistence.Cache createCache(String name) {
		if (this.manager == null) {
			throw new PersistenceException(
					"CacheFactory was not initialized. Call init() before creating a cache.");
		}
		try {
			net.sf.ehcache.Cache cache = this.manager.getCache(name);
			if (cache == null) {
				log
						.warn("Could not find a specific ehcache configuration for cache named ["
								+ name + "]; using defaults.");

				this.manager.addCache(name);
				cache = this.manager.getCache(name);
			}
			Ehcache backingCache = cache;
			if ((!(backingCache.getCacheEventNotificationService()
					.hasCacheEventListeners()))
					&& (this.listeners.size() > 0)) {
				for (CacheEventListener listener : this.listeners) {
					if (!(backingCache.getCacheEventNotificationService()
							.getCacheEventListeners().contains(listener))) {
						backingCache.getCacheEventNotificationService()
								.registerListener(listener);
					}

				}

			}

			this.cache = new EhCacheWrapper(cache);
			return this.cache;
		} catch (net.sf.ehcache.CacheException e) {
			throw new PersistenceException("Could not create cache: " + name, e);
		}
	}

	public javax.persistence.Cache getCache(String cacheName)
			throws PersistenceException {
		if (this.cache == null) {
			this.cache = createCache(cacheName);
		}

		return this.cache;
	}

	public void shutdown() {
		if (this.manager == null)
			return;
		this.manager.shutdown();
		this.manager = null;
	}

	public void clearAll() {
		this.manager.clearAll();
	}

	public CacheManager getCacheManager() {
		return this.manager;
	}

	public void addDefaultListener(CacheEventListener cacheEventListener) {
		this.listeners.add(cacheEventListener);
	}
}
