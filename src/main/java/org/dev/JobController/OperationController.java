package org.dev.JobController;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.Job.JobData;
import org.dev.Job.Operation;
import org.dev.JobStructure;
import org.dev.RunJob.JobRunController;
import org.dev.RunJob.OperationRunController;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class OperationController implements Initializable, JobDataController {
    @FXML
    private Node parentNode;
    @FXML
    private VBox mainOperationVBox;
    @FXML
    private TextField renameTextField;
    @FXML
    private VBox operationVBox;
    @FXML
    private HBox addTaskButton;

    private JobStructure currentStructure;
    private double currentScale = 1;

    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addTaskButton.setOnMouseClicked(this::addTaskGroupAction);
        renameTextField.focusedProperty().addListener((_, _, newValue) -> {
            if (!newValue) {
                //System.out.println("TextField lost focus");
                changeOperationName();
            }
        });
        loadMainOperationVBox();
    }

    public void setJobStructure(JobStructure structure) {
        currentStructure = structure;
    }

    private void loadMainOperationVBox() {
        if (currentScale != AppScene.currentGlobalScale) {
            currentScale = AppScene.currentGlobalScale;
            mainOperationVBox.getTransforms().add(new Scale(currentScale, currentScale, 0, 0));
        }
        AppScene.addLog(LogLevel.TRACE, className, "Loaded operation vbox with scale: " + currentScale);
    }

    private void changeOperationName() {
        String name = renameTextField.getText();
        name = name.strip();
        if (name.isBlank()) {
            renameTextField.setText(currentStructure.getName());
            return;
        }
        updateOperationName(name);
    }
    private void updateOperationName(String name) {
        currentStructure.changeName(name);
        renameTextField.setText(name);
        AppScene.addLog(LogLevel.DEBUG, className, "Updated operation name: " + name);
    }

    // ------------------------------------------------------
    private void addTaskGroupAction(MouseEvent event) {
        addSavedData(null);
    }

    // ------------------------------------------------------
    private Node currentSelectedTaskPane = null;
    private void selectTheTaskPaneAction(MouseEvent event) {
        selectTheTaskPane((Node) event.getSource());
    }
    public void selectTheTaskPane(Node taskPane) {
        if (currentSelectedTaskPane != null)
            setUnselected(currentSelectedTaskPane);
        currentSelectedTaskPane = taskPane;
        setSelected(currentSelectedTaskPane);
    }
    private void setSelected(Node taskPane) { taskPane.setStyle("-fx-border-color: black; -fx-border-width: 1px;"); }
    private void setUnselected(Node taskPane) { taskPane.setStyle(""); }

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
    public AppLevel getAppLevel() {
        return AppLevel.Operation;
    }

    @Override
    public void takeToDisplay() {
        AppScene.closeActionMenuPane();
        AppScene.closeConditionMenuPane();
        AppScene.backToOperationScene();
        AppScene.addLog(LogLevel.TRACE, className, "Take to display");
    }

    @Override
    public JobData getSavedData() {
        Operation operation = new Operation(currentStructure.getName());
        List<JobData> taskDataList = new ArrayList<>();
        for (JobStructure subJobStructure : currentStructure.getSubJobStructures())
            taskDataList.add(subJobStructure.getCurrentController().getSavedData());
        JobData operationData = new JobData(operation, taskDataList);
        AppScene.addLog(LogLevel.TRACE, className, "Got operation data");
        return operationData;
    }

    @Override
    public void loadSavedData(JobData jobData) {
        if (jobData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Operation data is null - cannot load from save");
            return;
        }
        Operation operation = (Operation) jobData.getMainJob();
        updateOperationName(operation.getOperationName());
        for (JobData taskGroupData : jobData.getJobDataList())
            addSavedData(taskGroupData);
    }

    @Override
    public void addSavedData(JobData taskData) {
        if (AppScene.isJobRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Another job is running - cannot modify");
            return;
        }
        if (!currentStructure.getSubJobStructures().isEmpty() && currentStructure.getSubJobStructures().getLast().getCurrentController().isSet()) {
            AppScene.addLog(LogLevel.INFO, className, "Recent Task Group is not set");
            return;
        }
        try {
            AppScene.addLog(LogLevel.TRACE, className, "Loading Task Group Pane");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("taskGroupPane.fxml"));
            Node taskPane = loader.load();
            taskPane.setOnMouseClicked(this::selectTheTaskPaneAction);
            TaskGroupController controller = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Task Group Pane");
            if (taskData != null)
                controller.loadSavedData(taskData);
            int numberOfTask = currentStructure.getSubStructureSize();
            controller.setTaskIndex(numberOfTask + 1);
            operationVBox.getChildren().add(taskPane);

            JobStructure taskGroupStructure = new JobStructure(this,this, controller, controller.getName());
            controller.setJobStructure(taskGroupStructure);
            currentStructure.addSubJobStructure(taskGroupStructure);
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading and adding task group pane: " + e.getMessage());
        }
    }

    @Override
    public void removeSavedData(JobDataController jobDataController) {
        int removeIndex = currentStructure.removeSubJobStructure(jobDataController);
        operationVBox.getChildren().remove(removeIndex);
        updateTaskIndex(removeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Removed selected task: " + removeIndex);
    }

    @Override
    public void moveSavedDataUp(JobDataController jobDataController) {
        int numberOfTasks = currentStructure.getSubStructureSize();
        if (numberOfTasks < 2)
            return;
        int selectedTaskPaneIndex = currentStructure.getSubStructureIndex(jobDataController);
        if (selectedTaskPaneIndex == 0)
            return;
        int changeIndex = selectedTaskPaneIndex-1;
        updateTaskPaneList(selectedTaskPaneIndex, changeIndex);
        currentStructure.updateSubJobStructure(jobDataController, changeIndex);
        updateTaskIndex(changeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Moved-down Task Group: " + changeIndex);
    }

    @Override
    public void moveSavedDataDown(JobDataController jobDataController) {
        int numberOfTasks = currentStructure.getSubStructureSize();
        if (numberOfTasks < 2)
            return;
        int selectedTaskPaneIndex = currentStructure.getSubStructureIndex(jobDataController);
        int changeIndex = selectedTaskPaneIndex+1;
        if (changeIndex == numberOfTasks)
            return;
        updateTaskPaneList(selectedTaskPaneIndex, changeIndex);
        currentStructure.updateSubJobStructure(jobDataController, changeIndex);
        updateTaskIndex(changeIndex-1);
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on move-up selected task: " + changeIndex);
    }

    private void updateTaskPaneList(int selectedIndex, int changeIndex) {
        ObservableList<Node> children = operationVBox.getChildren();
        Node taskGroupNode = children.get(selectedIndex);
        children.remove(taskGroupNode);
        children.add(changeIndex, taskGroupNode);
    }
    private void updateTaskIndex(int start) {
        for (int j = start; j < currentStructure.getSubStructureSize(); j++)
            ((TaskGroupController) currentStructure.getSubJobStructures().get(j).getCurrentController()).setTaskIndex(j+1);
    }

    @Override
    public JobRunController getRunJob() {
        try {
            FXMLLoader loader = new FXMLLoader(AppScene.class.getResource("RunJob/operationRunPane.fxml"));
            loader.load();
            OperationRunController operationRunController = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Operation Run");
            return operationRunController;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading Operation Run Pane: " + e.getMessage());
            return null;
        }
    }

}