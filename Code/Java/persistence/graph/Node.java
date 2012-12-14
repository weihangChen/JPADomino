package persistence.graph;

import persistence.client.Client;
import persistence.lifecycle.NodeStateContext;
import persistence.lifecycle.states.DetachedState;
import persistence.lifecycle.states.ManagedState;
import persistence.lifecycle.states.NodeState;
import persistence.lifecycle.states.RemovedState;
import persistence.lifecycle.states.TransientState;
import persistence.core.PersistenceDelegator;
import persistence.context.PersistenceCache;
import java.util.HashMap;
import java.util.Map;

/**
 * Node instance is representative entity within persistence context NOT DONE
 * 
 * @author weihang chen
 * 
 */
public class Node implements NodeStateContext {
	/**
	 * document unid
	 */
	private String nodeId;
	/**
	 * POJO class instance
	 */
	private Object data;
	/**
	 * NodeState - REMOVE/MANAGED/DETACHED
	 */
	private NodeState currentNodeState;
	/**
	 * POJO class
	 */
	private Class<?> dataClass;
	/**
	 * map holds the the nodes that are relation owner to current node
	 */
	private Map<NodeLink, Node> parents;
	/**
	 * map holds the nodes which current node is relation owner to
	 */
	private Map<NodeLink, Node> children;
	/**
	 * a Node instance that is already traversed will not be visited again
	 */
	private boolean traversed;
	/**
	 * if a node instance is not dirty, it will not be visited
	 */
	private boolean dirty;
	/**
	 * if a Node instance is head node, it will be put into the headnode map in
	 * cache, then it will be flushed if its dirty and not traversed
	 */
	private boolean isHeadNode;
	/**
	 * dbclient being associated with current Node object, instance is injected
	 */
	@SuppressWarnings("unchecked")
	Client client;
	/**
	 * first level cache instance being injected through constructor
	 */
	private PersistenceCache persistenceCache;
	/**
	 * main implementation class for almost all operations within persistence
	 * context issued by entityManager, instance is injected
	 */
	PersistenceDelegator pd;

	/**
	 * if there is no initial nodestate, make it Transient
	 * 
	 * @param nodeId
	 * @param data
	 * @param pc
	 */
	public Node(String nodeId, Object data, PersistenceCache pc) {
		initializeNode(nodeId, data);
		setPersistenceCache(pc);

		this.currentNodeState = new TransientState();
	}

	/**
	 * constructor to initialise a Node, with initial state
	 * 
	 * @param nodeId
	 * @param data
	 * @param initialNodeState
	 * @param pc
	 */
	public Node(String nodeId, Object data, NodeState initialNodeState,
			PersistenceCache pc) {
		initializeNode(nodeId, data);
		setPersistenceCache(pc);

		if (initialNodeState == null) {
			this.currentNodeState = new TransientState();
		} else {

			if (initialNodeState.getClass() == ManagedState.class)
				this.currentNodeState = new ManagedState();
			else if (initialNodeState.getClass() == RemovedState.class)
				this.currentNodeState = new RemovedState();
			else if (initialNodeState.getClass() == DetachedState.class)
				this.currentNodeState = new DetachedState();
			else if (initialNodeState.getClass() == TransientState.class)
				this.currentNodeState = new TransientState();

			// without initialise a new state instance, will lead to that all
			// nodes
			// sharing same state instance
			// this.currentNodeState = initialNodeState;
		}
	}

	public Node(String nodeId, Class<?> nodeDataClass,
			NodeState initialNodeState, PersistenceCache pc) {
		this.nodeId = nodeId;
		this.dataClass = nodeDataClass;
		setPersistenceCache(pc);

		if (initialNodeState == null) {
			this.currentNodeState = new TransientState();
		} else {
			this.currentNodeState = initialNodeState;
		}
	}

	private void initializeNode(String nodeId, Object data) {
		this.nodeId = nodeId;
		this.data = data;
		this.dataClass = data.getClass();
	}

