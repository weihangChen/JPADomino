package persistence.metadata;

import persistence.metadata.model.EntityMetadata;
import persistence.metadata.model.DominoMetadata;
import persistence.metadata.model.MetamodelImpl; //import persistence.metadata.model.PersistenceUnitMetadata;
import persistence.proxy.EntityEnhancerFactory; //import persistence.proxy.LazyInitializerFactory;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class used to access EntityMetadata from MetamodelImpl which is
 * stored in DominoMetadata.getApplicationMetadata()
 * 
 * @author weihang chen
 * 
 */
public class MetadataManager {
	private static Log log = LogFactory.getLog(MetadataManager.class);

	// public static PersistenceUnitMetadata getPersistenceUnitMetadata(
	// String persistenceUnit) {
	// return DominoMetadata.INSTANCE.getApplicationMetadata()
	// .getPersistenceUnitMetadata(persistenceUnit);
	// }

	public static MetamodelImpl getMetamodel(String persistenceUnit) {
		DominoMetadata dominoMetadata = DominoMetadata.getInstance();

		MetamodelImpl metamodel = (MetamodelImpl) dominoMetadata
				.getApplicationMetadata().getMetamodel(persistenceUnit);

		return metamodel;
	}

	/**
	 * not used
	 * 
	 * @param persistenceUnits
	 * @return
	 */
	public static MetamodelImpl getMetamodel(String[] persistenceUnits) {
		DominoMetadata dominoMetadata = DominoMetadata.getInstance();

		MetamodelImpl metamodel = null;
		for (String pu : persistenceUnits) {
			metamodel = (MetamodelImpl) dominoMetadata.getApplicationMetadata()
					.getMetamodel(pu);

			if (metamodel != null) {
				return metamodel;
			}

		}

		return metamodel;
	}

	/**
	 * not used
	 * 
	 * @param persistenceUnit
	 * @param entityClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static EntityMetadata getEntityMetadata(String persistenceUnit,
			Class entityClass) {
		return getMetamodel(persistenceUnit).getEntityMetadata(entityClass);
	}

	/**
	 * main method to access entity meta data by reading in a class instance
	 * 
	 * @param entityClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static EntityMetadata getEntityMetadata(Class entityClass) {
		List<String> persistenceUnits = DominoMetadata.getInstance()
				.getApplicationMetadata().getMappedPersistenceUnit(entityClass);
		if (persistenceUnits != null) {
			for (String pu : persistenceUnits) {
				MetamodelImpl metamodel = getMetamodel(pu);
				EntityMetadata metadata = metamodel
						.getEntityMetadata(entityClass);
				if (metadata != null) {
					return metadata;
				}
			}
		}
		log
				.warn("No Entity metadata found for the class "
						+ entityClass
						+ ". Any CRUD operation on this entity will fail."
						+ "If your entity is for RDBMS, make sure you put fully qualified entity class"
						+ " name under <class></class> tag in persistence.xml for RDBMS "
						+ "persistence unit. Returning null value.");

		return null;
	}

	/**
	 * not used
	 * 
	 * @param entityClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static EntityMetadata getEntityMetadata1(Class entityClass) {
		// hard coded string name, need to identical to the one being used in
		// configuration "persistenceXMLString"
		String DOMINOPERSISTENUNIT = "DOMINOJPATEST";
		MetamodelImpl metaModel = getMetamodel(DOMINOPERSISTENUNIT);
		System.out.println("metamodel is: " + metaModel);
		EntityMetadata metadata = metaModel.getEntityMetadata(entityClass);
		return metadata;
	}

	// public static LazyInitializerFactory getLazyInitializerFactory() {
	// return DominoMetadata.INSTANCE.getCoreMetadata()
	// .getLazyInitializerFactory();
	// }

	/**
	 * not used
	 */
	public static EntityEnhancerFactory getEntityEnhancerFactory() {
		return DominoMetadata.getInstance().getCoreMetadata()
				.getEnhancedProxyFactory();
	}
}
