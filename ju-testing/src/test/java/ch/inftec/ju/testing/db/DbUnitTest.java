package ch.inftec.ju.testing.db;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ch.inftec.ju.testing.db.data.repo.TestingEntityRepo;

import com.github.springtestdbunit.annotation.DatabaseSetup;

public class DbUnitTest extends DefaultContextAbstractBaseDbTest {
	@Autowired
	private TestingEntityRepo testingEntityRepo;
		
	@DatabaseSetup("DbUnitTest-singleTestingEntityData.xml")
	@Test
	public void databaseSetup() {
		Assert.assertEquals("SpringDbUnitTest", this.testingEntityRepo.findOne(1L).getName());
	}
}
