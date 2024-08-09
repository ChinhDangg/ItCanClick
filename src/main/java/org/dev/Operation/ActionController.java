package org.dev.Operation;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
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
import org.dev.App;
import org.dev.Enum.ActionTypes;
import org.dev.Operation.Action.Action;
import org.dev.Operation.Condition.Condition;
import org.dev.Operation.Data.ActionData;
import org.dev.SideMenuHierarchy;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ActionController implements Initializable, MainJobController, ActivityController {

    @FXML
    private Pane mainActionPane;
    @Getter
    @FXML
    private final Label actionNameLabel = new Label();
    @FXML
    private TextField renameTextField;
    @FXML
    private CheckBox requiredCheckBox, previousPassCheckBox;
    @FXML
    private ImageView actionImage;
    @FXML
    private StackPane actionPane;
    @FXML
    private HBox entryConditionPane, exitConditionPane;
    @FXML
    private Pane entryAddButton, exitAddButton;

    @Getter
    private boolean isSet;
    @Getter
    @Setter
    private Action action;
    @Getter
    @Setter
    private ActionTypes chosenActionPerform;
    @Getter
    private SideMenuHierarchy actionSideMenuHierarchy;

    private final List<ConditionController> entryConditionList = new ArrayList<>();
    private final List<ConditionController> exitConditionList = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        actionPane.setOnMouseClicked(this::openActionMenuPane);
        entryAddButton.setOnMouseClicked(this::addNewEntryCondition);
        exitAddButton.setOnMouseClicked(this::addNewExitCondition);
        actionNameLabel.setText(renameTextField.getText());
        requiredCheckBox.setOnAction(this::toggleRequiredCheckBox);
        previousPassCheckBox.setOnAction(this::togglePreviousPassCheckBox);
        renameTextField.focusedProperty().addListener((_, _, newValue) -> {
            if (!newValue) {
                //System.out.println("TextField lost focus");
                changeActionName();
            }
        });
        actionSideMenuHierarchy = new SideMenuHierarchy(actionNameLabel, this);
    }

    @Override
    public void takeToDisplay() {
        TaskController parentTaskController = findParentTaskController();
        if (parentTaskController == null)
            throw new IllegalStateException("Parent task controller is null for action controller - bug");
        if (mainActionPane.getScene() == null)
            App.displayNewNode(parentTaskController.getTaskPane());
        parentTaskController.changeTaskScrollPaneView(mainActionPane);
    }
    private TaskController findParentTaskController() {
        for (MinimizedTaskController taskController : App.currentLoadedOperationController.getTaskList()) {
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
    public boolean isRequired() {
        return requiredCheckBox.isSelected();
    }
    public boolean isPreviousPass() { return previousPassCheckBox.isSelected(); }

    private void toggleRequiredCheckBox(ActionEvent actionEvent) {
        action.setRequired(requiredCheckBox.isSelected());
    }
    private void togglePreviousPassCheckBox(ActionEvent actionEvent) {
        action.setPreviousPass(previousPassCheckBox.isSelected());
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
        App.openActionMenuPane(this);
    }
    private FXMLLoader getConditionPaneLoader() {
        return new FXMLLoader(getClass().getResource("conditionPane.fxml"));
    }
    public int getNumberOfCondition(HBox conditionBox) {
        return conditionBox.getChildren().size();
    }

    private void addNewEntryCondition(MouseEvent event) {
        System.out.println("Entry add clicked");
        int numberOfCondition = getNumberOfCondition(entryConditionPane) - 1;
        if (numberOfCondition > 0 && !entryConditionList.get(numberOfCondition - 1).isSet()) {
            System.out.println("Previous Entry Condition is not set yet");
            return;
        }
        try {
            if (numberOfCondition < 5)
                addNewCondition(entryConditionList, entryConditionPane, numberOfCondition);
        } catch (IOException e) {
            System.out.println("Fail loading and adding entry condition panes");
        }
    }

    private void addNewExitCondition(MouseEvent event) {
        System.out.println("Exit add clicked");
        int numberOfCondition = getNumberOfCondition(exitConditionPane) - 1;
        if (numberOfCondition > 0 && !exitConditionList.get(numberOfCondition - 1).isSet()) {
            System.out.println("Previous Exit Condition is not set yet");
            return;
        }
        try {
            if (numberOfCondition < 5)
                addNewCondition(exitConditionList, exitConditionPane, numberOfCondition);
        } catch (IOException e) {
            System.out.println("Fail loading and adding exit condition panes");
        }
    }

    private void addNewCondition(List<ConditionController> whichController, HBox whichPane, int numberOfCondition) throws IOException {
        FXMLLoader loader = getConditionPaneLoader();
        StackPane pane = loader.load();
        whichController.add(loader.getController());
        whichPane.getChildren().add(numberOfCondition, pane);
    }

    private void addNewSavedCondition(List<ConditionController> whichController, HBox whichPane, int numberOfCondition, Condition condition) throws IOException {
        FXMLLoader loader = getConditionPaneLoader();
        StackPane pane = loader.load();
        ConditionController controller = loader.getController();
        controller.loadSavedCondition(condition);
        whichController.add(controller);
        whichPane.getChildren().add(numberOfCondition, pane);
    }

    // ------------------------------------------------------
    public boolean performAction() {
        if (action == null) {
            System.out.println("Bug, no action is found");
            return false;
        }
        if (action.isProgressiveSearch())
            return performActionWithProgressiveSearch();
        return performActionWithAttempt();
    }
    private boolean performActionWithAttempt() {
        String actionName = actionNameLabel.getText();
        try {
            int count = action.getAttempt();
            boolean entryPassed = false;
            while (count > 0) {
                Thread.sleep(action.getWaitBeforeTime());
                if (checkAllConditions(entryConditionList)) {
                    System.out.println("Found entry with " + actionName);
                    action.performAction();
                    entryPassed = true;
                    System.out.println(actionName + " performed: " + count);
                }
                else if (!entryPassed) {
                    System.out.println("Not found entry with " + actionName + " " + count);
                    count--;
                    continue;
                }
                Thread.sleep(action.getWaitAfterTime());
                if (checkAllConditions(exitConditionList)) {
                    System.out.println("Found exit with " + actionName);
                    return true;
                }
                else
                    System.out.println("Can't find exit");
                count--;
            }
        } catch (Exception e) {
            System.out.println("Fail performing action with number of attempts");
        }
        System.out.println("Exceeded number of attempt for performing " + actionName);
        return false;
    }
    private boolean performActionWithProgressiveSearch() {
        try {
            long startTime = System.currentTimeMillis();
            int duration = action.getProgressiveSearchTime();
            boolean entryPassed = false;
            System.out.println("Starting progressive search");
            while (System.currentTimeMillis() - startTime < duration) {
                if (checkAllConditions(entryConditionList)) {
                    action.performAction();
                    entryPassed = true;
                }
                if (entryPassed && checkAllConditions(exitConditionList))
                    return true;
            }
        } catch (Exception e) {
            System.out.println("Fail performing action with progressive search");
        }
        System.out.println("Exceeded progressive search time with ");
        return false;
    }

    private boolean checkAllConditionsIsNotRequired(List<ConditionController> controllers) {
        if (controllers.isEmpty())
            return true;
        for (ConditionController c : controllers)
            if (c.getCondition().isRequired())
                return false;
        return true;
    }
    private boolean checkAllConditions(List<ConditionController> controllers) {
        if (controllers.isEmpty())
            return true;
        // all conditions are optional therefore only need one condition to pass
        if (checkAllConditionsIsNotRequired(controllers)) {
            for (ConditionController c : controllers)
                if (c.getCondition().checkCondition())
                    return true;
            return false;
        }
        else { // only check required condition and they must pass
            for (ConditionController c : controllers) {
                Condition condition = c.getCondition();
                if (condition.isRequired() && !condition.checkCondition())
                    return false;
            }
            return true;
        }
    }

    // ------------------------------------------------------
    public ActionData getActionData() {
        ActionData actionData = new ActionData();
        actionData.setAction(action);
        List<Condition> entryConditions = new ArrayList<>();
        List<Condition> exitConditions = new ArrayList<>();
        for (ConditionController c : entryConditionList)
            entryConditions.add(c.getCondition());
        for (ConditionController c : exitConditionList)
            exitConditions.add(c.getCondition());
        actionData.setEntryCondition(entryConditions);
        actionData.setExitCondition(exitConditions);
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
        List<Condition> entryConditions = actionData.getEntryCondition();
        for (int j = 0; j < entryConditions.size(); j++)
            addNewSavedCondition(entryConditionList, entryConditionPane, j, entryConditions.get(j));
        List<Condition> exitConditions = actionData.getExitCondition();
        for (int j = 0; j < exitConditions.size(); j++)
            addNewSavedCondition(exitConditionList, exitConditionPane, j, exitConditions.get(j));
    }
}