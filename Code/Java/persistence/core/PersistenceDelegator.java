package persistence.core;

import persistence.client.Client;
import persistence.client.domino.DominoDBClient;
import persistence.client.domino.DominoEntityReader;

import persistence.graph.Node;
import persistence.graph.NodeLink;
import persistence.graph.ObjectGraph;
import persistence.graph.ObjectGraphBuilder;
import persistence.lifecycle.states.ManagedState;
import persistence.lifecycle.states.RemovedState;
import persistence.lifecycle.states.TransientState;
import persistence.metadata.MetadataManager;
import persistence.metadata.model.EntityMetadata;
import persistence.context.FlushManager;
import persistence.context.FlushStack;
import persistence.context.MainCache;
import persistence.context.PersistenceCache;
import persistence.context.PersistenceCacheManager;
import persistence.event.EntityEventDispatcher;
import util.CloneUtil;
import util.CommonUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.FlushModeType;

import lotus.domino.Database;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.xsp.model.domino.DominoUtils;

/**
 * most important class in this API, all operations passed in from EntityManager
 * are implemented in this class
 * 
 * @author weihang chen
 * 
 */
public class PersistenceDelegator {
	private static final Log log = LogFactory
			.getLog(PersistenceDelegator.class);
	private boolean closed = false;
	/**
	 * associate with second level cache, not used
	 */
	private EntityManagerSession session;
	/**
	 * multiple DBClient can be supported, but this API supports only
	 * DominoDBClient
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Client> clientMap;
	/**
	 * used to handle event such as @presave @posesave, not implemented
	 */
	private EntityEventDispatcher eventDispatcher;
	/**
	 * not used
	 */
	boolean isRelationViaJoinTable;
	/**
	 * flush mode is AUTO in this API, persistence cache state will synchronise
	 * with database at the end of all operations (remove, persist, merge)
	 */
	private FlushModeType flushMode = FlushModeType.AUTO;
	/**
	 * used to build object graph, extremely important, since almost all
	 * operations require the building of an object graph
	 */
	private ObjectGraphBuilder graphBuilder;
	/**
	 * used to maintain flush stack
	 */
	private FlushManager flushManager;
	/**
	 * if a transaction is not in progress, no operation should be executed
	 */
	private boolean isTransactionInProgress;
	/**
	 * first level cache
	 */
	private PersistenceCache persistenceCache;

	public PersistenceDelegator(EntityManagerSession session,
			PersistenceCache pc) {
		this.session = session;
		this.eventDispatcher = new EntityEventDispatcher();
		this.graphBuilder = new ObjectGraphBuilder();
		this.flushManager = new FlushManager();
		this.persistenceCache = pc;
	}

	/**
	 * save a new Domino Entity to persistence cache and synchronise the
	 * persistence cache with database <br>
	 * 1. build object graph<br>
	 * 2. add head node from graph to cache head node map<br>
	 * 3. invoke persist() on the headnode, recursively propagate PERSIST to
	 * other nodes with the PERSIST/ALL CascadeType <br>
	 * 4. flush(), synchronise persistence cache with database<br>
	 * 5. clear the graph
	 * 
	 * @param e
	 */
	public void persist(Object e) {
		System.out
				.println("-----------------------------------PERSISTE STARTS-------------------------------------");
		// pre events might be needed later
		// getEventDispatcher().fireEventListeners(metadata, e,
		// PrePersist.class);
		ObjectGraph graph = this.graphBuilder.getObjectGraph(e,
				new TransientState(), getPersistenceCache());

		Node headNode = graph.getHeadNode();
		if (headNode.getParents() == null) {
			headNode.setHeadNode(true);
			getPersistenceCache().getMainCache().addHeadNode(headNode);
		}
		headNode.persist();
		flush();
		graph.getNodeMapping().clear();
		graph = null;
		System.out
				.println("-----------------------------------PERSISTE ENDS-------------------------------------");

		// post events might be needed later
		// getEventDispatcher().fireEventListeners(metadata, e,
		// PostPersist.class);

		log.debug("Data persisted successfully for entity : " + e.getClass());
	}

