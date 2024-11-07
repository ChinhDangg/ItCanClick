package org.dev.Operation;

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
import org.dev.AppScene;
import org.dev.Operation.Data.TaskData;
import org.dev.Operation.Task.Task;
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

    @Override
    public void takeToDisplay() {
        AppScene.currentLoadedOperationController.takeToDisplay();
        System.out.println("Minimized task take to display");
        AppScene.currentLoadedOperationController.changeOperationScrollPaneView(minimizedTaskLayerStackPane);
    }

    // ------------------------------------------------------
    public void openTask(MouseEvent event) {
        if (AppScene.isOperationRunning) {
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
        updateRepeatNumberLabel(repeatNumber + 1);
    }
    private void decreaseRepeatNumber(MouseEvent event) {
        updateRepeatNumberLabel(Math.max(repeatNumber - 1, -1));
    }
    private void updateRepeatNumberLabel(int newRepeatNumber) {
        repeatNumber = newRepeatNumber;
        repeatNumberLabel.setText(Integer.toString(repeatNumber));
    }

    // ------------------------------------------------------
    public TaskData getTaskData() {
        TaskData taskData = taskController.getTaskData();
        task.setRepeatNumber(repeatNumber);
        task.setRequired(requiredCheckBox.isSelected());
        task.setPreviousPass(requiredCheckBox.isSelected());
        taskData.setTask(task);
        return taskData;
    }

    public void loadSavedTaskData(TaskData taskData) throws IOException {
        loadNewTaskPane();
        taskController.loadSavedTaskData(taskData);
        task = taskData.getTask();
        requiredCheckBox.setSelected(task.isRequired());
        previousPassCheckBox.setSelected(task.isPreviousPass());
        updateRepeatNumberLabel(task.getRepeatNumber());
        updateTaskName(task.getTaskName());
    }
}
