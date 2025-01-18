package org.dev.Operation;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;
import org.dev.AppScene;
import org.dev.Enum.ActionTypes;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.Operation.Action.Action;
import org.dev.Operation.Condition.Condition;
import org.dev.Operation.Data.ActionData;
import org.dev.Operation.Data.AppData;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ActionController implements Initializable, DataController, ActivityController {

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

    @Override
    public void takeToDisplay() {
        AppScene.closeActionMenuPane();
        AppScene.closeConditionMenuPane();
        TaskController parentTaskController = findParentTaskController();
        if (parentTaskController == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Parent task controller is null - takeToDisplay");
            return;
        }
        if (mainActionParentNode.getScene() == null)
            parentTaskController.openTaskPane();
        parentTaskController.changeTaskScrollPaneView(mainActionParentNode);
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }
    private TaskController findParentTaskController() {
        for (MinimizedTaskController taskController : AppScene.currentLoadedOperationController.getTaskList()) {
            TaskController currentTaskController = taskController.getTaskController();
            List<ActionController> actionList = currentTaskController.getActionList();
            for (ActionController actionController : actionList)
                if (actionController == this)
                    return currentTaskController;
        }
        return null;
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

    public void registerActionPerform(Action action) {
        if (action == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Action is null - registerActionPerform");
            return;
        }
        isSet = true;
        this.action = action;
        if (action.getActionName() == null || action.getActionName().isBlank())
            action.setActionName(actionNameLabel.getText());
        displayActionImage(action.getDisplayImage());
    }

    private void displayActionImage(BufferedImage image) {
        actionImage.setImage(SwingFXUtils.toFXImage(image, null));
    }

    private void openActionMenuPane(MouseEvent event) {
        if (AppScene.isOperationRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Operation is running - cannot modify");
            return;
        }
        AppScene.openActionMenuPane(this);
    }
    public int getNumberOfCondition(HBox conditionBox) { return conditionBox.getChildren().size(); }

    private void addNewEntryCondition(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on add entry condition");
        if (AppScene.isOperationRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Operation is running - cannot modify");
            return;
        }
        int numberOfCondition = getNumberOfCondition(entryConditionHBox);
        if (numberOfCondition > 0 && !entryConditionList.get(numberOfCondition - 1).isSet()) {
            AppScene.addLog(LogLevel.INFO, className, "Previous Entry Condition is not set");
            return;
        }
        if (numberOfCondition < 5)
            addCondition(entryConditionList, entryConditionHBox, null);
    }

    private void addNewExitCondition(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on add exit condition");
        if (AppScene.isOperationRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Operation is running - cannot modify");
            return;
        }
        int numberOfCondition = getNumberOfCondition(exitConditionHBox);
        if (numberOfCondition > 0 && !exitConditionList.get(numberOfCondition - 1).isSet()) {
            AppScene.addLog(LogLevel.INFO, className, "Previous Exit Condition is not set");
            return;
        }
        if (numberOfCondition < 5)
            addCondition(exitConditionList, exitConditionHBox, null);
    }

    private void addCondition(List<ConditionController> whichController, HBox whichPane, Condition condition) {
        AppScene.addLog(LogLevel.TRACE, className, "Loading Condition Pane");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("conditionPane.fxml"));
            Node pane = loader.load();
            ConditionController controller = loader.getController();
            controller.setParentActionController(this);
            if (condition != null)
                controller.loadSavedCondition(condition);
            whichController.add(controller);
            whichPane.getChildren().add(pane);
            AppScene.addLog(LogLevel.DEBUG, className, "Loaded Condition Pane");
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading condition pane: " + e.getMessage());
        }
    }

    public void removeCondition(ConditionController conditionController) {
        if (entryConditionList.remove(conditionController)) {
            entryConditionHBox.getChildren().remove(conditionController.getParentNode());
        }
        else {
            exitConditionList.remove(conditionController);
            exitConditionHBox.getChildren().remove(conditionController.getParentNode());
        }
    }

    // ------------------------------------------------------
    @Override
    public void removeSavedData(DataController dataController) {}
    @Override
    public void addSavedData(AppData appData) {}
    @Override
    public AppLevel getAppLevel() { return AppLevel.Action; }

    @Override
    public ActionData getSavedData() {
        if (action == null)
            return null;
        ActionData actionData = new ActionData();
        action.setRequired(requiredCheckBox.isSelected());
        action.setPreviousPass(previousPassCheckBox.isSelected());
        action.setActionName(actionNameLabel.getText());
        actionData.setAction(action.getDeepCopied());
        List<Condition> entryConditions = new ArrayList<>();
        List<Condition> exitConditions = new ArrayList<>();
        for (ConditionController c : entryConditionList)
            entryConditions.add(c.getCondition().getDeepCopied());
        for (ConditionController c : exitConditionList)
            exitConditions.add(c.getCondition().getDeepCopied());
        actionData.setEntryConditionList(entryConditions);
        actionData.setExitConditionList(exitConditions);
        AppScene.addLog(LogLevel.TRACE, className, "Got action data");
        return actionData;
    }

    @Override
    public void loadSavedData(AppData appData) {
        if (appData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Action data is null - cannot load from save");
            return;
        }
        ActionData actionData = (ActionData) appData;
        registerActionPerform(actionData.getAction());
        updateActionName(action.getActionName());
        requiredCheckBox.setSelected(action.isRequired());
        previousPassCheckBox.setSelected(action.isPreviousPass());
        displayActionImage(action.getDisplayImage());
        List<Condition> entryConditions = actionData.getEntryConditionList();
        if (entryConditions != null)
            for (Condition entryCondition : entryConditions)
                addCondition(entryConditionList, entryConditionHBox, entryCondition);
        List<Condition> exitConditions = actionData.getExitConditionList();
        if (exitConditions != null)
            for (Condition exitCondition : exitConditions)
                addCondition(exitConditionList, exitConditionHBox, exitCondition);
    }
}