package org.dev.Operation;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import lombok.Getter;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import org.dev.Operation.Data.OperationData;
import org.dev.Operation.Data.TaskData;
import org.dev.SideMenu.SideMenuController;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class OperationController implements Initializable, Serializable, MainJobController {

    @FXML
    private ScrollPane operationScrollPane;
    @FXML
    private VBox mainOperationVBox;
    @FXML
    private TextField renameTextField;
    @Getter
    private final Label operationNameLabel = new Label();
    @FXML
    private StackPane removeTaskButton, moveTaskUpButton, moveTaskDownButton;
    @FXML
    private VBox operationVBox;
    @FXML
    private HBox addTaskButton;

    @Getter
    private final List<MinimizedTaskController> taskList = new ArrayList<>();
    @Getter
    private Operation operation = new Operation();
    private double currentGlobalScale = 1;
    @Getter
    private VBox taskGroupVBoxSideContent = new VBox();
    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addTaskButton.setOnMouseClicked(this::addMinimizedTaskAction);
        removeTaskButton.setOnMouseClicked(this::removeSelectedTaskPane);
        moveTaskUpButton.setOnMouseClicked(this::moveTaskUp);
        moveTaskDownButton.setOnMouseClicked(this::moveTaskDown);
        operationNameLabel.setText(renameTextField.getText());
        renameTextField.focusedProperty().addListener((_, _, newValue) -> {
            if (!newValue) {
                //System.out.println("TextField lost focus");
                changeOperationName();
            }
        });
        loadMainOperationVBox();
        taskGroupVBoxSideContent.setPadding(new Insets(0, 0, 0, 15));
        operationVBox.heightProperty().addListener((_, _, _) -> Platform.runLater(() -> operationScrollPane.setVvalue(1.0)));
    }

    public boolean isSet() { return !taskList.isEmpty() && taskList.getFirst().isSet(); }
    public void setVisible(boolean visible) { getParentNode().setVisible(visible); }
    public Node getParentNode() { return operationScrollPane; }

    @Override
    public void takeToDisplay() {
        AppScene.closeActionMenuPane();
        AppScene.closeConditionMenuPane();
        AppScene.backToOperationScene();
        AppScene.addLog(LogLevel.TRACE, className, "Take to display");
    }

    private void loadMainOperationVBox() {
        if (currentGlobalScale != AppScene.currentGlobalScale) {
            currentGlobalScale = AppScene.currentGlobalScale;
            mainOperationVBox.getTransforms().add(new Scale(currentGlobalScale, currentGlobalScale, 0, 0));
        }
        AppScene.addLog(LogLevel.TRACE, className, "Loaded operation vbox with scale: " + currentGlobalScale);
    }

    private void changeOperationName() {
        String name = renameTextField.getText();
        name = name.strip();
        if (name.isBlank()) {
            renameTextField.setText(operation.getOperationName());
            return;
        }
        operation.setOperationName(name);
        updateOperationName(name);
    }
    private void updateOperationName(String name) {
        operationNameLabel.setText(name);
        renameTextField.setText(name);
        AppScene.addLog(LogLevel.DEBUG, className, "Updated operation name: " + name);
    }

    // ------------------------------------------------------
    private void addMinimizedTaskAction(MouseEvent event) {
        if (AppScene.isOperationRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Operation is running - cannot modify");
            return;
        }
        if (!taskList.isEmpty() && !taskList.getLast().isSet()) {
            AppScene.addLog(LogLevel.INFO, className, "Recent minimized task is not set");
            return;
        }
        addNewMinimizedTask(null);
    }
    private void addNewMinimizedTask(TaskData taskData) {
        try {
            AppScene.addLog(LogLevel.TRACE, className, "Loading Minimized Task Pane");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("minimizedTaskPane.fxml"));
            Node taskPane = loader.load();
            taskPane.setOnMouseClicked(this::selectTheTaskPaneAction);
            MinimizedTaskController controller = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Minimized Task Pane");
            if (taskData != null)
                controller.loadSavedTaskData(taskData);
            int numberOfTask = operationVBox.getChildren().size();
            if (numberOfTask == 0)
                controller.disablePreviousOption();
            controller.setTaskIndex(numberOfTask + 1);
            operationVBox.getChildren().add(numberOfTask, taskPane);
            taskList.add(controller);
            createTaskSideContent(controller);
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading and adding minimized task pane: " + e.getMessage());
        }
    }
    private void createTaskSideContent(MinimizedTaskController controller) {
        VBox taskActionVBox = controller.getTaskController().getActionGroupVBoxSideContent();
        HBox taskLabelHBox = SideMenuController.getDropDownHBox(taskActionVBox, controller.getTaskNameLabel(), controller);
        taskGroupVBoxSideContent.getChildren().add(new VBox(taskLabelHBox, taskActionVBox));
        AppScene.addLog(LogLevel.TRACE, className, "Created task side content");
    }

    // ------------------------------------------------------
    public void changeOperationScrollPaneView(Node minimizedTaskPane) {
        selectTheTaskPane(minimizedTaskPane);
        changeScrollPaneView(minimizedTaskPane);
    }
    private void changeScrollPaneView(Node minimizedTaskPane) {
        double targetPaneY = minimizedTaskPane.getBoundsInParent().getMinY() * currentGlobalScale;
        double contentHeight = operationScrollPane.getContent().getBoundsInLocal().getHeight();
        double scrollPaneHeight = operationScrollPane.getViewportBounds().getHeight();
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
    private void removeSelectedTaskPane(MouseEvent event) {
        if (currentSelectedTaskPane == null)
            return;
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on remove selected task");
        int changeIndex = operationVBox.getChildren().indexOf(currentSelectedTaskPane.getParent());
        taskList.remove(changeIndex);
        if (changeIndex == 0 && !taskList.isEmpty())
            taskList.getFirst().disablePreviousOption();
        operationVBox.getChildren().remove(currentSelectedTaskPane.getParent());
        AppScene.addLog(LogLevel.DEBUG, className, "Removed selected task: " + changeIndex);
        currentSelectedTaskPane = null;
        updateTaskIndex(changeIndex);
        // update side menu
        taskGroupVBoxSideContent.getChildren().remove(changeIndex);
    }
    private void moveTaskUp(MouseEvent event) {
        if (currentSelectedTaskPane == null)
            return;
        ObservableList<Node> children = operationVBox.getChildren();
        int numberOfTasks = children.size();
        if (numberOfTasks < 2)
            return;
        Node selectedNode = currentSelectedTaskPane.getParent();
        int selectedTaskPaneIndex = children.indexOf(selectedNode);
        int changeIndex = selectedTaskPaneIndex+1;
        if (changeIndex == numberOfTasks)
            return;
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on move-up selected task: " + changeIndex);
        updateTaskPaneList(children, selectedTaskPaneIndex, changeIndex);
        updateTaskIndex(changeIndex-1);
        //update side menu
        updateTaskSideContent(selectedTaskPaneIndex, changeIndex);
    }
    private void moveTaskDown(MouseEvent event) {
        if (currentSelectedTaskPane == null)
            return;
        ObservableList<Node> children = operationVBox.getChildren();
        int numberOfTasks = children.size();
        if (numberOfTasks < 2)
            return;
        Node selectedNode = currentSelectedTaskPane.getParent();
        int selectedTaskPaneIndex = children.indexOf(selectedNode);
        if (selectedTaskPaneIndex == 0)
            return;
        int changeIndex = selectedTaskPaneIndex-1;
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on move-down selected task: " + changeIndex);
        updateTaskPaneList(children, selectedTaskPaneIndex, changeIndex);
        updateTaskIndex(changeIndex);
        //update side menu
        updateTaskSideContent(selectedTaskPaneIndex, changeIndex);
    }
    private void updateTaskPaneList(ObservableList<Node> children, int selectedIndex, int changeIndex) {
        taskList.add(changeIndex, taskList.remove(selectedIndex));
        updateTaskPreviousOption(changeIndex);
        children.remove(currentSelectedTaskPane.getParent());
        children.add(changeIndex, currentSelectedTaskPane.getParent());
    }
    private void updateTaskSideContent(int selectedIndex, int changeIndex) {
        ObservableList<Node> taskSideContent = taskGroupVBoxSideContent.getChildren();
        Node temp = taskSideContent.get(selectedIndex);
        taskSideContent.remove(selectedIndex);
        taskSideContent.add(changeIndex, temp);
    }
    private void updateTaskPreviousOption(int index) {
        if (index < 2) {
            taskList.get(0).disablePreviousOption();
            taskList.get(1).enablePreviousOption();
        }
    }
    private void updateTaskIndex(int start) {
        for (int j = start; j < taskList.size(); j++)
            taskList.get(j).setTaskIndex(j+1);
    }

    // ------------------------------------------------------
    public OperationData getOperationData() {
        OperationData operationData = new OperationData();
        operationData.setOperation(operation);
        List<TaskData> taskDataList = new ArrayList<>();
        for (MinimizedTaskController taskController : taskList)
            taskDataList.add(taskController.getTaskData());
        operationData.setTaskDataList(taskDataList);
        AppScene.addLog(LogLevel.TRACE, className, "Got operation data");
        return operationData;
    }

    public void loadSavedOperationData(OperationData operationData) {
        if (operationData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Operation data is null - cannot load from save");
            return;
        }
        this.operation = operationData.getOperation();
        updateOperationName(operation.getOperationName());
        for (TaskData data : operationData.getTaskDataList())
            addNewMinimizedTask(data);
    }
}