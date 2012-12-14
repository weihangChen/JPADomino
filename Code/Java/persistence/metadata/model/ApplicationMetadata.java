package persistence.metadata.model;

//import persistence.configure.schema.SchemaMetadata;
import persistence.core.EntityManagerFactoryImpl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.Metamodel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * this class is supposed to store entity meta data and queries at application
 * scope, at this moment only stores entity meta data, within entries
 * of metamodelMap
 * 
 * <br>
 * everything about query can be skipped for now
 * 
 * @author weihang chen
 * 
 */
public class ApplicationMetadata {
	private Map<String, Metamodel> metamodelMap;
	// private Map<String, PersistenceUnitMetadata> persistenceUnitMetadataMap;
	private static Log logger = LogFactory
			.getLog(EntityManagerFactoryImpl.class);
	// private SchemaMetadata schemaMetadata;
	private Map<String, List<String>> clazzToPuMap;
	private Map<String, QueryWrapper> namedNativeQueries;

	@SuppressWarnings("unchecked")
	public ApplicationMetadata() {
		this.metamodelMap = new ConcurrentHashMap();

		// this.persistenceUnitMetadataMap = new ConcurrentHashMap();

		// this.schemaMetadata = new SchemaMetadata();
	}

	@SuppressWarnings("unchecked")
	public void addEntityMetadata(String persistenceUnit, Class<?> clazz,
			EntityMetadata entityMetadata) {
		Metamodel metamodel = (Metamodel) getMetamodelMap()
				.get(persistenceUnit);
		Map entityClassToMetadataMap = ((MetamodelImpl) metamodel)
				.getEntityMetadataMap();
		if ((entityClassToMetadataMap == null)
				|| (entityClassToMetadataMap.isEmpty())) {
			entityClassToMetadataMap.put(clazz, entityMetadata);
		} else {
			logger
					.debug("Entity meta model already exists for persistence unit "
							+ persistenceUnit
							+ " and class "
							+ clazz
							+ ". Noting needs to be done");
		}
	}

	// public void addPersistenceUnitMetadata(
	// Map<String, PersistenceUnitMetadata> metadata) {
	// getPersistenceUnitMetadataMap().putAll(metadata);
	// }

	@SuppressWarnings("unchecked")
	public Map<String, Metamodel> getMetamodelMap() {
		if (this.metamodelMap == null) {
			this.metamodelMap = new HashMap();
		}
		return this.metamodelMap;
	}

	// public PersistenceUnitMetadata getPersistenceUnitMetadata(
	// String persistenceUnit) {
	// return ((PersistenceUnitMetadata) getPersistenceUnitMetadataMap().get(
	// persistenceUnit));
	// }

	@SuppressWarnings("unchecked")
	public Metamodel getMetamodel(String persistenceUnit) {
		Map model = getMetamodelMap();
		return (((persistenceUnit != null) && (model
				.containsKey(persistenceUnit))) ? (Metamodel) model
				.get(persistenceUnit) : null);
	}

	// public Map<String, PersistenceUnitMetadata>
	// getPersistenceUnitMetadataMap() {
	// return this.persistenceUnitMetadataMap;
	// }

	public void setClazzToPuMap(Map<String, List<String>> map) {
		if (this.clazzToPuMap == null) {
			this.clazzToPuMap = map;
		} else {
			this.clazzToPuMap.putAll(map);
		}
	}

	@SuppressWarnings("unchecked")
	public List<String> getMappedPersistenceUnit(Class<?> clazz) {
		return ((this.clazzToPuMap != null) ? (List) this.clazzToPuMap
				.get(clazz.getName()) : null);
	}

	@SuppressWarnings("unchecked")
	public String getMappedPersistenceUnit(String clazzName) {
		List pus = (List) this.clazzToPuMap.get(clazzName);

		// int _first = 0;
		String pu = null;

		if ((pus != null) && (!(pus.isEmpty()))) {
			if (pus.size() == 2) {
				onError(clazzName);
			}
			return ((String) pus.get(0));
		}

		Set mappedClasses = this.clazzToPuMap.keySet();
		boolean found = false;
		for (Object obj : mappedClasses) {
			String clazz = (String) obj;
			if ((found) && (clazz.endsWith("." + clazzName))) {
				onError(clazzName);
			} else if (clazz.endsWith("." + clazzName)) {
				pu = (String) ((List) this.clazzToPuMap.get(clazz)).get(0);
				found = true;
			}

		}

		return pu;
	}

	@SuppressWarnings("unchecked")
	public void addQueryToCollection(String queryName, String query,
			boolean isNativeQuery, Class clazz) {
		if (this.namedNativeQueries == null) {
			this.namedNativeQueries = new HashMap();
		}
		if (!(this.namedNativeQueries.containsKey(queryName))) {
			this.namedNativeQueries.put(queryName, new QueryWrapper(queryName,
					query, isNativeQuery, clazz));
		} else {
			if (getQuery(queryName).equals(query))
				return;
			logger.error("Duplicate named/native query with name:" + queryName
					+ "found! Already there is a query with same name:"
					+ this.namedNativeQueries.get(queryName));

			throw new PersistenceException(
					"Duplicate named/native query with name:" + queryName
							+ "found! Already there is a query with same name:"
							+ this.namedNativeQueries.get(queryName));
		}
	}

	public String getQuery(String name) {
		QueryWrapper wrapper = (this.namedNativeQueries != null) ? (QueryWrapper) this.namedNativeQueries
				.get(name)
				: null;
		return ((wrapper != null) ? wrapper.getQuery() : null);
	}

	public boolean isNative(String name) {
		QueryWrapper wrapper = (this.namedNativeQueries != null) ? (QueryWrapper) this.namedNativeQueries
				.get(name)
				: null;
		return ((wrapper != null) ? wrapper.isNativeQuery() : false);
	}

	@SuppressWarnings("unchecked")
	public Class getMappedClass(String name) {
		QueryWrapper wrapper = (this.namedNativeQueries != null) ? (QueryWrapper) this.namedNativeQueries
				.get(name)
				: null;
		return ((wrapper != null) ? wrapper.getMappedClazz() : null);
	}

	private void onError(String clazzName) {
		logger.error("Duplicate name:" + clazzName
				+ "Please provide entity with complete package name.");
		throw new PersistenceException("Duplicate name:" + clazzName
				+ "Please provide entity with complete package name");
	}

	// public SchemaMetadata getSchemaMetadata() {
	// return this.schemaMetadata;
	// }

	private class QueryWrapper {
		String queryName;
		String query;
		boolean isNativeQuery;
		@SuppressWarnings("unchecked")
		Class entityClazz;

		@SuppressWarnings("unchecked")
		public QueryWrapper(String paramString1, String paramString2,
				boolean paramBoolean, Class paramClass) {
			this.queryName = paramString1;
			this.query = paramString2;
			// this.isNativeQuery = isNativeQuery;
			this.entityClazz = paramClass;
		}

		String getQuery() {
			return this.query;
		}

		boolean isNativeQuery() {
			return this.isNativeQuery;
		}

		@SuppressWarnings("unchecked")
		Class getMappedClazz() {
			return this.entityClazz;
		}
	}
}
