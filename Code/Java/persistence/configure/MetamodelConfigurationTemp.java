package persistence.configure;

import persistence.metadata.MetadataBuilder;
import persistence.metadata.model.ApplicationMetadata;
import persistence.metadata.model.EntityMetadata;
import persistence.metadata.model.DominoMetadata;
import persistence.metadata.model.MetamodelImpl;
import util.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.persistence.metamodel.Metamodel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import model.resource.PersistenceClasses;

/**
 * this class reads the registered persistent classes from
 * model.resource.PersistenceClasses and store all entity meta data and
 * relations between entities
 * 
 * @author weihang chen
 * 
 */
class MetamodelConfigurationTemp implements Configuration {
	private final String DOMINOPERSISTENUNIT = "DOMINOJPATEST";
	private static Log log = LogFactory
			.getLog(MetamodelConfigurationTemp.class);

	MetamodelConfigurationTemp() {
	}

	public void configure() {
		log.debug("Loading Entity Metadata...");
		loadEntityMetadata();
	}

	/**
	 * create three maps, invoke scanClassAndPutMetadata to manipulate the maps,
	 * set the maps to DominoMetadata.ApplicationMetadata
	 */
	@SuppressWarnings("unchecked")
	private void loadEntityMetadata() {

		DominoMetadata dominoMetadata = DominoMetadata.getInstance();
		ApplicationMetadata appMetadata = dominoMetadata
				.getApplicationMetadata();
		// metamodelImpl implements javax.persistence.metamodel,populate three
		// maps in metamodel
		Metamodel metamodel = new MetamodelImpl();
		// map using class as key , metadata as value
		Map<Class<?>, EntityMetadata> entityMetadataMap = ((MetamodelImpl) metamodel)
				.getEntityMetadataMap();
		// map using class name string as key, class as value
		Map<String, Class<?>> entityNameToClassMap = PersistenceClasses
				.getPersistenceClasses();
		((MetamodelImpl) metamodel)
				.setEntityNameToClassMap(entityNameToClassMap);
		Map puToClazzMap = new HashMap();
		// main method to process all the maps
		scanClassAndPutMetadata(entityMetadataMap, entityNameToClassMap,
				puToClazzMap);
		// set map to DominoMetadata.ApplicationMetadata
		((MetamodelImpl) metamodel).setEntityMetadataMap(entityMetadataMap);
		appMetadata.getMetamodelMap().put(DOMINOPERSISTENUNIT, metamodel);
		appMetadata.setClazzToPuMap(puToClazzMap);

	}

	/**
	 * this method originally go through the persistence.xml, go through every
	 * entry from entityNameClassEntry, get the class, use MetadataBuilder to
	 * build class metadata, put the class as key and metadata as value in
	 * entityMetadataMap
	 * 
	 * @param entityMetadataMap
	 * @param entityNameToClassMap
	 * @param clazzToPuMap
	 */
	private void scanClassAndPutMetadata(
			Map<Class<?>, EntityMetadata> entityMetadataMap,
			Map<String, Class<?>> entityNameToClassMap,
			Map<String, List<String>> clazzToPuMap) {
		Set<Entry<String, Class<?>>> set = entityNameToClassMap.entrySet();
		Iterator<Entry<String, Class<?>>> iter = set.iterator();
		System.out.println("----------CLASS SCANNING STARTS-----------");
		while (iter.hasNext()) {
			Entry<String, Class<?>> entityNameClassEntry = iter.next();
			Class<?> clazz = entityNameClassEntry.getValue();
			EntityMetadata metadata = (EntityMetadata) entityMetadataMap
					.get(clazz);
			if (null == metadata) {
				log.debug("Metadata not found in cache for " + clazz.getName()
						+ " / use metadatbuilder to build metadata");

				MetadataBuilder metadataBuilder = new MetadataBuilder("", "");
				try {
					metadata = metadataBuilder.buildEntityMetadata(clazz);
					metadata.setPersistenceUnit(DOMINOPERSISTENUNIT);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				System.out.println("METHOD SIGNATURE: "
						+ CommonUtil.getMethodName(this.getClass().toString())
						+ " /METHOD DESCRIPTION: entity metadata is added "
						+ metadata);

				if (metadata != null) {
					entityMetadataMap.put(clazz, metadata);
					mapClazztoPu(clazz, DOMINOPERSISTENUNIT, clazzToPuMap);
				}

			}
		}
		System.out.println("----------CLASS SCANNING ENDS-----------");

	}

	/**
	 * do not remember if this method is needed
	 * 
	 * @param clazz
	 * @param pu
	 * @param clazzToPuMap
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, List<String>> mapClazztoPu(Class<?> clazz, String pu,
			Map<String, List<String>> clazzToPuMap) {
		List puCol = new ArrayList(1);
		if (clazzToPuMap == null) {
			clazzToPuMap = new HashMap();
		} else if (clazzToPuMap.containsKey(clazz.getName())) {
			puCol = (List) clazzToPuMap.get(clazz.getName());
		}

		if (!(puCol.contains(pu))) {
			puCol.add(pu);
			clazzToPuMap.put(clazz.getName(), puCol);
		}

		return clazzToPuMap;
	}
}
