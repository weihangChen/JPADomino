package persistence.graph;

import persistence.lifecycle.states.NodeState;
import persistence.metadata.MetadataManager;
import persistence.metadata.model.EntityMetadata;
import persistence.context.PersistenceCache;
import util.DeepEquals;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.ibm.commons.util.StringUtil;

import persistence.metadata.model.Relation;
import util.CloneUtil;
import util.JSFUtil;
import util.ReflectionUtils;

import model.notes.ModelBase;

/**
 * by giving an entity, wrap this entity as Node object, build object graph
 * using this Node object as head Node. recursively populate the children
 * objects and the NodeLinks defining the relations between Nodes
 * 
 * @author weihang chen
 * 
 */
public class ObjectGraphBuilder {
	PersistenceCache persistenceCache;

	/**
	 * builds the graph by populating new nodes or fetching them from cache
	 * 
	 * @param entity
	 * @param initialNodeState
	 * @param persistenceCache
	 * @return
	 */

	public ObjectGraph getObjectGraph(Object entity,
			NodeState initialNodeState, PersistenceCache persistenceCache) {
		ObjectGraph objectGraph = new ObjectGraph();
		this.persistenceCache = persistenceCache;
		Collection<Node> allNodes = this.persistenceCache.getMainCache()
				.getAllNodes();
		System.out.println("all entities in cache: " + allNodes.size());

		System.out.println("----------------------SUB GRAPH BUILD FOR ENTITY "
				+ entity + " STARTS----------------------");
		Node headNode = getNode(entity, objectGraph, initialNodeState);

		if (headNode != null) {
			objectGraph.setHeadNode(headNode);
		}

		// PRINT TO CHECK STARTS: code below is just used to print the object
		// graph
		Map<String, Node> nodeMap = objectGraph.getNodeMapping();
		Set<Entry<String, Node>> nodeSet = nodeMap.entrySet();
		Iterator<Entry<String, Node>> iter = nodeSet.iterator();
		while (iter.hasNext()) {
			Entry<String, Node> nodeEntry = iter.next();
			Node n = nodeEntry.getValue();
			if (!StringUtil.equals(n.getData().toString(), headNode.getData()
					.toString()))
				System.out.print("subgraph node: " + n + "/");
		}
		// PUSH THIS HEAD NODE TO XPAGES REQUESTMAP, TO VIASUALIZE THE GRAPH
		JSFUtil.pushData(objectGraph, headNode.getNodeId());
		// PRINT TO CHECK END
		System.out.println("----------------------SUB GRAPH BUILD FINISHS "
				+ objectGraph + " IN SIZE OF " + nodeMap.size() + " with "
				+ objectGraph.getHeadNode() + " as head node");
		return objectGraph;
	}

