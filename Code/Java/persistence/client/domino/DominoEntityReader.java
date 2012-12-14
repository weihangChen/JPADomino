package persistence.client.domino;

import persistence.client.Client;
import persistence.client.EnhanceEntity;
import persistence.metadata.model.EntityMetadata;
import persistence.core.AbstractEntityReader;
import persistence.core.EntityReader;
import java.util.List;

import model.notes.Key;

/**
 * need to merge with AbstractEntityReader instead, we have only one Reader (for
 * domino)
 * 
 * @author weihang chen
 * 
 */
public class DominoEntityReader extends AbstractEntityReader implements
		EntityReader {
	@SuppressWarnings("unchecked")
	public List<EnhanceEntity> populateRelation(EntityMetadata m,
			List<String> relationNames, boolean isParent, Client client) {
		throw new UnsupportedOperationException(
				"Method supported not required for mongo");
	}

	@SuppressWarnings("unchecked")
	public EnhanceEntity findById(Key key, EntityMetadata m,
			List<String> relationNames, Client client) {
		return super.findById(key, m, relationNames, client);
	}
}
