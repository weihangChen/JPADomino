package persistence.lifecycle.states;

import persistence.client.Client;
import persistence.graph.Node;
import persistence.graph.ObjectGraphBuilder;
import persistence.lifecycle.NodeStateContext;

import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceException;

import lotus.domino.NotesException;

/**
 * one of the four State class which the operations from entityManager is
 * delegate to, follow the guideline from JPA, define the actions <br>
 * 
 * @author weihang chen
 * 
 */
public class RemovedState extends NodeState {
	public void initialize(NodeStateContext nodeStateContext) {
	}

	public void handlePersist(NodeStateContext nodeStateContext) {
		moveNodeToNextState(nodeStateContext, new ManagedState());

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
		throw new IllegalArgumentException(
				"Merge operation not allowed in Removed state");
	}

	public void handleFind(NodeStateContext nodeStateContext) {
	}

	public void handleClose(NodeStateContext nodeStateContext) {
	}

	public void handleClear(NodeStateContext nodeStateContext) {
	}

	@SuppressWarnings("unchecked")
	public void handleFlush(NodeStateContext nodeStateContext) {
		Client client = nodeStateContext.getClient();

		Node node = (Node) nodeStateContext;
		String entityId = ObjectGraphBuilder.getEntityId(node.getNodeId());

		try {
			client.delete(node.getData(), entityId);
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		nodeStateContext.setDirty(false);
		// 1111 different from old code
		// corresponding document is already removed from database,remove it
		// from the main cache - headNodes/allNodes. since the scope of
		// entityManger might be larger than method/request

		nodeStateContext.getPersistenceCache().getMainCache()
				.removeNodeFromCache(node);

		nodeStateContext.getPersistenceCache().getMainCache().getHeadNodes()
				.remove(node);
		// 1111
	}

	public void handleLock(NodeStateContext nodeStateContext) {
	}

	public void handleDetach(NodeStateContext nodeStateContext) {
		moveNodeToNextState(nodeStateContext, new DetachedState());
		recursivelyPerformOperation(nodeStateContext,
				NodeState.OPERATION.DETACH);
	}

	public void handleCommit(NodeStateContext nodeStateContext) {
		nodeStateContext.setCurrentNodeState(new TransientState());
	}

	public void handleRollback(NodeStateContext nodeStateContext) {
		if (PersistenceContextType.EXTENDED.equals(nodeStateContext
				.getPersistenceCache().getPersistenceContextType())) {
			moveNodeToNextState(nodeStateContext, new ManagedState());
		} else {
			if (!(PersistenceContextType.TRANSACTION.equals(nodeStateContext
					.getPersistenceCache().getPersistenceContextType()))) {
				return;
			}
			nodeStateContext.detach();
		}
	}

	public void handleGetReference(NodeStateContext nodeStateContext) {
	}

	public void handleContains(NodeStateContext nodeStateContext) {
	}

}
