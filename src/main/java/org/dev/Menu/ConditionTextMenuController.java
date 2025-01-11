package org.dev.Menu;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
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
    private Group parentGroup;
    @FXML
    private Label readingResultLabel;
    @FXML
    private Node textScaleMinusButton, textScalePlusButton;
    @FXML
    private Label currentTextScaleLabel, registeredTextLabel;
    @FXML
    private StackPane addTextButton, addReadTextImageButton, popTextButton;
    @FXML
    private TextField addTextTextField;
    @FXML
    private CheckBox notOptionCheckBox, requiredOptionCheckBox;

    private ConditionController conditionController;
    private final String className = this.getClass().getSimpleName();

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
            AppScene.addLog(LogLevel.WARN, className, "Condition controller is not set - loadMenu");
            return;
        }
        this.conditionController = (ConditionController) activityController;
        Condition condition = conditionController.getCondition();
        if (condition != null && condition.getChosenReadingCondition() == ReadingCondition.Text) {
            TextCondition textCondition = (TextCondition) conditionController.getCondition();
            updateTextScaleValue(textCondition.getCurrentTextScale());
            currentMainImage = condition.getMainImage();
            displayMainImageView(currentMainImage);
            readTexts = textCondition.getSavedText();
            updateRegisteredTextLabel();
            notOptionCheckBox.setSelected(textCondition.isNot());
            requiredOptionCheckBox.setSelected(textCondition.isRequired());
            mainImageBoundingBox = textCondition.getMainImageBoundingBox();
            AppScene.addLog(LogLevel.TRACE, className, "Loaded preset reading text condition");
        }
        else
            resetMenu();
        GlobalScreen.addNativeKeyListener(this);
        showMenu(true);
    }
    @Override
    protected void resetMenu() {
        super.resetMenu();
        updateTextScaleValue(1);
        notOptionCheckBox.setSelected(false);
        requiredOptionCheckBox.setSelected(true);
        readingResultLabel.setText(initialReadingResult);
        registeredTextLabel.setText("None");
        addTextTextField.setText("");
        readTexts = new HashSet<>();
        AppScene.addLog(LogLevel.TRACE, className, "Menu reset");
    }
    @Override
    public void showMenu(boolean show) {
        visible = show;
        parentGroup.setVisible(visible);
        AppScene.addLog(LogLevel.TRACE, className, "Menu showed: " + visible);
    }

    @Override
    protected void save(MouseEvent event) {
        if (conditionController == null) {
            AppScene.addLog(LogLevel.WARN, className, "Condition controller is not set - save");
            return;
        }
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on save button");
        if (readTexts.isEmpty()) {
            AppScene.addLog(LogLevel.WARN, className, "Reading text condition is not set - save failed");
            return;
        }
        conditionController.registerReadingCondition(new TextCondition(ReadingCondition.Text, currentMainImage,
                mainImageBoundingBox, notOptionCheckBox.isSelected(), requiredOptionCheckBox.isSelected(),
                currentDisplayImage, currentTextScaleValue, readTexts));
        AppScene.addLog(LogLevel.INFO, className, "Saved");
    }
    @Override
    protected void backToPreviousMenu(MouseEvent event) {
        if (visible) {
            stopAllListeners();
            showMenu(false);
            AppScene.conditionMenuController.loadMenu(conditionController);
            AppScene.addLog(LogLevel.DEBUG, className, "Backed to main menu");
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
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on increase text scale");
    }
    private void decreaseTextScale(MouseEvent event) {
        System.out.println("Clicked on decrease text scale");
        double step = 0.25, min = 0.25;
        updateTextScaleValue(Math.max((currentTextScaleValue-step), min));
        updateZoomValue(1.00);
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on decrease text scale");
    }
    private void updateTextScaleValue(double value) {
        currentTextScaleValue = value;
        currentTextScaleLabel.setText(Double.toString(currentTextScaleValue));
        AppScene.addLog(LogLevel.DEBUG, className, "Updated text reading scale: " + currentTextScaleValue);
    }

    // ------------------------------------------------------
    private BufferedImage getDisplayImageForReadingText(int x, int y) throws AWTException {
        mainImageBoundingBox = new Rectangle(x, y, imageWidth, imageHeight);
        currentMainImage = captureCurrentScreen(mainImageBoundingBox);
        BufferedImage imageWithEdges = getImageWithEdges(currentMainImage, x, y, 0.5f);
        if (currentTextScaleValue != 1.00) {
            currentMainImage = getScaledImage(currentMainImage, currentTextScaleValue);
            if (imageWithEdges != null)
                imageWithEdges = getScaledImage(imageWithEdges, currentTextScaleValue);
        }
        currentDisplayImage = (imageWithEdges == null) ? currentMainImage : imageWithEdges;
        BufferedImage zoomedImage = getZoomedImage(imageWithEdges);
        if (zoomedImage != null)
            return zoomedImage;
        return (imageWithEdges == null) ? currentMainImage : imageWithEdges;
    }
    private void readAndUpdateReadTextLabel() {
        if (currentMainImage != null) {
            try {
                String readText = TextCondition.readTextFromImage(currentMainImage);
                Platform.runLater(() -> readingResultLabel.setText(readText));
            } catch (Exception e) {
                AppScene.addLog(LogLevel.ERROR, className, "Error reading from image or updating read text label: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------
    private Set<String> readTexts = new LinkedHashSet<>();
    private void addText(MouseEvent event) {
        String newText = addTextTextField.getText();
        if (!newText.isBlank()) {
            newText = newText.replace("\n", "");
            addNewReadingText(newText);
            updateRegisteredTextLabel();
            addTextTextField.setText("");
            AppScene.addLog(LogLevel.DEBUG, className, "Added new reading text: " + newText);
        }
        else
            AppScene.addLog(LogLevel.TRACE, className, "No text entered");
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
            addNewReadingText(readText);
            updateRegisteredTextLabel();
            AppScene.addLog(LogLevel.DEBUG, className, "Added text from read Image: " + readText);
        }
        else
            AppScene.addLog(LogLevel.DEBUG, className, "No text is read from image");
    }
    public void addNewReadingText(String text) {
        text = text.replace("\n", "");
        readTexts.add(text);
    }
    private void removeRecentAddedText(MouseEvent event) {
        int index = readTexts.size() - 1;
        if (index <= 0)
            AppScene.addLog(LogLevel.DEBUG, className, "No text found to remove");
        else {
            String lastElement = null;
            for (String readText : readTexts)
                lastElement = readText;
            readTexts.remove(lastElement);
            updateRegisteredTextLabel();
            AppScene.addLog(LogLevel.DEBUG, className, "Recent text removed: " + lastElement);
        }
    }
    private void updateRegisteredTextLabel() { registeredTextLabel.setText(getAllReadText(readTexts)); }

    // ------------------------------------------------------
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (e.getSource() == mouseTimer) {
            Point p = MouseInfo.getPointerInfo().getLocation();
            if (p.equals(previousMousePoint))
                return;
            previousMousePoint = p;
            try {
                displayMainImageView(getDisplayImageForReadingText(p.x, p.y));
            } catch (Exception ex) {
                AppScene.addLog(LogLevel.ERROR, className, "Error at display captured image at mouse pointer: " + ex.getMessage());
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
            AppScene.addLog(LogLevel.DEBUG, className, "Clicked on F2 key to start mouse listening");
            stopMouseMotionListening();
            readAndUpdateReadTextLabel();
        }
    }

    @Override
    protected void stopMouseMotion(MouseEvent event) {
        super.stopMouseMotion(event);
        readAndUpdateReadTextLabel();
    }
}
