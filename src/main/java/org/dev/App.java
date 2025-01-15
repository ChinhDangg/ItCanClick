package org.dev;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;

/**
 * JavaFX App
 */
public class App extends Application {

    public static void main(String[] args) {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setMinWidth(740.0);
        stage.setScene(AppScene.getAppMainScene());
        //stage.initStyle(StageStyle.UNDECORATED);
        stage.setOnCloseRequest(_ -> System.exit(0));
        stage.show();
    }
}