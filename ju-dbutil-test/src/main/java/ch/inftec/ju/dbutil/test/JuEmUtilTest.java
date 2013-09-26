package ch.inftec.ju.dbutil.test;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.db.DatabaseMetaDataCallback;
import ch.inftec.ju.db.JuEmUtil.DbType;
import ch.inftec.ju.testing.db.AbstractDbTest;
import ch.inftec.ju.testing.db.DbDataUtil;
import ch.inftec.ju.testing.db.DbSchemaUtil;
import ch.inftec.ju.testing.db.JuAssumeUtil;
import ch.inftec.ju.testing.db.data.entity.TestingEntity;

public class JuEmUtilTest extends AbstractDbTest {
	@Test
	public void processMetaData() {
		String url = this.emUtil.extractDatabaseMetaData(new DatabaseMetaDataCallback<String>() {
			@Override
			public String processMetaData(DatabaseMetaData dbmd) throws SQLException {
				return dbmd.getURL();
			}
		});
		
		logger.debug("Got DB URL: " + url);
		Assert.assertTrue(url.startsWith("jdbc:"));
	}
	
	@Test
	public void getDriverName_returnsNotNull() {
		Assert.assertNotNull(this.emUtil.getDbType());
	}
	
	@Test
	public void getMetaDataUserName_returnsNotNull() {
		Assert.assertNotNull(this.emUtil.getMetaDataUserName());
	}
	
	@Test
	public void canListSequences() {
		JuAssumeUtil.dbIsNot(this.emUtil, DbType.MYSQL); // Sequences are not supported by MqSQL
		
		new DbSchemaUtil(this.emUtil).runLiquibaseChangeLog("ch/inftec/ju/dbutil/test/JuEmUtilTest_canListSequences.xml");
		
		List<String> sequenceNames = this.emUtil.getSequenceNames();
		Assert.assertTrue(sequenceNames.contains("TESTSEQUENCE"));
	}
	
	@Test
	public void canResetIdentityGeneration_orSequences() {
		new DbSchemaUtil(this.emUtil).prepareDefaultTestData();
		
		// Try to set identity generation to 10
		this.emUtil.resetIdentityGenerationOrSequences(10);
		TestingEntity te1 = new TestingEntity();
		this.em.persist(te1);
		Assert.assertEquals(new Long(10L), te1.getId());
		
		// Delete all TestingEntities and set identity generation to 1
		this.em.createQuery("delete from TestingEntity t").executeUpdate();
		this.emUtil.resetIdentityGenerationOrSequences(1);
		TestingEntity te2 = new TestingEntity();
		this.em.persist(te2);
		Assert.assertEquals(new Long(1L), te2.getId());
	}
	
	@Test
	public void canEvaluate_primaryKeyColumnName_withSingleColumn() {
		new DbSchemaUtil(this.emUtil).prepareDefaultTestData();
		
		List<String> primaryKeyColumns = this.emUtil.getPrimaryKeyColumns("TestingEntity");
		Assert.assertEquals(1, primaryKeyColumns.size());
		Assert.assertEquals("id", primaryKeyColumns.get(0).toLowerCase());
	}
	
	@Test
	public void canEvaluate_primaryKeyColumnName_withMultipleColumns() {
		new DbSchemaUtil(this.emUtil).prepareDefaultTestData();
		
		List<String> primaryKeyColumns = this.emUtil.getPrimaryKeyColumns("Team_Player");
		Assert.assertEquals(2, primaryKeyColumns.size());
		Assert.assertEquals("players_id", primaryKeyColumns.get(0).toLowerCase());
		Assert.assertEquals("teams_id", primaryKeyColumns.get(1).toLowerCase());
	}
	
}
