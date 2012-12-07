package ch.inftec.ju.db;

import java.io.BufferedReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.JuObjectUtils;

/**
 * DB utility class. Mainly contains wrapper method for Apache commons DbUtils
 * to encapsulate the SQLExceptions and throw JuDbExceptions or to provide
 * log information on failed methods.
 * @author tgdmemae
 *
 */
public class DbUtil {
	static Logger log = LoggerFactory.getLogger(DbConnectionImpl.class);
	
	/**
	 * Commits and closes a connection.
	 * @param conn Connection
	 * @throws JuDbException If the commit and/or close fails
	 */
	public static void commitAndClose(Connection conn) throws JuDbException {
		try {
			log.debug("Commiting and closing connection [ID=" + JuObjectUtils.getIdentityString(conn) + "]");
			DbUtils.commitAndClose(conn);
		} catch (SQLException ex) {
			throw new JuDbException("Couldn't commit and close connection", ex);
		}
	}
	
	/**
	 * Rolls back and closes a connection.
	 * @param conn Connection
	 * @throws JuDbException If the rollback and/or close fails
	 */
	public static void rollbackAndClose(Connection conn) throws JuDbException {
		try {
			log.debug("Rolling back and closing connection [ID=" + JuObjectUtils.getIdentityString(conn) + "]");
			DbUtils.rollbackAndClose(conn);
		} catch (SQLException ex) {
			throw new JuDbException("Couldn't roll back and close connection", ex);
		}
	}
	
	/**
	 * Closes the specified connection, catching any SQLException that might be thrown.
	 * @param conn Connection to be closed
	 */
	public static void closeQuietly(Connection conn) {
		try {
			log.debug("Closing connection [ID=" + JuObjectUtils.getIdentityString(conn) + "]");
			DbUtils.close(conn);
		} catch (SQLException ex) {
			log.error("Couldn't close connection", ex);
		}
	}
	
	/**
	 * Closes the specified result set, catching any SQLException that might be thrown.
	 * @param rs ResultSet to be closed
	 */
	public static void closeQuietly(ResultSet rs) {
		try {
			DbUtils.close(rs);
		} catch (SQLException ex) {
			log.error("Couldn't close connection", ex);
		}
	}
	
	/**
	 * Converts the specified Clob into a String
	 * @param clob Database Clob
	 * @return String
	 * @throws JuDbException If the conversion fails
	 */
	public static String getClobString(Clob clob) throws JuDbException {
		if (clob == null) return null;
		
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(clob.getCharacterStream());
			String line;
			while ((line = reader.readLine()) != null) sb.append(line);
		} catch (Exception ex) {
			throw new JuDbException("Couldn't convert CLOB to String", ex);
		} finally {
			IOUtil.close(reader);
		}
		
		return sb.toString();
	}
}
