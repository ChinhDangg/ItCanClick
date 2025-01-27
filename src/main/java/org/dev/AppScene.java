package org.dev;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import org.dev.Enum.LogLevel;
import org.dev.SideMenu.*;
import org.dev.Menu.ActionMenuController;
import org.dev.Menu.ConditionMenuController;
import org.dev.JobController.ActionController;
import org.dev.JobController.ConditionController;
import org.dev.JobData.OperationData;
import org.dev.JobController.OperationController;
import org.dev.Enum.CurrentTab;
import org.dev.SideMenu.LeftMenu.SideBarController;
import org.dev.SideMenu.TopMenu.MenuBarController;
import org.dev.SideMenu.TopMenu.SettingMenuController;
import org.dev.SideMenu.TopMenu.WindowSizeMode;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Objects;

public class AppScene {

    private static final String className = AppScene.class.getSimpleName();

    public static BorderPane primaryBorderPane = new BorderPane();
    public static StackPane primaryCenterStackPane = new StackPane();
    public static VBox mainCenterVBox = new VBox(); // will contain the main HBox and bottom pane
    public static HBox mainCenterHBox = new HBox(); // will contain side menu and main display
    public static StackPane mainDisplayStackPane = new StackPane();
    public static StackPane mainNotificationStackPane = new StackPane();

    public static MenuBarController menuBarController;
    public static SettingMenuController settingMenuController;
    public static SideBarController sideBarController;
    public static BottomPaneController bottomPaneController;
    public static CenterBannerController centerBannerController;
    public static TopNotificationController topNotificationController;
    public static ActionMenuController actionMenuController;
    public static ConditionMenuController conditionMenuController;
    public static OperationController currentLoadedOperationController;

    public static double currentGlobalScale = 1.3;
    public static boolean isRunning = false;
    public static boolean isMaximized = false;

    /*
    Layout:
    primaryBorderPane
    Top: top menu bar
    Left: Left Side bar
    Center: primaryCenterStackPane
        mainCenterVBox
            mainCenterHBox
                sideMenu (for sidebar)
                mainDisplayStackPane (operation, task, action, menu (action, condition), and run operation (task, action, condition)
            bottomPane
        mainNotificationStackPane
            topNotificationBanner
            CenterBanner
     */

    public static Scene getAppMainScene() {
        VBox.setVgrow(mainCenterHBox, Priority.ALWAYS);
        mainCenterVBox.getChildren().add(mainCenterHBox);
        mainCenterVBox.getChildren().add(loadBottomPane());
        primaryCenterStackPane.getChildren().add(mainCenterVBox);
        primaryCenterStackPane.getChildren().add(mainNotificationStackPane);

        loadSettingPane();
        bottomPaneController.setDebug(settingMenuController.isDebug());
        bottomPaneController.setTrace(settingMenuController.isTrace());
        currentGlobalScale = settingMenuController.getGlobalScaleValue();

        mainNotificationStackPane.getChildren().add(loadTopNotificationBannerPane());
        mainNotificationStackPane.getChildren().add(loadCenterBannerPane());
        mainNotificationStackPane.setMouseTransparent(true);

        setPrimaryBorderPaneSize(settingMenuController.getWindowSizeMode());
        primaryBorderPane.getStylesheets().add(Objects.requireNonNull(AppScene.class.getResource("/styles/root.css")).toExternalForm());
        primaryBorderPane.setTop(loadTopMenuBar());
        primaryBorderPane.setCenter(primaryCenterStackPane);
        primaryBorderPane.setLeft(loadLeftSideBar());
        primaryBorderPane.setOnMouseClicked(_ -> primaryBorderPane.requestFocus());

        HBox.setHgrow(mainDisplayStackPane, Priority.ALWAYS);
        mainCenterHBox.getChildren().add(sideBarController.getSideMenuParentOuterNode());
        mainCenterHBox.getChildren().add(mainDisplayStackPane);

        loadConditionMenuPane();
        loadActionMenuPane();

        sideBarController.switchTab(CurrentTab.Operation);
        return new Scene(primaryBorderPane);
    }

    // ------------------------------------------------------
    private static void setPrimaryBorderPaneSize(WindowSizeMode mode) {
        if (mode == WindowSizeMode.Maximized)
            isMaximized = true;
        else if (mode == WindowSizeMode.Compact) {
            primaryBorderPane.setPrefHeight(400);
            primaryBorderPane.setPrefWidth(750);
            return;
        }
        primaryBorderPane.setPrefHeight(700);
        primaryBorderPane.setPrefWidth(1200);
    }

