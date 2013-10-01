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
	
	/**
	 * Creates a TxHandler wrapper without starting a new transaction.
	 * @param tx Underlying user transaction
	 */
	public TxHandler(UserTransaction tx) {
		this(tx, false);
	}
	
	/**
	 * Creates a new TxHandler wrapper and begins a new transaction
	 * if begin is true.
	 * @param tx UserTransaction
	 * @param begin If true, calls begin on the transaction
	 */
	public TxHandler(UserTransaction tx, boolean begin) {
		this.tx = tx;		
		if (begin) this.begin();
	}
	
	/**
	 * Begins a new transaction.
	 * @throws JuDbException If we cannot begin a transaction
	 */
	public void begin() {
		try {
			if (this.tx != null) tx.begin();
			this.committed = false;
		} catch (Exception ex) {
			throw new JuDbException("Couldn't begin JTA transaction", ex);
		}
	}
	
	/**
	 * Commits the transaction without beginning a new one.
	 */
	public void commit() {
		this.commit(false);
	}
	
	/**
	 * Commits the transaction
	 * @param beginNew If true, begins a new transaction
	 */
	public void commit(boolean beginNew) {
		try {
			if (this.tx != null) tx.commit();
			this.committed = true;
		} catch (Exception ex) {
			throw new JuDbException("Couldn't commit JTA transaction", ex);
		}
		
		if (beginNew) this.begin();
	}
	
	/**
	 * Rolls back the transaction if it hasn't been
	 * committed yet.
	 */
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