	public String getNodeId() {
		return this.nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public Object getData() {
		return this.data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@SuppressWarnings("unchecked")
	public Class getDataClass() {
		return this.dataClass;
	}

	@SuppressWarnings("unchecked")
	public void setDataClass(Class dataClass) {
		this.dataClass = dataClass;
	}

	public NodeState getCurrentNodeState() {
		return this.currentNodeState;
	}

	public void setCurrentNodeState(NodeState currentNodeState) {
		this.currentNodeState = currentNodeState;
	}

	public Map<NodeLink, Node> getParents() {
		return this.parents;
	}

	public void setParents(Map<NodeLink, Node> parents) {
		this.parents = parents;
	}

	public Map<NodeLink, Node> getChildren() {
		return this.children;
	}

	public void setChildren(Map<NodeLink, Node> children) {
		this.children = children;
	}

	public boolean isHeadNode() {
		return this.isHeadNode;
	}

	public void setHeadNode(boolean isHeadNode) {
		this.isHeadNode = isHeadNode;
	}

	public Node getParentNode(String parentNodeId) {
		NodeLink link = new NodeLink(parentNodeId, getNodeId());

		if (this.parents == null) {
			return null;
		}

		return ((Node) this.parents.get(link));
	}

	public Node getChildNode(String childNodeId) {
		NodeLink link = new NodeLink(getNodeId(), childNodeId);

		if (this.children == null) {
			return null;
		}

		return ((Node) this.children.get(link));
	}

	@SuppressWarnings("unchecked")
	public void addParentNode(NodeLink nodeLink, Node node) {
		if ((this.parents == null) || (this.parents.isEmpty())) {
			this.parents = new HashMap();
		}
		this.parents.put(nodeLink, node);
	}

	@SuppressWarnings("unchecked")
	public void addChildNode(NodeLink nodeLink, Node node) {
		if ((this.children == null) || (this.children.isEmpty())) {
			this.children = new HashMap();
		}
		this.children.put(nodeLink, node);
	}

	public boolean isTraversed() {
		return this.traversed;
	}

	public void setTraversed(boolean traversed) {
		this.traversed = traversed;
	}

	public boolean isDirty() {
		return this.dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	@SuppressWarnings("unchecked")
	public Client getClient() {
		return this.client;
	}

	@SuppressWarnings("unchecked")
	public void setClient(Client client) {
		this.client = client;
	}

	public PersistenceDelegator getPersistenceDelegator() {
		return this.pd;
	}

	public void setPersistenceDelegator(PersistenceDelegator pd) {
		this.pd = pd;
	}

	public String toString() {
		return "[" + this.nodeId + ": " + getData() + ": "
				+ getCurrentNodeState() + ": isDirty - " + isDirty() + "]";
	}

	public boolean equals(Object otherNode) {
		return super.equals(otherNode);
	}

	/**
	 * all operations issued by entityManager are eventually delegated to
	 * different NodeState (REMOVE, MANAGED, TRANSIENT)
	 */
	public void persist() {
		getCurrentNodeState().handlePersist(this);
	}

	public void remove() {
		getCurrentNodeState().handleRemove(this);
	}

	public void refresh() {
		getCurrentNodeState().handleRefresh(this);
	}

	public void merge() {
		getCurrentNodeState().handleMerge(this);
	}

	public void detach() {
		getCurrentNodeState().handleDetach(this);
	}

	public void close() {
		getCurrentNodeState().handleClose(this);
	}

	public void lock() {
		getCurrentNodeState().handleLock(this);
	}

	public void commit() {
		getCurrentNodeState().handleCommit(this);
	}

	public void rollback() {
		getCurrentNodeState().handleRollback(this);
	}

	public void find() {
		NodeState state = getCurrentNodeState();
		state.handleFind(this);
	}

	public void getReference() {
		getCurrentNodeState().handleGetReference(this);
	}

	public void contains() {
		getCurrentNodeState().handleContains(this);
	}

	public void clear() {
		getCurrentNodeState().handleClear(this);
	}

	public void flush() {
		if (!(isDirty()))
			return;
		getCurrentNodeState().handleFlush(this);
	}

	public boolean isInState(Class<?> stateClass) {
		return getCurrentNodeState().getClass().equals(stateClass);
	}

	public PersistenceCache getPersistenceCache() {
		return this.persistenceCache;
	}

	public void setPersistenceCache(PersistenceCache persistenceCache) {
		this.persistenceCache = persistenceCache;
	}

}
