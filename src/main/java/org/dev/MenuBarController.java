package org.dev;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.dev.Enum.LogLevel;
import org.dev.Operation.Data.OperationData;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class MenuBarController implements Initializable {

    @FXML
    private HBox topMenuBarMainHBox;
    @FXML
    private MenuItem saveMenuItem, exitMenuItem;
    @FXML
    private MenuItem newOperationMenuItem, openSavedOperationMenuItem;
    @FXML
    private StackPane startRunStackPaneButton, stopRunStackPaneButton;
    @FXML
    private HBox operationRunningIndicationHBox;

    private final String className = this.getClass().getSimpleName();
    private Path savedDirectory;

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

    public void save(ActionEvent event) {
        try {
            AppScene.addLog(LogLevel.DEBUG, className, "Clicked on save all");
            if (AppScene.currentLoadedOperationController == null)
                return;
            OperationData operationData = AppScene.currentLoadedOperationController.getOperationData();
            String fileName = operationData.getOperation().getOperationName() + ".ser";
            Path savedPath = getSavePath(fileName);
            AppScene.addLog(LogLevel.DEBUG, className, "Saving path: " + savedPath);
            if (savedPath == null)
                return;
            FileOutputStream fileOut = new FileOutputStream(savedPath.toString());
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(operationData);
            AppScene.addLog(LogLevel.INFO, className, "Saved All");
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error saving all operation data: " + e.getMessage());
        }
    }
    private Path getSavePath(String fileName) {
        if (savedDirectory != null)
            return Paths.get(savedDirectory.toString(), fileName);
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a Directory");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File selectedDirectory = directoryChooser.showDialog(topMenuBarMainHBox.getScene().getWindow());
        if (selectedDirectory != null)
            return Paths.get(selectedDirectory.getAbsolutePath(), fileName);
        return null;
    }

    public void exit(ActionEvent event) {
        System.out.println("Exit all");
        System.exit(0);
    }

    public void createNewOperationEvent(ActionEvent event) {
        AppScene.loadEmptyOperation();
        AppScene.updateOperationSideMenuHierarchy();
    }

    public void openSavedOperationEvent(ActionEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on open saved operation");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Serialized Files", "*.ser")
        );
        File selectedFile = fileChooser.showOpenDialog(topMenuBarMainHBox.getScene().getWindow());
        if (selectedFile == null)
            return;
        boolean saveLoaded = AppScene.loadSavedOperation(selectedFile.getAbsolutePath());
        if (saveLoaded)
            savedDirectory = Paths.get(selectedFile.getAbsolutePath()).getParent();
        AppScene.updateOperationSideMenuHierarchy();
    }

    public void startOperationRunEvent(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on start operation");
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
