package org.dev;

import javafx.application.Application;
import javafx.stage.Stage;

public class SmartClick extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setMinWidth(740.0);
        stage.setScene(AppScene.getAppMainScene());
        //stage.initStyle(StageStyle.UNDECORATED);
        stage.setOnCloseRequest(_ -> System.exit(0));
        stage.show();
    }
}
