package persistence.metadata.processor;

import persistence.metadata.MetadataProcessor;
import persistence.metadata.model.EntityMetadata; //import persistence.metadata.validator.EntityValidator;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.PersistenceException;
import javax.persistence.Temporal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * methods from this abstract class are not used
 * 
 * @author weihang chen
 * 
 */
public abstract class AbstractEntityFieldProcessor implements MetadataProcessor {
	private static final Log log = LogFactory
			.getLog(AbstractEntityFieldProcessor.class);

	// protected EntityValidator validator;

	public final void validate(Class<?> clazz) throws PersistenceException {
		// this.validator.validate(clazz);
	}

	protected final String getValidJPAColumnName(Class<?> entity, Field f) {
		String name = null;

		if (f.isAnnotationPresent(javax.persistence.Column.class)) {
			javax.persistence.Column c = (javax.persistence.Column) f
					.getAnnotation(javax.persistence.Column.class);
			if (!(c.name().isEmpty())) {
				name = c.name();
			} else {
				name = f.getName();
			}
		} else if (f.isAnnotationPresent(Basic.class)) {
			name = f.getName();
		}

		if (f.isAnnotationPresent(Temporal.class)) {
			if (!(f.getType().equals(Date.class))) {
				log.error("@Temporal must map to java.util.Date for @Entity("
						+ entity.getName() + "." + f.getName() + ")");

				return name;
			}
			if (null == name) {
				name = f.getName();
			}
		}
		return name;
	}

	protected final void populateIdAccessorMethods(EntityMetadata metadata,
			Class<?> clazz, Field f) {
		try {
			BeanInfo info = Introspector.getBeanInfo(clazz);

			for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
				if (!(descriptor.getName().equals(f.getName())))
					continue;
				metadata.setReadIdentifierMethod(descriptor.getReadMethod());
				metadata.setWriteIdentifierMethod(descriptor.getWriteMethod());
				return;
			}

		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
	}

}
