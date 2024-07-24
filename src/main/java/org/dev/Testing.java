package org.dev;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Testing extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {



        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setVvalue(1.00);
        scrollPane.setPrefHeight(10);
        VBox vBox = new VBox();
        Label label1 = new Label("Hello World1");
        Label label2 = new Label("Hello World2");
        Label label3 = new Label("Hello World3");
        Label label4 = new Label("Hello World4");
        Label label5 = new Label("Hello World5");
        vBox.getChildren().addAll(label1, label2, label3, label4, label5);
        scrollPane.setContent(vBox);

        Scene scene = new Scene(scrollPane);
        stage.setScene(scene);
        stage.show();

        Thread.sleep(5000);
        scrollPane.setVisible(false);
        Thread.sleep(2000);
        scrollPane.setVisible(true);
    }
}
