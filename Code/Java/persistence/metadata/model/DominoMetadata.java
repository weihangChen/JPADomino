package persistence.metadata.model;

/**
 * main wrapper class, hold CoreMetadata and ApplicationMetadata
 * 
 * @author weihang chen
 * 
 */
public class DominoMetadata {
	private CoreMetadata coreMetadata;
	private ApplicationMetadata applicationMetadata;

	private static DominoMetadata INSTANCE = new DominoMetadata();

	private DominoMetadata() {
		// update should be made later, use Configurator to set values in the
		// init the configurator in application listener
		getApplicationMetadata();
		getCoreMetadata();
	}

	public static synchronized DominoMetadata getInstance() {
		return INSTANCE;
	}

	public ApplicationMetadata getApplicationMetadata() {
		if (this.applicationMetadata == null) {
			this.applicationMetadata = new ApplicationMetadata();
		}
		return this.applicationMetadata;
	}

	public void setApplicationMetadata(ApplicationMetadata applicationMetadata) {
		this.applicationMetadata = applicationMetadata;
	}

	public CoreMetadata getCoreMetadata() {
		if (this.coreMetadata == null) {
			this.coreMetadata = new CoreMetadata();
		}
		return this.coreMetadata;
	}

	public void setCoreMetadata(CoreMetadata coreMetadata) {
		this.coreMetadata = coreMetadata;
	}

}
