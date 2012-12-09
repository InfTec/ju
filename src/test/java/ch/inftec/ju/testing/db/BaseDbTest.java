package ch.inftec.ju.testing.db;

import junit.framework.Assert;

import org.junit.Test;

import ch.inftec.ju.testing.db.data.entity.TestingEntity;
import ch.inftec.ju.util.IOUtil;

/**
 * Test cases for the AbstractBaseDb base class.
 * @author Martin
 *
 */
public class BaseDbTest extends AbstractBaseDbTest {
	/**
	 * Test case to assert that primary key sequences will be reset to
	 * produce predictable IDs.
	 */
	@Test
	public void sequenceReset1() {
		this.sequenceReset();
	}
	
	/**
	 * Same as sequenceReset1, to make sure order isn't important.
	 */
	@Test
	public void sequenceReset2() {
		this.sequenceReset();
	}
	
	private void sequenceReset() {
		TestingEntity te = new TestingEntity();
		em.persist(te);
		
		Assert.assertEquals(10, te.getId());
	}
	
	/**
	 * Makes sure that a DefaultDataSet of NONE contains no data.
	 */
	@Test
	public void noData() {
		new DbDataUtil(dbConn).buildAssert()
			.expected(IOUtil.getResourceURL("BaseDbTest_noData.xml"))
			.assertEqualsAll();
	}
	
	@Test
	public void singleTestingEntityData() {
		Assert.assertEquals(0, em.createQuery("select t from TestingEntity t").getResultList().size());
		this.loadDataSet(DefaultDataSet.SINGLE_TESTING_ENTITY);
		Assert.assertEquals(1, em.createQuery("select t from TestingEntity t").getResultList().size());
		
		new DbDataUtil(dbConn).buildAssert()
			.expected(IOUtil.getResourceURL("BaseDbTest_singleTestingEntityData.xml"))
			.assertEqualsTable("TestingEntity", "id");
	}
}
