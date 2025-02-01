package org.dev.RunJob;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.Job.Action.Action;
import org.dev.Job.JobData;
import org.dev.Job.Task.Task;
import org.dev.JobRunStructure;
import java.io.IOException;
import java.util.List;

public class TaskRunController implements JobRunController {

    @FXML
    private Group mainTaskRunGroup;
    @FXML
    private Label taskRunNameLabel;
    @FXML
    private VBox mainTaskRunVBox;

    private JobRunStructure currentRunStructure;

    @Getter
    private VBox taskRunSideContent = new VBox();
    private final String className = this.getClass().getSimpleName();

    @Override
    public Node getParentNode() { return mainTaskRunVBox; }

    @Override
    public AppLevel getAppLevel() {
        return AppLevel.Task;
    }

    @Override
    public void takeToDisplay() {
        OperationRunController parentOperationRunController = (OperationRunController) currentRunStructure.getDisplayParentController();
        parentOperationRunController.changeScrollPaneVValueView(getParentNode());
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    public void changeTaskRunName(String newName) {
        taskRunNameLabel.setText(newName);
    }

    // ------------------------------------------------------
    @Override
    public void setJobRunStructure(JobRunStructure runStructure) {
        currentRunStructure = runStructure;
    }

    @Override
    public boolean startJob(JobData jobData) {
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
        boolean pass = false;
        List<JobData> actionDataList = jobData.getJobDataList();
        for (JobData actionData : actionDataList) {
            Action currentAction = (Action) actionData.getMainJob();
            if (currentAction == null)
                continue;
            String actionName = currentAction.getActionName();
            if (pass && currentAction.isPreviousPass()) {
                AppScene.addLog(LogLevel.INFO, className, "Skipping action as previous is passed: " + actionName);
                continue;
            }
            pass = getNewActionRunPane(actionName).startJob(actionData);
            if (!currentAction.isRequired())
                pass = true;
            else if (!pass) { // action is required but failed
                AppScene.addLog(LogLevel.DEBUG, className, "Fail performing action: " + actionName);
                return false;
            }
        }
        return true;
    }

    private JobRunController getNewActionRunPane(String actionName) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("actionRunPane.fxml"));
            Node actionRunPaneGroup = fxmlLoader.load();
            JobRunController controller = fxmlLoader.getController();
            Platform.runLater(() -> mainTaskRunVBox.getChildren().add(actionRunPaneGroup));

            JobRunStructure jobRunStructure = new JobRunStructure(currentRunStructure.getDisplayParentController(), this, controller, actionName);
            controller.setJobRunStructure(jobRunStructure);
            Platform.runLater(() -> currentRunStructure.addToSideContent(jobRunStructure.getSideHBoxLabel(), jobRunStructure.getSideContent()));
            return controller;
        } catch (IOException e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading action run pane: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
