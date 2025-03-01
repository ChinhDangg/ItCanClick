package org.dev.Menu;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import org.dev.AppScene;
import org.dev.Enum.ActionTypes;
import org.dev.Enum.LogLevel;
import org.dev.Job.Action.Action;
import org.dev.Job.Condition.Condition;
import org.dev.JobController.ActionController;
import org.dev.JobController.ActivityController;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;

public class ActionMenuController extends MenuController implements Initializable {
    @FXML
    private ChoiceBox<ActionTypes> readingTypeChoice;

    private ActionController actionController;

    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
    }

    protected void loadTypeChoices() {
        readingTypeChoice.getItems().addAll(ActionTypes.values());
        readingTypeChoice.setValue(ActionTypes.MouseClick);
        AppScene.addLog(LogLevel.TRACE, className, "Action types loaded");
    }

    @Override
    protected void closeMenuControllerAction(MouseEvent event) {
        AppScene.closeActionMenuPane();
        if (actionPerformMenuController != null && actionPerformMenuController.visible)
            actionPerformMenuController.backToPreviousMenu(event);
        stopKeyListening();
        AppScene.addLog(LogLevel.DEBUG, className, "Action menu closed");
    }

    @Override
    protected void startRegisteringAction(MouseEvent event) {
        if (actionPerformMenuController == null)
            loadActionPerformMenu();
        actionController.setChosenActionPerform(readingTypeChoice.getValue());
        actionPerformMenuController.loadMenu(actionController);
        setMenuMainGroupVisible(false);
        stopKeyListening();
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on start registering");
    }

    private void stopKeyListening() {
        GlobalScreen.removeNativeKeyListener(this);
        isKeyListening = false;
        AppScene.addLog(LogLevel.TRACE, className, "Key is listening: " + isKeyListening);
    }

    @Override
    public void loadMenu(ActivityController activityController) {
        this.actionController = (ActionController) activityController;
        boolean isControllerSet = actionController.isSet();
        recheckContentVBox.setVisible(isControllerSet);
        recheckResultLabel.setText("");
        if (isControllerSet) {
            Action action = actionController.getAction();
            mainImageView.setImage(SwingFXUtils.toFXImage(Condition.getImageWithEdges(
                    action.getMainImageBoundingBox(), action.getDisplayImage(), 0.5f), null));
            readingTypeChoice.setValue(actionController.getAction().getChosenActionPerform());
        }
        else
            mainImageView.setImage(null);
        if (!isKeyListening && isControllerSet) {
            GlobalScreen.addNativeKeyListener(this);
            isKeyListening = true;
        }
        recheckResultImageView.setImage(null);
        setMenuMainGroupVisible(true);
        AppScene.addLog(LogLevel.DEBUG, className, "Loaded action menu");
        AppScene.addLog(LogLevel.TRACE, className, "Key is listening: " + isKeyListening);
        AppScene.addLog(LogLevel.TRACE, className, "Current action is set: " + isControllerSet);
    }

    // ------------------------------------------------------
    @Override
    protected void recheck() {
        AppScene.addLog(LogLevel.DEBUG, className, "Rechecking action");
        if (!actionController.isSet()) {
            AppScene.addLog(LogLevel.WARN, className, "Action controller is not set");
            return;
        }
        try {
            AppScene.addLog(LogLevel.INFO, className, "Test performing action without conditions");
            Thread thread = new Thread(this::runRecheckActionThread);
            thread.start();
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail rechecking action: " + e.getMessage());
        }
    }
    @Override
    protected void recheck(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on recheck button");
        recheck();
    }
    @Override
    protected void updateRecheckResultLabel(boolean pass, String resultText) {
        recheckResultLabel.setText(resultText);
    }
    private void runRecheckActionThread() {
        try {
            AppScene.addLog(LogLevel.INFO, className, "Performing action in 3s");
            Thread.sleep(3000);
            Action action = actionController.getAction();
            BufferedImage seenImage = action.getSeenImage();
            action.performAction();
            AppScene.addLog(LogLevel.INFO, className, "Action performed");
            recheckResultImageView.setImage(SwingFXUtils.toFXImage(seenImage, null));
            Platform.runLater(() -> updateRecheckResultLabel(false, "Action finished performing"));
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail run recheck action with sleep: " + e.getMessage());
        }
    }
    public void nativeKeyReleased(NativeKeyEvent e) {
        int nativeKeyCode = e.getKeyCode();
        if (nativeKeyCode == NativeKeyEvent.VC_F2) {
            AppScene.addLog(LogLevel.INFO, className, "Rechecking action");
            AppScene.addLog(LogLevel.DEBUG, className, "Clicked on F2 key to recheck");
            recheck();
        }
    }

    // ------------------------------------------------------
    private ActionPerformMenuController actionPerformMenuController;
    private void loadActionPerformMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("actionPerformMenuPane.fxml"));
            Node actionPerformMenuPane = loader.load();
            actionPerformMenuController = loader.getController();
            mainMenuStackPane.getChildren().add(actionPerformMenuPane);
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error loading action perform menu pane: " + e.getMessage());
        }
    }
}
