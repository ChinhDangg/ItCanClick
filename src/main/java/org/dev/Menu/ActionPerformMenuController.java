package org.dev.Menu;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.dev.Task.ActionController;
import org.dev.Task.TaskController;
import java.awt.event.ActionEvent;

public class ActionPerformMenuController extends OptionsMenuController {
    @FXML
    private Pane actionPerformMenu;
    @FXML
    private Label actionPerformIndicationLabel;
    @FXML
    private CheckBox progressiveSearchCheckBox;
    @FXML
    private Pane registeredKeyPane, progressiveSearchButtonsPane, progressiveSearchMinusButton, progressiveSearchPlusButton;
    @FXML
    private Pane waitBeforeMinusButton, waitBeforePlusButton, waitAfterMinusButton, waitAfterPlusButton;
    @FXML
    private Label registeredKeyLabel, progressiveSearchTimeLabel, waitBeforeTimeLabel, waitAfterTimeLabel;
    private ActionController actionController;

    protected void save(MouseEvent event) {

    }
    protected void backToPreviousMenu(MouseEvent event) {

    }
    protected void loadMenu(TaskController taskController) {
        if (taskController == null) {
            System.out.println("Action controller is not set - bug");
            return;
        }
        actionController = (ActionController) taskController;
    }



    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
