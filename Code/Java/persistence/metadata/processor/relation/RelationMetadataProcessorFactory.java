package persistence.metadata.processor.relation;

import java.lang.reflect.Field;

import persistence.annotation.DocumentReferences;

/**
 * 
 * RelationMetadataProcessorFactory can only create processor for
 * DocumentReferences annotated Relation in current version of API
 * 
 * @author weihang chen
 * 
 */
public class RelationMetadataProcessorFactory {
	public static RelationMetadataProcessor getRelationMetadataProcessor(
			Field relationField) {
		RelationMetadataProcessor relProcessor = null;

		// if (relationField.isAnnotationPresent(OneToOne.class))
		// {
		// relProcessor = new OneToOneRelationMetadataProcessor();
		// }
		// if (relationField.isAnnotationPresent(OneToMany.class))
		// {
		// relProcessor = new OneToManyRelationMetadataProcessor();
		// }

		if (relationField.isAnnotationPresent(DocumentReferences.class)) {
			relProcessor = new OneToManyRelationMetadataProcessor();
		}

		// else if (relationField.isAnnotationPresent(ManyToOne.class))
		// {
		// relProcessor = new ManyToOneRelationMetadataProcessor();
		// }
		// else if
		// (relationField.isAnnotationPresent(ManyToMany.class))
		// {
		// relProcessor = new ManyToManyRelationMetadataProcessor();
		// }
		return relProcessor;
	}
}
