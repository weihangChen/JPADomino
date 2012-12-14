package persistence.context;

import persistence.graph.Node;
import persistence.graph.ObjectGraph;
import util.CloneUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * parent cache class<br>
 * manage nodes in first level cache
 * 
 * @author weihang chen
 * 
 */
public class CacheBase {
	private static Log log = LogFactory.getLog(CacheBase.class);
	/**
	 * reference to all nodes
	 */
	private Map<String, Node> nodeMappings;
	/**
	 * reference to all head nodes
	 */
	private Set<Node> headNodes;

	@SuppressWarnings("unchecked")
	public CacheBase() {
		this.headNodes = new HashSet();
		this.nodeMappings = new HashMap();
	}

	/**
	 * get a node from all nodes map by id, id is document unique "$+id"
	 * 
	 * @param nodeId
	 * @return
	 */
	public Node getNodeFromCache(String nodeId) {
		Node node = (Node) this.nodeMappings.get(nodeId);
		return node;
	}

	/**
	 * clone and node and put in cache only if its dirty (new node is dirty,
	 * node with changed Field value is dirty)
	 * <p>
	 * 2. if input node does existing in cache, put parent/children nodes from
	 * the existing node from persistence cache to the new clone
	 * 
	 * @param node
	 */
	@SuppressWarnings("unchecked")
	public void addNodeToCache(Node node) {
		// 1126 old version does not have dirty check
		if (!node.isDirty())
			return;
		// 1126
		Object nodeDataCopy = CloneUtil.cloneDominoEntity(node.getData());
		node.setData(nodeDataCopy);

		// node already exists from persistence cache, put parent, children
		// nodes into the
		if (this.nodeMappings.containsKey(node.getNodeId())) {
			Node existingNode = (Node) this.nodeMappings.get(node.getNodeId());

			if (existingNode.getParents() != null) {
				if (node.getParents() == null) {
					node.setParents(new HashMap());
				}
				node.getParents().putAll(existingNode.getParents());
			}

			if (existingNode.getChildren() != null) {
				if (node.getChildren() == null) {
					node.setChildren(new HashMap());
				}
				node.getChildren().putAll(existingNode.getChildren());
			}

			this.nodeMappings.put(node.getNodeId(), node);
			logCacheEvent("ADDED TO ", node.getNodeId());
		} else {
			// node does not exist from persistence cache
			// add it to cache
			logCacheEvent("ADDED TO ", node.getNodeId());
			this.nodeMappings.put(node.getNodeId(), node);
		}

		if (!(node.isHeadNode()))
			return;

		node.getPersistenceCache().getMainCache().addHeadNode(node);

	}

	/**
	 * when a node is removed, remove it from headnode map and allnode map
	 * 
	 * @param node
	 */
	public void removeNodeFromCache(Node node) {
		if (getHeadNodes().contains(node)) {
			getHeadNodes().remove(node);
		}

		if (this.nodeMappings.get(node.getNodeId()) != null) {
			this.nodeMappings.remove(node.getNodeId());
		}

		logCacheEvent("REMOVED FROM ", node.getNodeId());
		node = null;
	}

	/**
	 * merge object graph with the existing persistence
	 * cache<br>
	 * 1.go through all nodes from the graph, invoke addNodeToCache()<br>
	 * 2. since flush manager only checks the head nodes for flush, if current
	 * visiting graph node is not head node and it exists in cache as head node,
	 * remove it from cache headnode map 3.one graph has only one headNode, add
	 * it to the cache, so it will be flushed
	 * 
	 * @param graph
	 * @param persistenceCache
	 */
	public void addGraphToCache(ObjectGraph graph,
			PersistenceCache persistenceCache) {
		for (String key : graph.getNodeMapping().keySet()) {
			Node thisNode = (Node) graph.getNodeMapping().get(key);
			addNodeToCache(thisNode);
			if ((!(thisNode.isHeadNode()))
					&& (persistenceCache.getMainCache().getHeadNodes()
							.contains(thisNode))) {
				persistenceCache.getMainCache().getHeadNodes().remove(thisNode);
			}
		}
		addHeadNode(graph.getHeadNode());
		System.out.println("CURRENT CACHE INSTANCE "
				+ persistenceCache.getMainCache() + " /CURRENT CACHE HEADNODE "
				+ graph.getHeadNode() + " /CURRENT CACHE SIZE "
				+ persistenceCache.getMainCache().size());
	}

	private void logCacheEvent(String eventType, String nodeId) {
		log.debug("Node: " + nodeId + ":: " + eventType
				+ " Persistence Context");
	}

	public void setNodeMappings(Map<String, Node> nodeMappings) {
		this.nodeMappings = nodeMappings;
	}

	public Set<Node> getHeadNodes() {
		return this.headNodes;
	}

	public void addHeadNode(Node headNode) {
		this.headNodes.add(headNode);
	}

	public int size() {
		return this.nodeMappings.size();
	}

	public Collection<Node> getAllNodes() {
		return this.nodeMappings.values();
	}

	public void clear() {
		this.nodeMappings.clear();
		this.headNodes.clear();
	}
}
