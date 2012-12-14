package persistence.annotation.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Collection;
import java.util.List;

import persistence.client.Client;
import persistence.core.PersistenceDelegator;
import persistence.metadata.model.EntityMetadata;
import persistence.metadata.model.Relation;

import model.notes.Key;
import model.notes.ModelBase;
import net.sf.cglib.proxy.LazyLoader;
import util.CommonUtil;
import util.ReflectionUtils;

/**
 * this class implements CGLib LazyLoader, when property getter method is
 * invoked the first time, load() method is invoked to populate child
 * collection. load() is invoked only once. This class in action refer to
 * AbstractEntityReader.recursivelyFindEntities()
 * 
 * @author weihang chen
 */
public class CollectionLazyLoader implements LazyLoader {
	protected ModelBase ownerObj;
	protected Relation relation;
	protected ConstructibleAnnotatedCollection constructibleAnnotatedCollection;
	protected PersistenceDelegator persistenceDelegator;
	protected EntityMetadata entityMetadata;

	public CollectionLazyLoader(ModelBase ownerObj, Relation relation,
			ConstructibleAnnotatedCollection constructibleField,
			PersistenceDelegator persistenceDelegator,
			EntityMetadata entityMetadata) throws IllegalArgumentException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {
		this.ownerObj = ownerObj;
		this.relation = relation;
		this.constructibleAnnotatedCollection = constructibleField;
		this.persistenceDelegator = persistenceDelegator;
		this.entityMetadata = entityMetadata;
	}

	/**
	 * 1. get dbclient from entity metadata, in this case DominoDBClient <br>
	 * 2. gather information from relation object, get the foreignkey value <br>
	 * 3. find the corresponding constructor of the collection Field, init the
	 * collection object <br>
	 * 4. invoke dbClient.findAll, get converted objects from database,populate
	 * the collection object with the fetched objects
	 */
	@SuppressWarnings("unchecked")
	public Object loadObject() throws Exception {
		System.out
				.println("!!!!!!!!!!!!!!!!!!!!!!!!LAZY LOADING STARTS!!!!!!!!!!!!!!!!!!!!!!");
		if (ownerObj == null || relation == null
				|| constructibleAnnotatedCollection == null
				|| persistenceDelegator == null)
			return null;
		Client dbClient = persistenceDelegator.getClient(entityMetadata);
		String foreignKey = relation.getDominoForeignKey();
		Method foreignKeyGetter = ReflectionUtils.findMethod(ownerObj
				.getClass(), "get"
				+ CommonUtil.firstCharToUpperCase(foreignKey));
		Collection collection = constructibleAnnotatedCollection
				.getConstructor().newInstance();
		Object foreignKeyFieldValue = ReflectionUtils.invokeGetterMethod(
				ownerObj, foreignKeyGetter);

		if (foreignKeyFieldValue == null)
			return null;
		Key key = new Key();
		key.appendEntry(foreignKeyFieldValue.toString());
		List childs = dbClient.findAll(constructibleAnnotatedCollection
				.getReturnType(), key);
		for (Object o : childs) {
			collection.add(o);
		}
		System.out.println("<=========lazy loaded collection======");
		for (Object o : collection)
			System.out.println(o);
		System.out.println("=========lazy loaded collection======>");

		// the purpose of the code below is to ensure that the colleciton list
		// contains only the entity from the first level cache, I might have
		// missunderstood the whole concept, since the return list is always
		// detached ones
		// System.out.println("before======>");
		//
		// for (Object o : collection)
		// System.out.println(o);
		// ArrayList replaceCollection = new ArrayList();
		// for (Object nodeData : collection) {
		// ObjectGraph graph = new ObjectGraphBuilder().getObjectGraph(
		// nodeData, new ManagedState(), persistenceDelegator
		// .getPersistenceCache());
		// // the head node being returned is always the one from cache if its
		// // found in cache
		// replaceCollection.add(graph.getHeadNode().getData());
		// // merge the graph
		// persistenceDelegator.getPersistenceCache().getMainCache()
		// .addGraphToCache(graph,
		// persistenceDelegator.getPersistenceCache());
		// }
		// System.out
		// .println("!!!!!!!!!!!!!!!!!!!!!!!!LAZY LOADING ENDS!!!!!!!!!!!!!!!!!!!!!!compare the collection before and after");
		//
		// System.out.println("after======>");
		//
		// for (Object o : replaceCollection)
		// System.out.println(o);
		// return replaceCollection;
		return collection;

	}
}
