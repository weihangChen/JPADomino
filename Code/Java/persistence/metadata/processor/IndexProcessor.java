package persistence.metadata.processor;

import persistence.annotation.Index;
import persistence.metadata.MetadataProcessor;
import persistence.metadata.model.EntityMetadata; //import persistence.metadata.model.PropertyIndex;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Id;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * no implemented
 * 
 * @author weihang chen
 * 
 */
public class IndexProcessor implements MetadataProcessor {
	private static Log log = LogFactory.getLog(IndexProcessor.class);

	@SuppressWarnings("unchecked")
	public final void process(Class<?> clazz, EntityMetadata metadata) {
		metadata.setIndexName(clazz.getSimpleName());
		Index idx = (Index) clazz.getAnnotation(Index.class);
		List columnsToBeIndexed = new ArrayList();

		if (null != idx) {
			boolean isIndexable = idx.index();
			metadata.setIndexable(isIndexable);

			String indexName = idx.name();
			if ((indexName != null) && (!(indexName.isEmpty()))) {
				metadata.setIndexName(indexName);
			} else {
				metadata.setIndexName(clazz.getSimpleName());
			}

			if ((idx.columns() != null) && (idx.columns().length != 0)) {
				columnsToBeIndexed = Arrays.asList(idx.columns());
			}

			if (!(isIndexable)) {
				log.debug(new StringBuilder().append("@Entity ").append(
						clazz.getName()).append(" will not be indexed for ")
						.append(
								(columnsToBeIndexed.isEmpty()) ? "all columns"
										: columnsToBeIndexed).toString());

				return;
			}
		}

		log.debug(new StringBuilder().append("Processing @Entity ").append(
				clazz.getName()).append(" for Indexes.").toString());

		for (Field f : clazz.getDeclaredFields()) {
			if (f.isAnnotationPresent(Id.class)) {
				String alias = f.getName();
				alias = getIndexName(f, alias);
				// metadata.addIndexProperty(new PropertyIndex(f, alias));
			} else {
				if (!(f.isAnnotationPresent(Column.class)))
					continue;
				String alias = f.getName();
				alias = getIndexName(f, alias);

				if ((!(columnsToBeIndexed.isEmpty()))
						&& (!(columnsToBeIndexed.contains(alias))))
					continue;
				// metadata.addIndexProperty(new PropertyIndex(f, alias));
			}
		}
	}

	private String getIndexName(Field f, String alias) {
		if (f.isAnnotationPresent(Column.class)) {
			Column c = (Column) f.getAnnotation(Column.class);
			alias = c.name().trim();
			if (alias.isEmpty()) {
				alias = f.getName();
			}
		}
		return alias;
	}
}
