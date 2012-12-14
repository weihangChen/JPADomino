package persistence.metadata;

import persistence.metadata.model.EntityMetadata;
import persistence.metadata.processor.DominoEntityProcessor;
import persistence.metadata.processor.TableProcessor;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.PersistenceException;

/**
 * main class being used from MetamodelConfigurationTemp to build up entity meta
 * data for a class. Two MetadataProcessors are in use <br>
 * 1.DominoEntityProcessor - process entity annotated with @DominoEntity<br>
 * 2.TableProcessor - process Fields annotated with @DominoProperty
 * 
 * @author weihang chen
 */

public class MetadataBuilder {

	private List<MetadataProcessor> metadataProcessors;
	// private EntityValidator validator;
	// private boolean instantiated = false;
	@SuppressWarnings("unused")
	private String persistenceUnit;
	@SuppressWarnings("unused")
	private String client;

	/**
	 * 
	 * @param puName
	 * @param client
	 */

	public MetadataBuilder(String puName, String client) {
		this.persistenceUnit = puName;
		this.client = client;
		// this.validator = new EntityValidatorImpl();
		this.metadataProcessors = new ArrayList<MetadataProcessor>();
		// this.metadataProcessors.add(new TableProcessor());
		// this.metadataProcessors.add(new CacheableAnnotationProcessor());
		// this.metadataProcessors.add(new IndexProcessor());
		// this.metadataProcessors.add(new EntityListenersProcessor());
		this.metadataProcessors.add(new DominoEntityProcessor());
		this.metadataProcessors.add(new TableProcessor());

	}

	/**
	 * add entity validator back when persistence.xml is in use, not implemented
	 */
	public final void validate(Class<?> clazz) throws PersistenceException {

		// this.validator.validate(clazz);
	}

	/**
	 * go through MetadataProcessors and set them up
	 * 
	 * @param clazz
	 * @return
	 */
	public EntityMetadata buildEntityMetadata(Class<?> clazz) {
		EntityMetadata metadata = new EntityMetadata(clazz);
		validate(clazz);
		for (MetadataProcessor processor : this.metadataProcessors) {
			processor.process(clazz, metadata);
		}
		return metadata;
	}

}
