package org.dev;

import javafx.application.Application;
import javafx.stage.Stage;
import org.dev.SideMenu.TopMenu.WindowSizeMode;

public class SmartClick extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setMinWidth(750.0);
        stage.setScene(AppScene.getAppMainScene());

        WindowSizeMode mode = AppScene.windowSizeMode;
        if (mode == WindowSizeMode.Maximized)
            stage.setMaximized(true);
        else if (mode == WindowSizeMode.Compact) {
            stage.setWidth(400);
            stage.setHeight(750);
        }
        else if (mode == WindowSizeMode.Default) {
            stage.setWidth(700);
            stage.setHeight(1200);
        }

        stage.setOnCloseRequest(_ -> System.exit(0));
        stage.show();
    }
}
