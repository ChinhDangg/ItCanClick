package org.dev.SideMenu;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.dev.AppScene;

import java.net.URL;
import java.util.ResourceBundle;

public class CenterBannerController implements Initializable {

    @FXML @Getter
    private StackPane parentOuterStackPane;
    @FXML
    private StackPane edgeStackPane;
    @FXML
    private StackPane xStackPaneButton;
    @FXML
    private VBox centerContentVBox;
    @FXML
    private Label bannerTitleLabel, centerMessageLabel;
    @FXML
    private StackPane confirmStackPaneButton, cancelStackPaneButton;

    @Getter
    private boolean resultStatus = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        edgeStackPane.setOnMouseClicked(this::closeCenterBanner);
        xStackPaneButton.setOnMouseClicked(this::closeCenterBanner);
        confirmStackPaneButton.setOnMouseClicked(this::confirmClick);
        cancelStackPaneButton.setOnMouseClicked(this::closeCenterBanner);
        closeCenterBanner();
    }

    public void openCenterBanner(String title, String centerMessage) {
        resultStatus = false;
        Platform.runLater(() -> bannerTitleLabel.setText(title));
        Platform.runLater(() -> centerMessageLabel.setText(centerMessage));
        parentOuterStackPane.setVisible(true);
    }

    private void confirmClick(MouseEvent mouseEvent) {
        resultStatus = true;
        closeCenterBanner();
    }

    private void closeCenterBanner(MouseEvent mouseEvent) {
        resultStatus = false;
        closeCenterBanner();
    }

    private void closeCenterBanner() {
        parentOuterStackPane.setVisible(false);
        AppScene.closeCenterBanner();
    }
}
