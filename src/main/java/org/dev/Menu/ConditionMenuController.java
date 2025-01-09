package org.dev.Menu;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import org.dev.Enum.ReadingCondition;
import org.dev.Operation.ActivityController;
import org.dev.Operation.Condition.Condition;
import org.dev.Operation.Condition.ImageCheckResult;
import org.dev.Operation.ConditionController;
import java.net.URL;
import java.util.ResourceBundle;

public class ConditionMenuController extends MenuController implements Initializable {
    @FXML
    private ChoiceBox<ReadingCondition> readingTypeChoice;
    @FXML
    private StackPane removeButton;

    private ConditionController conditionController;
    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        removeButton.setOnMouseClicked(this::removeSelectedCondition);
    }

    @Override
    protected void loadTypeChoices() {
        readingTypeChoice.getItems().addAll(ReadingCondition.values());
        readingTypeChoice.setValue(ReadingCondition.Text);
        AppScene.addLog(LogLevel.TRACE, className, "Condition reading types loaded");
    }

    @Override
    protected void closeMenuControllerAction(MouseEvent event) {
        AppScene.closeConditionMenuPane();
        if (textMenuController != null && textMenuController.visible)
            textMenuController.backToPreviousMenu(event);
        else if (pixelMenuController != null && pixelMenuController.visible)
            pixelMenuController.backToPreviousMenu(event);
        stopKeyListening();
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on close menu button");
    }

    @Override
    protected void startRegisteringAction(MouseEvent event) {
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
        stopKeyListening();
        setMenuMainGroupVisible(false);
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on start registering button");
        AppScene.addLog(LogLevel.DEBUG, className, "Reading type chosen: " + readingTypeChoice.getValue());
    }

    private void stopKeyListening() {
        GlobalScreen.removeNativeKeyListener(this);
        isKeyListening = false;
        AppScene.addLog(LogLevel.TRACE, className, "Key is listening: " + isKeyListening);
    }

    @Override
    public void loadMenu(ActivityController activityController) {

        this.conditionController = (ConditionController) activityController;
        boolean isControllerSet = conditionController.isSet();
        recheckContentVBox.setVisible(isControllerSet);
        updateRecheckResultLabel(false, null);
        if (isControllerSet) {
            mainImageView.setImage(SwingFXUtils.toFXImage(conditionController.getCondition().getMainDisplayImage(), null));
            readingTypeChoice.setValue(conditionController.getCondition().getChosenReadingCondition());
        }
        else
            mainImageView.setImage(null);
        if (!isKeyListening && isControllerSet) {
            GlobalScreen.addNativeKeyListener(this);
            isKeyListening = true;
        }
        recheckResultImageView.setImage(null);
        setMenuMainGroupVisible(true);
        AppScene.addLog(LogLevel.TRACE, className, "Loaded Menu");
        AppScene.addLog(LogLevel.TRACE, className, "Key is listening: " + isKeyListening);
        AppScene.addLog(LogLevel.TRACE, className, "Controller is set: " + isControllerSet);
    }

    // ------------------------------------------------------
    @Override
    protected void recheck() {
        AppScene.addLog(LogLevel.DEBUG, className, "Rechecking condition");
        if (!conditionController.isSet()) {
            AppScene.addLog(LogLevel.WARN, className, "Condition controller is not set");
            return;
        }
        try {
            Condition condition = conditionController.getCondition();
            ImageCheckResult checkedConditionResult = condition.checkCondition();
            //Platform.runLater(() -> updateRecheckResultLabel(checkedConditionResult.isPass(), condition.getChosenReadingCondition().name()));
            updateRecheckResultLabel(checkedConditionResult.isPass(), checkedConditionResult.getReadResult());
            recheckResultImageView.setImage(SwingFXUtils.toFXImage(checkedConditionResult.getDisplayImage(), null));
            AppScene.addLog(LogLevel.TRACE, className, "Condition recheck result: " + checkedConditionResult.getReadResult());
        } catch(Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail rechecking condition: " + e.getMessage());
        }
    }
    @Override
    protected void recheck(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on recheck button");
        recheck();
    }
    @Override
    protected void updateRecheckResultLabel(boolean pass, String resultText) {
        if (resultText == null)
            recheckResultLabel.setText("Result");
        else if (pass)
            recheckResultLabel.setText("Pass reading " + resultText);
        else
            recheckResultLabel.setText("Fail reading " + resultText);
    }
    public void nativeKeyReleased(NativeKeyEvent e) {
        int nativeKeyCode = e.getKeyCode();
        if (nativeKeyCode == NativeKeyEvent.VC_F2) {
            AppScene.addLog(LogLevel.INFO, className, "Rechecking condition");
            AppScene.addLog(LogLevel.DEBUG, className, "Clicked on F2 key to recheck");
            recheck();
        }
    }

    private ConditionTextMenuController textMenuController;
    private ConditionPixelMenuController pixelMenuController;
    private void loadReadingTextConditionMenu() { // called one or else redundant
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("conditionTextMenuPane.fxml"));
            Node textMenuPane = loader.load();
            textMenuController = loader.getController();
            mainMenuStackPane.getChildren().add(textMenuPane);
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading reading text condition menu pane: " + e.getMessage());
        }
    }
    private void loadReadingPixelConditionMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("conditionPixelMenuPane.fxml"));
            Node pixelMenuPane = loader.load();
            pixelMenuController = loader.getController();
            mainMenuStackPane.getChildren().add(pixelMenuPane);
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading reading pixel condition menu pane: " + e.getMessage());
        }
    }

    private void removeSelectedCondition(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on remove condition button");
        conditionController.removeThisConditionFromParent();
        closeMenuControllerAction(event);
    }
}
