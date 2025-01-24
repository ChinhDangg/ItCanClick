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
import org.dev.JobData.OperationData;
import org.dev.JobData.TaskData;
import org.dev.JobController.MainJobController;
import org.dev.JobController.OperationController;
import org.dev.Job.Task.Task;
import org.dev.SideMenu.LeftMenu.SideMenuController;
import java.net.URL;
import java.util.ResourceBundle;

public class OperationRunController implements Initializable, MainJobController {
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
    private Thread operationRunThread = null;
    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadMainOperationRunVBox();
    }

    public Node getParentNode() {
        return operationRunScrollPane;
    }

    private double currentGlobalScale = 1.0;
    private void loadMainOperationRunVBox() {
        if (currentGlobalScale != AppScene.currentGlobalScale) {
            currentGlobalScale = AppScene.currentGlobalScale;
            mainOperationRunVBox.getTransforms().add(new Scale(currentGlobalScale, currentGlobalScale, 0, 0));
        }
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

    @Override
    public AppLevel getAppLevel() {
        return AppLevel.Operation;
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

    public void setVisible(boolean visible) { getParentNode().setVisible(visible); }

    private void changeOperationRunName(String newName) { operationNameRunLabel.setText(newName); }

    // ------------------------------------------------------
    public void startOperation(OperationController operationController) {
        OperationData operationData = operationController.getSavedData();
        if (operationData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Operation data is null - cannot start");
            return;
        }
        String operationName = operationData.getOperation().getOperationName();
        changeOperationRunName(operationName);
        AppScene.addLog(LogLevel.INFO, className, "Start running operation: " + operationName);

        javafx.concurrent.Task<Void> operationRunTask = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() {
                try {
                    runOperation(operationData);
                    AppScene.addLog(LogLevel.INFO, className, "Finished running operation: " + operationName);
                    Platform.runLater(() -> AppScene.setIsOperationRunning(false));
                } catch (Exception e) {
                    AppScene.addLog(LogLevel.ERROR, className, "Error to start run operation: " + e.getMessage());
                }
                return null;
            }
        };
        operationRunThread = new Thread(operationRunTask);
        AppScene.setIsOperationRunning(true);
        operationRunThread.start();
    }

    public void stopOperation() {
        operationRunThread.interrupt();
        AppScene.setIsOperationRunning(false);
    }

    private void runOperation(OperationData operationData) throws InterruptedException {
//        boolean pass = false;
//        for (TaskData taskData : operationData.getTaskDataList()) {
//            Task currentTask = taskData.getTask();
//            String taskName = currentTask.getTaskName();
//            loadAndAddNewTaskRunPane(currentTask.getTaskName());
//            if (pass && currentTask.isPreviousPass()) {
//                AppScene.addLog(LogLevel.INFO, className, "Previous is passed, Skipping task " + taskName);
//                continue;
//            }
//            pass = currentTaskRunController.startTask(taskData);
//            if (!currentTask.isRequired())
//                pass = true;
//            else if (!pass) { // task is required but failed
//                AppScene.addLog(LogLevel.INFO, className, "Fail performing task: " + taskName);
//                break;
//            }
//        }
    }

    private TaskRunController currentTaskRunController;
    private void loadAndAddNewTaskRunPane(String taskName) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("taskRunPane.fxml"));
            Node taskRunGroup = fxmlLoader.load();
            currentTaskRunController = fxmlLoader.getController();
            currentTaskRunController.changeTaskRunName(taskName);
            VBox taskRunSideContent = currentTaskRunController.getTaskRunVBoxSideContent();
            Node taskRunHBoxLabel = SideMenuController.getNewSideHBoxLabel(
                    new Label(taskName), taskRunSideContent, currentTaskRunController);
            // update side hierarchy
            Platform.runLater(() -> operationRunSideContent.getChildren().addAll(taskRunHBoxLabel, taskRunSideContent));
            Platform.runLater(() -> runVBox.getChildren().add(taskRunGroup));
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading task run pane: " + e.getMessage());
        }
    }
}