	/**
	 * find an object with Key<br>
	 * When find() is issued, it will try to find from persistence cache, if a
	 * Node with that specific id is found, clone the entity within the Node and
	 * return the clone. If a Node with that specific id can’t be found from
	 * cache. Create a new Node in ManagedState, find the document from
	 * database, convert it to Domino Entity and assign it to the Node. Build
	 * object graph using the new Node as the head Node, synchronize the object
	 * graph with the persistence cache using addGraphToCache(), clone the
	 * entity and return the detached entity. Operation Find does not only
	 * initialize one single Java object, but recursively propagate the same
	 * find mechanism to all the children objects as well.
	 * 
	 * 
	 * @param <E>
	 * @param entityClass
	 * @param primaryKey
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <E> E find(Class<E> entityClass, Object primaryKey) {
		boolean isCached = true;
		EntityMetadata entityMetadata = getMetadata(entityClass);
		String nodeId = ObjectGraphBuilder.getNodeId(primaryKey, entityClass);
		MainCache mainCache = (MainCache) getPersistenceCache().getMainCache();

		Node node = mainCache.getNodeFromCache(nodeId);
		// if node can not be found from cache, create a new one, and populate
		// all its children objects as well

		if (node == null) {
			isCached = false;
			node = new Node(nodeId, entityClass, new ManagedState(),
					getPersistenceCache());
			DominoDBClient client = (DominoDBClient) getClient(entityMetadata);
			node.setClient(client);
			node.setPersistenceDelegator(this);
			node.find();
		} else
			System.out
					.println("NODE ALREADY EXISTIS IN CACHE, MAKE CLONE OF EXISTING MANAGED ENTITY AND RETURN THE DETACHED ONE: "
							+ node);

		Object nodeData = node.getData();
		if (nodeData == null)
			return null;

		if (!isCached) {
			ObjectGraph graph = new ObjectGraphBuilder().getObjectGraph(
					nodeData, new ManagedState(), getPersistenceCache());
			// JSFUtil.pushData(graph, node.getNodeId());

			// cache the graph
			System.out
					.println("------------------------ADD GRAPH TO CACHE STARTS-----------------------");
			getPersistenceCache().getMainCache().addGraphToCache(graph,
					getPersistenceCache());
			System.out
					.println("------------------------ADD GRAPH TO CACHE ENDS-----------------------");

		}
		// return (E) nodeData;
		// System.out
		// .println("METHOD SIGNATURE: "
		// + CommonUtil.getMethodName(this.getClass().toString())
		// +
		// " /METHOD DESCRIPTION: deepclone an managed entity and return a detached one -- target: "
		// + nodeData);
		E clone = (E) CloneUtil.cloneDominoEntity(nodeData);
		// System.out
		// .println("METHOD SIGNATURE: "
		// + CommonUtil.getMethodName(this.getClass().toString())
		// +
		// " /METHOD DESCRIPTION: deepclone an managed entity and return a detached one -- clone: "
		// + clone);
		// return (E) nodeData;
		return clone;
	}

	/**
	 * not used
	 * 
	 * @param <E>
	 * @param entityClass
	 * @param primaryKeys
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <E> List<E> find(Class<E> entityClass, Object[] primaryKeys) {
		List entities = new ArrayList();
		Set pKeys = new HashSet(Arrays.asList(primaryKeys));
		for (Iterator i$ = pKeys.iterator(); i$.hasNext();) {
			Object primaryKey = i$.next();
			entities.add(find(entityClass, primaryKey));
		}
		return entities;
	}

	/**
	 * remove an entity from persistence cache + database<br>
	 * 1. build object graph<br>
	 * 2. change the headnode's state to REMOVE, recursively propagate REMOVE
	 * operation to other nodes with the REMOVE/ALL CascadeType<br>
	 * 3. synchronise persistence cache with database by invoking flush
	 * 
	 * @param e
	 */
	public void remove(Object e) {
		// getEventDispatcher().fireEventListeners(metadata, e,
		// PreRemove.class);
		ObjectGraph graph = this.graphBuilder.getObjectGraph(e,
				new ManagedState(), getPersistenceCache());
		Node headNode = graph.getHeadNode();
		// 1111 different from Kundera, only the location object is in the
		// headnodes, and its not dirty, flush method goes through all headnodes
		// and find none of it being dirty, notthing happends
		getPersistenceCache().getMainCache().addHeadNode(headNode);
		// 1111
		if (headNode.getParents() == null) {
			headNode.setHeadNode(true);
		}
		headNode.remove();
		flush();
		// getEventDispatcher().fireEventListeners(metadata, e,
		// PostRemove.class);
		log.debug("Data removed successfully for entity : " + e.getClass());
	}

