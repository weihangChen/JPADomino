package persistence.lifecycle.states;

import persistence.lifecycle.NodeStateContext;

/**
 * one of the four State class which the operations from entityManager is
 * delegate to, follow the guideline from JPA, define the actions <br>
 * 
 * if a Node is in DetachedState, it can be only merged, the rest operations
 * either throws exception or will be ignored
 * 
 * @author weihang chen
 * 
 */
public class DetachedState extends NodeState {
	public void initialize(NodeStateContext nodeStateContext) {
	}

	public void handlePersist(NodeStateContext nodeStateContext) {
		throw new IllegalArgumentException(
				"Persist operation not allowed in Detached state");
	}

	public void handleRemove(NodeStateContext nodeStateContext) {
		throw new IllegalArgumentException(
				"Remove operation not allowed in Detached state. Possible reason: You may have closed entity manager before calling remove. A solution is to call merge before remove.");
	}

	public void handleRefresh(NodeStateContext nodeStateContext) {
		throw new IllegalArgumentException(
				"Refresh operation not allowed in Detached state");
	}

	public void handleMerge(NodeStateContext nodeStateContext) {
		moveNodeToNextState(nodeStateContext, new ManagedState());

		recursivelyPerformOperation(nodeStateContext, NodeState.OPERATION.MERGE);
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
