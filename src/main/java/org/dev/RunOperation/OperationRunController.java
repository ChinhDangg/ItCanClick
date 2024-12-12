package org.dev.RunOperation;

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
import org.dev.SideMenuController;
import java.net.URL;
import java.util.ResourceBundle;

public class OperationRunController implements Initializable, MainJobController {

    @FXML
    private VBox mainOperationRunVBox;
    @FXML @Getter
    private Label operationNameRunLabel;
    @FXML @Getter
    private ScrollPane operationRunScrollPane;
    @FXML @Getter
    private VBox runVBox;

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
        operationRunScrollPane.setPrefHeight(newScrollHeight - 25);
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

    private void changeOperationRunName(String newName) {
        operationNameRunLabel.setText(newName);
    }

    // ------------------------------------------------------
    public void startOperation(OperationController operationController) {
        try {
            OperationData operationData = operationController.getOperationData();
            String operationName = operationData.getOperation().getOperationName();
            changeOperationRunName(operationName);
            System.out.println("Start running operation: " + operationName);

            //runOperation(operationData);
            System.out.println("waiting");
            Thread.sleep(20000);
            System.out.println("Operation is finished");
        } catch (Exception e) {
            System.out.println("Fail starting operation");
        }
    }

    public void runOperation(OperationData operationData) throws InterruptedException {
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
    public void loadAndAddNewTaskRunPane() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("taskRunPane.fxml"));;
            Group taskRunGroup = fxmlLoader.load();
            currentTaskRunController = fxmlLoader.getController();
            VBox actionRunVBox = currentTaskRunController.getActionRunVBoxSideContent();
            HBox taskRunLabelHBox = SideMenuController.getDropDownHBox(actionRunVBox,
                    new Label(currentTaskRunController.getTaskRunNameLabel().getText()),
                    currentTaskRunController);
            taskRunVBoxSideContent.getChildren().add(new VBox(taskRunLabelHBox, actionRunVBox));
            runVBox.getChildren().add(taskRunGroup);
        } catch (Exception e) {
            System.out.println("Fail to load task run pane");
        }
    }
}
