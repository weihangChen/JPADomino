package persistence.context;

import persistence.graph.Node;
import persistence.graph.NodeLink;
import persistence.metadata.model.Relation;
import util.CommonUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * manager class building/clearing flushstack
 * 
 * @author weihang chen
 * 
 */
public class FlushManager {

	/**
	 * 1. mark all the nodes in the persistence cache as not traversed - new
	 * PersistenceCacheManager(pc).markAllNodesNotTraversed();
	 * <p>
	 * 2. go through all the head nodes and invoke addNodesToFlushStack(pc,
	 * headNode) on it if its dirty and being not null
	 * 
	 * @param pc
	 */
	@SuppressWarnings("unchecked")
	public void buildFlushStack(PersistenceCache pc) {
		MainCache mainCache = (MainCache) pc.getMainCache();
		System.out.println(CommonUtil.getMethodName(this.getClass().toString())
				+ "/MAIN CACHE: " + mainCache + "/size: " + mainCache.size());
		new PersistenceCacheManager(pc).markAllNodesNotTraversed();
		Set headNodes = mainCache.getHeadNodes();
		for (Object obj : headNodes) {
			// System.out.println("current headnode is: "+obj);
			Node headNode = (Node) obj;
			if (headNode != null && headNode.isDirty())
				addNodesToFlushStack(pc, headNode);
		}
	}

	/**
	 * get FlushStack object from perisistenceCache, recursively add relative
	 * nodes to FlushStack object <br>
	 * IMPORTANT: NOT code is in use, only OneToMany
	 * 
	 * @param pc
	 * @param node
	 */
	@SuppressWarnings("unchecked")
	public void addNodesToFlushStack(PersistenceCache pc, Node node) {

		FlushStack flushStack = pc.getFlushStack();
		MainCache mainCache = (MainCache) pc.getMainCache();
		if (node == null)
			return;
		Map children = node.getChildren();

		if (children != null) {
			Map oneToOneChildren = new HashMap();
			Map oneToManyChildren = new HashMap();
			Map manyToOneChildren = new HashMap();
			Map manyToManyChildren = new HashMap();

			for (Object obj : children.keySet()) {
				NodeLink nodeLink = (NodeLink) obj;
				Relation.ForeignKey multiplicity = nodeLink.getMultiplicity();
				switch (multiplicity.ordinal()) {
				case 0:
					oneToOneChildren.put(nodeLink, children.get(nodeLink));
					break;
				case 1:
					oneToManyChildren.put(nodeLink, children.get(nodeLink));
					break;
				case 2:
					manyToOneChildren.put(nodeLink, children.get(nodeLink));
					break;
				case 3:
					manyToManyChildren.put(nodeLink, children.get(nodeLink));
				}

			}

			for (Object obj : oneToManyChildren.keySet()) {
				NodeLink nodeLink = (NodeLink) obj;
				Node childNode = mainCache.getNodeFromCache(nodeLink
						.getTargetNodeId());
				if (!(childNode.isTraversed())) {
					addNodesToFlushStack(pc, childNode);
				}
			}

			for (Object obj : manyToManyChildren.keySet()) {
				NodeLink nodeLink = (NodeLink) obj;
				Node childNode = mainCache.getNodeFromCache(nodeLink
						.getTargetNodeId());

				if (childNode != null) {
					if ((node.isDirty()) && (!(node.isTraversed()))) {
						// JoinTableMetadata jtmd = (JoinTableMetadata) nodeLink
						// .getLinkProperty(NodeLink.LinkProperty.JOIN_TABLE_METADATA);
						//
						// if (jtmd != null) {
						// String joinColumnName = (String) jtmd
						// .getJoinColumns().toArray()[0];
						// String inverseJoinColumnName = (String) jtmd
						// .getInverseJoinColumns().toArray()[0];
						// Object entityId = ObjectGraphBuilder
						// .getEntityId(node.getNodeId());
						// Object childId = ObjectGraphBuilder
						// .getEntityId(childNode.getNodeId());
						//
						// Set childValues = new HashSet();
						// childValues.add(childId);
						//
						// JoinTableData.OPERATION operation = null;
						// if (node.getCurrentNodeState().getClass().equals(
						// ManagedState.class)) {
						// operation = JoinTableData.OPERATION.INSERT;
						// } else if (node.getCurrentNodeState().getClass()
						// .equals(RemovedState.class)) {
						// operation = JoinTableData.OPERATION.DELETE;
						// }
						//
						// pc.addJoinTableDataIntoMap(operation, jtmd
						// .getJoinTableName(), joinColumnName,
						// inverseJoinColumnName, node.getDataClass(),
						// entityId, childValues);
						// }

					}

					if (!(childNode.isTraversed())) {
						addNodesToFlushStack(pc, childNode);
					}

				}

			}

			for (Object obj : oneToOneChildren.keySet()) {
				NodeLink nodeLink = (NodeLink) obj;
				if (!(node.isTraversed())) {
					node.setTraversed(true);
					flushStack.push(node);

					Node childNode = mainCache.getNodeFromCache(nodeLink
							.getTargetNodeId());
					addNodesToFlushStack(pc, childNode);
				}

			}

			for (Object obj : manyToOneChildren.keySet()) {
				NodeLink nodeLink = (NodeLink) obj;
				if (!(node.isTraversed())) {
					node.setTraversed(true);
					flushStack.push(node);
				}
				Node childNode = mainCache.getNodeFromCache(nodeLink
						.getTargetNodeId());

				Map parents = childNode.getParents();
				for (Object obj1 : parents.keySet()) {
					NodeLink parentLink = (NodeLink) obj1;
					Relation.ForeignKey multiplicity = parentLink
							.getMultiplicity();
					if (multiplicity.equals(Relation.ForeignKey.MANY_TO_ONE)) {
						Node parentNode = (Node) parents.get(parentLink);

						if ((!(parentNode.isTraversed()))
								&& (parentNode.isDirty())) {
							addNodesToFlushStack(pc, parentNode);
						}
					}

				}

				if ((!(childNode.isTraversed())) && (childNode.isDirty())) {
					addNodesToFlushStack(pc, childNode);
				} else if (!(childNode.isDirty())) {
					childNode.setTraversed(true);
					flushStack.push(childNode);
				}

			}
		}
		// System.out.println("!!!!!!!!!!!!!!!!!!!!!! node: "+node.getData().toString());
		// System.out.println("!!!!!!!!!!!!!!!!!!!!!! istraversed()" +
		// node.isTraversed());
		// System.out.println("!!!!!!!!!!!!!!!!!!!!!! isdirty()" +
		// node.isDirty());
		if ((node.isTraversed()) || (!(node.isDirty())))
			return;
		node.setTraversed(true);
		flushStack.push(node);
	}

	/**
	 * clear the FlushStack object from a PersistenceCache object
	 * 
	 * @param pc
	 */
	public void clearFlushStack(PersistenceCache pc) {
		FlushStack flushStack = pc.getFlushStack();
		if ((flushStack == null) || (flushStack.isEmpty()))
			return;
		flushStack.clear();
	}
}
