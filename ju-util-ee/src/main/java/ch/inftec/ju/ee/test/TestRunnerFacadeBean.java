package ch.inftec.ju.ee.test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.logging.Logger;

import ch.inftec.ju.testing.db.DataSet;
import ch.inftec.ju.testing.db.DbDataUtil;
import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.ReflectUtils;

public class TestRunnerFacadeBean implements TestRunnerFacade {
	private static Logger logger = Logger.getLogger(TestRunnerFacadeBean.class);
	
	@Resource
	private EJBContext ctx;

	@Inject
	private EntityManager em;
	
//	@Inject
//	private DateProvider dateProvider;
	
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void runTestMethodInEjbContext(String className, String methodName, TestRunnerContext context) throws Exception {
		logger.debug(String.format("Running Test %s.%s()", className, methodName));
//		this.dateProvider.resetProvider();
		
		try {
			Class<?> clazz = Class.forName(className);
			Object instance = clazz.newInstance();
			Method method = clazz.getMethod(methodName);
			
			// Load test data as defined by annotations
			
			// Get all annotations for the method and the declaring class (including super classes, but
			// excluding overridden methods)
			List<DataSet> dataSets = ReflectUtils.getAnnotations(method, DataSet.class, false, true, true);
			// Reverse the list as we want to start with the base class, then class and method last
			Collections.reverse(dataSets);
			this.loadDataSets(dataSets, clazz);
			
			// Try to set the context (if the class is ContextAware)
			if (ContextAware.class.isAssignableFrom(clazz)) {
				((ContextAware) instance).setContext(context);
			}
			
			// Try to call the init method (if the class implements Initializable)
			if (Initializable.class.isAssignableFrom(clazz)) {
				((Initializable) instance).init();
			}
			
			method.invoke(instance);
		} finally {
			logger.debug("Rolling back changes");
			this.ctx.setRollbackOnly();
		}
	}
	
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Object runMethodInEjbContext(String className, String methodName,
			Class<?> argumentTypes[], Object[] args) throws Exception {
		
		Class<?> clazz = Class.forName(className);
		Object instance = clazz.newInstance();
		
		Method method = clazz.getMethod(methodName, argumentTypes);
		return method.invoke(instance, args);
	}
	
	private void loadDataSets(List<DataSet> dataSets, Class<?> testClass) {
		// Load test data as defined by annotations
		for (DataSet dataSet : dataSets) {
			new DbDataUtil(em).buildImport()
				.from(IOUtil.getResourceURL(dataSet.value(), testClass))
				.executeCleanInsert();
		}
	}
}
