package ch.inftec.ju.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import ch.inftec.ju.util.JuCollectionUtils;

/**
 * Implementation of the DbConnectionFactory interface to get DbConnection instances.
 * @author tgdmemae
 *
 */
class DbConnectionFactoryImpl implements DbConnectionFactory {	
	/**
	 * HashMap containing the connection's flags. The linked hash map makes sure the
	 * order is preserved.
	 */
	private LinkedHashMap<String, String[]> flags = new LinkedHashMap<>();

	/**
	 * Hashtable containing the connection's factories.
	 */
	private Hashtable<String, EntityManagerFactory> factories = new Hashtable<>();
	
	/**
	 * Gets the EntityManagerFactory for the specified connection.
	 * @param name Connection name
	 * @return EntityManagerFactory
	 */
	private EntityManagerFactory getEntityManagerFactory(String name) {
		if (!this.factories.containsKey(name)) {
			this.factories.put(name, Persistence.createEntityManagerFactory(name)); 
		}
		
		return this.factories.get(name);
	}
	
	@Override
	public List<String> getAvailableConnections(String... flags) {
		ArrayList<String> connections = new ArrayList<>();
		
		for (String name : this.flags.keySet()) {			
			if (flags.length > 0) {
				String creatorFlags[] = this.flags.get(name);
				
				if (!JuCollectionUtils.isSubsetOf(flags, creatorFlags)) continue;
			}
			
			connections.add(name);
		}
		
		return Collections.unmodifiableList(connections);
	}

	@Override
	public DbConnection openDbConnection(String name) {
		EntityManagerFactory emf = this.getEntityManagerFactory(name);
		return new DbConnectionImpl(name, emf);
	}

	/**
	 * Adds the specified connection to the factory.
	 * @param name Connection name
	 * @param flags Flags of the connection, used by the getAvailableConnections method to filters
	 */
	public void addDbConnection(String name, String... flags) {
		if (this.flags.containsKey(name)) {
			throw new IllegalArgumentException("Factory already contains a connection with the name: " + name);
		}
		
		this.flags.put(name, flags);
	}

	@Override
	public void close() {
		Iterator<EntityManagerFactory> factoriesIterator = this.factories.values().iterator();
		while (factoriesIterator.hasNext()) {
			factoriesIterator.next().close();
			factoriesIterator.remove();
		}
	}
}
