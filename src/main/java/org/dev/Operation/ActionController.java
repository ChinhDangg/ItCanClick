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
import org.dev.App;
import org.dev.Enum.ActionTypes;
import org.dev.Operation.Action.Action;
import org.dev.Operation.Condition.Condition;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ActionController implements Initializable, ActivityController {

    @Getter
    @FXML
    private Label actionNameLabel;
    @FXML
    private Pane renameOptionPane;
    @FXML
    private StackPane renameButton;
    @FXML
    private TextField renameActionTextField;
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
    private Action action;
    @Getter
    @Setter
    private ActionTypes chosenActionPerform;
    private final List<ConditionController> entryConditionControllers = new ArrayList<>();
    private final List<ConditionController> exitConditionControllers = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        actionPane.setOnMouseClicked(this::openActionMenuPane);
        entryAddButton.setOnMouseClicked(this::entryAddNewCondition);
        exitAddButton.setOnMouseClicked(this::exitAddNewCondition);
        actionNameLabel.setOnMouseClicked(this::showRenameActionOption);
        renameButton.setOnMouseClicked(this::changeActionName);
        renameOptionPane.setVisible(false);
    }

    private boolean actionNameVisible = false;
    private void showRenameActionOption(MouseEvent event) {
        if (!actionNameVisible)
            renameActionTextField.setText(actionNameLabel.getText());
        actionNameVisible = !actionNameVisible;
        renameOptionPane.setVisible(actionNameVisible);
    }

    private void changeActionName(MouseEvent event) {
        String newName = renameActionTextField.getText().replace("\n", "");
        if (!newName.isBlank()) {
            renameActionTextField.setText("");
            actionNameLabel.setText(newName);
            actionNameVisible = !actionNameVisible;
            renameOptionPane.setVisible(actionNameVisible);
        }
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

    public void registerActionPerform(Action action, BufferedImage displayImage) {
        isSet = true;
        this.action = action;
        actionImage.setImage(SwingFXUtils.toFXImage(displayImage, null));
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

    private void entryAddNewCondition(MouseEvent event) {
        System.out.println("Entry add clicked");
        int numberOfCondition = getNumberOfCondition(entryConditionPane) - 1;
        if (numberOfCondition > 0 && !entryConditionControllers.get(numberOfCondition - 1).isSet()) {
            System.out.println("Previous Entry Condition is not set yet");
            return;
        }
        try {
            if (numberOfCondition < 5) {
                FXMLLoader loader = getConditionPaneLoader();
                StackPane pane = loader.load();
                entryConditionControllers.add(loader.getController());
                entryConditionPane.getChildren().add(numberOfCondition, pane);
            }
        } catch (IOException e) {
            System.out.println("Fail loading and adding condition panes");
        }
    }

    private void exitAddNewCondition(MouseEvent event) {
        System.out.println("Exit add clicked");
        int numberOfCondition = getNumberOfCondition(exitConditionPane) - 1;
        if (numberOfCondition > 0 && !exitConditionControllers.get(numberOfCondition - 1).isSet()) {
            System.out.println("Previous Exit Condition is not set yet");
            return;
        }
        try {
            if (numberOfCondition < 5) {
                FXMLLoader loader = getConditionPaneLoader();
                StackPane pane = loader.load();
                exitConditionControllers.add(loader.getController());
                exitConditionPane.getChildren().add(numberOfCondition, pane);
            }
        } catch (IOException e) {
            System.out.println("Fail loading and adding condition panes");
        }
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
                if (checkAllConditions(entryConditionControllers)) {
                    System.out.println(STR."Found entry with \{actionName}");
                    action.performAction();
                    entryPassed = true;
                    System.out.println(STR."\{actionName} performed: \{count}");
                }
                else if (!entryPassed) {
                    System.out.println(STR."Not found entry with \{actionName} \{count}");
                    continue;
                }
                Thread.sleep(action.getWaitAfterTime());
                if (checkAllConditions(exitConditionControllers)) {
                    System.out.println(STR."Found exit with \{actionName}");
                    return true;
                }
                else
                    System.out.println("Can't find exit");
                count--;
            }
        } catch (Exception e) {
            System.out.println("Fail performing action with number of attempts");
        }
        System.out.println(STR."Exceeded number of attempt for performing  \{actionName}");
        return false;
    }
    private boolean performActionWithProgressiveSearch() {
        try {
            long startTime = System.currentTimeMillis();
            int duration = action.getProgressiveSearchTime();
            boolean entryPassed = false;
            System.out.println("Starting progressive search");
            while (System.currentTimeMillis() - startTime < duration) {
                if (checkAllConditions(entryConditionControllers)) {
                    action.performAction();
                    entryPassed = true;
                }
                if (entryPassed && checkAllConditions(exitConditionControllers))
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
}