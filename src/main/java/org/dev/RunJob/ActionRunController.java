package org.dev.RunJob;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.ConditionType;
import org.dev.Enum.LogLevel;
import org.dev.Job.Action.Action;
import org.dev.Job.Condition.Condition;
import org.dev.JobData.ActionData;
import org.dev.JobData.ConditionData;
import org.dev.JobController.MainJobController;
import org.dev.SideMenu.LeftMenu.SideMenuController;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ActionRunController extends RunActivity implements Initializable, MainJobController {

    @FXML
    private Group mainActionRunGroup;
    @FXML
    private ScrollPane entryConditionScrollPane, exitConditionScrollPane;
    @FXML
    private ImageView actionSavedImageView, actionPerformedImageView;
    @FXML
    private Label actionRunNameLabel;
    @FXML
    private Label actionStatusLabel;
    @FXML
    private StackPane actionStackPaneImageContainer;
    @FXML
    private HBox entryConditionHBox, exitConditionHBox;
    @FXML
    private VBox actionRunVBox, conditionRunEntryVBoxContainer, conditionRunExitVBoxContainer;

    @Getter
    private VBox actionRunSideContent = new VBox();
    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        showActionRunPane(false);
        conditionRunExitVBoxContainer.setVisible(false);
        setFitDimensionImageView();
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
        AppScene.currentLoadedOperationRunController.changeScrollPaneVValueView(mainActionRunGroup);
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    @Override
    public AppLevel getAppLevel() {
        return AppLevel.Action;
    }

    private void showActionRunPane(boolean visible) {
        actionRunVBox.setVisible(visible);
        actionRunVBox.setManaged(visible);
    }

    private void changeActionRunStatus(RunningStatus newStatus) {
        Platform.runLater(() -> actionStatusLabel.setText(newStatus.name()));
    }
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
    public boolean startAction(ActionData actionData) throws InterruptedException {
        if (actionData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Action data is null - cannot start");
            return false;
        }
        Action action = actionData.getAction();
        if (action == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Action is null - cannot start");
            return false;
        }
        changeLabelText(actionRunNameLabel, action.getActionName());
        changeActionRunStatus(RunningStatus.Running);
        AppScene.addLog(LogLevel.INFO, className, "Start running action: " + action.getActionName());
        return runAction(actionData);
    }

    private boolean runAction(ActionData actionData) throws InterruptedException {
        boolean passed;
        if (actionData.getAction().isProgressiveSearch())
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
        int totalAttempt = action.getAttempt();
        int count = totalAttempt;
        boolean entryPassed, actionPerformed = false;
        while (count > 0) {
            count--;
            AppScene.addLog(LogLevel.INFO, className, "Waiting " + action.getWaitBeforeTime()/1000 + " seconds");
            Thread.sleep(action.getWaitBeforeTime());
            entryPassed = checkAllConditions(actionData.getEntryConditionList(), ConditionType.Entry);
            if (!entryPassed) {
                AppScene.addLog(LogLevel.INFO, className, "Not found entry at action: " + actionName + " : " + count + "/" + totalAttempt);
                if (!actionPerformed)
                    continue;
                actionPerformed = false;
            }
            else {
                AppScene.addLog(LogLevel.INFO, className, "Found entry at action: " + actionName + " : " + count + "/" + totalAttempt);
                performAction(action);
                actionPerformed = true;
            }
            AppScene.addLog(LogLevel.INFO, className, "Waiting " + action.getWaitAfterTime()/1000 + " seconds");
            Thread.sleep(action.getWaitAfterTime());
            if (checkAllConditions(actionData.getExitConditionList(), ConditionType.Exit)) {
                AppScene.addLog(LogLevel.INFO, className, "Found exit at action: " + actionName + " : " + count + "/" + totalAttempt);
                return true;
            }
            AppScene.addLog(LogLevel.INFO, className, "Not found exit at action: " + actionName + " : " + count + "/" + totalAttempt);
        }
        AppScene.addLog(LogLevel.INFO, className, "Exceeded number of attempt at action: " + actionName);
        return false;
    }

    private boolean performActionWithProgressiveSearch(ActionData actionData) {
        Action action = actionData.getAction();
        String actionName = action.getActionName();
        long startTime = System.currentTimeMillis();
        int duration = action.getProgressiveSearchTime();
        boolean entryPassed, actionPerformed = false;
        AppScene.addLog(LogLevel.INFO, className, "Start progressive search at action: " + actionName + " for: " + duration);
        while (System.currentTimeMillis() - startTime < duration) {
            entryPassed = checkAllConditions(actionData.getEntryConditionList(), ConditionType.Entry);
            if (!entryPassed) {
                if (!actionPerformed)
                    continue;
                actionPerformed = false;
            }
            else {
                performAction(action);
                actionPerformed = true;
            }
            conditionRunExitVBoxContainer.setVisible(true);
            if (checkAllConditions(actionData.getExitConditionList(), ConditionType.Exit))
                return true;
        }
        AppScene.addLog(LogLevel.INFO, className, "Exceeded progressive search time at action: " + actionName);
        return false;
    }

    private void performAction(Action action) {
        showActionRunPane(true);
        updateImageView(actionSavedImageView, action.getDisplayImage());
        try {
            updateImageView(actionPerformedImageView, action.getSeenImage());
        } catch (AWTException e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error getting action seen image");
        }
        action.performAction();
        AppScene.addLog(LogLevel.INFO, className, "Performed action: " + action.getActionName());
    }

    private boolean checkAllConditions(List<ConditionData> conditionData, ConditionType conditionType) {
        AppScene.addLog(LogLevel.INFO, className, "Start checking condition: " + conditionType);
        if (conditionData == null || conditionData.isEmpty())
            return true;
        Platform.runLater(() -> clearConditionHBox(conditionType));
        // all conditions are optional therefore only need one condition to pass
        if (checkAllConditionsIsNotRequired(conditionData)) {
            for (ConditionData c : conditionData) {
                loadConditionRunPane(conditionType);
                if (currentConditionRunController.checkCondition(c.getCondition()))
                    return true;
            }
            return false;
        }
        else { // only check required condition and they must pass
            for (ConditionData c : conditionData) {
                loadConditionRunPane(conditionType);
                Condition condition = c.getCondition();
                if (condition.isRequired() && !currentConditionRunController.checkCondition(condition))
                    return false;
            }
            return true;
        }
    }
    private boolean checkAllConditionsIsNotRequired(List<ConditionData> conditionData) {
        if (conditionData == null || conditionData.isEmpty())
            return true;
        for (ConditionData c : conditionData)
            if (c.getCondition().isRequired())
                return false;
        return true;
    }

    // ------------------------------------------------------
    private ConditionRunController currentConditionRunController;
    private void loadConditionRunPane(ConditionType conditionType) {
        if (conditionType == ConditionType.Entry)
            loadEntryConditionRunPane();
        else
            loadExitConditionRunPane();
    }

    private void loadEntryConditionRunPane() {
        Node conditionRunPane = loadConditionRunPane();
        if (conditionRunPane == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - cannot load condition run pane - Could not load entry condition run pane");
            return;
        }
        conditionRunEntryVBoxContainer.setVisible(true);
        Platform.runLater(() -> actionRunSideContent.getChildren().clear());
        currentConditionRunController.setParentScrollPane(entryConditionScrollPane);
        Node conditionEntryRunHBoxLabel = SideMenuController.getNewSideHBoxLabel(
                new Label(ConditionType.Entry.name()), null, currentConditionRunController);
        // update side hierarchy
        Platform.runLater(() -> actionRunSideContent.getChildren().add(conditionEntryRunHBoxLabel));
        Platform.runLater(() -> entryConditionHBox.getChildren().add(conditionRunPane));
    }

    private void loadExitConditionRunPane() {
        Node conditionRunPane = loadConditionRunPane();
        if (conditionRunPane == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - condition run pane is null - Could not load exit condition run pane");
            return;
        }
        conditionRunExitVBoxContainer.setVisible(true);
        currentConditionRunController.setParentScrollPane(exitConditionScrollPane);
        Node conditionExitRunHBoxLabel = SideMenuController.getNewSideHBoxLabel(
                new Label(ConditionType.Exit.name()), null, currentConditionRunController);
        // update side hierarchy
        Platform.runLater(() -> actionRunSideContent.getChildren().add(conditionExitRunHBoxLabel));
        Platform.runLater(() -> exitConditionHBox.getChildren().add(conditionRunPane));
    }

    private Node loadConditionRunPane() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("conditionRunPane.fxml"));
            Node conditionRunPane = fxmlLoader.load();
            currentConditionRunController = fxmlLoader.getController();
            return conditionRunPane;
        } catch (IOException e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading condition run pane: " + e.getMessage());
            return null;
        }
    }

    private void clearConditionHBox(ConditionType conditionType) {
        if (conditionType == ConditionType.Entry)
            entryConditionHBox.getChildren().clear();
        else
            exitConditionHBox.getChildren().clear();
    }
}
