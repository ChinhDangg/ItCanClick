package org.dev;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import org.dev.Menu.ActionMenuController;
import org.dev.Menu.ConditionMenuController;
import org.dev.Operation.ActionController;
import org.dev.Operation.Condition.PixelCondition;
import org.dev.Operation.Condition.TextCondition;
import org.dev.Operation.ConditionController;
import org.dev.Operation.Data.ActionData;
import org.dev.Operation.Data.OperationData;
import org.dev.Operation.TaskController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
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

    public static Pane actionMenuPane;
    public static Pane conditionMenuPane;
    public static StackPane primaryCenterStackPane;
    public static ActionMenuController actionMenuController;
    public static ConditionMenuController conditionMenuController;
    public static double currentGlobalScale = 1.5;

    @Override
    public void start(Stage stage) throws IOException {
        loadConditionMenuPane();
        loadActionMenuPane();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("Operation/operationPane.fxml"));
        VBox taskPane = loader.load();
        Scale scale = new Scale(1.5, 1.5, 0, 0);
        taskPane.getTransforms().add(scale);

        Group group = new Group(taskPane);

//        ScrollPane scrollPane = new ScrollPane(stackPane);
//        scrollPane.fitToWidthProperty().set(true);

        primaryCenterStackPane = new StackPane();
        primaryCenterStackPane.getChildren().add(group);
        primaryCenterStackPane.setAlignment(Pos.TOP_CENTER);

        BorderPane primary = new FXMLLoader(getClass().getResource("primary.fxml")).load();
        primary.setCenter(primaryCenterStackPane);

        Scene scene = new Scene(primary);
        stage.setScene(scene);
        stage.show();
    }

    public static void displayNewNode(Node node) {
        primaryCenterStackPane.getChildren().getFirst().setVisible(false);
        primaryCenterStackPane.getChildren().add(node);
    }
    public static void backToPrevious(Node node) {
        primaryCenterStackPane.getChildren().remove(node);
        primaryCenterStackPane.getChildren().getFirst().setVisible(true);
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