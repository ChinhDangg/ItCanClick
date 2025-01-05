package org.dev.SideMenu;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import java.io.IOException;

public class TopNotificationController {

    private final Group root;
    private final VBox notificationListVBox;

    private Timeline notificationTimeline;
    private final String className = this.getClass().getSimpleName();

    public TopNotificationController() {
        notificationListVBox = new VBox();
        root = new Group(notificationListVBox);
        initializeNotificationTimeLine();
    }

    public Node getOuterParentNode() { return root; }

    public void showNotification(String notification) {
        ObservableList<Node> children = notificationListVBox.getChildren();
        Parent parent = getTopNotificationPane();
        if (parent == null) {
            AppScene.addLog(LogLevel.ERROR, className, "Error getting top notification");
            return;
        }
        StackPane stackPane = (StackPane) parent.getChildrenUnmodifiable().getFirst();
        Label notificationLabel = new Label(notification);
        notificationLabel.setFont(new Font(14));
        notificationLabel.setTextFill(Color.WHITE);
        stackPane.getChildren().add(notificationLabel);
        children.add(parent);
        if (notificationTimeline.getStatus() == Timeline.Status.RUNNING) {
            if (children.size() > 3)
                children.removeFirst();
        }
        else
            notificationTimeline.playFromStart();
    }

    private void initializeNotificationTimeLine() {
        notificationTimeline = getNotificationTimeline();
        notificationTimeline.setCycleCount(Timeline.INDEFINITE);

        // Add a listener to stop the Timeline when there are no children left
        notificationTimeline.currentTimeProperty().addListener((_, _, _) -> {
            if (notificationListVBox.getChildren().isEmpty()) {
                notificationTimeline.stop();
            }
        });
    }
    private Timeline getNotificationTimeline() {
        KeyFrame keyFrame = new KeyFrame(
                Duration.millis(1500), _ -> {
            if (!notificationListVBox.getChildren().isEmpty()) {
                // Remove the first child node
                notificationListVBox.getChildren().removeFirst();
            }
        });
        return new Timeline(keyFrame);
    }

    private Parent getTopNotificationPane() {
        AppScene.addLog(LogLevel.TRACE, className, "Loading a top notification");
        try {
            FXMLLoader topBanner = new FXMLLoader(this.getClass().getResource("topNotificationBannerPane.fxml"));
            Parent parentNode = topBanner.load();
            AppScene.addLog(LogLevel.TRACE, className, "Loaded a top notification");
            return parentNode;
        } catch (IOException e) {
            System.out.println("Error loading top notification banner");
            AppScene.addLog(LogLevel.ERROR, className, "Error loading a top notification");
            return null;
        }
    }
}

