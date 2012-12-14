package persistence.graph;

import persistence.metadata.model.Relation;
import java.util.HashMap;
import java.util.Map;

import com.ibm.commons.util.StringUtil;

/**
 * NodeLink is used to define relation between Node objects, parents/children
 * NodeLink map from Node.java is used navigating to related nodes
 * 
 * @author weihang chen
 * 
 */
public class NodeLink {
	/**
	 * giving the relation between NodeA and NodeB, sounceNodeId is the id for
	 * relation owner NodeA's id
	 */
	private String sourceNodeId;
	/**
	 * giving the relation between NodeA and NodeB, sounceNodeId is the id for
	 * relation target NodeB's id
	 */
	private String targetNodeId;
	/**
	 * used to define foreign key
	 */
	private Relation.ForeignKey multiplicity;
	/**
	 * used to define the uniqueness of specific relations, refer to <br>
	 * getLinkProperties( EntityMetadata meta data, Relation relation) from
	 * ObjectGraphBuilder.java for more information
	 */
	private Map<LinkProperty, Object> linkProperties;

	public NodeLink() {
	}

	public NodeLink(String sourceNodeId, String targetNodeId) {
		this.sourceNodeId = sourceNodeId;
		this.targetNodeId = targetNodeId;
	}

	public String getSourceNodeId() {
		return this.sourceNodeId;
	}

	public void setSourceNodeId(String sourceNodeId) {
		this.sourceNodeId = sourceNodeId;
	}

	public String getTargetNodeId() {
		return this.targetNodeId;
	}

	public void setTargetNodeId(String targetNodeId) {
		this.targetNodeId = targetNodeId;
	}

	public Relation.ForeignKey getMultiplicity() {
		return this.multiplicity;
	}

	public void setMultiplicity(Relation.ForeignKey multiplicity) {
		this.multiplicity = multiplicity;
	}

	public Map<LinkProperty, Object> getLinkProperties() {
		return this.linkProperties;
	}

	public void setLinkProperties(Map<LinkProperty, Object> linkProperties) {
		this.linkProperties = linkProperties;
	}

	public Object getLinkProperty(LinkProperty name) {
		if ((this.linkProperties == null) || (this.linkProperties.isEmpty())) {
			throw new IllegalStateException("Link properties not initialized");
		}

		return this.linkProperties.get(name);
	}

	@SuppressWarnings("unchecked")
	public void addLinkProperty(LinkProperty name, Object propertyValue) {
		if (this.linkProperties == null) {
			this.linkProperties = new HashMap();
		}

		this.linkProperties.put(name, propertyValue);
	}

	public int hashCode() {
		int n = getSourceNodeId().hashCode() * getTargetNodeId().hashCode();
		return n;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof NodeLink)) {
			return false;
		}

		NodeLink targetNodeLink = (NodeLink) obj;
		if (StringUtil.equals(getSourceNodeId(), targetNodeLink
				.getSourceNodeId())
				&& StringUtil.equals(getTargetNodeId(), targetNodeLink
						.getTargetNodeId()))
			return true;
		return false;
	}

	public String toString() {
		return this.sourceNodeId + "---(" + this.multiplicity + ")--->"
				+ this.targetNodeId;
	}

	public static enum LinkProperty {
		LINK_NAME, LINK_VALUE, IS_SHARED_BY_PRIMARY_KEY, IS_BIDIRECTIONAL, IS_RELATED_VIA_JOIN_TABLE, PROPERTY, BIDIRECTIONAL_PROPERTY, CASCADE, JOIN_TABLE_METADATA;
	}
}
