package ch.inftec.ju.fx.control;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

/**
 * Helper class that can be used as a CellFactory callback that returns
 * ImageViews based on Images.
 * <p>
 * In short, this will display images in a table when the date of the property is an image.
 * @author Martin
 *
 */
public class ImageViewCellFactory<S> implements Callback<TableColumn<S, Image>, TableCell<S, Image>> {
	@Override
	public TableCell<S, Image> call(TableColumn<S, Image> col) {
		return new ImageViewCell<S>();
	}
	
	/**
	 * Implementation of a TableCell consisting of an ImageView representation, based on an Image.
	 * @author Martin
	 *
	 * @param <S>
	 */
	private static class ImageViewCell<S> extends TableCell<S, Image> {
		@Override
		protected void updateItem(Image item, boolean empty) {
			this.setAlignment(Pos.BASELINE_CENTER);
			this.setGraphic(new ImageView(item));
		}
	}
}
