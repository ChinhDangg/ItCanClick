package org.dev.SideMenu.LeftMenu;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.dev.Enum.AppLevel;

public class SideMenuLabelController {
    @FXML
    private HBox parentHBoxNode, contentHBox;
    @FXML
    private ImageView collapseImageIcon, labelIndicationImageView;
    @FXML
    private StackPane refHolderStackPane;
    @FXML
    private Label sideLabel;
    private final Label refLabel = new Label("REF");
    private VBox collapseContent;

    public Node createHBoxLabel(String name, VBox collapseContent, AppLevel appLevel) {
        if (appLevel == AppLevel.Action || appLevel == AppLevel.Condition) {
            labelIndicationImageView.setFitWidth(13);
            labelIndicationImageView.setFitHeight(15);
        }
        setIndicationIcon(appLevel);
        sideLabel.setText(name);
        if (collapseContent != null) {
            this.collapseContent = collapseContent;
            collapseImageIcon.setOnMouseClicked(_ -> toggleCollapseContent());
        }
        if (appLevel == AppLevel.Condition)
            collapseImageIcon.setImage(null);
        return parentHBoxNode;
    }

    public void markLabelAsRef() {
        if (!refHolderStackPane.getChildren().isEmpty())
            return;
        refHolderStackPane.getChildren().add(new Label(refLabel.getText()));
    }

    public void unmarkLabelAsRef() {
        refHolderStackPane.getChildren().clear();
    }

    public String getName() {
        return sideLabel.getText();
    }

    public void changeLabelName(String name) {
        sideLabel.setText(name);
    }

    public Node getHBoxLabel() {
        return parentHBoxNode;
    }

    private void setIndicationIcon(AppLevel appLevel) {
        String path = "/images/icons/";
        if (appLevel == AppLevel.Operation)
            path += "operation-icon.png";
        else if (appLevel == AppLevel.TaskGroup) {
            path += "group-icon.png";
            contentHBox.setStyle("-fx-padding: 0 0 0 10");
        }
        else if (appLevel == AppLevel.Task) {
            path += "task-icon.png";
            contentHBox.setStyle("-fx-padding: 0 0 0 20");
        }
        else if (appLevel == AppLevel.Action) {
            path += "action-icon.png";
            contentHBox.setStyle("-fx-padding: 0 0 0 30");
        }
        else if (appLevel == AppLevel.Condition) {
            path += "condition-icon.png";
            contentHBox.setStyle("-fx-padding: 0 0 0 40");
        }
        Image image = new Image(String.valueOf(this.getClass().getResource(path)));
        labelIndicationImageView.setImage(image);
    }

    private void toggleCollapseContent() {
        boolean isCollapsed = !collapseContent.isVisible();
        double newRotate = (isCollapsed) ? 0.0 : -90;
        collapseImageIcon.setRotate(newRotate);
        collapseContent.setVisible(isCollapsed);
        collapseContent.setManaged(isCollapsed);
    }

    public void collapseContent() {
        boolean isCollapsed = !collapseContent.isVisible();
        if (!isCollapsed)
            toggleCollapseContent();
    }

}
