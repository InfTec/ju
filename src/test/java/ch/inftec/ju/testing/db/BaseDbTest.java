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
	public BaseDbTest() {
		super(DefaultDataSet.NONE);
	}
	
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
	
	@Test
	public void noData() {
		new DbDataUtil(dbConn).buildAssert()
			.expected(IOUtil.getResourceURL("BaseDbTest_noData.xml"))
			.assertEqualsAll();
	}
}
