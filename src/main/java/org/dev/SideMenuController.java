package org.dev;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import lombok.Setter;
import org.dev.Operation.*;
import java.io.IOException;
import java.io.Serial;
import java.net.URL;
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

        ObservableList< Node> sideHierarchyChildren = sideHierarchyVBox.getChildren();
        sideHierarchyChildren.clear();

        VBox operationTaskVBox = operationController.getTaskGroupVBox();
        HBox operationSideLabelHBox = getDropDownHBox(operationTaskVBox, operationController.getOperationNameLabel(), operationController);
        sideHierarchyChildren.add(operationSideLabelHBox);
        sideHierarchyChildren.add(operationTaskVBox);
    }

    private static void sideLabelDoubleClick(MouseEvent event, MainJobController jobController) {
        if (event.getClickCount() == 2 && !App.isOperationRunning)
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
