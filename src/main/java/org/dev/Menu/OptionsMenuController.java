package org.dev.Menu;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import org.dev.JobController.ActivityController;
import org.dev.Job.Condition.TextCondition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;

public abstract class OptionsMenuController implements ActionListener, Initializable, NativeKeyListener {
    protected boolean visible = false;
    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        backButton.setOnMouseClicked(this::backToPreviousMenu);
        saveButton.setOnMouseClicked(this::save);
        xMinusButton.setOnMouseClicked(this::decreaseImageWidth);
        xPlusButton.setOnMouseClicked(this::increaseImageWidth);
        yMinusButton.setOnMouseClicked(this::decreaseImageHeight);
        yPlusButton.setOnMouseClicked(this::increaseImageHeight);
        outsideBoxMinusButton.setOnMouseClicked(this::decreaseOutsideBox);
        outsideBoxPlusButton.setOnMouseClicked(this::increaseOutsideBox);
        zoomMinusButton.setOnMouseClicked(this::decreaseZoom);
        zoomPlusButton.setOnMouseClicked(this::increaseZoom);
        startReadingConditionButton.setOnMouseClicked(this::startMouseMotion);
        stopReadingConditionButton.setOnMouseClicked(this::stopMouseMotion);
        fitImageCheckBox.setOnAction(this::toggleFillImageOption);
    }

    @FXML
    protected Label currentZoomLabel;
    @FXML
    protected Node zoomMinusButton, zoomPlusButton;
    protected double currentZoomValue = 1.00;
    protected void increaseZoom(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on zoom plus button");
        double step = 0.25, max = 5.00;
        updateZoomValue(Math.min((currentZoomValue+step), max));
    }
    protected void decreaseZoom(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on zoom minus button");
        double step = 0.25, min = 0.25;
        updateZoomValue(Math.max((currentZoomValue-step), min));
    }
    protected void updateZoomValue(double value) {
        currentZoomValue = value;
        currentZoomLabel.setText(Double.toString(currentZoomValue));
        AppScene.addLog(LogLevel.DEBUG, className, "Updated Zoom value: " + currentZoomValue);
    }
    protected BufferedImage getZoomedImage(BufferedImage image) {
        if (currentZoomValue == 1.00)
            return null;
        return getScaledImage(image, currentZoomValue);
    }

    // ------------------------------------------------------
    @FXML
    protected ImageView mainImageView;
    @FXML
    protected StackPane mainImageViewContainer;
    @FXML
    protected CheckBox fitImageCheckBox;
    protected BufferedImage currentDisplayImage = null;
    protected Rectangle mainImageBoundingBox = null;
    protected void toggleFillImageOption(ActionEvent event) {
        if (!fitImageCheckBox.isSelected()) {
            mainImageViewContainer.setClip(new javafx.scene.shape.Rectangle(mainImageViewContainer.getPrefWidth(), mainImageViewContainer.getPrefHeight()));
            mainImageView.setFitWidth(0);
            mainImageView.setFitHeight(0);
        }
        else if (currentDisplayImage != null) {
            adjustMainImageWidth(currentDisplayImage.getWidth());
            adjustMainImageHeight(currentDisplayImage.getHeight());
            displayMainImageView(currentDisplayImage);
        }
        AppScene.addLog(LogLevel.DEBUG, className, "Fill Image option toggled: " + fitImageCheckBox.isSelected());
    }
    protected void displayMainImageView(BufferedImage image) {
        if (image != null)
            mainImageView.setImage(SwingFXUtils.toFXImage(image, null));
        else
            mainImageView.setImage(null);
    }

    protected int imageWidth = 100, imageHeight = 100;
    protected void resetImageWidthHeight() {
        imageWidth = 100; imageHeight = 100;
        AppScene.addLog(LogLevel.TRACE, className, "Image width and Image height reset to 100");
    }
    protected void adjustMainImageWidth(int checkingWidth) {
        if (fitImageCheckBox.isSelected()) {
            int maxViewWidth = (int) mainImageViewContainer.getPrefWidth();
            if (checkingWidth > maxViewWidth && mainImageView.getFitWidth() != maxViewWidth)
                mainImageView.setFitWidth(maxViewWidth);
            else if (checkingWidth < maxViewWidth && mainImageView.getFitWidth() != 0)
                mainImageView.setFitWidth(0);
        }
    }
    protected void adjustMainImageHeight(int checkingHeight) {
        if (fitImageCheckBox.isSelected()) {
            int maxViewHeight = (int) mainImageViewContainer.getPrefHeight();
            if (checkingHeight > maxViewHeight && mainImageView.getFitHeight() != maxViewHeight)
                mainImageView.setFitHeight(maxViewHeight);
            else if (checkingHeight < maxViewHeight && mainImageView.getFitHeight() != 0)
                mainImageView.setFitHeight(0);
        }
    }

    // ------------------------------------------------------
    @FXML
    protected Node xMinusButton, xPlusButton, yMinusButton, yPlusButton;
    protected void increaseImageWidth(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on x plus button");
        int step = 10;
        imageWidth += step;
        adjustMainImageWidth(imageWidth);
    }
    protected void decreaseImageWidth(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on x minus button");
        int step = 10;
        imageWidth = Math.max((imageWidth-step), 1);
    }
    protected void increaseImageHeight(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on y plus button");
        int step = 10;
        imageHeight += step;
        adjustMainImageHeight(imageHeight);
    }
    protected void decreaseImageHeight(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on y minus button");
        int step = 10;
        imageHeight = Math.max((imageHeight-step), 1);
    }
    public static BufferedImage captureCurrentScreen(Rectangle rectangle) throws AWTException {
        return new Robot().createScreenCapture(rectangle);
    }
    protected BufferedImage getScaledImage(BufferedImage image, double scaleValue) {
        int w = (int) ((double) image.getWidth() * scaleValue);
        int h = (int) ((double) image.getHeight() * scaleValue);
        adjustMainImageWidth(w);
        adjustMainImageHeight(h);
        return TextCondition.getScaledImage(image, scaleValue);
    }

    // ------------------------------------------------------
    @FXML
    protected Node outsideBoxMinusButton, outsideBoxPlusButton;
    protected int outsideBoxWidth = 10;
    protected void increaseOutsideBox(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on extending outside box plus button");
        int step = 10;
        outsideBoxWidth += step;
        int totalOutsideWidth = outsideBoxWidth * 2;
        AppScene.addLog(LogLevel.INFO, className, "Updated Edges width: " + totalOutsideWidth);
        adjustMainImageWidth(imageWidth + totalOutsideWidth);
        adjustMainImageHeight(imageHeight + totalOutsideWidth);
    }
    protected void decreaseOutsideBox(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on extending outside box minus button");
        int step = 10;
        outsideBoxWidth = Math.max((outsideBoxWidth-step), 0);
        int totalOutsideWidth = outsideBoxWidth * 2;
        AppScene.addLog(LogLevel.INFO, className, "Updated Edges width: " + totalOutsideWidth);
        adjustMainImageWidth(imageWidth + totalOutsideWidth);
        adjustMainImageHeight(imageHeight + totalOutsideWidth);
    }

    // ------------------------------------------------------
    protected final Timer mouseTimer = new Timer(0, this);
    protected Point previousMousePoint = new Point(-1, -1);
    protected boolean mouseStopped = true;
    protected void startMouseMotionListening() {
        if (mouseStopped) {
            mouseTimer.start();
            mouseStopped = false;
            AppScene.addLog(LogLevel.DEBUG, className, "Started mouse motion listening");
        }
    }
    protected void stopMouseMotionListening() {
        if (!mouseStopped) {
            mouseTimer.stop();
            mouseStopped = true;
            AppScene.addLog(LogLevel.DEBUG, className, "Stopped mouse motion listening");
        }
    }

    @FXML
    protected Node startReadingConditionButton, stopReadingConditionButton;
    protected void startMouseMotion(MouseEvent event) {
        AppScene.addLog(LogLevel.INFO, className, "Starting capturing image at mouse");
        startMouseMotionListening();
    }
    protected void stopMouseMotion(MouseEvent event) {
        AppScene.addLog(LogLevel.INFO, className, "Stopping capturing image at mouse");
        stopMouseMotionListening();
    }
    protected void stopAllListeners() {
        try {
            GlobalScreen.removeNativeKeyListener(this);
            stopMouseMotionListening();
            AppScene.addLog(LogLevel.DEBUG, className, "Stopped tracking mouse and keyboard");
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error with removing all listener: " + e.getMessage());
        }
    }

    @FXML
    protected Node backButton, saveButton;
    protected abstract void save(MouseEvent event);
    protected abstract void backToPreviousMenu(MouseEvent event);
    protected abstract void loadMenu(ActivityController controller);
    protected abstract void showMenu(boolean show);
    protected void resetMenu() {
        updateZoomValue(1);
        displayMainImageView(null);
        resetImageWidthHeight();
        outsideBoxWidth = 10;
        fitImageCheckBox.setSelected(true);
        currentDisplayImage = null;
        AppScene.addLog(LogLevel.TRACE, className, "Shared menu content is reset");
    }
}
