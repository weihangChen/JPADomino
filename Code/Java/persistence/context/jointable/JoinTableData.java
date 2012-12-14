package persistence.context.jointable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * not used
 * 
 * @author weihang chen
 * 
 */
public class JoinTableData {
	private String joinTableName;
	private Class<?> entityClass;
	private String joinColumnName;
	private String inverseJoinColumnName;
	private OPERATION operation;
	Map<Object, Set<Object>> joinTableRecords;

	@SuppressWarnings("unchecked")
	public JoinTableData(OPERATION operation, String joinTableName,
			String joinColumnName, String inverseJoinColumnName,
			Class<?> entityClass) {
		this.operation = operation;
		this.joinTableName = joinTableName;
		this.joinColumnName = joinColumnName;
		this.inverseJoinColumnName = inverseJoinColumnName;
		this.entityClass = entityClass;

		this.joinTableRecords = new HashMap();
	}

	public String getJoinTableName() {
		return this.joinTableName;
	}

	public void setJoinTableName(String joinTableName) {
		this.joinTableName = joinTableName;
	}

	public String getJoinColumnName() {
		return this.joinColumnName;
	}

	public void setJoinColumnName(String joinColumnName) {
		this.joinColumnName = joinColumnName;
	}

	public String getInverseJoinColumnName() {
		return this.inverseJoinColumnName;
	}

	public void setInverseJoinColumnName(String inverseJoinColumnName) {
		this.inverseJoinColumnName = inverseJoinColumnName;
	}

	public Map<Object, Set<Object>> getJoinTableRecords() {
		return this.joinTableRecords;
	}

	public Class<?> getEntityClass() {
		return this.entityClass;
	}

	public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	public OPERATION getOperation() {
		return this.operation;
	}

	public void setOperation(OPERATION operation) {
		this.operation = operation;
	}

	@SuppressWarnings("unchecked")
	public void addJoinTableRecord(Object key, Set<Object> values) {
		Set existingValues = (Set) this.joinTableRecords.get(key);
		if (existingValues == null) {
			existingValues = new HashSet();
			existingValues.addAll(values);
			this.joinTableRecords.put(key, existingValues);
		} else {
			existingValues.addAll(values);
		}
	}

	public static enum OPERATION {
		INSERT, UPDATE, DELETE;
	}
}
