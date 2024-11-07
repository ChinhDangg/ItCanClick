package org.dev.Operation;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Group;
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
import org.dev.Operation.Data.OperationData;
import org.dev.Operation.Data.TaskData;
import org.dev.SideMenuController;

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
    @Getter
    private final Label operationNameLabel = new Label();
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
    @Getter
    private Operation operation = new Operation();
    private double currentGlobalScale = 1;

    @Getter
    private VBox taskGroupVBoxSideContent = new VBox();

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
    }

    public boolean isSet() { return !taskList.isEmpty() && taskList.getFirst().isSet(); }
    public void setVisible(boolean visible) { mainOperaiontGroup.setVisible(visible); }

    @Override
    public void takeToDisplay() {
        System.out.println("Operation take to display");
        AppScene.closeActionMenuPane();
        AppScene.closeConditionMenuPane();
        if (mainOperationVBox.getScene() != null) {
            if (AppScene.currentDisplayNode != null && AppScene.currentDisplayNode.getScene() != null)
                AppScene.backToOperationScene();
            return;
        }
        System.out.println("This will probably never get reached. If seen then recheck");
        loadMainOperationVBox();
        AppScene.primaryCenterStackPane.getChildren().clear();
        AppScene.primaryCenterStackPane.getChildren().add(mainOperaiontGroup);
    }
    private void loadMainOperationVBox() {
        double offset = ((VBox) mainOperationVBox.getChildren().getFirst()).getPrefHeight();
        double newScrollHeight = AppScene.primaryBorderPane.getPrefHeight() - offset;
        if (currentGlobalScale != AppScene.currentGlobalScale) {
            currentGlobalScale = AppScene.currentGlobalScale;
            mainOperationVBox.getTransforms().add(new Scale(currentGlobalScale, currentGlobalScale, 0, 0));
            newScrollHeight = (AppScene.primaryBorderPane.getPrefHeight() - offset * currentGlobalScale) / currentGlobalScale;
        }
        operationScrollPane.setPrefHeight(newScrollHeight - 25);
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
    }

    // ------------------------------------------------------
    private void addMinimizedTaskAction(MouseEvent event) {
        if (AppScene.isOperationRunning) {
            System.out.println("Operation is running, cannot modify");
            return;
        }
        if (!taskList.isEmpty() && !taskList.getLast().isSet()) {
            System.out.println("Recent minimized task is not set");
            return;
        }
        try {
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
        // update side menu
        VBox taskActionVBox = controller.getTaskController().getActionGroupVBoxSideContent();
        HBox taskLabelHBox = SideMenuController.getDropDownHBox(taskActionVBox, controller.getTaskNameLabel(), controller);
        taskGroupVBoxSideContent.getChildren().add(new VBox(taskLabelHBox, taskActionVBox));
    }
    public void changeOperationScrollPaneView(StackPane minimizedTaskLayerStackPane) {
        Pane innerChildPane = (Pane) minimizedTaskLayerStackPane.getChildren().getFirst();
        selectTheTaskPane(innerChildPane);
        changeScrollPaneView(minimizedTaskLayerStackPane);
    }
    private void changeScrollPaneView(StackPane minimizedTaskLayerStackPane) {
        double targetPaneY = minimizedTaskLayerStackPane.getBoundsInParent().getMinY();
        double contentHeight = operationScrollPane.getContent().getBoundsInLocal().getHeight();
        double scrollPaneHeight = operationScrollPane.getViewportBounds().getHeight();
        double vValue = Math.min(targetPaneY / (contentHeight - scrollPaneHeight), 1.00);
        operationScrollPane.setVvalue(vValue);
//        Lock lock = new ReentrantLock();
//        Condition condition = lock.newCondition();
//        new Thread(() -> {
//            try {
//                lock.lock();
//                while (operationScrollPane.getVvalue() != vValue) {
//                    operationScrollPane.setVvalue(vValue);
//                    if (condition.await(1, TimeUnit.SECONDS))
//                        break;
//                }
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            } finally { lock.unlock(); }
//        }).start();
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
        taskList.add(changeIndex, taskList.remove(selectedTaskPaneIndex));
        updateTaskPreviousOption(changeIndex);
        children.remove(selectedNode);
        children.add(changeIndex, selectedNode);
        updateTaskIndex(changeIndex-1);
        //update side menu
        ObservableList<Node> taskSideContent = taskGroupVBoxSideContent.getChildren();
        Node temp = taskSideContent.get(selectedTaskPaneIndex);
        taskSideContent.remove(selectedTaskPaneIndex);
        taskSideContent.add(changeIndex, temp);
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
        updateTaskPreviousOption(changeIndex);
        children.remove(selectedNode);
        children.add(changeIndex, selectedNode);
        updateTaskIndex(changeIndex);
        //update side menu
        ObservableList<Node> taskSideContent = taskGroupVBoxSideContent.getChildren();
        Node temp = taskSideContent.get(selectedTaskPaneIndex);
        taskSideContent.remove(selectedTaskPaneIndex);
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
        return operationData;
    }

    public void loadSavedOperationData(OperationData operationData) throws IOException {
        if (operationData == null)
            throw new NullPointerException("Operation data is null");
        this.operation = operationData.getOperation();
        updateOperationName(operation.getOperationName());
        for (TaskData data : operationData.getTaskDataList())
            addNewMinimizedTask(data);
    }
}