	// 1. returns a cache node as headNode if it's found in cache
	// 2. assign cached data back to the pojo Fields
	@SuppressWarnings("unchecked")
	private Node getNode(Object entity, ObjectGraph graph,
			NodeState initialNodeState) {
		// EntityMetadata is stored for POJO classes, not for Enhanced
		// ones, therefore always need to perform check to get the real POJO
		// class
		Class realClass = JSFUtil.getRealClass(entity.getClass());
		EntityMetadata entityMetadata = persistence.metadata.MetadataManager
				.getEntityMetadata(realClass);
		if (entityMetadata == null) {
			return null;
		}

		String id = ((ModelBase) entity).getUnid();
		String nodeId = getNodeId(id);
		// graph is temporary graph with one head node, if there are duplicated
		// nodes, they still share the same relations, therefore no need to
		// build again for duplicated nodes

		Node node = null;
		// try to get the node from cache
		Node nodeInPersistenceCache = this.persistenceCache.getMainCache()
				.getNodeFromCache(nodeId);
		// delete1123
		// Object nodeDataCopy = CloneUtil.cloneDominoEntity(entity);
		// delete1123 end

		// try to get the node from graph
		Node nodeInGraph = graph.getNode(nodeId);

		// if it already exists in the graph return null
		// System.out.println("nodeInGraph: " + nodeInGraph
		// + " /nodeInPersistenceCache: " + nodeInPersistenceCache);
		if (nodeInGraph != null && nodeInPersistenceCache == null) {
			return null;
		}

		// if it does not exist in the one-time graph nor the cache, clone the
		// entity, wrap the clone in a new Node object, indicate the Node object
		// as dirty so it will be flush later.
		if (nodeInPersistenceCache == null) {
			Object nodeDataCopy = CloneUtil.cloneDominoEntity(entity);
			node = new Node(nodeId, nodeDataCopy, initialNodeState,
					this.persistenceCache);
			// 1108 added code, a new node should always be dirty
			node.setDirty(true);
			// 1108
		} else {
			// if it does not exist in one-time graph but exists in cache,it is
			// possible that related nodes (parents/children) might not exist in
			// persistence cache, therefore perform deepequal test to see if two
			// entities have same Field
			// values
			node = nodeInPersistenceCache;
			boolean isDeepEqual = DeepEquals.deepEquals(node.getData(), entity);

			// IMPORTANT: in the CloneUtil.cloneDominoEntity(entity), a new
			// entity is never cloned, since its wrapping a new document. A
			// clone wrapping another new notes document will never override the
			// a existing one from database. therefore if it is the same entity
			// in the persistence cache as being exposed to user. We know for
			// sure that this entity was wrapping a new document in the previous
			// request. Therefore need to make a clone for it in the current
			// request

			// if two entities with same id are not deepequal, then replace the
			// managed entity with a clone of the detached entity
			// if the entity in persistence cache is the same as the one being
			// exposed to user, clone the entity, put the clone into the node in
			// persistence cache
			if (node.getData() == entity || !isDeepEqual) {
				Object nodeDataCopy = CloneUtil.cloneDominoEntity(entity);
				// assign the new POJO entity to the Node object inside of the
				// persistence cache and mark it as dirty
				node.setData(nodeDataCopy);
				node.setDirty(true);
			} else {
				// if two objects are deepequal, then mark it as not dirty so it
				// not be picked in flush to save computation time
				node.setDirty(false);
			}
			// System.out.println("is the node dirty " + node.isDirty());
			// delete 1123 since deep equal is finished, no need to hard code
			// all nodes as dirty
			// node.setData(nodeDataCopy);
			// node.setDirty(true);
			// delete 1123 since deep equal is finished
		}
		// the new Node or the updated Node object is added to currently
		// building grapH (note: the new Node object is not added to persistence
		// cache)
		graph.addNode(nodeId, node);
		// 1111 old code does not require clearing the children/parent map,
		// since its method scope(remove an entity in request1, close the
		// entityManager, start a new one, all cache are gone, child/parent map
		// are new as well). The cache is always new one between methods,
		// in the demo application, the cache is by default being viewscope but
		// can be closed programmatically anytime. if the parent/children map is
		// not cleared first, the old reference will be seen when you navigate
		// from one Node to another Node through NodeLink, then you might try to
		// merge an entity in REMOVESTATE which will yield an exception(do not
		// do explict nulling, it never works on reference)
		if (node.getChildren() != null) {
			node.getChildren().clear();
		}
		// 111

		// System.out.println("relation amounts for entity " + entity + ": "
		// + entityMetadata.getRelations().size());

		// go through relations, get the Field objects and recursively populate
		// the graph
		for (Relation relation : entityMetadata.getRelations()) {
			// do not invoke any getter if the relation is lazy
			Field relationTargetField = relation.getProperty();
			// IMPORTANT************: experience technical difficulty here, do
			// not know how to check if lazy loaded object/collection has
			// already been loaded, correct mechanism is that if lazy collection
			// is not initialised, then it should not be part of the object
			// graph building
			/*
			 * if (FetchType.LAZY == relation.getFetchType()) continue;
			 */
			// IMPORTANT END********
			Object childObject = ReflectionUtils.getFieldObject(entity,
					relationTargetField);

			// what objectgraphbuilder does is to eliminate duplication and
			// build a unique graph

			System.out.println("PROCESS RELATION " + relation + " STARTS/"
					+ childObject);

			if (childObject != null) {
				// if Field value is Collection, get each object from the
				// collection, invoke addChildNodesToGraph
				if (Collection.class.isAssignableFrom(childObject.getClass())) {
					Collection childrenObjects = (Collection) childObject;
					Iterator i = childrenObjects.iterator();
					while (i.hasNext()) {
						Object childObj = i.next();
						if (childObj != null) {
							addChildNodesToGraph(graph, node, relation,
									childObj, initialNodeState);
						}
					}
				} else {
					// if its not Collection but single object,
					// addChildNodesToGraph
					addChildNodesToGraph(graph, node, relation, childObject,
							initialNodeState);
				}
			}

			System.out.println("PROCESS RELATION " + relation + " ENDS");
		}

		return node;
	}

