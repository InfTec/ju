package ch.inftec.ju.ee.test;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import org.jboss.logging.Logger;

import ch.inftec.ju.db.JuEmUtil;
import ch.inftec.ju.db.TxHandler;
import ch.inftec.ju.ee.client.ServiceLocator;
import ch.inftec.ju.ee.client.ServiceLocatorBuilder;
import ch.inftec.ju.testing.db.DataSet;
import ch.inftec.ju.testing.db.DataSetExport;
import ch.inftec.ju.testing.db.DataVerifier;
import ch.inftec.ju.testing.db.DbDataUtil;
import ch.inftec.ju.util.AssertUtil;
import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.JuRuntimeException;
import ch.inftec.ju.util.ReflectUtils;

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
	public void runTestMethodInEjbContext(String className, String methodName, TestRunnerContext context) throws Exception {
		try (TxHandler txHandler = new TxHandler(this.tx, true)) { 
			logger.debug(String.format("Running Test %s.%s()", className, methodName));
	//		this.dateProvider.resetProvider();
			
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
			// Reset the sequences to 1
			new JuEmUtil(this.em).resetIdentityGenerationOrSequences(1);
			txHandler.commit(true);
			
			// Try to set the context (if the class is ContextAware)
			if (ContextAware.class.isAssignableFrom(clazz)) {
				((ContextAware) instance).setContext(context);
			}
			
			// Try to call the init method (if the class implements Initializable)
			if (Initializable.class.isAssignableFrom(clazz)) {
				((Initializable) instance).init();
			}
			
			// Invoke the method
			method.invoke(instance);
			txHandler.commit();
			
			// Export test data if needed
			List<DataSetExport> dataSetExports = ReflectUtils.getAnnotations(method, DataSetExport.class, false, false, false); // TODO: Handle inherited annotations...
			if (dataSetExports.size() == 1) {
				DataSetExport dataSetExport = dataSetExports.get(0);
				
				String targetDirName = "target/dataSetExport";
				// Create target directory
				Path targetDirPath = Paths.get(context.getLocalRoot(), targetDirName);
				Files.createDirectories(targetDirPath);
				
				// Get file name
				String targetFileName = String.format("%s_%s.xml", method.getDeclaringClass().getSimpleName(), method.getName());
				
				// Build file path
				Path targetFilePath = targetDirPath.resolve(targetFileName);
				
				txHandler.begin();
				DbDataUtil du = new DbDataUtil(this.em);
				du.buildExport()
					.addTablesByDataSet(dataSetExport.value(), true)
					.writeToXmlFile(targetFilePath.toString());
				txHandler.commit();
			} else if (dataSetExports.size() > 1) {
				throw new JuRuntimeException("Inherited @DataSetExport annotations not supported yet");
			}
		}
	}
		
	private void loadDataSets(List<DataSet> dataSets, Class<?> testClass) {
		// Load test data as defined by annotations
		for (DataSet dataSet : dataSets) {
			new DbDataUtil(em).buildImport()
				.from(IOUtil.getResourceURL(dataSet.value(), testClass))
				.executeCleanInsert();
		}
	}
	
	@Override
	public void runDataVerifierInEjbContext(List<DataVerifierInfo> dataVerifierInfos) throws Exception {
		try (TxHandler txHandler = new TxHandler(this.tx, true)) { 
			ServiceLocator serviceLocator = ServiceLocatorBuilder.buildLocal().createServiceLocator();
			
			for (DataVerifierInfo info : dataVerifierInfos) {
				Class<?> verifierClass = Class.forName(info.getClassName());
				AssertUtil.assertTrue("Verifier must be of type DataVerifier: " + verifierClass.getName(), DataVerifier.class.isAssignableFrom(verifierClass));
				
				DataVerifier verifier = (DataVerifier) ReflectUtils.newInstance(verifierClass, false, info.getParameters().toArray(new Object[0]));
					
				if (verifier instanceof DataVerifierCdi) {
					((DataVerifierCdi) verifier).init(this.em, serviceLocator);
				} else {
					verifier.init(this.em);
				}
				
				verifier.verify();
			}
			
			txHandler.commit();
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
