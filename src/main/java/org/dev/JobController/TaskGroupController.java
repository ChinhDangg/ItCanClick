package org.dev.JobController;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.JobData.JobData;
import org.dev.SideMenu.LeftMenu.SideMenuController;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TaskGroupController implements Initializable, JobDataController {
    @FXML
    private Label taskIndexLabel;
    @FXML
    private VBox taskGroupVBox;

    private final String className = this.getClass().getSimpleName();
    @Getter
    private final List<MinimizedTaskController> minimizedTaskList = new ArrayList<>();
    private VBox taskGroupSideContent = new VBox();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @Override
    public void takeToDisplay() {

    }

    @Override
    public AppLevel getAppLevel() {
        return null;
    }

    @Override
    public JobData getSavedData() {
        return null;
    }

    @Override
    public void loadSavedData(JobData jobData) {

    }

    @Override
    public void addSavedData(JobData taskData) {
        try {
            AppScene.addLog(LogLevel.TRACE, className, "Loading Minimized Task Pane");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("minimizedTaskPane.fxml"));
            Node taskPane = loader.load();
            //taskPane.setOnMouseClicked(this::selectTheTaskPaneAction);
            MinimizedTaskController controller = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Minimized Task Pane");
            if (taskData != null)
                controller.loadSavedData(taskData);
            int numberOfTask = taskGroupVBox.getChildren().size();
            if (numberOfTask == 0)
                controller.disablePreviousOption();
            controller.setTaskIndex(numberOfTask + 1);
            taskGroupVBox.getChildren().add(taskPane);
            minimizedTaskList.add(controller);
            createTaskGroupSideContent(controller);
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading and adding minimized task pane: " + e.getMessage());
        }
    }

    // ------------------------------------------------------
    private Node currentSelectedTaskPane = null;
    private void selectTheTaskPane(Node taskPane) {
        if (currentSelectedTaskPane != null)
            setUnselected(currentSelectedTaskPane);
        currentSelectedTaskPane = taskPane;
        setSelected(currentSelectedTaskPane);
    }
    private void setSelected(Node taskPane) { taskPane.setStyle("-fx-border-color: black; -fx-border-width: 1px;"); }
    private void setUnselected(Node taskPane) { taskPane.setStyle(""); }

    private void removeTask(int changeIndex) {
        minimizedTaskList.remove(changeIndex);
        if (changeIndex == 0 && !minimizedTaskList.isEmpty())
            minimizedTaskList.getFirst().disablePreviousOption();
        taskGroupVBox.getChildren().remove(changeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Removed selected task: " + changeIndex);
        updateTaskIndex(changeIndex);
        // update side menu
        removeTaskGroupSideContent(changeIndex);
    }

    @Override
    public void removeSavedData(JobDataController jobDataController) {
        int changeIndex = minimizedTaskList.indexOf((MinimizedTaskController) jobDataController);
        removeTask(changeIndex);
    }

    private void moveTaskUp(JobDataController jobDataController) {
        int numberOfTasks = minimizedTaskList.size();
        if (numberOfTasks < 2)
            return;
        int selectedTaskIndex = minimizedTaskList.indexOf((MinimizedTaskController) jobDataController);
        int changeIndex = selectedTaskIndex +1;
        if (changeIndex == numberOfTasks)
            return;
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on move-up selected task: " + changeIndex);
        updateTaskPaneList(selectedTaskIndex, changeIndex);
        updateTaskIndex(changeIndex-1);
        //update side menu
        updateTaskGroupSideContent(selectedTaskIndex, changeIndex);
    }
    private void moveTaskDown(JobDataController jobDataController) {
        int numberOfTasks = minimizedTaskList.size();
        if (numberOfTasks < 2)
            return;
        int selectedTaskIndex = minimizedTaskList.indexOf((MinimizedTaskController) jobDataController);
        if (selectedTaskIndex == 0)
            return;
        int changeIndex = selectedTaskIndex -1;
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on move-down selected task: " + changeIndex);
        updateTaskPaneList(selectedTaskIndex, changeIndex);
        updateTaskIndex(changeIndex);
        //update side menu
        updateTaskGroupSideContent(selectedTaskIndex, changeIndex);
    }

    private void updateTaskPaneList(int selectedIndex, int changeIndex) {
        ObservableList<Node> children = taskGroupVBox.getChildren();
        minimizedTaskList.add(changeIndex, minimizedTaskList.remove(selectedIndex));
        updateTaskPreviousOption(changeIndex);
        children.remove(currentSelectedTaskPane);
        children.add(changeIndex, currentSelectedTaskPane);
    }
    private void updateTaskPreviousOption(int index) {
        if (index < 2) {
            minimizedTaskList.get(0).disablePreviousOption();
            minimizedTaskList.get(1).enablePreviousOption();
        }
    }
    private void updateTaskIndex(int start) {
        for (int j = start; j < minimizedTaskList.size(); j++)
            minimizedTaskList.get(j).setTaskIndex(j+1);
    }

    // ------------------------------------------------------
    private void createTaskGroupSideContent(MinimizedTaskController controller) {
        VBox taskSideContent = controller.getTaskController().getTaskSideContent();
        Node taskLabelHBox = SideMenuController.getNewSideHBoxLabel(controller.getTaskNameLabel(), taskSideContent, controller, this);
        taskGroupSideContent.getChildren().add(new VBox(taskLabelHBox, taskSideContent));
        AppScene.addLog(LogLevel.TRACE, className, "Created task group side content");
    }
    private void removeTaskGroupSideContent(int changeIndex) {
        taskGroupSideContent.getChildren().remove(changeIndex);
    }
    private void updateTaskGroupSideContent(int selectedIndex, int changeIndex) {
        ObservableList<Node> taskSideContent = taskGroupSideContent.getChildren();
        Node temp = taskSideContent.get(selectedIndex);
        taskSideContent.remove(selectedIndex);
        taskSideContent.add(changeIndex, temp);
    }
}
