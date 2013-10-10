package ch.inftec.ju.testing.db;

import org.junit.Assume;

import ch.inftec.ju.db.JuEmUtil;
import ch.inftec.ju.db.JuEmUtil.DbType;

/**
 * Util class providing JUnit assume functionality.
 * @author Martin
 *
 */
public class JuAssumeUtil {
	/**
	 * Assumes that the DB represented by JuEmUtil is none of the
	 * DbTypes specified.
	 * @param emUtil JuEmUtil
	 * @param types Types we assume the DB is not
	 */
	public static void dbIsNot(JuEmUtil emUtil, DbType... types) {
		DbType actualType = emUtil.getDbType();
		
		for (DbType type : types) {
			Assume.assumeFalse("Assumed DB was not " + type, type == actualType);
		}
	}
}
