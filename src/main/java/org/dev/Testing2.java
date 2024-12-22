package org.dev;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Testing2 extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create a Pane
        Pane resizablePane = new Pane();
        resizablePane.setStyle("-fx-background-color: lightblue;");
        resizablePane.setPrefSize(200, 100); // Initial size

        // Create a resize handle (e.g., a small rectangle at the bottom edge)
        Rectangle resizeHandle = new Rectangle(200, 50);
        resizeHandle.setFill(Color.DARKGRAY);
        resizeHandle.setCursor(javafx.scene.Cursor.S_RESIZE);
        resizeHandle.setLayoutY(resizablePane.getPrefHeight() - resizeHandle.getHeight());

        // Attach resize behavior to the handle
        resizeHandle.setOnMouseDragged(event -> {
            double newHeight = event.getY();
            if (newHeight > 50) { // Minimum height constraint
                resizablePane.setPrefHeight(newHeight);
                resizeHandle.setLayoutY(newHeight - resizeHandle.getHeight());
            }
        });

        // Add the resize handle to the pane
        resizablePane.getChildren().add(resizeHandle);

        // Set up the Scene and Stage
        Scene scene = new Scene(resizablePane, 400, 300);
        primaryStage.setTitle("Draggable Resizable Pane");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