	/**
	 * 1.recursively populate children by calling getNode()<br>
	 * 2.populate NodeLink between Nodes
	 * 
	 * @param graph
	 * @param parentNode
	 * @param relation
	 * @param childObject
	 * @param initialNodeState
	 */
	private void addChildNodesToGraph(ObjectGraph graph, Node parentNode,
			Relation relation, Object childObject, NodeState initialNodeState) {
		Node childNode = getNode(childObject, graph, initialNodeState);
		if (childNode == null) {
			return;
		}
		NodeLink nodeLink = new NodeLink(parentNode.getNodeId(), childNode
				.getNodeId());
		nodeLink.setMultiplicity(relation.getType());

		EntityMetadata metadata = MetadataManager.getEntityMetadata(parentNode
				.getDataClass());
		nodeLink.setLinkProperties(getLinkProperties(metadata, relation));
		childNode.addParentNode(nodeLink, parentNode);
		parentNode.addChildNode(nodeLink, childNode);

	}

	/**
	 * populate Link properties for a NodeLink object
	 * 
	 * @param metadata
	 * @param relation
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<NodeLink.LinkProperty, Object> getLinkProperties(
			EntityMetadata metadata, Relation relation) {
		Map linkProperties = new HashMap();

		linkProperties.put(NodeLink.LinkProperty.LINK_NAME, getMappedName(
				metadata, relation));
		linkProperties.put(NodeLink.LinkProperty.IS_SHARED_BY_PRIMARY_KEY,
				Boolean.valueOf(relation.isJoinedByPrimaryKey()));
		linkProperties.put(NodeLink.LinkProperty.IS_BIDIRECTIONAL, Boolean
				.valueOf(!(relation.isUnary())));
		linkProperties.put(NodeLink.LinkProperty.IS_RELATED_VIA_JOIN_TABLE,
				Boolean.valueOf(relation.isRelatedViaJoinTable()));
		linkProperties.put(NodeLink.LinkProperty.PROPERTY, relation
				.getProperty());

		linkProperties.put(NodeLink.LinkProperty.CASCADE, relation
				.getCascades());

		// if (relation.isRelatedViaJoinTable())
		// {
		// linkProperties.put(NodeLink.LinkProperty.JOIN_TABLE_METADATA,
		// relation.getJoinTableMetadata());
		// }

		return linkProperties;
	}

	/**
	 * not used
	 * 
	 * @param parentMetadata
	 * @param relation
	 * @return
	 */
	public String getMappedName(EntityMetadata parentMetadata, Relation relation) {
		if (relation != null) {
			String joinColumn = relation.getJoinColumnName();
			// if (joinColumn == null)
			// {
			// Class clazz = relation.getTargetEntity();
			// EntityMetadata metadata =
			// KunderaMetadataManager.getEntityMetadata(clazz);
			// joinColumn =
			// (relation.getType().equals(Relation.ForeignKey.ONE_TO_MANY)) ?
			// parentMetadata.getIdColumn().getName() :
			// metadata.getIdColumn().getName();
			// }

			return joinColumn;
		}
		return null;
	}

	/**
	 * 
	 * @param entityId
	 * @return $+document uniqueid
	 */
	public static String getNodeId(String entityId) {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("$");
		strBuffer.append(entityId);
		return strBuffer.toString();
	}

	/**
	 * not used
	 * 
	 * @param pk
	 * @param objectClass
	 * @return
	 */
	public static String getNodeId(Object pk, Class<?> objectClass) {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("$");
		strBuffer.append(pk);
		return strBuffer.toString();
	}

	/**
	 * 
	 * @param nodeId
	 * @return document uniqueid
	 */
	public static String getEntityId(String nodeId) {
		return nodeId.substring(nodeId.indexOf("$") + 1, nodeId.length());
	}
}
