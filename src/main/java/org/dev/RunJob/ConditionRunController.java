package org.dev.RunJob;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.dev.AppScene;
import org.dev.Enum.AppLevel;
import org.dev.Enum.LogLevel;
import org.dev.Job.Condition.Condition;
import org.dev.Job.Condition.ImageCheckResult;
import org.dev.Job.JobData;
import org.dev.jobManagement.JobRunStructure;
import java.net.URL;
import java.util.ResourceBundle;

public class ConditionRunController extends RunActivity implements Initializable, JobRunController<ImageCheckResult> {

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

    private JobRunStructure currentRunStructure;

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
    public Node getParentNode() { return mainConditionRunHBox; }

    @Override
    public AppLevel getAppLevel() {
        return AppLevel.Condition;
    }

    @Override
    public void takeToDisplay() {
        AppScene.updateMainDisplayScrollValue(getParentNode());
        ActionRunController parentActionRunController = (ActionRunController) currentRunStructure.getParentController();
        parentActionRunController.showCondition(getParentNode());
        AppScene.addLog(LogLevel.DEBUG, className, "Take to display");
    }

    @Override
    public void setJobRunStructure(JobRunStructure runStructure) {
        currentRunStructure = runStructure;
    }

    @Override
    public ImageCheckResult startJob(JobData jobData) {
        Condition condition = (Condition) jobData.getMainJob();
        updateImageView(conditionExpectedImageView, condition.getMainDisplayImage());
        if (condition.isNot())
            changeLabelText(expectedResultLabel, "Not " + condition.getExpectedResult());
        else
            changeLabelText(expectedResultLabel, condition.getExpectedResult());

        ImageCheckResult checkedResult = condition.checkCondition();
        changeLabelText(readResultLabel, checkedResult.getReadResult());
        updateImageView(conditionReadImageView, checkedResult.getDisplayImage());

        boolean passed = checkedResult.isPass();
        updatePaneStatusColor(conditionReadPane, passed);
        if (condition.isRequired())
            updatePaneStatusColor(conditionExpectedPane, passed);
        return checkedResult;
    }

}
