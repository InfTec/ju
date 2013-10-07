package ch.inftec.ju.ee.test;

import java.lang.reflect.Method;

import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import org.jboss.logging.Logger;

import ch.inftec.ju.db.JuEmUtil;
import ch.inftec.ju.db.TxHandler;
import ch.inftec.ju.ee.client.ServiceLocatorBuilder;

/**
 * Helper bean to run container tests in the container.
 * <p>
 * We'll use bean managed transaction management to control the transactions better.
 * @author Martin
 *
 */
@TransactionManagement(TransactionManagementType.BEAN)
public class TestRunnerFacadeBean implements TestRunnerFacade {
	private static Logger logger = Logger.getLogger(TestRunnerFacadeBean.class);
	
	@Inject
	private UserTransaction tx;

	@Inject
	private EntityManager em;
		
//	@Inject
//	private DateProvider dateProvider;
	
	@Override
	public void runTestMethodInEjbContext(TestRunnerAnnotationHandler handler) throws Exception {
		JuEmUtil emUtil = new JuEmUtil(this.em);
		
		try (TxHandler txHandler = new TxHandler(this.tx, true)) { 
			
			logger.debug(String.format("Running Test %s", handler));
	//		this.dateProvider.resetProvider();
			
			// Execute pre test annotations (dataset loading)
			handler.executePreTestAnnotations(emUtil);
			txHandler.commit(true); // Perform commit before we execute the test method
			
			// Run the test method
			handler.executeTestMethod();
			txHandler.commit(); // Perform another commit after the execution of the test method
		}
	}
	
	@Override
	public void runPostTestActionsInEjbContext(TestRunnerAnnotationHandler handler) throws Exception {
		try (TxHandler txHandler = new TxHandler(this.tx, true)) { 
			// Execute post test annotations (dataset exporting, data verifying)
			handler.executePostTestAnnotations(new JuEmUtil(this.em), ServiceLocatorBuilder.buildLocal().createServiceLocator());
			txHandler.commit(); // Commit after data verifying / exporting
		}
	}


	@Override
	public Object runMethodInEjbContext(String className, String methodName,
			Class<?> argumentTypes[], Object[] args) throws Exception {
		try (TxHandler txHandler = new TxHandler(this.tx, true)) {
			Class<?> clazz = Class.forName(className);
			Object instance = clazz.newInstance();
			
			Method method = clazz.getMethod(methodName, argumentTypes);
			Object res = method.invoke(instance, args);
			
			txHandler.commit();
			return res;
		}
	}
}
