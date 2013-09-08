package ch.inftec.ju.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;

import ch.inftec.ju.util.DataHolder;

/**
 * Helper class building on EntityManager that provides DB utility functions.
 * <p>
 * This class won't handle disposing of EntityManager. It won't handle transactions either. These things
 * should either be handled by a container or by the JuEmfUtil class.
 * @author Martin
 *
 */
public class JuEmUtil {
	private EntityManager em;
	
	/**
	 * Creates a new JuDbUtil based on the specified EntityManager instance.
	 * <p>
	 * Make sure the client takes care of EntityManager disposing
	 * @param em EntityManager instance
	 */
	public JuEmUtil(EntityManager em) {
		this.em = em;
	}
	
	/**
	 * Executes some DB work using a raw JDBC connection.
	 * <p>
	 * Makes use of the Hibernate Work facility.
	 * @param work Work callback interface
	 */
	public void doWork(Work work) {
		Session session = this.em.unwrap(Session.class);
		session.doWork(work);
	}

	/**
	 * Method to extract info from the DatabaseMetaData.
	 * @param action Callback method to do the data extracting into an arbitrary data structure.
	 * @return Data as returned by the callback function
	 */
	public <T> T extractDatabaseMetaData(final DatabaseMetaDataCallback<T> action) {
		final DataHolder<T> res = new DataHolder<>();
		
		this.doWork(new Work() {
			@Override
			public void execute(Connection connection) throws SQLException {
				DatabaseMetaData metaData = connection.getMetaData();
				res.setValue(action.processMetaData(metaData));
			}
		});
		
		return res.getValue();
	}
}
