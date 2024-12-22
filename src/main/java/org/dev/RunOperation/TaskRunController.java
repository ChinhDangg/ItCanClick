package org.dev.RunOperation;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.dev.AppScene;
import org.dev.Operation.Action.Action;
import org.dev.Operation.Data.ActionData;
import org.dev.Operation.Data.TaskData;
import org.dev.Operation.MainJobController;
import org.dev.Operation.Task.Task;
import org.dev.LeftSideMenu.SideMenuController;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TaskRunController extends RunActivity implements Initializable, MainJobController {

    @FXML
    private Group mainTaskRunGroup;
    @FXML @Getter
    private Label taskRunNameLabel;
    @FXML @Getter
    private VBox mainTaskRunVBox;

    @Getter
    private VBox actionRunVBoxSideContent = new VBox();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        actionRunVBoxSideContent.setPadding(new Insets(0, 0, 0, 15));
    }

    @Override
    public void takeToDisplay() {
        AppScene.currentLoadedOperationRunController.takeToDisplay();
        System.out.println("Task Run take To Display");
        changeScrollPaneVValueView(AppScene.currentLoadedOperationRunController.getOperationRunScrollPane(), null, mainTaskRunGroup);
    }

    private void changeTaskRunName(String newName) {
        taskRunNameLabel.setText(newName);
    }

    // ------------------------------------------------------
    public boolean startTask(TaskData taskData) throws InterruptedException {
        if (taskData == null) {
            System.out.println("Task Data not found in run task - bug");
            return false;
        }
        Task currentTask = taskData.getTask();
        if (currentTask == null) {
            System.out.println("Task not found in run task - bug");
            return false;
        }
        String taskName = currentTask.getTaskName();
        changeTaskRunName(taskName);

        int repeatNumber = currentTask.getRepeatNumber();
        if (repeatNumber == -1) {
            System.out.println("Running task 'infinitely'");
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
        System.out.println("Start running task: " + taskName);
        boolean pass = false;
        for (ActionData actionData : actionDataList) {
            Action currentAction = actionData.getAction();
            if (currentAction == null)
                continue;
            String actionName = currentAction.getActionName();
            if (pass && currentAction.isPreviousPass()) {
                System.out.println("Skipping action " + actionName + " as previous is passed");
                continue;
            }
            loadAndAddNewActionRunPane();
            System.out.println("Start running action: " + actionName);
            pass = currentActionRunController.startAction(actionData);
            if (!currentAction.isRequired())
                pass = true;
            else if (!pass) { // action is required but failed
                System.out.println("Fail performing action: " + actionName);
                return false;
            }
        }
        return true;
    }

    private ActionRunController currentActionRunController;
    private void loadAndAddNewActionRunPane() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("actionRunPane.fxml"));
            Group actionRunPaneGroup = fxmlLoader.load();
            currentActionRunController = fxmlLoader.getController();
            VBox conditionRunVBox = currentActionRunController.getConditionRunVBoxSideContent();
            HBox actionRunLabelHBox = SideMenuController.getDropDownHBox(conditionRunVBox,
                    new Label(currentActionRunController.getActionRunNameLabel().getText()),
                    currentActionRunController);
            // update side hierarchy
            Platform.runLater(() -> actionRunVBoxSideContent.getChildren().add(new VBox(actionRunLabelHBox, conditionRunVBox)));
            Platform.runLater(() -> mainTaskRunVBox.getChildren().add(actionRunPaneGroup));
        } catch (IOException e) {
            System.out.println("Fail to load action run pane");
        }
    }
}
