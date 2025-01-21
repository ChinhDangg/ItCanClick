package org.dev.Menu;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseEvent;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import org.dev.Job.Condition.Condition;
import org.dev.Job.Condition.PixelCondition;
import org.dev.JobController.ConditionController;
import org.dev.Enum.ReadingCondition;
import org.dev.JobController.ActivityController;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;

public class ConditionPixelMenuController extends OptionsMenuController implements Initializable {
    @FXML
    private Group parentGroup;
    @FXML
    private CheckBox showHideLineCheckBox, blackWhiteLineCheckBox;
    @FXML
    private CheckBox notOptionCheckBox, requiredOptionCheckBox, globalSearchCheckBox;

    private ConditionController conditionController;
    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        loadPixelTypeChoiceBox();
    }
    private void loadPixelTypeChoiceBox() {
        showHideLineCheckBox.setOnAction(this::showHideBoxAction);
        blackWhiteLineCheckBox.setOnAction(this::changePixelLineColor);
        AppScene.addLog(LogLevel.TRACE, className, "Pixel menu pixel type choice loaded");
    }

    // ------------------------------------------------------
    @Override
    public void loadMenu(ActivityController activityController) {
        if (activityController == null) {
            AppScene.addLog(LogLevel.WARN, className, "Condition controller is not set - loadMenu");
            return;
        }
        this.conditionController = (ConditionController) activityController;
        Condition condition = conditionController.getCondition();
        if (condition != null && condition.getChosenReadingCondition() == ReadingCondition.Pixel) {
            PixelCondition pixelCondition = (PixelCondition) conditionController.getCondition();
            displayMainImageView(pixelCondition.getDisplayImage());
            mainImageBoundingBox = pixelCondition.getMainImageBoundingBox();
            currentDisplayImage = pixelCondition.getDisplayImage();
            notOptionCheckBox.setSelected(pixelCondition.isNot());
            requiredOptionCheckBox.setSelected(pixelCondition.isRequired());
            imageWidth = (int) mainImageBoundingBox.getWidth();
            imageHeight = (int) mainImageBoundingBox.getHeight();
            outsideBoxWidth = (currentDisplayImage.getHeight() - imageHeight)/2;
            AppScene.addLog(LogLevel.TRACE, className, "Loaded preset reading pixel");
        }
        else
            resetMenu();
        GlobalScreen.addNativeKeyListener(this);
        showMenu(true);
    }
    @Override
    protected void resetMenu() {
        super.resetMenu();
        notOptionCheckBox.setSelected(false);
        requiredOptionCheckBox.setSelected(true);
        showHideLineCheckBox.setSelected(true);
        blackWhiteLineCheckBox.setSelected(true);
        AppScene.addLog(LogLevel.TRACE, className, "Menu reset");
    }
    public void showMenu(boolean show) {
        visible = show;
        parentGroup.setVisible(visible);
        AppScene.addLog(LogLevel.TRACE, className, "Menu showed: " + visible);
    }

    // ------------------------------------------------------
    @Override
    protected void save(MouseEvent event) {
        if (conditionController == null) {
            AppScene.addLog(LogLevel.WARN, className, "Condition controller is not set - save");
            return;
        }
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on save button");
        if (currentDisplayImage == null) {
            AppScene.addLog(LogLevel.WARN, className, "Reading pixel condition is not set - save failed");
            return;
        }
        conditionController.registerReadingCondition(new PixelCondition(ReadingCondition.Pixel, currentDisplayImage,
                mainImageBoundingBox, notOptionCheckBox.isSelected(), requiredOptionCheckBox.isSelected(),
                globalSearchCheckBox.isSelected()));
        AppScene.addLog(LogLevel.INFO, className, "Pixel - saved");
    }
    @Override
    protected void backToPreviousMenu(MouseEvent event) {
        if (visible) {
            stopAllListeners();
            showMenu(false);
            AppScene.conditionMenuController.loadMenu(conditionController);
            resetMenu();
            AppScene.addLog(LogLevel.DEBUG, className, "Backed to main condition menu");
        }
    }

    // ------------------------------------------------------
    private BufferedImage getDisplayImageForReadingPixel(int x, int y) throws AWTException {
        mainImageBoundingBox = new Rectangle(x, y, imageWidth, imageHeight);
        Rectangle fullBounds = new Rectangle(x-outsideBoxWidth, y-outsideBoxWidth,
                imageWidth+outsideBoxWidth*2, imageHeight+outsideBoxWidth*2);
        currentDisplayImage = captureCurrentScreen(fullBounds);
        BufferedImage box = (showHideLineCheckBox.isSelected()) ? drawBox(imageWidth, imageHeight, getPixelColor()) : null;
        BufferedImage imageWithEdges = (box == null) ? Condition.getImageWithEdges(mainImageBoundingBox, currentDisplayImage, 1.0f) :
                Condition.getImageWithEdges(box, currentDisplayImage, 1.0f);
        BufferedImage zoomedImage = getZoomedImage(imageWithEdges);
        if (zoomedImage != null)
            return zoomedImage;
        return imageWithEdges;
    }
    private BufferedImage drawBox(int width, int height, Color color) {
        BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = temp.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return temp;
    }

    // ------------------------------------------------------
    private void showHideBoxAction(javafx.event.ActionEvent event) {
        if (currentDisplayImage == null || !mouseStopped)
            return;
        Graphics2D g = currentDisplayImage.createGraphics();
        if (showHideLineCheckBox.isSelected())
            g.drawImage(drawBox(imageWidth, imageHeight, getPixelColor()),
                    outsideBoxWidth, outsideBoxWidth, imageWidth, imageHeight, null);
        g.dispose();
        if (currentZoomValue != 1.00)
            displayMainImageView(getScaledImage(currentDisplayImage, currentZoomValue));
        else
            displayMainImageView(currentDisplayImage);
    }
    private void changePixelLineColor(javafx.event.ActionEvent event) {
        if (currentDisplayImage == null || !mouseStopped)
            return;
        showHideLineCheckBox.setSelected(true);
        AppScene.addLog(LogLevel.DEBUG, className, "Change pixel line color");
        Graphics2D g = currentDisplayImage.createGraphics();
        g.drawImage(drawBox(imageWidth, imageHeight, getPixelColor()),
                outsideBoxWidth, outsideBoxWidth, imageWidth, imageHeight, null);
        g.dispose();
        if (currentZoomValue != 1.00)
            displayMainImageView(getScaledImage(currentDisplayImage, currentZoomValue));
        else
            displayMainImageView(currentDisplayImage);
    }
    private Color getPixelColor() {
        return blackWhiteLineCheckBox.isSelected() ? Color.BLACK : Color.WHITE;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == mouseTimer) {
            Point p = MouseInfo.getPointerInfo().getLocation();
            if (p.equals(previousMousePoint))
                return;
            previousMousePoint = p;
            try {
                displayMainImageView(getDisplayImageForReadingPixel(p.x, p.y));
            } catch (Exception ex) {
                AppScene.addLog(LogLevel.ERROR, className, "Error at displaying captured image at mouse pointer: " + ex.getMessage());
            }
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        AppScene.addLog(LogLevel.TRACE, className, "Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        if (e.getKeyCode() == NativeKeyEvent.VC_F2) {
            AppScene.addLog(LogLevel.INFO, className, "Starting mouse listening");
            AppScene.addLog(LogLevel.DEBUG, className, "Clicked on F2 key to start mouse listening");
            startMouseMotionListening();
        }
        else if (e.getKeyCode() == NativeKeyEvent.VC_F1) {
            AppScene.addLog(LogLevel.INFO, className, "Stopping mouse listening");
            AppScene.addLog(LogLevel.DEBUG, className, "Clicked on F1 key to stop mouse listening");
            stopMouseMotionListening();
        }
    }
}