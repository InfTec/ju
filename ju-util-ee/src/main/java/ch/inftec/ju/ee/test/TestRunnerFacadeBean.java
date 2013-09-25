package ch.inftec.ju.ee.test;

import java.lang.reflect.Method;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.logging.Logger;

public class TestRunnerFacadeBean implements TestRunnerFacade {
	private static Logger logger = Logger.getLogger(TestRunnerFacadeBean.class);
	
	@Resource
	private EJBContext ctx;

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
			
			// Try to set the context (if the class is ContextAware)
			if (ContextAware.class.isAssignableFrom(clazz)) {
				((ContextAware) instance).setContext(context);
			}
			
			// Try to call the init method (if the class implements Initializable)
			if (Initializable.class.isAssignableFrom(clazz)) {
				((Initializable) instance).init();
			}
			
			Method method = clazz.getMethod(methodName);
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
}
