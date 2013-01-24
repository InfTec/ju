package ch.inftec.ju.util.change;

import java.util.List;

import ch.inftec.ju.util.general.Descriptor;

/**
 * Interface for change items.
 * <p>
 * Change items provide an appropriate handler that can be used to execute the
 * item and to perform other actions on it, like creating an undo item.
 * <p>
 * Change items can contain other child items, thus building a tree.
 * 
 * @author Martin
 *
 */
public interface ChangeItem {
	/**
	 * Gets a Descriptor describing the change item.
	 * @return Descriptor
	 */
	public Descriptor getDescriptor();
	
	/**
	 * Gets the child items of this item.
	 * @return Child items
	 */
	public List<ChangeItem> getChildItems();
	
	/**
	 * Gets a ChangeSetHandler instance that can be used to work with the ChangeSet.
	 * <p>
	 * Non-root items don't have to return a handler. They can return null if
	 * they wish to.
	 * @return ChangeSetHandler ChangeItemHandler instance or null if this item doesn't provide
	 * a handler
	 */
	public ChangeItemHandler getHandler();
	
	/**
	 * Contains methods to handle ChangeItems.
	 * 
	 * @author tgdmemae
	 *
	 */
	public interface ChangeItemHandler {
		/**
		 * Creates the undoItem for the item associated with this handler.
		 * @return
		 */
		public ChangeItem createUndoItem();
		
		/**
		 * Executes the ChangeItem associated with this handler.
		 */
		public void execute();
	}
}
