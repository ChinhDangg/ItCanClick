package org.dev.RunOperation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import lombok.Setter;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.Operation.Condition.Condition;
import org.dev.Operation.Condition.ImageCheckResult;
import org.dev.Operation.MainJobController;
import java.net.URL;
import java.util.ResourceBundle;

public class ConditionRunController extends RunActivity implements Initializable, MainJobController {

    @FXML
    private HBox mainConditionRunHBox;
    @FXML
    private Label expectedResultLabel, readResultLabel;
    @FXML
    private Pane conditionExpectedPane, conditionReadPane;
    @FXML
    private ImageView conditionExpectedImageView, conditionReadImageView;
    @FXML
    private StackPane stackPaneImageViewContainer;

    @Setter
    private ScrollPane parentScrollPane;
    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        double width = stackPaneImageViewContainer.getPrefWidth();
        double height = stackPaneImageViewContainer.getPrefHeight();
        conditionExpectedImageView.setFitWidth(width);
        conditionExpectedImageView.setFitHeight(height);
        conditionReadImageView.setFitWidth(width);
        conditionReadImageView.setFitHeight(height);
    }

    @Override
    public void takeToDisplay() {
        AppScene.currentLoadedOperationRunController.changeScrollPaneVValueView(parentScrollPane);
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    @Override
    public AppLevel getAppLevel() {
        return AppLevel.Condition;
    }

    public boolean checkCondition(Condition condition) {
        updateImageView(conditionExpectedImageView, condition.getMainDisplayImage());
        changeLabelText(expectedResultLabel, condition.getExpectedResult());

        ImageCheckResult checkedResult = condition.checkCondition();
        changeLabelText(readResultLabel, condition.getActualResult());
        updateImageView(conditionReadImageView, checkedResult.getDisplayImage());

        boolean passed = checkedResult.isPass();
        updatePaneStatusColor(conditionReadPane, passed);
        if (condition.isRequired())
            updatePaneStatusColor(conditionExpectedPane, passed);
        return passed;
    }
}
