package org.dev.JobController;

import javafx.application.Platform;
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
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.JobData.ActionData;
import org.dev.JobData.JobData;
import org.dev.JobData.TaskData;
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
    private StackPane removeActionButton, moveActionUpButton, moveActionDownButton, addNewActionButton;

    @Getter
    private final List<ActionController> actionList = new ArrayList<>();
    @Getter
    private VBox taskSideContent = new VBox();
    private double currentGlobalScale = 1;
    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addNewActionButton.setOnMouseClicked(this::addNewActionPane);
        removeActionButton.setOnMouseClicked(this::removeSelectedActionPane);
        moveActionDownButton.setOnMouseClicked(this::moveActionDown);
        moveActionUpButton.setOnMouseClicked(this::moveActionUp);
        backButton.setOnMouseClicked(this::backToPreviousAction);
        loadMainTaskVBox();
        taskVBox.heightProperty().addListener((_, _, _) -> Platform.runLater(() -> taskScrollPane.setVvalue(1.0)));
    }

    private void loadMainTaskVBox() {
        if (currentGlobalScale != AppScene.currentGlobalScale) {
            currentGlobalScale = AppScene.currentGlobalScale;
            mainTaskOuterVBox.getTransforms().add(new Scale(currentGlobalScale, currentGlobalScale, 0, 0));
        }
    }

    public boolean isSet() { return (!actionList.isEmpty() && actionList.getFirst().isSet()); }
    public void openTaskPane() { AppScene.displayNewCenterNode(taskScrollPane); }
    private void backToPreviousAction(MouseEvent event) { AppScene.backToOperationScene(); }
    public void changeTaskName(String name) { taskNameLabel.setText(name); }

    // ------------------------------------------------------
    private void addNewActionPane(MouseEvent event) {
        if (AppScene.isOperationRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Operation is running - cannot modify");
            return;
        }
        if (!actionList.isEmpty() && !actionList.getLast().isSet()) {
            AppScene.addLog(LogLevel.INFO, className, "Recent action is not set");
            return;
        }
        addSavedData(null);
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
            int numberOfActions = taskVBox.getChildren().size();
            if (numberOfActions == 0)
                actionController.disablePreviousOptions();
            taskVBox.getChildren().add(actionPane);
            actionList.add(actionController);
            // update side menu
            Node actionHBoxLabel = SideMenuController.getNewSideHBoxLabel(actionController.getActionNameLabel(),
                    null, actionController, this);
            taskSideContent.getChildren().add(actionHBoxLabel);
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading and adding action pane: " + e.getMessage());
        }
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
    private void removeSelectedActionPane(MouseEvent event) {
        if (currentSelectedActionPane == null)
            return;
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on remove selected action");
        int changeIndex = taskVBox.getChildren().indexOf(currentSelectedActionPane);
        removeAction(changeIndex);
        currentSelectedActionPane = null;
    }
    private void removeAction(int changeIndex) {
        actionList.remove(changeIndex);
        if (changeIndex == 0 && !actionList.isEmpty())
            actionList.getFirst().disablePreviousOptions();
        taskVBox.getChildren().remove(changeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Removed selected action: " + changeIndex);
        //update side menu
        taskSideContent.getChildren().remove(changeIndex);
    }
    @Override
    public void removeSavedData(JobDataController jobDataController) {
        int changeIndex = actionList.indexOf((ActionController) jobDataController);
        removeAction(changeIndex);
    }

    private void setSelectedAction(Node actionPane) { actionPane.setStyle("-fx-border-color: black; -fx-border-width: 1px;"); }
    private void setUnSelectedAction(Node actionPane) { actionPane.setStyle(""); }

    private void moveActionUp(MouseEvent event) {
        if (currentSelectedActionPane == null)
            return;
        ObservableList<Node> children = taskVBox.getChildren();
        int numberOfActions = children.size();
        if (numberOfActions < 2)
            return;
        int selectedActionPaneIndex = children.indexOf(currentSelectedActionPane);
        int changeIndex = selectedActionPaneIndex+1;
        if (changeIndex == numberOfActions)
            return;
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on move-up selected action: " + changeIndex);
        updateActionPaneList(children, selectedActionPaneIndex, changeIndex);
        //update side menu
        updateActionSideContent(selectedActionPaneIndex, changeIndex);
    }
    private void moveActionDown(MouseEvent event) {
        if (currentSelectedActionPane == null)
            return;
        ObservableList<Node> children = taskVBox.getChildren();
        int numberOfActions = children.size();
        if (numberOfActions < 2)
            return;
        int selectedActionPaneIndex = children.indexOf(currentSelectedActionPane);
        if (selectedActionPaneIndex == 0)
            return;
        int changeIndex = selectedActionPaneIndex-1;
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on move-down selected action: " + changeIndex);
        updateActionPaneList(children, selectedActionPaneIndex, changeIndex);
        //update side menu
        updateActionSideContent(selectedActionPaneIndex, changeIndex);
    }
    private void updateActionPaneList(ObservableList<Node> children, int selectedIndex, int changeIndex) {
        actionList.add(changeIndex, actionList.remove(selectedIndex));
        changeActionPreviousOptions(changeIndex);
        children.remove(currentSelectedActionPane);
        children.add(changeIndex, currentSelectedActionPane);
    }
    private void updateActionSideContent(int selectedActionPaneIndex, int changeIndex) {
        ObservableList<Node> actionSideContent = taskSideContent.getChildren();
        Node temp = actionSideContent.get(selectedActionPaneIndex);
        actionSideContent.remove(selectedActionPaneIndex);
        actionSideContent.add(changeIndex, temp);
    }
    private void changeActionPreviousOptions(int index) {
        if (index < 2) {
            actionList.get(0).disablePreviousOptions();
            actionList.get(1).enablePreviousOptions();
        }
    }

    // ------------------------------------------------------
    @Override
    public AppLevel getAppLevel() {
        return null;
    }

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
    public void takeToDisplay() {
        AppScene.addLog(LogLevel.WARN, className, "Empty take to display is called - advice to recheck");
    }
}