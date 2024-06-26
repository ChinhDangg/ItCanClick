package org.dev.Menu;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.dev.Enum.ActionTypes;
import org.dev.Operation.Action.*;
import org.dev.Operation.ActionController;
import org.dev.Operation.ActivityController;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;

public class ActionPerformMenuController extends OptionsMenuController {
    @FXML
    private Pane actionPerformMenu;
    @FXML
    private Label actionPerformIndicationLabel;
    @FXML
    private CheckBox progressiveSearchCheckBox;
    @FXML
    private Pane registeredKeyPane, registeredKeyLabelPane, progressiveSearchButtonsPane, progressiveSearchMinusButton, progressiveSearchPlusButton;
    @FXML
    private Pane waitBeforeMinusButton, waitBeforePlusButton, waitAfterMinusButton, waitAfterPlusButton;
    @FXML
    private Label registeredKeyLabel, progressiveSearchTimeLabel, waitBeforeTimeLabel, waitAfterTimeLabel;
    private ActionController actionController;

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

    protected void save(MouseEvent event) {
        if (actionController == null) {
            System.out.println("Action controller is not set - bug");
            return;
        }
        System.out.println("Clicked on saved button");
        if (currentMainImage == null) {
            System.out.println("Please save an image for the action for future reference");
            return;
        }
        ActionTypes actionTypes = actionController.getChosenActionPerform();
        if (actionTypes != ActionTypes.MouseClick)
            if (registeredKey == -1) {
                System.out.println("A key have not been registered");
                return;
            }
        Action newAction;
        switch(actionTypes) {
            case MouseClick:
                newAction = new ActionMouseClick();
                break;
            case KeyClick:
                newAction = new ActionKeyClick();
                break;
            case KeyPress:
                newAction = new ActionKeyPress();
                break;
            case KeyPressMouseClick:
                newAction = new ActionKeyPressMouseClick();
                break;
            default:
                System.out.println("Fail saving action");
                return;
        }
        newAction.setActionOptions(attempt, progressiveSearchCheckBox.isSelected(), progressiveSearchTime, waitBeforeTime,
                waitAfterTime, actionTypes, currentMainImage, currentDisplayImage, mainImageBoundingBox, registeredKey);
        actionController.registerActionPerform(newAction, currentDisplayImage);
    }
    protected void backToPreviousMenu(MouseEvent event) {
        if (visible) {
            System.out.println("Backed");
            stopAllListeners();
            showMenu(false);
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
    }
    protected void showMenu(boolean show) {
        actionPerformMenu.setVisible(show);
        visible = show;
    }
    protected void loadMenu(ActivityController activityController) {
        if (activityController == null) {
            System.out.println("Action controller is not set - bug");
            return;
        }
        GlobalScreen.addNativeKeyListener(this);
        actionController = (ActionController) activityController;
        ActionTypes actionTypes = actionController.getChosenActionPerform();
        actionPerformIndicationLabel.setText(actionTypes.name());
        registeredKeyPane.setVisible(actionTypes != ActionTypes.MouseClick);
        if (!loadPresetAction())
            resetMenu();
        showMenu(true);
    }
    private boolean loadPresetAction() {
        if (!actionController.isSet())
            return false;
        Action action = actionController.getAction();
        ActionTypes actionTypes = actionController.getChosenActionPerform();
        if (action.getChosenActionPerform() != actionTypes)
            return false;
        if (actionTypes != ActionTypes.MouseClick)
            updateRegisteredKeyLabel(action.getKeyCode());
        updateAttemptLabel(action.getAttempt());
        progressiveSearchCheckBox.setSelected(action.isProgressiveSearch());
        updateProgressiveSearchTimeLabel(action.getProgressiveSearchTime());
        updateWaitBeforeTimeLabel(action.getWaitBeforeTime());
        updateWaitAfterTimeLabel(action.getWaitAfterTime());
        currentDisplayImage = action.getDisplayImage();
        displayMainImageView(currentDisplayImage);
        currentMainImage = action.getMainImage();
        mainImageBoundingBox = action.getMainImageBoundingBox();
        return true;
    }

    // ------------------------------------------------------
    private int attempt = 1;
    @FXML
    private Label attemptNumberLabel;
    @FXML
    private Pane attemptMinusButton, attemptPlusButton;
    private void increaseNumberOfAttempt(MouseEvent event) {
        updateAttemptLabel(Math.min(attempt + 1, 10));
    }
    private void decreaseNumberOfAttempt(MouseEvent event) {
        updateAttemptLabel(Math.max(attempt - 1, 1));
    }
    private void updateAttemptLabel(int newAttempt) {
        attempt = newAttempt;
        attemptNumberLabel.setText(Integer.toString(attempt));
    }

    private final int timeStep = 500;
    // ------------------------------------------------------
    private void toggleProgressiveSearchCheckBox(javafx.event.ActionEvent event) {
        progressiveSearchButtonsPane.setVisible(progressiveSearchCheckBox.isSelected());
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
        progressiveSearchTimeLabel.setText(STR."\{convertMilliToSecond(progressiveSearchTime)}s");
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
        waitBeforeTimeLabel.setText(STR."\{convertMilliToSecond(waitBeforeTime)}s");
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
        waitAfterTimeLabel.setText(STR."\{convertMilliToSecond(waitAfterTime)}s");
    }

    // ------------------------------------------------------
    private BufferedImage getDisplayImage(int x, int y) throws AWTException {
        mainImageBoundingBox = new Rectangle(x, y, imageWidth, imageHeight);
        currentMainImage = captureCurrentScreen(mainImageBoundingBox);
        BufferedImage imageWithEdges = getImageWithEdges(currentMainImage, x, y, 0.5f);
        currentDisplayImage = (imageWithEdges == null) ? currentMainImage : imageWithEdges;
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
                System.out.println("Error at mose displaying captured image at pixel menu");
            }
        }
    }

    // ------------------------------------------------------
    @FXML
    private StackPane startRegisterKeyButton;
    private boolean keyIsListening = false;
    private void startRegisteringKey(MouseEvent event) {
        keyIsListening = true;
        registeredKeyLabelPane.setDisable(false);
    }
    @Override
    protected void stopMouseMotion(MouseEvent event) {
        stopMouseMotionListening();
        if (keyIsListening) {
            registeredKeyLabelPane.setDisable(true);
            keyIsListening = false;
        }
    }
    private int registeredKey = -1;
    public void nativeKeyReleased(NativeKeyEvent e) {
        System.out.println(STR."Key Pressed: \{NativeKeyEvent.getKeyText(e.getKeyCode())}");
        int nativeKeyCode = e.getKeyCode();
        if (keyIsListening) {
            int keyEvent = mapNativeKeyToKeyEvent(nativeKeyCode);
            if (keyEvent == -1) {
                System.out.println("Key is not supported, please try another key");
                return;
            }
            updateRegisteredKeyLabel(keyEvent);
        }
        if (e.getKeyCode() == NativeKeyEvent.VC_F2)
            startMouseMotionListening();
        else if (e.getKeyCode() == NativeKeyEvent.VC_F1)
            stopMouseMotionListening();
    }
    private void updateRegisteredKeyLabel(int keyEvent) {
        registeredKey = keyEvent;
        Platform.runLater(() -> registeredKeyLabel.setText(KeyEvent.getKeyText(registeredKey)));
    }
    private int mapNativeKeyToKeyEvent(int nativeKey) {
        if (nativeKey > 1 && nativeKey < 12) // 0-9
            return NativeKeyEvent.getKeyText(nativeKey).charAt(0);
        else if ((nativeKey > 15 && nativeKey < 26) || (nativeKey > 29 && nativeKey < 39) ||
                (nativeKey > 43 && nativeKey < 51)) // a - z
            return NativeKeyEvent.getKeyText(nativeKey).toLowerCase().charAt(0);
        return -1;
    }
}
