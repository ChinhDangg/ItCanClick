package org.dev;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.dev.Menu.ActionMenuController;
import org.dev.Menu.ConditionMenuController;
import org.dev.Operation.ActionController;
import org.dev.Operation.ConditionController;
import org.dev.Operation.Data.OperationData;
import org.dev.Operation.OperationController;
import org.dev.RunOperation.OperationRunController;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class AppScene {

    public static BorderPane primaryBorderPane;
    public static StackPane primaryCenterStackPane;
    public static StackPane primaryLeftStackPane;
    public static Node currentDisplayNode;

    public static MenuBarController menuBarController;
    public static SideMenuController sideMenuController;
    public static ActionMenuController actionMenuController;
    public static ConditionMenuController conditionMenuController;
    public static OperationController currentLoadedOperationController;
    public static OperationRunController currentLoadedOperationRunController;

    public static double currentGlobalScale = 1.5;
    public static boolean isOperationRunning = false;

    public static Scene getAppMainScene() throws IOException {
        loadConditionMenuPane();
        loadActionMenuPane();

        primaryCenterStackPane = new StackPane();
        primaryCenterStackPane.setAlignment(Pos.TOP_CENTER);
        primaryCenterStackPane.setOnMouseClicked(_ -> primaryCenterStackPane.requestFocus());

        FXMLLoader sideMenuLoader = new FXMLLoader(AppScene.class.getResource("sideMenuPane.fxml"));
        primaryLeftStackPane = sideMenuLoader.load();
        sideMenuController = sideMenuLoader.getController();

        FXMLLoader primaryLoader = new FXMLLoader(AppScene.class.getResource("primary.fxml"));
        primaryBorderPane = primaryLoader.load();
        menuBarController = primaryLoader.getController();

        primaryBorderPane.setCenter(primaryCenterStackPane);
        primaryBorderPane.setLeft(primaryLeftStackPane);

        return new Scene(primaryBorderPane);
    }

    // ------------------------------------------------------
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

            primaryCenterStackPane.getChildren().clear();
            System.out.println("Passed removing all children in primary stack pane");

            currentLoadedOperationController.loadSavedOperationData(operationData);
            System.out.println("Passed loading saved data into current operation controller");

            primaryCenterStackPane.getChildren().add(operationPane);
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
    public static void backToOperationScene() {
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
        primaryCenterStackPane.getChildren().remove(actionMenuController.getMainMenuStackPane());
    }

    // ------------------------------------------------------
    private static void loadActionMenuPane() {
        System.out.println("Loading Action Menu Pane");
        try {
            FXMLLoader loader = new FXMLLoader(AppScene.class.getResource("Menu/actionMenuPane.fxml"));
            loader.load();
            actionMenuController = loader.getController();
        } catch (IOException e) {
            System.out.println("Error loading action menu pane");
        }
    }
    private static void loadConditionMenuPane() {
        System.out.println("Loading Condition Menu Pane");
        try {
            FXMLLoader loader = new FXMLLoader(AppScene.class.getResource("Menu/conditionMenuPane.fxml"));
            loader.load();
            conditionMenuController = loader.getController();
        } catch (IOException e) {
            System.out.println("Error loading condition menu pane");
        }
    }

    public static void Test() {
        System.out.println("hello");
    }

}
