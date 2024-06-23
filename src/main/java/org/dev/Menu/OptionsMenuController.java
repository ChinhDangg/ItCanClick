package org.dev.Menu;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.dev.Operation.ActivityController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;

public abstract class OptionsMenuController implements ActionListener, Initializable, NativeKeyListener {
    protected boolean visible = false;

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
    protected Pane zoomMinusButton, zoomPlusButton;
    protected double currentZoomValue = 1.00;
    protected void increaseZoom(MouseEvent event) {
        System.out.println("Clicked on zoom plus button");
        double step = 0.25, max = 5.00;
        updateZoomValue(Math.min((currentZoomValue+step), max));
    }
    protected void decreaseZoom(MouseEvent event) {
        System.out.println("Clicked on zoom minus button");
        double step = 0.25, min = 0.25;
        updateZoomValue(Math.max((currentZoomValue-step), min));
    }
    protected void updateZoomValue(double value) {
        currentZoomValue = value;
        currentZoomLabel.setText(Double.toString(currentZoomValue));
    }
    protected BufferedImage getZoomedImage(BufferedImage imageWithEdges) {
        if (currentZoomValue != 1.00) {
            if (imageWithEdges != null) {
                BufferedImage temp = getScaledImage(imageWithEdges, currentZoomValue);
                adjustMainImageWidth(temp.getWidth());
                adjustMainImageHeight(temp.getHeight());
                return temp;
            }
            adjustMainImageWidth((int) (currentMainImage.getWidth() * currentZoomValue));
            adjustMainImageHeight((int) (currentMainImage.getHeight() * currentZoomValue));
            return getScaledImage(currentMainImage, currentZoomValue);
        }
        return null;
    }

    // ------------------------------------------------------
    @FXML
    protected ImageView mainImageView;
    @FXML
    protected StackPane mainImageViewContainer;
    @FXML
    protected CheckBox fitImageCheckBox;
    protected BufferedImage currentDisplayImage = null;
    protected BufferedImage currentMainImage = null;
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
    protected Pane xMinusButton, xPlusButton, yMinusButton, yPlusButton;
    protected void increaseImageWidth(MouseEvent event) {
        System.out.println("Clicked on x plus button");
        int step = 10;
        imageWidth += step;
        adjustMainImageWidth(imageWidth);
    }
    protected void decreaseImageWidth(MouseEvent event) {
        System.out.println("Clicked on x minus button");
        int step = 10;
        imageWidth = Math.max((imageWidth-step), 1);
    }
    protected void increaseImageHeight(MouseEvent event) {
        System.out.println("Clicked on y plus button");
        int step = 10;
        imageHeight += step;
        adjustMainImageHeight(imageHeight);
    }
    protected void decreaseImageHeight(MouseEvent event) {
        System.out.println("Clicked on y minus button");
        int step = 10;
        imageHeight = Math.max((imageHeight-step), 1);
    }
    protected BufferedImage getImageWithEdges(BufferedImage mainImage, int x, int y, float opacity) throws AWTException {
        if (outsideBoxWidth != 0) {
            BufferedImage outsideImage = captureCurrentScreen(new Rectangle(x-outsideBoxWidth, y-outsideBoxWidth,
                    imageWidth+outsideBoxWidth*2, imageHeight+outsideBoxWidth*2));
            BufferedImage imageWithEdges = new BufferedImage(outsideImage.getWidth(), outsideImage.getHeight(), outsideImage.getType());
            Graphics2D g = imageWithEdges.createGraphics();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g.drawImage(outsideImage, 0, 0, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g.drawImage(mainImage, outsideBoxWidth, outsideBoxWidth, imageWidth, imageHeight, null);
            g.dispose();
            return imageWithEdges;
        }
        return null;
    }
    public static BufferedImage captureCurrentScreen(Rectangle rectangle) throws AWTException {
        return new Robot().createScreenCapture(rectangle);
    }
    protected static BufferedImage getScaledImage(BufferedImage image, double scaleValue) {
        int w = (int) ((double) image.getWidth() * scaleValue);
        int h = (int) ((double) image.getHeight() * scaleValue);
        BufferedImage tempImage = new BufferedImage(w, h, image.getType());
        Graphics2D g = tempImage.createGraphics();
        g.drawImage(image, 0,0, w, h,null);
        g.dispose();
        return tempImage;
    }
    // ------------------------------------------------------

    @FXML
    protected Pane outsideBoxMinusButton, outsideBoxPlusButton;
    protected int outsideBoxWidth = 10;
    protected void increaseOutsideBox(MouseEvent event) {
        System.out.println("Clicked on extending outside box plus button");
        int step = 10;
        outsideBoxWidth += step;
        int totalOutsideWidth = outsideBoxWidth*2;
        adjustMainImageWidth(imageWidth + totalOutsideWidth);
        adjustMainImageHeight(imageHeight + totalOutsideWidth);
    }
    protected void decreaseOutsideBox(MouseEvent event) {
        System.out.println("Clicked on extending outside box minus button");
        int step = 10;
        outsideBoxWidth = Math.max((outsideBoxWidth-step), 0);
        int totalOutsideWidth = outsideBoxWidth*2;
        adjustMainImageWidth(imageWidth + totalOutsideWidth);
        adjustMainImageHeight(imageHeight + totalOutsideWidth);
    }

    // ------------------------------------------------------
    protected final Timer mouseTimer = new Timer(0, this);
    protected Point previousMousePoint = new Point(-1, -1);
    protected boolean mouseStopped = true;
    protected void startMouseMotionListening() {
        if (mouseStopped) {
            System.out.println("Start mouse motion listening");
            mouseTimer.start();
            mouseStopped = false;
        }
    }
    protected void stopMouseMotionListening() {
        if (!mouseStopped) {
            System.out.println("Stop mouse motion listening");
            mouseTimer.stop();
            mouseStopped = true;
        }
    }
    @FXML
    protected Pane startReadingConditionButton, stopReadingConditionButton;
    protected void startMouseMotion(MouseEvent event) { startMouseMotionListening(); }
    protected void stopMouseMotion(MouseEvent event) { stopMouseMotionListening(); }
    protected void stopAllListeners() {
        try {
            GlobalScreen.removeNativeKeyListener(this);
            stopMouseMotionListening();
            System.out.println("Stopped tracking mouse and keyboard");
        } catch (Exception e) {
            System.out.println("Problem with remove all listener");
        }
    }

    @FXML
    protected Pane backButton, saveButton;
    protected abstract void save(MouseEvent event);
    protected abstract void backToPreviousMenu(MouseEvent event);
    protected abstract void loadMenu(ActivityController controller);
    protected abstract void showMenu(boolean show);
    protected void resetMenu() {
        updateZoomValue(1);
        displayMainImageView(null);
        resetImageWidthHeight();
        fitImageCheckBox.setSelected(true);
    }
}
