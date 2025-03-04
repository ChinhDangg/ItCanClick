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
import org.dev.Job.Action.Action;
import org.dev.Job.JobData;
import org.dev.Job.Task.Task;
import org.dev.jobManagement.JobRunStructure;

import java.io.IOException;
import java.util.List;

public class TaskRunController implements JobRunController<Boolean> {
    @FXML
    private Node parentNode;
    @FXML
    private Node containerPane;
    @FXML
    private Label requireLabel, taskRunNameLabel;
    @FXML
    private VBox mainTaskRunVBox;

    private JobRunStructure currentRunStructure;
    private final String className = this.getClass().getSimpleName();

    private void loadScale() {
        if (currentRunStructure.getParentController() != null)
            return;
        double currentGlobalScale = 1.0;
        if (currentGlobalScale != AppScene.currentGlobalScale) {
            currentGlobalScale = AppScene.currentGlobalScale;
            containerPane.getTransforms().add(new Scale(currentGlobalScale, currentGlobalScale, 0, 0));
        }
    }

    @Override
    public Node getParentNode() { return parentNode; }

    @Override
    public AppLevel getAppLevel() {
        return AppLevel.Task;
    }

    @Override
    public void takeToDisplay() {
        AppScene.updateMainDisplayScrollValue(getParentNode());
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    public void changeTaskRunName(String newName) {
        Platform.runLater(() -> taskRunNameLabel.setText(newName));
    }

    // ------------------------------------------------------
    @Override
    public void setJobRunStructure(JobRunStructure runStructure) {
        currentRunStructure = runStructure;
        loadScale();
    }

    @Override
    public Boolean startJob(JobData jobData) {
        if (jobData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Task data is null - cannot start");
            return false;
        }
        Task currentTask = (Task) jobData.getMainJob();
        if (currentTask == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Task is null - cannot start");
            return false;
        }
        String taskName = currentTask.getTaskName();
        changeTaskRunName(taskName);
        requireLabel.setText(currentTask.isRequired() ? "Required" : "Optional");

        int repeatNumber = currentTask.getRepeatNumber();
        if (repeatNumber == -1) {
            AppScene.addLog(LogLevel.INFO, className, "Task is set to run Infinitely");
            for (int j = 0; j < Integer.MAX_VALUE; j++)
                if (!runTask(jobData))
                    return false;
            return true;
        }
        for (int j = -1; j < repeatNumber; j++)
            if (!runTask(jobData))
                return false;
        return true;
    }

    private boolean runTask(JobData jobData) {
        AppScene.addLog(LogLevel.INFO, className, "Start running task: " + ((Task) jobData.getMainJob()).getTaskName());
        boolean pass = true;
        List<JobData> actionDataList = jobData.getJobDataList();
        for (JobData actionData : actionDataList) {
            if (actionData == null)
                continue;
            Action currentAction = (Action) actionData.getMainJob();
            if (currentAction == null)
                continue;
            String actionName = currentAction.getActionName();
            JobRunController<Boolean> actionRunController = getNewActionRunPane(actionName);
            if (pass && currentAction.isPreviousPass()) {
                AppScene.addLog(LogLevel.INFO, className, "Skipping action as previous is passed: " + actionName);
                continue;
            }
            else if (!pass && !currentAction.isPreviousPass()) {
                AppScene.addLog(LogLevel.INFO, className, "Fail performing action: " + actionName);
                return false;
            }
            pass = actionRunController.startJob(actionData);
            if (!currentAction.isRequired())
                pass = true;
        }
        if (!pass) {
            AppScene.addLog(LogLevel.INFO, className, "Fail performing last action:");
            return false;
        }
        AppScene.addLog(LogLevel.INFO, className, "Finished running task: " + ((Task) jobData.getMainJob()).getTaskName());
        return true;
    }

    private JobRunController<Boolean> getNewActionRunPane(String actionName) {
        AppScene.addLog(LogLevel.TRACE, className, "Loading Action Run Pane");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("actionRunPane.fxml"));
            Node actionRunPaneGroup = fxmlLoader.load();
            JobRunController<Boolean> controller = fxmlLoader.getController();
            AppScene.addLog(LogLevel.TRACE, className, "Loaded Action Pane");
            Platform.runLater(() -> mainTaskRunVBox.getChildren().add(actionRunPaneGroup));

            JobRunStructure jobRunStructure = new JobRunStructure(currentRunStructure.getDisplayParentController(), this, controller, actionName);
            controller.setJobRunStructure(jobRunStructure);
            currentRunStructure.addSubJobRunStructure(jobRunStructure);
            return controller;
        } catch (IOException e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading action run pane: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
