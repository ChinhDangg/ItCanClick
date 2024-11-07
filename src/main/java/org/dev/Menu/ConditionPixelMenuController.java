package org.dev.Menu;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.dev.App;
import org.dev.AppScene;
import org.dev.Operation.Condition.Condition;
import org.dev.Operation.Condition.PixelCondition;
import org.dev.Operation.ConditionController;
import org.dev.Enum.ReadingCondition;
import org.dev.Operation.ActivityController;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;

public class ConditionPixelMenuController extends OptionsMenuController implements Initializable {
    @FXML
    private Pane pixelMenuPane;
    @FXML
    private CheckBox showHideLineCheckBox, blackWhiteLineCheckBox;
    @FXML
    private CheckBox notOptionCheckBox, requiredOptionCheckBox, globalSearchCheckBox;
    private ConditionController conditionController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        loadPixelTypeChoiceBox();
    }
    private void loadPixelTypeChoiceBox() {
        System.out.println("Loading pixel menu pixel type choice");
        showHideLineCheckBox.setOnAction(this::showHideBoxAction);
        blackWhiteLineCheckBox.setOnAction(this::changePixelLineColor);
    }

    // ------------------------------------------------------
    @Override
    public void loadMenu(ActivityController activityController) {
        if (activityController == null) {
            System.out.println("Condition Controller is not set pixel menu - bug");
            return;
        }
        this.conditionController = (ConditionController) activityController;
        Condition condition = conditionController.getCondition();
        if (condition != null && condition.getChosenReadingCondition() == ReadingCondition.Pixel) {
            PixelCondition pixelCondition = (PixelCondition) conditionController.getCondition();
            System.out.println("Loading preset reading pixel");
            currentMainImage = pixelCondition.getMainImage();
            displayMainImageView(pixelCondition.getDisplayImage());
            mainImageBoundingBox = pixelCondition.getMainImageBoundingBox();
            currentDisplayImage = pixelCondition.getDisplayImage();
            notOptionCheckBox.setSelected(pixelCondition.isNot());
            requiredOptionCheckBox.setSelected(pixelCondition.isRequired());
            imageWidth = currentMainImage.getWidth();
            imageHeight = currentMainImage.getHeight();
            outsideBoxWidth = (currentDisplayImage.getHeight() - imageHeight)/2;
        }
        else
            resetPixelMenu();
        GlobalScreen.addNativeKeyListener(this);
        showMenu(true);
    }
    private void resetPixelMenu() {
        resetMenu();
        notOptionCheckBox.setSelected(false);
        requiredOptionCheckBox.setSelected(true);
        showHideLineCheckBox.setSelected(true);
        blackWhiteLineCheckBox.setSelected(true);
    }
    public void showMenu(boolean show) {
        pixelMenuPane.setVisible(show);
        visible = show;
    }

    // ------------------------------------------------------
    @Override
    protected void save(MouseEvent event) {
        if (conditionController == null) {
            System.out.println("Condition Controller is not set - bug");
            return;
        }
        System.out.println("Clicked on save button");
        if (currentMainImage == null) {
            System.out.println("Reading pixel condition is not set - save failed");
            return;
        }
        conditionController.registerReadingCondition(new PixelCondition(ReadingCondition.Pixel, currentMainImage,
                mainImageBoundingBox, notOptionCheckBox.isSelected(), requiredOptionCheckBox.isSelected(),
                currentDisplayImage, globalSearchCheckBox.isSelected()));
    }
    @Override
    protected void backToPreviousMenu(MouseEvent event) {
        if (visible) {
            System.out.println("Backed to main menu");
            stopAllListeners();
            showMenu(false);
            AppScene.conditionMenuController.loadMenu(conditionController);
        }
    }

    // ------------------------------------------------------
    private BufferedImage getDisplayImageForReadingPixel(int x, int y) throws AWTException {
        mainImageBoundingBox = new Rectangle(x, y, imageWidth, imageHeight);
        currentMainImage = captureCurrentScreen(mainImageBoundingBox);
        BufferedImage box = (showHideLineCheckBox.isSelected()) ? drawBox(imageWidth, imageHeight, getPixelColor()) : null;
        BufferedImage imageWithEdges = (box == null) ? getImageWithEdges(currentMainImage, x, y, 1.0f) :
                getImageWithEdges(box, x, y, 1.0f);
        currentDisplayImage = (imageWithEdges == null) ? currentMainImage : imageWithEdges;
        BufferedImage zoomedImage = getZoomedImage(imageWithEdges);
        if (zoomedImage != null)
            return zoomedImage;
        else if (imageWithEdges != null)
            return imageWithEdges;
        return (box == null) ? currentMainImage : box;
    }
    public BufferedImage drawBox(int width, int height, Color color) {
        BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = temp.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return temp;
    }

    // ------------------------------------------------------
    private void showHideBoxAction(javafx.event.ActionEvent event) {
        if (mouseStopped) {
            Graphics2D g = currentDisplayImage.createGraphics();
            if (showHideLineCheckBox.isSelected())
                g.drawImage(drawBox(imageWidth, imageHeight, getPixelColor()),
                        outsideBoxWidth, outsideBoxWidth, imageWidth, imageHeight, null);
            else
                g.drawImage(currentMainImage, outsideBoxWidth, outsideBoxWidth, imageWidth, imageHeight, null);
            g.dispose();
            if (currentZoomValue != 1.00)
                displayMainImageView(getScaledImage(currentDisplayImage, currentZoomValue));
            else
                displayMainImageView(currentDisplayImage);
        }
    }
    private void changePixelLineColor(javafx.event.ActionEvent event) {
        if (mouseStopped) {
            showHideLineCheckBox.setSelected(true);
            Graphics2D g = currentDisplayImage.createGraphics();
            g.drawImage(drawBox(imageWidth, imageHeight, getPixelColor()),
                    outsideBoxWidth, outsideBoxWidth, imageWidth, imageHeight, null);
            g.dispose();
            if (currentZoomValue != 1.00)
                displayMainImageView(getScaledImage(currentDisplayImage, currentZoomValue));
            else
                displayMainImageView(currentDisplayImage);
        }
    }
    private Color getPixelColor() {
        return (blackWhiteLineCheckBox.isSelected()) ? Color.BLACK : Color.WHITE;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == mouseTimer) {
            Point p = MouseInfo.getPointerInfo().getLocation();
            if (p.equals(previousMousePoint)) {
                return;
            }
            previousMousePoint = p;
            try {
                displayMainImageView(getDisplayImageForReadingPixel(p.x, p.y));
            } catch (Exception ex) {
                System.out.println("Error at mose displaying captured image at pixel menu");
            }
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        if (e.getKeyCode() == NativeKeyEvent.VC_F2)
            startMouseMotionListening();
        else if (e.getKeyCode() == NativeKeyEvent.VC_F1)
            stopMouseMotionListening();
    }
}