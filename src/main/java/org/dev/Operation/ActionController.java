package org.dev.Operation;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;
import org.dev.AppScene;
import org.dev.Enum.ActionTypes;
import org.dev.Operation.Action.Action;
import org.dev.Operation.Condition.Condition;
import org.dev.Operation.Data.ActionData;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ActionController implements Initializable, MainJobController, ActivityController {

    @FXML
    private Pane mainActionPane;
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
    private Pane entryAddButton, exitAddButton;

    @Getter
    private boolean isSet;
    @Getter @Setter
    private Action action;
    @Getter @Setter
    private ActionTypes chosenActionPerform;
    @Getter
    private final Label actionNameLabel = new Label();

    @Getter
    private final List<ConditionController> entryConditionList = new ArrayList<>();
    @Getter
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
        System.out.println("Action take to display");
        AppScene.closeActionMenuPane();
        AppScene.closeConditionMenuPane();
        TaskController parentTaskController = findParentTaskController();
        if (parentTaskController == null)
            throw new IllegalStateException("Parent task controller is null for action controller - bug");
        if (mainActionPane.getScene() == null)
            parentTaskController.openTaskPane();
        parentTaskController.changeTaskScrollPaneView(mainActionPane);
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
        if (action != null)
            action.setActionName(name);
        updateActionName(name);
    }
    private void updateActionName(String name) {
        renameTextField.setText(name);
        actionNameLabel.setText(name);
    }

    public void disablePreviousOptions() {
        previousPassCheckBox.setSelected(false);
        previousPassCheckBox.setVisible(false);
    }
    public void enablePreviousOptions() {
        previousPassCheckBox.setVisible(true);
    }

    public void registerActionPerform(Action action) {
        if (action == null)
            throw new NullPointerException();
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
            System.out.println("Operation is running, cannot modify");
            return;
        }
        AppScene.openActionMenuPane(this);
    }
    private FXMLLoader getConditionPaneLoader() {
        return new FXMLLoader(getClass().getResource("conditionPane.fxml"));
    }
    public int getNumberOfCondition(HBox conditionBox) {
        return conditionBox.getChildren().size();
    }

    private void addNewEntryCondition(MouseEvent event) {
        System.out.println("Entry add clicked");
        if (AppScene.isOperationRunning) {
            System.out.println("Operation is running, cannot modify");
            return;
        }
        int numberOfCondition = getNumberOfCondition(entryConditionHBox);
        if (numberOfCondition > 0 && !entryConditionList.get(numberOfCondition - 1).isSet()) {
            System.out.println("Previous Entry Condition is not set yet");
            return;
        }
        try {
            if (numberOfCondition < 5)
                addNewCondition(entryConditionList, entryConditionHBox);
        } catch (IOException e) {
            System.out.println("Fail loading and adding entry condition panes");
        }
    }

    private void addNewExitCondition(MouseEvent event) {
        System.out.println("Exit add clicked");
        if (AppScene.isOperationRunning) {
            System.out.println("Operation is running, cannot modify");
            return;
        }
        int numberOfCondition = getNumberOfCondition(exitConditionHBox);
        if (numberOfCondition > 0 && !exitConditionList.get(numberOfCondition - 1).isSet()) {
            System.out.println("Previous Exit Condition is not set yet");
            return;
        }
        try {
            if (numberOfCondition < 5)
                addNewCondition(exitConditionList, exitConditionHBox);
        } catch (IOException e) {
            System.out.println("Fail loading and adding exit condition panes");
        }
    }

    private void addNewCondition(List<ConditionController> whichController, HBox whichPane) throws IOException {
        FXMLLoader loader = getConditionPaneLoader();
        StackPane pane = loader.load();
        whichController.add(loader.getController());
        whichPane.getChildren().add(pane);
    }

    private void addSavedCondition(List<ConditionController> whichController, HBox whichPane, Condition condition) throws IOException {
        FXMLLoader loader = getConditionPaneLoader();
        StackPane pane = loader.load();
        ConditionController controller = loader.getController();
        controller.loadSavedCondition(condition);
        whichController.add(controller);
        whichPane.getChildren().add(pane);
    }

    // ------------------------------------------------------
    public ActionData getActionData() {
        ActionData actionData = new ActionData();
        action.setRequired(requiredCheckBox.isSelected());
        action.setPreviousPass(previousPassCheckBox.isSelected());
        actionData.setAction(action);
        List<Condition> entryConditions = new ArrayList<>();
        List<Condition> exitConditions = new ArrayList<>();
        for (ConditionController c : entryConditionList)
            entryConditions.add(c.getCondition());
        for (ConditionController c : exitConditionList)
            exitConditions.add(c.getCondition());
        actionData.setEntryConditionList(entryConditions);
        actionData.setExitConditionList(exitConditions);
        return actionData;
    }

    public void loadSavedActionData(ActionData actionData) throws IOException {
        if (actionData == null)
            throw new NullPointerException("Can't load from saved action data");
        registerActionPerform(actionData.getAction());
        updateActionName(action.getActionName());
        requiredCheckBox.setSelected(action.isRequired());
        previousPassCheckBox.setSelected(action.isPreviousPass());
        displayActionImage(action.getDisplayImage());
        List<Condition> entryConditions = actionData.getEntryConditionList();
        if (entryConditions != null)
            for (Condition entryCondition : entryConditions)
                addSavedCondition(entryConditionList, entryConditionHBox, entryCondition);
        List<Condition> exitConditions = actionData.getExitConditionList();
        if (exitConditions != null)
            for (Condition exitCondition : exitConditions)
                addSavedCondition(exitConditionList, exitConditionHBox, exitCondition);
    }
}