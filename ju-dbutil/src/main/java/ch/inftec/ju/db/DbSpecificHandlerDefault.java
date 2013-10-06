package ch.inftec.ju.db;

import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of DbSpecificHandler. DB specific handlers can extend this class
 * and only have to override a method if the DB doesn't comply with the default handling.
 * @author Martin
 *
 */
public abstract class DbSpecificHandlerDefault implements DbSpecificHandler {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	protected final JuEmUtil emUtil;
	protected final EntityManager em;
	
	protected DbSpecificHandlerDefault(JuEmUtil emUtil, EntityManager em) {
		this.emUtil = emUtil;
		this.em = em;
	}
	
	@Override
	public String convertTableNameCasing(String tableName) {
		return tableName.toUpperCase();
	}
	
	@Override
	public List<String> getSequenceNames() {
		throw new JuDbException("Sequences not supported by " + this.emUtil.getDbType());
	}
	
	@Override
	public Long getNextValueFromSequence(String sequenceName) {
		throw new JuDbException("Sequences not supported by " + this.emUtil.getDbType());
	}
}
