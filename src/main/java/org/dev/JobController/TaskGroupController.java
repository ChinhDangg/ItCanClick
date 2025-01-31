package org.dev.JobController;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.Job.Task.TaskGroup;
import org.dev.JobData.JobData;
import org.dev.JobData.TaskGroupData;
import org.dev.JobStructure;
import org.dev.RunJob.JobRunController;
import org.dev.RunJob.TaskGroupRunController;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TaskGroupController implements Initializable, JobDataController {
    @FXML
    private Node parentNode;
    @FXML
    private Label taskIndexLabel;
    @FXML
    private VBox taskGroupVBox;
    @FXML
    private TextField renameTextField;
    @FXML
    private CheckBox requiredCheckBox, disabledCheckBox;

    private JobStructure currentStructure;

    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        renameTextField.focusedProperty().addListener((_, _, newValue) -> {
            if (!newValue) {
                //System.out.println("TextField lost focus");
                changeTaskGroupName();
            }
        });
    }

    public void setJobStructure(JobStructure structure) {
        currentStructure = structure;
    }

    public void setTaskIndex(int taskIndex) { taskIndexLabel.setText(Integer.toString(taskIndex)); }

    private void changeTaskGroupName() {
        String name = renameTextField.getText();
        name = name.strip();
        if (name.isBlank()) {
            renameTextField.setText(currentStructure.getName());
            return;
        }
        updateTaskGroupName(name);
    }
    private void updateTaskGroupName(String name) {
        currentStructure.changeName(name);
        renameTextField.setText(name);
        AppScene.addLog(LogLevel.DEBUG, className, "Updated task group name: " + name);
    }

    // ------------------------------------------------------
    @Override
    public boolean isSet() {
        if (currentStructure == null)
            return false;
        return !currentStructure.getSubJobStructures().isEmpty()
                && currentStructure.getSubJobStructures().getFirst().getCurrentController().isSet();
    }

    @Override
    public String getName() { return renameTextField.getText(); }

    @Override
    public Node getParentNode() { return parentNode; }

    @Override
    public AppLevel getAppLevel() { return AppLevel.TaskGroup; }

    @Override
    public void takeToDisplay() {
        OperationController parentOperationController = (OperationController) currentStructure.getParentController();
        parentOperationController.takeToDisplay();
        parentOperationController.changeOperationScrollPaneView(getParentNode());
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    @Override
    public TaskGroupData getSavedData() {
        TaskGroupData taskGroupData = new TaskGroupData();
        TaskGroup taskGroup = new TaskGroup(currentStructure.getName(), requiredCheckBox.isSelected(), disabledCheckBox.isSelected());
        taskGroupData.setTaskGroup(taskGroup);
        List<JobData> taskDataList = new ArrayList<>();
        for (JobStructure subJobStructure: currentStructure.getSubJobStructures())
            taskDataList.add(subJobStructure.getCurrentController().getSavedData());
        taskGroupData.setTaskDataList(taskDataList);
        AppScene.addLog(LogLevel.TRACE, className, "Got task group data");
        return taskGroupData;
    }

    @Override
    public void loadSavedData(JobData jobData) {
        if (jobData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Operation data is null - cannot load from save");
            return;
        }
        TaskGroupData taskGroupData = (TaskGroupData) jobData;
        TaskGroup taskGroup = (TaskGroup) taskGroupData.getTaskGroup();
        requiredCheckBox.setSelected(taskGroup.isRequired());
        disabledCheckBox.setSelected(taskGroup.isDisabled());
        updateTaskGroupName(taskGroup.getTaskGroupName());
        for (JobData taskData : taskGroupData.getTaskDataList())
            addSavedData(taskData);
    }

    @Override
    public void addSavedData(JobData taskData) {
        if (AppScene.isJobRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Another job is running - cannot modify");
            return;
        }
        if (!currentStructure.getSubJobStructures().isEmpty() && currentStructure.getSubJobStructures().getLast().getCurrentController().isSet()) {
            AppScene.addLog(LogLevel.INFO, className, "Recent Minimized Task is not set");
            return;
        }
        try {
            AppScene.addLog(LogLevel.TRACE, className, "Loading Minimized Task Pane");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("minimizedTaskPane.fxml"));
            Node taskPane = loader.load();
            MinimizedTaskController controller = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Minimized Task Pane");
            if (taskData != null)
                controller.loadSavedData(taskData);
            int numberOfTask = currentStructure.getSubStructureSize();
            if (numberOfTask == 0)
                controller.disablePreviousOption();
            taskGroupVBox.getChildren().add(taskPane);

            JobStructure taskStructure = new JobStructure(currentStructure.getDisplayParentController(), this, controller, controller.getName());
            controller.setJobStructure(taskStructure);
            currentStructure.addSubJobStructure(taskStructure);
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading and adding minimized task pane: " + e.getMessage());
        }
    }

    @Override
    public void removeSavedData(JobStructure jobStructure) {
        int removeIndex = currentStructure.removeSubJobStructure(jobStructure);
        taskGroupVBox.getChildren().remove(removeIndex);
        if (removeIndex == 0)
            ((MinimizedTaskController) currentStructure.getSubJobStructures().getFirst().getCurrentController()).disablePreviousOption();
        AppScene.addLog(LogLevel.DEBUG, className, "Removed selected task: " + removeIndex);
    }

    @Override
    public void moveSavedDataUp(JobStructure jobStructure) {
        int numberOfTasks = jobStructure.getSubStructureSize();
        if (numberOfTasks < 2)
            return;
        int selectedTaskIndex = jobStructure.getSubStructureIndex(jobStructure);
        if (selectedTaskIndex == 0)
            return;
        int changeIndex = selectedTaskIndex -1;
        updateTaskPaneList(selectedTaskIndex, changeIndex);
        updateTaskPreviousOption(changeIndex);
        currentStructure.updateSubJobStructure(jobStructure, changeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Moved up Task: " + changeIndex);
    }

    @Override
    public void moveSavedDataDown(JobStructure jobStructure) {
        int numberOfTasks = jobStructure.getSubStructureSize();
        if (numberOfTasks < 2)
            return;
        int selectedTaskIndex = jobStructure.getSubStructureIndex(jobStructure);
        int changeIndex = selectedTaskIndex +1;
        if (changeIndex == numberOfTasks)
            return;
        updateTaskPaneList(selectedTaskIndex, changeIndex);
        updateTaskPreviousOption(changeIndex);
        currentStructure.updateSubJobStructure(jobStructure, changeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Moved down task group: " + changeIndex);
    }

    private void updateTaskPaneList(int selectedIndex, int changeIndex) {
        ObservableList<Node> children = taskGroupVBox.getChildren();
        Node minimizedTaskNode = children.get(selectedIndex);
        children.remove(minimizedTaskNode);
        children.add(changeIndex, minimizedTaskNode);
    }
    private void updateTaskPreviousOption(int index) {
        if (index < 2) {
            ((MinimizedTaskController) currentStructure.getSubJobStructures().get(0).getCurrentController()).disablePreviousOption();
            ((MinimizedTaskController) currentStructure.getSubJobStructures().get(1).getCurrentController()).enablePreviousOption();
        }
    }

    @Override
    public JobRunController getRunJob() {
        try {
            FXMLLoader loader = new FXMLLoader(AppScene.class.getResource("RunJob/taskGroupRunPane.fxml"));
            loader.load();
            TaskGroupRunController taskGroupRunController = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Task Group Run");
            return taskGroupRunController;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading Task Group Run Pane: " + e.getMessage());
            return null;
        }
    }

}
