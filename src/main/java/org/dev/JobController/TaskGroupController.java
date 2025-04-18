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
import org.dev.Job.JobData;
import org.dev.jobManagement.JobStructure;
import org.dev.RunJob.JobRunController;
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
    private JobData jobData = new JobData();
    private final TaskGroup taskGroup = new TaskGroup();

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
        taskGroup.setTaskGroupName(name);
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
        parentOperationController.selectTheTaskPane(getParentNode());
        AppScene.updateMainDisplayScrollValue(getParentNode());
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    @Override
    public JobData getSavedData() {
        taskGroup.setRequired(requiredCheckBox.isSelected());
        taskGroup.setDisabled(disabledCheckBox.isSelected());
        List<JobData> taskDataList = new ArrayList<>();
        for (JobStructure subJobStructure: currentStructure.getSubJobStructures())
            taskDataList.add(subJobStructure.getCurrentController().getSavedData());
        JobData taskGroupData = new JobData(taskGroup.cloneData(), taskDataList);
        AppScene.addLog(LogLevel.TRACE, className, "Got Task Group data");
        return taskGroupData;
    }

    @Override
    public JobData getSavedDataByReference() {
        taskGroup.setRequired(requiredCheckBox.isSelected());
        taskGroup.setDisabled(disabledCheckBox.isSelected());
        List<JobData> taskDataList = new ArrayList<>();
        for (JobStructure subJobStructure: currentStructure.getSubJobStructures())
            taskDataList.add(subJobStructure.getCurrentController().getSavedDataByReference());
        jobData.setMainJob(taskGroup.cloneData());
        jobData.setJobDataList(taskDataList);
        AppScene.addLog(LogLevel.TRACE, className, "Got Reference Task Group data");
        return jobData;
    }

    @Override
    public void loadSavedData(JobData newJobData) {
        if (newJobData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail -task group data is null - cannot load from save");
            return;
        }
        if (newJobData.isRef())
            AppScene.addKnownJobReference(newJobData, currentStructure);
        jobData = newJobData;
        TaskGroup taskGroup = (TaskGroup) jobData.getMainJob();
        requiredCheckBox.setSelected(taskGroup.isRequired());
        disabledCheckBox.setSelected(taskGroup.isDisabled());
        updateTaskGroupName(taskGroup.getTaskGroupName());
        for (JobData taskData : jobData.getJobDataList())
            addSavedData(taskData);
    }

    @Override
    public JobStructure addSavedData(JobData taskData) {
        if (AppScene.isJobRunning()) {
            AppScene.addLog(LogLevel.INFO, className, "Another job is running - cannot modify");
            return null;
        }
        if (taskData == null)
            if (!currentStructure.getSubJobStructures().isEmpty() && !currentStructure.getSubJobStructures().getLast().getCurrentController().isSet()) {
                AppScene.addLog(LogLevel.INFO, className, "Recent Minimized Task is not set");
                return null;
            }
        try {
            AppScene.addLog(LogLevel.TRACE, className, "Loading Minimized Task Pane");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("minimizedTaskPane.fxml"));
            Node taskPane = loader.load();
            MinimizedTaskController controller = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Minimized Task Pane");
            int numberOfTask = currentStructure.getSubStructureSize();
            if (numberOfTask == 0)
                controller.disablePreviousOption();
            taskGroupVBox.getChildren().add(taskPane);

            JobStructure taskStructure = new JobStructure(currentStructure.getDisplayParentController(), this, controller, controller.getName());
            controller.setJobStructure(taskStructure);
            currentStructure.addSubJobStructure(taskStructure);

            if (taskData != null)
                controller.loadSavedData(taskData);
            else
                controller.addSavedData(null);

            return taskStructure;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading and adding minimized task pane: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void removeSavedData(JobDataController jobDataController) {
        int removeIndex = currentStructure.removeSubJobStructure(jobDataController);
        taskGroupVBox.getChildren().remove(removeIndex);
        if (removeIndex == 0 && currentStructure.getSubStructureSize() != 0)
            ((MinimizedTaskController) currentStructure.getSubJobStructures().getFirst().getCurrentController()).disablePreviousOption();
        AppScene.addLog(LogLevel.DEBUG, className, "Removed selected task: " + removeIndex);
    }

    @Override
    public void moveSavedDataUp(JobDataController jobDataController) {
        int numberOfTasks = currentStructure.getSubStructureSize();
        if (numberOfTasks < 2)
            return;
        int selectedTaskIndex = currentStructure.getSubStructureIndex(jobDataController);
        if (selectedTaskIndex == 0)
            return;
        int changeIndex = selectedTaskIndex -1;
        updateTaskPaneList(selectedTaskIndex, changeIndex);
        currentStructure.updateSubJobStructure(jobDataController, changeIndex);
        updateTaskPreviousOption(changeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Moved up Task: " + changeIndex);
    }

    @Override
    public void moveSavedDataDown(JobDataController jobDataController) {
        int numberOfTasks = currentStructure.getSubStructureSize();
        if (numberOfTasks < 2)
            return;
        int selectedTaskIndex = currentStructure.getSubStructureIndex(jobDataController);
        int changeIndex = selectedTaskIndex +1;
        if (changeIndex == numberOfTasks)
            return;
        updateTaskPaneList(selectedTaskIndex, changeIndex);
        currentStructure.updateSubJobStructure(jobDataController, changeIndex);
        updateTaskPreviousOption(changeIndex);
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
    public JobRunController<Object> getRunJob() {
        try {
            FXMLLoader loader = new FXMLLoader(AppScene.class.getResource("RunJob/taskGroupRunPane.fxml"));
            loader.load();
            JobRunController<Object> taskGroupRunController = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Task Group Run");
            return taskGroupRunController;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading Task Group Run Pane: " + e.getMessage());
            return null;
        }
    }

}
