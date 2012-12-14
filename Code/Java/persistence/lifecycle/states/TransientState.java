package persistence.lifecycle.states;

import persistence.graph.Node;
import persistence.lifecycle.NodeStateContext;
import util.CloneUtil;

/**
 * not used
 * 
 */
public class TransientState extends NodeState {
	public void initialize(NodeStateContext nodeStateContext) {
	}

	public void handlePersist(NodeStateContext nodeStateContext) {
		moveNodeToNextState(nodeStateContext, new ManagedState());

		nodeStateContext.setDirty(true);

		nodeStateContext.getPersistenceCache().getMainCache().addNodeToCache(
				(Node) nodeStateContext);

		recursivelyPerformOperation(nodeStateContext,
				NodeState.OPERATION.PERSIST);
	}

	public void handleRemove(NodeStateContext nodeStateContext) {
		recursivelyPerformOperation(nodeStateContext,
				NodeState.OPERATION.REMOVE);
	}

	public void handleRefresh(NodeStateContext nodeStateContext) {
		recursivelyPerformOperation(nodeStateContext,
				NodeState.OPERATION.REFRESH);
	}

	public void handleMerge(NodeStateContext nodeStateContext) {
		Object copiedNodeData = CloneUtil.cloneDominoEntity(nodeStateContext
				.getData());
		nodeStateContext.setData(copiedNodeData);
	}

	public void handleFind(NodeStateContext nodeStateContext) {
	}

	public void handleClose(NodeStateContext nodeStateContext) {
	}

	public void handleClear(NodeStateContext nodeStateContext) {
	}

	public void handleFlush(NodeStateContext nodeStateContext) {
	}

	public void handleLock(NodeStateContext nodeStateContext) {
	}

	public void handleDetach(NodeStateContext nodeStateContext) {
	}

	public void handleCommit(NodeStateContext nodeStateContext) {
	}

	public void handleRollback(NodeStateContext nodeStateContext) {
	}

	public void handleGetReference(NodeStateContext nodeStateContext) {
	}

	public void handleContains(NodeStateContext nodeStateContext) {
	}
}
