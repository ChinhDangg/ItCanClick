package org.dev;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import org.dev.Operation.*;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class SideMenuController implements Initializable {

    @FXML
    private Group folderIconGroupButton;
    @FXML
    private StackPane mainSideHierarchyStackPane;
    @FXML
    private VBox sideHierarchyVBox;
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
            App.loadNewEmptyOperation();
            App.loadSideMenuHierarchy();
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

        sideHierarchyVBox.getChildren().clear();
        Label operationLabel = new Label(operationController.getOperation().getOperationName());
        operationLabel.setOnMouseClicked(mouseEvent -> sideLabelDoubleClick(mouseEvent, operationController));
        VBox taskGroup = new VBox();
        HBox operationHBox = getDropDownHBox(taskGroup, operationLabel);
        sideHierarchyVBox.getChildren().add(operationHBox);

        taskGroup.setPadding(new Insets(0, 0, 0, 15));
        List<MinimizedTaskController> taskControllerList = operationController.getTaskList();

        for (MinimizedTaskController minimizedTask : taskControllerList) {
            TaskController currentTaskController = minimizedTask.getTaskController();
            Label taskLabel = new Label(currentTaskController.getTask().getTaskName());
            taskLabel.setOnMouseClicked(mouseEvent -> sideLabelDoubleClick(mouseEvent, minimizedTask));
            List<ActionController> actionControllerList = currentTaskController.getActionList();

            VBox actionGroup = new VBox();
            actionGroup.setPadding(new Insets(0, 0, 0, 35));
            HBox taskHBox = getDropDownHBox(actionGroup, taskLabel);
            taskGroup.getChildren().add(taskHBox);

            for (ActionController currentActionController : actionControllerList) {
                Label actionLabel = new Label(currentActionController.getAction().getActionName());
                actionLabel.setOnMouseClicked(mouseEvent -> sideLabelDoubleClick(mouseEvent, currentActionController));
                actionGroup.getChildren().add(actionLabel);
            }
            taskGroup.getChildren().add(actionGroup);
        }
        sideHierarchyVBox.getChildren().add(taskGroup);
    }

    private void sideLabelDoubleClick(MouseEvent event, MainJobController jobController) {
        if (event.getClickCount() == 2)
            jobController.takeToDisplay();
    }

    private HBox getDropDownHBox(VBox dropDownContent, Label displayLabel) {
        Image iconImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/arrowDownIcon.png")));
        ImageView iconImageView = new ImageView(iconImage);
        StackPane group = getStackPane(dropDownContent, iconImageView);
        iconImageView.setFitHeight(7);
        iconImageView.setFitWidth(7);
        HBox dropDownHBox = new HBox();
        dropDownHBox.setAlignment(Pos.CENTER_LEFT);
        dropDownHBox.setSpacing(5);
        dropDownHBox.getChildren().add(group);
        dropDownHBox.getChildren().add(displayLabel);
        return dropDownHBox;
    }

    private StackPane getStackPane(VBox dropDownContent, ImageView iconImageView) {
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
