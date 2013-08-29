package ch.inftec.ju.db;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.lang3.StringUtils;

import ch.inftec.ju.util.AssertUtil;
import ch.inftec.ju.util.XString;

/**
 * Helper class that provides functionality related to EntitManagerFactories, transactions
 * and the like.
 * <p>
 * This class is likely to be used in a SE or testing environment where we don't have a
 * container taking care of persistence.
 * 
 * @author Martin
 *
 */
public class JuEmfUtil {
	private final EntityManagerFactory emf;

	public JuEmfUtil(EntityManagerFactory emf) {
		this.emf = emf;
	}
	
	public EmfWork startWork() {
		EntityManager em = this.emf.createEntityManager();
		return new EmfWorkImpl(em);
	}
	
	public static JuEmfUtilBuilder create() {
		return new JuEmfUtilBuilder();
	}
	
	public static class JuEmfUtilBuilder {
		private static HashMap<XString, JuEmfUtil> emfUtils = new HashMap<>();
		
		private String persistenceUnitName;
		
		private String connectionUrl;
		
		private String userName;
		private String password;
		
		private String hostName;
		private String dbName;
		
		private String driverClass;
		
		public JuEmfUtilBuilder persistenceUnitName(String persistenceUnitName) {
			this.persistenceUnitName = persistenceUnitName;
			return this;
		}
		
		public JuEmfUtilBuilder connectionUrl(String connectionUrl) {
			this.connectionUrl = connectionUrl;
			return this;
		}
		
		public JuEmfUtil build() {
			AssertUtil.assertNotEmpty("PersistenceUnit name must be specified", this.persistenceUnitName);
			
			// Build a unique identifier String to see if we already have this EmfUtil available.
			XString xs = new XString();
			xs.addItems("|", this.persistenceUnitName, this.connectionUrl);
			
			if (!JuEmfUtilBuilder.emfUtils.containsKey(xs)) {
				// Create a new EmfUtil
				
				String dialect = null;
				String driver = null;
				
				// If a connection URL is specified, we'll try to evaluate the DB type
				if (!StringUtils.isEmpty(this.connectionUrl)) {
					if (this.connectionUrl.startsWith("jdbc:derby")) {
						// Derby DB
						dialect = "org.hibernate.dialect.DerbyDialect";
						driver = "org.apache.derby.jdbc.EmbeddedDriver";
					} else {
						throw new IllegalStateException("Cannot evaluate DB type from connection URL " + this.connectionUrl);
					}
				}
				
				// Prepare properties
				Map<String, String> props = new HashMap<>();
				if (dialect != null) props.put("hibernate.dialect", dialect);
				if (driver != null) props.put("javax.persistence.jdbc.driver", driver);
				if (this.connectionUrl != null) props.put("javax.persistence.jdbc.url", this.connectionUrl);
				
				EntityManagerFactory emf = Persistence.createEntityManagerFactory(this.persistenceUnitName, props);
				JuEmfUtilBuilder.emfUtils.put(xs, new JuEmfUtil(emf));
			}
			
			return JuEmfUtilBuilder.emfUtils.get(xs);
		}
	}
	
	private static class EmfWorkImpl implements EmfWork {
		private final EntityManager em;
		private boolean rollbackOnly = false;
		
		public EmfWorkImpl(EntityManager em) {
			this.em = em;
			this.em.getTransaction().begin();
		}
		
		@Override
		public EntityManager getEm() {
			return this.em;
		}

		@Override
		public JuEmUtil getEmUtil() {
			return new JuEmUtil(this.em);
		}

		@Override
		public void setRollbackOnly() {
			this.rollbackOnly = true;
		}
		
		@Override
		public void close() throws Exception {
			if (this.rollbackOnly) {
				this.em.getTransaction().rollback();
			} else {
				this.em.getTransaction().commit();
			}
			this.em.close();
		}
	}
}
