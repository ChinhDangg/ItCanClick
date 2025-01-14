package org.dev.Menu;

import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.dev.Operation.ActivityController;
import java.net.URL;
import java.util.ResourceBundle;

public abstract class MenuController implements Initializable, NativeKeyListener {
    @FXML @Getter
    protected StackPane mainMenuStackPane;
    @FXML
    protected Pane backgroundPane;
    @FXML
    protected Group menuMainGroup;
    @FXML
    protected StackPane backButton, recheckButton;
    @FXML
    protected VBox recheckContentVBox;
    @FXML
    protected Pane startRegisteringButton;
    @FXML
    protected ImageView mainImageView, recheckResultImageView;
    @FXML
    protected Label recheckResultLabel;

    @FXML
    protected StackPane recheckClearButton, fitButton, zoomInButton, zoomOutButton, centerButton;
    @FXML
    protected ScrollPane savedScrollPane, recheckScrollPane;
    @FXML
    protected StackPane savedImageViewContainer;

    protected boolean isKeyListening = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadTypeChoices();
        backgroundPane.setOnMouseClicked(this::closeMenuControllerAction);
        startRegisteringButton.setOnMouseClicked(this::startRegisteringAction);
        backButton.setOnMouseClicked(this::closeMenuControllerAction);
        recheckButton.setOnMouseClicked(this::recheck);
        recheckClearButton.setOnMouseClicked(this::clearRecheckedResultAction);
        fitButton.setOnMouseClicked(this::adjustFitImageView);
        centerButton.setOnMouseClicked(this::centerImageView);
        zoomInButton.setOnMouseClicked(this::zoomInImageView);
        zoomOutButton.setOnMouseClicked(this::zoomOutImageView);
    }

    protected abstract void loadTypeChoices();
    protected abstract void closeMenuControllerAction(MouseEvent event);
    protected abstract void startRegisteringAction(MouseEvent event);
    public abstract void loadMenu(ActivityController activityController);
    protected abstract void recheck();
    protected abstract void recheck(MouseEvent event);
    protected abstract void updateRecheckResultLabel(boolean pass, String resultText);

    protected void setMenuMainGroupVisible(boolean visible) { menuMainGroup.setVisible(visible); }

    protected void clearRecheckedResultAction(MouseEvent event) {
        recheckResultImageView.setImage(null);
        updateRecheckResultLabel(false, null);
    }

    protected void adjustFitImageView(MouseEvent event) {
        double width = 0.0, height = 0.0;
        if (mainImageView.getFitWidth() == 0.0) {
            width = savedImageViewContainer.getPrefWidth();
            height = savedImageViewContainer.getPrefHeight();
        }
        mainImageView.setFitWidth(width);
        mainImageView.setFitHeight(height);
        recheckResultImageView.setFitWidth(width);
        recheckResultImageView.setFitHeight(height);
    }

    protected double zoomedValue = 1.00;
    protected void zoomInImageView(MouseEvent event) {
        Image image = mainImageView.getImage();
        if (image == null)
            return;
        zoomedValue = Math.min(5.00, zoomedValue + 0.25);
        double width = image.getWidth() * zoomedValue, height = image.getHeight() * zoomedValue;
        mainImageView.setFitWidth(width);
        mainImageView.setFitHeight(height);
        recheckResultImageView.setFitWidth(width);
        recheckResultImageView.setFitHeight(height);
    }
    protected void zoomOutImageView(MouseEvent event) {
        Image image = mainImageView.getImage();
        if (image == null)
            return;
        zoomedValue = Math.max(0.25, zoomedValue - 0.25);
        double width = image.getWidth() * zoomedValue, height = image.getHeight() * zoomedValue;
        mainImageView.setFitWidth(width);
        mainImageView.setFitHeight(height);
        recheckResultImageView.setFitWidth(width);
        recheckResultImageView.setFitHeight(height);
    }

    protected void centerImageView(MouseEvent event) {
        double centerValue = 0.5;
        savedScrollPane.setHvalue(centerValue);
        savedScrollPane.setVvalue(centerValue);
        recheckScrollPane.setHvalue(centerValue);
        recheckScrollPane.setVvalue(centerValue);
    }

}
