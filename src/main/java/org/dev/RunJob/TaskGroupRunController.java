package org.dev.RunJob;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.NonNull;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.Job.Task.Task;
import org.dev.Job.Task.TaskGroup;
import org.dev.JobController.MainJobController;
import org.dev.JobData.JobData;
import org.dev.JobData.TaskData;
import org.dev.JobData.TaskGroupData;
import org.dev.SideMenu.LeftMenu.SideMenuController;

import java.util.List;

public class TaskGroupRunController implements JobRunController {
    @FXML
    private Node parentNode;
    @FXML
    private Label taskGroupRunNameLabel;
    @FXML
    private VBox mainTaskGroupRunVBox;

    @Getter
    private VBox taskGroupRunSideContent = new VBox();
    private final String className = this.getClass().getSimpleName();

    @Override
    public Node getParentNode() { return parentNode; }

    @Override
    public AppLevel getAppLevel() {
        return AppLevel.TaskGroup;
    }

    @Override
    public void takeToDisplay(@NonNull MainJobController parentController) {
        OperationRunController parentOperationRunController = (OperationRunController) parentController;
        parentOperationRunController.changeScrollPaneVValueView(getParentNode());
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    @Override
    public boolean startJob(JobData jobData) {
        if (jobData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Task group data is null - cannot start");
            return false;
        }
        TaskGroupData taskGroupData = (TaskGroupData) jobData;
        TaskGroup taskGroup  = taskGroupData.getTaskGroup();
        if (taskGroup == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Task group is null - cannot start");
            return false;
        }
        taskGroupRunNameLabel.setText(taskGroup.getTaskGroupName());
        if (taskGroup.isDisabled())
            return true;
        return runTaskGroup(taskGroupData);
    }

    private boolean runTaskGroup(TaskGroupData taskGroupData) {
        AppScene.addLog(LogLevel.INFO, className, "Start running task group " + taskGroupData.getTaskGroup().getTaskGroupName());
        boolean pass = false;
        List<TaskData> taskDataList = taskGroupData.getTaskDataList();
        for (TaskData taskData : taskDataList) {
            Task currentTask = taskData.getTask();
            if (currentTask == null)
                continue;
            String taskName = currentTask.getTaskName();
            if (pass && currentTask.isPreviousPass()) {
                AppScene.addLog(LogLevel.INFO, className, "Previous is passed, Skipping task " + taskName);
                continue;
            }
            loadAndAddNewTaskRunPane(currentTask.getTaskName());
            pass = currentTaskRunController.startJob(taskData);
            if (!currentTask.isRequired())
                pass = true;
            else if (!pass) { // task is required but failed
                AppScene.addLog(LogLevel.INFO, className, "Fail performing task: " + taskName);
                return false;
            }
        }
        return true;
    }

    private TaskRunController currentTaskRunController;
    private void loadAndAddNewTaskRunPane(String taskName) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("taskRunPane.fxml"));
            Node taskRunGroup = fxmlLoader.load();
            currentTaskRunController = fxmlLoader.getController();
            VBox taskRunSideContent = currentTaskRunController.getTaskRunSideContent();
            Node taskRunHBoxLabel = SideMenuController.getNewSideHBoxLabel(
                    new Label(taskName), taskRunSideContent, currentTaskRunController);
            // update side hierarchy
            Platform.runLater(() -> taskGroupRunSideContent.getChildren().addAll(taskRunHBoxLabel, taskRunSideContent));
            Platform.runLater(() -> mainTaskGroupRunVBox.getChildren().add(taskRunGroup));
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading task run pane: " + e.getMessage());
        }
    }
}
