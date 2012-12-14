package persistence.metadata.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

/**
 * the class holding the entityMetadata
 * 
 * @author SWECWI
 * 
 */
public class MetamodelImpl implements Metamodel {
	Map<Class<?>, EntityMetadata> entityMetadataMap;
	Map<String, Class<?>> entityNameToClassMap;

	public <X> EntityType<X> entity(Class<X> paramClass) {
		return null;
	}

	public <X> ManagedType<X> managedType(Class<X> paramClass) {
		return null;
	}

	public <X> EmbeddableType<X> embeddable(Class<X> paramClass) {
		return null;
	}

	public Set<ManagedType<?>> getManagedTypes() {
		return null;
	}

	public Set<EntityType<?>> getEntities() {
		return null;
	}

	public Set<EmbeddableType<?>> getEmbeddables() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public MetamodelImpl() {
		setEntityMetadataMap(new HashMap());
	}

	@SuppressWarnings("unchecked")
	public Map<Class<?>, EntityMetadata> getEntityMetadataMap() {
		if (this.entityMetadataMap == null) {
			this.entityMetadataMap = new HashMap();
		}
		return this.entityMetadataMap;
	}

	public void setEntityMetadataMap(
			Map<Class<?>, EntityMetadata> entityMetadataMap) {
		this.entityMetadataMap = entityMetadataMap;
	}

	public void addEntityMetadata(Class<?> clazz, EntityMetadata entityMetadata) {
		getEntityMetadataMap().put(clazz, entityMetadata);
	}

	@SuppressWarnings("unchecked")
	public EntityMetadata getEntityMetadata(Class<?> entityClass) {
		Iterator iter = getEntityMetadataMap().entrySet().iterator();
		@SuppressWarnings("unused")
		Entry o = (Entry) iter.next();
		return ((EntityMetadata) getEntityMetadataMap().get(entityClass));
	}

	@SuppressWarnings("unchecked")
	public Map<String, Class<?>> getEntityNameToClassMap() {
		if (this.entityNameToClassMap == null) {
			this.entityNameToClassMap = new HashMap();
		}
		return this.entityNameToClassMap;
	}

	public void setEntityNameToClassMap(
			Map<String, Class<?>> entityNameToClassMap) {
		this.entityNameToClassMap = entityNameToClassMap;
	}

	public void addEntityNameToClassMapping(String className,
			Class<?> entityClass) {
		getEntityNameToClassMap().put(className, entityClass);
	}

	@SuppressWarnings("unchecked")
	public Class<?> getEntityClass(String className) {
		return ((Class) getEntityNameToClassMap().get(className));
	}

	public String toString() {
		return this.entityMetadataMap.toString();
	}
}