	/**
	 * FlutModeType in this project is by default AUTO, there are a couple of
	 * steps in the flush process
	 * <p>
	 * 1. build stack <br>
	 * 2. go through the nodes from stack, call flush
	 */
	@SuppressWarnings("unchecked")
	public void flush() {
		if (FlushModeType.COMMIT.equals(getFlushMode())) {
			return;
		}
		if (!(FlushModeType.AUTO.equals(getFlushMode()))) {
			return;
		}
		this.flushManager.buildFlushStack(getPersistenceCache());

		FlushStack fs = getPersistenceCache().getFlushStack();
		System.out.println(CommonUtil.getMethodName(this.getClass().toString())
				+ ": " + fs);
		// .debug("Flushing following flush stack to database(s) (showing stack objects from top to bottom):\n"
		// + fs);
		Node node;
		while (!(fs.isEmpty())) {
			node = (Node) fs.pop();
			if ((node.isInState(ManagedState.class))
					|| (node.isInState(RemovedState.class))) {
				// metadata is null, hardcode through it
				// EntityMetadata metadata = getMetadata(node.getDataClass());
				// node.setClient(getClient(metadata));
				node.setClient(getClient(null));
				node.flush();
				Map parents = node.getParents();
				Map children = node.getChildren();
				// relation links need to be updated as well
				if ((parents != null) && (!(parents.isEmpty()))) {
					for (Object obj : parents.keySet()) {
						NodeLink parentNodeLink = (NodeLink) obj;
						parentNodeLink.addLinkProperty(
								NodeLink.LinkProperty.LINK_VALUE,
								ObjectGraphBuilder
										.getEntityId(node.getNodeId()));
					}
				}
				if ((children != null) && (!(children.isEmpty()))) {
					for (Object obj : children.keySet()) {
						NodeLink childNodeLink = (NodeLink) obj;
						childNodeLink.addLinkProperty(
								NodeLink.LinkProperty.LINK_VALUE,
								ObjectGraphBuilder
										.getEntityId(node.getNodeId()));
					}
				}
			}
		}

		// Map joinTableDataMap = getPersistenceCache().getJoinTableDataMap();
		// for (Iterator i$ = joinTableDataMap.values().iterator();
		// i$.hasNext();) {
		// System.out.println("joinTableDataMap");
		// JoinTableData jtData = (JoinTableData) i$.next();
		// EntityMetadata m =
		// KunderaMetadataManager.getEntityMetadata(jtData
		// .getEntityClass());
		// EntityMetadata m = null;
		// Client client = getClient(m);
		// if (JoinTableData.OPERATION.INSERT.equals(jtData.getOperation()))
		// {
		// client.persistJoinTable(jtData);
		// } else if (JoinTableData.OPERATION.DELETE.equals(jtData
		// .getOperation())) {
		// for (i$ = jtData.getJoinTableRecords().keySet().iterator(); i$
		// .hasNext();) {
		// Object pk = i$.next();
		// client.deleteByColumn(jtData.getJoinTableName(), m
		// .getIdColumn().getName(), pk);
		// }
		// }
		// }

		// JoinTableData jtData;
		// EntityMetadata m;
		// Client client;
		// Iterator i$;
		// joinTableDataMap.clear();
	}

