package org.dev.RunJob;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import lombok.Getter;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.Job.Task.TaskGroup;
import org.dev.JobData.JobData;
import org.dev.JobData.OperationData;
import org.dev.JobData.TaskGroupData;
import org.dev.SideMenu.LeftMenu.SideMenuController;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class OperationRunController implements Initializable, JobRunController {
    @FXML
    private ScrollPane operationRunScrollPane;
    @FXML
    private VBox mainOperationRunVBox;
    @FXML @Getter
    private Label operationNameRunLabel;
    @FXML
    private VBox runVBox;

    @Getter
    private VBox operationRunSideContent = new VBox();
    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadMainOperationRunVBox();
    }

    private double currentGlobalScale = 1.0;
    private void loadMainOperationRunVBox() {
        if (currentGlobalScale != AppScene.currentGlobalScale) {
            currentGlobalScale = AppScene.currentGlobalScale;
            mainOperationRunVBox.getTransforms().add(new Scale(currentGlobalScale, currentGlobalScale, 0, 0));
        }
    }

    @Override
    public Node getParentNode() {
        return operationRunScrollPane;
    }

    @Override
    public AppLevel getAppLevel() {
        return AppLevel.Operation;
    }

    @Override
    public void takeToDisplay() {
        if (mainOperationRunVBox.getScene() == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Operation run scene is null - but take to display is called");
            return;
        }
        AppScene.closeActionMenuPane();
        AppScene.closeConditionMenuPane();
        operationRunScrollPane.setVvalue(0.0);
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    public void changeScrollPaneVValueView(Node node) {
        double targetPaneY = node.getBoundsInParent().getMinY();
        Node parentChecking = node.getParent();
        while (parentChecking != mainOperationRunVBox) {
            targetPaneY += parentChecking.getBoundsInParent().getMinY();
            parentChecking = parentChecking.getParent();
        }
        targetPaneY += parentChecking.getBoundsInParent().getMinY();
        targetPaneY *= currentGlobalScale;
        double contentHeight = operationRunScrollPane.getContent().getBoundsInLocal().getHeight();
        double scrollPaneHeight = operationRunScrollPane.getViewportBounds().getHeight();
        targetPaneY -= scrollPaneHeight / 3;
        double vValue = Math.min(targetPaneY / (contentHeight - scrollPaneHeight), 1.00);
        operationRunScrollPane.setVvalue(vValue);
        AppScene.addLog(LogLevel.TRACE, RunActivity.class.getSimpleName(), "Updated scroll pane v value: " + vValue);
    }

    private void changeOperationRunName(String newName) { operationNameRunLabel.setText(newName); }

    // ------------------------------------------------------
    @Override
    public boolean startJob(JobData jobData) {
        if (jobData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Operation data is null - cannot start");
            return false;
        }
        OperationData operationData = (OperationData) jobData;
        String operationName = operationData.getOperation().getOperationName();
        changeOperationRunName(operationName);
        return runOperation(operationData);
    }

    private boolean runOperation(OperationData operationData) {
        AppScene.addLog(LogLevel.INFO, className, "Start running operation: " + operationData.getOperation().getOperationName());
        List<TaskGroupData> taskGroupDataList = operationData.getTaskGroupDataList();
        for (TaskGroupData taskData : taskGroupDataList) {
            TaskGroup currentTaskGroup = taskData.getTaskGroup();
            String taskName = currentTaskGroup.getTaskGroupName();
            loadAndAddNewTaskRunPane(taskName);
            boolean pass = currentTaskGroupRunController.startJob(taskData);
            if (currentTaskGroup.isRequired() && !pass) { // task is required but failed
                AppScene.addLog(LogLevel.INFO, className, "Fail performing task group: " + taskName);
                return false;
            }
        }
        return true;
    }

    private TaskGroupRunController currentTaskGroupRunController;
    private void loadAndAddNewTaskRunPane(String taskName) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("taskGroupRunPane.fxml"));
            Node taskRunGroup = fxmlLoader.load();
            currentTaskGroupRunController = fxmlLoader.getController();
            VBox taskRunSideContent = currentTaskGroupRunController.getTaskGroupRunSideContent();
            Node taskRunHBoxLabel = SideMenuController.getNewSideHBoxLabel(
                    new Label(taskName), taskRunSideContent, currentTaskGroupRunController);
            // update side hierarchy
            Platform.runLater(() -> operationRunSideContent.getChildren().addAll(taskRunHBoxLabel, taskRunSideContent));
            Platform.runLater(() -> runVBox.getChildren().add(taskRunGroup));
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading task run pane: " + e.getMessage());
        }
    }
}
