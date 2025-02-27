package org.dev.JobController;

import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;
import org.dev.AppScene;
import org.dev.Enum.ActionTypes;
import org.dev.Enum.AppLevel;
import org.dev.Enum.ConditionType;
import org.dev.Enum.LogLevel;
import org.dev.Job.Action.Action;
import org.dev.Job.Action.ActionKeyClick;
import org.dev.Job.Condition.Condition;
import org.dev.Job.JobData;
import org.dev.JobStructure;
import org.dev.RunJob.JobRunController;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ActionController implements Initializable, JobDataController, ActivityController {

    @FXML
    private Node mainActionParentNode;
    @FXML
    private TextField renameTextField;
    @FXML
    private CheckBox requiredCheckBox, previousPassCheckBox;
    @FXML
    private ImageView actionImage;
    @FXML
    private StackPane actionPane;
    @FXML
    private HBox entryConditionHBox, exitConditionHBox;
    @FXML
    private StackPane entryAddButton, exitAddButton;

    private JobStructure currentStructure;

    @Getter
    private boolean isSet;
    private JobData jobData = new JobData();
    @Getter
    private Action action;
    @Getter @Setter
    private ActionTypes chosenActionPerform;

    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        actionPane.setOnMouseClicked(this::openActionMenuPane);
        entryAddButton.setOnMouseClicked(this::addNewEntryCondition);
        exitAddButton.setOnMouseClicked(this::addNewExitCondition);
        renameTextField.focusedProperty().addListener((_, _, newValue) -> {
            if (!newValue) {
                //System.out.println("TextField lost focus");
                changeActionName();
            }
        });
    }

    public void setJobStructure(JobStructure structure) {
        currentStructure = structure;
    }

    private void changeActionName() {
        String name = renameTextField.getText();
        name = name.strip();
        if (name.isBlank()) {
            renameTextField.setText(currentStructure.getName());
            return;
        }
        updateActionName(name);
    }
    private void updateActionName(String name) {
        currentStructure.changeName(name);
        renameTextField.setText(name);
        if (action != null)
            action.setActionName(name);
        AppScene.addLog(LogLevel.DEBUG, className, "Renamed action: " + name);
    }

    public void disablePreviousOption() {
        previousPassCheckBox.setSelected(false);
        previousPassCheckBox.setVisible(false);
    }
    public void enablePreviousOption() { previousPassCheckBox.setVisible(true); }

    public void registerActionPerform(Action newAction) {
        if (newAction == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Action is null - registerActionPerform");
            return;
        }
        isSet = true;
        if (action != null) {
            newAction.setActionName(action.getActionName());
            newAction.setRequired(action.isRequired());
            newAction.setPreviousPass(action.isPreviousPass());
        }
        action = newAction;
        jobData.setMainJob(newAction);
        displayActionImage(newAction.getMainDisplayImage());
    }

    public void registerActionPerformUseEntryCondition(Action newAction) {
        int index = getConditionControllerIndex(ConditionType.Entry, false);
        if (index == -1) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - No Required Entry Condition");
            return;
        }
        ConditionController conditionController = (ConditionController) currentStructure.getSubJobStructures().get(index).getCurrentController();
        Condition condition = conditionController.getCondition();
        newAction.setMainImageBoundingBox(condition.getMainImageBoundingBox());
        newAction.setDisplayImage(condition.getDisplayImage());
        registerActionPerform(newAction);
    }

    public BufferedImage getLastRequiredEntryConditionImage() {
        int index = getConditionControllerIndex(ConditionType.Entry, false);
        if (index == -1)
            return null;
        ConditionController conditionController = (ConditionController) currentStructure.getSubJobStructures().get(index).getCurrentController();
        Condition condition = conditionController.getCondition();
        Action tempAction = new ActionKeyClick(); // just for the method
        tempAction.setMainImageBoundingBox(condition.getMainImageBoundingBox());
        tempAction.setDisplayImage(condition.getDisplayImage());
        return tempAction.getMainDisplayImage();
    }

    public boolean hasRequiredEntryCondition() {
        int index = getConditionControllerIndex(ConditionType.Entry, false);
        return index != -1;
    }

    private void displayActionImage(BufferedImage image) {
        actionImage.setImage(SwingFXUtils.toFXImage(image, null));
    }

    private void openActionMenuPane(MouseEvent event) {
        if (AppScene.isJobRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Operation is running - cannot modify");
            return;
        }
        AppScene.openActionMenuPane(this);
    }

    private void addNewEntryCondition(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on add entry condition");
        if (AppScene.isJobRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Operation is running - cannot modify");
            return;
        }
        if (event.getButton() == MouseButton.PRIMARY) {
            addCondition(ConditionType.Entry, null);
        }
    }

    private void addNewExitCondition(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on add exit condition");
        if (AppScene.isJobRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Operation is running - cannot modify");
            return;
        }
        if (event.getButton() == MouseButton.PRIMARY) {
            addCondition(ConditionType.Exit, null);
        }
    }

    private JobStructure addCondition(ConditionType conditionType, JobData conditionData) {
        HBox whichPane = findWhichConditionHBox(conditionType);
        int numberOfCondition = whichPane.getChildren().size();
        int lastIndex = getConditionControllerIndex(conditionType, false);
        if (conditionData == null) {
            if (numberOfCondition > 0 && (lastIndex != -1 && !currentStructure.getSubJobStructures().get(lastIndex).getCurrentController().isSet())) {
                AppScene.addLog(LogLevel.INFO, className, "Previous Condition is not set");
                return null;
            }
            if (numberOfCondition >= 5) {
                AppScene.addLog(LogLevel.INFO, className, "Max number of condition's reached");
                return null;
            }
        }

        AppScene.addLog(LogLevel.TRACE, className, "Loading Condition Pane");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("conditionPane.fxml"));
            Node pane = loader.load();
            ConditionController controller = loader.getController();
            controller.setConditionType(conditionType);
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Condition Pane");
            whichPane.getChildren().add(pane);

            lastIndex = (lastIndex == -1) ? currentStructure.getSubStructureSize() : lastIndex+1;
            JobStructure conditionStructure = new JobStructure(this,this, controller, null);
            controller.setJobStructure(conditionStructure);
            currentStructure.addSubJobStructure(lastIndex, conditionStructure);

            if (conditionData != null)
                controller.loadSavedData(conditionData);

            return conditionStructure;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading condition pane: " + e.getMessage());
            return null;
        }
    }

    private int getConditionControllerIndex(ConditionType conditionType, boolean getFirstIndex) {
        int count = -1;
        for (JobStructure subJobStructure : currentStructure.getSubJobStructures()) {
            if (((ConditionController) subJobStructure.getCurrentController()).getConditionType() == conditionType) {
                count++;
                if (getFirstIndex)
                    return count;
            }
        }
        return count;
    }

    private HBox findWhichConditionHBox(ConditionType conditionType) {
        return (conditionType == ConditionType.Entry) ? entryConditionHBox : exitConditionHBox;
    }

    // ------------------------------------------------------
    @Override
    public String getName() { return renameTextField.getText(); }

    @Override
    public Node getParentNode() { return mainActionParentNode; }

    @Override
    public AppLevel getAppLevel() { return AppLevel.Action; }

    @Override
    public void takeToDisplay() {
        AppScene.closeActionMenuPane();
        AppScene.closeConditionMenuPane();
        TaskController parentTaskController = (TaskController) currentStructure.getParentController();
        parentTaskController.takeToDisplay();
        parentTaskController.selectTheActionPane(getParentNode());
        AppScene.updateMainDisplayScrollValue(getParentNode());
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    @Override
    public JobData getSavedData() {
        if (action == null)
            return null;
        action.setRequired(requiredCheckBox.isSelected());
        action.setPreviousPass(previousPassCheckBox.isSelected());
        List<JobData> conditionDataList = new ArrayList<>();
        for (JobStructure subJobStructure : currentStructure.getSubJobStructures())
            conditionDataList.add(subJobStructure.getCurrentController().getSavedData());
        JobData actionData = new JobData(action.cloneData(), conditionDataList);
        AppScene.addLog(LogLevel.TRACE, className, "Got action data");
        return actionData;
    }

    @Override
    public JobData getSavedDataByReference() {
        if (action == null)
            return null;
        action.setRequired(requiredCheckBox.isSelected());
        action.setPreviousPass(previousPassCheckBox.isSelected());
        List<JobData> conditionDataList = new ArrayList<>();
        for (JobStructure subJobStructure : currentStructure.getSubJobStructures())
            conditionDataList.add(subJobStructure.getCurrentController().getSavedDataByReference());
        jobData.setMainJob(action.cloneData());
        jobData.setJobDataList(conditionDataList);
        AppScene.addLog(LogLevel.TRACE, className, "Got reference action data");
        return jobData;
    }

    @Override
    public void loadSavedData(JobData newJobData) {
        if (newJobData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Action data is null - cannot load from save");
            return;
        }
        jobData = newJobData;
        Action action = (Action) jobData.getMainJob();
        registerActionPerform(action);
        updateActionName(action.getActionName());
        requiredCheckBox.setSelected(action.isRequired());
        previousPassCheckBox.setSelected(action.isPreviousPass());
        List<JobData> conditionDataList = jobData.getJobDataList();
        for (JobData conditionData : conditionDataList)
            addSavedData(conditionData);
    }

    @Override
    public JobStructure addSavedData(JobData conditionData) {
        if (conditionData == null)
            return null;
        Condition condition = (Condition) conditionData.getMainJob();
        return addCondition(condition.getConditionType(), conditionData);
    }

    @Override
    public void removeSavedData(JobDataController jobDataController) {
        ConditionController conditionController = (ConditionController) jobDataController;
        Node conditionNode = conditionController.getParentNode();
        if (conditionController.getConditionType() == ConditionType.Entry)
            entryConditionHBox.getChildren().remove(conditionNode);
        else if (conditionController.getConditionType() == ConditionType.Exit)
            exitConditionHBox.getChildren().remove(conditionNode);
        currentStructure.removeSubJobStructure(jobDataController);
    }

    @Override
    public void moveSavedDataUp(JobDataController jobDataController) {
        ConditionController conditionController = (ConditionController) jobDataController;
        HBox whichHBox = findWhichConditionHBox(conditionController.getConditionType());
        int numberOfConditions = whichHBox.getChildren().size();
        if (numberOfConditions < 2)
            return;
        int selectedIndex = whichHBox.getChildren().indexOf(conditionController.getParentNode());
        if (selectedIndex == 0)
            return;
        int changeIndex = selectedIndex -1;
        updateActionPaneList(whichHBox, selectedIndex, changeIndex);
        int newIndex = currentStructure.getSubStructureIndex(jobDataController) -1;
        currentStructure.updateSubJobStructure(jobDataController, newIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Moved down condition: " + changeIndex);
    }

    @Override
    public void moveSavedDataDown(JobDataController jobDataController) {
        ConditionController conditionController = (ConditionController) jobDataController;
        HBox whichHBox = findWhichConditionHBox(conditionController.getConditionType());
        int numberOfConditions = whichHBox.getChildren().size();
        if (numberOfConditions < 2)
            return;
        int selectedIndex = whichHBox.getChildren().indexOf(conditionController.getParentNode());
        int changeIndex = selectedIndex +1;
        if (changeIndex == numberOfConditions)
            return;
        updateActionPaneList(whichHBox, selectedIndex, changeIndex);
        int newIndex = currentStructure.getSubStructureIndex(jobDataController) +1;
        currentStructure.updateSubJobStructure(jobDataController, newIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Moved down condition: " + changeIndex);
    }

    private void updateActionPaneList(HBox whichHBox, int selectedIndex, int changeIndex) {
        ObservableList<Node> children = whichHBox.getChildren();
        Node conditionNode = children.get(selectedIndex);
        children.remove(conditionNode);
        children.add(changeIndex, conditionNode);
    }

    @Override
    public JobRunController<Object> getRunJob() {
        try {
            FXMLLoader loader = new FXMLLoader(AppScene.class.getResource("RunJob/actionRunPane.fxml"));
            loader.load();
            JobRunController<Object> actionRunController = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Action Run");
            return actionRunController;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading Action Run Pane: " + e.getMessage());
            return null;
        }
    }
}