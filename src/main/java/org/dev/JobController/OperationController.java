package org.dev.JobController;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import lombok.Getter;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.JobData.JobData;
import org.dev.JobData.OperationData;
import org.dev.Job.Operation;
import org.dev.JobData.TaskGroupData;
import org.dev.SideMenu.LeftMenu.SideMenuController;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class OperationController implements Initializable, Serializable, JobDataController {
    @FXML
    private ScrollPane operationScrollPane;
    @FXML
    private VBox mainOperationVBox;
    @FXML
    private TextField renameTextField;
    @FXML
    private VBox operationVBox;
    @FXML
    private HBox addTaskButton;

    @Getter
    private final List<TaskGroupController> taskList = new ArrayList<>();
    private Operation operation = new Operation();
    @Getter
    private final Label operationNameLabel = new Label();
    @Getter
    private VBox operationSideContent = new VBox();
    private double currentScale = 1;
    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addTaskButton.setOnMouseClicked(this::addTaskGroupAction);
        operationNameLabel.setText(renameTextField.getText());
        renameTextField.focusedProperty().addListener((_, _, newValue) -> {
            if (!newValue) {
                //System.out.println("TextField lost focus");
                changeOperationName();
            }
        });
        loadMainOperationVBox();
    }

    public boolean isSet() { return !taskList.isEmpty() && taskList.getFirst().isSet(); }
    public void setVisible(boolean visible) { getParentNode().setVisible(visible); }
    public Node getParentNode() { return operationScrollPane; }

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
            renameTextField.setText(operation.getOperationName());
            return;
        }
        updateOperationName(name);
    }
    private void updateOperationName(String name) {
        operationNameLabel.setText(name);
        renameTextField.setText(name);
        AppScene.addLog(LogLevel.DEBUG, className, "Updated operation name: " + name);
    }

    // ------------------------------------------------------
    private void addTaskGroupAction(MouseEvent event) {
        if (AppScene.isOperationRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Operation is running - cannot modify");
            return;
        }
        if (!taskList.isEmpty() && !taskList.getLast().isSet()) {
            AppScene.addLog(LogLevel.INFO, className, "Recent minimized task is not set");
            return;
        }
        addSavedData(null);
    }

    // ------------------------------------------------------
    public void changeOperationScrollPaneView(Node minimizedTaskPane) {
        selectTheTaskPane(minimizedTaskPane);
        changeScrollPaneView(minimizedTaskPane);
    }
    private void changeScrollPaneView(Node taskPane) {
        double targetPaneY = taskPane.getBoundsInParent().getMinY();
        Node parentChecking = taskPane.getParent();
        while (parentChecking != mainOperationVBox) {
            targetPaneY += parentChecking.getBoundsInParent().getMinY();
            parentChecking = parentChecking.getParent();
        }
        targetPaneY += parentChecking.getBoundsInParent().getMinY();
        targetPaneY *= currentScale;
        double contentHeight = operationScrollPane.getContent().getBoundsInLocal().getHeight();
        double scrollPaneHeight = operationScrollPane.getViewportBounds().getHeight();
        targetPaneY -= scrollPaneHeight / 3;
        double vValue = Math.min(targetPaneY / (contentHeight - scrollPaneHeight), 1.00);
        operationScrollPane.setVvalue(vValue);
        AppScene.addLog(LogLevel.TRACE, className, "Operation Scroll Pane v value changed: " + vValue);
    }

    // ------------------------------------------------------
    private Node currentSelectedTaskPane = null;
    private void selectTheTaskPaneAction(MouseEvent event) {
        selectTheTaskPane((Node) event.getSource());
    }
    private void selectTheTaskPane(Node taskPane) {
        if (currentSelectedTaskPane != null)
            setUnselected(currentSelectedTaskPane);
        currentSelectedTaskPane = taskPane;
        setSelected(currentSelectedTaskPane);
    }
    private void setSelected(Node taskPane) { taskPane.setStyle("-fx-border-color: black; -fx-border-width: 1px;"); }
    private void setUnselected(Node taskPane) { taskPane.setStyle(""); }

    // ------------------------------------------------------
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
    public OperationData getSavedData() {
        OperationData operationData = new OperationData();
        operation.setOperationName(operationNameLabel.getText());
        operationData.setOperation(operation.getDeepCopied());
        List<TaskGroupData> taskDataList = new ArrayList<>();
        for (TaskGroupController taskGroupController : taskList)
            taskDataList.add(taskGroupController.getSavedData());
        operationData.setTaskGroupDataList(taskDataList);
        AppScene.addLog(LogLevel.TRACE, className, "Got operation data");
        return operationData;
    }

    @Override
    public void loadSavedData(JobData jobData) {
        if (jobData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Operation data is null - cannot load from save");
            return;
        }
        OperationData operationData = (OperationData) jobData;
        this.operation = operationData.getOperation();
        updateOperationName(operation.getOperationName());
        for (TaskGroupData data : operationData.getTaskGroupDataList())
            addSavedData(data);
    }

    @Override
    public void addSavedData(JobData taskData) {
        try {
            AppScene.addLog(LogLevel.TRACE, className, "Loading Task Group Pane");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("taskGroupPane.fxml"));
            Node taskPane = loader.load();
            taskPane.setOnMouseClicked(this::selectTheTaskPaneAction);
            TaskGroupController controller = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Task Group Pane");
            if (taskData != null)
                controller.loadSavedData(taskData);
            int numberOfTask = taskList.size();
            controller.setTaskIndex(numberOfTask + 1);
            operationVBox.getChildren().add(taskPane);
            taskList.add(controller);
            createTaskSideContent(controller);
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading and adding task group pane: " + e.getMessage());
        }
    }

    @Override
    public void removeSavedData(JobDataController jobDataController) {
        int changeIndex = taskList.indexOf((TaskGroupController) jobDataController);
        removeTask(changeIndex);
    }
    private void removeTask(int changeIndex) {
        taskList.remove(changeIndex);
        operationVBox.getChildren().remove(changeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Removed selected task: " + changeIndex);
        updateTaskIndex(changeIndex);
        // update side menu
        removeOperationSideContent(changeIndex);
    }

    @Override
    public void moveSavedDataUp(JobDataController jobDataController) {
        int numberOfTasks = taskList.size();
        if (numberOfTasks < 2)
            return;
        int selectedTaskPaneIndex = taskList.indexOf((TaskGroupController) jobDataController);
        if (selectedTaskPaneIndex == 0)
            return;
        int changeIndex = selectedTaskPaneIndex-1;
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on move-down selected task: " + changeIndex);
        updateTaskPaneList(selectedTaskPaneIndex, changeIndex);
        updateTaskIndex(changeIndex);
        //update side menu
        updateTaskSideContent(selectedTaskPaneIndex, changeIndex);
    }

    @Override
    public void moveSavedDataDown(JobDataController jobDataController) {
        int numberOfTasks = taskList.size();
        if (numberOfTasks < 2)
            return;
        int selectedTaskPaneIndex = taskList.indexOf((TaskGroupController) jobDataController);
        int changeIndex = selectedTaskPaneIndex+1;
        if (changeIndex == numberOfTasks)
            return;
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on move-up selected task: " + changeIndex);
        updateTaskPaneList(selectedTaskPaneIndex, changeIndex);
        updateTaskIndex(changeIndex-1);
        //update side menu
        updateTaskSideContent(selectedTaskPaneIndex, changeIndex);
    }

    private void updateTaskPaneList(int selectedIndex, int changeIndex) {
        ObservableList<Node> children = operationVBox.getChildren();
        Node taskGroupNode = children.get(selectedIndex);
        taskList.add(changeIndex, taskList.remove(selectedIndex));
        children.remove(taskGroupNode);
        children.add(changeIndex, taskGroupNode);
    }
    private void updateTaskIndex(int start) {
        for (int j = start; j < taskList.size(); j++)
            taskList.get(j).setTaskIndex(j+1);
    }

    // ------------------------------------------------------
    private void createTaskSideContent(TaskGroupController controller) {
        VBox taskSideContent = controller.getTaskGroupSideContent();
        Node taskLabelHBox = SideMenuController.getNewSideHBoxLabel(controller.getTaskGroupNameLabel(), taskSideContent, controller, this);
        operationSideContent.getChildren().add(new VBox(taskLabelHBox, taskSideContent));
        AppScene.addLog(LogLevel.TRACE, className, "Created operation side content");
    }
    private void removeOperationSideContent(int changeIndex) {
        operationSideContent.getChildren().remove(changeIndex);
    }
    private void updateTaskSideContent(int selectedIndex, int changeIndex) {
        ObservableList<Node> taskSideContent = operationSideContent.getChildren();
        Node temp = taskSideContent.get(selectedIndex);
        taskSideContent.remove(selectedIndex);
        taskSideContent.add(changeIndex, temp);
    }
}