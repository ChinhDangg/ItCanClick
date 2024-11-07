package org.dev.RunOperation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import org.dev.AppScene;
import org.dev.Operation.Action.Action;
import org.dev.Operation.Condition.Condition;
import org.dev.Operation.Data.ActionData;
import org.dev.Operation.MainJobController;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ActionRunController extends RunActivity implements Initializable, MainJobController {

    @FXML
    private Pane mainActionRunPane;
    @FXML
    private ImageView actionSavedImageView, actionPerformedImageView;
    @FXML @Getter
    private Label actionRunNameLabel;
    @FXML
    private Label actionSavedResultLabel, actionPerformedResultLabel, actionStatusLabel;
    @FXML
    private Pane actionSavedPane, actionPerformedPane;
    @FXML
    private StackPane actionStackPaneImageContainer;
    @FXML
    private HBox entryConditionHBox, exitConditionHBox;
    @FXML
    private VBox actionRunVBox, conditionRunEntryVBoxContainer, conditionRunExitVBoxContainer;

    @Getter
    private VBox conditionRunVBoxSideContent = new VBox();
    @Setter
    private TaskRunController parentTaskRunController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        showActionRunPane(false);
        conditionRunExitVBoxContainer.setVisible(false);
        setFitDimensionImageView();
        conditionRunVBoxSideContent.setPadding(new Insets(0, 0, 0, 50));
    }

    private void setFitDimensionImageView() {
        double width = actionStackPaneImageContainer.getPrefWidth();
        double height = actionStackPaneImageContainer.getPrefHeight();
        actionSavedImageView.setFitWidth(width);
        actionSavedImageView.setFitWidth(height);
        actionPerformedImageView.setFitHeight(width);
        actionPerformedImageView.setFitHeight(height);
    }

    @Override
    public void takeToDisplay() {
        AppScene.currentLoadedOperationRunController.takeToDisplay();
        // todo
        // action view is in task view and task view is in operation view
        // only operation view control the view scroll
        // need to get task view relative to parent view first
        // then use that task view for offset ? to get actual action view relative to operation view
    }

    private void showActionRunPane(boolean visible) {
        actionRunVBox.setVisible(visible);
        actionRunVBox.setManaged(visible);
    }

    private void changeActionRunStatus(RunningStatus newStatus) { actionStatusLabel.setText(newStatus.name()); }
    private void updateActionRunStatus(boolean pass) {
        if (pass) {
            changeActionRunStatus(RunningStatus.Passed);
            actionStatusLabel.setStyle("-fx-text-fill: green;");
        }
        else {
            changeActionRunStatus(RunningStatus.Failed);
            actionStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // ------------------------------------------------------
    public boolean runAction(ActionData actionData) throws InterruptedException {
        if (actionData == null) {
            System.out.println("Action data not found - bug");
            return false;
        }
        Action action = actionData.getAction();
        if (action == null) {
            System.out.println("No action is found");
            return false;
        }
        changeLabelText(actionRunNameLabel, action.getActionName());
        changeActionRunStatus(RunningStatus.Running);

//        boolean entryPassed = checkAllConditions(entryConditionHBox, actionData.getEntryConditionList());
//        System.out.println(entryPassed);
//
//        performAction(action);
//
//        boolean exitPassed = checkAllConditions(exitConditionHBox, actionData.getExitConditionList());
//        System.out.println(exitPassed);

        boolean passed;
        if (action.isProgressiveSearch())
            passed = performActionWithProgressiveSearch(actionData);
        else
            passed = performActionWithAttempt(actionData);
        updateActionRunStatus(passed);
        return passed;
    }

    // ------------------------------------------------------
    private boolean performActionWithAttempt(ActionData actionData) throws InterruptedException {
        Action action = actionData.getAction();
        String actionName = action.getActionName();
        int count = action.getAttempt();
        boolean entryPassed;
        while (count > 0) {
            count--;
            Thread.sleep(action.getWaitBeforeTime());
            entryPassed = checkAllConditions(entryConditionHBox, actionData.getEntryConditionList());
            if (!entryPassed) {
                System.out.println("Not found entry with " + actionName + " " + count);
                continue;
            }
            System.out.println("Found entry with " + actionName);
            performAction(action);
            Thread.sleep(action.getWaitAfterTime());
            if (checkAllConditions(exitConditionHBox, actionData.getExitConditionList())) {
                System.out.println("Found exit with " + actionName);
                return true;
            }
            System.out.println("Can't find exit");
        }
        System.out.println("Exceeded number of attempt for performing " + actionName);
        return false;
    }

    private boolean performActionWithProgressiveSearch(ActionData actionData) {
        Action action = actionData.getAction();
        String actionName = action.getActionName();
        long startTime = System.currentTimeMillis();
        int duration = action.getProgressiveSearchTime();
        boolean entryPassed;
        System.out.println("Starting progressive search: " + actionName);
        while (System.currentTimeMillis() - startTime < duration) {
            entryPassed = checkAllConditions(entryConditionHBox, actionData.getEntryConditionList());
            if (!entryPassed)
                continue;
            performAction(action);
            if (checkAllConditions(exitConditionHBox, actionData.getExitConditionList()))
                return true;
        }
        System.out.println("Exceeded progressive search time with " + actionName);
        return false;
    }

    private void performAction(Action action) {
        showActionRunPane(true);
        updateImageView(actionSavedImageView, action.getDisplayImage());
        updateImageView(actionPerformedImageView, action.getMainImageBoundingBox());
        //action.performAction();
        System.out.println("Performed action: " + action.getActionName());
    }

    private boolean checkAllConditions(HBox whichRunConditionHBox, List<Condition> conditions) {
        if (conditions == null || conditions.isEmpty())
            return true;
        // all conditions are optional therefore only need one condition to pass
        System.out.println(conditions.size());
        if (checkAllConditionsIsNotRequired(conditions)) {
            for (Condition c : conditions) {
                whichRunConditionHBox.getChildren().add(loadConditionRunPane());
                if (currentConditionRunController.checkCondition(c))
                    return true;
            }
            return false;
        }
        else { // only check required condition and they must pass
            for (Condition c : conditions) {
                whichRunConditionHBox.getChildren().add(loadConditionRunPane());
                if (c.isRequired() && !currentConditionRunController.checkCondition(c))
                    return false;
            }
            return true;
        }
    }
    private boolean checkAllConditionsIsNotRequired(List<Condition> conditions) {
        if (conditions == null || conditions.isEmpty())
            return true;
        for (Condition c : conditions)
            if (c.isRequired())
                return false;
        return true;
    }

    // ------------------------------------------------------
    private ConditionRunController currentConditionRunController;
    private HBox loadConditionRunPane() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("conditionRunPane.fxml"));
            HBox conditionRunPane = fxmlLoader.load();
            currentConditionRunController = fxmlLoader.getController();
            return conditionRunPane;
        } catch (IOException e) {
            System.out.println("Fail loading condition run pane");
            return null;
        }
    }
}
