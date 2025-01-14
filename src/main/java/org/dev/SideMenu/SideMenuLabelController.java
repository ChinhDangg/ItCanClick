package org.dev.SideMenu;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.dev.Enum.AppLevel;
import org.dev.Operation.DataController;
import org.dev.Operation.MainJobController;

public class SideMenuLabelController {
    @FXML
    private HBox parentHBoxNode;
    @FXML
    private ImageView collapseImageIcon, labelIndicationImageView;
    @FXML
    private Label sideLabel;

    public Node createHBoxLabel(Label label, VBox collapseContent, DataController dataController, DataController parentController,
                                RightClickMenuController rightClickMenuController) {
        AppLevel appLevel = dataController.getAppLevel();
        parentHBoxNode.getChildren().remove(sideLabel);
        if (appLevel == AppLevel.Action || appLevel == AppLevel.Condition) {
            labelIndicationImageView.setFitWidth(13);
            labelIndicationImageView.setFitHeight(15);
            collapseImageIcon.setImage(null);
        }
        setIndicationIcon(appLevel);
        parentHBoxNode.getChildren().add(label);
        if (collapseContent != null)
            collapseImageIcon.setOnMouseClicked(_ -> collapseContent(collapseContent));
        parentHBoxNode.setOnMouseClicked(
                event -> doubleClickAndRightClick(event, dataController, parentController, rightClickMenuController));
        return parentHBoxNode;
    }

    public Node createHBoxLabel(Label label, VBox collapseContent, MainJobController jobController) {
        parentHBoxNode.getChildren().remove(sideLabel);
        parentHBoxNode.getChildren().add(label);
        if (collapseContent != null)
            collapseImageIcon.setOnMouseClicked(_ -> collapseContent(collapseContent));
        parentHBoxNode.setOnMouseClicked(event -> doubleClick(event, jobController));
        return parentHBoxNode;
    }

    private void setIndicationIcon(AppLevel appLevel) {
        String path = "/images/icons/";
        if (appLevel == AppLevel.Operation)
            path += "operation-icon.png";
        else if (appLevel == AppLevel.Task) {
            path += "task-icon.png";
            parentHBoxNode.setStyle("-fx-padding: 0 0 0 10");
        }
        else if (appLevel == AppLevel.Action) {
            path += "action-icon.png";
            parentHBoxNode.setStyle("-fx-padding: 0 0 0 20");
        }
        else if (appLevel == AppLevel.Condition) {
            path += "condition-icon.png";
            parentHBoxNode.setStyle("-fx-padding: 0 0 0 30");
        }
        Image image = new Image(String.valueOf(this.getClass().getResource(path)));
        labelIndicationImageView.setImage(image);
    }

    private void collapseContent(VBox collapseContent) {
        boolean isCollapsed = !collapseContent.isVisible();
        double newRotate = (isCollapsed) ? 0.0 : -90;
        collapseImageIcon.setRotate(newRotate);
        collapseContent.setVisible(isCollapsed);
        collapseContent.setManaged(isCollapsed);
    }

    private void doubleClick(MouseEvent event, MainJobController jobController) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2)
            jobController.takeToDisplay();
    }

    private void doubleClickAndRightClick(MouseEvent event, DataController dataController, DataController parentController, RightClickMenuController rightClickMenuController) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2)
            dataController.takeToDisplay();
        else if (event.getButton() == MouseButton.SECONDARY) {
            rightClickMenuController.showRightMenu(event, dataController, parentController);
        }
    }
}