    // ------------------------------------------------------
    public static void openCenterBanner(String title, String content) {
        if (centerBannerController == null)
            return;
        centerBannerController.openCenterBanner(title, content);
        mainNotificationStackPane.setMouseTransparent(false);
    }
    public static void closeCenterBanner() {
        mainNotificationStackPane.setMouseTransparent(true);
    }

    public static void showNotification(String content) {
        topNotificationController.showNotification(content);
    }

    // ------------------------------------------------------
    public static void addLog(LogLevel logLevel, String className, String content) {
        bottomPaneController.addToLog(logLevel, className, content);
    }

    // ------------------------------------------------------
    public static void setIsRunning(boolean isRunning) {
        AppScene.isRunning = isRunning;
        menuBarController.setOperationRunning(isRunning);
    }

    public static void startOperationRun() {
        if (currentLoadedOperationController == null)
            return;
        setIsRunning(true);
        AppScene.addLog(LogLevel.INFO, className, "Starting operation");
        currentLoadedOperationRunController.startOperation(currentLoadedOperationController.getSavedData());
    }
    public static void stopOperationRun() {
        AppScene.addLog(LogLevel.INFO, className, "Stopping operation");
        currentLoadedOperationRunController.stopOperation();
        setIsRunning(false);
    }

    // ------------------------------------------------------
    public static void updateOperationSideMenuHierarchy() {
        if (currentLoadedOperationController != null)
            sideBarController.loadSideHierarchy(currentLoadedOperationController);
    }
    public static void updateOperationRunSideMenuHierarchy() {
        if (currentLoadedOperationController == null)
            return;
        String name = currentLoadedOperationController.getOperationNameLabel().getText();
        sideBarController.loadRunSideHierarchy(name, currentLoadedOperationController.getOperationSideContent(),
                currentLoadedOperationController);
    }

    // ------------------------------------------------------
    public static void displayNewCenterNode(Node node) {
        mainDisplayStackPane.getChildren().clear();
        mainDisplayStackPane.getChildren().add(node);
        AppScene.addLog(LogLevel.TRACE, className, "Displayed new center node");
    }
    public static void backToOperationScene() {
        if (currentLoadedOperationController == null)
            return;
        displayNewCenterNode(currentLoadedOperationController.getParentNode());
        AppScene.addLog(LogLevel.TRACE, className, "Backed to Operation Scene");
    }

    // ------------------------------------------------------
    public static void openConditionMenuPane(ConditionController conditionController) {
        conditionMenuController.loadMenu(conditionController);
        mainDisplayStackPane.getChildren().add(conditionMenuController.getMainMenuStackPane());
        AppScene.addLog(LogLevel.DEBUG, className, "Opened condition menu");
    }
    public static void closeConditionMenuPane() {
        mainDisplayStackPane.getChildren().remove(conditionMenuController.getMainMenuStackPane());
        AppScene.addLog(LogLevel.DEBUG, className, "Closed condition menu");
    }
    public static void openActionMenuPane(ActionController actionController) {
        actionMenuController.loadMenu(actionController);
        mainDisplayStackPane.getChildren().add(actionMenuController.getMainMenuStackPane());
        AppScene.addLog(LogLevel.DEBUG, className, "Opened action menu");
    }
    public static void closeActionMenuPane() {
        mainDisplayStackPane.getChildren().remove(actionMenuController.getMainMenuStackPane());
        AppScene.addLog(LogLevel.DEBUG, className, "Closed action menu");
    }

    // ------------------------------------------------------
    public static void loadEmptyOperation() {
        if (currentLoadedOperationController != null && !currentLoadedOperationController.isSet()) {
            AppScene.addLog(LogLevel.INFO, className, "Empty operation already loaded");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(AppScene.class.getResource("JobController/operationPane.fxml"));
            Node operationPane = loader.load();
            displayNewCenterNode(operationPane);
            currentLoadedOperationController = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded empty operation pane");
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading empty operation pane: " + e.getMessage());
        }
    }

