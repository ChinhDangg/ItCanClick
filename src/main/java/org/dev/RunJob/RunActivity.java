package org.dev.RunJob;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import java.awt.image.BufferedImage;

public abstract class RunActivity {

    public void updateImageView(ImageView imageView, BufferedImage image) {
        if (image != null)
            imageView.setImage(SwingFXUtils.toFXImage(image, null));
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
