package org.dev.Menu;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.dev.App;
import org.dev.Task.Condition.PixelCondition;
import org.dev.Task.Condition.TextCondition;
import org.dev.Task.ConditionController;
import org.dev.Enum.ReadingCondition;
import org.dev.Task.ActivityController;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ConditionMenuController extends MenuController implements Initializable {
    @FXML
    private ChoiceBox<ReadingCondition> readingTypeChoice;
    @FXML
    private StackPane recheckButton;
    @FXML
    private Pane recheckAndAddTextPane;
    @FXML
    private Label recheckResultLabel;
    private ConditionController conditionController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        recheckButton.setOnMouseClicked(this:: recheck);
    }

    protected void loadTypeChoices() {
        System.out.println("Loading Condition reading type");
        readingTypeChoice.getItems().addAll(ReadingCondition.values());
        readingTypeChoice.setValue(ReadingCondition.Text);
    }
    protected void closeMenuController(MouseEvent event) {
        App.closeConditionMenuPane();
        if (textMenuController != null && textMenuController.visible) {
            textMenuController.backToPreviousMenu(event);
            textMenuController.resetTextMenu();
        }
        else if (pixelMenuController != null && pixelMenuController.visible) {
            pixelMenuController.backToPreviousMenu(event);
            pixelMenuController.resetPixelMenu();
        }
    }
    protected void startRegistering(MouseEvent event) {
        System.out.println("Click on start registering");
        if (readingTypeChoice.getValue() == ReadingCondition.Text) {
            if (textMenuController == null)
                loadReadingTextConditionMenu();
            textMenuController.loadMenu(conditionController);
        }
        else {
            if (pixelMenuController == null)
                loadReadingPixelConditionMenu();
            pixelMenuController.loadMenu(conditionController);
        }
    }
    public void loadMenu(ActivityController activityController) {
        this.conditionController = (ConditionController) activityController;
        boolean controllerSet = conditionController.isSet();
        recheckAndAddTextPane.setVisible(controllerSet);
        updateRecheckResultLabel(false, null);
        if (controllerSet)
            mainImageView.setImage(SwingFXUtils.toFXImage(
                    conditionController.getCondition().getMainDisplayImage(), null));
        else
            mainImageView.setImage(null);
    }

    private void recheck(MouseEvent event) {
        System.out.println("Recheck");
        if (!conditionController.isSet()) {
            System.out.println("Condition Controller is not set - bug");
            return;
        }
        try {
            ReadingCondition readingType = conditionController.getCondition().getChosenReadingCondition();
            if (readingType == ReadingCondition.Text) {
                TextCondition condition = (TextCondition) conditionController.getCondition();
                String currentReadText = textMenuController.readTextFromCurrentScreen(
                        condition.getMainImageBoundingBox(), condition.getCurrentTextScale());
                updateRecheckResultLabel(recheckReadText(condition.getReadText(), currentReadText), currentReadText);
            }
            else if (readingType == ReadingCondition.Pixel) {
                PixelCondition condition = (PixelCondition) conditionController.getCondition();
                BufferedImage recent = pixelMenuController.captureCurrentScreen(condition.getMainImageBoundingBox());
                BufferedImage saved = condition.getMainImage();
                boolean checked = pixelMenuController.checkPixelFromTwoImages(recent, saved);
                updateRecheckResultLabel(checked, "All");
            }
        } catch(Exception e) {
            System.out.println("Fail rechecking");
        }
    }
    private boolean recheckReadText(List<String> readTexts, String text) {
        text = text.replace("\n", "");
        for (String s : readTexts)
            if (s.equals(text))
                return true;
        return false;
    }
    private void updateRecheckResultLabel(boolean pass, String newReadText) {
        if (newReadText == null)
            recheckResultLabel.setText("Result");
        else if (pass)
            recheckResultLabel.setText("Pass: " + newReadText);
        else
            recheckResultLabel.setText("Fail: " + newReadText);
    }

    private ConditionTextMenuController textMenuController;
    private ConditionPixelMenuController pixelMenuController;
    private void loadReadingTextConditionMenu() { // called one or else redundant
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("conditionTextMenuPane.fxml"));
            Pane textMenuPane = loader.load();
            textMenuController = loader.getController();
            mainMenuStackPane.getChildren().add(textMenuPane);
        } catch (IOException e) {
            System.out.println("Fail loading reading text menu pane");
        }
    }
    private void loadReadingPixelConditionMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("conditionPixelMenuPane.fxml"));
            Pane pixelMenuPane = loader.load();
            pixelMenuController = loader.getController();
            mainMenuStackPane.getChildren().add(pixelMenuPane);
        } catch (IOException e) {
            System.out.println("Fail loading reading pixel menu pane");
        }
    }

    private ReadingCondition getCurrentReadingTypeChoice() { return readingTypeChoice.getValue(); }
}
