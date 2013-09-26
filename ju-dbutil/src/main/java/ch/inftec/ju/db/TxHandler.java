package ch.inftec.ju.db;

import javax.transaction.UserTransaction;

/**
 * Helper wrapper around a UserTransaction to help coping with optional
 * transaction object (null) and exceptions.
 * @author Martin
 *
 */
public final class TxHandler implements AutoCloseable {
	private final UserTransaction tx;
	private boolean committed = true;
	
	public TxHandler(UserTransaction tx) {
		this.tx = tx;
	}
	
	public void begin() {
		try {
			if (this.tx != null) tx.begin();
			this.committed = false;
		} catch (Exception ex) {
			throw new JuDbException("Couldn't begin JTA transaction", ex);
		}
	}
	
	public void commit() {
		try {
			if (this.tx != null) tx.commit();
			this.committed = true;
		} catch (Exception ex) {
			throw new JuDbException("Couldn't commit JTA transaction", ex);
		}
	}
	
	public void rollbackIfNotCommitted() {
		if (!this.committed && this.tx != null) {
			try {
				this.tx.rollback();
				this.committed = true;
			} catch (Exception ex) {
				throw new JuDbException("Couldn't rollback transaction", ex);
			}
		}
	}
	
	@Override
	public void close() {
		this.rollbackIfNotCommitted();
	}
}
