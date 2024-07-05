package org.dev.Menu;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Platform;
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
import org.dev.Enum.ReadingCondition;
import org.dev.Operation.ActivityController;
import org.dev.Operation.Condition.Condition;
import org.dev.Operation.ConditionController;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ConditionMenuController extends MenuController implements Initializable {
    @FXML
    private ChoiceBox<ReadingCondition> readingTypeChoice;
    @FXML
    private StackPane removeButton;
    private ConditionController conditionController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        removeButton.setOnMouseClicked(this::removeSelectedCondition);
    }

    protected void loadTypeChoices() {
        System.out.println("Loading Condition reading type");
        readingTypeChoice.getItems().addAll(ReadingCondition.values());
        readingTypeChoice.setValue(ReadingCondition.Text);
    }
    protected void closeMenuController(MouseEvent event) {
        App.closeConditionMenuPane();
        if (textMenuController != null && textMenuController.visible)
            textMenuController.backToPreviousMenu(event);
        else if (pixelMenuController != null && pixelMenuController.visible)
            pixelMenuController.backToPreviousMenu(event);
        GlobalScreen.removeNativeKeyListener(this);
        isKeyListening = false;
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
        if (!isKeyListening) {
            isKeyListening = true;
            GlobalScreen.addNativeKeyListener(this);
        }
        this.conditionController = (ConditionController) activityController;
        boolean controllerSet = conditionController.isSet();
        recheckPane.setVisible(controllerSet);
        updateRecheckResultLabel(false, null);
        if (controllerSet)
            mainImageView.setImage(SwingFXUtils.toFXImage(conditionController.getCondition().getMainDisplayImage(), null));
        else
            mainImageView.setImage(null);
    }

    // ------------------------------------------------------
    protected void recheck() {
        System.out.println("Rechecking condition");
        if (!conditionController.isSet()) {
            System.out.println("Condition Controller is not set - bug");
            return;
        }
        try {
            Condition condition = conditionController.getCondition();
            boolean checkedCondition = condition.checkCondition();
            Platform.runLater(() -> updateRecheckResultLabel(checkedCondition, condition.getChosenReadingCondition().name()));
        } catch(Exception e) {
            System.out.println("Fail rechecking condition");
        }
    }
    protected void recheck(MouseEvent event) { recheck(); }
    private void updateRecheckResultLabel(boolean pass, String newReadText) {
        if (newReadText == null)
            recheckResultLabel.setText("Result");
        else if (pass)
            recheckResultLabel.setText("Pass reading " + newReadText);
        else
            recheckResultLabel.setText("Fail reading " + newReadText);
    }
    public void nativeKeyReleased(NativeKeyEvent e) {
        int nativeKeyCode = e.getKeyCode();
        if (nativeKeyCode == NativeKeyEvent.VC_F2)
            recheck();
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

    private void removeSelectedCondition(MouseEvent event) {
        conditionController.removeThisConditionFromParent();
        closeMenuController(event);
        System.out.println("Removed selected condition from action");
    }
}
