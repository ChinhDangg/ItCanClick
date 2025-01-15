package org.dev;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Testing1 extends Application {
    private double xOffset = 0;
    private double yOffset = 0;
    private double xResizeOffset = 0;
    private double yResizeOffset = 0;

    private final double RESIZE_MARGIN = 10;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        BorderPane root = new BorderPane();

        // Custom title bar
        HBox titleBar = new HBox();
        titleBar.setStyle("-fx-background-color: #444444; -fx-padding: 5;");
        Button closeButton = new Button("X");
        closeButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        closeButton.setOnAction(e -> primaryStage.close());
        titleBar.getChildren().add(closeButton);

        // Enable dragging
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        titleBar.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });

        // Resizable content
        StackPane content = new StackPane();
        content.setStyle("-fx-background-color: lightgray;");
        content.getChildren().add(new Button("Resizable Window"));

        root.setTop(titleBar);
        root.setCenter(content);

        Scene scene = new Scene(root, 400, 300);
        scene.setFill(null);

        // Add resizable functionality
        scene.setOnMouseMoved(event -> {
            double x = event.getSceneX();
            double y = event.getSceneY();
            double width = primaryStage.getWidth();
            double height = primaryStage.getHeight();

            if (x > width - RESIZE_MARGIN && y > height - RESIZE_MARGIN) {
                scene.setCursor(javafx.scene.Cursor.SE_RESIZE);
            } else if (x > width - RESIZE_MARGIN) {
                scene.setCursor(javafx.scene.Cursor.E_RESIZE);
            } else if (y > height - RESIZE_MARGIN) {
                scene.setCursor(javafx.scene.Cursor.S_RESIZE);
            } else {
                scene.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });

        scene.setOnMouseDragged(event -> {
            if (scene.getCursor() == javafx.scene.Cursor.SE_RESIZE) {
                primaryStage.setWidth(event.getSceneX());
                primaryStage.setHeight(event.getSceneY());
            } else if (scene.getCursor() == javafx.scene.Cursor.E_RESIZE) {
                primaryStage.setWidth(event.getSceneX());
            } else if (scene.getCursor() == javafx.scene.Cursor.S_RESIZE) {
                primaryStage.setHeight(event.getSceneY());
            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

