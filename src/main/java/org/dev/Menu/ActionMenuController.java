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
import org.dev.App;
import org.dev.Enum.ActionTypes;
import org.dev.Operation.ActionController;
import org.dev.Operation.ActivityController;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ActionMenuController extends MenuController implements Initializable {
    @FXML
    private ChoiceBox<ActionTypes> actionTypeChoice;
    private ActionController actionController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
    }

    protected void loadTypeChoices() {
        System.out.println("Loading Action types");
        actionTypeChoice.getItems().addAll(ActionTypes.values());
        actionTypeChoice.setValue(ActionTypes.MouseClick);
    }
    protected void closeMenuController(MouseEvent event) {
        App.closeActionMenuPane();
        if (actionPerformMenuController != null && actionPerformMenuController.visible)
            actionPerformMenuController.backToPreviousMenu(event);
        GlobalScreen.removeNativeKeyListener(this);
        isKeyListening = false;
    }
    protected void startRegistering(MouseEvent event) {
        System.out.println("Click on start registering");
        if (actionPerformMenuController == null)
            loadActionPerformMenu();
        actionController.setChosenActionPerform(actionTypeChoice.getValue());
        actionPerformMenuController.loadMenu(actionController);
    }
    public void loadMenu(ActivityController activityController) {
        if (!isKeyListening) {
            isKeyListening = true;
            GlobalScreen.addNativeKeyListener(this);
        }
        this.actionController = (ActionController) activityController;
        boolean controllerSet = actionController.isSet();
        recheckPane.setVisible(controllerSet);
        recheckResultLabel.setText("");
        if (controllerSet) {
            mainImageView.setImage(SwingFXUtils.toFXImage(actionController.getAction().getDisplayImage(), null));
            actionTypeChoice.setValue(actionController.getAction().getChosenActionPerform());
        }
        else
            mainImageView.setImage(null);
    }

    // ------------------------------------------------------
    protected void recheck() {
        System.out.println("Rechecking action");
        if (!actionController.isSet()) {
            System.out.println("Action Controller is not set - bug");
            return;
        }
        try {
            System.out.println("Test performing action without conditions");
            Thread thread = new Thread(this::runRecheckAction);
            thread.start();
        } catch (Exception e) {
            System.out.println("Fail rechecking action");
        }
    }
    protected void recheck(MouseEvent event) { recheck(); }
    private void runRecheckAction() {
        try {
            System.out.println("Performing action in 3s");
            Thread.sleep(2700);
            actionController.getAction().performAction();
            System.out.println("Action performed");
            Platform.runLater(() -> recheckResultLabel.setText("Action finished performing"));
        } catch (InterruptedException e) {
            System.out.println("Fail run recheck action with sleep");
        }
    }
    public void nativeKeyReleased(NativeKeyEvent e) {
        int nativeKeyCode = e.getKeyCode();
        if (nativeKeyCode == NativeKeyEvent.VC_F2)
            recheck();
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
            System.out.println("Fail loading action perform menu pane");
        }
    }
}
