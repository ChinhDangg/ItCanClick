package org.dev.JobController;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.ConditionRequirement;
import org.dev.Enum.LogLevel;
import org.dev.Job.Condition.Condition;
import org.dev.JobData.JobData;
import org.dev.JobData.ConditionData;
import org.dev.SideMenu.LeftMenu.SideMenuController;

import java.net.URL;
import java.util.ResourceBundle;

public class ConditionController implements Initializable, JobDataController, ActivityController {

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

    public Node getParentNode() { return conditionStackPane; }

    public void registerReadingCondition(Condition newCondition) {
        if (newCondition == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Condition is null - registerReadingCondition");
            return;
        }
        isSet = true;
        condition = newCondition;
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
        if (event.getButton() == MouseButton.PRIMARY)
            AppScene.openConditionMenuPane(this);
        else if (event.getButton() == MouseButton.SECONDARY) {
            SideMenuController.rightClickMenuController.showRightMenu(event, this, parentActionController);
        }
    }

    public void removeThisConditionFromParent() {
        parentActionController.removeSavedData(this);
        AppScene.addLog(LogLevel.DEBUG, className, "Condition removed");
    }

    @Override
    public ConditionData getSavedData() {
        if (condition == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Error - Empty condition being used as data");
            return null;
        }
        ConditionData conditionData = new ConditionData();
        conditionData.setCondition(condition.getDeepCopied());
        return conditionData;
    }

    @Override
    public void loadSavedData(JobData jobData) {
        if (jobData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - cannot load null saved condition");
            return;
        }
        registerReadingCondition((Condition) jobData);
    }

    @Override
    public void addSavedData(JobData data) {}

    @Override
    public void removeSavedData(JobDataController jobDataController) {}

    @Override
    public void takeToDisplay() {}

    @Override
    public AppLevel getAppLevel() {
        return AppLevel.Condition;
    }
}
