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
import lombok.Getter;
import lombok.NonNull;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.Job.Task.TaskGroup;
import org.dev.JobData.JobData;
import org.dev.JobData.TaskData;
import org.dev.JobData.TaskGroupData;
import org.dev.JobStructure;
import org.dev.RunJob.JobRunController;
import org.dev.RunJob.TaskGroupRunController;
import org.dev.SideMenu.LeftMenu.SideMenuController;
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

    @Getter
    private JobStructure jobStructure;

    @Getter
    private final List<MinimizedTaskController> minimizedTaskList = new ArrayList<>();
    @Getter
    private VBox taskGroupSideContent = new VBox();
    @Getter
    private Label taskGroupNameLabel = new Label();
    private final String className = this.getClass().getSimpleName();
    private TaskGroup taskGroup = new TaskGroup();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        taskGroupNameLabel.setText(renameTextField.getText());
        renameTextField.focusedProperty().addListener((_, _, newValue) -> {
            if (!newValue) {
                //System.out.println("TextField lost focus");
                changeTaskGroupName();
            }
        });
    }

    public void setTaskIndex(int taskIndex) { taskIndexLabel.setText(Integer.toString(taskIndex)); }

    private void changeTaskGroupName() {
        String name = renameTextField.getText();
        name = name.strip();
        if (name.isBlank()) {
            renameTextField.setText(taskGroupNameLabel.getText());
            return;
        }
        updateTaskGroupName(name);
    }
    private void updateTaskGroupName(String name) {
        taskGroupNameLabel.setText(name);
        renameTextField.setText(name);
        AppScene.addLog(LogLevel.DEBUG, className, "Updated task group name: " + name);
    }

    // ------------------------------------------------------
    @Override
    public boolean isSet() { return !minimizedTaskList.isEmpty() && minimizedTaskList.getFirst().isSet(); }

    @Override
    public Node getParentNode() { return parentNode; }

    @Override
    public AppLevel getAppLevel() { return AppLevel.TaskGroup; }

    @Override
    public void takeToDisplay(@NonNull MainJobController parentController) {
        OperationController parentOperationController = (OperationController) parentController;
        parentOperationController.takeToDisplay(null);
        parentOperationController.changeOperationScrollPaneView(getParentNode());
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    @Override
    public TaskGroupData getSavedData() {
        TaskGroupData taskGroupData = new TaskGroupData();
        taskGroup.setDisabled(disabledCheckBox.isSelected());
        taskGroup.setRequired(requiredCheckBox.isSelected());
        taskGroup.setTaskGroupName(taskGroupNameLabel.getText());
        taskGroupData.setTaskGroup(taskGroup.getDeepCopied());
        List<TaskData> taskDataList = new ArrayList<>();
        for (MinimizedTaskController taskController : minimizedTaskList)
            taskDataList.add(taskController.getSavedData());
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
        this.taskGroup = taskGroupData.getTaskGroup();
        requiredCheckBox.setSelected(taskGroup.isRequired());
        disabledCheckBox.setSelected(taskGroup.isDisabled());
        updateTaskGroupName(taskGroup.getTaskGroupName());
        for (TaskData taskData : taskGroupData.getTaskDataList())
            addSavedData(taskData);
    }

    @Override
    public void addSavedData(JobData taskData) {
        try {
            AppScene.addLog(LogLevel.TRACE, className, "Loading Minimized Task Pane");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("minimizedTaskPane.fxml"));
            Node taskPane = loader.load();
            MinimizedTaskController controller = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Minimized Task Pane");
            if (taskData != null)
                controller.loadSavedData(taskData);
            int numberOfTask = minimizedTaskList.size();
            if (numberOfTask == 0)
                controller.disablePreviousOption();
            taskGroupVBox.getChildren().add(taskPane);
            minimizedTaskList.add(controller);
            createTaskGroupSideContent(controller);
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading and adding minimized task pane: " + e.getMessage());
        }
    }

    @Override
    public void removeSavedData(JobDataController jobDataController) {
        int changeIndex = minimizedTaskList.indexOf((MinimizedTaskController) jobDataController);
        removeTask(changeIndex);
    }
    private void removeTask(int changeIndex) {
        minimizedTaskList.remove(changeIndex);
        if (changeIndex == 0 && !minimizedTaskList.isEmpty())
            minimizedTaskList.getFirst().disablePreviousOption();
        taskGroupVBox.getChildren().remove(changeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Removed selected task: " + changeIndex);
        // update side menu
        removeTaskGroupSideContent(changeIndex);
    }

    @Override
    public void moveSavedDataUp(JobDataController jobDataController) {
        int numberOfTasks = minimizedTaskList.size();
        if (numberOfTasks < 2)
            return;
        int selectedTaskIndex = minimizedTaskList.indexOf((MinimizedTaskController) jobDataController);
        if (selectedTaskIndex == 0)
            return;
        int changeIndex = selectedTaskIndex -1;
        updateTaskPaneList(selectedTaskIndex, changeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Moved up task group: " + changeIndex);
        //update side menu
        updateTaskGroupSideContent(selectedTaskIndex, changeIndex);
    }

    @Override
    public void moveSavedDataDown(JobDataController jobDataController) {
        int numberOfTasks = minimizedTaskList.size();
        if (numberOfTasks < 2)
            return;
        int selectedTaskIndex = minimizedTaskList.indexOf((MinimizedTaskController) jobDataController);
        int changeIndex = selectedTaskIndex +1;
        if (changeIndex == numberOfTasks)
            return;
        updateTaskPaneList(selectedTaskIndex, changeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Moved down task group: " + changeIndex);
        //update side menu
        updateTaskGroupSideContent(selectedTaskIndex, changeIndex);
    }

    private void updateTaskPaneList(int selectedIndex, int changeIndex) {
        ObservableList<Node> children = taskGroupVBox.getChildren();
        Node minimizedTaskNode = children.get(selectedIndex);
        minimizedTaskList.add(changeIndex, minimizedTaskList.remove(selectedIndex));
        updateTaskPreviousOption(changeIndex);
        children.remove(minimizedTaskNode);
        children.add(changeIndex, minimizedTaskNode);
    }
    private void updateTaskPreviousOption(int index) {
        if (index < 2) {
            minimizedTaskList.get(0).disablePreviousOption();
            minimizedTaskList.get(1).enablePreviousOption();
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

    // ------------------------------------------------------
    private void createTaskGroupSideContent(MinimizedTaskController controller) {
        VBox taskSideContent = controller.getTaskController().getTaskSideContent();
        Node taskLabelHBox = SideMenuController.getNewSideHBoxLabel(controller.getTaskNameLabel(), taskSideContent, controller, this);
        taskGroupSideContent.getChildren().add(new VBox(taskLabelHBox, taskSideContent));
        AppScene.addLog(LogLevel.TRACE, className, "Created task group side content");
    }
    private void removeTaskGroupSideContent(int changeIndex) {
        taskGroupSideContent.getChildren().remove(changeIndex);
    }
    private void updateTaskGroupSideContent(int selectedIndex, int changeIndex) {
        ObservableList<Node> taskSideContent = taskGroupSideContent.getChildren();
        Node temp = taskSideContent.get(selectedIndex);
        taskSideContent.remove(selectedIndex);
        taskSideContent.add(changeIndex, temp);
    }
}
