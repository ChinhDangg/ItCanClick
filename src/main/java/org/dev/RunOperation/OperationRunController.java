package org.dev.RunOperation;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import lombok.Getter;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import org.dev.Operation.Data.OperationData;
import org.dev.Operation.Data.TaskData;
import org.dev.Operation.MainJobController;
import org.dev.Operation.OperationController;
import org.dev.Operation.Task.Task;
import org.dev.SideMenu.SideMenuController;
import java.net.URL;
import java.util.ResourceBundle;

public class OperationRunController implements Initializable, MainJobController {

    @FXML @Getter
    private ScrollPane operationRunScrollPane;
    @FXML
    private VBox mainOperationRunVBox;
    @FXML @Getter
    private Label operationNameRunLabel;
    @FXML @Getter
    private VBox runVBox;

    private Thread operationRunThread = null;
    @Getter
    private VBox taskRunVBoxSideContent = new VBox();
    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadMainOperationRunVBox();
        taskRunVBoxSideContent.setPadding(new Insets(0, 0, 0, 15));
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
            AppScene.addLog(LogLevel.ERROR, className, "Operation run scene is null but take to display is called");
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

    public void setVisible(boolean visible) { getParentNode().setVisible(visible); }

    private void changeOperationRunName(String newName) { operationNameRunLabel.setText(newName); }

    // ------------------------------------------------------
    public void startOperation(OperationController operationController) {
        OperationData operationData = operationController.getOperationData();
        if (operationData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "No operation data found - cannot start");
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
                } catch (InterruptedException e) {
                    AppScene.addLog(LogLevel.ERROR, className, "Error to start run operation");
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
        boolean pass = false;
        for (TaskData taskData : operationData.getTaskDataList()) {
            Task currentTask = taskData.getTask();
            String taskName = currentTask.getTaskName();
            loadAndAddNewTaskRunPane();
            if (pass && currentTask.isPreviousPass()) {
                AppScene.addLog(LogLevel.INFO, className, "Previous is passed, Skipping task " + taskName);
                continue;
            }
            pass = currentTaskRunController.startTask(taskData);
            if (!currentTask.isRequired())
                pass = true;
            else if (!pass) { // task is required but failed
                AppScene.addLog(LogLevel.INFO, className, "Fail performing task: " + taskName);
                break;
            }
        }
    }

    private TaskRunController currentTaskRunController;
    private void loadAndAddNewTaskRunPane() {
        try {
            currentTaskRunController = null;
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("taskRunPane.fxml"));
            Group taskRunGroup = fxmlLoader.load();
            currentTaskRunController = fxmlLoader.getController();
            VBox actionRunVBox = currentTaskRunController.getActionRunVBoxSideContent();
            HBox taskRunLabelHBox = SideMenuController.getDropDownHBox(actionRunVBox,
                    new Label(currentTaskRunController.getTaskRunNameLabel().getText()),
                    currentTaskRunController);
            // update side hierarchy
            Platform.runLater(() -> taskRunVBoxSideContent.getChildren().add(new VBox(taskRunLabelHBox, actionRunVBox)));
            Platform.runLater(() -> runVBox.getChildren().add(taskRunGroup));
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading task run pane");
        }
    }
}
