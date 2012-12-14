package persistence.core;

import persistence.cache.Cache;
import persistence.context.FlushManager;
import persistence.context.PersistenceCache;
import util.Assert;

import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitTransactionType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.commons.util.NotImplementedException;

/**
 * implement interface EntityManager/EntityTransaction, main interface class
 * exposed to API user
 * 
 * @author weihang chen
 * 
 */
public class EntityManagerImpl implements EntityManager, EntityTransaction {
	private static Log logger = LogFactory.getLog(EntityManagerImpl.class);
	private EntityManagerFactory factory;
	private boolean closed;
	FlushModeType flushMode;
	private EntityManagerSession session;
	private Map<String, Object> properties;
	private PersistenceDelegator persistenceDelegator;
	private PersistenceContextType persistenceContextType;
	private PersistenceUnitTransactionType transactionType;
	private PersistenceCache persistenceCache;
	FlushManager flushStackManager;

	public EntityManagerImpl(EntityManagerFactory factory,
			PersistenceUnitTransactionType transactionType,
			PersistenceContextType persistenceContextType) {
		this.closed = false;
		// default flush mode is auto in this API, which means that no manual
		// flush is needed, by invoking remove/persist/merge then flush is
		// invoked afterward
		this.flushMode = FlushModeType.AUTO;
		this.factory = factory;
		logger.debug("Creating EntityManager for persistence unit : "
				+ getPersistenceUnit());
		// sessio object not used
		this.session = new EntityManagerSession((Cache) factory.getCache());
		// persistence cache holds different kinds of cache, the only one being
		// implemented holds all nodes
		this.persistenceCache = new PersistenceCache();
		this.persistenceCache.setPersistenceContextType(persistenceContextType);
		// most important class in the whole API, all detail operation
		// implementation is initialised by this class
		// instance of persistenceDelegator is IOC everywhere including
		// lazyloader
		this.persistenceDelegator = new PersistenceDelegator(this.session,
				this.persistenceCache);
		this.persistenceContextType = persistenceContextType;
		this.transactionType = transactionType;

		logger.debug("Created EntityManager for persistence unit : "
				+ getPersistenceUnit());
	}

	@SuppressWarnings("unchecked")
	public EntityManagerImpl(EntityManagerFactory factory, Map properties,
			PersistenceUnitTransactionType transactionType,
			PersistenceContextType persistenceContextType) {
		this(factory, transactionType, persistenceContextType);
		this.properties = properties;
	}

	/**
	 * find one entity using Key instance
	 */
	public final <E> E find(Class<E> entityClass, Object primaryKey) {
		checkClosed();
		Assert
				.notNull(primaryKey,
						"PrimaryKey value must not be null for object you want to find.");
		return getPersistenceDelegator().find(entityClass, primaryKey);
	}

	/**
	 * remove an entity and flush to database
	 */
	public final void remove(Object e) {
		checkClosed();
		checkTransactionNeeded();
		Assert.notNull(e, "Entity to be removed must not be null.");
		getPersistenceDelegator().remove(e);
	}

	/**
	 * merge detached objects into persistence cache and synchronise the updated
	 * cache with database
	 */
	public final <E> E merge(E e) {
		checkClosed();
		checkTransactionNeeded();
		Assert.notNull(e, "Entity to be merged must not be null.");
		return getPersistenceDelegator().merge(e);
	}

	/**
	 * persist a new object into persistence cache and synchronise the update
	 * cache with database
	 */
	public final void persist(Object e) {
		checkClosed();
		checkTransactionNeeded();
		Assert.notNull(e, "Entity to be persisted must not be null.");
		getPersistenceDelegator().persist(e);
	}

	/**
	 * not used
	 */
	public final void clear() {
		checkClosed();
		this.session.clear();
		if (PersistenceUnitTransactionType.JTA.equals(this.transactionType))
			return;
		this.persistenceDelegator.clear();
	}

	/**
	 * kill the pointers, so the unreferenced objects can be GC
	 */
	public final void close() {
		checkClosed();
		this.session.clear();
		this.session = null;
		this.persistenceDelegator.close();

		if (!(PersistenceUnitTransactionType.JTA.equals(this.transactionType))) {
			this.persistenceDelegator.clear();
		}
		this.closed = true;
	}

	public final boolean contains(Object entity) {
		return false;
	}

	/**
	 * not implemented
	 */
	public final Query createQuery(String query) {
		// return this.persistenceDelegator.createQuery(query);
		return null;
	}

	/**
	 * synchronise persistence cache state with database
	 */
	public final void flush() {
		checkClosed();
		this.persistenceDelegator.flush();
	}

	public final Object getDelegate() {
		return null;
	}

	public final Query createNamedQuery(String name) {
		return null;
		// return this.persistenceDelegator.createQuery(name);
	}

	public final Query createNativeQuery(String sqlString) {
		throw new NotImplementedException(
				"Please use createNativeQuery(String sqlString, Class resultClass) instead. ");
	}

	/**
	 * not implemented
	 */
	@SuppressWarnings("unchecked")
	public final Query createNativeQuery(String sqlString, Class resultClass) {
		// ApplicationMetadata appMetadata = DominoMetadata.INSTANCE
		// .getApplicationMetadata();
		// if (appMetadata.getQuery(sqlString) == null) {
		// appMetadata.addQueryToCollection(sqlString, sqlString, true,
		// resultClass);
		// }
		// return this.persistenceDelegator.createQuery(sqlString);
		return null;
	}

