package org.dev;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import org.dev.Operation.*;
import org.dev.RunOperation.OperationRunController;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 operationHBox
    taskVBox
    Vbox
    taskLabelHBox
        taskActionVBox
        actionLabelHBox
        actionLabelHBox
        actionLabelHBox
    Vbox
    taskLabelHBox
        taskActionVBox
        actionLabelHBox
        actionLabelHBox
 */

public class SideMenuController implements Initializable {

    @FXML
    private Group folderIconGroupButton;
    @FXML
    private StackPane mainSideHierarchyStackPane;
    @FXML
    private VBox runSideHierarchyVBox, sideHierarchyVBox;
    @FXML
    private Group newOperationGroupButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        folderIconGroupButton.setOnMouseClicked(this::toggleSideHierarchy);
        setScaleHierarchyVBox();
        newOperationGroupButton.setOnMouseClicked(this::getNewOperationPane);
    }

    private void getNewOperationPane(MouseEvent event) {
        try {
            AppScene.loadNewEmptyOperation();
            AppScene.loadSideMenuHierarchy();
        } catch (IOException e) {
            System.out.println("Fail loading empty operation at side menu hierarchy");
        }
    }

    private void setScaleHierarchyVBox() {
        double currentScale = 1.3;
        mainSideHierarchyStackPane.getTransforms().add(new Scale(currentScale, currentScale, 0, 0));
    }

    private void toggleSideHierarchy(MouseEvent mouseEvent) {
        boolean newSet = !mainSideHierarchyStackPane.isVisible();
        mainSideHierarchyStackPane.setVisible(newSet);
        mainSideHierarchyStackPane.setManaged(newSet);
    }

    public void loadSideHierarchy(OperationController operationController) {
        newOperationGroupButton.setVisible(false);

        ObservableList<Node> sideHierarchyChildren = sideHierarchyVBox.getChildren();
        sideHierarchyChildren.clear();

        VBox operationTaskVBox = operationController.getTaskGroupVBoxSideContent();
        HBox operationSideLabelHBox = getDropDownHBox(operationTaskVBox, operationController.getOperationNameLabel(), operationController);
        sideHierarchyChildren.add(operationSideLabelHBox);
        sideHierarchyChildren.add(operationTaskVBox);
    }

    public void loadRunSideHierarchy(OperationRunController operationRunController) {
        sideHierarchyVBox.setVisible(false);
        ObservableList<Node> runSideHierarchyChildren = runSideHierarchyVBox.getChildren();
        runSideHierarchyChildren.clear();

        VBox operationRunTaskVBox = operationRunController.getTaskRunVBoxSideContent();
        HBox operationRunSideLabelHBox = getDropDownHBox(operationRunTaskVBox,
                new Label(operationRunController.getOperationNameRunLabel().getText()),
                operationRunController);
        runSideHierarchyChildren.add(operationRunSideLabelHBox);
        runSideHierarchyChildren.add(operationRunTaskVBox);
    }

    private static void sideLabelDoubleClick(MouseEvent event, MainJobController jobController) {
        if (event.getClickCount() == 2 && !AppScene.isOperationRunning)
            jobController.takeToDisplay();
    }

    public static HBox getDropDownHBox(VBox dropDownContent, Label displayLabel, MainJobController jobController) {
        HBox dropDownHBox = new HBox();
        dropDownHBox.setOnMouseClicked(event -> sideLabelDoubleClick(event, jobController));
        dropDownHBox.setOnMouseEntered(_ -> dropDownHBox.setStyle("-fx-background-color: rgb(220,220,220);"));
        dropDownHBox.setOnMouseExited(_ -> dropDownHBox.setStyle("-fx-background-color: transparent;"));
        if (dropDownContent == null) {
            dropDownHBox.getChildren().add(displayLabel);
            return dropDownHBox;
        }
        Image iconImage = new Image(Objects.requireNonNull(SideMenuController.class.getResourceAsStream("images/arrowDownIcon.png")));
        ImageView iconImageView = new ImageView(iconImage);
        StackPane group = getStackPane(dropDownContent, iconImageView);
        iconImageView.setFitHeight(7);
        iconImageView.setFitWidth(7);
        dropDownHBox.setAlignment(Pos.CENTER_LEFT);
        dropDownHBox.setSpacing(5);
        dropDownHBox.getChildren().add(group);
        dropDownHBox.getChildren().add(displayLabel);
        return dropDownHBox;
    }
    private static StackPane getStackPane(VBox dropDownContent, ImageView iconImageView) {
        StackPane group = new StackPane(iconImageView);
        group.setPrefWidth(17);
        group.setOnMouseClicked(_ -> {
            double rotation = iconImageView.getRotate();
            if (rotation == 0.0) {
                dropDownContent.setVisible(false);
                dropDownContent.setManaged(false);
                iconImageView.setRotate(-90);
            }
            else {
                dropDownContent.setVisible(true);
                dropDownContent.setManaged(true);
                iconImageView.setRotate(0);
            }
        });
        return group;
    }
}
