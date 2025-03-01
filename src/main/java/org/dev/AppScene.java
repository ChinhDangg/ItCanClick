package org.dev;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import org.dev.Enum.LogLevel;
import org.dev.Job.JobData;
import org.dev.JobController.JobDataController;
import org.dev.SideMenu.*;
import org.dev.Menu.ActionMenuController;
import org.dev.Menu.ConditionMenuController;
import org.dev.JobController.ActionController;
import org.dev.JobController.ConditionController;
import org.dev.JobController.OperationController;
import org.dev.Enum.CurrentTab;
import org.dev.SideMenu.LeftMenu.SideBarController;
import org.dev.SideMenu.TopMenu.MenuBarController;
import org.dev.SideMenu.TopMenu.SettingMenuController;
import org.dev.SideMenu.TopMenu.WindowSizeMode;
import org.dev.jobManagement.JobRunScheduler;
import org.dev.jobManagement.JobRunStructure;
import org.dev.jobManagement.JobStructure;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class AppScene {
    private static final String className = AppScene.class.getSimpleName();

    public static MainDisplay mainDisplay;

    public static MenuBarController menuBarController;
    public static SettingMenuController settingMenuController;
    public static SideBarController sideBarController;
    public static BottomPaneController bottomPaneController;
    public static CenterBannerController centerBannerController;
    public static TopNotificationController topNotificationController;
    public static ActionMenuController actionMenuController;
    public static ConditionMenuController conditionMenuController;

    public static JobStructure currentJobStructure;
    public static final JobRunScheduler jobRunScheduler = new JobRunScheduler();

    public static double currentGlobalScale = 1.3;
    public static WindowSizeMode windowSizeMode;

    public static Scene getAppMainScene() {
        Node bottomPane = loadBottomPane();
        Node notificationPane = loadTopNotificationBannerPane();
        Node topPane = loadTopMenuBar();
        Node leftBarPane = loadLeftSideBar();
        Node leftMenuPane = sideBarController.getSideMenuParentOuterNode();

        centerBannerController = loadCenterBannerPane();

        mainDisplay = new MainDisplay(bottomPane, notificationPane, topPane, leftBarPane, leftMenuPane);

        loadSettingPane();
        bottomPaneController.setDebug(settingMenuController.isDebug());
        bottomPaneController.setTrace(settingMenuController.isTrace());
        currentGlobalScale = settingMenuController.getGlobalScaleValue();
        windowSizeMode = settingMenuController.getWindowSizeMode();

        loadConditionMenuPane();
        loadActionMenuPane();

        sideBarController.switchTab(CurrentTab.Operation);
        return new Scene(mainDisplay.getParentNode());
    }

    // ------------------------------------------------------
    public static void openCenterBanner(String title, String content) {
        if (centerBannerController == null)
            return;
        centerBannerController.openCenterBanner(mainDisplay.getParentNode(), title, content);
    }

    public static void showNotification(String content) {
        topNotificationController.showNotification(content);
    }

    // ------------------------------------------------------
    public static void addLog(LogLevel logLevel, String className, String content) {
        bottomPaneController.addToLog(logLevel, className, content);
    }

    // ------------------------------------------------------

    public static boolean isJobRunning() {
        return jobRunScheduler.isJobRunning();
    }

    public static void setIsJobRunning(boolean isJobRunning) {
        menuBarController.setOperationRunning(isJobRunning);
    }

    public static void startOperationRun() {
        if (currentJobStructure == null)
            return;
        startJobRun(currentJobStructure.getCurrentController());
    }

    public static void stopOperationRun() {
        jobRunScheduler.stopRunJob();
    }

    public static void startJobRun(JobDataController jobDataController) {
        jobRunScheduler.startJobRun(jobDataController);
    }

    // ------------------------------------------------------
    public static void loadSideMenuHierarchy() {
        if (currentJobStructure != null)
            sideBarController.loadSideHierarchy(currentJobStructure);
    }
    public static void loadRunSideMenuHierarchy() {
        JobRunStructure jobRunStructure = jobRunScheduler.getCurrentJobRunStructure();
        if (jobRunStructure != null)
            sideBarController.loadRunSideHierarchy(jobRunStructure);
    }

    // ------------------------------------------------------
    public static void displayNewCenterNode(Node node) {
        mainDisplay.displayNewMainNode(node);
        AppScene.addLog(LogLevel.TRACE, className, "Displayed new center node");
    }

    public static void updateMainDisplayScrollValue(Node node) {
        mainDisplay.changeScrollPaneView(node);
    }

    public static void backToOperationScene() {
        if (currentJobStructure == null)
            return;
        displayNewCenterNode(currentJobStructure.getCurrentController().getParentNode());
        AppScene.addLog(LogLevel.TRACE, className, "Backed to Operation Scene");
    }

    public static boolean displayCurrentRunJobNode() {
        try {
            displayNewCenterNode(jobRunScheduler.getCurrentJobRunStructure().getCurrentController().getParentNode());
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    // ------------------------------------------------------
    public static void openConditionMenuPane(ConditionController conditionController) {
        conditionMenuController.loadMenu(conditionController);
        mainDisplay.displayInMainDisplayStackPane(conditionMenuController.getMainMenuStackPane());
        AppScene.addLog(LogLevel.DEBUG, className, "Opened condition menu");
    }
    public static void closeConditionMenuPane() {
        mainDisplay.clearDisplayInMainDisplayStackPane(conditionMenuController.getMainMenuStackPane());
        AppScene.addLog(LogLevel.DEBUG, className, "Closed condition menu");
    }
    public static void openActionMenuPane(ActionController actionController) {
        actionMenuController.loadMenu(actionController);
        mainDisplay.displayInMainDisplayStackPane(actionMenuController.getMainMenuStackPane());
        AppScene.addLog(LogLevel.DEBUG, className, "Opened action menu");
    }
    public static void closeActionMenuPane() {
        mainDisplay.clearDisplayInMainDisplayStackPane(actionMenuController.getMainMenuStackPane());
        AppScene.addLog(LogLevel.DEBUG, className, "Closed action menu");
    }

    // ------------------------------------------------------
    public static void loadEmptyOperation() {
        if (currentJobStructure != null && !currentJobStructure.getCurrentController().isSet()) {
            AppScene.addLog(LogLevel.INFO, className, "Empty operation already loaded");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(AppScene.class.getResource("JobController/operationPane.fxml"));
            Node operationPane = loader.load();
            displayNewCenterNode(operationPane);
            OperationController controller = loader.getController();
            currentJobStructure = new JobStructure(null, null, controller, controller.getName());
            controller.setJobStructure(currentJobStructure);
            controller.addSavedData(null);
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded empty operation pane");
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading empty operation pane: " + e.getMessage());
        }
    }

    public static boolean loadSavedOperation(String path) {
        AppScene.addLog(LogLevel.DEBUG, className, "Loading saved operation");
        try (FileInputStream fileIn = new FileInputStream(path);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            JobData operationData = (JobData) in.readObject();
            AppScene.addLog(LogLevel.TRACE, className, "Passed getting data");

            FXMLLoader loader = new FXMLLoader(AppScene.class.getResource("JobController/operationPane.fxml"));
            Node operationPane = loader.load();
            AppScene.addLog(LogLevel.TRACE, className, "Passed loading operation pane");

            OperationController controller = loader.getController();
            AppScene.addLog(LogLevel.TRACE, className, "Passed getting controller");

            currentJobStructure = new JobStructure(null, null, controller, controller.getName());
            controller.setJobStructure(currentJobStructure);
            AppScene.addLog(LogLevel.TRACE, className, "Passed assigning structure");

            controller.loadSavedData(operationData);
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
    private static CenterBannerController loadCenterBannerPane() {
        AppScene.addLog(LogLevel.TRACE, className, "Loading center banner pane");
        try {
            FXMLLoader centerBannerLoader = new FXMLLoader(AppScene.class.getResource("SideMenu/centerBannerPane.fxml"));
            centerBannerLoader.load();
            CenterBannerController controller =  centerBannerLoader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded center banner pane");
            return controller;
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
