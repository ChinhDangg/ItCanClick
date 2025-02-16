package org.dev.JobController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.Job.JobData;
import org.dev.Job.Task.Task;
import org.dev.JobStructure;
import org.dev.RunJob.JobRunController;

import java.net.URL;
import java.util.ResourceBundle;

public class MinimizedTaskController implements Initializable, JobDataController {
    @FXML
    private Node parentNode;
    @FXML
    private StackPane taskNameAreaStackPane;
    @FXML
    private Label repeatNumberLabel;
    @FXML
    private TextField renameTextField;
    @FXML
    private CheckBox requiredCheckBox, previousPassCheckBox;
    @FXML
    private StackPane repeatMinusButton, repeatPlusButton;

    private JobStructure currentStructure;
    private TaskController taskController;
    private final Task task = new Task();

    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        taskNameAreaStackPane.setOnMouseClicked(this::openTask);
        repeatMinusButton.setOnMouseClicked(this::decreaseRepeatNumber);
        repeatPlusButton.setOnMouseClicked(this::increaseRepeatNumber);
        loadTaskPane();
        renameTextField.focusedProperty().addListener((_, _, newValue) -> {
            if (!newValue) {
                //System.out.println("TextField lost focus");
                changeTaskName();
            }
        });
    }

    public void setJobStructure(JobStructure structure) {
        currentStructure = structure;
        taskController.setJobStructure(currentStructure);
    }

    public void disablePreviousOption() {
        previousPassCheckBox.setSelected(false);
        previousPassCheckBox.setVisible(false);
    }
    public void enablePreviousOption() {
        previousPassCheckBox.setSelected(false);
        previousPassCheckBox.setVisible(true);
    }

    private void changeTaskName() {
        String name = renameTextField.getText();
        name = name.strip();
        if (name.isBlank()) {
            renameTextField.setText(currentStructure.getName());
            return;
        }
        updateTaskName(name);
    }
    private void updateTaskName(String name) {
        currentStructure.changeName(name);
        renameTextField.setText(name);
        task.setTaskName(name);
        taskController.changeTaskName(name);
        AppScene.addLog(LogLevel.DEBUG, className, "Updated minimized task name: " + name);
    }

    // ------------------------------------------------------
    public void openTask(MouseEvent event) {
        if (AppScene.isJobRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Operation is running - cannot navigate");
            return;
        }
        taskController.takeToDisplay();
    }

    private void loadTaskPane() {
        AppScene.addLog(LogLevel.TRACE, className, "Loading Task Pane");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("taskPane.fxml"));
            loader.load();
            taskController = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Task Pane");
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading task pane: " + e.getMessage());
        }
    }

    // ------------------------------------------------------
    private int repeatNumber = 0;
    private void increaseRepeatNumber(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on increase repeat number");
        updateRepeatNumberLabel(repeatNumber + 1);
    }
    private void decreaseRepeatNumber(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on decrease repeat number");
        updateRepeatNumberLabel(Math.max(repeatNumber - 1, -1));
    }
    private void updateRepeatNumberLabel(int newRepeatNumber) {
        repeatNumber = newRepeatNumber;
        repeatNumberLabel.setText(Integer.toString(repeatNumber));
        AppScene.addLog(LogLevel.DEBUG, className, "Updated Repeat number: " + repeatNumber);
    }

    // ------------------------------------------------------
    @Override
    public boolean isSet() { return (taskController != null && taskController.isSet()); }

    @Override
    public String getName() { return renameTextField.getText(); }

    @Override
    public Node getParentNode() { return parentNode; }

    @Override
    public AppLevel getAppLevel() { return AppLevel.Task; }

    @Override
    public void takeToDisplay() {
        OperationController parentOperationController = (OperationController) currentStructure.getDisplayParentController();
        parentOperationController.takeToDisplay();
        parentOperationController.selectTheTaskPane(getParentNode());
        AppScene.updateMainDisplayScrollValue(getParentNode());
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    @Override
    public JobData getSavedData() {
        JobData tempData = taskController.getSavedData();
        task.setRequired(requiredCheckBox.isSelected());
        task.setPreviousPass(previousPassCheckBox.isSelected());
        task.setRepeatNumber(repeatNumber);
        JobData taskData = new JobData(task.cloneData(), tempData.getJobDataList());
        AppScene.addLog(LogLevel.TRACE, className, "Get Task Data");
        return taskData;
    }

    @Override
    public JobData getSavedDataByReference() {
        JobData tempData = taskController.getSavedDataByReference();
        task.setRequired(requiredCheckBox.isSelected());
        task.setPreviousPass(previousPassCheckBox.isSelected());
        task.setRepeatNumber(repeatNumber);
        JobData taskData = new JobData(task, tempData.getJobDataList());
        AppScene.addLog(LogLevel.TRACE, className, "Get Reference Task Data");
        return taskData;
    }

    @Override
    public void loadSavedData(JobData jobData) {
        taskController.loadSavedData(jobData);
        Task task = (Task) jobData.getMainJob();
        requiredCheckBox.setSelected(task.isRequired());
        previousPassCheckBox.setSelected(task.isPreviousPass());
        updateRepeatNumberLabel(task.getRepeatNumber());
        updateTaskName(task.getTaskName());
        AppScene.addLog(LogLevel.TRACE, className, "Loaded saved task data");
    }

    @Override
    public void addSavedData(JobData jobData) {
        taskController.addSavedData(jobData);
    }

    @Override
    public void removeSavedData(JobDataController jobDataController) { taskController.removeSavedData(jobDataController); }
    @Override
    public void moveSavedDataUp(JobDataController jobDataController) {}
    @Override
    public void moveSavedDataDown(JobDataController jobDataController) {}
    @Override
    public JobRunController getRunJob() { return taskController.getRunJob(); }
}
