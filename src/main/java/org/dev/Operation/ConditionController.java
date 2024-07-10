package org.dev.Operation;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import org.dev.App;
import org.dev.Enum.ConditionRequirement;
import org.dev.Operation.Condition.Condition;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;

public class ConditionController implements Initializable, ActivityController {

    @FXML
    private StackPane conditionStackPane;
    @FXML
    private Label requirementStatusLabel, readingConditionLabel;
    @FXML
    private ImageView conditionImageView;

    @Getter
    private Condition condition;

    @Getter
    private boolean isSet = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        conditionStackPane.setOnMouseClicked(this::openConditionOptionPane);
        conditionStackPane.setPadding(new javafx.geometry.Insets(10));
    }

    public void registerReadingCondition(Condition condition) {
        if (condition == null)
            throw new NullPointerException("Condition is null");
        isSet = true;
        this.condition = condition;
        readingConditionLabel.setText(condition.getChosenReadingCondition().name());
        conditionImageView.setImage(SwingFXUtils.toFXImage(condition.getMainDisplayImage(), null));
        requirementStatusLabel.setText(condition.isRequired() ?
                ConditionRequirement.Required.name() : ConditionRequirement.Optional.name());
    }

    private void openConditionOptionPane(MouseEvent event) { App.openConditionMenuPane(this); }

    public void removeThisConditionFromParent() {
        try {
            HBox parent = (HBox) conditionStackPane.getParent();
            parent.getChildren().remove(conditionStackPane);
        } catch (Exception e) {
            System.out.println("Fail removing condition pane from parent");
        }
    }

    public void loadSavedCondition(Condition condition) {
        if (condition == null)
            throw new NullPointerException("Can't load null Condition");
        registerReadingCondition(condition);
    }
}
