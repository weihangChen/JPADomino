package persistence.core;

import persistence.annotation.support.CollectionLazyLoader;
import persistence.annotation.support.ConstructibleAnnotatedCollection;
import persistence.client.Client;
import persistence.client.EnhanceEntity;
import persistence.metadata.MetadataManager;
import persistence.metadata.model.EntityMetadata;
import persistence.metadata.model.Relation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.FetchType;
import javax.persistence.PersistenceException;
import persistence.property.PropertyAccessorHelper;
import util.CommonUtil;

import util.ReflectionUtils;

import model.notes.Key;
import model.notes.ModelBase;
import net.sf.cglib.proxy.Enhancer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * main class being used to hook up with Client class dealing with database
 * transactions
 * 
 * @author weihang chen
 * 
 */
public class AbstractEntityReader {
	private static Log log = LogFactory.getLog(AbstractEntityReader.class);

	/**
	 * giving an enhanceEntity, recursively find and IOC the wrapped entity's
	 * Field values, set up Lazy Loading mechanism, return the wrapped entity
	 * after data population
	 * 
	 * @param enhanceEntity
	 * @param client
	 * @param m
	 * @param persistenceDelegator
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object recursivelyFindEntities(EnhanceEntity enhanceEntity,
			Client client, EntityMetadata m,
			PersistenceDelegator persistenceDelegator) {
		Map<String, Collection> relationValuesMap = new HashMap<String, Collection>();
		Client childClient = null;
		Class childClass = null;
		Object parentObj = enhanceEntity.getEntity();
		EntityMetadata childMetadata = null;
		// EntityMetadata instance holds all the relations data for an entity
		// class, go through all relations, populate Field values
		for (Relation relation : m.getRelations()) {
			// get info from relation object
			Relation.ForeignKey multiplicity = relation.getType();
			Field collectionField = relation.getProperty();
			childClass = relation.getTargetEntity();
			String relationSignature = relation.getDominoRelationSignature();
			FetchType fetchType = relation.getFetchType();
			// relation one_to_many == documentsreferences
			// if foreignkey + viewname is the same, children collections is the
			// same
			if (multiplicity.equals(Relation.ForeignKey.ONE_TO_MANY)) {

				childMetadata = MetadataManager.getEntityMetadata(childClass);

				childClient = persistenceDelegator.getClient(childMetadata);
				// relationValue ex. document uniqueid is the relationalValue

				if ((relationSignature != null)
						&& (!(relationValuesMap.containsKey(relationSignature
								+ childClass.getName())))) {
					// two situations here, eager, then fetch the collection and
					// assign it to the field
					// if its lazy, send in the required parameters so lazy
					// loader knows how to initialize the collection and merging
					// with the object graph
					System.out
							.println("RELATIONVALUEMAP DOES NOT CONTAIN THE COLLECTION, INIT COLLECTION ISSUED");
					Class<?> collectionClass = collectionField.getType();
					Method collectionSetterMethod = ReflectionUtils
							.getFieldSetterMethod(collectionField);
					Method collectionGetterMethod = ReflectionUtils
							.getFieldGetterMethod(collectionField);
					// ex. ArrayList<CSS> list=new ArrayList<CSS>(); CSS is the
					// returnType
					Class<?> returnType = ReflectionUtils
							.resolveReturnType(collectionGetterMethod);
					// constructor for the Collection Field
					Constructor<Collection<Object>> constructor = null;
					Collection concreteCollection = null;
					try {
						constructor = ReflectionUtils.findCtor(collectionField);
						concreteCollection = constructor.newInstance();
					} catch (Exception e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					// EAGER
					if (fetchType == FetchType.EAGER) {
						System.out.println("EAGER COLLECTION FETCH INIT");
						List childs = null;
						// ex. foreignKey = "unid" then use the keyword unid to
						// get the getter method name getUnid(), invoke the
						// method to get the document unid as foreignkey for the
						// child collection
						String foreignKey = relation.getDominoForeignKey();
						Method foreignKeyGetter = ReflectionUtils
								.findMethod(
										parentObj.getClass(),
										"get"
												+ CommonUtil
														.firstCharToUpperCase(foreignKey));
						Object foreignKeyFieldValue = ReflectionUtils
								.invokeGetterMethod(parentObj, foreignKeyGetter);

						if (foreignKeyFieldValue == null)
							continue;
						Key key = new Key();
						key.appendEntry(foreignKeyFieldValue.toString());

						try {
							// use DBClient to find all
							childs = childClient.findAll(childClass, key);
							System.out
									.println("RETURN CHILDREN COLLECTION FROM DATABASE "
											+ childs);
							// recursive find using the found children
							if ((childs != null) && (!(childs.isEmpty()))) {
								for (Iterator i = childs.iterator(); i
										.hasNext();) {
									Object child = i.next();
									Object o = (child instanceof EnhanceEntity) ? ((EnhanceEntity) child)
											.getEntity()
											: child;

									if (!(o.getClass().equals(enhanceEntity
											.getEntity().getClass()))) {
										recursivelyFindEntities(
												new EnhanceEntity(
														o,
														PropertyAccessorHelper
																.getId(o,
																		childMetadata),
														null), childClient,
												childMetadata,
												persistenceDelegator);
									}
									concreteCollection.add(o);
								}
								relationValuesMap.put(relationSignature
										+ childClass.getName(),
										concreteCollection);
							}
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						System.out.println("EAGER COLLECTION FETCH ENDS");
					} else {
						// CGLIB lazyloader
						System.out.println("LAZY COLLECTION INIT");
						ConstructibleAnnotatedCollection constructibleField = new ConstructibleAnnotatedCollection(
								collectionField, constructor, returnType);
						try {
							// create a CollectionLazyLoader instance with all
							// necessary info, create a CGLib object with the
							// CollectionLazyLoader, assign it to the Field,
							// once collection getter is invoked, database
							// transaction will happen to populate the Field
							CollectionLazyLoader collectionLazyLoader = new CollectionLazyLoader(
									(ModelBase) parentObj, relation,
									constructibleField, persistenceDelegator,
									childMetadata);
							Object lazyCollection = Enhancer.create(
									collectionClass, collectionLazyLoader);
							collectionSetterMethod.invoke(parentObj,
									lazyCollection);
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						System.out
								.println("LAZY COLLECTION INIT finished, no real collection is initied, only set up the lazyloader");
					}
				} else
					System.out
							.println("---------RELATIONVALUEMAP ALREDAY EXIST IN THE RELATIONMAP, JUST COPY OVER THE VALUE FROM relationValuesMap note that even its the same relationship if its eager ("
									+ fetchType
									+ ")/ relationsignature(eager and lazy has differnt signature): "
									+ relationSignature);

				ReflectionUtils.setFieldObject(parentObj, collectionField,
						relationValuesMap.get(relationSignature
								+ childClass.getName()));

			}
		}
		return enhanceEntity.getEntity();

	}

	/**
	 * use DBClient to find one entity by id
	 * 
	 * @param key
	 * @param entityMetadata
	 * @param relationNames
	 * @param client
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected EnhanceEntity findById(Key key, EntityMetadata entityMetadata,
			List<String> relationNames, Client client) {
		try {
			System.out.println("use client to find object: " + client
					+ " with key " + key + "/entity class: "
					+ entityMetadata.getEntityClazz());
			Object entity = client.find(entityMetadata.getEntityClazz(), key);
			if (entity == null) {
				return null;
			}
			return new EnhanceEntity(entity, getId(entity, entityMetadata),
					null);
		} catch (Exception e) {
			throw new PersistenceException(e);
		}
	}

	@SuppressWarnings( { "unused", "unchecked" })
	private Set<?> onReflect(Object entity, Field ownerField, List<?> childs)
			throws RuntimeException {
		Set chids = new HashSet();
		if (childs != null) {
			chids = new HashSet(childs);

			PropertyAccessorHelper
					.set(entity, ownerField,
							(PropertyAccessorHelper.isCollection(ownerField
									.getType())) ? getFieldInstance(childs,
									ownerField) : childs.get(0));
		}
		return chids;
	}

	@SuppressWarnings("unchecked")
	private Object getFieldInstance(List chids, Field f) {
		if (Set.class.isAssignableFrom(f.getType())) {
			Set col = new HashSet(chids);
			return col;
		}
		return chids;
	}

	/**
	 * get node id from entity meta data
	 * 
	 * @param entity
	 * @param metadata
	 * @return node id
	 */
	protected String getId(Object entity, EntityMetadata metadata) {
		if (entity instanceof ModelBase) {
			return ((ModelBase) entity).getUnid();
		}
		try {
			return PropertyAccessorHelper.getId(entity, metadata);
		} catch (RuntimeException e) {
			log.error("Error while Getting ID. Details:" + e.getMessage());
			throw new PersistenceException("Error while Getting ID for entity "
					+ entity, e);
		}
	}

}
