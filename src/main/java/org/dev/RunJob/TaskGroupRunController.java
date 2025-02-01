package org.dev.RunJob;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.Job.Task.Task;
import org.dev.Job.Task.TaskGroup;
import org.dev.Job.JobData;
import org.dev.JobRunStructure;

import java.util.List;

public class TaskGroupRunController implements JobRunController {
    @FXML
    private Node parentNode;
    @FXML
    private Label taskGroupRunNameLabel;
    @FXML
    private VBox mainTaskGroupRunVBox;

    private JobRunStructure currentRunStructure;
    private final String className = this.getClass().getSimpleName();

    @Override
    public Node getParentNode() { return parentNode; }

    @Override
    public AppLevel getAppLevel() {
        return AppLevel.TaskGroup;
    }

    @Override
    public void takeToDisplay() {
        OperationRunController parentOperationRunController = (OperationRunController) currentRunStructure.getDisplayParentController();
        parentOperationRunController.changeScrollPaneVValueView(getParentNode());
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    @Override
    public void setJobRunStructure(JobRunStructure runStructure) {
        currentRunStructure = runStructure;
    }

    @Override
    public boolean startJob(JobData jobData) {
        if (jobData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Task group data is null - cannot start");
            return false;
        }
        TaskGroup taskGroup  = (TaskGroup) jobData.getMainJob();
        if (taskGroup == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Task group is null - cannot start");
            return false;
        }
        taskGroupRunNameLabel.setText(taskGroup.getTaskGroupName());
        if (taskGroup.isDisabled())
            return true;
        return runTaskGroup(jobData);
    }

    private boolean runTaskGroup(JobData jobData) {
        AppScene.addLog(LogLevel.INFO, className, "Start running task group " + ((TaskGroup) jobData.getMainJob()).getTaskGroupName());
        boolean pass = false;
        List<JobData> taskDataList = jobData.getJobDataList();
        for (JobData taskData : taskDataList) {
            Task currentTask = (Task) taskData.getMainJob();
            if (currentTask == null)
                continue;
            String taskName = currentTask.getTaskName();
            if (pass && currentTask.isPreviousPass()) {
                AppScene.addLog(LogLevel.INFO, className, "Previous is passed, Skipping task " + taskName);
                continue;
            }
            pass = getNewTaskRunPane(taskName).startJob(taskData);
            if (!currentTask.isRequired())
                pass = true;
            else if (!pass) { // task is required but failed
                AppScene.addLog(LogLevel.INFO, className, "Fail performing task: " + taskName);
                return false;
            }
        }
        return true;
    }

    private JobRunController getNewTaskRunPane(String taskName) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("taskRunPane.fxml"));
            Node taskRunGroup = fxmlLoader.load();
            JobRunController controller = fxmlLoader.getController();
            Platform.runLater(() -> mainTaskGroupRunVBox.getChildren().add(taskRunGroup));

            JobRunStructure jobRunStructure = new JobRunStructure(currentRunStructure.getDisplayParentController(), this, controller, taskName);
            controller.setJobRunStructure(jobRunStructure);
            Platform.runLater(() -> currentRunStructure.addToSideContent(jobRunStructure.getSideHBoxLabel(), jobRunStructure.getSideContent()));
            return controller;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading task run pane: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
