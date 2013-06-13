package ch.inftec.ju.fx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.junit.Test;

import ch.inftec.ju.util.JuStringUtils;
import ch.inftec.ju.util.fx.JuFxUtils;

public class DialogTestGui {
	@Test
	public void dialog() {
		JuFxUtils.startApplication()
			.button("Show dialog", new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent ev) {
					FlowPane flowPane = new FlowPane();
					flowPane.getChildren().add(new Text("Hello There"));
					Button btn = new Button("Buhu");
					btn.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent ev) {
							Button btn = (Button)ev.getSource();
							JuFxUtils.closeWindow(btn);
						}
					});
					
					flowPane.getChildren().add(btn);
					flowPane.setPrefHeight(100);
					
					Scene scene = new Scene(flowPane);
					
					Stage stage = new Stage(StageStyle.UTILITY);
					stage.setTitle("Hello there");
					stage.initModality(Modality.APPLICATION_MODAL);
					stage.setScene(scene);
					stage.sizeToScene();
					stage.showAndWait();
				}
			})
			.start();
	}
	
	@Test
	public void detailMessageDialog() {
		JuFxUtils.startApplication()
		.button("Show detail dialog", new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent ev) {
				DetailMessageViewModel model = new DetailMessageViewModel();
				model.titleProperty().set("huhu");
				model.messageProperty().set("Test Message");
				model.detailedMessageProperty().set("Detailed message...\nRather short...");
				
				JuFxUtils.showDetailMessageDialog(model, (Node)ev.getSource());
			}
		})
		.button("Show detail dialog long message", new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent ev) {
				String message = JuStringUtils.createLoremIpsum().getWords(200);
				String detailMessage = JuStringUtils.createLoremIpsum().getParagraphs(10);
				
				DetailMessageViewModel model = new DetailMessageViewModel();
				model.titleProperty().set("Looooong");
				model.messageProperty().set(message);
				model.detailedMessageProperty().set(detailMessage);
				
				JuFxUtils.showDetailMessageDialog(model, (Node)ev.getSource());
			}
		})
		.button("Show detail dialog long detail message", new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent ev) {
				String message = JuStringUtils.createLoremIpsum().getWords(2);
				String detailMessage = JuStringUtils.createLoremIpsum().getWords(500);
				detailMessage += JuStringUtils.createLoremIpsum().getParagraphs(10);
				
				DetailMessageViewModel model = new DetailMessageViewModel();
				model.titleProperty().set("Looooong");
				model.messageProperty().set(message);
				model.detailedMessageProperty().set(detailMessage);
				
				JuFxUtils.showDetailMessageDialog(model, (Node)ev.getSource());
			}
		})
		.start();
	}
}
