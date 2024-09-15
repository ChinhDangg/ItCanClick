package org.dev;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.dev.Menu.ActionMenuController;
import org.dev.Menu.ConditionMenuController;
import org.dev.Operation.ActionController;
import org.dev.Operation.ConditionController;
import org.dev.Operation.Data.OperationData;
import org.dev.Operation.OperationController;
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

    public static BorderPane primaryBorderPane;
    public static StackPane primaryCenterStackPane;
    public static StackPane primaryLeftStackPane;
    public static Node currentDisplayNode;

    public static MenuBarController menuBarController;
    public static SideMenuController sideMenuController;
    public static ActionMenuController actionMenuController;
    public static ConditionMenuController conditionMenuController;
    public static OperationController currentLoadedOperationController;

    public static double currentGlobalScale = 1.5;
    public static boolean isOperationRunning = false;

    @Override
    public void start(Stage stage) throws IOException {

        loadConditionMenuPane();
        loadActionMenuPane();

        primaryCenterStackPane = new StackPane();
        primaryCenterStackPane.setAlignment(Pos.TOP_CENTER);
        primaryCenterStackPane.setOnMouseClicked(_ -> primaryCenterStackPane.requestFocus());

        FXMLLoader sideMenuLoader = new FXMLLoader(getClass().getResource("sideMenuPane.fxml"));
        primaryLeftStackPane = sideMenuLoader.load();
        sideMenuController = sideMenuLoader.getController();

        FXMLLoader primaryLoader = new FXMLLoader(getClass().getResource("primary.fxml"));
        primaryBorderPane = primaryLoader.load();
        menuBarController = primaryLoader.getController();

        primaryBorderPane.setCenter(primaryCenterStackPane);
        primaryBorderPane.setLeft(primaryLeftStackPane);

        Scene scene = new Scene(primaryBorderPane);
        stage.setScene(scene);
        stage.setOnCloseRequest(_ -> System.exit(0));
        stage.show();
    }

    public static void loadNewEmptyOperation() throws IOException {
        if (currentLoadedOperationController != null && !currentLoadedOperationController.isSet()) {
            System.out.println("Empty operation already loaded");
            return;
        }
        FXMLLoader loader = new FXMLLoader(App.class.getResource("Operation/operationPane.fxml"));
        Group operationPane = loader.load();
        currentLoadedOperationController = loader.getController();
        primaryCenterStackPane.getChildren().clear();
        primaryCenterStackPane.getChildren().add(operationPane);
        System.out.println("New empty operation pane added");
    }

    public static void loadSideMenuHierarchy() {
        if (currentLoadedOperationController != null)
            sideMenuController.loadSideHierarchy(currentLoadedOperationController);
    }

    public static void loadSavedOperation(String path) {
        try (FileInputStream fileIn = new FileInputStream(path);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
             OperationData operationData = (OperationData) in.readObject();
             System.out.println("Passed getting data");

             FXMLLoader loader = new FXMLLoader(App.class.getResource("Operation/operationPane.fxml"));
             Group operationPane = loader.load();
             System.out.println("Passed loading operation pane");

             currentLoadedOperationController = loader.getController();
             System.out.println("Passed assigning controller");

             ObservableList<Node> children = primaryCenterStackPane.getChildren();
             primaryCenterStackPane.getChildren().clear();
             System.out.println("Passed removing all children in primary stack pane");

             currentLoadedOperationController.loadSavedOperationData(operationData);
             System.out.println("Passed loading saved data into current operation controller");

             children.add(operationPane);
             System.out.println("Passed adding operation pane to primary stack pane");

        } catch (IOException | ClassNotFoundException i) {
            System.out.println("Fail loading saved operation data");
        }
    }


    public static void displayNewNode(Node node) {
        currentLoadedOperationController.setVisible(false);
        if (currentDisplayNode != null)
            primaryCenterStackPane.getChildren().remove(currentDisplayNode);
        primaryCenterStackPane.getChildren().add(node);
        currentDisplayNode = node;
    }
    public static void backToPrevious() {
        if (currentDisplayNode != null)
            primaryCenterStackPane.getChildren().remove(currentDisplayNode);
        currentLoadedOperationController.setVisible(true);
    }

    public static void openConditionMenuPane(ConditionController conditionController) {
        conditionMenuController.loadMenu(conditionController);
        primaryCenterStackPane.getChildren().add(conditionMenuController.getMainMenuStackPane());
    }
    public static void closeConditionMenuPane() {
        primaryCenterStackPane.getChildren().remove(conditionMenuController.getMainMenuStackPane());
    }
    public static void openActionMenuPane(ActionController actionController) {
        actionMenuController.loadMenu(actionController);
        primaryCenterStackPane.getChildren().add(actionMenuController.getMainMenuStackPane());
    }
    public static void closeActionMenuPane() {
        primaryCenterStackPane.getChildren().remove(actionMenuController.getMainMenuStackPane()); }


    private void loadActionMenuPane() {
        System.out.println("Loading Action Menu Pane");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu/actionMenuPane.fxml"));
            loader.load();
            actionMenuController = loader.getController();
        } catch (IOException e) {
            System.out.println("Error loading action menu pane");
        }
    }
    private void loadConditionMenuPane() {
        System.out.println("Loading Condition Menu Pane");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu/conditionMenuPane.fxml"));
            loader.load();
            conditionMenuController = loader.getController();
        } catch (IOException e) {
            System.out.println("Error loading condition menu pane");
        }
    }
}