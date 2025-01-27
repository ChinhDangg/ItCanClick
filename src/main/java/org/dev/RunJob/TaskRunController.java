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
import org.dev.JobData.ActionData;
import org.dev.JobData.JobData;
import org.dev.JobData.TaskData;
import org.dev.JobController.MainJobController;
import org.dev.Job.Task.Task;
import org.dev.SideMenu.LeftMenu.SideMenuController;
import java.io.IOException;
import java.util.List;

public class TaskRunController implements JobRunController {

    @FXML
    private Group mainTaskRunGroup;
    @FXML
    private Label taskRunNameLabel;
    @FXML
    private VBox mainTaskRunVBox;

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
        AppScene.currentLoadedOperationRunController.changeScrollPaneVValueView(mainTaskRunGroup);
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    public void changeTaskRunName(String newName) {
        taskRunNameLabel.setText(newName);
    }

    // ------------------------------------------------------
    @Override
    public boolean startJob(JobData jobData) {
        if (jobData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Task data is null - cannot start");
            return false;
        }
        TaskData taskData = (TaskData) jobData;
        Task currentTask = taskData.getTask();
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
                if (!runTask(taskData))
                    return false;
            return true;
        }
        for (int j = -1; j < repeatNumber; j++)
            if (!runTask(taskData))
                return false;
        return true;
    }

    private boolean runTask(TaskData taskData) {
        AppScene.addLog(LogLevel.INFO, className, "Start running task: " + taskData.getTask().getTaskName());
        boolean pass = false;
        List<ActionData> actionDataList = taskData.getActionDataList();
        for (ActionData actionData : actionDataList) {
            Action currentAction = actionData.getAction();
            if (currentAction == null)
                continue;
            String actionName = currentAction.getActionName();
            if (pass && currentAction.isPreviousPass()) {
                AppScene.addLog(LogLevel.INFO, className, "Skipping action as previous is passed: " + actionName);
                continue;
            }
            loadAndAddNewActionRunPane(currentAction.getActionName());
            pass = currentActionRunController.startAction(actionData);
            if (!currentAction.isRequired())
                pass = true;
            else if (!pass) { // action is required but failed
                AppScene.addLog(LogLevel.DEBUG, className, "Fail performing action: " + actionName);
                return false;
            }
        }
        return true;
    }

    private ActionRunController currentActionRunController;
    private void loadAndAddNewActionRunPane(String actionName) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("actionRunPane.fxml"));
            Node actionRunPaneGroup = fxmlLoader.load();
            currentActionRunController = fxmlLoader.getController();
            VBox actionRunSideContent = currentActionRunController.getActionRunSideContent();
            Node actionRunHBoxLabel = SideMenuController.getNewSideHBoxLabel(
                    new Label(actionName), actionRunSideContent, currentActionRunController);
            // update side hierarchy
            Platform.runLater(() -> taskRunSideContent.getChildren().addAll(actionRunHBoxLabel, actionRunSideContent));
            Platform.runLater(() -> mainTaskRunVBox.getChildren().add(actionRunPaneGroup));
        } catch (IOException e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading action run pane: " + e.getMessage());
        }
    }
}
