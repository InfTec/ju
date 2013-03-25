package ch.inftec.ju.fx.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;

import org.junit.Test;

import ch.inftec.ju.fx.ImageLoader;
import ch.inftec.ju.util.fx.JuFxUtils;

public class TableViewTestGui {
	@Test
	public void tableView() {
		ObservableList<TestData> data = FXCollections.observableArrayList();
		
		data.add(new TestData("Test1", ImageLoader.getDefaultLoader().loadImage("information.png")));
		data.add(new TestData(null, null));
		data.add(new TestData("Test2", ImageLoader.getDefaultLoader().loadImage("information.png")));
		
		TableView<TestData> tableView = new TableView<>(data);
		TableColumn<TestData, String> colText = new TableColumn<TestData, String>("Text");
		TableColumn<TestData, Image> colImage = new TableColumn<TestData, Image>("Image");
		
		tableView.getColumns().add(colText);
		tableView.getColumns().add(colImage);
		
		colText.setCellValueFactory(new PropertyValueFactory<TestData, String>("text"));
		
		colImage.setCellValueFactory(new PropertyValueFactory<TestData, Image>("image"));
		colImage.setCellFactory(new ImageViewCellFactory<TestData>());
		
		JuFxUtils.startApplication()
			.node(tableView)
			.start();
	}
	
	public static class TestData {
		private final String text;
		private final Image image;
		
		public TestData(String text, Image image) {
			this.text = text;
			this.image = image;
		}
		
		public String getText() {
			return text;
		}
		
		public Image getImage() {
			return image;
		}
	}
}
