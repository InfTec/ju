package ch.inftec.ju.db.specific;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import ch.inftec.ju.db.BasicDbTest;
import ch.inftec.ju.db.JpaTest;
import ch.inftec.ju.db.JuDbUtilsTest;
import ch.inftec.ju.db.change.DbActionTest;
import ch.inftec.ju.db.change.DbChangeSetTest;

/**
 * Runs all DB tests using an Oracle test DB instead of Derby.
 * @author tgdmemae
 *
 */
@RunWith(Enclosed.class)
public class DbTestsOracle {
	@ContextConfiguration(locations="classpath:/ch/inftec/ju/db/specific/DbTestsOracle-context.xml", inheritLocations=false)
	public static class BasicDbTestOracle extends BasicDbTest {}

	@ContextConfiguration(locations="classpath:/ch/inftec/ju/db/specific/DbTestsOracle-context.xml", inheritLocations=false)
	public static class JpaTestOracle extends JpaTest {}
	
	@ContextConfiguration(locations="classpath:/ch/inftec/ju/db/specific/DbTestsOracle-context.xml", inheritLocations=false)
	public static class JuDbUtilsTestOracle extends JuDbUtilsTest {}
	
	@ContextConfiguration(locations="classpath:/ch/inftec/ju/db/specific/DbTestsOracle-context.xml", inheritLocations=false)
	public static class DbActionTestOracle extends DbActionTest {}
	
	@ContextConfiguration(locations="classpath:/ch/inftec/ju/db/specific/DbTestsOracle-context.xml", inheritLocations=false)
	public static class DbChangeSetTestOracle extends DbChangeSetTest {}
}
