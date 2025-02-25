package org.dev.Job.Condition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.awt.image.BufferedImage;

@AllArgsConstructor @Getter
public class ImageCheckResult {

    private String readResult;
    private double confidence;
    private Rectangle boundingBox;
    private BufferedImage displayImage;
    @Setter
    private boolean pass;

    public ImageCheckResult(boolean pass) {
        this.pass = pass;
    }
}
