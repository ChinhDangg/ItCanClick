package org.dev.SideMenu.LeftMenu;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import org.dev.AppScene;
import org.dev.Enum.CurrentTab;
import org.dev.Enum.LogLevel;
import org.dev.Operation.OperationController;
import org.dev.RunOperation.OperationRunController;

import java.net.URL;
import java.util.ResourceBundle;

public class SideBarController implements Initializable {

    @FXML
    private StackPane sideBarMainStackPane;
    @FXML
    private StackPane folderIconStackPane, runIconStackPane, logIconStackPane;

    @Getter
    private Node sideMenuParentOuterNode;
    private SideMenuController sideMenuController;
    private CurrentTab currentTab;
    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        folderIconStackPane.setOnMouseClicked(this::folderIconClickAction);
        runIconStackPane.setOnMouseClicked(this::runIconClickAction);
        logIconStackPane.setOnMouseClicked(this::logIconClickAction);
        loadLeftSideMenu();
    }

    // ------------------------------------------------------
    private void folderIconClickAction(MouseEvent mouseEvent) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on folder icon");
        if (!switchTab(CurrentTab.Operation))
            toggleSideHierarchy();
    }

    private void runIconClickAction(MouseEvent mouseEvent) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on run icon");
        if (!switchTab(CurrentTab.OperationRun))
            toggleSideHierarchy();
    }

    private boolean isLogIconHighlighted = false;
    private void logIconClickAction(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on log icon");
        toggleLogPane();
    }
    public void toggleLogPane() {
        isLogIconHighlighted = !isLogIconHighlighted;
        logIconStackPane.pseudoClassStateChanged(PseudoClass.getPseudoClass("highlighted"), isLogIconHighlighted);
        AppScene.bottomPaneController.switchBottomPaneVisible();
        AppScene.addLog(LogLevel.DEBUG, className, "Log icon highlighted: " + isLogIconHighlighted);
    }

    // ------------------------------------------------------
    private void toggleSideHierarchy() {
        sideMenuController.toggleSideMenuHierarchy();
    }

    public boolean switchTab(CurrentTab newTab) {
        if (currentTab == newTab)
            return false;
        boolean isOperationRun = false;
        if (newTab == CurrentTab.Operation)
            AppScene.backToOperationScene();
        else {
            boolean isDisplayed = AppScene.displayCurrentOperationRun();
            if (!isDisplayed)
                return false;
            isOperationRun = true;
        }
        currentTab = newTab;
        sideMenuController.showSideMenuContent(currentTab);
        folderIconStackPane.pseudoClassStateChanged(PseudoClass.getPseudoClass("highlighted"), !isOperationRun);
        runIconStackPane.pseudoClassStateChanged(PseudoClass.getPseudoClass("highlighted"), isOperationRun);
        AppScene.addLog(LogLevel.DEBUG, className, "Tab switched: " + currentTab);
        return true;
    }

    public void loadSideHierarchy(OperationController operationController) {
        switchTab(CurrentTab.Operation);
        sideMenuController.loadOperationSideHierarchy(operationController);
    }

    public void loadRunSideHierarchy(OperationRunController operationRunController) {
        switchTab(CurrentTab.OperationRun);
        sideMenuController.loadOperationRunSideHierarchy(operationRunController);
    }

    private void loadLeftSideMenu() {
        AppScene.addLog(LogLevel.TRACE, className, "Loading Left Side Menu");
        try {
            FXMLLoader sideMenuLoader = new FXMLLoader(this.getClass().getResource("sideMenuPane.fxml"));
            sideMenuParentOuterNode = sideMenuLoader.load();
            sideMenuController = sideMenuLoader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Left Side Menu");
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading left side menu pane: " + e.getMessage());
        }
    }
}
