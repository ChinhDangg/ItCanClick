package org.dev.Menu;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.dev.AppScene;
import org.dev.Enum.ActionTypes;
import org.dev.Enum.LogLevel;
import org.dev.Operation.ActionController;
import org.dev.Operation.ActivityController;
import java.io.IOException;
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
        GlobalScreen.removeNativeKeyListener(this);
        isKeyListening = false;
        AppScene.addLog(LogLevel.DEBUG, className, "Action menu closed");
        AppScene.addLog(LogLevel.TRACE, className, "Key is listening: " + isKeyListening);
    }

    @Override
    protected void startRegisteringAction(MouseEvent event) {
        if (actionPerformMenuController == null)
            loadActionPerformMenu();
        actionController.setChosenActionPerform(readingTypeChoice.getValue());
        actionPerformMenuController.loadMenu(actionController);
        GlobalScreen.removeNativeKeyListener(this);
        isKeyListening = false;
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on start registering");
    }

    @Override
    public void loadMenu(ActivityController activityController) {
        if (!isKeyListening) {
            isKeyListening = true;
            GlobalScreen.addNativeKeyListener(this);
        }
        this.actionController = (ActionController) activityController;
        boolean controllerSet = actionController.isSet();
        recheckContentVBox.setVisible(controllerSet);
        recheckResultLabel.setText("");
        if (controllerSet) {
            mainImageView.setImage(SwingFXUtils.toFXImage(actionController.getAction().getDisplayImage(), null));
            readingTypeChoice.setValue(actionController.getAction().getChosenActionPerform());
        }
        else
            mainImageView.setImage(null);
        AppScene.addLog(LogLevel.DEBUG, className, "Loaded action menu");
        AppScene.addLog(LogLevel.TRACE, className, "Key is listening: " + isKeyListening);
        AppScene.addLog(LogLevel.TRACE, className, "Current action is set: " + controllerSet);
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
            Thread thread = new Thread(this::runRecheckAction);
            thread.start();
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail rechecking action");
        }
    }
    @Override
    protected void recheck(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on recheck button");
        recheck();
    }
    private void runRecheckAction() {
        try {
            AppScene.addLog(LogLevel.INFO, className, "Performing action in 3s");
            Thread.sleep(3000);
            actionController.getAction().performAction();
            AppScene.addLog(LogLevel.INFO, className, "Action performed");
            Platform.runLater(() -> recheckResultLabel.setText("Action finished performing"));
        } catch (InterruptedException e) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail run recheck action with sleep");
        }
    }
    public void nativeKeyReleased(NativeKeyEvent e) {
        int nativeKeyCode = e.getKeyCode();
        if (nativeKeyCode == NativeKeyEvent.VC_F2) {
            AppScene.addLog(LogLevel.DEBUG, className, "Clicked on F2 key to recheck");
            recheck();
        }
    }

    // ------------------------------------------------------
    private ActionPerformMenuController actionPerformMenuController;
    private void loadActionPerformMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("actionPerformMenuPane.fxml"));
            Pane actionPerformMenuPane = loader.load();
            actionPerformMenuController = loader.getController();
            mainMenuStackPane.getChildren().add(actionPerformMenuPane);
        } catch (IOException e) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail loading action perform menu pane");
        }
    }
}
