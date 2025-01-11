package org.dev;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class Testing2 extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create a VBox to act as a custom menu
        VBox customMenu = new VBox();
        customMenu.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-padding: 5;");

        // Add menu items
        Label item1 = new Label("Option 1");
        Label item2 = new Label("Option 2");
        Label item3 = new Label("Option 3");

        customMenu.getChildren().addAll(item1, item2, item3);

        // Add hover effects
        for (Label item : new Label[]{item1, item2, item3}) {
            item.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-text-fill: black;");
            item.setOnMouseEntered(e -> item.setStyle("-fx-padding: 10; -fx-background-color: lightblue; -fx-text-fill: black;"));
            item.setOnMouseExited(e -> item.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-text-fill: black;"));
            item.setOnMouseClicked(e -> System.out.println(item.getText() + " clicked"));
        }

        // Create a Popup to display the menu
        Popup popup = new Popup();
        popup.getContent().add(customMenu);

        // Tie the popup to the main stage
        popup.setAutoHide(true); // Optional: Automatically hide on click outside

        // Show the popup on right-click
        StackPane root = new StackPane();
        root.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                popup.show(primaryStage, event.getScreenX(), event.getScreenY());
            } //else {
//                popup.hide();
//            }
        });

        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.setTitle("Popup Lifecycle Fix");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
