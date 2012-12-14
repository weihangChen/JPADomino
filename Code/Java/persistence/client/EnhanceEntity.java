package persistence.client;

import java.util.Collections;
import java.util.Map;

/**
 * 
 * @author weihang chen
 *
 */
/**
 * this class is a wrapper of ordinary java pojo class, with extra information
 * such as id and relations
 */
public class EnhanceEntity {
	private Object entity;
	private String entityId;
	private Map<String, Object> relations;

	public EnhanceEntity() {
	}

	public EnhanceEntity(Object entity, String entityId,
			Map<String, Object> relations) {
		this.entity = entity;
		this.entityId = entityId;
		this.relations = relations;
	}

	public Object getEntity() {
		return this.entity;
	}

	public void setEntity(Object entity) {
		this.entity = entity;
	}

	public String getEntityId() {
		return this.entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public Map<String, Object> getRelations() {
		return ((this.relations != null) ? Collections
				.unmodifiableMap(this.relations) : null);
	}

	public void setRelations(Map<String, Object> relations) {
		this.relations = relations;
	}
}
