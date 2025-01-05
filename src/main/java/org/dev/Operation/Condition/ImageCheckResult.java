package org.dev.Operation.Condition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.awt.image.BufferedImage;

@AllArgsConstructor @Getter
public class ImageCheckResult {

    private String readResult;
    private BufferedImage displayImage;
    @Setter
    private boolean pass;
}
