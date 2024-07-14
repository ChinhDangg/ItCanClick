package org.dev;

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
import org.dev.Operation.ActionController;
import org.dev.Operation.MinimizedTaskController;
import org.dev.Operation.OperationController;
import org.dev.Operation.TaskController;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class SideMenuController implements Initializable {

    @FXML
    private Group folderIconGroupButton;
    @FXML
    private VBox sideHierarchyVBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        folderIconGroupButton.setOnMouseClicked(this::toggleSideHierarchy);
    }

    private void toggleSideHierarchy(MouseEvent mouseEvent) {
        boolean newSet = !sideHierarchyVBox.isVisible();
        sideHierarchyVBox.setVisible(newSet);
        sideHierarchyVBox.setManaged(newSet);
    }

    public void loadSideHierarchy(OperationController operationController) {
        VBox operationGroup = new VBox();
        VBox taskGroup = new VBox();
        operationGroup.setPadding(new Insets(0, 0, 0, 5));
        HBox operationHBox = getDropDownHBox(taskGroup);
        Label operationLabel = new Label(operationController.getOperation().getOperationName());
        operationHBox.getChildren().add(operationLabel);
        operationGroup.getChildren().add(operationHBox);

        taskGroup.setPadding(new Insets(0, 0, 0, 15));
        List<MinimizedTaskController> taskControllerList = operationController.getTaskList();

        for (MinimizedTaskController minimizedTask : taskControllerList) {
            TaskController currentTaskController = minimizedTask.getTaskController();
            Label taskLabel = new Label(currentTaskController.getTask().getTaskName());
            List<ActionController> actionControllerList = currentTaskController.getActionList();

            VBox actionGroup = new VBox();
            HBox taskHBox = getDropDownHBox(actionGroup);
            taskHBox.getChildren().add(taskLabel);
            taskGroup.getChildren().add(taskHBox);

            actionGroup.setPadding(new Insets(0, 0, 0, 35));
            for (ActionController currentActionController : actionControllerList) {
                Label actionLabel = new Label(currentActionController.getAction().getActionName());
                actionGroup.getChildren().add(actionLabel);
            }
            taskGroup.getChildren().add(actionGroup);
        }
        operationGroup.getChildren().add(taskGroup);
        sideHierarchyVBox.getChildren().add(operationGroup);
    }

    private HBox getDropDownHBox(VBox dropDownContent) {
        Image iconImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/arrowDownIcon.png")));
        ImageView iconImageView = new ImageView(iconImage);
        StackPane group = getStackPane(dropDownContent, iconImageView);
        iconImageView.setFitHeight(7);
        iconImageView.setFitWidth(7);
        HBox dropDownHBox = new HBox();
        dropDownHBox.setAlignment(Pos.CENTER_LEFT);
        dropDownHBox.setSpacing(5);
        dropDownHBox.getChildren().add(group);
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
