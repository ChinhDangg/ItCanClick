package org.dev.RunOperation;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.awt.Rectangle;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.image.BufferedImage;

public abstract class RunActivity {

    public void updateImageView(ImageView imageView, BufferedImage image) {
        if (image != null)
            imageView.setImage(SwingFXUtils.toFXImage(image, null));
    }

    public void updateImageView(ImageView imageView, Rectangle boundingBox) {
        try {
            BufferedImage image = new Robot().createScreenCapture(boundingBox);
            imageView.setImage(SwingFXUtils.toFXImage(image, null));
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    public void changeLabelText(Label label, String labelText) {
        Platform.runLater(() -> label.setText(labelText));
    }

    public void updatePaneStatusColor(Pane whichPane, boolean pass) {
        if (pass)
            whichPane.setStyle("-fx-border-color: green;");
        else
            whichPane.setStyle("-fx-border-color: red;");
    }

    public void changeScrollPaneVValueView(ScrollPane scrollPane, VBox container, Node node) {
        double targetPaneY = node.getBoundsInParent().getMinY();
        if (container != null) {
            Node parentChecking = node.getParent();
            while (parentChecking != container) {
                targetPaneY += parentChecking.getBoundsInParent().getMinY();
                parentChecking = parentChecking.getParent();
            }
        }
        double contentHeight = scrollPane.getContent().getBoundsInLocal().getHeight();
        double scrollPaneHeight = scrollPane.getViewportBounds().getHeight();
        targetPaneY -= scrollPaneHeight / 3;
        double vValue = Math.min(targetPaneY / (contentHeight - scrollPaneHeight), 1.00);
        scrollPane.setVvalue(vValue);
    }

    public void changeScrollPaneHValueView(ScrollPane scrollPane, Node node) {
        double targetPaneX = node.getBoundsInParent().getMinX();
        double contentWidth = scrollPane.getContent().getBoundsInLocal().getWidth();
        double scrollPaneWidth = scrollPane.getViewportBounds().getWidth();
        double hValue = Math.min(targetPaneX / (contentWidth - scrollPaneWidth), 1.00);
        scrollPane.setHvalue(hValue);
    }

}
