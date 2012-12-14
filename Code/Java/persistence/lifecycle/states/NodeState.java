package persistence.lifecycle.states;

import persistence.graph.Node;
import persistence.graph.NodeLink;
import persistence.lifecycle.NodeStateContext;

import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class NodeState {
	private static Log log = LogFactory.getLog(NodeState.class);

	public abstract void initialize(NodeStateContext paramNodeStateContext);

	public abstract void handlePersist(NodeStateContext paramNodeStateContext);

	public abstract void handleRemove(NodeStateContext paramNodeStateContext);

	public abstract void handleRefresh(NodeStateContext paramNodeStateContext);

	public abstract void handleMerge(NodeStateContext paramNodeStateContext);

	public abstract void handleDetach(NodeStateContext paramNodeStateContext);

	public abstract void handleClose(NodeStateContext paramNodeStateContext);

	public abstract void handleLock(NodeStateContext paramNodeStateContext);

	public abstract void handleCommit(NodeStateContext paramNodeStateContext);

	public abstract void handleRollback(NodeStateContext paramNodeStateContext);

	public abstract void handleFind(NodeStateContext paramNodeStateContext);

	public abstract void handleGetReference(
			NodeStateContext paramNodeStateContext);

	public abstract void handleContains(NodeStateContext paramNodeStateContext);

	public abstract void handleClear(NodeStateContext paramNodeStateContext);

	public abstract void handleFlush(NodeStateContext paramNodeStateContext);

	protected void moveNodeToNextState(NodeStateContext nodeStateContext,
			NodeState nextState) {
		nodeStateContext.setCurrentNodeState(nextState);
	}

	/**
	 * by giving a Node, recursively propagate the OPERATION to its children
	 * 
	 * @param nodeStateContext
	 * @param operation
	 */
	@SuppressWarnings("unchecked")
	protected void recursivelyPerformOperation(
			NodeStateContext nodeStateContext, OPERATION operation) {
		// System.out.println("NodeState as super class for all states "+CommonUtil.getMethodName(this.getClass().toString()));
		Map children = nodeStateContext.getChildren();
		if (children == null)
			return;
		for (Object obj : children.keySet()) {
			NodeLink nodeLink = (NodeLink) obj;
			List cascadeTypes = (List) nodeLink
					.getLinkProperty(NodeLink.LinkProperty.CASCADE);

			switch (operation.ordinal()) {
			case 0:

				if ((cascadeTypes.contains(CascadeType.PERSIST))
						|| (cascadeTypes.contains(CascadeType.ALL))) {
					Node childNode = (Node) children.get(nodeLink);
					childNode.persist();
				}
				break;
			case 1:
				if ((cascadeTypes.contains(CascadeType.MERGE))
						|| (cascadeTypes.contains(CascadeType.ALL))) {
					Node childNode = (Node) children.get(nodeLink);
					System.out.println("!!!!!!!!!!!!!!!!! " + childNode);
					childNode.merge();
				}
				break;
			case 2:
				if ((cascadeTypes.contains(CascadeType.REMOVE))
						|| (cascadeTypes.contains(CascadeType.ALL))) {
					Node childNode = (Node) children.get(nodeLink);
					childNode.remove();
				}
				break;
			case 3:

				if ((cascadeTypes.contains(CascadeType.REFRESH))
						|| (cascadeTypes.contains(CascadeType.ALL))) {
					Node childNode = (Node) children.get(nodeLink);
					childNode.refresh();
				}
				break;
			case 4:

				if ((cascadeTypes.contains(CascadeType.DETACH))
						|| (cascadeTypes.contains(CascadeType.ALL))) {
					Node childNode = (Node) children.get(nodeLink);
					childNode.detach();
				}

			}

		}
	}

	public void logStateChangeEvent(NodeState prevState, NodeState nextState,
			String nodeId) {
		log.debug("Node: " + nodeId + ":: "
				+ prevState.getClass().getSimpleName() + " >>> "
				+ nextState.getClass().getSimpleName());
	}

	public void logNodeEvent(String eventType, NodeState currentState,
			String nodeId) {
		log.debug("Node: " + nodeId + ":: " + eventType + " in state "
				+ currentState.getClass().getSimpleName());
	}

	public static enum OPERATION {
		PERSIST, MERGE, REMOVE, REFRESH, DETACH;
	}

}
