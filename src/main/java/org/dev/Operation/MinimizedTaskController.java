package org.dev.Operation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import org.dev.App;
import org.dev.Operation.Data.TaskData;
import org.dev.Operation.Task.Task;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MinimizedTaskController implements Initializable {
    @FXML
    private StackPane minimizedTaskLayerStackPane;
    @FXML
    private StackPane taskNameAreaStackPane;
    @FXML
    private Label taskIndexLabel, taskNameLabel, repeatNumberLabel;
    @FXML
    private CheckBox requiredCheckBox, previousPassCheckBox;
    @FXML
    private StackPane repeatMinusButton, repeatPlusButton;

    @Getter
    private TaskController taskController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        taskNameAreaStackPane.setOnMouseClicked(this::openTask);
        repeatMinusButton.setOnMouseClicked(this::decreaseRepeatNumber);
        repeatPlusButton.setOnMouseClicked(this::increaseRepeatNumber);
        requiredCheckBox.setOnAction(this::toggleRequiredCheckBox);
        previousPassCheckBox.setOnAction(this::togglePreviousPassOption);
        loadNewTaskPane();
    }

    public boolean isSet() { return (taskController != null && taskController.isSet()); }
    public void setTaskIndex(int newIndex) { taskIndexLabel.setText(Integer.toString(newIndex)); }
    public boolean isRequired() { return requiredCheckBox.isSelected(); }
    public boolean isPreviousPass() { return previousPassCheckBox.isSelected(); }
    public void disablePreviousOption() {
        previousPassCheckBox.setSelected(false);
        previousPassCheckBox.setVisible(false);
    }
    public void enablePreviousOption() {
        previousPassCheckBox.setSelected(false);
        previousPassCheckBox.setVisible(true);
    }
    public String getTaskName() { return taskNameLabel.getText(); }

    private void toggleRequiredCheckBox(ActionEvent event) {
        taskController.getTask().setRequired(requiredCheckBox.isSelected());
    }
    private void togglePreviousPassOption(ActionEvent event) {
        taskController.getTask().setPreviousPass(requiredCheckBox.isSelected());
    }

    // ------------------------------------------------------
    public void openTask(MouseEvent event) {
        taskNameLabel.setText(taskController.getTaskName());
        App.displayNewNode(taskController.getTaskPane());
    }
    private void loadNewTaskPane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("taskPane.fxml"));
            loader.load();
            taskController = loader.getController();
        } catch (Exception e) {
            System.out.println("Fail loading task vbox");
        }
    }

    // ------------------------------------------------------
    private int repeatNumber = 0;
    private void increaseRepeatNumber(MouseEvent event) {
        repeatNumber++;
        updateRepeatNumberLabel();
    }
    private void decreaseRepeatNumber(MouseEvent event) {
        repeatNumber = Math.max(repeatNumber - 1, -1);
        updateRepeatNumberLabel();
    }
    private void updateRepeatNumberLabel() { repeatNumberLabel.setText(Integer.toString(repeatNumber)); }

    public boolean runTask() {
        if (taskController == null) {
            System.out.println("Task controller not found in minimized task - bug");
            return false;
        }
        if (repeatNumber == -1) {
            System.out.println("Running task 'infinitely'");
            for (int j = 0; j < Integer.MAX_VALUE; j++)
                if (!taskController.runTask())
                    return false;
            return true;
        }
        for (int j = -1; j < repeatNumber; j++)
            if (!taskController.runTask())
                return false;
        return true;
    }

    // ------------------------------------------------------
    public TaskData getTaskData() {
        Task task = taskController.getTask();
        task.setRequired(requiredCheckBox.isSelected());
        task.setPreviousPass(previousPassCheckBox.isSelected());
        return taskController.getTaskData();
    }

    public void loadSavedTaskData(TaskData taskData) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("taskPane.fxml"));
        loader.load();
        taskController = loader.getController();
        taskController.loadSavedTaskData(taskData);
        Task task = taskController.getTask();
        taskNameLabel.setText(task.getTaskName());
        requiredCheckBox.setSelected(task.isRequired());
        previousPassCheckBox.setSelected(task.isPreviousPass());
    }
}
