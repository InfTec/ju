package ch.inftec.ju.db;

import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ContextHolder class used by the ConnectionInfoRoutingDataSource to set the
 * ConnectionInfo that should be used for the current Thread.
 * <p>
 * Note that the ConnectionInfo will only apply to the NEXT Spring transaction.
 * We cannot change the ConnectionInfo for the currently running transaction.
 * @author Martin
 *
 */
public class ConnectionInfoContextHolder {
	static final Logger logger = LoggerFactory.getLogger(ConnectionInfoContextHolder.class);
	
	private static final ThreadLocal<ConnectionInfo> contextHolder = new ThreadLocal<>();
	private static Set<ConnectionInfo> availableConnectionInfos = new LinkedHashSet<>();
	
	/**
	 * Sets the ConnectionInfo to be used by the following database interactions / transaction.
	 * <p>
	 * If we set the ConnectionInfo within a transaction, it will be applied to the next
	 * Transaction that is started - or to any database interaction without a transaction.
	 * @param connectionInfo New ConnectionInfo
	 */
	public static void setConnectionInfo(ConnectionInfo connectionInfo) {
		logger.debug("Setting ConnectionInfo: " + connectionInfo);
		contextHolder.set(connectionInfo);
	}

	public static ConnectionInfo getConnectionInfo() {
		return contextHolder.get();
	}

	/**
	 * Clears the ConnectionInfo, meaning the default ConnectionInfo will be used.
	 */
	public static void clearConnectionInfo() {
		contextHolder.remove();
	}
	
	static void setAvailableConnectionInfos(Set<ConnectionInfo> availableConnectionInfos) {
		ConnectionInfoContextHolder.availableConnectionInfos = availableConnectionInfos; 
	}
	
	/**
	 * Gets a set of all available ConnectionInfo instances.
	 * @return Set of available ConnectionInfo instances
	 */
	public static Set<ConnectionInfo> getAvailableConnectionInfos() {
		return availableConnectionInfos;
	}
}
