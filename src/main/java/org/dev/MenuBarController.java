package org.dev;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.Getter;
import org.dev.Operation.Data.OperationData;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuBarController implements Initializable {

    @FXML
    private MenuItem saveMenuItem, exitMenuItem;
    @FXML
    private MenuItem newOperationMenuItem, openSavedOperationMenuItem;
    @FXML
    private Group startRunGroupButton, stopRunGroupButton;
    @FXML
    private HBox operationRunningIndicationHBox;

    private Thread operationRunThread = null;
    @Getter
    private boolean operationIsRunning = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        saveMenuItem.setOnAction(this::save);
        exitMenuItem.setOnAction(this::exit);
        newOperationMenuItem.setOnAction(this::createNewOperation);
        openSavedOperationMenuItem.setOnAction(this::openSavedOperation);
        startRunGroupButton.setOnMouseClicked(this::runOperation);
        stopRunGroupButton.setOnMouseClicked(this::stopOperation);
        stopRunGroupButton.setVisible(false);
        operationRunningIndicationHBox.setVisible(false);
    }

    private final String savedRootPath = "SavedOp/";
    public void save(ActionEvent event) {
        try {
            System.out.println("CLicked on saved all");
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

    public void createNewOperation(ActionEvent event) {
        System.out.println("Opening new operation");
        try {
            AppScene.loadNewEmptyOperation();
            AppScene.loadSideMenuHierarchy();
        } catch (IOException e) {
            System.out.println("Fail loading empty operation pane at top menu bar");
        }
    }

    public void openSavedOperation(ActionEvent event) {
        System.out.println("Opening saved operation");
        AppScene.loadSavedOperation(savedRootPath + "SomeOperationName.ser");
        AppScene.loadSideMenuHierarchy();
    }

    public void runOperation(MouseEvent event) {
        System.out.println("Start running operation");
        if (AppScene.currentLoadedOperationController == null) {
            System.out.println("No operation found");
            return;
        }
        operationRunThread = new Thread(() -> {
            AppScene.currentLoadedOperationRunController.startOperation(AppScene.currentLoadedOperationController);
            setOperationIsRunning(false);
        });
        operationRunThread.start();
        setOperationIsRunning(true);
    }
    public void stopOperation(MouseEvent event) {
        System.out.println("Stopping operation");
        operationRunThread.interrupt();
        setOperationIsRunning(false);
    }
    private void setOperationIsRunning(boolean isRunning) {
        operationIsRunning = isRunning;
        AppScene.isOperationRunning = isRunning;
        stopRunGroupButton.setVisible(isRunning);
        operationRunningIndicationHBox.setVisible(isRunning);
    }
}
