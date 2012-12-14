package util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import lotus.domino.Document;
import model.notes.ModelBase;
import persistence.annotation.support.JavaBeanFactory;
import persistence.metadata.model.EntityMetadata;
import persistence.metadata.model.Relation;
import persistence.metadata.model.Relation.ForeignKey;

import com.ibm.xsp.model.domino.wrapped.DominoDocument;
import com.ibm.xsp.model.domino.wrapped.DominoDocument.FieldValueHolder;

/**
 * make deepclone of a ModelBase object
 * 
 * @author weihang chen
 * 
 */
public class CloneUtil {
	/**
	 * 1. use SerialClone to copy the _changedFields <br>
	 * 2. if its newNote, no need to make the clone, if its not newNote, use
	 * JavaBeanFactory to create a new ModelBase object, assigned the cloned
	 * _changeFields to the ModelBase object<br>
	 * NOTICE1: DominoDocument.wrap hard coded<br>
	 * NOTICE2: if its newNote, do not make clone of it (a new doc will not
	 * override an existing doc). For all existing entities, there are a managed
	 * one in persistence context and a detached one being exposed to user, for
	 * a new entity, after its saved, managed entity within persistence context
	 * is the same as the one being exposed to user
	 * 
	 * 
	 * @param <E>
	 * @param originalEntity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <E> E cloneDominoEntity(E originalEntity) {
		if (!(originalEntity instanceof ModelBase))
			return null;
		E copy = null;
		try {
			((ModelBase) originalEntity).checkState();
			DominoDocument doc = ((ModelBase) originalEntity).getDoc();
			Map<String, FieldValueHolder> e = doc.getChangedFields();
			// clone the map which holds the update
			Object clonedChangedFields = SerialClone.clone(e);
			Field stateField = DominoDocument.class
					.getDeclaredField("_changedFields");
			stateField.setAccessible(true);
			boolean isNew = doc.isNewNote();
			if (isNew) {
				return originalEntity;
			} else {
				// if its existing doc
				Document notesDoc = doc.getDocument();
				DominoDocument dominoDoc1 = DominoDocument.wrap("", notesDoc,
						"both", "force", true, "", "");
				copy = (E) JavaBeanFactory.getInstance(JSFUtil
						.getRealClass(originalEntity.getClass()), dominoDoc1);
			}
			// assign clonedChangedFields to the clone's _changedFields
			stateField.set(((ModelBase) copy).getDoc(), clonedChangedFields);
			// recursive clone
			recursiveClone(originalEntity, copy);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return copy;
	}

	/**
	 * go through relations from the stored meta data, get the children , repeat
	 * the same process recursively
	 * 
	 * @param <E>
	 * @param entity
	 * @param copy
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("unchecked")
	public static <E> void recursiveClone(E entity, E copy)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		EntityMetadata entityMetadata = persistence.metadata.MetadataManager
				.getEntityMetadata(JSFUtil.getRealClass(entity.getClass()));
		if (entityMetadata == null)
			return;
		// the only supported Relation is one-to-many
		for (Relation relation : entityMetadata.getRelations()) {
			ForeignKey key = relation.getType();
			Field collectionField = relation.getProperty();
			Method collectionSetterMethod = ReflectionUtils
					.getFieldSetterMethod(collectionField);

			Constructor<Collection<Object>> constructor = null;
			Collection concreteCollection = null;
			try {
				constructor = ReflectionUtils.findCtor(collectionField);
				concreteCollection = constructor.newInstance();
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			// if it is one to many
			if (key == ForeignKey.ONE_TO_MANY) {
				collectionField.setAccessible(true);
				Collection collection = (Collection) ReflectionUtils
						.getFieldObject(entity, collectionField);
				if (collection == null)
					continue;

				for (Object baseObj : collection) {
					if (!(baseObj instanceof ModelBase))
						continue;
					concreteCollection.add(cloneDominoEntity(baseObj));
				}
				// System.out.println("check see if they are lazy init: "+childObject);
			}
			collectionSetterMethod.invoke(copy, concreteCollection);
		}
	}
}
