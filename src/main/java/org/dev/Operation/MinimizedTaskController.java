package org.dev.Operation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.dev.App;
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
    private TaskController taskController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        taskNameAreaStackPane.setOnMouseClicked(this::openTask);
        repeatMinusButton.setOnMouseClicked(this::decreaseRepeatNumber);
        repeatPlusButton.setOnMouseClicked(this::increaseRepeatNumber);
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

    public void openTask(MouseEvent event) {
        loadNewTaskPane();
        taskNameLabel.setText(taskController.getTaskName());
        App.displayNewNode(taskController.getTaskPane());
    }
    private void loadNewTaskPane() {
        try {
            if (taskController != null)
                return;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("taskPane.fxml"));
            loader.load();
            taskController = loader.getController();
        } catch (Exception e) {
            System.out.println("Fail loading task vbox");
        }
    }

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
}
