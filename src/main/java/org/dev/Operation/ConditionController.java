package org.dev.Operation;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;
import org.dev.AppScene;
import org.dev.Enum.ConditionRequirement;
import org.dev.Enum.LogLevel;
import org.dev.Operation.Condition.Condition;
import java.net.URL;
import java.util.ResourceBundle;

public class ConditionController implements Initializable, ActivityController {

    @FXML
    private StackPane conditionStackPane;
    @FXML
    private Label requirementStatusLabel, readingConditionLabel;
    @FXML
    private ImageView conditionImageView;

    @Setter
    private ActionController parentActionController;
    @Getter
    private Condition condition;
    @Getter
    private boolean isSet = false;
    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        conditionStackPane.setOnMouseClicked(this::openConditionOptionPane);
    }

    public Node getParentNode() {
        return conditionStackPane;
    }

    public void registerReadingCondition(Condition condition) {
        if (condition == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Condition is null - registerReadingCondition");
            return;
        }
        isSet = true;
        this.condition = condition;
        readingConditionLabel.setText(condition.getChosenReadingCondition().name());
        conditionImageView.setImage(SwingFXUtils.toFXImage(condition.getMainDisplayImage(), null));
        requirementStatusLabel.setText(condition.isRequired() ?
                ConditionRequirement.Required.name() : ConditionRequirement.Optional.name());
    }

    private void openConditionOptionPane(MouseEvent event) {
        if (AppScene.isOperationRunning) {
            AppScene.addLog(LogLevel.INFO, className, "Operation is running - cannot modify");
            return;
        }
        AppScene.openConditionMenuPane(this);
    }

    public void removeThisConditionFromParent() {
        parentActionController.removeCondition(this);
        AppScene.addLog(LogLevel.DEBUG, className, "Condition removed");
    }

    public void loadSavedCondition(Condition condition) {
        if (condition == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - cannot load null saved condition");
            return;
        }
        registerReadingCondition(condition);
    }
}
