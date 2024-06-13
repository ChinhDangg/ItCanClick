package org.dev;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import org.dev.Menu.ActionMenuController;
import org.dev.Menu.ConditionMenuController;
import org.dev.Task.ActionController;
import org.dev.Task.ConditionController;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

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

    public static Pane actionMenuPane;
    public static Pane conditionMenuPane;
    public static StackPane primaryCenterStackPane;
    public static ActionMenuController actionMenuController;
    public static ConditionMenuController conditionMenuController;

    @Override
    public void start(Stage stage) throws IOException {
        loadConditionMenuPane();
        loadActionMenuPane();

//        VBox vBox = new VBox(actionPane); //will be a task controller to hold all action controllers
//        Scale scale = new Scale(1.5, 1.5, 0, 0);
//        vBox.getTransforms().add(scale);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Task/taskPane.fxml"));
        VBox taskPane = loader.load();
        Scale scale = new Scale(1.5, 1.5, 0, 0);
        taskPane.getTransforms().add(scale);

        Group group = new Group(taskPane);
        StackPane stackPane = new StackPane(group);
        stackPane.setAlignment(Pos.TOP_CENTER);

//        ScrollPane scrollPane = new ScrollPane(stackPane);
//        scrollPane.fitToWidthProperty().set(true);

        primaryCenterStackPane = new StackPane(stackPane);

        BorderPane primary = new FXMLLoader(getClass().getResource("primary.fxml")).load();
        primary.setCenter(primaryCenterStackPane);

        Scene scene = new Scene(primary);
        stage.setScene(scene);
        stage.show();
    }

    public static void openConditionMenuPane(ConditionController conditionController) {
        conditionMenuController.loadMenu(conditionController);
        primaryCenterStackPane.getChildren().add(conditionMenuPane);
    }
    public static void closeConditionMenuPane() {
        primaryCenterStackPane.getChildren().remove(conditionMenuPane);
    }
    public static void openActionMenuPane(ActionController actionController) {
        actionMenuController.loadMenu(actionController);
        primaryCenterStackPane.getChildren().add(actionMenuPane);
    }
    public static void closeActionMenuPane() { primaryCenterStackPane.getChildren().remove(actionMenuPane); }

    private void loadActionMenuPane() {
        System.out.println("Loading Action Menu Pane");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu/actionMenuPane.fxml"));
            actionMenuPane = loader.load();
            actionMenuController = loader.getController();
        } catch (IOException e) {
            System.out.println("Error loading action menu pane");
        }
    }
    private void loadConditionMenuPane() {
        System.out.println("Loading Condition Menu Pane");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu/conditionMenuPane.fxml"));
            conditionMenuPane = loader.load();
            conditionMenuController = loader.getController();
        } catch (IOException e) {
            System.out.println("Error loading condition menu pane");
        }
    }
}