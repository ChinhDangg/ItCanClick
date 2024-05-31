package org.dev.Task;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import org.dev.App;
import org.dev.Task.Action.Action;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ActionController implements Initializable, TaskController {
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
    public void registerActionPerform(Action action, BufferedImage displayImage) {
        isSet = true;
        this.action = action;
        actionImage.setImage(SwingFXUtils.toFXImage(displayImage, null));
    }

    private final List<ConditionController> entryConditionControllers = new ArrayList<>();
    private final List<ConditionController> exitConditionControllers = new ArrayList<>();
    public List<ConditionController> getEntryConditionControllers() { return new ArrayList<>(entryConditionControllers);}
    public List<ConditionController> getExitConditionControllers() {
        return new ArrayList<>(exitConditionControllers);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        actionPane.setOnMouseClicked(this::openActionMenuPane);
        entryAddButton.setOnMouseClicked(this::entryAddNewCondition);
        exitAddButton.setOnMouseClicked(this::exitAddNewCondition);
    }

    private void openActionMenuPane(MouseEvent event) { App.openActionMenuPane(this); }

    private FXMLLoader getConditionPaneLoader() {
        return new FXMLLoader(getClass().getResource("conditionPane.fxml"));
    }
    public int getNumberOfCondition(HBox conditionBox) {
        return conditionBox.getChildren().size();
    }
    private void entryAddNewCondition(MouseEvent event) {
        System.out.println("Entry add clicked");
        int numberOfCondition = getNumberOfCondition(entryConditionPane) - 1;
        if (numberOfCondition > 0 && !entryConditionControllers.get(numberOfCondition-1).isSet()) {
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
        } catch (IOException e) { System.out.println("Fail loading and adding condition panes"); }
    }
    private void exitAddNewCondition(MouseEvent event) {
        System.out.println("Exit add clicked");
        int numberOfCondition = getNumberOfCondition(exitConditionPane) - 1;
        if (numberOfCondition > 0 && !exitConditionControllers.get(numberOfCondition-1).isSet()) {
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
        } catch (IOException e) { System.out.println("Fail loading and adding condition panes"); }
    }
}