	public final Query createNativeQuery(String sqlString,
			String resultSetMapping) {
		throw new NotImplementedException("TODO");
	}

	public final <T> T getReference(Class<T> entityClass, Object primaryKey) {
		throw new NotImplementedException("TODO");
	}

	public final FlushModeType getFlushMode() {
		return this.flushMode;
	}

	public final EntityTransaction getTransaction() {
		if (this.transactionType == PersistenceUnitTransactionType.JTA) {
			throw new IllegalStateException(
					"A JTA EntityManager cannot use getTransaction()");
		}
		return this;
	}

	public final void joinTransaction() {
		throw new NotImplementedException("TODO");
	}

	public final void lock(Object entity, LockModeType lockMode) {
		throw new NotImplementedException("TODO");
	}

	public final void refresh(Object entity) {
		checkTransactionNeeded();
		throw new NotImplementedException("TODO");
	}

	public <T> T find(Class<T> paramClass, Object paramObject,
			Map<String, Object> paramMap) {
		throw new NotImplementedException("TODO");
	}

	public <T> T find(Class<T> paramClass, Object paramObject,
			LockModeType paramLockModeType) {
		throw new NotImplementedException("TODO");
	}

	public <T> T find(Class<T> paramClass, Object paramObject,
			LockModeType paramLockModeType, Map<String, Object> paramMap) {
		throw new NotImplementedException("TODO");
	}

	public void lock(Object paramObject, LockModeType paramLockModeType,
			Map<String, Object> paramMap) {
		throw new NotImplementedException("TODO");
	}

	public void refresh(Object paramObject, Map<String, Object> paramMap) {
		throw new NotImplementedException("TODO");
	}

	public void refresh(Object paramObject, LockModeType paramLockModeType) {
		throw new NotImplementedException("TODO");
	}

	public void refresh(Object paramObject, LockModeType paramLockModeType,
			Map<String, Object> paramMap) {
		throw new NotImplementedException("TODO");
	}

	public void detach(Object paramObject) {
		throw new NotImplementedException("TODO");
	}

	public LockModeType getLockMode(Object paramObject) {
		throw new NotImplementedException("TODO");
	}

	public void setProperty(String paramString, Object paramObject) {
		throw new NotImplementedException("TODO");
	}

	public <T> TypedQuery<T> createQuery(CriteriaQuery<T> paramCriteriaQuery) {
		throw new NotImplementedException("TODO");
	}

	public <T> TypedQuery<T> createQuery(String paramString, Class<T> paramClass) {
		throw new NotImplementedException("TODO");
	}

	public <T> TypedQuery<T> createNamedQuery(String paramString,
			Class<T> paramClass) {
		throw new NotImplementedException("TODO");
	}

	public <T> T unwrap(Class<T> paramClass) {
		throw new NotImplementedException("TODO");
	}

	public final void setFlushMode(FlushModeType flushMode) {
		this.flushMode = flushMode;
		this.persistenceDelegator.setFlushMode(flushMode);
	}

	public Map<String, Object> getProperties() {
		return this.properties;
	}

	public EntityManagerFactory getEntityManagerFactory() {
		return this.factory;
	}

	public CriteriaBuilder getCriteriaBuilder() {
		return this.factory.getCriteriaBuilder();
	}

	public Metamodel getMetamodel() {
		return this.factory.getMetamodel();
	}

	public final boolean isOpen() {
		return (!(this.closed));
	}

	private void checkClosed() {
		if (isOpen())
			return;
		throw new IllegalStateException(
				"EntityManager has already been closed.");
	}

	private void checkTransactionNeeded() {
		if ((this.persistenceContextType != PersistenceContextType.TRANSACTION)
				|| (this.persistenceDelegator.isTransactionInProgress())) {
			return;
		}
		throw new TransactionRequiredException(
				"no transaction is in progress for a TRANSACTION type persistence context");
	}

	private String getPersistenceUnit() {
		return ((String) this.factory.getProperties()
				.get("persistenceUnitName"));
	}

	@SuppressWarnings("unused")
	private EntityManagerSession getSession() {
		return this.session;
	}

	private PersistenceDelegator getPersistenceDelegator() {
		return this.persistenceDelegator;
	}

	public PersistenceContextType getPersistenceContextType() {
		return this.persistenceContextType;
	}

	/**
	 * start a transaction
	 */
	public void begin() {
		this.persistenceDelegator.begin();
	}

	/**
	 * almost same as flush()
	 */
	public void commit() {
		checkClosed();
		this.persistenceDelegator.commit();
	}

	public boolean getRollbackOnly() {
		if (!(isActive())) {
			throw new IllegalStateException("No active transaction found");
		}
		return this.persistenceDelegator.getRollbackOnly();
	}

	public void setRollbackOnly() {
		this.persistenceDelegator.setRollbackOnly();
	}

	public boolean isActive() {
		return ((isOpen()) && (this.persistenceDelegator.isActive()));
	}

	/**
	 * not implemented
	 */
	public void rollback() {
		checkClosed();
		this.persistenceDelegator.rollback();
	}

	// used to display the cache
	public PersistenceCache getPersistenceCache() {
		return persistenceCache;
	}

}
