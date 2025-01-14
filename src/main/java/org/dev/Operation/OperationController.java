package org.dev.Operation;

import javafx.application.Platform;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import lombok.Getter;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.Operation.Data.AppData;
import org.dev.Operation.Data.OperationData;
import org.dev.Operation.Data.TaskData;
import org.dev.SideMenu.SideMenuController;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class OperationController implements Initializable, Serializable, DataController {

    @FXML
    private ScrollPane operationScrollPane;
    @FXML
    private VBox mainOperationVBox;
    @FXML
    private TextField renameTextField;
    @FXML
    private StackPane removeTaskButton, moveTaskUpButton, moveTaskDownButton;
    @FXML
    private VBox operationVBox;
    @FXML
    private HBox addTaskButton;

    private double currentGlobalScale = 1;
    private final String className = this.getClass().getSimpleName();
    @Getter
    private final List<MinimizedTaskController> taskList = new ArrayList<>();
    @Getter
    private Operation operation = new Operation();
    @Getter
    private final Label operationNameLabel = new Label();
    @Getter
    private VBox operationSideContent = new VBox();

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
        addSavedData(null);
    }
    @Override
    public void addSavedData(AppData taskData) {
        try {
            AppScene.addLog(LogLevel.TRACE, className, "Loading Minimized Task Pane");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("minimizedTaskPane.fxml"));
            Node taskPane = loader.load();
            taskPane.setOnMouseClicked(this::selectTheTaskPaneAction);
            MinimizedTaskController controller = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Minimized Task Pane");
            if (taskData != null)
                controller.loadSavedData(taskData);
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
        VBox taskSideContent = controller.getTaskController().getTaskSideContent();
        Node taskLabelHBox = SideMenuController.getNewSideHBoxLabel(controller.getTaskNameLabel(), taskSideContent, controller, this);
        operationSideContent.getChildren().addAll(taskLabelHBox, taskSideContent);
        AppScene.addLog(LogLevel.TRACE, className, "Created operation side content");
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
        int changeIndex = operationVBox.getChildren().indexOf(currentSelectedTaskPane);
        removeTask(changeIndex);
        currentSelectedTaskPane = null;
    }
    private void removeTask(int changeIndex) {
        taskList.remove(changeIndex);
        if (changeIndex == 0 && !taskList.isEmpty())
            taskList.getFirst().disablePreviousOption();
        operationVBox.getChildren().remove(changeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Removed selected task: " + changeIndex);
        updateTaskIndex(changeIndex);
        // update side menu
        operationSideContent.getChildren().remove(changeIndex * 2);
        operationSideContent.getChildren().remove(changeIndex * 2);
    }
    @Override
    public void removeSavedData(DataController dataController) {
        int changeIndex = taskList.indexOf((MinimizedTaskController) dataController);
        removeTask(changeIndex);
    }

    private void moveTaskUp(MouseEvent event) {
        System.out.println(operationSideContent.getChildren().size());
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
        ObservableList<Node> taskSideContent = operationSideContent.getChildren();
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
    @Override
    public AppLevel getAppLevel() {
        return AppLevel.Operation;
    }

    @Override
    public AppData getSavedData() {
        OperationData operationData = new OperationData();
        operationData.setOperation(operation);
        List<TaskData> taskDataList = new ArrayList<>();
        for (MinimizedTaskController taskController : taskList)
            taskDataList.add((TaskData) taskController.getSavedData());
        operationData.setTaskDataList(taskDataList);
        AppScene.addLog(LogLevel.TRACE, className, "Got operation data");
        return operationData;
    }

    @Override
    public void loadSavedData(AppData appData) {
        if (appData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Operation data is null - cannot load from save");
            return;
        }
        OperationData operationData = (OperationData) appData;
        this.operation = operationData.getOperation();
        updateOperationName(operation.getOperationName());
        for (TaskData data : operationData.getTaskDataList())
            addSavedData(data);
    }
}