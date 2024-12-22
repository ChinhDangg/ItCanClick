package org.dev.LeftSideMenu;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class BottomPaneController implements Initializable {

    @FXML
    private StackPane bottomMainStackPane;
    @FXML
    private StackPane resizeStackPane, minimizeStackPaneButton;
    @FXML
    private VBox bottomTextVBox;

    private final String className = this.getClass().getSimpleName();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bottomTextVBox.setPrefHeight(bottomMainStackPane.getMaxHeight() - resizeStackPane.getPrefHeight() - 30);
        resizeStackPane.setOnMouseDragged(event -> {
            double newHeight = bottomMainStackPane.getMaxHeight() - event.getY();
            bottomMainStackPane.setMaxHeight(newHeight);
            bottomMainStackPane.setPrefHeight(newHeight);
            bottomTextVBox.setPrefHeight(newHeight - resizeStackPane.getPrefHeight() - 30);
        });
        minimizeStackPaneButton.setOnMouseClicked(this::minimizeStackPaneButtonEvent);
        bottomMainStackPane.setVisible(false);
    }

    private void minimizeStackPaneButtonEvent(MouseEvent event) {
        AppScene.addLog(LogLevel.DEBUG, className, "Clicked on minimized bottom pane button");
        AppScene.sideBarController.toggleLogPane();
    }

    public void switchBottomPaneVisible() {
        boolean newVisible = !bottomMainStackPane.isVisible();
        bottomMainStackPane.setVisible(newVisible);
        AppScene.addLog(LogLevel.DEBUG, className, "Bottom pane visible switched: " + newVisible);
    }

    Font monospacedFont = Font.font("Courier New", 14);
    public void addToLog(LogLevel logLevel, String className, String content) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTimestamp = now.format(formatter);

        String timeStampStr = String.format("%-" + 20 + "s", formattedTimestamp);
        String levelStr = String.format("%-" + 7 + "s", logLevel);
        String loggerStr = String.format("%-" + 25 + "s", "[" + className + "]");

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
        bottomTextVBox.getChildren().add(line);
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
}
