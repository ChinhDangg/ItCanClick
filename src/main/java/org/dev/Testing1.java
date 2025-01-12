package org.dev;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Testing1 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SideMenu/sideMenuLabel.fxml"));
        Node n = fxmlLoader.load();
        VBox vbox = new VBox(n);


        stage.setMinWidth(740.0);
        stage.setScene(new Scene(vbox));
        stage.setOnCloseRequest(_ -> System.exit(0));
        stage.show();
    }
}
