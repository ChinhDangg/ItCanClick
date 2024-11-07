package org.dev;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
        stage.setScene(AppScene.getAppMainScene());
        stage.setOnCloseRequest(_ -> System.exit(0));
        stage.show();
    }
}