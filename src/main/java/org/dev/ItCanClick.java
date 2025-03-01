package org.dev;

import javafx.application.Application;
import javafx.stage.Stage;
import org.dev.SideMenu.TopMenu.WindowSizeMode;

public class ItCanClick extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setMinWidth(750.0);
        stage.setScene(AppScene.getAppMainScene());

        WindowSizeMode mode = AppScene.windowSizeMode;
        if (mode == WindowSizeMode.Maximized)
            stage.setMaximized(true);
        else if (mode == WindowSizeMode.Compact) {
            stage.setWidth(750);
            stage.setHeight(400);
        }
        else if (mode == WindowSizeMode.Default) {
            stage.setWidth(1200);
            stage.setHeight(700);
        }

        stage.setOnCloseRequest(_ -> System.exit(0));
        stage.show();
    }
}
