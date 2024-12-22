package org.dev.RunOperation;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.dev.AppScene;
import org.dev.Enum.ConditionType;
import org.dev.Operation.Action.Action;
import org.dev.Operation.Condition.Condition;
import org.dev.Operation.Data.ActionData;
import org.dev.Operation.MainJobController;
import org.dev.LeftSideMenu.SideMenuController;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        showActionRunPane(false);
        conditionRunExitVBoxContainer.setVisible(false);
        setFitDimensionImageView();
        conditionRunVBoxSideContent.setPadding(new Insets(0, 0, 0, 35));
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
        System.out.println("Action run take To Display");
        changeScrollPaneVValueView(AppScene.currentLoadedOperationRunController.getOperationRunScrollPane(), null, mainActionRunGroup);
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
        int count = action.getAttempt();
        boolean entryPassed;
        while (count > 0) {
            count--;
            Thread.sleep(action.getWaitBeforeTime());
            entryPassed = checkAllConditions(actionData.getEntryConditionList(), ConditionType.Entry);
            if (!entryPassed) {
                System.out.println("Not found entry with " + actionName + " " + count);
                continue;
            }
            System.out.println("Found entry with " + actionName);
            performAction(action);
            Thread.sleep(action.getWaitAfterTime());
            if (checkAllConditions(actionData.getExitConditionList(), ConditionType.Exit)) {
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
            entryPassed = checkAllConditions(actionData.getEntryConditionList(), ConditionType.Entry);
            if (!entryPassed)
                continue;
            performAction(action);
            conditionRunExitVBoxContainer.setVisible(true);
            if (checkAllConditions(actionData.getExitConditionList(), ConditionType.Exit))
                return true;
        }
        System.out.println("Exceeded progressive search time with " + actionName);
        return false;
    }

    private void performAction(Action action) {
        showActionRunPane(true);
        updateImageView(actionSavedImageView, action.getDisplayImage());
        updateImageView(actionPerformedImageView, action.getMainImageBoundingBox());
        action.performAction();
        System.out.println("Performed action: " + action.getActionName());
    }

    private boolean checkAllConditions(List<Condition> conditions, ConditionType conditionType) {
        if (conditions == null || conditions.isEmpty())
            return true;
        Platform.runLater(() -> clearConditionHBox(conditionType));
        // all conditions are optional therefore only need one condition to pass
        if (checkAllConditionsIsNotRequired(conditions)) {
            for (Condition c : conditions) {
                loadConditionRunPane(conditionType);
                if (currentConditionRunController.checkCondition(c))
                    return true;
            }
            return false;
        }
        else { // only check required condition and they must pass
            for (Condition c : conditions) {
                loadConditionRunPane(conditionType);
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
    private void loadConditionRunPane(ConditionType conditionType) {
        if (conditionType == ConditionType.Entry)
            loadEntryConditionRunPane();
        else
            loadExitConditionRunPane();
    }

    private void loadEntryConditionRunPane() {
        Node conditionRunPane = loadConditionRunPane();
        if (conditionRunPane == null) {
            System.out.println("Error loading condition run pane");
            return;
        }
        Platform.runLater(() -> conditionRunVBoxSideContent.getChildren().clear());
        currentConditionRunController.setParentScrollPane(entryConditionScrollPane);
        HBox entryConditionSideMenuHbox = SideMenuController.getDropDownHBox(null, new Label(ConditionType.Entry.name()), currentConditionRunController);
        // update side hierarchy
        Platform.runLater(() -> conditionRunVBoxSideContent.getChildren().add(entryConditionSideMenuHbox));
        Platform.runLater(() -> entryConditionHBox.getChildren().add(conditionRunPane));
    }

    private void loadExitConditionRunPane() {
        Node conditionRunPane = loadConditionRunPane();
        if (conditionRunPane == null) {
            System.out.println("Error loading condition run pane");
            return;
        }
        conditionRunExitVBoxContainer.setVisible(true);
        currentConditionRunController.setParentScrollPane(exitConditionScrollPane);
        HBox exitConditionSideMenuHbox = SideMenuController.getDropDownHBox(null, new Label(ConditionType.Exit.name()), currentConditionRunController);
        // update side hierarchy
        Platform.runLater(() -> conditionRunVBoxSideContent.getChildren().add(exitConditionSideMenuHbox));
        Platform.runLater(() -> exitConditionHBox.getChildren().add(conditionRunPane));
    }

    private Node loadConditionRunPane() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("conditionRunPane.fxml"));
            Node conditionRunPane = fxmlLoader.load();
            currentConditionRunController = fxmlLoader.getController();
            return conditionRunPane;
        } catch (IOException e) {
            System.out.println("Fail loading condition run pane");
        }
        return null;
    }

    private void clearConditionHBox(ConditionType conditionType) {
        if (conditionType == ConditionType.Entry)
            entryConditionHBox.getChildren().clear();
        else
            exitConditionHBox.getChildren().clear();
    }
}
