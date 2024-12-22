package org.dev;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.dev.Enum.LogLevel;
import org.dev.LeftSideMenu.BottomPaneController;
import org.dev.LeftSideMenu.SideBarController;
import org.dev.Menu.ActionMenuController;
import org.dev.Menu.ConditionMenuController;
import org.dev.Operation.ActionController;
import org.dev.Operation.ConditionController;
import org.dev.Operation.Data.OperationData;
import org.dev.Operation.OperationController;
import org.dev.RunOperation.OperationRunController;
import org.dev.Enum.CurrentTab;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class AppScene {

    public static BorderPane primaryBorderPane;
    public static StackPane primaryLeftStackPane;
    public static StackPane primaryCenterStackPane;
    public static HBox mainCenterHBox; // will contain side menu and main display
    public static StackPane mainDisplayStackPane;
    public static Node currentDisplayNode;

    public static MenuBarController menuBarController;
    public static SideBarController sideBarController;
    public static BottomPaneController bottomPaneController;
    public static ActionMenuController actionMenuController;
    public static ConditionMenuController conditionMenuController;
    public static OperationController currentLoadedOperationController;
    public static OperationRunController currentLoadedOperationRunController;

    public static double currentGlobalScale = 1.5;
    public static boolean isOperationRunning = false;

    public static Scene getAppMainScene() throws IOException {
        FXMLLoader bottomPaneLoader = new FXMLLoader(AppScene.class.getResource("LeftSideMenu/bottomPane.fxml"));
        Node bottomPane = bottomPaneLoader.load();
        bottomPaneController = bottomPaneLoader.getController();

        loadConditionMenuPane();
        loadActionMenuPane();

        primaryCenterStackPane = new StackPane();

        FXMLLoader sideBarLoader = new FXMLLoader(AppScene.class.getResource("LeftSideMenu/sideBarPane.fxml"));
        primaryLeftStackPane = sideBarLoader.load();
        sideBarController = sideBarLoader.getController();

        FXMLLoader sideMenuLoader = new FXMLLoader(AppScene.class.getResource("LeftSideMenu/sideMenuPane.fxml"));
        Node sideMenuNodeContent = sideMenuLoader.load();
        sideBarController.setSideMenuController(sideMenuLoader.getController());

        mainDisplayStackPane = new StackPane();
        mainCenterHBox = new HBox();
        HBox.setHgrow(mainDisplayStackPane, Priority.ALWAYS);
        mainCenterHBox.getChildren().addAll(sideMenuNodeContent, mainDisplayStackPane);

        FXMLLoader primaryLoader = new FXMLLoader(AppScene.class.getResource("primary.fxml"));
        primaryBorderPane = primaryLoader.load();
        menuBarController = primaryLoader.getController();

        StackPane.setAlignment(mainCenterHBox, Pos.TOP_CENTER);
        StackPane.setAlignment(bottomPane, Pos.BOTTOM_CENTER);
        primaryCenterStackPane.getChildren().addAll(mainCenterHBox, bottomPane);
        primaryBorderPane.setCenter(primaryCenterStackPane);
        primaryBorderPane.setLeft(primaryLeftStackPane);
        primaryBorderPane.setOnMouseClicked(_ -> primaryBorderPane.requestFocus());

        sideBarController.switchTab(CurrentTab.Operation);

        return new Scene(primaryBorderPane);
    }

    // ------------------------------------------------------
    public static void addLog(LogLevel logLevel, String className, String content) {
        bottomPaneController.addToLog(logLevel, className, content);
    }

    // ------------------------------------------------------
    public static void setIsOperationRunning(boolean isRunning) {
        isOperationRunning = isRunning;
        menuBarController.setOperationRunning(isRunning);
    }

    public static void startOperationRun() {
        if (currentLoadedOperationController == null) {
            System.out.println("No operation found");
            return;
        }
        System.out.println("Starting operation");
        currentLoadedOperationRunController.startOperation(currentLoadedOperationController);
    }

    public static void stopOperationRun() {
        System.out.println("Stopping operation");
        currentLoadedOperationRunController.stopOperation();
    }

    public static boolean loadAndDisplayOperationRun() {
        if (isOperationRunning) {
            System.out.println("Operation already running");
            return false;
        }
        if (currentLoadedOperationController == null) {
            System.out.println("No operation found");
            return false;
        }
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("RunOperation/operationRunPane.fxml"));
            loader.load();
            currentLoadedOperationRunController = loader.getController();
            displayCurrentOperationRun();
            System.out.println("Loaded operation run");
            return true;
        } catch (IOException e) {
            System.out.println("Fail to load Operation Run Pane");
            return false;
        }
    }

    public static boolean displayCurrentOperationRun() {
        if (currentLoadedOperationRunController == null)
            return false;
        displayNewCenterNode(currentLoadedOperationRunController.getOperationRunMainGroup());
        return true;
    }

    // ------------------------------------------------------
    public static void updateOperationSideMenuHierarchy() {
        if (currentLoadedOperationController != null)
            sideBarController.loadSideHierarchy(currentLoadedOperationController);
    }

    public static void updateOperationRunSideMenuHierarchy() {
        if (currentLoadedOperationRunController != null)
            sideBarController.loadRunSideHierarchy(currentLoadedOperationRunController);
    }

    // ------------------------------------------------------
    public static void displayNewCenterNode(Node node) {
        if (currentLoadedOperationController == null)
            return;
        currentLoadedOperationController.setVisible(false);
        if (currentDisplayNode != null)
            mainDisplayStackPane.getChildren().remove(currentDisplayNode);
        currentDisplayNode = node;
        mainDisplayStackPane.getChildren().add(currentDisplayNode);
    }
    public static void backToOperationScene() {
        if (currentLoadedOperationController == null)
            return;
        if (currentDisplayNode != null)
            mainDisplayStackPane.getChildren().remove(currentDisplayNode);
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
    public static void loadEmptyOperation() {
        if (currentLoadedOperationController != null && !currentLoadedOperationController.isSet()) {
            System.out.println("Empty operation already loaded");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(AppScene.class.getResource("Operation/operationPane.fxml"));
            mainDisplayStackPane.getChildren().clear();
            mainDisplayStackPane.getChildren().add(loader.load());
            currentLoadedOperationController = loader.getController();
            System.out.println("New empty operation pane added");
        } catch (IOException e) {
            System.out.println("Fail loading empty operation pane");
        }
    }

    public static void loadSavedOperation(String path) {
        try (FileInputStream fileIn = new FileInputStream(path);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            OperationData operationData = (OperationData) in.readObject();
            System.out.println("Passed getting data");

            FXMLLoader loader = new FXMLLoader(AppScene.class.getResource("Operation/operationPane.fxml"));
            Node operationPane = loader.load();
            System.out.println("Passed loading operation pane");

            currentLoadedOperationController = loader.getController();
            System.out.println("Passed assigning controller");

            mainDisplayStackPane.getChildren().clear();
            System.out.println("Passed removing all children in primary stack pane");

            currentLoadedOperationController.loadSavedOperationData(operationData);
            System.out.println("Passed loading saved data into current operation controller");

            mainDisplayStackPane.getChildren().add(operationPane);
            System.out.println("Passed adding operation pane to primary stack pane");

        } catch (IOException | ClassNotFoundException i) {
            System.out.println("Fail loading saved operation data");
        }
    }

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
}
