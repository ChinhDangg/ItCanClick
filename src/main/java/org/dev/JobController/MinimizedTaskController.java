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
import lombok.Getter;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.JobData.JobData;
import org.dev.JobData.TaskData;
import org.dev.Job.Task.Task;

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

    @Getter
    private final Label taskNameLabel = new Label();
    @Getter
    private TaskController taskController;
    private Task task = new Task();
    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        taskNameAreaStackPane.setOnMouseClicked(this::openTask);
        repeatMinusButton.setOnMouseClicked(this::decreaseRepeatNumber);
        repeatPlusButton.setOnMouseClicked(this::increaseRepeatNumber);
        taskNameLabel.setText(renameTextField.getText());
        loadNewTaskPane();
        renameTextField.focusedProperty().addListener((_, _, newValue) -> {
            if (!newValue) {
                //System.out.println("TextField lost focus");
                changeTaskName();
            }
        });
    }

    public boolean isSet() { return (taskController != null && taskController.isSet()); }
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
            renameTextField.setText(taskNameLabel.getText());
            return;
        }
        updateTaskName(name);
    }
    private void updateTaskName(String name) {
        taskNameLabel.setText(name);
        renameTextField.setText(name);
        taskController.changeTaskName(name);
        AppScene.addLog(LogLevel.DEBUG, className, "Updated minimized task name: " + name);
    }

    // ------------------------------------------------------
    public void openTask(MouseEvent event) {
        if (AppScene.isRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Operation is running - cannot navigate");
            return;
        }
        taskController.openTaskPane();
    }
    private void loadNewTaskPane() {
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
    public AppLevel getAppLevel() { return AppLevel.Task; }

    @Override
    public void takeToDisplay() {
        AppScene.currentLoadedOperationController.takeToDisplay();
        AppScene.currentLoadedOperationController.changeOperationScrollPaneView(parentNode);
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    @Override
    public TaskData getSavedData() {
        TaskData taskData = taskController.getSavedData();
        task.setRepeatNumber(repeatNumber);
        task.setRequired(requiredCheckBox.isSelected());
        task.setPreviousPass(previousPassCheckBox.isSelected());
        task.setTaskName(taskNameLabel.getText());
        Task newCopiedTask = task.getDeepCopied();
        taskData.setTask(newCopiedTask);
        AppScene.addLog(LogLevel.TRACE, className, "Get Task Data");
        return taskData;
    }

    @Override
    public void loadSavedData(JobData jobData) {
        loadNewTaskPane();
        TaskData taskData = (TaskData) jobData;
        taskController.loadSavedData(taskData);
        task = taskData.getTask();
        requiredCheckBox.setSelected(task.isRequired());
        previousPassCheckBox.setSelected(task.isPreviousPass());
        updateRepeatNumberLabel(task.getRepeatNumber());
        updateTaskName(task.getTaskName());
        AppScene.addLog(LogLevel.TRACE, className, "Loaded saved task data");
    }

    @Override
    public void addSavedData(JobData jobData) { taskController.addSavedData(jobData); }
    @Override
    public void removeSavedData(JobDataController jobDataController) { taskController.removeSavedData(jobDataController); }
    @Override
    public void moveSavedDataUp(JobDataController jobDataController) {}
    @Override
    public void moveSavedDataDown(JobDataController jobDataController) {}
}
