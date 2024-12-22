package org.dev;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.dev.Operation.Data.OperationData;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuBarController implements Initializable {

    @FXML
    private MenuItem saveMenuItem, exitMenuItem;
    @FXML
    private MenuItem newOperationMenuItem, openSavedOperationMenuItem;
    @FXML
    private StackPane startRunStackPaneButton, stopRunStackPaneButton;
    @FXML
    private HBox operationRunningIndicationHBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        saveMenuItem.setOnAction(this::save);
        exitMenuItem.setOnAction(this::exit);
        newOperationMenuItem.setOnAction(this::createNewOperationEvent);
        openSavedOperationMenuItem.setOnAction(this::openSavedOperationEvent);
        startRunStackPaneButton.setOnMouseClicked(this::startOperationRunEvent);
        stopRunStackPaneButton.setOnMouseClicked(this::stopOperationRunEvent);
        stopRunStackPaneButton.setVisible(false);
        operationRunningIndicationHBox.setVisible(false);
    }

    private final String savedRootPath = "SavedOp/";
    public void save(ActionEvent event) {
        try {
            System.out.println("Clicked on saved all");
            if (AppScene.currentLoadedOperationController == null)
                return;
            OperationData operationData = AppScene.currentLoadedOperationController.getOperationData();
            FileOutputStream fileOut = new FileOutputStream(savedRootPath + operationData.getOperation().getOperationName()+".ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(operationData);
            System.out.println("Saved");
        } catch (Exception e) {
            System.out.println("Fail saving all operation data");
        }
    }
    public void exit(ActionEvent event) {
        System.out.println("Exit all");
    }

    public void createNewOperationEvent(ActionEvent event) {
        System.out.println("Opening new operation");
        AppScene.loadEmptyOperation();
        AppScene.updateOperationSideMenuHierarchy();
    }

    public void openSavedOperationEvent(ActionEvent event) {
        System.out.println("Opening saved operation");
        AppScene.loadSavedOperation(savedRootPath + "Operation Name.ser");
        AppScene.updateOperationSideMenuHierarchy();
    }

    public void startOperationRunEvent(MouseEvent event) {
        boolean loadedOperationRun = AppScene.loadAndDisplayOperationRun();
        if (!loadedOperationRun)
            return;
        AppScene.updateOperationRunSideMenuHierarchy();
        AppScene.startOperationRun();
        setOperationRunning(true);
    }

    public void stopOperationRunEvent(MouseEvent event) {
        AppScene.stopOperationRun();
        setOperationRunning(false);
    }

    public void setOperationRunning(boolean isRunning) {
        stopRunStackPaneButton.setVisible(isRunning);
        operationRunningIndicationHBox.setVisible(isRunning);
    }
}
