package persistence.lifecycle;

import persistence.client.Client;
import persistence.graph.Node;
import persistence.graph.NodeLink;
import persistence.lifecycle.states.NodeState;
import persistence.core.PersistenceDelegator;
import persistence.context.PersistenceCache;
import java.util.Map;

/**
 * INTERFACE FOR persistence.graph.Node
 * 
 * @author weihang chen
 * 
 */
public abstract interface NodeStateContext {
	public abstract NodeState getCurrentNodeState();

	public abstract void setCurrentNodeState(NodeState paramNodeState);

	public abstract String getNodeId();

	public abstract void setNodeId(String paramString);

	public abstract Object getData();

	public abstract void setData(Object paramObject);

	@SuppressWarnings("unchecked")
	public abstract Class getDataClass();

	@SuppressWarnings("unchecked")
	public abstract void setDataClass(Class paramClass);

	public abstract Map<NodeLink, Node> getParents();

	public abstract void setParents(Map<NodeLink, Node> paramMap);

	public abstract Map<NodeLink, Node> getChildren();

	public abstract void setChildren(Map<NodeLink, Node> paramMap);

	public abstract Node getParentNode(String paramString);

	public abstract Node getChildNode(String paramString);

	public abstract void addParentNode(NodeLink paramNodeLink, Node paramNode);

	public abstract void addChildNode(NodeLink paramNodeLink, Node paramNode);

	public abstract boolean isTraversed();

	public abstract void setTraversed(boolean paramBoolean);

	public abstract boolean isDirty();

	public abstract void setDirty(boolean paramBoolean);

	public abstract boolean isHeadNode();

	public abstract void setHeadNode(boolean paramBoolean);

	@SuppressWarnings("unchecked")
	public abstract Client getClient();

	@SuppressWarnings("unchecked")
	public abstract void setClient(Client paramClient);

	public abstract PersistenceDelegator getPersistenceDelegator();

	public abstract void setPersistenceDelegator(
			PersistenceDelegator paramPersistenceDelegator);

	public abstract void persist();

	public abstract void remove();

	public abstract void refresh();

	public abstract void merge();

	public abstract void detach();

	public abstract void close();

	public abstract void lock();

	public abstract void commit();

	public abstract void rollback();

	public abstract void find();

	public abstract void getReference();

	public abstract void contains();

	public abstract void clear();

	public abstract void flush();

	public abstract boolean isInState(Class<?> paramClass);

	public abstract PersistenceCache getPersistenceCache();

	public abstract void setPersistenceCache(
			PersistenceCache paramPersistenceCache);
}
