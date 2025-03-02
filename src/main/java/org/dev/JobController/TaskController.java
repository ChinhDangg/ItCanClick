package org.dev.JobController;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.Job.Task.Task;
import org.dev.Job.JobData;
import org.dev.jobManagement.JobStructure;
import org.dev.RunJob.JobRunController;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TaskController implements Initializable, JobDataController {

    @FXML
    private Node parentNode;
    @FXML
    private Label taskNameLabel;
    @FXML
    private VBox mainTaskOuterVBox, taskVBox;
    @FXML
    private Group backButton;
    @FXML
    private StackPane addNewActionButton;

    private JobStructure currentStructure;
    private JobData jobData = new JobData();
    private double currentGlobalScale = 1;

    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addNewActionButton.setOnMouseClicked(this::addNewActionPane);
        backButton.setOnMouseClicked(this::backToPreviousAction);
        loadMainTaskVBox();
    }

    public void setJobStructure(JobStructure structure) { currentStructure = structure; }

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
        addSavedData(null);
    }

    // ------------------------------------------------------
    private Node currentSelectedActionPane = null;
    private void selectTheActionPaneAction(MouseEvent event) {
        selectTheActionPane((Node) event.getSource());
    }
    public void selectTheActionPane(Node actionPane) {
        if (currentSelectedActionPane != null)
            setUnSelectedAction(currentSelectedActionPane);
        currentSelectedActionPane = actionPane;
        setSelectedAction(currentSelectedActionPane);
    }
    private void setSelectedAction(Node actionPane) { actionPane.setStyle("-fx-border-color: black; -fx-border-width: 1px;"); }
    private void setUnSelectedAction(Node actionPane) { actionPane.setStyle(""); }

    // ------------------------------------------------------
    @Override
    public boolean isSet() {
        if (currentStructure == null)
            return false;
        return !currentStructure.getSubJobStructures().isEmpty()
                && currentStructure.getSubJobStructures().getFirst().getCurrentController().isSet();
    }

    @Override
    public String getName() { return taskNameLabel.getText(); }

    @Override
    public Node getParentNode() { return parentNode; }

    @Override
    public AppLevel getAppLevel() { return AppLevel.Task; }

    @Override
    public void takeToDisplay() { AppScene.displayNewCenterNode(getParentNode()); }

    @Override
    public JobData getSavedData() {
        List<JobData> actionDataList = new ArrayList<>();
        for (JobStructure subJobStructure: currentStructure.getSubJobStructures())
            actionDataList.add(subJobStructure.getCurrentController().getSavedData());
        JobData taskData = new JobData(null, actionDataList);
        AppScene.addLog(LogLevel.TRACE, className, "Got Task data");
        return taskData;
    }

    @Override
    public JobData getSavedDataByReference() {
        List<JobData> actionDataList = new ArrayList<>();
        for (JobStructure subJobStructure: currentStructure.getSubJobStructures())
            actionDataList.add(subJobStructure.getCurrentController().getSavedDataByReference());
        jobData.setJobDataList(actionDataList);
        AppScene.addLog(LogLevel.TRACE, className, "Got Reference Task data");
        return jobData;
    }

    @Override
    public void loadSavedData(JobData newJobData) {
        if (newJobData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Task data is null - cannot load from save");
            return;
        }
        if (newJobData.isRef())
            currentStructure.markLabelAsRef();
        jobData = newJobData;
        Task task = (Task) jobData.getMainJob();
        taskNameLabel.setText(task.getTaskName());
        for (JobData actionData : jobData.getJobDataList())
            addSavedData(actionData);
    }

    @Override
    public JobStructure addSavedData(JobData actionData) {
        if (AppScene.isJobRunning()) {
            AppScene.addLog(LogLevel.INFO, className, "Another job is running - cannot modify");
            return null;
        }
        if (actionData == null)
            if (!currentStructure.getSubJobStructures().isEmpty() && !currentStructure.getSubJobStructures().getLast().getCurrentController().isSet()) {
                AppScene.addLog(LogLevel.INFO, className, "Recent Action is not set");
                return null;
            }
        try {
            AppScene.addLog(LogLevel.TRACE, className, "Loading Action Pane");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("actionPane.fxml"));
            Node actionPane = loader.load();
            actionPane.setOnMouseClicked(this::selectTheActionPaneAction);
            ActionController controller = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Action Pane");
            int numberOfActions = currentStructure.getSubStructureSize();
            if (numberOfActions == 0)
                controller.disablePreviousOption();
            taskVBox.getChildren().add(actionPane);

            JobStructure actionStructure = new JobStructure(this, this, controller, controller.getName());
            controller.setJobStructure(actionStructure);
            currentStructure.addSubJobStructure(actionStructure);

            if (actionData != null)
                controller.loadSavedData(actionData);

            return actionStructure;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading and adding action pane: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void removeSavedData(JobDataController jobDataController) {
        int removeIndex = currentStructure.removeSubJobStructure(jobDataController);
        taskVBox.getChildren().remove(removeIndex);
        if (removeIndex == 0 && currentStructure.getSubStructureSize() != 0)
            ((ActionController) currentStructure.getSubJobStructures().getFirst().getCurrentController()).disablePreviousOption();
        AppScene.addLog(LogLevel.DEBUG, className, "Removed selected action: " + removeIndex);
    }

    @Override
    public void moveSavedDataUp(JobDataController jobDataController) {
        int numberOfActions = currentStructure.getSubStructureSize();
        if (numberOfActions < 2)
            return;
        int selectedActionPaneIndex = currentStructure.getSubStructureIndex(jobDataController);
        if (selectedActionPaneIndex == 0)
            return;
        int changeIndex = selectedActionPaneIndex -1;
        updateActionPaneList(selectedActionPaneIndex, changeIndex);
        currentStructure.updateSubJobStructure(jobDataController, changeIndex);
        updateActionPreviousOption(changeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Moved up action: " + changeIndex);
    }

    @Override
    public void moveSavedDataDown(JobDataController jobDataController) {
        int numberOfActions = currentStructure.getSubStructureSize();
        if (numberOfActions < 2)
            return;
        int selectedActionPaneIndex = currentStructure.getSubStructureIndex(jobDataController);
        int changeIndex = selectedActionPaneIndex +1;
        if (changeIndex == numberOfActions)
            return;
        updateActionPaneList(selectedActionPaneIndex, changeIndex);
        currentStructure.updateSubJobStructure(jobDataController, changeIndex);
        updateActionPreviousOption(changeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Moved down action: " + changeIndex);
    }

    private void updateActionPaneList(int selectedIndex, int changeIndex) {
        ObservableList<Node> children = taskVBox.getChildren();
        Node actionNode = children.get(selectedIndex);
        children.remove(actionNode);
        children.add(changeIndex, actionNode);
    }
    private void updateActionPreviousOption(int index) {
        if (index < 2) {
            ((ActionController) currentStructure.getSubJobStructures().get(0).getCurrentController()).disablePreviousOption();
            ((ActionController) currentStructure.getSubJobStructures().get(1).getCurrentController()).enablePreviousOption();
        }
    }

    @Override
    public JobRunController<Object> getRunJob() {
        try {
            FXMLLoader loader = new FXMLLoader(AppScene.class.getResource("RunJob/taskRunPane.fxml"));
            loader.load();
            JobRunController<Object> taskRunController = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Task Run");
            return taskRunController;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading Task Run Pane: " + e.getMessage());
            return null;
        }
    }

}