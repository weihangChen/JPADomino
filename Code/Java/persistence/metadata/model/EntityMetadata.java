package persistence.metadata.model;

import persistence.event.CallbackMethod;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.PrimaryKeyJoinColumn;

/**
 * 
 * IMPORTANT: only DOMINO SPECIFIC variables and relationsMap are in use in current
 * version of API
 * 
 * @DominoEntity annotation values are saved to dbName/formName/viewName;
 * @DominoProperty annotation Field values are saved to relationsMap and fetched
 *                 via getRelations() <br>
 * 
 * @author weihang chen
 * 
 */
@SuppressWarnings("unchecked")
public final class EntityMetadata {
	Class<?> entityClazz;
	private String tableName;
	private String schema;
	private String persistenceUnit;
	private String indexName;
	private boolean isIndexable = true;

	private boolean cacheable = false;
	private Method readIdentifierMethod;
	private Method writeIdentifierMethod;
	private Map<String, Column> columnsMap = new HashMap();

	private Map<Class, List> callbackMethodsMap = new HashMap();
	/**
	 * 
	 */
	private Map<String, Relation> relationsMap = new HashMap();
	private List<String> relationNames;
	private boolean isParent;

	// DOMINO SPECIFIC
	private String dbName;
	private String formName;
	private String viewName;

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public EntityMetadata(Class<?> entityClazz) {
		this.entityClazz = entityClazz;
	}

	public Class<?> getEntityClazz() {
		return this.entityClazz;
	}

