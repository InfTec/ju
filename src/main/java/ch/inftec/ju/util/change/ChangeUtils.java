package ch.inftec.ju.util.change;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class for functinality of the util.change package.
 * @author tgdmemae
 *
 */
public final class ChangeUtils {
	
	/**
	 * Abstract base class for implementations of the ChangeItem interface.
	 * @author tgdmemae
	 *
	 */
	public abstract static class AbstractChangeItem implements ChangeItem {
		private List<ChangeItem> childItems = new ArrayList<ChangeItem>();
		
		@Override
		public final List<ChangeItem> getChildItems() {
			return Collections.unmodifiableList(this.childItems);
		}
	}
}
