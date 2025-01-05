package org.dev.RunOperation;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import java.awt.Rectangle;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.image.BufferedImage;

public abstract class RunActivity {

    private final String className = this.getClass().getSimpleName();

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
}
