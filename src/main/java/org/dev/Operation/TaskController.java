package org.dev.Operation;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import lombok.Getter;
import org.dev.App;
import org.dev.Operation.Data.ActionData;
import org.dev.Operation.Data.TaskData;
import org.dev.SideMenuHierarchy;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TaskController implements Initializable, MainJobController {
    @FXML
    private Group mainTaskGroup;
    @FXML
    private Label taskNameLabel;
    @FXML
    private ScrollPane taskScrollPane;
    @FXML
    private VBox mainTaskOuterVBox, taskVBox;
    @FXML
    private Group backButton;
    @FXML
    private StackPane removeActionButton, moveActionUpButton, moveActionDownButton, addNewActionButton;

    @Getter
    private final List<ActionController> actionList = new ArrayList<>();
    @Getter
    private SideMenuHierarchy taskSideMenuHierarchy;
    private double currentGlobalScale = 1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addNewActionButton.setOnMouseClicked(this::addNewActionPane);
        removeActionButton.setOnMouseClicked(this::removeSelectedActionPane);
        moveActionDownButton.setOnMouseClicked(this::moveActionDown);
        moveActionUpButton.setOnMouseClicked(this::moveActionUp);
        backButton.setOnMouseClicked(this::backToPrevious);
        taskSideMenuHierarchy = new SideMenuHierarchy(taskNameLabel, this);
    }

    public boolean isSet() { return (!actionList.isEmpty() && actionList.getFirst().isSet()); }

    public Node getTaskPane() {
        if (currentGlobalScale != App.currentGlobalScale) {
            currentGlobalScale = App.currentGlobalScale;
            mainTaskOuterVBox.getTransforms().add(new Scale(currentGlobalScale, currentGlobalScale, 0, 0));
        }
        return mainTaskGroup;
    }

    private void backToPrevious(MouseEvent event) { App.backToPrevious(); }
    public void changeTaskName(String name) { taskNameLabel.setText(name); }

    // ------------------------------------------------------
    private void addNewActionPane(MouseEvent event) {
        if (!actionList.isEmpty() && !actionList.getLast().isSet()) {
            System.out.println("Recent action is not set");
            return;
        }
        try {
            addNewAction(null);
        } catch (IOException e) {
            System.out.println("Fail loading action pane in task controller");
        }
    }
    private void addNewAction(ActionData actionData) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("actionPane.fxml"));
        Pane actionPane = loader.load();
        actionPane.setOnMouseClicked(this::selectTheActionPane);
        ActionController actionController = loader.getController();
        if (actionData != null)
            actionController.loadSavedActionData(actionData);
        int numberOfActions = taskVBox.getChildren().size();
        if (numberOfActions == 0)
            actionController.disablePreviousOptions();
        taskVBox.getChildren().add(numberOfActions, actionPane);
        addNewActionController(actionController);
        taskSideMenuHierarchy.addSubHierarchy(actionController.getActionSideMenuHierarchy());
    }
    public void addNewActionController(ActionController actionController) {
        actionList.add(actionController);
    }

    public void changeTaskScrollPaneView(Pane mainActionPane) {
        double targetPaneY = mainActionPane.getBoundsInParent().getMinY();
        double contentHeight = taskScrollPane.getContent().getBoundsInLocal().getHeight();
        double scrollPaneHeight = taskScrollPane.getViewportBounds().getHeight();

        double vValue = targetPaneY / (contentHeight - scrollPaneHeight);
        taskScrollPane.setVvalue(Math.min(vValue, 1.00));
    }

    // ------------------------------------------------------
    private Pane currentSelectedActionPane = null;
    private void selectTheActionPane(MouseEvent event) { 
        try {
            if (currentSelectedActionPane != null)
                setUnSelectedAction(currentSelectedActionPane);
            currentSelectedActionPane = (Pane) event.getSource();
            setSelectedAction(currentSelectedActionPane);
        } catch (Exception e) {
            System.out.println("Fail assigning current selected action pane");
        }
    }
    private void removeSelectedActionPane(MouseEvent event) {
        if (currentSelectedActionPane != null) {
            int changeIndex = taskVBox.getChildren().indexOf(currentSelectedActionPane);
            actionList.remove(changeIndex);
            if (changeIndex == 0 && !actionList.isEmpty())
                actionList.getFirst().disablePreviousOptions();
            taskVBox.getChildren().remove(currentSelectedActionPane);
            System.out.println("Removed the selected action pane");
            currentSelectedActionPane = null;
        }
    }
    private void setSelectedAction(Pane actionPane) { actionPane.setStyle("-fx-border-color: black; -fx-border-width: 1px;"); }
    private void setUnSelectedAction(Pane actionPane) { actionPane.setStyle(""); }

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
        actionList.add(changeIndex, actionList.remove(selectedActionPaneIndex));
        changeActionPreviousOptions(changeIndex);
        children.remove(currentSelectedActionPane);
        children.add(changeIndex, currentSelectedActionPane);
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
        actionList.add(changeIndex, actionList.remove(selectedActionPaneIndex));
        changeActionPreviousOptions(changeIndex);
        children.remove(currentSelectedActionPane);
        children.add(changeIndex, currentSelectedActionPane);
    }
    private void changeActionPreviousOptions(int index) {
        if (index < 2) {
            actionList.get(0).disablePreviousOptions();
            actionList.get(1).enablePreviousOptions();
        }
    }

    // ------------------------------------------------------
    public boolean runTask() {
        System.out.println("Start running task: " + taskNameLabel.getText());
        boolean pass = false;
        for (ActionController actionController : actionList) {
            String actionName = actionController.getAction().getActionName();
            if (pass && actionController.isPreviousPass()) {
                System.out.println("Skipping action " + actionName + " as previous is passed");
                continue;
            }
            System.out.println("Performing action: " + actionName);
            pass = actionController.performAction();
            if (!actionController.isRequired())
                pass = true;
            else if (!pass) { // action is required but failed
                System.out.println("Fail performing action: " + actionName);
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------
    public TaskData getTaskData() {
        TaskData taskData = new TaskData();
        List<ActionData> actionData = new ArrayList<>();
        for (ActionController actionController : actionList)
            actionData.add(actionController.getActionData());
        taskData.setActionDataList(actionData);
        return taskData;
    }

    public void loadSavedTaskData(TaskData taskData) throws IOException {
        if (taskData == null)
            throw new NullPointerException("Task data is null");
        taskNameLabel.setText(taskData.getTask().getTaskName());
        for (ActionData actionData : taskData.getActionDataList())
            addNewAction(actionData);
    }

    @Override
    public void takeToDisplay() {}
}