    public static boolean loadSavedOperation(String path) {
        AppScene.addLog(LogLevel.DEBUG, className, "Loading saved operation");
        try (FileInputStream fileIn = new FileInputStream(path);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            OperationData operationData = (OperationData) in.readObject();
            AppScene.addLog(LogLevel.TRACE, className, "Passed getting data");

            FXMLLoader loader = new FXMLLoader(AppScene.class.getResource("JobController/operationPane.fxml"));
            Node operationPane = loader.load();
            AppScene.addLog(LogLevel.TRACE, className, "Passed loading operation pane");

            currentLoadedOperationController = loader.getController();
            AppScene.addLog(LogLevel.TRACE, className, "Passed getting controller");

            currentLoadedOperationController.loadSavedData(operationData);
            AppScene.addLog(LogLevel.TRACE, className, "Passed loading saved data");

            displayNewCenterNode(operationPane);
            AppScene.addLog(LogLevel.TRACE, className, "Passed displaying operation pane");

            AppScene.addLog(LogLevel.DEBUG, className, "Loaded saved operation: " + path);
            return true;
        } catch (IOException | ClassNotFoundException e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading operation data: " + e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------------
    private static Node loadBottomPane() {
        try {
            FXMLLoader bottomPaneLoader = new FXMLLoader(AppScene.class.getResource("SideMenu/bottomPane.fxml"));
            Node bottomPane = bottomPaneLoader.load();
            bottomPaneController = bottomPaneLoader.getController();
            return bottomPane;
        } catch (Exception e) {
            System.out.println("Error loading bottom pane: " + e.getMessage());
            return null;
        }
    }
    private static void loadSettingPane() {
        AppScene.addLog(LogLevel.TRACE, className, "Loading setting menu pane");
        try {
            FXMLLoader settingMenuLoader = new FXMLLoader(AppScene.class.getResource("SideMenu/TopMenu/settingPane.fxml"));
            settingMenuLoader.load();
            settingMenuController = settingMenuLoader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded setting menu pane");
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading setting menu pane: " + e.getMessage());
        }
    }
    private static Node loadTopMenuBar() {
        AppScene.addLog(LogLevel.TRACE, className, "Loading Top menu bar");
        try {
            FXMLLoader menuBarLoader = new FXMLLoader(AppScene.class.getResource("SideMenu/TopMenu/topMenuBar.fxml"));
            Node menuBarNode = menuBarLoader.load();
            menuBarController = menuBarLoader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Top menu bar");
            return menuBarNode;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading top menu bar: " + e.getMessage());
            return null;
        }
    }
    private static Node loadLeftSideBar() {
        AppScene.addLog(LogLevel.TRACE, className, "Loading Left Side Bar");
        try {
            FXMLLoader sideBarLoader = new FXMLLoader(AppScene.class.getResource("SideMenu/LeftMenu/sideBarPane.fxml"));
            Node leftSideBarNode = sideBarLoader.load();
            sideBarController = sideBarLoader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Left Side Bar");
            return leftSideBarNode;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading left side bar: " + e.getMessage());
            return null;
        }
    }
    private static Node loadCenterBannerPane() {
        AppScene.addLog(LogLevel.TRACE, className, "Loading center banner pane");
        try {
            FXMLLoader centerBannerLoader = new FXMLLoader(AppScene.class.getResource("SideMenu/centerBannerPane.fxml"));
            Node centerBannerNode = centerBannerLoader.load();
            centerBannerController = centerBannerLoader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded center banner pane");
            return centerBannerNode;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading center banner pane: " + e.getMessage());
            return null;
        }
    }
    private static Node loadTopNotificationBannerPane() {
        AppScene.addLog(LogLevel.TRACE, className, "Loading top notification banner pane");
        topNotificationController = new TopNotificationController();
        Node topNotificationNode = topNotificationController.getOuterParentNode();
        StackPane.setAlignment(topNotificationNode, Pos.TOP_CENTER);
        AppScene.addLog(LogLevel.DEBUG, className, "Loaded top notification banner pane");
        return topNotificationNode;
    }

    private static void loadActionMenuPane() {
        AppScene.addLog(LogLevel.TRACE, className, "Loading Action Menu");
        try {
            FXMLLoader loader = new FXMLLoader(AppScene.class.getResource("Menu/actionMenuPane.fxml"));
            loader.load();
            actionMenuController = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Action Menu");
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading action menu pane: " + e.getMessage());
        }
    }
    private static void loadConditionMenuPane() {
        AppScene.addLog(LogLevel.TRACE, className, "Loading Condition Menu");
        try {
            FXMLLoader loader = new FXMLLoader(AppScene.class.getResource("Menu/conditionMenuPane.fxml"));
            loader.load();
            conditionMenuController = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Condition Menu");
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading condition menu pane: " + e.getMessage());
        }
    }
}
