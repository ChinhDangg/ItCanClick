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
import org.dev.Enum.ConditionType;
import org.dev.Enum.LogLevel;
import org.dev.Job.Condition.Condition;
import org.dev.Job.JobData;
import org.dev.jobManagement.JobStructure;
import org.dev.RunJob.JobRunController;
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
    @FXML
    private Node notIndicationNode;

    private JobStructure currentStructure;
    private JobData jobData = new JobData();

    @Getter
    private Condition condition;
    @Getter @Setter
    private ConditionType conditionType;
    @Getter
    private boolean isSet = false;
    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        conditionStackPane.setOnMouseClicked(this::openConditionOptionPane);
        notIndicationNode.setVisible(false);
    }

    public void setJobStructure(JobStructure structure) {
        currentStructure = structure;
    }

    public void registerReadingCondition(Condition newCondition) {
        if (newCondition == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - Condition is null - registerReadingCondition");
            return;
        }
        isSet = true;
        jobData.setMainJob(newCondition);
        condition = newCondition;
        condition.setConditionType(conditionType);
        notIndicationNode.setVisible(condition.isNot());
        readingConditionLabel.setText(condition.getChosenReadingCondition().name());
        conditionImageView.setImage(SwingFXUtils.toFXImage(condition.getMainDisplayImage(), null));
        requirementStatusLabel.setText(condition.isRequired() ?
                ConditionRequirement.Required.name() : ConditionRequirement.Optional.name());
    }

    private void openConditionOptionPane(MouseEvent event) {
        if (AppScene.isJobRunning()) {
            AppScene.addLog(LogLevel.INFO, className, "Operation is running - cannot modify");
            return;
        }
        if (event.getButton() == MouseButton.PRIMARY)
            AppScene.openConditionMenuPane(this);
        else if (event.getButton() == MouseButton.SECONDARY) {
            SideMenuController.rightClickMenuController.showRightMenu(event, currentStructure);
        }
    }

    public void removeThisConditionFromParent() {
        currentStructure.getParentController().removeSavedData(this);
        AppScene.addLog(LogLevel.DEBUG, className, "Condition removed");
    }

    // ------------------------------------------------------
    @Override
    public String getName() { return null; }

    @Override
    public Node getParentNode() { return conditionStackPane; }

    @Override
    public JobData getSavedData() {
        if (condition == null)
            return null;
        Condition newCondition = condition.cloneData();
        newCondition.setConditionType(conditionType);
        AppScene.addLog(LogLevel.TRACE, className, "Got condition data");
        return new JobData(newCondition, null);
    }

    @Override
    public JobData getSavedDataByReference() {
        if (condition == null)
            return null;
        condition.setConditionType(conditionType);
        jobData.setMainJob(condition.cloneData());
        jobData.setJobDataList(null);
        AppScene.addLog(LogLevel.TRACE, className, "Got reference condition data");
        return jobData;
    }

    @Override
    public void loadSavedData(JobData newJobData) {
        if (newJobData == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - cannot load null saved condition");
            return;
        }
        jobData = newJobData;
        Condition condition = (Condition) jobData.getMainJob();
        registerReadingCondition(condition);
    }

    @Override
    public JobStructure addSavedData(JobData data) { return null; }

    @Override
    public void removeSavedData(JobDataController jobDataController) {}

    @Override
    public void takeToDisplay() {}

    @Override
    public AppLevel getAppLevel() {
        return AppLevel.Condition;
    }

    @Override
    public void moveSavedDataDown(JobDataController jobDataController) {}
    @Override
    public void moveSavedDataUp(JobDataController jobDataController) {}

    @Override
    public JobRunController<Object> getRunJob() { return null; }
}
