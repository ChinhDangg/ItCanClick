package org.dev.Menu;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.dev.Task.Condition.Condition;
import org.dev.Task.Condition.PixelCondition;
import org.dev.Task.ConditionController;
import org.dev.Enum.ReadingCondition;
import org.dev.Task.TaskController;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.net.URL;
import java.util.ResourceBundle;

public class ConditionPixelMenuController extends OptionsMenuController implements Initializable {
    @FXML
    private Pane pixelMenuPane;
    @FXML
    private CheckBox showHideLineCheckBox, blackWhiteLineCheckBox;
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
    public void loadMenu(TaskController taskController) {
        if (taskController == null) {
            System.out.println("Condition Controller is not set pixel menu - bug");
            return;
        }
        this.conditionController = (ConditionController) taskController;
        Condition condition = conditionController.getCondition();
        if (condition != null && condition.getChosenReadingCondition() == ReadingCondition.Pixel) {
            PixelCondition pixelCondition = (PixelCondition) conditionController.getCondition();
            System.out.println("Loading preset reading pixel");
            currentMainImage = pixelCondition.getMainImage();
            displayMainImageView(pixelCondition.getDisplayImage());
        }
        GlobalScreen.addNativeKeyListener(this);
        showPixelMenuPane(true);
    }
    public void resetPixelMenu() {
        resetMenu();
        showHideLineCheckBox.setSelected(true);
        blackWhiteLineCheckBox.setSelected(true);
    }
    public void showPixelMenuPane(boolean show) {
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
                currentDisplayImage), currentDisplayImage);
    }
    @Override
    protected void backToPreviousMenu(MouseEvent event) {
        if (visible) {
            System.out.println("Backed to main menu");
            stopAllListeners();
            showPixelMenuPane(false);
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
    public boolean checkPixelFromTwoImages(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight())
            return false;
        DataBuffer db1 = img1.getRaster().getDataBuffer();
        DataBuffer db2 = img2.getRaster().getDataBuffer();
        int size1 = db1.getSize();
        int size2 = db2.getSize();
        if (size1 != size2)
            return false;
        for (int i = 0; i < size1; i++)
            if (db1.getElem(i) != db2.getElem(i))
                return false;
        return true;
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
            if (!p.equals(previousMousePoint)) {
                previousMousePoint = p;
                try {
                    displayMainImageView(getDisplayImageForReadingPixel(p.x, p.y));
                } catch (Exception ex) {
                    System.out.println("Error at mose displaying captured image at pixel menu");
                }
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
