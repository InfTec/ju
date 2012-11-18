package ch.inftec.ju.db.data;

import java.util.List;

import javax.persistence.Query;

import junit.framework.Assert;

import org.junit.Test;

import ch.inftec.ju.db.AbstractBaseDbTest;
import ch.inftec.ju.db.DbRows;
import ch.inftec.ju.db.data.entity.Player;
import ch.inftec.ju.util.JuCollectionUtils;

/**
 * Generic tests to test a TestDb instance. Extending classes must override
 * the getTestDb method and return an instance to a TestDb implementation.
 * @author tgdmemae
 *
 */
public abstract class AbstractTestDbTest extends AbstractBaseDbTest {
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
		Assert.assertEquals(TestDbUtils.ENTITY_TEAM_COUNT, allstar.getTeams().size());
		Assert.assertEquals(TestDbUtils.ENTITY_TEAM_PLAYER_COUNT + 1, allstar.getTeams().iterator().next().getPlayers().size());
	}
}
