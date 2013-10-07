package ch.inftec.ju.ee.test;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import ch.inftec.ju.db.JuEmUtil;
import ch.inftec.ju.ee.client.ServiceLocator;
import ch.inftec.ju.ee.test.TestRunnerFacade.ContextAware;
import ch.inftec.ju.ee.test.TestRunnerFacade.Initializable;
import ch.inftec.ju.ee.test.TestRunnerFacade.TestRunnerContext;
import ch.inftec.ju.testing.db.DataSet;
import ch.inftec.ju.testing.db.DataSetExport;
import ch.inftec.ju.testing.db.DataSetVerify;
import ch.inftec.ju.testing.db.DataVerifier;
import ch.inftec.ju.testing.db.DataVerify;
import ch.inftec.ju.testing.db.DbDataUtil;
import ch.inftec.ju.util.AssertUtil;
import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.JuRuntimeException;
import ch.inftec.ju.util.ReflectUtils;
import ch.inftec.ju.util.xml.XmlUtils;

/**
 * Helper class to handle test annotations like @DataSet and @DataVerify.
 * <p>
 * The handler is serializable so it can be used for container tests that run code in different JVMs.
 * <p>
 * When calling the execute... methods, the client is responsible that a valid transaction is present.
 * @author Martin
 *
 */
class TestRunnerAnnotationHandler implements Serializable {
	private final List<DataSet> dataSetAnnos;
	private final List<DataSetExport> dataSetExportAnnos;
	private final List<DataSetVerify> dataSetVerifyAnnos;
	private final List<DataVerify> dataVerifyAnnos;
	
	private final String testClassName;
	private final String testMethodName;
	private final TestRunnerContext context;
	
	TestRunnerAnnotationHandler(Method method, TestRunnerContext context) {
		// Get all annotations for the method and the declaring class (including super classes, but
		// excluding overridden methods)
		this.dataSetAnnos = ReflectUtils.getAnnotations(method, DataSet.class, false, true, true);
		// Reverse the list as we want to start with the base class, then class and method last
		Collections.reverse(this.dataSetAnnos);
		
		this.dataSetExportAnnos = ReflectUtils.getAnnotations(method, DataSetExport.class, false, false, false); // TODO: Handle inherited annotations...
		this.dataSetVerifyAnnos = ReflectUtils.getAnnotations(method, DataSetVerify.class, true, false, false);
		this.dataVerifyAnnos = ReflectUtils.getAnnotations(method, DataVerify.class, true, false, false);
		
		this.testClassName = method.getDeclaringClass().getName();
		this.testMethodName = method.getName();
		this.context = context;
	}
	
	private Class<?> getTestClass() {
		try {
			return Class.forName(this.testClassName);
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't get test class. Make sure it's on the classpath: " + this.testClassName);
		}
	}
	
	private Method getTestMethod() {
		return ReflectUtils.getMethod(this.getTestClass(), this.testMethodName, new Class<?>[0]);
	}
	
	TestRunnerContext getContext() {
		return context;
	}
	
	void executePreTestAnnotations(JuEmUtil emUtil) {
		// Load test data as defined by annotations
		for (DataSet dataSet : this.dataSetAnnos) {
			new DbDataUtil(emUtil).buildImport()
				.from(IOUtil.getResourceURL(dataSet.value(), this.getTestClass()))
				.executeCleanInsert();
		}
		
		// Reset the sequences to 1
		emUtil.resetIdentityGenerationOrSequences(1);
	}
	
	void executeTestMethod() {
		try {
			// Get an instance of the test method so we can invoke it
			Class<?> clazz = Class.forName(this.testClassName);
			Object instance = clazz.newInstance();
			Method method = clazz.getMethod(this.testMethodName);
			
			// Try to set the context (if the class is ContextAware)
			if (ContextAware.class.isAssignableFrom(clazz)) {
				((ContextAware) instance).setContext(this.getContext());
			}
			
			// Try to call the init method (if the class implements Initializable)
			if (Initializable.class.isAssignableFrom(clazz)) {
				((Initializable) instance).init();
			}
			
			// Invoke the method
			method.invoke(instance);
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't execute test method " + this, ex);
		}
	}
	
