package org.dev.RunJob;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.ConditionType;
import org.dev.Enum.LogLevel;
import org.dev.Job.Action.Action;
import org.dev.Job.Condition.Condition;
import org.dev.Job.Condition.ImageCheckResult;
import org.dev.Job.JobData;
import org.dev.jobManagement.JobRunStructure;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ActionRunController extends RunActivity implements Initializable, JobRunController<Boolean> {

    @FXML
    private Node parentNode;
    @FXML
    private Node containerPane;
    @FXML
    private ScrollPane entryConditionScrollPane, exitConditionScrollPane;
    @FXML
    private ImageView actionSavedImageView, actionPerformedImageView;
    @FXML
    private Label actionRunNameLabel;
    @FXML
    private Label actionStatusLabel, actionRequireLabel;
    @FXML
    private StackPane actionStackPaneImageContainer;
    @FXML
    private HBox entryConditionHBox, exitConditionHBox;
    @FXML
    private VBox actionRunVBox, conditionRunEntryVBoxContainer, conditionRunExitVBoxContainer;

    private JobRunStructure currentRunStructure;
    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        showActionRunPane(false);
        conditionRunExitVBoxContainer.setVisible(false);
        setFitDimensionImageView();
    }

    private void loadScale() {
        if (currentRunStructure.getParentController() != null)
            return;
        double currentGlobalScale = 1.0;
        if (currentGlobalScale != AppScene.currentGlobalScale) {
            currentGlobalScale = AppScene.currentGlobalScale;
            containerPane.getTransforms().add(new Scale(currentGlobalScale, currentGlobalScale, 0, 0));
        }
    }

    private void setFitDimensionImageView() {
        double width = actionStackPaneImageContainer.getPrefWidth();
        double height = actionStackPaneImageContainer.getPrefHeight();
        actionSavedImageView.setFitWidth(width);
        actionSavedImageView.setFitWidth(height);
        actionPerformedImageView.setFitHeight(width);
        actionPerformedImageView.setFitHeight(height);
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

    public void showCondition(Node node) {
        if (entryConditionHBox.getChildren().contains(node))
            changeConditionScrollPaneValue(entryConditionScrollPane, node);
        else if (exitConditionHBox.getChildren().contains(node))
            changeConditionScrollPaneValue(exitConditionScrollPane, node);
    }

    private void changeConditionScrollPaneValue(ScrollPane conditionScrollPane, Node node) {
        double targetPaneX = node.getBoundsInParent().getMinX() * AppScene.currentGlobalScale;
        double contentWidth = conditionScrollPane.getContent().getBoundsInLocal().getWidth();
        double scrollPaneHeight = conditionScrollPane.getViewportBounds().getWidth();
        double vValue = Math.min(targetPaneX / (contentWidth - scrollPaneHeight), 1.00);
        conditionScrollPane.setVvalue(vValue);
    }

    // ------------------------------------------------------
    @Override
    public Node getParentNode() { return parentNode; }

    @Override
    public AppLevel getAppLevel() {
        return AppLevel.Action;
    }

    @Override
    public void takeToDisplay() {
        AppScene.updateMainDisplayScrollValue(getParentNode());
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    @Override
    public void setJobRunStructure(JobRunStructure runStructure) {
        currentRunStructure = runStructure;
        loadScale();
    }

    @Override
    public Boolean startJob(JobData jobData) {
        if (jobData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Action data is null - cannot start");
            return false;
        }
        Action action = (Action) jobData.getMainJob();
        if (action == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Action is null - cannot start");
            return false;
        }
        changeLabelText(actionRunNameLabel, action.getActionName());
        actionRequireLabel.setText(action.isRequired() ? "Required" : "Optional");
        changeActionRunStatus(RunningStatus.Running);
        AppScene.addLog(LogLevel.INFO, className, "Start running action: " + action.getActionName());
        try {
            return runAction(jobData);
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail to start running action: " + action.getActionName());
            return false;
        }
    }

    private boolean runAction(JobData actionData) throws InterruptedException {
        boolean passed;
        if (((Action) actionData.getMainJob()).isUseProgressiveSearch())
            passed = performActionWithProgressiveSearch(actionData);
        else
            passed = performActionWithAttempt(actionData);
        updateActionRunStatus(passed);
        return passed;
    }

    // ------------------------------------------------------
    private boolean performActionWithAttempt(JobData actionData) throws InterruptedException {
        Action action = (Action) actionData.getMainJob();
        String actionName = action.getActionName();
        int totalAttempt = action.getAttempt();
        int count = totalAttempt;
        boolean entryPassed, actionPerformed = false;
        while (count > 0) {
            count--;
            AppScene.addLog(LogLevel.INFO, className, "Waiting " + action.getWaitBeforeTime()/1000 + " seconds");
            Thread.sleep(action.getWaitBeforeTime());
            ImageCheckResult entryResult = checkAllConditions(actionData.getJobDataList(), ConditionType.Entry);
            entryPassed = entryResult.isPass();
            if (!entryPassed) {
                AppScene.addLog(LogLevel.INFO, className, "Not found entry at action: " + actionName + " : " + count + "/" + totalAttempt);
                if (!actionPerformed)
                    continue;
                actionPerformed = false;
            }
            else {
                AppScene.addLog(LogLevel.INFO, className, "Found entry at action: " + actionName + " : " + count + "/" + totalAttempt);
                if (action.isUseEntry()) // if action is set to click on found entry
                    action.setMainImageBoundingBox(entryResult.getBoundingBox());
                performAction(action);
                actionPerformed = true;
            }
            AppScene.addLog(LogLevel.INFO, className, "Waiting " + action.getWaitAfterTime()/1000 + " seconds");
            Thread.sleep(action.getWaitAfterTime());
            ImageCheckResult exitResult = checkAllConditions(actionData.getJobDataList(), ConditionType.Exit);
            if (exitResult.isPass()) {
                AppScene.addLog(LogLevel.INFO, className, "Found exit at action: " + actionName + " : " + count + "/" + totalAttempt);
                return true;
            }
            AppScene.addLog(LogLevel.INFO, className, "Not found exit at action: " + actionName + " : " + count + "/" + totalAttempt);
        }
        AppScene.addLog(LogLevel.INFO, className, "Exceeded number of attempt at action: " + actionName);
        return false;
    }

    private boolean performActionWithProgressiveSearch(JobData actionData) {
        Action action = (Action) actionData.getMainJob();
        String actionName = action.getActionName();
        long startTime = System.currentTimeMillis();
        int duration = action.getProgressiveSearchTime();
        boolean entryPassed, actionPerformed = false;
        AppScene.addLog(LogLevel.INFO, className, "Start progressive search at action: " + actionName + " for: " + duration);
        while (System.currentTimeMillis() - startTime < duration) {
            ImageCheckResult entryResult = checkAllConditions(actionData.getJobDataList(), ConditionType.Entry);
            entryPassed = entryResult.isPass();
            if (!entryPassed) {
                if (!actionPerformed)
                    continue;
                actionPerformed = false;
            }
            else {
                if (action.isUseEntry()) // if action is set to click on found entry
                    action.setMainImageBoundingBox(entryResult.getBoundingBox());
                performAction(action);
                actionPerformed = true;
            }
            conditionRunExitVBoxContainer.setVisible(true);
            ImageCheckResult exitResult = checkAllConditions(actionData.getJobDataList(), ConditionType.Exit);
            if (exitResult.isPass())
                return true;
        }
        AppScene.addLog(LogLevel.INFO, className, "Exceeded progressive search time at action: " + actionName);
        return false;
    }

    private void performAction(Action action) {
        showActionRunPane(true);
        updateImageView(actionSavedImageView, action.getMainDisplayImage());
        try {
            updateImageView(actionPerformedImageView, action.getSeenImage());
        } catch (AWTException e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error getting action seen image");
        }
        action.performAction();
        AppScene.addLog(LogLevel.INFO, className, "Performed action: " + action.getActionName());
    }

    private ImageCheckResult checkAllConditions(List<JobData> fullConditionList, ConditionType conditionType) {
        AppScene.addLog(LogLevel.INFO, className, "Start checking condition: " + conditionType);
        if (fullConditionList == null)
            return new ImageCheckResult(true);
        List<JobData> conditionDataList = getConditionList(fullConditionList, conditionType);
        if (conditionDataList.isEmpty())
            return new ImageCheckResult(true);
        Platform.runLater(() -> clearConditionHBox(conditionType));
        // all conditions are optional therefore only need one condition to pass
        if (checkAllConditionsIsNotRequired(conditionDataList)) {
            for (JobData c : conditionDataList) {
                loadConditionRunPane(conditionType);
                ImageCheckResult result = currentConditionRunController.startJob(c);
                if (result.isPass())
                    return result;
            }
            return new ImageCheckResult(false);
        }
        else { // only check required condition and they must pass
            ImageCheckResult result = null;
            for (JobData c : conditionDataList) {
                loadConditionRunPane(conditionType);
                if (((Condition) c.getMainJob()).isRequired()) {
                    result = currentConditionRunController.startJob(c);
                    if (!result.isPass())
                        return new ImageCheckResult(false);
                }
            }
            return result; // return the last result, still passed
        }
    }
    private boolean checkAllConditionsIsNotRequired(List<JobData> conditionData) {
        if (conditionData == null || conditionData.isEmpty())
            return true;
        for (JobData c : conditionData)
            if (((Condition) c.getMainJob()).isRequired())
                return false;
        return true;
    }
    private List<JobData> getConditionList(List<JobData> fullCondition, ConditionType conditionType) {
        List<JobData> conditionList = new ArrayList<>();
        for (JobData c : fullCondition)
            if (((Condition) c.getMainJob()).getConditionType() == conditionType)
                conditionList.add(c);
        return conditionList;
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
        Platform.runLater(() -> entryConditionHBox.getChildren().add(conditionRunPane));
        Platform.runLater(() -> currentRunStructure.getSideContent().getChildren().clear());

        JobRunStructure jobRunStructure = new JobRunStructure(currentRunStructure.getDisplayParentController(), this, currentConditionRunController, ConditionType.Entry.name());
        currentConditionRunController.setJobRunStructure(jobRunStructure);
        Platform.runLater(() -> currentRunStructure.addToSideContent(jobRunStructure.getSideHBoxLabel(), jobRunStructure.getSideContent()));
    }

    private void loadExitConditionRunPane() {
        Node conditionRunPane = loadConditionRunPane();
        if (conditionRunPane == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - condition run pane is null - Could not load exit condition run pane");
            return;
        }
        conditionRunExitVBoxContainer.setVisible(true);
        Platform.runLater(() -> exitConditionHBox.getChildren().add(conditionRunPane));

        JobRunStructure jobRunStructure = new JobRunStructure(currentRunStructure.getDisplayParentController(), this, currentConditionRunController, ConditionType.Exit.name());
        currentConditionRunController.setJobRunStructure(jobRunStructure);
        Platform.runLater(() -> currentRunStructure.addToSideContent(jobRunStructure.getSideHBoxLabel(), jobRunStructure.getSideContent()));
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
