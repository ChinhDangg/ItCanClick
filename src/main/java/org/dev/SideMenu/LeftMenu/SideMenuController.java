package org.dev.SideMenu.LeftMenu;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import org.dev.AppScene;
import org.dev.Enum.CurrentTab;
import org.dev.Enum.LogLevel;
import org.dev.Operation.*;
import org.dev.RunOperation.OperationRunController;

import java.net.URL;
import java.util.ResourceBundle;

/**
 operationHBox
 operationSideContentVBox
    VBox
    taskLabelHBox
    taskSideContentVBox
        actionLabelHBox
        actionLabelHBox
        actionLabelHBox
    VBox
    taskLabelHBox
    taskSideContentVBox
        actionLabelHBox
        actionLabelHBox
 */

public class SideMenuController implements Initializable {

    @FXML
    private ScrollPane sideMenuMainScrollPane;
    @FXML
    private StackPane mainSideHierarchyStackPane;
    @FXML
    private VBox runSideHierarchyVBox, sideHierarchyVBox;
    @FXML
    private Group newOperationGroupButton;

    private final String className = this.getClass().getSimpleName();
    private static RightClickMenuController rightClickMenuController;
    private double currentScale = 1.0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setScaleHierarchyVBox();
        rightClickMenuController = loadRightClickMenu();
        newOperationGroupButton.setOnMouseClicked(this::getNewOperationPane);
    }

    // ------------------------------------------------------
    private void setScaleHierarchyVBox() {
        if (currentScale != AppScene.currentGlobalScale) {
            currentScale = AppScene.currentGlobalScale;
            mainSideHierarchyStackPane.getTransforms().add(new Scale(currentScale, currentScale, 0, 0));
        }
        AppScene.addLog(LogLevel.TRACE, className, "Scaled side menu: " + currentScale);
    }

    private void getNewOperationPane(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on new operation button");
        AppScene.loadEmptyOperation();
        AppScene.updateOperationSideMenuHierarchy();
    }

    public void showSideMenuContent(CurrentTab whatTab) {
        if (whatTab == CurrentTab.Operation) {
            sideHierarchyVBox.setVisible(true);
            runSideHierarchyVBox.setVisible(false);
        }
        else {
            sideHierarchyVBox.setVisible(false);
            runSideHierarchyVBox.setVisible(true);
        }
        AppScene.addLog(LogLevel.DEBUG, className, "Showing tab: " + whatTab.name());
    }

    public void toggleSideMenuHierarchy() {
        boolean newSet = !sideMenuMainScrollPane.isVisible();
        sideMenuMainScrollPane.setVisible(newSet);
        sideMenuMainScrollPane.setManaged(newSet);
        AppScene.addLog(LogLevel.DEBUG, className, "Showed Side menu: " + newSet);
    }

    // ------------------------------------------------------
    public static Node getNewSideHBoxLabel(Label label, VBox content, DataController dataController, DataController parentController) {
        SideMenuLabelController controller = loadSideMenuLabelController();
        if (controller == null)
            return null;
        return controller.createHBoxLabel(label, content, dataController, parentController, rightClickMenuController);
    }

    public static Node getNewSideHBoxLabel(Label label, VBox content, MainJobController jobController) {
        SideMenuLabelController controller = loadSideMenuLabelController();
        if (controller == null)
            return null;
        return controller.createHBoxLabel(label, content, jobController);
    }

    private static SideMenuLabelController loadSideMenuLabelController() {
        try {
            FXMLLoader sideMenuLabelLoader = new FXMLLoader(SideMenuController.class.getResource("sideMenuLabel.fxml"));
            sideMenuLabelLoader.load();
            return sideMenuLabelLoader.getController();
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, SideMenuController.class.getSimpleName(), "Error loading side menu label: " + e.getMessage());
            return null;
        }
    }

    // ------------------------------------------------------
    public void loadOperationSideHierarchy(OperationController operationController) {
        newOperationGroupButton.setVisible(false);

        ObservableList<Node> sideHierarchyChildren = sideHierarchyVBox.getChildren();
        sideHierarchyChildren.clear();

        VBox operationSideContent = operationController.getOperationSideContent();
        Node operationSideHBoxLabel = getNewSideHBoxLabel(operationController.getOperationNameLabel(),
                operationSideContent, operationController, null);
        sideHierarchyChildren.add(operationSideHBoxLabel);
        sideHierarchyChildren.add(operationSideContent);

        AppScene.addLog(LogLevel.DEBUG, className, "Loaded Operation side hierarchy");
    }

    public void loadOperationRunSideHierarchy(OperationRunController operationRunController) {
        ObservableList<Node> runSideHierarchyChildren = runSideHierarchyVBox.getChildren();
        runSideHierarchyChildren.clear();

        VBox operationRunSideContent = operationRunController.getOperationRunSideContent();
        Node operationRunSideHBoxLabel = getNewSideHBoxLabel(new Label(operationRunController.getOperationNameRunLabel().getText()),
                operationRunSideContent, operationRunController);
        runSideHierarchyChildren.add(operationRunSideHBoxLabel);
        runSideHierarchyChildren.add(operationRunSideContent);

        AppScene.addLog(LogLevel.DEBUG, className, "Loaded Operation run side hierarchy");
    }

    private RightClickMenuController loadRightClickMenu() {
        try {
            FXMLLoader rightClickMenuLoader = new FXMLLoader(SideMenuController.class.getResource("rightClickMenuPane.fxml"));
            rightClickMenuLoader.load();
            return rightClickMenuLoader.getController();
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading right click menu: " + e.getMessage());
            return null;
        }
    }
}
