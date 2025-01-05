package org.dev.SideMenu;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.ResourceBundle;

public class BottomPaneController implements Initializable {

    @FXML
    private StackPane bottomMainStackPane;
    @FXML
    private StackPane resizeStackPane, minimizeStackPaneButton, trashStackPaneButton;
    @FXML
    private ScrollPane logScrollPane;
    @FXML
    private VBox topVBoxHeader, bottomTextVBox;

    private final String className = this.getClass().getSimpleName();
    private boolean isTrace, isDebug;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bottomTextVBox.setPrefHeight(bottomMainStackPane.getMaxHeight() - resizeStackPane.getPrefHeight() - 30);
        resizeStackPane.setOnMouseDragged(this::resizeBottomPane);
        trashStackPaneButton.setOnMouseClicked(this::clearLog);
        minimizeStackPaneButton.setOnMouseClicked(this::minimizeStackPaneButtonEvent);
        bottomTextVBox.heightProperty().addListener((_, _, _) -> logScrollPane.setVvalue(1.0));
        bottomMainStackPane.setVisible(false);
        bottomMainStackPane.setManaged(false);
        readProperties();
    }

    private double maxHeight = Double.MAX_VALUE;
    private void resizeBottomPane(MouseEvent event) {
        double newHeight = bottomMainStackPane.getMaxHeight() - event.getY();
        if (newHeight > maxHeight || newHeight < topVBoxHeader.getHeight())
            return;
        if (bottomMainStackPane.getLayoutY() <= topVBoxHeader.getHeight())
            maxHeight = newHeight;
        bottomMainStackPane.setMaxHeight(newHeight);
        bottomMainStackPane.setPrefHeight(newHeight);
        bottomMainStackPane.setMinHeight(newHeight);
        bottomTextVBox.setPrefHeight(newHeight - topVBoxHeader.getHeight());
    }

    private void minimizeStackPaneButtonEvent(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on minimized bottom pane button");
        AppScene.sideBarController.toggleLogPane();
    }

    public void switchBottomPaneVisible() {
        boolean newVisible = !bottomMainStackPane.isVisible();
        bottomMainStackPane.setVisible(newVisible);
        bottomMainStackPane.setManaged(newVisible);
        AppScene.addLog(LogLevel.DEBUG, className, "Bottom pane visible switched: " + newVisible);
    }

    Font monospacedFont = Font.font("Courier New", 14);
    public void addToLog(LogLevel logLevel, String className, String content) {
        if (logLevel == LogLevel.TRACE && !isTrace)
            return;
        if (logLevel == LogLevel.DEBUG && !isDebug)
            return;

        if (logLevel == LogLevel.INFO)
            Platform.runLater(() -> AppScene.showNotification(content));
        else if (logLevel == LogLevel.WARN || logLevel == LogLevel.ERROR)
            AppScene.openCenterBanner(logLevel.name(), content);

        TextFlow line = getLogLine(logLevel, className, content);
        System.out.println(logLevel + " " + className + " " + content);
        Platform.runLater(() -> bottomTextVBox.getChildren().add(line));
    }

    private void clearLog(MouseEvent event) {
        Platform.runLater(() -> bottomTextVBox.getChildren().clear());
    }

    private TextFlow getLogLine(LogLevel logLevel, String className, String content) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTimestamp = now.format(formatter);

        String timeStampStr = String.format("%-" + 20 + "s", formattedTimestamp);
        String levelStr = String.format("%-" + 7 + "s", logLevel);
        String loggerStr = String.format("%-" + 30 + "s", "[" + className + "]");

        Text timeStampText = new Text(timeStampStr);
        timeStampText.setFont(monospacedFont);
        timeStampText.setFill(Color.BLACK);

        TextFlow line = new TextFlow();
        Text logLevelText = new Text(levelStr);
        logLevelText.setFont(monospacedFont);
        logLevelText.setFill(getLogLevelColor(logLevel));

        Text loggerText = new Text(loggerStr);
        loggerText.setFont(monospacedFont);
        loggerText.setFill(Color.BLACK);

        Text colonText = new Text(" : ");
        colonText.setFont(monospacedFont);
        colonText.setFill(Color.BLACK);

        Text contentText = new Text(content);
        contentText.setFont(monospacedFont);
        contentText.setFill(Color.BLACK);
        line.getChildren().addAll(timeStampText, logLevelText, loggerText, colonText, contentText);
        return line;
    }

    private Color getLogLevelColor(LogLevel level) {
        return switch (level) {
            case LogLevel.DEBUG -> Color.DARKGREEN;
            case LogLevel.TRACE -> Color.DARKBLUE;
            case LogLevel.WARN -> Color.DARKORANGE;
            case LogLevel.ERROR -> Color.DARKRED;
            default -> Color.BLACK;
        };
    }

    private void readProperties() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/application.properties")) {
            if (input == null)
                throw new IOException("Unable to find application.properties");
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        isTrace = Boolean.parseBoolean(properties.getProperty("app.config.log-trace"));
        isDebug = Boolean.parseBoolean(properties.getProperty("app.config.log-debug"));
    }
}
