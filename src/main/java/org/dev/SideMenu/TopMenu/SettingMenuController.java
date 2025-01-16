package org.dev.SideMenu.TopMenu;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class SettingMenuController implements Initializable {
    @FXML
    private Node settingParentNode;
    @FXML
    private CheckBox logDebugCheckBox, logTraceCheckBox;
    @FXML
    private Label scaleValueLabel;
    @FXML
    private StackPane scaleMinusButton, scalePlusButton;
    @FXML
    private ChoiceBox<WindowSizeMode> windowSizeChoiceBox;
    @FXML
    private Node confirmButton, cancelButton;


    private final String className = this.getClass().getSimpleName();
    private final String FILE_PATH = "application.properties";
    private Popup settingPopup;
    private double scaleValue = 1.0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        settingPopup = new Popup();
        settingPopup.getContent().add(settingParentNode);
        settingPopup.setAutoHide(true);
        scaleMinusButton.setOnMouseClicked(this::decreaseScaleValue);
        scalePlusButton.setOnMouseClicked(this::increaseScaleValue);
        confirmButton.setOnMouseClicked(this::confirmSetting);
        cancelButton.setOnMouseClicked(this::cancelSetting);
        windowSizeChoiceBox.getItems().addAll(WindowSizeMode.values());
        setDefaultOption();
    }

    public boolean isTrace() { return logTraceCheckBox.isSelected(); }
    public boolean isDebug() { return logDebugCheckBox.isSelected(); }
    public double getGlobalScaleValue() { return scaleValue; }
    public WindowSizeMode getWindowSizeMode() { return windowSizeChoiceBox.getValue(); }

    private void confirmSetting(MouseEvent mouseEvent) {
        writeProperties();
        hidePopUp();
    }

    private void cancelSetting(MouseEvent mouseEvent) {
        readProperties();
        hidePopUp();
    }

    public void showSettingPopUp(Scene scene) {
        Stage stage = (Stage) scene.getWindow();
        settingPopup.show(stage, stage.getX() + 100, stage.getY() + 100);
    }

    private void hidePopUp() {
        settingPopup.hide();
    }

    private void setDefaultOption() {
        if (readProperties())
            return;
        logDebugCheckBox.setSelected(false);
        logTraceCheckBox.setSelected(false);
        windowSizeChoiceBox.setValue(WindowSizeMode.Default);
    }

    private void decreaseScaleValue(MouseEvent mouseEvent) { updateScaleValue(Math.max(scaleValue - 0.1, 0.5)); }
    private void increaseScaleValue(MouseEvent mouseEvent) { updateScaleValue(Math.min(scaleValue + 0.1, 2.0)); }
    private void updateScaleValue(double newValue) {
        scaleValue = newValue;
        scaleValueLabel.setText(String.valueOf(scaleValue));
    }

    private void writeProperties() {
        Properties properties = loadProperties();
        if (properties == null)
            return;
        try (OutputStream output = new FileOutputStream(FILE_PATH)) {
            properties.setProperty("app.config.log-trace", String.valueOf(logDebugCheckBox.isSelected()));
            properties.setProperty("app.config.log-debug", String.valueOf(logTraceCheckBox.isSelected()));
            properties.setProperty("app.config.scene-scale", String.valueOf(scaleValue));
            properties.setProperty("app.config.window-size", String.valueOf(windowSizeChoiceBox.getValue()));
            properties.store(output, "Application Configuration");
            System.out.println("Property saved successfully!");
            AppScene.addLog(LogLevel.INFO, className, "Setting will apply in the next start up");
        } catch (IOException e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error saving properties file: " + e.getMessage());
        }
    }

    private boolean readProperties() {
        Properties properties = loadProperties();
        if (properties == null)
            return false;
        try {
            boolean isTrace = Boolean.parseBoolean(properties.getProperty("app.config.log-trace"));
            boolean isDebug = Boolean.parseBoolean(properties.getProperty("app.config.log-debug"));
            double sceneScaleValue = Double.parseDouble(properties.getProperty("app.config.scene-scale"));
            WindowSizeMode windowSizeMode = WindowSizeMode.valueOf(properties.getProperty("app.config.window-size"));

            logTraceCheckBox.setSelected(isTrace);
            logDebugCheckBox.setSelected(isDebug);
            updateScaleValue(sceneScaleValue);
            windowSizeChoiceBox.setValue(windowSizeMode);
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error reading property file: " + e.getMessage());
            return false;
        }
        return true;
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(FILE_PATH)) {
            properties.load(input);
        } catch (IOException e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error finding properties file: " + e.getMessage());
            return null;
        }
        return properties;
    }
}
