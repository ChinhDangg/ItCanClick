package org.dev.Menu;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.dev.App;
import org.dev.Enum.ActionTypes;
import org.dev.Task.ActionController;
import org.dev.Task.ActivityController;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ActionMenuController extends MenuController implements Initializable {
    @FXML
    private ChoiceBox<ActionTypes> actionTypeChoice;
    private ActionController actionController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
    }

    protected void loadTypeChoices() {
        System.out.println("Loading Action types");
        actionTypeChoice.getItems().addAll(ActionTypes.values());
        actionTypeChoice.setValue(ActionTypes.MouseClick);
    }
    protected void closeMenuController(MouseEvent event) {
        App.closeActionMenuPane();
        if (actionPerformMenuController != null && actionPerformMenuController.visible) {
            actionPerformMenuController.backToPreviousMenu(event);
            actionPerformMenuController.resetMenu();
        }

    }
    protected void startRegistering(MouseEvent event) {
        System.out.println("Click on start registering");
        if (actionPerformMenuController == null)
            loadActionPerformMenu();
        actionController.setChosenActionPerform(actionTypeChoice.getValue());
        actionPerformMenuController.loadMenu(actionController);
    }
    public void loadMenu(ActivityController activityController) {
        this.actionController = (ActionController) activityController;
    }

    private ActionPerformMenuController actionPerformMenuController;
    private void loadActionPerformMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("actionPerformMenuPane.fxml"));
            Pane actionPerformMenuPane = loader.load();
            actionPerformMenuController = loader.getController();
            mainMenuStackPane.getChildren().add(actionPerformMenuPane);
        } catch (IOException e) {
            System.out.println("Fail loading action perform menu pane");
        }
    }


}
