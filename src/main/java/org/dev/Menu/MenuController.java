package org.dev.Menu;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.dev.Task.ActivityController;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class MenuController implements Initializable {
    @FXML
    protected StackPane mainMenuStackPane;
    @FXML
    protected Pane backgroundPane;
    @FXML
    protected Pane startRegisteringButton;
    @FXML
    protected ImageView mainImageView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadTypeChoices();
        backgroundPane.setOnMouseClicked(this::closeMenuController);
        startRegisteringButton.setOnMouseClicked(this::startRegistering);
    }

    protected abstract void loadTypeChoices();
    protected abstract void closeMenuController(MouseEvent event);
    protected abstract void startRegistering(MouseEvent event);
    public abstract void loadMenu(ActivityController activityController);
}