	void executePostTestAnnotations(JuEmUtil emUtil, ServiceLocator serviceLocator) throws Exception {
		// Export test data
		Document doc = null;
		if (this.dataSetExportAnnos.size() == 1) {
			DataSetExport dataSetExport = this.dataSetExportAnnos.get(0);
			
			String targetDirName = "target/dataSetExport";
			// Create target directory
			Path targetDirPath = Paths.get(this.getContext().getLocalRoot(), targetDirName);
			Files.createDirectories(targetDirPath);
			
			// Get file name
			String targetFileName = String.format("%s_%s.xml"
					,this.getTestClass().getSimpleName()
					, this.getTestMethod().getName());
			
			// Build file path
			Path targetFilePath = targetDirPath.resolve(targetFileName);
			
			DbDataUtil du = new DbDataUtil(emUtil);
			du.buildExport()
				.addTablesByDataSet(dataSetExport.value(), true)
				.writeToXmlFile(targetFilePath.toString());
			
			doc = XmlUtils.loadXml(Paths.get(targetFilePath.toString()).toUri().toURL());
		} else if (this.dataSetExportAnnos.size() > 1) {
			throw new JuRuntimeException("Inherited @DataSetExport annotations not supported yet");
		}
		
		// Run data verifiers (provided the test method and data set export has succeeded)
		List<DataVerifier> verifiers = new ArrayList<DataVerifier>();
		
		// First, check if we need to perform DataSetVerify
		for (DataSetVerify dataSetVerify : this.dataSetVerifyAnnos) {
			verifiers.add(new DataSetVerifier(dataSetVerify.value(), emUtil.getEm()));
		}
		
		// Now check for programmatic verifiers
		for (DataVerify verify : this.dataVerifyAnnos) {
			Class<?> verifierClass = null;
			if (verify.value() == DataVerify.DEFAULT_DATA_VERIFIER.class) {
				String verifierName = StringUtils.capitalize(this.testMethodName);
				Class<?> defaultVerifier = ReflectUtils.getInnerClass(this.getTestClass(), verifierName);
				AssertUtil.assertNotNull(String.format("Couldn't find Verifier %s as inner class of %s. Make sure it exists and is public static."
							, verifierName, this.getTestClass())
						, defaultVerifier);
				
				verifierClass = defaultVerifier;
			} else {
				verifierClass = verify.value();
			}
			
			verifiers.add(this.createVerifier(verifierClass, emUtil.getEm(), doc, serviceLocator));
		}
		
		// Run verifiers
		for (DataVerifier verifier : verifiers) {
			verifier.verify();
		}
	}
	
	private DataVerifier createVerifier(Class<?> verifierClass, EntityManager em, Document doc, ServiceLocator serviceLocator) {
		AssertUtil.assertTrue("Verifier must be of type DataVerifier: " + verifierClass.getName(), DataVerifier.class.isAssignableFrom(verifierClass));
		
		DataVerifier verifier = (DataVerifier) ReflectUtils.newInstance(verifierClass, false);
			
		if (verifier instanceof DataVerifierCdi) {
			((DataVerifierCdi) verifier).init(em, doc, serviceLocator);
		} else {
			verifier.init(em, doc);
		}

		return verifier;
	}
	
	@Override
	public String toString() {
		return String.format("%s.%s()", this.testClassName, this.testMethodName);
	}
	
	/**
	 * Data verifier that verifies agains a data set.
	 * @author Martin
	 *
	 */
	private static class DataSetVerifier extends DataVerifier {
		private final String dataSet;
		
		public DataSetVerifier(String dataSet, EntityManager em) {
			this.dataSet = dataSet;
			this.init(em, null);
		}
		
		@Override
		public void verify() {
			DbDataUtil du = new DbDataUtil(this.emUtil);
			du.buildAssert()
				.expected(this.dataSet)
				.assertEquals();
		}
	}
}
