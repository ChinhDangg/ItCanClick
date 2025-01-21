package org.dev.Menu;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.dev.AppScene;
import org.dev.Enum.ActionTypes;
import org.dev.Enum.LogLevel;
import org.dev.Job.Action.*;
import org.dev.Job.Condition.Condition;
import org.dev.JobController.ActionController;
import org.dev.JobController.ActivityController;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;

public class ActionPerformMenuController extends OptionsMenuController {
    @FXML
    private Group parentGroup;
    @FXML
    private Label actionPerformIndicationLabel;
    @FXML
    private CheckBox progressiveSearchCheckBox;
    @FXML
    private Node registeredKeyPane, registeredKeyLabelPane, progressiveSearchButtonsPane, progressiveSearchMinusButton, progressiveSearchPlusButton;
    @FXML
    private Node waitBeforeMinusButton, waitBeforePlusButton, waitAfterMinusButton, waitAfterPlusButton;
    @FXML
    private Label registeredKeyLabel, progressiveSearchTimeLabel, waitBeforeTimeLabel, waitAfterTimeLabel;
    @FXML
    private Label attemptNumberLabel;
    @FXML
    private Node attemptMinusButton, attemptPlusButton;
    @FXML
    private StackPane startRegisterKeyButton;

    private ActionController actionController;
    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        startRegisterKeyButton.setOnMouseClicked(this::startRegisteringKey);
        attemptMinusButton.setOnMouseClicked(this::decreaseNumberOfAttempt);
        attemptPlusButton.setOnMouseClicked(this::increaseNumberOfAttempt);
        progressiveSearchCheckBox.setOnAction(this::toggleProgressiveSearchCheckBox);
        progressiveSearchMinusButton.setOnMouseClicked(this::decreaseProgressiveSearchTime);
        progressiveSearchPlusButton.setOnMouseClicked(this::increaseProgressiveSearchTime);
        waitBeforeMinusButton.setOnMouseClicked(this::decreaseWaitBeforeTime);
        waitBeforePlusButton.setOnMouseClicked(this::increaseWaitBeforeTime);
        waitAfterMinusButton.setOnMouseClicked(this::decreaseWaitAfterTime);
        waitAfterPlusButton.setOnMouseClicked(this::increaseWaitAfterTime);
    }

    @Override
    protected void save(MouseEvent event) {
        if (actionController == null) {
            AppScene.addLog(LogLevel.WARN, className, "save - Action controller is not set");
            return;
        }
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on save button");
        if (currentDisplayImage == null) {
            AppScene.addLog(LogLevel.INFO, className, "Please save an image for the action for future reference");
            return;
        }
        ActionTypes actionTypes = actionController.getChosenActionPerform();
        if (actionTypes.isKeyAction() && registeredKey == -1) {
            AppScene.addLog(LogLevel.INFO, className, "A key has not been registered for key action");
            return;
        }
        Action newAction = Action.getCorrespondAction(actionTypes);
        newAction.setActionOptions(attempt, progressiveSearchCheckBox.isSelected(), progressiveSearchTime, waitBeforeTime,
                waitAfterTime, actionTypes, currentDisplayImage, mainImageBoundingBox, registeredKey);
        actionController.registerActionPerform(newAction);
        AppScene.addLog(LogLevel.INFO, className, "Saved registered action");
    }

    @Override
    protected void backToPreviousMenu(MouseEvent event) {
        if (visible) {
            stopAllListeners();
            showMenu(false);
            AppScene.actionMenuController.loadMenu(actionController);
            AppScene.addLog(LogLevel.DEBUG, className, "Backed to action menu");
        }
    }

    @Override
    protected void resetMenu() {
        super.resetMenu();
        progressiveSearchCheckBox.setSelected(false);
        updateProgressiveSearchTimeLabel(1000);
        updateWaitBeforeTimeLabel(1000);
        updateWaitAfterTimeLabel(1000);
        updateAttemptLabel(1);
        AppScene.addLog(LogLevel.TRACE, className, "Menu is reset");
    }

    @Override
    protected void showMenu(boolean show) {
        parentGroup.setVisible(show);
        visible = show;
    }

    @Override
    protected void loadMenu(ActivityController activityController) {
        if (activityController == null) {
            AppScene.addLog(LogLevel.WARN, className, "loadMenu - Action is not set");
            return;
        }
        GlobalScreen.addNativeKeyListener(this);
        actionController = (ActionController) activityController;
        ActionTypes actionTypes = actionController.getChosenActionPerform();
        actionPerformIndicationLabel.setText(actionTypes.name());
        registeredKeyPane.setVisible(actionTypes.isKeyAction());
        if (!loadPresetAction())
            resetMenu();
        showMenu(true);
        AppScene.addLog(LogLevel.TRACE, className, "Menu is loaded");
    }
    private boolean loadPresetAction() {
        if (!actionController.isSet())
            return false;
        Action action = actionController.getAction();
        ActionTypes actionTypes = actionController.getChosenActionPerform();
        if (action.getChosenActionPerform() != actionTypes)
            return false;
        if (actionTypes.isKeyAction())
            updateRegisteredKeyLabel(action.getKeyCode());
        updateAttemptLabel(action.getAttempt());
        progressiveSearchCheckBox.setSelected(action.isProgressiveSearch());
        updateProgressiveSearchTimeLabel(action.getProgressiveSearchTime());
        updateWaitBeforeTimeLabel(action.getWaitBeforeTime());
        updateWaitAfterTimeLabel(action.getWaitAfterTime());
        currentDisplayImage = action.getDisplayImage();
        displayMainImageView(Condition.getImageWithEdges(mainImageBoundingBox, currentDisplayImage, 0.5f));
        mainImageBoundingBox = action.getMainImageBoundingBox();
        AppScene.addLog(LogLevel.TRACE, className, "Preset action is loaded");
        return true;
    }

    // ------------------------------------------------------
    private int attempt = 1;
    private void increaseNumberOfAttempt(MouseEvent event) {
        updateAttemptLabel(Math.min(attempt + 1, 10));
    }
    private void decreaseNumberOfAttempt(MouseEvent event) {
        updateAttemptLabel(Math.max(attempt - 1, 1));
    }
    private void updateAttemptLabel(int newAttempt) {
        attempt = newAttempt;
        attemptNumberLabel.setText(Integer.toString(attempt));
        AppScene.addLog(LogLevel.TRACE, className, "Updated action attempt: " + newAttempt);
    }

    private final int timeStep = 500;
    // ------------------------------------------------------
    private void toggleProgressiveSearchCheckBox(javafx.event.ActionEvent event) {
        progressiveSearchButtonsPane.setVisible(progressiveSearchCheckBox.isSelected());
        AppScene.addLog(LogLevel.TRACE, className, "Progressive Search Checkbox is selected: " + progressiveSearchCheckBox.isSelected());
        AppScene.addLog(LogLevel.TRACE, className, "Progressive Search Button visible: " + progressiveSearchButtonsPane.isVisible());
    }
    private int progressiveSearchTime = 1000;
    private void increaseProgressiveSearchTime(MouseEvent event) {
        updateProgressiveSearchTimeLabel(progressiveSearchTime + timeStep);
    }
    private void decreaseProgressiveSearchTime(MouseEvent event) {
        updateProgressiveSearchTimeLabel(Math.max(progressiveSearchTime - timeStep, 0));
    }
    private void updateProgressiveSearchTimeLabel(int newProgressiveTime) {
        progressiveSearchTime = newProgressiveTime;
        progressiveSearchTimeLabel.setText(convertMilliToSecond(progressiveSearchTime) + "s");
        AppScene.addLog(LogLevel.TRACE, className, "Updated progressive Search Time: " + newProgressiveTime);
    }
    private double convertMilliToSecond(int milli) { return (double) milli / 1000.0; }

    // ------------------------------------------------------
    private int waitBeforeTime = 1000;
    private void increaseWaitBeforeTime(MouseEvent event) {
        updateWaitBeforeTimeLabel(waitBeforeTime + timeStep);
    }
    private void decreaseWaitBeforeTime(MouseEvent event) {
        updateWaitBeforeTimeLabel(Math.max(waitBeforeTime-timeStep, 0));
    }
    private void updateWaitBeforeTimeLabel(int newWaitBeforeTime) {
        waitBeforeTime = newWaitBeforeTime;
        waitBeforeTimeLabel.setText(convertMilliToSecond(waitBeforeTime) + "s");
        AppScene.addLog(LogLevel.TRACE, className, "Updated Wait Before Time: " + newWaitBeforeTime);
    }

    // ------------------------------------------------------
    private int waitAfterTime = 1000;
    private void increaseWaitAfterTime(MouseEvent event) {
        updateWaitAfterTimeLabel(waitAfterTime + timeStep);
    }
    private void decreaseWaitAfterTime(MouseEvent event) {
        updateWaitAfterTimeLabel(Math.max(waitAfterTime-timeStep, 0));
    }
    private void updateWaitAfterTimeLabel(int newWaitAfterTime) {
        waitAfterTime = newWaitAfterTime;
        waitAfterTimeLabel.setText(convertMilliToSecond(waitAfterTime) + "s");
        AppScene.addLog(LogLevel.TRACE, className, "Updated Wait After Time: " + newWaitAfterTime);
    }

    // ------------------------------------------------------
    private BufferedImage getDisplayImage(int x, int y) throws AWTException {
        mainImageBoundingBox = new Rectangle(x, y, imageWidth, imageHeight);
        Rectangle fullBounds = new Rectangle(x-outsideBoxWidth, y-outsideBoxWidth,
                imageWidth+outsideBoxWidth*2, imageHeight+outsideBoxWidth*2);
        currentDisplayImage = captureCurrentScreen(fullBounds);
        BufferedImage imageWithEdges = Condition.getImageWithEdges(mainImageBoundingBox, currentDisplayImage, 0.5f);
        BufferedImage zoomedImage = getZoomedImage(imageWithEdges);
        if (zoomedImage != null)
            return zoomedImage;
        return imageWithEdges;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == mouseTimer) {
            Point p = MouseInfo.getPointerInfo().getLocation();
            if (p.equals(previousMousePoint))
                return;
            previousMousePoint = p;
            try {
                displayMainImageView(getDisplayImage(p.x, p.y));
            } catch (Exception ex) {
                AppScene.addLog(LogLevel.ERROR, className, "Error at displaying captured image at mouse pointer: " + ex.getMessage());
            }
        }
    }

    // ------------------------------------------------------
    private boolean keyIsListening = false;
    private void startRegisteringKey(MouseEvent event) {
        keyIsListening = true;
        GlobalScreen.addNativeKeyListener(this);
        registeredKeyLabelPane.setDisable(false);
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on start registering key button");
    }

    @Override
    protected void stopMouseMotion(MouseEvent event) {
        super.stopMouseMotion(event);
        if (keyIsListening) {
            GlobalScreen.removeNativeKeyListener(this);
            registeredKeyLabelPane.setDisable(true);
            keyIsListening = false;
        }
    }

    private int registeredKey = -1;
    public void nativeKeyReleased(NativeKeyEvent e) {
        AppScene.addLog(LogLevel.TRACE, className, "Key pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        int nativeKeyCode = e.getKeyCode();
        if (keyIsListening) {
            int keyEvent = mapNativeKeyToKeyEvent(nativeKeyCode);
            if (keyEvent == -1) {
                AppScene.addLog(LogLevel.WARN, className, "Key is not supported, please try another key");
                return;
            }
            updateRegisteredKeyLabel(keyEvent);
        }
        if (nativeKeyCode == NativeKeyEvent.VC_F2) {
            AppScene.addLog(LogLevel.INFO, className, "Starting mouse listening");
            AppScene.addLog(LogLevel.DEBUG, className, "Clicked on F2 key to start mouse listening");
            startMouseMotionListening();
        }
        else if (nativeKeyCode == NativeKeyEvent.VC_F1) {
            AppScene.addLog(LogLevel.INFO, className, "Stopping mouse listening");
            AppScene.addLog(LogLevel.DEBUG, className, "Clicked on F2 key to start mouse listening");
            stopMouseMotionListening();
        }
    }
    private void updateRegisteredKeyLabel(int keyEvent) {
        registeredKey = keyEvent;
        String keyText = KeyEvent.getKeyText(registeredKey);
        Platform.runLater(() -> registeredKeyLabel.setText(keyText));
        AppScene.addLog(LogLevel.TRACE, className, "Updated registered key: " + keyText);
    }
    private int mapNativeKeyToKeyEvent(int nativeKey) {
        if (nativeKey > 1 && nativeKey < 12) // 0-9
            return NativeKeyEvent.getKeyText(nativeKey).charAt(0);
        else if ((nativeKey > 15 && nativeKey < 26) || (nativeKey > 29 && nativeKey < 39) ||
                (nativeKey > 43 && nativeKey < 51)) // A-Z
            return NativeKeyEvent.getKeyText(nativeKey).charAt(0);
        return -1;
    }
}
