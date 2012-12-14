package persistence.core;

import persistence.client.Client;
import persistence.client.EnhanceEntity;
import persistence.metadata.model.EntityMetadata;
import java.util.List;

import model.notes.Key;

/**
 * 
 * @author weihang chen
 * 
 */
public abstract interface EntityReader {
	@SuppressWarnings("unchecked")
	public abstract List<EnhanceEntity> populateRelation(
			EntityMetadata paramEntityMetadata, List<String> paramList,
			boolean paramBoolean, Client paramClient);

	@SuppressWarnings("unchecked")
	public abstract Object recursivelyFindEntities(
			EnhanceEntity paramEnhanceEntity, Client paramClient,
			EntityMetadata paramEntityMetadata,
			PersistenceDelegator paramPersistenceDelegator);

	@SuppressWarnings("unchecked")
	public abstract EnhanceEntity findById(Key key,
			EntityMetadata paramEntityMetadata, List<String> paramList,
			Client paramClient);
}
