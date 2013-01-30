package ch.inftec.ju.testing.db.data;

import java.util.List;

import javax.persistence.Query;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import ch.inftec.ju.db.DbRows;
import ch.inftec.ju.testing.db.AbstractBaseDbTest;
import ch.inftec.ju.testing.db.data.entity.Player;
import ch.inftec.ju.util.JuCollectionUtils;

/**
 * Tests to test a TestDb instance.
 * @author tgdmemae
 *
 */
@ContextConfiguration(classes={TestDbTest.Configuration.class})
public class TestDbTest extends AbstractBaseDbTest {
	static class Configuration {
		@Bean
		private DefaultDataSet fullData() {
			return AbstractBaseDbTest.DefaultDataSet.FULL;
		}
	}

	/**
	 * Tests if the table TEST_A has been created correctly.
	 */
	@Test
	public final void testA() {
		List<String> columnNames = dbConn.getColumnNames("TEST_A");
			
		Assert.assertEquals(3, columnNames.size());
		Assert.assertTrue(columnNames.containsAll(JuCollectionUtils.arrayList("AID", "TEXT", "B_FK")));
		
		DbRows rows = dbConn.getQueryRunner().query("select * from test_a");
		Assert.assertEquals(3, rows.getRowCount());
	}
	
	/**
	 * Tests if the JPA entities (Team, Player) have been created correctly.
	 */
	@Test
	public final void testEntities() {		
		Query q = em.createQuery("select p from Player p where p.firstName='All' and p.lastName='Star'");
		Player allstar = (Player)q.getSingleResult();		
		
		Assert.assertEquals("AllStar", allstar.getFirstName() + allstar.getLastName());
		Assert.assertEquals(2, allstar.getTeams().size());
		Assert.assertEquals(3, allstar.getTeams().iterator().next().getPlayers().size());
	}
}
