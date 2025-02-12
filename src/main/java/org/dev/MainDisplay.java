package org.dev;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import java.util.Objects;

/*
    Layout:
    primaryBorderPane
        Top: top menu bar
        Left: Left Side bar
        Center: primaryCenterStackPane
            mainCenterVBox
                mainCenterHBox
                    sideMenu (for sidebar)
                    mainDisplayStackPane
                        mainDisplayScrollPane
                            mainDisplayVBox (operation, task, action, menu (action, condition), and run operation (task, action, condition)
                bottomPane
            mainNotificationStackPane
                topNotificationBanner
     */
public class MainDisplay {
    private final BorderPane primaryBorderPane = new BorderPane();
    private StackPane primaryCenterStackPane = new StackPane();
    private VBox mainCenterVBox = new VBox(); // will contain the main HBox and bottom pane
    private HBox mainCenterHBox = new HBox(); // will contain side menu and main display
    private final StackPane mainDisplayStackPane = new StackPane();
    private final ScrollPane mainDisplayScrollPane = new ScrollPane();
    private final VBox mainDisplayVBox = new VBox();
    private StackPane mainNotificationStackPane = new StackPane();

    public MainDisplay(Node bottomPane, Node notificationPane, Node topPane, Node leftBarPane, Node leftMenuPane) {
        VBox.setVgrow(mainCenterHBox, Priority.ALWAYS);
        mainCenterVBox.getChildren().add(mainCenterHBox);
        mainCenterVBox.getChildren().add(bottomPane);
        primaryCenterStackPane.getChildren().add(mainCenterVBox);
        primaryCenterStackPane.getChildren().add(mainNotificationStackPane);

        mainNotificationStackPane.getChildren().add(notificationPane);
        mainNotificationStackPane.setMouseTransparent(true);

        primaryBorderPane.getStylesheets().add(Objects.requireNonNull(AppScene.class.getResource("/styles/root.css")).toExternalForm());
        primaryBorderPane.setTop(topPane);
        primaryBorderPane.setCenter(primaryCenterStackPane);
        primaryBorderPane.setLeft(leftBarPane);
        primaryBorderPane.setOnMouseClicked(_ -> primaryBorderPane.requestFocus());

        HBox.setHgrow(mainDisplayStackPane, Priority.ALWAYS);
        mainCenterHBox.getChildren().add(leftMenuPane);
        mainCenterHBox.getChildren().add(mainDisplayStackPane);
        mainDisplayStackPane.getChildren().add(mainDisplayScrollPane);
        mainDisplayScrollPane.setContent(mainDisplayVBox);
        mainDisplayScrollPane.setFitToWidth(true);
        mainDisplayScrollPane.setFitToHeight(true);
    }

    public void displayInMainDisplayStackPane(Node displayNode) {
        mainDisplayStackPane.getChildren().add(displayNode);
    }

    public void clearDisplayInMainDisplayStackPane(Node displayNode) {
        mainDisplayStackPane.getChildren().remove(displayNode);
    }

    public void displayNewMainNode(Node centerNode) {
        if (mainDisplayVBox.getChildren().contains(centerNode))
            return;
        mainDisplayVBox.getChildren().clear();
        mainDisplayVBox.getChildren().add(centerNode);
    }

    public Parent getParentNode() {
        return primaryBorderPane;
    }

    public void changeScrollPaneView(Node taskPane) {
        double targetPaneY = taskPane.getBoundsInParent().getMinY();
        Node parentChecking = taskPane.getParent();
        while (parentChecking != mainDisplayVBox) {
            targetPaneY += parentChecking.getBoundsInParent().getMinY();
            parentChecking = parentChecking.getParent();
        }
        targetPaneY += parentChecking.getBoundsInParent().getMinY();
        targetPaneY *= AppScene.currentGlobalScale;
        double contentHeight = mainDisplayScrollPane.getContent().getBoundsInLocal().getHeight();
        double scrollPaneHeight = mainDisplayScrollPane.getViewportBounds().getHeight();
        targetPaneY -= scrollPaneHeight / 3;
        double vValue = Math.min(targetPaneY / (contentHeight - scrollPaneHeight), 1.00);
        mainDisplayScrollPane.setVvalue(vValue);
    }
}
