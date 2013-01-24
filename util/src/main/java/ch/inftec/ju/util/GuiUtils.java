package ch.inftec.ju.util;

import java.awt.Component;
import java.awt.Dimension;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Class containing GUI related utility methods.
 * @author tgdmemae
 *
 */
public final class GuiUtils {
	/**
	 * Don't instantiate.
	 */
	private GuiUtils() {
		throw new AssertionError("use only statically");
	}
	
	/**
	 * Gets an XString representation of the model's tree.
	 * @param model TreeModel
	 * @return XString representation of the tree
	 */
	public static XString toXString(TreeModel model) {
		XString xs = new XString();
		
		Object root = model.getRoot();
		if (root != null) {
			GuiUtils.buildXString(xs, model, model.getRoot());
		}
		
		return xs;
	}
	
	/**
	 * Recursively builds an XString representation of a tree.
	 * @param xs XString instance to use
	 * @param model TreeModel
	 * @param node Current node
	 */
	private static void buildXString(XString xs, TreeModel model, Object node) {
		xs.addLine(node);
		
		xs.increaseIndent();
		for (int i = 0; i < model.getChildCount(node); i++) {
			GuiUtils.buildXString(xs, model, model.getChild(node, i));
		}
		xs.decreaseIndent();
	}
	
	/**
	 * Sets the minimum size of a component.
	 * @param comp Component to set minimum size
	 * @param minimumSize Minimum size
	 */
	public static void setMinimumSize(Component comp, Dimension minimumSize) {
		Dimension s = comp.getSize();
		if (s.height < minimumSize.height
				|| s.width < minimumSize.width) {
			comp.setSize(MathUtils.max(s, minimumSize));
		}
	}
	
	/**
	 * Shows the specified frame and sets the appropriate default close operation (ExitOnClose if
	 * the flag is true, otherwise DisposeOnClose).
	 * @param frame Frame to show
	 * @param exitOnClose Flags if default close operation should be set to ExitOnClose
	 */
	public static void showFrame(JFrame frame, boolean exitOnClose) {
		frame.setDefaultCloseOperation(exitOnClose ? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		GuiUtils.setMinimumSize(frame, new Dimension(300, 200));
		
		frame.setVisible(true);
	}

	/**
	 * Loads the icon in the specified resource.
	 * @param resourcePath File name of the icon's resource, relative to the calling class
	 * @return ImageIcon instance
	 * @throws IllegalArgumentException If the icon resource cannot be found
	 */
	public static ImageIcon loadIconResource(String resourcePath) throws IllegalArgumentException {
		return GuiUtils.loadIconResource(resourcePath, ReflectUtils.getCallingClass());
	}

	/**
	 * Loads the icon in the specified resource.
	 * @param resourcePath File name of the icon's resource
	 * @param relativeClass If not null, the path is used relative to this classes package. If null,
	 * the path is used relative to the calling's class' package.
	 * @returns ImageIcon instance
	 * @throws IllegalArgumentException If the icon resource cannot be found
	 */
	public static ImageIcon loadIconResource(String resourcePath, Class<?> relativeClass) {
		if (relativeClass == null) relativeClass = ReflectUtils.getCallingClass();
		
		URL url = IOUtil.getResourceURL(resourcePath, relativeClass);
		
		if (url == null) {
			throw new IllegalArgumentException("Icon resource not found: " + resourcePath + " (relative to " + relativeClass + ")");
		} else {
			ImageIcon icon = new ImageIcon(url);
			return icon;
		}
	}
	
	/**
	 * Fully expands the nodes in the specified tree.
	 * @param tree Tree
	 */
	public static void expandAll(JTree tree) {
		Object root = tree.getModel().getRoot();
		if (root != null) {
			GuiUtils.expandAll(tree, new TreePath(root));
		}
	}
	
	/**
	 * Expands all nodes under the specified TreePath.
	 * This method is used recursively.
	 * @param path TreePath to expand the nodes of
	 */
	private static void expandAll(JTree tree, TreePath path) {
		// Traverse children
		Object parent = (Object)path.getLastPathComponent();
		
		for (int i = 0; i < tree.getModel().getChildCount(parent); i++) {
			Object child = tree.getModel().getChild(parent, i);
			TreePath subPath = path.pathByAddingChild(child);
			GuiUtils.expandAll(tree, subPath);
		}
		
		// Expand the tree bottom-up
		tree.expandPath(path);
	}	
}