	/**
	 * merge detached entities into persistence cache, synchronise with database<br>
	 * 1. build object graph<br>
	 * 2. call merge from the head node, recursively propagate merge to other
	 * nodes with the MERGE/ALL CascadeType <br>
	 * 3. flush
	 * 
	 * @param <E>
	 * @param e
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <E> E merge(E e) {
		log.debug("Merging Entity : " + e);
		// getEventDispatcher().fireEventListeners(m, e, PreUpdate.class);
		ObjectGraph graph = this.graphBuilder.getObjectGraph(e,
				new ManagedState(), getPersistenceCache());

		Node headNode = graph.getHeadNode();
		if (headNode.getParents() == null) {
			headNode.setHeadNode(true);
		}
		headNode.merge();
		System.out.println("after merging the persistence cache "
				+ persistenceCache.getMainCache().toString()
				+ " is in size of " + persistenceCache.getMainCache().size()
				+ " and headnode is " + headNode.toString());

		flush();

		// getEventDispatcher().fireEventListeners(m, e, PostUpdate.class);
		return (E) headNode.getData();
	}

	/**
	 * it's supposed to support multiple DBClient types, but in this API, only
	 * DominoDBClient is in use
	 * 
	 * @param m
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Client getClient(EntityMetadata m) {
		// IndexManager indexManager = null;
		DominoEntityReader entityReader = new DominoEntityReader();
		Database dominoDb = DominoUtils.getCurrentDatabase();
		DominoDBClient dbClient = new DominoDBClient(dominoDb, entityReader,
				"hardcoded info");
		return dbClient;

		/**
		 * choose dbclient based on entityMetadata, origianl code
		 */
		/*
		 * Client client = null; String persistenceUnit =
		 * m.getPersistenceUnit(); if ((this.clientMap == null) ||
		 * (this.clientMap.isEmpty())) { this.clientMap = new HashMap(); client
		 * = ClientResolver.discoverClient(persistenceUnit);
		 * this.clientMap.put(persistenceUnit, client); } else if
		 * (this.clientMap.get(persistenceUnit) == null) { client =
		 * ClientResolver.discoverClient(persistenceUnit);
		 * this.clientMap.put(persistenceUnit, client); } else { client =
		 * (Client) this.clientMap.get(persistenceUnit); } return client;
		 */
	}

	@SuppressWarnings("unused")
	private EntityManagerSession getSession() {
		return this.session;
	}

	@SuppressWarnings("unused")
	private EntityEventDispatcher getEventDispatcher() {
		return this.eventDispatcher;
	}

	/**
	 * not used
	 * 
	 * @param <E>
	 * @param entityClass
	 * @param embeddedColumnMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <E> List<E> find(Class<E> entityClass,
			Map<String, String> embeddedColumnMap) {
		EntityMetadata entityMetadata = getMetadata(entityClass);

		List entities = new ArrayList();
		entities = getClient(entityMetadata).find(entityClass,
				embeddedColumnMap);
		return entities;
	}

	// public Query createQuery(String jpaQuery) {
	// Query query = new QueryResolver()
	// .getQueryImplementation(jpaQuery, this);
	// return query;
	// }

	/**
	 * check if entityManager/transaction is still opened
	 */
	public final boolean isOpen() {
		return (!(this.closed));
	}

	/**
	 * clear references so allocated memory will be GC, close
	 * entityManager/transaction
	 */
	@SuppressWarnings("unchecked")
	public final void close() {
		this.eventDispatcher = null;
		if ((this.clientMap != null) && (!(this.clientMap.isEmpty()))) {
			for (Client client : this.clientMap.values()) {
				client.close();
			}
			this.clientMap.clear();
			this.clientMap = null;
		}
		this.closed = true;
	}

	public final void clear() {
		new PersistenceCacheManager(getPersistenceCache())
				.clearPersistenceCache();
	}

	public EntityMetadata getMetadata(Class<?> clazz) {
		return MetadataManager.getEntityMetadata(clazz);
	}

	// public String getId(Object entity, EntityMetadata metadata) {
	// try {
	// return PropertyAccessorHelper.getId(entity, metadata);
	// } catch (PropertyAccessException e) {
	// }
	// }

	/**
	 * not used
	 */
	public void store(Object id, Object entity) {
		this.session.store(id, entity);
	}

	/**
	 * not used
	 * 
	 * @param entities
	 * @param entityMetadata
	 */
	@SuppressWarnings("unchecked")
	public void store(List entities, EntityMetadata entityMetadata) {
		for (Iterator i$ = entities.iterator(); i$.hasNext();) {
			@SuppressWarnings("unused")
			Object o = i$.next();
			// this.session.store(getId(o, entityMetadata), o);
		}
	}

	/**
	 * get the Reader object to deal with the database transaction
	 * 
	 * @param client
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public EntityReader getReader(Client client) {
		return client.getReader();
	}

	public FlushModeType getFlushMode() {
		return this.flushMode;
	}

	public void setFlushMode(FlushModeType flushMode) {
		this.flushMode = flushMode;
	}

	public boolean isTransactionInProgress() {
		return this.isTransactionInProgress;
	}

	public PersistenceCache getPersistenceCache() {
		return this.persistenceCache;
	}

	/**
	 * start a transaction
	 */
	public void begin() {
		this.isTransactionInProgress = true;
	}

	/**
	 * synchronise persistence cache state with database, close transaction
	 */
	public void commit() {
		flush();
		this.isTransactionInProgress = false;
	}

	public void rollback() {
		this.isTransactionInProgress = false;
	}

	public boolean getRollbackOnly() {
		return false;
	}

	public void setRollbackOnly() {
	}

	public boolean isActive() {
		return this.isTransactionInProgress;
	}

}
