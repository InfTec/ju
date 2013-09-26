package ch.inftec.ju.db;

import java.util.List;

import javax.persistence.EntityManager;

/**
 * DbSpecificHandler implementations for H2.
 * @author Martin
 *
 */
public class DbSpecificHandlerOracle extends DbSpecificHandlerDefault {
	public DbSpecificHandlerOracle(JuEmUtil emUtil, EntityManager em) {
		super(emUtil, em);
	}
	
	@Override
	public List<String> getSequenceNames() {
		@SuppressWarnings("unchecked")
		List<String> results = (List<String>) this.em.createNativeQuery(
				"select SEQUENCE_NAME from USER_SEQUENCES").getResultList();
		
		return results;
	}
	
	@Override
	public void resetIdentityGenerationOrSequences(int val) {
		for (String sequence : this.getSequenceNames()) {
			// We'll just drop and recreate the sequence.
			this.em.createNativeQuery("drop sequence " + sequence).executeUpdate();
			this.em.createNativeQuery(String.format("create sequence %s start with %d", sequence, val)).executeUpdate();
//			this.oracleSequenceSetNextVal(sequence, val);
		}
	}
}
