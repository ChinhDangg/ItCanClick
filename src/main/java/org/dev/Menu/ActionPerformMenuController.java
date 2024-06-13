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
import org.dev.Task.Action.*;
import org.dev.Task.ActionController;
import org.dev.Task.ActivityController;
import java.awt.*;
import java.awt.event.ActionEvent;
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
        newAction.setActionOptions(progressiveSearchCheckBox.isSelected(), waitBeforeTime, waitAfterTime, actionTypes,
                currentMainImage, currentDisplayImage, mainImageBoundingBox, registeredKey);
        actionController.registerActionPerform(newAction, currentDisplayImage);
    }
    protected void backToPreviousMenu(MouseEvent event) {
        if (visible) {
            System.out.println("Backed");
            stopAllListeners();
            showMenu(false);
        }
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
        showMenu(true);
    }

    private final int timeStep = 500;
    // ------------------------------------------------------
    private void toggleProgressiveSearchCheckBox(javafx.event.ActionEvent event) {
        progressiveSearchButtonsPane.setVisible(progressiveSearchCheckBox.isSelected());
    }
    private int progressiveSearchTime = 1000;
    private void increaseProgressiveSearchTime(MouseEvent event) {
        progressiveSearchTime += timeStep;
        updateProgressiveSearchTimeLabel();
    }
    private void decreaseProgressiveSearchTime(MouseEvent event) {
        progressiveSearchTime = Math.max(progressiveSearchTime-timeStep, 0);
        updateProgressiveSearchTimeLabel();
    }
    private void updateProgressiveSearchTimeLabel() {
        progressiveSearchTimeLabel.setText(convertMilliToSecond(progressiveSearchTime)+"s");
    }
    private double convertMilliToSecond(int milli) {
        return (double) milli / 1000.0;
    }

    // ------------------------------------------------------
    private int waitBeforeTime = 1000;
    private void increaseWaitBeforeTime(MouseEvent event) {
        waitBeforeTime += timeStep;
        updateWaitBeforeTimeLabel();
    }
    private void decreaseWaitBeforeTime(MouseEvent event) {
        waitBeforeTime = Math.max(waitBeforeTime-timeStep, 0);
        updateWaitBeforeTimeLabel();
    }
    private void updateWaitBeforeTimeLabel() {
        waitBeforeTimeLabel.setText(convertMilliToSecond(waitBeforeTime) +"s");
    }

    // ------------------------------------------------------
    private int waitAfterTime = 1000;
    private void increaseWaitAfterTime(MouseEvent event) {
        waitAfterTime += timeStep;
        updateWaitAfterTime();
    }
    private void decreaseWaitAfterTime(MouseEvent event) {
        waitAfterTime = Math.max(waitAfterTime-timeStep, 0);
        updateWaitAfterTime();
    }
    private void updateWaitAfterTime() {
        waitAfterTimeLabel.setText(convertMilliToSecond(waitAfterTime) +"s");
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
            if (!p.equals(previousMousePoint)) {
                previousMousePoint = p;
                try {
                    displayMainImageView(getDisplayImage(p.x, p.y));
                } catch (Exception ex) {
                    System.out.println("Error at mose displaying captured image at pixel menu");
                }
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
            GlobalScreen.removeNativeKeyListener(this);
            registeredKeyLabelPane.setDisable(true);
        }
    }
    private int registeredKey = -1;
    public void nativeKeyReleased(NativeKeyEvent e) {
        System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
        if (keyIsListening) {
            int keyEvent = mapNativeKeyToKeyEvent(e.getKeyCode());
            if (keyEvent == -1) {
                System.out.println("Key is not supported, please try another key");
                return;
            }
            registeredKey = keyEvent;
            Platform.runLater(() -> registeredKeyLabel.setText(NativeKeyEvent.getKeyText(e.getKeyCode())));
        }
        if (e.getKeyCode() == NativeKeyEvent.VC_F2)
            startMouseMotionListening();
        else if (e.getKeyCode() == NativeKeyEvent.VC_F1)
            stopMouseMotionListening();
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