	public String getTableName() {
		return this.tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getSchema() {
		return this.schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getPersistenceUnit() {
		return this.persistenceUnit;
	}

	public void setPersistenceUnit(String persistenceUnit) {
		this.persistenceUnit = persistenceUnit;
	}

	public Method getReadIdentifierMethod() {
		return this.readIdentifierMethod;
	}

	public void setReadIdentifierMethod(Method readIdentifierMethod) {
		this.readIdentifierMethod = readIdentifierMethod;
	}

	public Method getWriteIdentifierMethod() {
		return this.writeIdentifierMethod;
	}

	public void setWriteIdentifierMethod(Method writeIdentifierMethod) {
		this.writeIdentifierMethod = writeIdentifierMethod;
	}

	public Map<String, Column> getColumnsMap() {
		return this.columnsMap;
	}

	public Column getColumn(String key) {
		return ((Column) this.columnsMap.get(key));
	}

	public List<Column> getColumnsAsList() {
		return new ArrayList(this.columnsMap.values());
	}

	public List<String> getColumnFieldNames() {
		return new ArrayList(this.columnsMap.keySet());
	}

	public void addColumn(String key, Column column) {
		this.columnsMap.put(key, column);
	}

	public void addRelation(String property, Relation relation) {
		this.relationsMap.put(property, relation);
		addRelationName(relation);
	}

	public Relation getRelation(String property) {
		return ((Relation) this.relationsMap.get(property));
	}

	public List<Relation> getRelations() {
		return new ArrayList(this.relationsMap.values());
	}

	public String getIndexName() {
		return this.indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public boolean isIndexable() {
		return this.isIndexable;
	}

	public void setIndexable(boolean isIndexable) {
		this.isIndexable = isIndexable;
	}

	public Map<Class, List> getCallbackMethodsMap() {
		return this.callbackMethodsMap;
	}

	public void setCallbackMethodsMap(Map<Class, List> callbackMethodsMap) {
		this.callbackMethodsMap = callbackMethodsMap;
	}

	public List<? extends CallbackMethod> getCallbackMethods(Class<?> event) {
		return ((List) this.callbackMethodsMap.get(event));
	}

	public boolean isCacheable() {
		return this.cacheable;
	}

	public void setCacheable(boolean cacheable) {
		this.cacheable = cacheable;
	}

	// /* */ public String toString()
	// /* */ {
	// /* 653 */ int start = 0;
	// /* 654 */ StringBuilder builder = new StringBuilder();
	// /* 655 */ builder.append(new
	// StringBuilder().append(this.entityClazz.getName()).append(" (\n").toString());
	// /* 656 */ builder.append(new
	// StringBuilder().append("\tTable: ").append(this.tableName).append(", \n").toString());
	// /* 657 */ builder.append(new
	// StringBuilder().append("\tKeyspace: ").append(this.schema).append(",\n").toString());
	// /* 658 */ builder.append(new
	// StringBuilder().append("\tPersistence Unit: ").append(this.persistenceUnit).append(",\n").toString());
	// /* 660 */ builder.append(new
	// StringBuilder().append("\tReadIdMethod: ").append(this.readIdentifierMethod.getName()).append(",\n").toString());
	// /* 661 */ builder.append(new
	// StringBuilder().append("\tWriteIdMethod: ").append(this.writeIdentifierMethod.getName()).append(",\n").toString());
	// /* 662 */ builder.append(new
	// StringBuilder().append("\tCacheable: ").append(this.cacheable).append(",\n").toString());
	// /* */
	// /* 664 */ if (!(this.columnsMap.isEmpty()))
	// /* */ {
	// /* 666 */ builder.append("\tColumns (");
	// /* 667 */ for (Column col : this.columnsMap.values())
	// /* */ {
	// /* 669 */ if (start++ != 0)
	// /* */ {
	// /* 671 */ builder.append(", ");
	// /* */ }
	// /* 673 */ builder.append(col.name());
	// /* */ }
	// /* 675 */ builder.append("),\n");
	// /* */ }
	// /* */
	//
	//
	// /* */
	// /* 717 */ if (!(this.callbackMethodsMap.isEmpty()))
	// /* */ {
	// /* 719 */ builder.append("\tListeners (\n");
	// /* 720 */ for (Map.Entry entry : this.callbackMethodsMap.entrySet())
	// /* */ {
	// /* 722 */ String key = ((Class)entry.getKey()).getSimpleName();
	// /* 723 */ for (Object obj : (List)entry.getValue())
	// /* */ {
	// CallbackMethod cbm=(CallbackMethod)obj;
	// /* 725 */ builder.append(new
	// StringBuilder().append("\t\t").append(key).append(": ").append(cbm).append("\n").toString());
	// /* */ }
	// /* */ }
	// /* */ String key;
	// /* 728 */ builder.append("\t)\n");
	// /* */ }
	// /* */
	// /* 731 */ if (!(this.relationsMap.isEmpty()))
	// /* */ {
	// /* 733 */ builder.append("\tRelation (\n");
	// /* 734 */ for (Relation rel : this.relationsMap.values())
	// /* */ {
	// /* 736 */ builder.append(new
	// StringBuilder().append("\t\t").append(rel.getTargetEntity().getName()).append("#").append(rel.getProperty().getName()).toString());
	// /* 737 */ builder.append(new
	// StringBuilder().append(" (").append(rel.getCascades()).toString());
	// /* 738 */ builder.append(new
	// StringBuilder().append(", ").append(rel.getType()).toString());
	// /* 739 */ builder.append(new
	// StringBuilder().append(", ").append(rel.fetchType).toString());
	// /* 740 */ builder.append(")\n");
	// /* */ }
	// /* 742 */ builder.append("\t)\n");
	// /* */ }
	// /* */
	// /* 745 */ builder.append(")");
	// /* 746 */ return builder.toString();
	// /* */ }

	public boolean isParent() {
		return this.isParent;
	}

	public void setParent(boolean isParent) {
		this.isParent = isParent;
	}

	public List<String> getRelationNames() {
		return this.relationNames;
	}

	private void addRelationName(Relation rField) {
		if (rField.isRelatedViaJoinTable())
			return;
		String relationName = getJoinColumnName(rField.getProperty());
		if (rField.getProperty()
				.isAnnotationPresent(PrimaryKeyJoinColumn.class)) {
			// relationName = getIdColumn().getName();
		}

		addToRelationNameCollection(relationName);
	}

	private void addToRelationNameCollection(String relationName) {
		if (this.relationNames == null) {
			this.relationNames = new ArrayList();
		}
		if (relationName == null)
			return;
		this.relationNames.add(relationName);
	}

	private String getJoinColumnName(Field relation) {
		String columnName = null;
		JoinColumn ann = (JoinColumn) relation.getAnnotation(JoinColumn.class);
		if (ann != null) {
			columnName = ann.name();
		}

		return ((columnName != null) ? columnName : relation.getName());
	}

	@SuppressWarnings("unused")
	public static enum Type {
		COLUMN_FAMILY {
			public boolean isColumnFamilyMetadata() {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean isDocumentMetadata() {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean isSuperColumnFamilyMetadata() {
				// TODO Auto-generated method stub
				return false;
			}
		},
		SUPER_COLUMN_FAMILY {
			public boolean isColumnFamilyMetadata() {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean isDocumentMetadata() {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean isSuperColumnFamilyMetadata() {
				// TODO Auto-generated method stub
				return false;
			}
		};

	}
}
