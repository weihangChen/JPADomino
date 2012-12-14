package persistence.metadata.processor.relation;

import persistence.metadata.model.EntityMetadata;
import java.lang.reflect.Field;

/**
 * all relation relation processors need to implement this interface
 * 
 * @author weihang chen
 * 
 */
public abstract interface RelationMetadataProcessor {
	public abstract void addRelationIntoMetadata(Field paramField,
			EntityMetadata paramEntityMetadata);
}
