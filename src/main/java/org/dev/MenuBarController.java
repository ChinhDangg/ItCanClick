package org.dev;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        saveMenuItem.setOnAction(this::save);
        exitMenuItem.setOnAction(this::exit);
        newOperationMenuItem.setOnAction(this::createNewOperation);
        openSavedOperationMenuItem.setOnAction(this::openSavedOperation);
    }

    public void save(ActionEvent event) {
        try {
            System.out.println("Saved all");
            if (App.currentLoadedOperationController == null)
                return;
            OperationData operationData = App.currentLoadedOperationController.getOperationData();
            FileOutputStream fileOut = new FileOutputStream(operationData.getOperation().getOperationName()+".ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(operationData);
        } catch (Exception e) {
            System.out.println("Fail saving all operation data");
        }
    }
    public void exit(ActionEvent event) {
        System.out.println("Exit all");
    }

    public void createNewOperation(ActionEvent event) {
        System.out.println("New operation opened");
    }

    public void openSavedOperation(ActionEvent event) {
        System.out.println("Opening saved operation");
        App.loadSavedOperation("SomeOperationName.ser");
    }
}
