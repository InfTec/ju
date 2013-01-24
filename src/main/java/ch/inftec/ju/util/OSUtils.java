package ch.inftec.ju.util;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for OS specific functionality.
 * <p>
 * This class uses the NTSystem and UnixSystem class and will not work on all
 * environments / with all JDKs.
 * <p>
 * Unix will return null for the domain name.
 * @author tgdmemae
 *
 */
public class OSUtils {
	private static XSystem[] xSystems = new XSystem[] {
			new XSystemImpl("com.sun.security.auth.module.NTSystem", "getName", "getDomain"),
			new XSystemImpl("com.sun.security.auth.module.UnixSystem", "getUsername", null)
	};
	
	/**
	 * Gets the windows login name.
	 * @return Windows login name
	 */
	public static String getUserName() {
		return OSUtils.getXSystem().getUserName();
	}
	
	/**
	 * Gets the windows domain name.
	 * @return Windows domain name
	 */
	public static String getDomainName() {
		return OSUtils.getXSystem().getDomainName();
	}
	
	private static XSystem getXSystem() {
		for (XSystem xSystem : OSUtils.xSystems) {
			if (xSystem.isAvailable()) {
				return xSystem;
			}
		}
		throw new JuRuntimeException("No XSystem implementation available");
	}
	
	private static interface XSystem {
		String getUserName();
		String getDomainName();
		boolean isAvailable();
	}
	
	private static class XSystemImpl implements XSystem {
		Logger logger = LoggerFactory.getLogger(XSystemImpl.class);
		
		private final String className;
		private final Class<?> clazz;
		private final String userNameMethodName;
		private final String domainNameMethodName;
		
		public XSystemImpl(String className, String userNameMethodName, String domainNameMethodName) {
			this.className = className;
			this.userNameMethodName = userNameMethodName;
			this.domainNameMethodName = domainNameMethodName;
			
			Class<?> clazz = null;
			try {
				clazz = Class.forName(this.className);
			} catch (ClassNotFoundException ex) {
				logger.warn("Class not found: " + this.className);
			}
			
			this.clazz = clazz;
		}
		
		public boolean isAvailable() {
			return this.clazz != null;
		}
		
		private String getValue(String methodName) {
			if (methodName == null) return null;
			
			String className = "";
			try {
				Method method = clazz.getMethod(methodName);
				Object instance = clazz.newInstance();
				
				return (String)method.invoke(instance);
			} catch (Exception ex) {
				throw new JuRuntimeException(String.format("Couldn't invoke method %s of %s. Make sure this is a Oracle Windows JDK.", methodName, className), ex);
			}
		}
		
		@Override
		public String getUserName() {
			return this.getValue(this.userNameMethodName);
		}
		
		@Override
		public String getDomainName() {
			return this.getValue(this.domainNameMethodName);
		}
	}
}
