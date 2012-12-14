package persistence.context;

import persistence.context.jointable.JoinTableData;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.persistence.PersistenceContextType;

/**
 * wrapper class holding some important objects such as
 * CacheBase/FlushManager/FlushStack
 * 
 * @author weihang chen
 * 
 */
public class PersistenceCache {
	private CacheBase mainCache;

	FlushManager flushManager;
	private PersistenceContextType persistenceContextType;
	private FlushStack flushStack;
	private Map<String, JoinTableData> joinTableDataMap;

	public PersistenceCache() {
		initialize();
	}

	@SuppressWarnings("unchecked")
	private void initialize() {
		this.mainCache = new MainCache();
		this.flushStack = new FlushStack();
		this.joinTableDataMap = new HashMap();
		this.flushManager = new FlushManager();
	}

	public void clean() {
		if (this.mainCache != null) {
			this.mainCache.clear();
		}

		if (this.flushStack != null) {
			this.flushStack.clear();
		}
		if (this.joinTableDataMap == null)
			return;
		this.joinTableDataMap.clear();
	}

	public CacheBase getMainCache() {
		return this.mainCache;
	}

	public void setMainCache(CacheBase mainCache) {
		this.mainCache = mainCache;
	}

	public FlushStack getFlushStack() {
		return this.flushStack;
	}

	public void setFlushStack(FlushStack flushStack) {
		this.flushStack = flushStack;
	}

	public Map<String, JoinTableData> getJoinTableDataMap() {
		return this.joinTableDataMap;
	}

	public PersistenceContextType getPersistenceContextType() {
		return this.persistenceContextType;
	}

	public void setPersistenceContextType(
			PersistenceContextType persistenceContextType) {
		this.persistenceContextType = persistenceContextType;
	}

	// not used
	public void addJoinTableDataIntoMap(JoinTableData.OPERATION operation,
			String joinTableName, String joinColumnName,
			String invJoinColumnName, Class<?> entityClass,
			Object joinColumnValue, Set<Object> invJoinColumnValues) {
		JoinTableData joinTableData = (JoinTableData) this.joinTableDataMap
				.get(joinTableName);
		if (joinTableData == null) {
			joinTableData = new JoinTableData(operation, joinTableName,
					joinColumnName, invJoinColumnName, entityClass);
			joinTableData.addJoinTableRecord(joinColumnValue,
					invJoinColumnValues);
			this.joinTableDataMap.put(joinTableName, joinTableData);
		} else {
			joinTableData.addJoinTableRecord(joinColumnValue,
					invJoinColumnValues);
		}
	}
}
