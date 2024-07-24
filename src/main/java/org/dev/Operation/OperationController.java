package org.dev.Operation;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import lombok.Getter;
import org.dev.App;
import org.dev.Operation.Data.OperationData;
import org.dev.Operation.Data.TaskData;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class OperationController implements Initializable, Serializable, MainJobController {
    @FXML
    private Group mainOperaiontGroup;
    @FXML
    private VBox mainOperationVBox;
    @FXML
    private TextField renameTextField;
    @FXML
    private StackPane removeTaskButton, moveTaskUpButton, moveTaskDownButton;
    @FXML
    private ScrollPane operationScrollPane;
    @FXML
    private VBox operationVBox;
    @FXML
    private HBox addTaskButton;

    @Getter
    private final List<MinimizedTaskController> taskList = new ArrayList<>();

    private double currentGlobalScale = 1;

    @Getter
    private Operation operation = new Operation();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addTaskButton.setOnMouseClicked(this::addMinimizedTask);
        removeTaskButton.setOnMouseClicked(this::removeSelectedTaskPane);
        moveTaskUpButton.setOnMouseClicked(this::moveTaskUp);
        moveTaskDownButton.setOnMouseClicked(this::moveTaskDown);
        renameTextField.focusedProperty().addListener((_, _, newValue) -> {
            if (!newValue) {
                //System.out.println("TextField lost focus");
                changeOperationName();
            }
        });
        loadMainTaskVBox();
    }

    public boolean isSet() { return !taskList.isEmpty() && taskList.getFirst().isSet(); }
    public void setVisible(boolean visible) { mainOperaiontGroup.setVisible(visible); }

    @Override
    public void takeToDisplay() {
        if (mainOperationVBox.getScene() != null) {
            if (App.currentDisplayNode != null && App.currentDisplayNode.getScene() != null)
                App.backToPrevious();
            return;
        }
        System.out.println("This will probably never get reached. If seen then recheck");
        loadMainTaskVBox();
        App.primaryCenterStackPane.getChildren().clear();
        App.primaryCenterStackPane.getChildren().add(mainOperaiontGroup);
    }
    private void loadMainTaskVBox() {
        if (currentGlobalScale != App.currentGlobalScale) {
            currentGlobalScale = App.currentGlobalScale;
            mainOperationVBox.getTransforms().add(new Scale(currentGlobalScale, currentGlobalScale, 0, 0));
        }
    }

    private void changeOperationName() {
        String name = renameTextField.getText();
        name = name.trim();
        if (name.isBlank())
            return;
        operation.setOperationName(name);
        removeTaskButton.requestFocus();
        renameTextField.setText(name);
    }

    // ------------------------------------------------------
    private void addMinimizedTask(MouseEvent event) {
        try {
            if (!taskList.isEmpty() && !taskList.getLast().isSet()) {
                System.out.println("Recent minimized task is not set");
                return;
            }
            addNewMinimizedTask(null);
        } catch (Exception e) {
            System.out.println("Fail loading minimized task pane");
        }
    }
    private void addNewMinimizedTask(TaskData taskData) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("minimizedTaskPane.fxml"));
        StackPane taskPane = loader.load();
        taskPane.getChildren().getFirst().setOnMouseClicked(this::selectTheTaskPane);
        MinimizedTaskController controller = loader.getController();
        if (taskData != null)
            controller.loadSavedTaskData(taskData);
        int numberOfTask = operationVBox.getChildren().size();
        if (numberOfTask == 0)
            controller.disablePreviousOption();
        controller.setTaskIndex(numberOfTask+1);
        operationVBox.getChildren().add(numberOfTask, taskPane);
        taskList.add(controller);
    }
    public void changeOperationScrollPaneView(StackPane minimizedTaskLayerStackPane) {
        Pane innerChildPane = (Pane) minimizedTaskLayerStackPane.getChildren().getFirst();
        selectTheTaskPane(innerChildPane);
        operationScrollPane.setVvalue(0.0);
        Thread thread = new Thread(() -> changeScrollPaneView(minimizedTaskLayerStackPane));
        thread.start();
    }
    private void changeScrollPaneView(StackPane minimizedTaskLayerStackPane) {
        double targetPaneY = minimizedTaskLayerStackPane.getBoundsInParent().getMinY();
        double contentHeight = operationScrollPane.getContent().getBoundsInLocal().getHeight();
        double scrollPaneHeight = operationScrollPane.getViewportBounds().getHeight();
        double vValue = Math.min(targetPaneY / (contentHeight - scrollPaneHeight), 1.00);
        try {
            while (operationScrollPane.getVvalue() != vValue) {
                operationScrollPane.setVvalue(vValue);
                Thread.sleep(500);
            }
        } catch (Exception e) {
            System.out.println("bullshit");
        }
    }


    // ------------------------------------------------------
    private Pane currentSelectedTaskPane = null;
    private void selectTheTaskPane(MouseEvent event) {
        try {
            selectTheTaskPane((Pane) event.getSource());
        } catch (Exception e) {
            System.out.println("Fail assigning current selected task pane");
        }
    }
    private void selectTheTaskPane(Pane taskPane) {
        if (currentSelectedTaskPane != null)
            setUnselected(currentSelectedTaskPane);
        currentSelectedTaskPane = taskPane;
        setSelected(currentSelectedTaskPane);
    }
    private void setSelected(Pane taskPane) { taskPane.setStyle("-fx-border-color: black; -fx-border-width: 1px;"); }
    private void setUnselected(Pane taskPane) { taskPane.setStyle(""); }

    // ------------------------------------------------------
    private void removeSelectedTaskPane(MouseEvent event) {
        if (currentSelectedTaskPane == null)
            return;
        int changeIndex = operationVBox.getChildren().indexOf(currentSelectedTaskPane.getParent());
        taskList.remove(changeIndex);
        if (changeIndex == 0 && !taskList.isEmpty())
            taskList.getFirst().disablePreviousOption();
        operationVBox.getChildren().remove(currentSelectedTaskPane.getParent());
        System.out.println("Removed the selected task pane");
        currentSelectedTaskPane = null;
        updateTaskIndex(changeIndex);
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
        taskList.add(changeIndex, taskList.remove(selectedTaskPaneIndex));
        changeTaskPreviousOption(changeIndex);
        children.remove(selectedNode);
        children.add(changeIndex, selectedNode);
        updateTaskIndex(changeIndex-1);
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
        taskList.add(changeIndex, taskList.remove(selectedTaskPaneIndex));
        changeTaskPreviousOption(changeIndex);
        children.remove(selectedNode);
        children.add(changeIndex, selectedNode);
        updateTaskIndex(changeIndex);
    }
    private void changeTaskPreviousOption(int index) {
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
    public void startOperation() {
        Thread thread = new Thread(this::runOperation);
        thread.start();
    }
    private void runOperation() {
        System.out.println("Start running operation: " + operation.getOperationName());
        boolean pass = false;
        for (MinimizedTaskController taskController : taskList) {
            String taskName = taskController.getTaskName();
            if (pass && taskController.isPreviousPass()) {
                System.out.println("Skipping task " + taskName + " as previous is passed");
                continue;
            }
            pass = taskController.runTask();
            if (!taskController.isRequired())
                pass = true;
            else if (!pass) { // task is required but failed
                System.out.println("Fail performing task: " + taskName);
                break;
            }
        }
    }

    // ------------------------------------------------------
    public OperationData getOperationData() {
        OperationData operationData = new OperationData();
        operationData.setOperation(operation);
        List<TaskData> taskDataList = new ArrayList<>();
        for (MinimizedTaskController taskController : taskList)
            taskDataList.add(taskController.getTaskData());
        operationData.setTaskDataList(taskDataList);
        return operationData;
    }

    public void loadSavedOperationData(OperationData operationData) throws IOException {
        if (operationData == null)
            throw new NullPointerException("Operation data is null");
        this.operation = operationData.getOperation();
        renameTextField.setText(operation.getOperationName());
        for (TaskData data : operationData.getTaskDataList())
            addNewMinimizedTask(data);
    }
}