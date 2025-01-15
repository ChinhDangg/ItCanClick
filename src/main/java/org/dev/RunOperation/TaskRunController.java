package org.dev.RunOperation;

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
import org.dev.Operation.Action.Action;
import org.dev.Operation.Data.ActionData;
import org.dev.Operation.Data.TaskData;
import org.dev.Operation.MainJobController;
import org.dev.Operation.Task.Task;
import org.dev.SideMenu.SideMenuController;
import java.io.IOException;
import java.util.List;

public class TaskRunController extends RunActivity implements MainJobController {

    @FXML
    private Group mainTaskRunGroup;
    @FXML
    private Label taskRunNameLabel;
    @FXML
    private VBox mainTaskRunVBox;

    @Getter
    private VBox taskRunVBoxSideContent = new VBox();
    private final String className = this.getClass().getSimpleName();

    @Override
    public void takeToDisplay() {
        AppScene.currentLoadedOperationRunController.changeScrollPaneVValueView(mainTaskRunGroup);
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    @Override
    public AppLevel getAppLevel() {
        return AppLevel.Task;
    }

    public void changeTaskRunName(String newName) {
        taskRunNameLabel.setText(newName);
    }

    // ------------------------------------------------------
    public boolean startTask(TaskData taskData) throws InterruptedException {
        if (taskData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Task data is null - cannot start");
            return false;
        }
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
                if (!runTask(taskData.getActionDataList(), taskName))
                    return false;
            return true;
        }
        for (int j = -1; j < repeatNumber; j++)
            if (!runTask(taskData.getActionDataList(), taskName))
                return false;
        return true;
    }

    private boolean runTask(List<ActionData> actionDataList, String taskName) throws InterruptedException {
        AppScene.addLog(LogLevel.INFO, className, "Start running task: " + taskName);
        boolean pass = false;
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
            AppScene.addLog(LogLevel.INFO, className, "Start running action: " + actionName);
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
            currentActionRunController.changeActionRunName(actionName);
            VBox actionRunSideContent = currentActionRunController.getActionRunSideContent();
            Node actionRunHBoxLabel = SideMenuController.getNewSideHBoxLabel(
                    new Label(actionName), actionRunSideContent, currentActionRunController);
            // update side hierarchy
            Platform.runLater(() -> taskRunVBoxSideContent.getChildren().addAll(actionRunHBoxLabel, actionRunSideContent));
            Platform.runLater(() -> mainTaskRunVBox.getChildren().add(actionRunPaneGroup));
        } catch (IOException e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading action run pane: " + e.getMessage());
        }
    }
}
