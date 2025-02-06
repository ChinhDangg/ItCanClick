package org.dev.RunJob;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.Job.Operation;
import org.dev.Job.Task.TaskGroup;
import org.dev.Job.JobData;
import org.dev.JobRunStructure;

import java.util.List;

public class OperationRunController implements JobRunController {
    @FXML
    private Node parentNode;
    @FXML
    private VBox mainOperationRunVBox;
    @FXML
    private Label operationNameRunLabel;
    @FXML
    private VBox runVBox;

    private JobRunStructure currentRunStructure;
    private final String className = this.getClass().getSimpleName();

    private void loadScale() {
        if (currentRunStructure.getParentController() != null)
            return;
        double currentGlobalScale = 1.0;
        if (currentGlobalScale != AppScene.currentGlobalScale) {
            currentGlobalScale = AppScene.currentGlobalScale;
            mainOperationRunVBox.getTransforms().add(new Scale(currentGlobalScale, currentGlobalScale, 0, 0));
        }
    }

    private void changeOperationRunName(String newName) {
        Platform.runLater(() -> operationNameRunLabel.setText(newName));
    }

    // ------------------------------------------------------
    @Override
    public Node getParentNode() {
        return parentNode;
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
        AppScene.updateMainDisplayScrollValue(getParentNode());
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    @Override
    public void setJobRunStructure(JobRunStructure runStructure) {
        currentRunStructure = runStructure;
        loadScale();
    }

    @Override
    public boolean startJob(JobData jobData) {
        if (jobData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Operation data is null - cannot start");
            return false;
        }
        Operation operation = (Operation) jobData.getMainJob();
        String operationName = operation.getOperationName();
        changeOperationRunName(operationName);
        return runOperation(jobData);
    }

    private boolean runOperation(JobData operationData) {
        AppScene.addLog(LogLevel.INFO, className, "Start running operation: " + ((Operation) operationData.getMainJob()).getOperationName());
        List<JobData> taskGroupDataList = operationData.getJobDataList();
        for (JobData taskData : taskGroupDataList) {
            if (taskData == null)
                continue;
            TaskGroup currentTaskGroup = (TaskGroup) taskData.getMainJob();
            String taskName = currentTaskGroup.getTaskGroupName();
            boolean pass = getNewTaskGroupRunController(taskName).startJob(taskData);
            if (currentTaskGroup.isRequired() && !pass) { // task is required but failed
                AppScene.addLog(LogLevel.WARN, className, "Fail performing task group: " + taskName);
                break;
            }
        }
        AppScene.addLog(LogLevel.INFO, className, "Finished running operation: " + ((Operation) operationData.getMainJob()).getOperationName());
        return true;
    }

    private JobRunController getNewTaskGroupRunController(String taskName) {
        AppScene.addLog(LogLevel.TRACE, className, "Loading Task Group Run Pane");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("taskGroupRunPane.fxml"));
            Node taskRunGroup = fxmlLoader.load();
            JobRunController controller = fxmlLoader.getController();
            AppScene.addLog(LogLevel.TRACE, className, "Loaded Task Group Run Pane");
            Platform.runLater(() -> runVBox.getChildren().add(taskRunGroup));

            JobRunStructure jobRunStructure = new JobRunStructure(this, this, controller, taskName);
            controller.setJobRunStructure(jobRunStructure);
            Platform.runLater(() -> currentRunStructure.addToSideContent(jobRunStructure.getSideHBoxLabel(), jobRunStructure.getSideContent()));
            return controller;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading task run pane: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
