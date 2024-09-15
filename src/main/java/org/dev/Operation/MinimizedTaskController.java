package org.dev.Operation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;
import org.dev.App;
import org.dev.Operation.Data.TaskData;
import org.dev.Operation.Task.Task;
import org.dev.SideMenuController;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MinimizedTaskController implements Initializable, MainJobController {
    @FXML
    private StackPane minimizedTaskLayerStackPane, taskNameAreaStackPane;
    @FXML
    private Label taskIndexLabel, repeatNumberLabel;
    @FXML
    private TextField renameTextField;
    @FXML
    private CheckBox requiredCheckBox, previousPassCheckBox;
    @FXML
    private StackPane repeatMinusButton, repeatPlusButton;

    @Getter
    private final Label taskNameLabel = new Label();

    @Getter
    @Setter
    private TaskController taskController;
    @Getter
    private Task task = new Task();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        taskNameAreaStackPane.setOnMouseClicked(this::openTask);
        repeatMinusButton.setOnMouseClicked(this::decreaseRepeatNumber);
        repeatPlusButton.setOnMouseClicked(this::increaseRepeatNumber);
        requiredCheckBox.setOnAction(this::toggleRequiredCheckBox);
        previousPassCheckBox.setOnAction(this::togglePreviousPassOption);
        taskNameLabel.setText(renameTextField.getText());
        loadNewTaskPane();
        renameTextField.focusedProperty().addListener((_, _, newValue) -> {
            if (!newValue) {
                //System.out.println("TextField lost focus");
                changeTaskName();
            }
        });
    }

    private void changeTaskName() {
        String name = renameTextField.getText();
        name = name.strip();
        if (name.isBlank()) {
            renameTextField.setText(task.getTaskName());
            return;
        }
        updateTaskName(name);
    }
    private void updateTaskName(String name) {
        taskNameLabel.setText(name);
        renameTextField.setText(name);
        taskController.changeTaskName(name);
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

    private void toggleRequiredCheckBox(ActionEvent event) {
        task.setRequired(requiredCheckBox.isSelected());
    }
    private void togglePreviousPassOption(ActionEvent event) {
        task.setPreviousPass(requiredCheckBox.isSelected());
    }

    @Override
    public void takeToDisplay() {
        App.currentLoadedOperationController.takeToDisplay();
        System.out.println("Minimized task take to display");
        App.currentLoadedOperationController.changeOperationScrollPaneView(minimizedTaskLayerStackPane);
    }

    // ------------------------------------------------------
    public void openTask(MouseEvent event) {
        if (App.isOperationRunning) {
            System.out.println("Operation is running, cannot navigate");
            return;
        }
        taskController.openTaskPane();
    }
    private void loadNewTaskPane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("taskPane.fxml"));
            loader.load();
            setTaskController(loader.getController());
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
        TaskData taskData = taskController.getTaskData();
        taskData.setTask(task);
        return taskController.getTaskData();
    }

    public void loadSavedTaskData(TaskData taskData) throws IOException {
        loadNewTaskPane();
        taskController.loadSavedTaskData(taskData);
        task = taskData.getTask();
        requiredCheckBox.setSelected(task.isRequired());
        previousPassCheckBox.setSelected(task.isPreviousPass());
        updateTaskName(task.getTaskName());
    }
}
