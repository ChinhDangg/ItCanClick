package org.dev.Menu;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.dev.App;
import org.dev.Enum.ReadingCondition;
import org.dev.Operation.ActivityController;
import org.dev.Operation.Condition.Condition;
import org.dev.Operation.Condition.TextCondition;
import org.dev.Operation.ConditionController;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;

public class ConditionTextMenuController extends OptionsMenuController implements Initializable {
    @FXML
    private Pane textMenuPane;
    @FXML
    private Label readingResultLabel;
    @FXML
    private Pane textScaleMinusButton, textScalePlusButton;
    @FXML
    private Label currentTextScaleLabel, registeredTextLabel;
    @FXML
    private StackPane addTextButton, addReadTextImageButton, popTextButton;
    @FXML
    private TextField addTextTextField;
    @FXML
    private CheckBox notOptionCheckBox, requiredOptionCheckBox;
    private ConditionController conditionController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        textScaleMinusButton.setOnMouseClicked(this::decreaseTextScale);
        textScalePlusButton.setOnMouseClicked(this::increaseTextScale);
        addTextButton.setOnMouseClicked(this::addText);
        addReadTextImageButton.setOnMouseClicked(this::addTextFromReadImage);
        popTextButton.setOnMouseClicked(this::removeRecentAddedText);
    }

    // ------------------------------------------------------
    @Override
    public void loadMenu(ActivityController activityController) {
        if (activityController == null) {
            System.out.println("Condition Controller is not set text menu- bug");
            return;
        }
        this.conditionController = (ConditionController) activityController;
        Condition condition = conditionController.getCondition();
        if (condition != null && condition.getChosenReadingCondition() == ReadingCondition.Text) {
            TextCondition textCondition = (TextCondition) conditionController.getCondition();
            System.out.println("Loading preset reading text");
            updateTextScaleValue(textCondition.getCurrentTextScale());
            currentMainImage = condition.getMainImage();
            displayMainImageView(currentMainImage);
            registeredTextLabel.setText(getAllReadText(textCondition.getReadText()));
        }
        else
            resetTextMenu();
        GlobalScreen.addNativeKeyListener(this);
        showMenu(true);
    }
    private void resetTextMenu() {
        resetMenu();
        updateTextScaleValue(1);
        notOptionCheckBox.setSelected(false);
        requiredOptionCheckBox.setSelected(true);
        readingResultLabel.setText(initialReadingResult);
        registeredTextLabel.setText("None");
        addTextTextField.setText("");
        readTexts = new HashSet<>();
    }
    public void showMenu(boolean show) {
        textMenuPane.setVisible(show);
        visible = show;
    }

    @Override
    protected void save(MouseEvent event) {
        if (conditionController == null) {
            System.out.println("Condition Controller is not set - bug");
            return;
        }
        System.out.println("Clicked on save button");
        if (readTexts.isEmpty()) {
            System.out.println("Reading text condition is not set - save failed");
            return;
        }
        conditionController.registerReadingCondition(new TextCondition(ReadingCondition.Text, currentMainImage,
                mainImageBoundingBox, notOptionCheckBox.isSelected(), requiredOptionCheckBox.isSelected(),
                currentTextScaleValue, readTexts), currentMainImage);
    }
    @Override
    protected void backToPreviousMenu(MouseEvent event) {
        if (visible) {
            System.out.println("Backed to main menu");
            stopAllListeners();
            showMenu(false);
            App.conditionMenuController.loadMenu(conditionController);
        }
    }

    // ------------------------------------------------------
    private double currentTextScaleValue = 1.00;
    private final String initialReadingResult = ".....";
    private void increaseTextScale(MouseEvent event) {
        System.out.println("Clicked on increase text scale");
        double step = 0.25, max = 5.00;
        updateTextScaleValue(Math.min((currentTextScaleValue+step), max));
        updateZoomValue(1.00);
    }
    private void decreaseTextScale(MouseEvent event) {
        System.out.println("Clicked on decrease text scale");
        double step = 0.25, min = 0.25;
        updateTextScaleValue(Math.max((currentTextScaleValue-step), min));
        updateZoomValue(1.00);
    }
    private void updateTextScaleValue(double value) {
        currentTextScaleValue = value;
        currentTextScaleLabel.setText(Double.toString(currentTextScaleValue));
    }

    // ------------------------------------------------------
    private BufferedImage getDisplayImageForReadingText(int x, int y) throws AWTException {
        mainImageBoundingBox = new Rectangle(x, y, imageWidth, imageHeight);
        currentMainImage = captureCurrentScreen(mainImageBoundingBox);
        BufferedImage imageWithEdges = getImageWithEdges(currentMainImage, x, y, 0.5f);
        if (currentTextScaleValue != 1.00) {
            currentMainImage = getScaledImage(currentMainImage, currentTextScaleValue);
            if (imageWithEdges != null) {
                imageWithEdges = getScaledImage(imageWithEdges, currentTextScaleValue);
                adjustMainImageWidth(imageWithEdges.getWidth());
                adjustMainImageHeight(imageWithEdges.getHeight());
            }
            else {
                adjustMainImageWidth(currentMainImage.getWidth());
                adjustMainImageHeight(currentMainImage.getHeight());
            }
        }
        BufferedImage zoomedImage = getZoomedImage(imageWithEdges);
        if (zoomedImage != null)
            return zoomedImage;
        return (imageWithEdges == null) ? currentMainImage : imageWithEdges;
    }
    public static String readTextFromImage(BufferedImage image) throws TesseractException {
        System.out.println("Read texts from an image");
        ITesseract tesseract = new Tesseract();
        // deprecated but usable to remove warning variable or just import all languages in tessdata
        tesseract.setTessVariable("debug_file", "/dev/null");
        tesseract.setDatapath("tessdata");
        return tesseract.doOCR(image);
    }
    private void readAndUpdateReadTextLabel() {
        if (currentMainImage != null) {
            try {
                String readText = readTextFromImage(currentMainImage);
                Platform.runLater(() -> readingResultLabel.setText(readText));
            } catch (TesseractException e) {
                System.out.println("Error reading text from image or updating read text label");
            }
        }
    }
    public static String readTextFromCurrentScreen(Rectangle boundingBox, double scale) throws AWTException, TesseractException {
        BufferedImage image = captureCurrentScreen(boundingBox);
        if (scale != 1.00)
             image = getScaledImage(image, scale);
        return readTextFromImage(image);
    }

    // ------------------------------------------------------
    private Set<String> readTexts = new LinkedHashSet<>();
    private void addText(MouseEvent event) {
        System.out.println("Add text");
        String newText = addTextTextField.getText();
        if (!newText.isBlank()) {
            newText = newText.replace("\n", "");
            addNewReadingText(newText);
            updateRegisteredTextLabel();
            addTextTextField.setText("");
        }
        else
            System.out.println("No text entered");
    }
    public String getAllReadText(Set<String> texts) {
        StringBuilder builder = new StringBuilder();
        for (String s : texts)
            builder.append(s).append(" | ");
        return builder.toString();
    }
    private void addTextFromReadImage(MouseEvent event) {
        String readText = readingResultLabel.getText();
        if (!readText.isEmpty() && !readText.equals(initialReadingResult)) {
            System.out.println("Add text from read Image");
            addNewReadingText(readText);
            updateRegisteredTextLabel();
        }
        else
            System.out.println("No text is read");
    }
    public void addNewReadingText(String text) {
        text = text.replace("\n", "");
        readTexts.add(text);
    }
    private void removeRecentAddedText(MouseEvent event) {
        int index = readTexts.size() - 1;
        if (index <= 0)
            System.out.println("No text found to remove");
        else {
            String lastElement = null;
            for (String readText : readTexts)
                lastElement = readText;
            readTexts.remove(lastElement);
            updateRegisteredTextLabel();
            System.out.println("Recent text removed");
        }
    }
    private void updateRegisteredTextLabel() { registeredTextLabel.setText(getAllReadText(readTexts)); }

    // ------------------------------------------------------
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (e.getSource() == mouseTimer) {
            Point p = MouseInfo.getPointerInfo().getLocation();
            if (!p.equals(previousMousePoint)) {
                previousMousePoint = p;
                try {
                    currentDisplayImage = getDisplayImageForReadingText(p.x, p.y);
                    displayMainImageView(currentDisplayImage);
                } catch (Exception ex) {
                    System.out.println("Error at mouse displaying captured image");
                }
            }
        }
    }
    public void nativeKeyReleased(NativeKeyEvent e) {
        System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        if (e.getKeyCode() == NativeKeyEvent.VC_F2)
            startMouseMotionListening();
        else if (e.getKeyCode() == NativeKeyEvent.VC_F1) {
            stopMouseMotionListening();
            readAndUpdateReadTextLabel();
        }
    }
    @Override
    protected void stopMouseMotion(MouseEvent event) {
        stopMouseMotionListening();
        readAndUpdateReadTextLabel();
    }
}
