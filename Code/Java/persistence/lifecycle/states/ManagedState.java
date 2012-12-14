package persistence.lifecycle.states;

import persistence.client.Client;
import persistence.client.EnhanceEntity;
import persistence.graph.Node;
import persistence.graph.ObjectGraphBuilder;
import persistence.lifecycle.NodeStateContext;
import persistence.metadata.MetadataManager;
import persistence.metadata.model.EntityMetadata;
import persistence.core.EntityReader;
import javax.persistence.PersistenceContextType;

import model.notes.Key;

/**
 * one of the four State class which the operations from entityManager is
 * delegate to, follow the guideline from JPA, define the actions <br>
 * 
 * @author weihang chen
 * 
 */
public class ManagedState extends NodeState {
	public void initialize(NodeStateContext nodeStateContext) {
	}

	/**
	 * do nothing actually, just invoke the recursive method from NodeState.java
	 */
	public void handlePersist(NodeStateContext nodeStateContext) {
		recursivelyPerformOperation(nodeStateContext,
				NodeState.OPERATION.PERSIST);
	}

	/**
	 * change current Node to REMOVE state, propagate the same mechanism to
	 * children Nodes by invoking recursivelyPerformOperation from
	 * NodeState.java
	 */
	public void handleRemove(NodeStateContext nodeStateContext) {
		moveNodeToNextState(nodeStateContext, new RemovedState());
		nodeStateContext.setDirty(true);
		recursivelyPerformOperation(nodeStateContext,
				NodeState.OPERATION.REMOVE);
	}

	/**
	 * do nothing actually, invoke the recursive method from NodeState.java
	 */
	public void handleRefresh(NodeStateContext nodeStateContext) {
		recursivelyPerformOperation(nodeStateContext,
				NodeState.OPERATION.REFRESH);
	}

	/**
	 * merge current Node to persistence cache , propagate the same mechanism to
	 * children Nodes by invoking recursivelyPerformOperation from
	 * NodeState.java
	 */
	public void handleMerge(NodeStateContext nodeStateContext) {
		nodeStateContext.getPersistenceCache().getMainCache().addNodeToCache(
				(Node) nodeStateContext);
		recursivelyPerformOperation(nodeStateContext, NodeState.OPERATION.MERGE);
	}

	/**
	 * find from database
	 */
	@SuppressWarnings("unchecked")
	public void handleFind(NodeStateContext nodeStateContext) {
		Client client = nodeStateContext.getClient();
		Class nodeDataClass = nodeStateContext.getDataClass();
		EntityMetadata entityMetadata = MetadataManager
				.getEntityMetadata(nodeDataClass);

		String entityId = ObjectGraphBuilder.getEntityId(nodeStateContext
				.getNodeId());
		Object nodeData = null;
		EntityReader reader = client.getReader();
		Key key = new Key();
		key.appendEntry(entityId);
		EnhanceEntity enhanceEntity = reader.findById(key, entityMetadata,
				entityMetadata.getRelationNames(), client);
		System.out.println("RETURNED ENHANCEENTITY: " + enhanceEntity
				+ " /id: " + enhanceEntity.getEntityId());
		if ((enhanceEntity != null) && (enhanceEntity.getEntity() != null)) {
			Object entity = enhanceEntity.getEntity();
			if ((((entityMetadata.getRelationNames() == null) || (entityMetadata
					.getRelationNames().isEmpty())))) {
				nodeData = entity;
			} else {
				nodeData = reader.recursivelyFindEntities(enhanceEntity,
						client, entityMetadata, nodeStateContext
								.getPersistenceDelegator());
			}
		}

		nodeStateContext.setData(nodeData);
		nodeStateContext.setDirty(false);
	}

	public void handleClose(NodeStateContext nodeStateContext) {
		handleDetach(nodeStateContext);
	}

	public void handleClear(NodeStateContext nodeStateContext) {
		handleDetach(nodeStateContext);
	}

	@SuppressWarnings("unchecked")
	public void handleFlush(NodeStateContext nodeStateContext) {
		Client client = nodeStateContext.getClient();
		client.persist((Node) nodeStateContext);

		nodeStateContext.setDirty(false);
	}

	/**
	 * not implemented
	 */
	public void handleLock(NodeStateContext nodeStateContext) {
	}

	/**
	 * change Node state , propagate the same mechanism to children Nodes by
	 * invoking recursivelyPerformOperation from NodeState.java
	 */
	public void handleDetach(NodeStateContext nodeStateContext) {
		moveNodeToNextState(nodeStateContext, new DetachedState());

		recursivelyPerformOperation(nodeStateContext,
				NodeState.OPERATION.DETACH);
	}

	public void handleCommit(NodeStateContext nodeStateContext) {
		nodeStateContext.setCurrentNodeState(new DetachedState());
	}

	/**
	 * not used
	 */
	public void handleRollback(NodeStateContext nodeStateContext) {
		if (PersistenceContextType.EXTENDED.equals(nodeStateContext
				.getPersistenceCache().getPersistenceContextType())) {
			moveNodeToNextState(nodeStateContext, new TransientState());
		} else {
			if (!(PersistenceContextType.TRANSACTION.equals(nodeStateContext
					.getPersistenceCache().getPersistenceContextType()))) {
				return;
			}
			moveNodeToNextState(nodeStateContext, new DetachedState());
		}
	}

	public void handleGetReference(NodeStateContext nodeStateContext) {
	}

	public void handleContains(NodeStateContext nodeStateContext) {
	}
}
