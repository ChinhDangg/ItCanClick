package org.dev.JobController;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import lombok.Getter;
import lombok.Setter;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.JobData.ActionData;
import org.dev.JobData.JobData;
import org.dev.JobData.TaskData;
import org.dev.JobStructure;
import org.dev.RunJob.JobRunController;
import org.dev.RunJob.TaskRunController;
import org.dev.SideMenu.LeftMenu.SideMenuController;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TaskController implements Initializable, JobDataController {

    @FXML
    private ScrollPane taskScrollPane;
    @FXML
    private Label taskNameLabel;
    @FXML
    private VBox mainTaskOuterVBox, taskVBox;
    @FXML
    private Group backButton;
    @FXML
    private StackPane addNewActionButton;

    @Setter
    private JobStructure currentStructure;
    private double currentGlobalScale = 1;

    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addNewActionButton.setOnMouseClicked(this::addNewActionPane);
        backButton.setOnMouseClicked(this::backToPreviousAction);
        loadMainTaskVBox();
    }

    public void openTaskPane() { AppScene.displayNewCenterNode(taskScrollPane); }
    private void backToPreviousAction(MouseEvent event) { AppScene.backToOperationScene(); }
    public void changeTaskName(String name) { taskNameLabel.setText(name); }

    private void loadMainTaskVBox() {
        if (currentGlobalScale != AppScene.currentGlobalScale) {
            currentGlobalScale = AppScene.currentGlobalScale;
            mainTaskOuterVBox.getTransforms().add(new Scale(currentGlobalScale, currentGlobalScale, 0, 0));
        }
    }

    // ------------------------------------------------------
    private void addNewActionPane(MouseEvent event) {
        if (AppScene.isJobRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Operation is running - cannot modify");
            return;
        }
        if (!actionList.isEmpty() && !actionList.getLast().isSet()) {
            AppScene.addLog(LogLevel.INFO, className, "Recent action is not set");
            return;
        }
        addSavedData(null);
    }

    public void changeTaskScrollPaneView(Node mainActionPane) {
        selectTheActionPane(mainActionPane);
        double targetPaneY = mainActionPane.getBoundsInParent().getMinY() * currentGlobalScale;
        double contentHeight = taskScrollPane.getContent().getBoundsInLocal().getHeight();
        double scrollPaneHeight = taskScrollPane.getViewportBounds().getHeight();
        double vValue = Math.min(targetPaneY / (contentHeight - scrollPaneHeight), 1.00);
        taskScrollPane.setVvalue(vValue);
        AppScene.addLog(LogLevel.TRACE, className, "Task scroll pane v value changed: " + vValue);
    }

    // ------------------------------------------------------
    private Node currentSelectedActionPane = null;
    private void selectTheActionPaneAction(MouseEvent event) {
        selectTheActionPane((Node) event.getSource());
    }
    private void selectTheActionPane(Node actionPane) {
        if (currentSelectedActionPane != null)
            setUnSelectedAction(currentSelectedActionPane);
        currentSelectedActionPane = actionPane;
        setSelectedAction(currentSelectedActionPane);
    }
    private void setSelectedAction(Node actionPane) { actionPane.setStyle("-fx-border-color: black; -fx-border-width: 1px;"); }
    private void setUnSelectedAction(Node actionPane) { actionPane.setStyle(""); }

    // ------------------------------------------------------
    @Override
    public boolean isSet() { return (!actionList.isEmpty() && actionList.getFirst().isSet()); }

    @Override
    public Node getParentNode() { return taskScrollPane; }

    @Override
    public AppLevel getAppLevel() { return AppLevel.Task; }

    @Override
    public void takeToDisplay(MainJobController parentController) { openTaskPane(); }

    @Override
    public TaskData getSavedData() {
        TaskData taskData = new TaskData();
        List<ActionData> actionData = new ArrayList<>();
        for (ActionController actionController : actionList)
            actionData.add(actionController.getSavedData());
        taskData.setActionDataList(actionData);
        AppScene.addLog(LogLevel.TRACE, className, "Got task data");
        return taskData;
    }

    @Override
    public void loadSavedData(JobData jobData) {
        if (jobData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Task data is null - cannot load from save");
            return;
        }
        TaskData taskData = (TaskData) jobData;
        taskNameLabel.setText(taskData.getTask().getTaskName());
        for (ActionData actionData : taskData.getActionDataList())
            addSavedData(actionData);
    }

    @Override
    public void addSavedData(JobData actionData) {
        try {
            AppScene.addLog(LogLevel.TRACE, className, "Loading Action Pane");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("actionPane.fxml"));
            Node actionPane = loader.load();
            actionPane.setOnMouseClicked(this::selectTheActionPaneAction);
            ActionController actionController = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Action Pane");
            if (actionData != null)
                actionController.loadSavedData(actionData);
            int numberOfActions = actionList.size();
            if (numberOfActions == 0)
                actionController.disablePreviousOptions();
            taskVBox.getChildren().add(actionPane);
            actionList.add(actionController);
            // update side menu
            createTaskSideContent(actionController);
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading and adding action pane: " + e.getMessage());
        }
    }

    @Override
    public void removeSavedData(JobDataController jobDataController) {
        int changeIndex = actionList.indexOf((ActionController) jobDataController);
        removeAction(changeIndex);
    }
    private void removeAction(int changeIndex) {
        actionList.remove(changeIndex);
        if (changeIndex == 0 && !actionList.isEmpty())
            actionList.getFirst().disablePreviousOptions();
        taskVBox.getChildren().remove(changeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Removed selected action: " + changeIndex);
        //update side menu
        removeTaskSideContent(changeIndex);
    }

    @Override
    public void moveSavedDataUp(JobDataController jobDataController) {
        int numberOfActions = actionList.size();
        if (numberOfActions < 2)
            return;
        int selectedActionPaneIndex = actionList.indexOf((ActionController) jobDataController);
        if (selectedActionPaneIndex == 0)
            return;
        int changeIndex = selectedActionPaneIndex-1;
        updateActionPaneList(selectedActionPaneIndex, changeIndex);
        changeActionPreviousOptions(changeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Moved up action: " + changeIndex);
        //update side menu
        updateActionSideContent(selectedActionPaneIndex, changeIndex);
    }

    @Override
    public void moveSavedDataDown(JobDataController jobDataController) {
        int numberOfActions = actionList.size();
        if (numberOfActions < 2)
            return;
        int selectedActionPaneIndex = actionList.indexOf((ActionController) jobDataController);
        int changeIndex = selectedActionPaneIndex +1;
        if (changeIndex == numberOfActions)
            return;
        updateActionPaneList(selectedActionPaneIndex, changeIndex);
        changeActionPreviousOptions(changeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Moved down action: " + changeIndex);
        //update side menu
        updateActionSideContent(selectedActionPaneIndex, changeIndex);
    }

    private void updateActionPaneList(int selectedIndex, int changeIndex) {
        ObservableList<Node> children = taskVBox.getChildren();
        Node actionNode = children.get(selectedIndex);
        actionList.add(changeIndex, actionList.remove(selectedIndex));
        children.remove(actionNode);
        children.add(changeIndex, actionNode);
    }
    private void changeActionPreviousOptions(int index) {
        if (index < 2) {
            actionList.get(0).disablePreviousOptions();
            actionList.get(1).enablePreviousOptions();
        }
    }

    @Override
    public JobRunController getRunJob() {
        try {
            FXMLLoader loader = new FXMLLoader(AppScene.class.getResource("RunJob/taskRunPane.fxml"));
            loader.load();
            TaskRunController taskRunController = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Task Run");
            return taskRunController;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading Task Run Pane: " + e.getMessage());
            return null;
        }
    }

    // ------------------------------------------------------
    private void createTaskSideContent(ActionController actionController) {
        Node actionHBoxLabel = SideMenuController.getNewSideHBoxLabel(actionController.getActionNameLabel(),
                null, actionController, this);
        taskSideContent.getChildren().add(actionHBoxLabel);
        AppScene.addLog(LogLevel.TRACE, className, "Created task side content");
    }
    private void removeTaskSideContent(int changeIndex) {
        taskSideContent.getChildren().remove(changeIndex);
    }
    private void updateActionSideContent(int selectedActionPaneIndex, int changeIndex) {
        ObservableList<Node> actionSideContent = taskSideContent.getChildren();
        Node temp = actionSideContent.get(selectedActionPaneIndex);
        actionSideContent.remove(selectedActionPaneIndex);
        actionSideContent.add(changeIndex, temp);
    }
}