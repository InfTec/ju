package ch.inftec.ju.testing.db;

import javax.persistence.EntityManager;

import junit.framework.Assert;

import org.junit.Test;

import ch.inftec.ju.db.EmWork;
import ch.inftec.ju.db.JuDbUtils;
import ch.inftec.ju.testing.db.data.entity.TestingEntity;

public class LiquibaseTest {
	@Test
	public void canGenerateSchema_usingLiquibase() {
		new DbSchemaUtil().runLiquibaseChangeLog("ju pu-liquibase", "ch/inftec/ju/testing/db/LiquibaseTest_testingEntityChangeLog.xml");
		
		JuDbUtils.createByPersistenceUnitName("ju pu-liquibase").doWork(new EmWork() {
			@Override
			public void execute(EntityManager em) {
				TestingEntity te = new TestingEntity();
				em.persist(te);
				Assert.assertNotNull(te);
			}
		});
	}
}
