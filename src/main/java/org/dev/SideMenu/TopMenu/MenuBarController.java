package org.dev.SideMenu.TopMenu;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import org.dev.Job.JobData;
import org.dev.Job.Operation;
import org.dev.jobManagement.JobRunStructure;
import org.dev.jobManagement.JobStructure;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

public class MenuBarController implements Initializable, NativeKeyListener {

    @FXML
    private HBox topMenuBarMainHBox;
    @FXML
    private MenuItem saveMenuItem, exitMenuItem, settingMenuItem;
    @FXML
    private MenuItem newOperationMenuItem, openSavedOperationMenuItem;
    @FXML
    private MenuItem collapseTaskGroupMenuItem, collapseTaskMenuItem;
    @FXML
    private MenuItem collapseTaskGroupRunMenuItem, collapseTaskRunMenuItem, collapseActionRunMenuItem;
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
        settingMenuItem.setOnAction(this::openSetting);
        newOperationMenuItem.setOnAction(this::createNewOperationEvent);
        openSavedOperationMenuItem.setOnAction(this::openSavedOperationEvent);
        collapseTaskGroupMenuItem.setOnAction(this::collapseTaskGroup);
        collapseTaskMenuItem.setOnAction(this::collapseTask);
        collapseTaskGroupRunMenuItem.setOnAction(this::collapseTaskGroupRun);
        collapseTaskRunMenuItem.setOnAction(this::collapseTaskRun);
        collapseActionRunMenuItem.setOnAction(this::collapseActionRun);
        startRunStackPaneButton.setOnMouseClicked(this::startOperationRunEvent);
        stopRunStackPaneButton.setOnMouseClicked(this::stopOperationRunEvent);
        stopRunStackPaneButton.setVisible(false);
        operationRunningIndicationHBox.setVisible(false);
        registerKeyListener();
    }

    public void save(ActionEvent event) {
        try {
            AppScene.addLog(LogLevel.DEBUG, className, "Clicked on save all");
            if (AppScene.currentJobStructure == null)
                return;
            JobData operationData = AppScene.currentJobStructure.getCurrentController().getSavedDataByReference();
            String fileName = ((Operation) operationData.getMainJob()).getOperationName() + ".ser";
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

    private void openSetting(ActionEvent event) {
        AppScene.settingMenuController.showSettingPopUp(topMenuBarMainHBox.getScene());
    }

    private void exit(ActionEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Exit in 3s");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        unregisterKeyListener();
        System.exit(0);
    }

    private void createNewOperationEvent(ActionEvent event) {
        AppScene.loadEmptyOperation();
        AppScene.loadSideMenuHierarchy();
    }

    private void openSavedOperationEvent(ActionEvent event) {
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
        AppScene.loadSideMenuHierarchy();
    }

    private void startOperationRunEvent(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on start operation");
        AppScene.clearLog();
        AppScene.startOperationRun();
    }

    private void stopOperationRunEvent(MouseEvent event) {
        AppScene.stopOperationRun();
    }

    public void setOperationRunning(boolean isRunning) {
        stopRunStackPaneButton.setVisible(isRunning);
        operationRunningIndicationHBox.setVisible(isRunning);
    }

    private boolean isKeyListening = false;
    private void registerKeyListener() {
        if (isKeyListening)
            return;
        GlobalScreen.addNativeKeyListener(this);
        isKeyListening = true;
        AppScene.addLog(LogLevel.TRACE, className, "Registering key listener");
    }

    private void unregisterKeyListener() {
        if (!isKeyListening)
            return;
        GlobalScreen.removeNativeKeyListener(this);
        isKeyListening = false;
        AppScene.addLog(LogLevel.TRACE, className, "Unregistering key listener");
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_F1) {
            AppScene.addLog(LogLevel.INFO, className, "Hot key clicked to stop running job");
            stopOperationRunEvent(null);
        } else if (e.getKeyCode() == NativeKeyEvent.VC_F2) {
            AppScene.addLog(LogLevel.INFO, className, "Hot key clicked to start running job");
            startOperationRunEvent(null);
        }
    }


    private void collapseTaskGroup(ActionEvent event) {
        List<JobStructure> taskGroupStructureList = AppScene.getAllTaskGroupStructure();
        for (JobStructure taskGroupStructure : taskGroupStructureList)
            taskGroupStructure.collapseSubContent();
    }

    private void collapseTask(ActionEvent event) {
        List<JobStructure> taskStructureList = AppScene.getAllTaskStructure();
        for (JobStructure taskStructure : taskStructureList)
            taskStructure.collapseSubContent();
    }

    private void collapseTaskGroupRun(ActionEvent event) {
        collapseJobRun(AppScene.getAllTaskGroupRunStructure());
    }

    private void collapseTaskRun(ActionEvent event) {
        collapseJobRun(AppScene.getAllTaskRunStructure());
    }

    private void collapseActionRun(ActionEvent event) {
        collapseJobRun(AppScene.getAllActionRunStructure());
    }

    private void collapseJobRun(List<JobRunStructure> jobRunStructureList) {
        if (jobRunStructureList == null)
            return;
        for (JobRunStructure jobRunStructure : jobRunStructureList)
            jobRunStructure.collapseContent();
    }

}
