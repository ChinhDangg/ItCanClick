package org.dev.JobController;

import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.dev.AppScene;
import org.dev.Enum.ActionTypes;
import org.dev.Enum.AppLevel;
import org.dev.Enum.ConditionType;
import org.dev.Enum.LogLevel;
import org.dev.Job.Action.Action;
import org.dev.JobData.ActionData;
import org.dev.JobData.JobData;
import org.dev.JobData.ConditionData;
import org.dev.JobStructure;
import org.dev.RunJob.ActionRunController;
import org.dev.RunJob.JobRunController;
import org.dev.SideMenu.LeftMenu.SideMenuController;

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

    @Setter
    private JobStructure jobStructure;

    @Getter
    private boolean isSet;
    @Getter @Setter
    private Action action;
    @Getter @Setter
    private ActionTypes chosenActionPerform;
    @Getter
    private final Label actionNameLabel = new Label();

    private final String className = this.getClass().getSimpleName();
    private final List<ConditionController> entryConditionList = new ArrayList<>();
    private final List<ConditionController> exitConditionList = new ArrayList<>();
    private ConditionType currentConditionTypeForPasting;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        actionPane.setOnMouseClicked(this::openActionMenuPane);
        entryAddButton.setOnMouseClicked(this::addNewEntryCondition);
        exitAddButton.setOnMouseClicked(this::addNewExitCondition);
        actionNameLabel.setText(renameTextField.getText());
        renameTextField.focusedProperty().addListener((_, _, newValue) -> {
            if (!newValue) {
                //System.out.println("TextField lost focus");
                changeActionName();
            }
        });
    }

    private void changeActionName() {
        String name = renameTextField.getText();
        name = name.strip();
        if (name.isBlank()) {
            renameTextField.setText(actionNameLabel.getText());
            return;
        }
        updateActionName(name);
    }
    private void updateActionName(String name) {
        renameTextField.setText(name);
        actionNameLabel.setText(name);
        AppScene.addLog(LogLevel.DEBUG, className, "Renamed action: " + name);
    }

    public void disablePreviousOptions() {
        previousPassCheckBox.setSelected(false);
        previousPassCheckBox.setVisible(false);
    }
    public void enablePreviousOptions() { previousPassCheckBox.setVisible(true); }

    public void registerActionPerform(Action newAction) {
        if (newAction == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Action is null - registerActionPerform");
            return;
        }
        isSet = true;
        action = newAction;
        if (action.getActionName() == null || action.getActionName().isBlank())
            action.setActionName(actionNameLabel.getText());
        displayActionImage(action.getMainDisplayImage());
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
            addCondition(entryConditionList, entryConditionHBox, null);
        }
        else if (event.getButton() == MouseButton.SECONDARY) {
            currentConditionTypeForPasting = ConditionType.Entry;
            SideMenuController.rightClickMenuController.showRightMenu(event, this, this);
        }
    }

    private void addNewExitCondition(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on add exit condition");
        if (AppScene.isJobRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Operation is running - cannot modify");
            return;
        }
        if (event.getButton() == MouseButton.PRIMARY) {
            addCondition(exitConditionList, exitConditionHBox, null);
        }
        else if (event.getButton() == MouseButton.SECONDARY) {
            currentConditionTypeForPasting = ConditionType.Exit;
            SideMenuController.rightClickMenuController.showRightMenu(event, this, this);
        }
    }

    private void addCondition(List<ConditionController> whichController, HBox whichPane, JobData condition) {
        AppScene.addLog(LogLevel.TRACE, className, "Loading Condition Pane");
        try {
            int numberOfCondition = whichController.size();
            if (numberOfCondition > 0 && !whichController.get(numberOfCondition - 1).isSet()) {
                AppScene.addLog(LogLevel.INFO, className, "Previous Condition is not set");
                return;
            }
            if (numberOfCondition >= 5) {
                AppScene.addLog(LogLevel.INFO, className, "Max number of condition's reached");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("conditionPane.fxml"));
            Node pane = loader.load();
            ConditionController controller = loader.getController();
            controller.setParentActionController(this);
            if (condition != null)
                controller.loadSavedData(condition);
            whichController.add(controller);
            whichPane.getChildren().add(pane);
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Condition Pane");
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading condition pane: " + e.getMessage());
        }
    }

    // ------------------------------------------------------
    @Override
    public Node getParentNode() { return mainActionParentNode; }

    @Override
    public AppLevel getAppLevel() { return AppLevel.Action; }

    @Override
    public void takeToDisplay(@NonNull  MainJobController parentController) {
        AppScene.closeActionMenuPane();
        AppScene.closeConditionMenuPane();
        TaskController parentTaskController = (TaskController) parentController;
        parentTaskController.takeToDisplay(null);
        parentTaskController.changeTaskScrollPaneView(getParentNode());
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    @Override
    public ActionData getSavedData() {
        if (action == null)
            return null;
        ActionData actionData = new ActionData();
        action.setRequired(requiredCheckBox.isSelected());
        action.setPreviousPass(previousPassCheckBox.isSelected());
        action.setActionName(actionNameLabel.getText());
        actionData.setAction(action.getDeepCopied());
        List<ConditionData> entryConditions = new ArrayList<>();
        List<ConditionData> exitConditions = new ArrayList<>();
        for (ConditionController c : entryConditionList)
            entryConditions.add(c.getSavedData());
        for (ConditionController c : exitConditionList)
            exitConditions.add(c.getSavedData());
        actionData.setEntryConditionList(entryConditions);
        actionData.setExitConditionList(exitConditions);
        AppScene.addLog(LogLevel.TRACE, className, "Got action data");
        return actionData;
    }

    @Override
    public void loadSavedData(JobData jobData) {
        if (jobData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Action data is null - cannot load from save");
            return;
        }
        ActionData actionData = (ActionData) jobData;
        registerActionPerform(actionData.getAction());
        updateActionName(action.getActionName());
        requiredCheckBox.setSelected(action.isRequired());
        previousPassCheckBox.setSelected(action.isPreviousPass());
        displayActionImage(action.getDisplayImage());
        List<ConditionData> entryConditions = actionData.getEntryConditionList();
        if (entryConditions != null)
            for (ConditionData entryCondition : entryConditions)
                addCondition(entryConditionList, entryConditionHBox, entryCondition);
        List<ConditionData> exitConditions = actionData.getExitConditionList();
        if (exitConditions != null)
            for (ConditionData exitCondition : exitConditions)
                addCondition(exitConditionList, exitConditionHBox, exitCondition);
    }

    @Override
    public void addSavedData(JobData jobData) {
        if (currentConditionTypeForPasting == ConditionType.Entry)
            addCondition(entryConditionList, entryConditionHBox, jobData);
        else if (currentConditionTypeForPasting == ConditionType.Exit)
            addCondition(exitConditionList, exitConditionHBox, jobData);
    }

    @Override
    public void removeSavedData(JobDataController jobDataController) {
        ConditionController conditionController = (ConditionController) jobDataController;
        if (entryConditionList.remove(conditionController)) {
            entryConditionHBox.getChildren().remove(conditionController.getParentNode());
        }
        else {
            exitConditionList.remove(conditionController);
            exitConditionHBox.getChildren().remove(conditionController.getParentNode());
        }
    }

    @Override
    public void moveSavedDataUp(JobDataController jobDataController) {
        ConditionController conditionController = (ConditionController) jobDataController;
        WhichCondition whichCondition = getWhichConditionController(conditionController);
        if (whichCondition == null)
            return;
        List<ConditionController> whichController = whichCondition.controllers;
        int numberOfConditions = whichController.size();
        if (numberOfConditions < 2)
            return;
        int selectedIndex = whichController.indexOf(conditionController);
        if (selectedIndex == 0)
            return;
        int changeIndex = selectedIndex -1;
        updateActionPaneList(whichController, whichCondition.whichHBox, selectedIndex, changeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Moved down condition: " + changeIndex);
    }

    @Override
    public void moveSavedDataDown(JobDataController jobDataController) {
        ConditionController conditionController = (ConditionController) jobDataController;
        WhichCondition whichCondition = getWhichConditionController(conditionController);
        if (whichCondition == null)
            return;
        List<ConditionController> whichController = whichCondition.controllers;
        int numberOfConditions = whichController.size();
        if (numberOfConditions < 2)
            return;
        int selectedIndex = whichController.indexOf(conditionController);
        int changeIndex = selectedIndex +1;
        if (changeIndex == numberOfConditions)
            return;
        updateActionPaneList(whichController, whichCondition.whichHBox, selectedIndex, changeIndex);
        AppScene.addLog(LogLevel.DEBUG, className, "Moved down condition: " + changeIndex);
    }
    private record WhichCondition(List<ConditionController> controllers, HBox whichHBox) {}
    private WhichCondition getWhichConditionController(ConditionController controller) {
        if (entryConditionList.contains(controller))
            return new WhichCondition(entryConditionList, entryConditionHBox);
        else if (exitConditionList.contains(controller))
            return new WhichCondition(exitConditionList, exitConditionHBox);
        return null;
    }
    private void updateActionPaneList(List<ConditionController> controllers, HBox whichHBox, int selectedIndex, int changeIndex) {
        ObservableList<Node> children = whichHBox.getChildren();
        Node conditionNode = children.get(selectedIndex);
        controllers.add(changeIndex, controllers.remove((selectedIndex)));
        children.remove(conditionNode);
        children.add(changeIndex, conditionNode);
    }

    @Override
    public JobRunController getRunJob() {
        try {
            FXMLLoader loader = new FXMLLoader(AppScene.class.getResource("RunJob/actionRunPane.fxml"));
            loader.load();
            ActionRunController actionRunController = loader.getController();
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Action Run");
            return actionRunController;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading Action Run Pane: " + e.getMessage());
            return null;
        }
    }
}