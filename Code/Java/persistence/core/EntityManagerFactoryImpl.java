package persistence.core;

import persistence.cache.CacheProvider;
import persistence.cache.NonOperationalCacheProvider;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.commons.util.NotImplementedException;

/**
 * used to create EntityManager instances
 * 
 * @author weihang chen
 * 
 */
public class EntityManagerFactoryImpl implements EntityManagerFactory {
	private static Log logger = LogFactory
			.getLog(EntityManagerFactoryImpl.class);
	private boolean closed;
	private Map<String, Object> properties;
	private CacheProvider cacheProvider;
	private static EntityManagerFactoryImpl entityManagerFactory;
	String[] persistenceUnits;
	PersistenceUnitTransactionType transactionType;

	public static synchronized EntityManagerFactoryImpl getInstance(
			String persistenceUnit, Map<String, Object> properties) {
		if (entityManagerFactory == null)
			entityManagerFactory = new EntityManagerFactoryImpl(
					persistenceUnit, properties);
		return entityManagerFactory;
	}

	@SuppressWarnings("unchecked")
	private EntityManagerFactoryImpl(PersistenceUnitInfo persistenceUnitInfo,
			Map props) {
		this((persistenceUnitInfo != null) ? persistenceUnitInfo
				.getPersistenceUnitName() : null, props);
	}

	private EntityManagerFactoryImpl() {
	}

	/**
	 * no need to read the detail in this constructor, no properties or
	 * persistenceunits are in used
	 * 
	 * @param persistenceUnit
	 * @param properties
	 */
	@SuppressWarnings("unchecked")
	private EntityManagerFactoryImpl(String persistenceUnit,
			Map<String, Object> properties) {
		this.closed = false;

		if (properties == null) {
			properties = new HashMap();
		}
		properties.put("persistenceUnitName", persistenceUnit);
		this.properties = properties;
		this.persistenceUnits = persistenceUnit.split(",");

		this.cacheProvider = initSecondLevelCache();
		this.cacheProvider.createCache("Domino");

		logger.info("Loading Client(s) For Persistence Unit(s) "
				+ persistenceUnit);
		// Set txTypes = new HashSet();
		// for (String pu : this.persistenceUnits) {
		// PersistenceUnitTransactionType txType = DominoMetadataManager
		// .getPersistenceUnitMetadata(pu).getTransactionType();
		// txTypes.add(txType);
		// ClientResolver.getClientFactory(pu).load(pu);
		// }
		//		
		// if (txTypes.size() != 1) {
		// throw new IllegalArgumentException(
		// "For polyglot persistence, it is mandatory for all persistence units to have same Transction type.");
		// }

		// this.transactionType = ((PersistenceUnitTransactionType) txTypes
		// .iterator().next());
		//		
		this.transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;
		logger.info("EntityManagerFactory created for persistence unit : "
				+ persistenceUnit);
	}

	public final void close() {
		this.closed = true;

		if (this.cacheProvider != null) {
			this.cacheProvider.shutdown();
		}
		// for (String pu : this.persistenceUnits) {
		// ((ClientLifeCycleManager) ClientResolver.getClientFactory(pu))
		// .destroy();
		// }
	}

	/**
	 * method to create a new EntityManager
	 */
	public final EntityManager createEntityManager() {
		return new EntityManagerImpl(this, this.transactionType,
				PersistenceContextType.EXTENDED);
	}

	@SuppressWarnings("unchecked")
	public final EntityManager createEntityManager(Map map) {
		return new EntityManagerImpl(this, map, this.transactionType,
				PersistenceContextType.EXTENDED);
	}

	public final boolean isOpen() {
		return (!(this.closed));
	}

	public CriteriaBuilder getCriteriaBuilder() {
		throw new NotImplementedException("TODO");
	}

	public Metamodel getMetamodel() {
		// return DominoMetadataManager.getMetamodel(getPersistenceUnits());
		return null;
	}

	public Map<String, Object> getProperties() {
		return this.properties;
	}

	public Cache getCache() {
		return this.cacheProvider.getCache("Domino");
	}

	public PersistenceUnitUtil getPersistenceUnitUtil() {
		throw new NotImplementedException("TODO");
	}

	public PersistenceUnitTransactionType getTransactionType() {
		return this.transactionType;
	}

	public void setTransactionType(
			PersistenceUnitTransactionType transactionType) {
		this.transactionType = transactionType;
	}

	/**
	 * no second level cache is implemented
	 * 
	 */
	@SuppressWarnings("unchecked")
	private CacheProvider initSecondLevelCache() {
		String classResourceName = (String) getProperties().get(
				"domino.cache.config.resource");
		String cacheProviderClassName = (String) getProperties().get(
				"domino.cache.provider.class");

		CacheProvider cacheProvider = null;
		if (cacheProviderClassName != null) {
			try {
				Class cacheProviderClass = Class
						.forName(cacheProviderClassName);
				cacheProvider = (CacheProvider) cacheProviderClass
						.newInstance();
				cacheProvider.init(classResourceName);
			} catch (ClassNotFoundException e) {
				throw new PersistenceException(
						"Could not find class "
								+ cacheProviderClassName
								+ ". Check whether you spelled it correctly in persistence.xml",
						e);
			} catch (InstantiationException e) {
				throw new PersistenceException("Could not instantiate "
						+ cacheProviderClassName, e);
			} catch (IllegalAccessException e) {
				throw new PersistenceException(e);
			}
		}
		if (cacheProvider == null) {
			cacheProvider = new NonOperationalCacheProvider();
		}
		return cacheProvider;
	}

	@SuppressWarnings("unused")
	private String[] getPersistenceUnits() {
		return this.persistenceUnits;
	}
}
