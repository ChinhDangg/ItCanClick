package org.dev.SideMenu;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import lombok.Getter;
import org.dev.AppScene;

import java.net.URL;
import java.util.ResourceBundle;

public class CenterBannerController implements Initializable {

    @FXML @Getter
    private StackPane parentOuterStackPane;
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
    private Popup centerMenuPopup;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        centerMenuPopup = new Popup();
        centerMenuPopup.getContent().add(parentOuterStackPane);
        xStackPaneButton.setOnMouseClicked(this::closeCenterBanner);
        confirmStackPaneButton.setOnMouseClicked(this::confirmClick);
        cancelStackPaneButton.setOnMouseClicked(this::closeCenterBanner);
    }

    public void openCenterBanner(Node node, String title, String centerMessage) {
        resultStatus = false;
        Platform.runLater(() -> bannerTitleLabel.setText(title));
        Platform.runLater(() -> centerMessageLabel.setText(centerMessage));
        int x = (int) ((node.getLayoutBounds().getWidth() - parentOuterStackPane.getWidth())/2);
        int y = (int) ((node.getLayoutBounds().getHeight() - parentOuterStackPane.getHeight())/2);
        centerMenuPopup.show(node, x, y);
    }

    private void closeCenterBanner(MouseEvent mouseEvent) {
        resultStatus = false;
        hideCenterBanner();
    }

    private void confirmClick(MouseEvent mouseEvent) {
        resultStatus = true;
        hideCenterBanner();
    }

    private void hideCenterBanner() {
        centerMenuPopup.hide();
    }
}
