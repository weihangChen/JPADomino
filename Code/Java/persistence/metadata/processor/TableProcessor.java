package persistence.metadata.processor;

import persistence.annotation.DominoProperty;
import persistence.metadata.model.EntityMetadata;
import persistence.metadata.processor.relation.RelationMetadataProcessor;
import persistence.metadata.processor.relation.RelationMetadataProcessorFactory; //import persistence.metadata.validator.EntityValidatorImpl;
//import persistence.property.PropertyAccessorHelper;
import java.lang.reflect.Field;
import java.util.HashMap;
import javax.persistence.PersistenceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * assign value from annotated DominoProperty to EntityMetadata
 * 
 * @author Weihang Chen this processor populate metadata for relations and query
 */
public class TableProcessor extends AbstractEntityFieldProcessor {
	private static final Log LOG = LogFactory.getLog(TableProcessor.class);

	public TableProcessor() {
		// this.validator = new EntityValidatorImpl();
	}

	@SuppressWarnings("unchecked")
	public void process(Class clazz, EntityMetadata metadata) {
		LOG.debug("Processing @Entity(" + clazz.getName()
				+ ") for Persistence Object.");
		populateMetadata(metadata, clazz);
	}

	private void populateMetadata(EntityMetadata metadata, Class<?> clazz) {
		HashMap<String, Class<?>> fieldTypeMap = new HashMap<String, Class<?>>();
		for (Field field : clazz.getDeclaredFields()) {
			addRelationIntoMetadata(clazz, field, metadata);
			if (field.isAnnotationPresent(DominoProperty.class)) {
				fieldTypeMap.put(field.getName(), field.getType());
			}
		}
		updateView(fieldTypeMap);

	}

	private void updateView(HashMap<String, Class<?>> fieldTypeMap) {

	}

	private void addRelationIntoMetadata(Class<?> entityClass,
			Field relationField, EntityMetadata metadata) {
		RelationMetadataProcessor relProcessor = null;
		try {
			// RelationMetadataProcessorFactory can only create processor for
			// DocumentReferences annotated Relations
			relProcessor = RelationMetadataProcessorFactory
					.getRelationMetadataProcessor(relationField);

			if (relProcessor != null) {
				relProcessor.addRelationIntoMetadata(relationField, metadata);
			}
		} catch (PersistenceException pe) {
			throw new RuntimeException("Error with relationship in @Entity("
					+ entityClass + "." + relationField.getName()
					+ "), reason: " + pe.getMessage());
		}
	}
}
