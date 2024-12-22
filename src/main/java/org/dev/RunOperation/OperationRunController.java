package org.dev.RunOperation;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import lombok.Getter;
import org.dev.AppScene;
import org.dev.Operation.Data.OperationData;
import org.dev.Operation.Data.TaskData;
import org.dev.Operation.MainJobController;
import org.dev.Operation.OperationController;
import org.dev.Operation.Task.Task;
import org.dev.LeftSideMenu.SideMenuController;
import java.net.URL;
import java.util.ResourceBundle;

public class OperationRunController implements Initializable, MainJobController {

    @FXML @Getter
    private Group operationRunMainGroup;
    @FXML
    private VBox mainOperationRunVBox;
    @FXML @Getter
    private Label operationNameRunLabel;
    @FXML @Getter
    private ScrollPane operationRunScrollPane;
    @FXML @Getter
    private VBox runVBox;

    private Thread operationRunThread = null;

    @Getter
    private VBox taskRunVBoxSideContent = new VBox();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadMainOperationRunVBox();
        taskRunVBoxSideContent.setPadding(new Insets(0, 0, 0, 15));
    }

    private double currentGlobalScale = 1.0;
    private void loadMainOperationRunVBox() {
        double offset = ((StackPane) mainOperationRunVBox.getChildren().getFirst()).getPrefHeight() + mainOperationRunVBox.getSpacing();
        double newScrollHeight = AppScene.primaryBorderPane.getPrefHeight() - offset;
        if (currentGlobalScale != AppScene.currentGlobalScale) {
            currentGlobalScale = AppScene.currentGlobalScale;
            mainOperationRunVBox.getTransforms().add(new Scale(currentGlobalScale, currentGlobalScale, 0, 0));
            newScrollHeight = (AppScene.primaryBorderPane.getPrefHeight() - offset * currentGlobalScale) / currentGlobalScale;
        }
        operationRunScrollPane.setPrefHeight(newScrollHeight - 50);
    }

    @Override
    public void takeToDisplay() {
        if (mainOperationRunVBox.getScene() == null)
            return;
        System.out.println("Operation Run take to display");
        AppScene.closeActionMenuPane();
        AppScene.closeConditionMenuPane();
        operationRunScrollPane.setVvalue(0.0);
    }

    public void setVisible(boolean visible) { operationRunMainGroup.setVisible(visible); }

    private void changeOperationRunName(String newName) {
        operationNameRunLabel.setText(newName);
    }

    // ------------------------------------------------------
    public void startOperation(OperationController operationController) {
        OperationData operationData = operationController.getOperationData();
        if (operationData == null) {
            System.out.println("No operation data found");
            return;
        }
        String operationName = operationData.getOperation().getOperationName();
        changeOperationRunName(operationName);
        System.out.println("Start running operation: " + operationName);

        javafx.concurrent.Task<Void> operationRunTask = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() {
                try {
                    runOperation(operationData);
                    System.out.println("Operation is finished");
                } catch (InterruptedException e) {
                    System.out.println("Fail starting operation");
                }
                AppScene.setIsOperationRunning(false);
                return null;
            }
        };
        operationRunThread = new Thread(operationRunTask);
        operationRunThread.start();
        AppScene.setIsOperationRunning(true);
    }

    public void stopOperation() {
        operationRunThread.interrupt();
        AppScene.setIsOperationRunning(false);
    }

    private void runOperation(OperationData operationData) throws InterruptedException {
        boolean pass = false;
        for (TaskData taskData : operationData.getTaskDataList()) {
            Task currentTask = taskData.getTask();
            String taskName = currentTask.getTaskName();
            loadAndAddNewTaskRunPane();
            if (pass && currentTask.isPreviousPass()) {
                System.out.println("Previous is passed, Skipping task " + taskName);
                continue;
            }
            pass = currentTaskRunController.startTask(taskData);
            if (!currentTask.isRequired())
                pass = true;
            else if (!pass) { // task is required but failed
                System.out.println("Fail performing task: " + taskName);
                break;
            }
        }
    }

    private TaskRunController currentTaskRunController;
    private void loadAndAddNewTaskRunPane() {
        try {
            currentTaskRunController = null;
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("taskRunPane.fxml"));
            Group taskRunGroup = fxmlLoader.load();
            currentTaskRunController = fxmlLoader.getController();
            VBox actionRunVBox = currentTaskRunController.getActionRunVBoxSideContent();
            HBox taskRunLabelHBox = SideMenuController.getDropDownHBox(actionRunVBox,
                    new Label(currentTaskRunController.getTaskRunNameLabel().getText()),
                    currentTaskRunController);
            // update side hierarchy
            Platform.runLater(() -> taskRunVBoxSideContent.getChildren().add(new VBox(taskRunLabelHBox, actionRunVBox)));
            Platform.runLater(() -> runVBox.getChildren().add(taskRunGroup));
        } catch (Exception e) {
            System.out.println("Fail to load task run pane");
        }
    }
}
