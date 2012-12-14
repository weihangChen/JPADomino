package persistence.metadata.processor;

import persistence.metadata.MetadataProcessor;
import persistence.metadata.model.EntityMetadata;
import javax.persistence.Cacheable;

/**
 * second level cache is not implemented
 * 
 * @author weihang chen
 * 
 */
public class CacheableAnnotationProcessor implements MetadataProcessor {

	public final void process(Class<?> entityClass, EntityMetadata metadata) {
		Cacheable cacheable = (Cacheable) entityClass
				.getAnnotation(Cacheable.class);

		if (null == cacheable)
			return;
		metadata.setCacheable(cacheable.value());
	}
}
