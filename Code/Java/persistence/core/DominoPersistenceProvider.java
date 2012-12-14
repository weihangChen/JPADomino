package persistence.core;

import persistence.configure.Configurator;
import persistence.core.EntityManagerFactoryImpl;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

import com.ibm.commons.util.NotImplementedException;

/**
 * the class being used to initialize all the set up at application event
 * listener create a unique instance of EntityManagerFactory
 * 
 * @author weihang chen
 * 
 */
public class DominoPersistenceProvider implements PersistenceProvider {
	private final String DOMINOPERSISTENUNIT = "DOMINOJPATEST";
	private static EntityManagerFactory emf;

	public synchronized static EntityManagerFactory getEntityManagerFactory() {
		return emf;
	}

	public DominoPersistenceProvider() {
	}

	@SuppressWarnings("unchecked")
	public final EntityManagerFactory createContainerEntityManagerFactory(
			PersistenceUnitInfo info, Map map) {
		// return createEntityManagerFactory(info.getPersistenceUnitName(),
		// map);
		return createEntityManagerFactory(DOMINOPERSISTENUNIT, map);
	}

	@SuppressWarnings("unchecked")
	public final synchronized EntityManagerFactory createEntityManagerFactory(
			String persistenceUnit, Map map) {
		synchronized (persistenceUnit) {
			// load persistence.xml, luncence.xml, cache global resources, kick
			// start jpa runtime
			initialize(persistenceUnit);
			emf = EntityManagerFactoryImpl.getInstance(persistenceUnit, map);
			return emf;
		}
	}

	/**
	 * main init function , get all configuration class and configure them
	 * 
	 * @param persistenceUnit
	 */
	private void initialize(String persistenceUnit) {
		String[] persistenceUnits = persistenceUnit.split(",");
		Configurator.getInstance(persistenceUnits).configure();
	}

	public ProviderUtil getProviderUtil() {
		throw new NotImplementedException("TODO");
	}
}
