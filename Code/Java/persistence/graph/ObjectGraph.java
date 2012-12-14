package persistence.graph;

import java.util.HashMap;
import java.util.Map;

/**
 * headNode used to define the single head Node object in the graph, nodeMapping
 * stores all the Nodes' reference, by visiting any Node object, NodeLink is
 * used navigating to related Nodes through relations
 * 
 * @author weihang chen
 * 
 */
public class ObjectGraph {
	private Node headNode;
	private Map<String, Node> nodeMapping;

	@SuppressWarnings("unchecked")
	public ObjectGraph() {
		this.nodeMapping = new HashMap();
	}

	public void addNode(String nodeId, Node node) {
		this.nodeMapping.put(nodeId, node);
	}

	public Node getNode(String nodeId) {
		return ((Node) this.nodeMapping.get(nodeId));
	}

	public Node getHeadNode() {
		return this.headNode;
	}

	public void setHeadNode(Node headNode) {
		this.headNode = headNode;
	}

	public Map<String, Node> getNodeMapping() {
		return this.nodeMapping;
	}
}
