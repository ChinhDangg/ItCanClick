package org.dev.Task.Action;

import org.dev.Enum.Actions;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public abstract class Action {
    protected Actions chosenActionPerform;
    protected BufferedImage mainImage;
    protected BufferedImage displayImage;
    protected Rectangle mainImageBoundingBox;
    protected int keyCode;

    public Action(Actions actions, BufferedImage mainImage, BufferedImage displayImage, Rectangle boundingBox,
                  int keyCode) {
        chosenActionPerform = actions;
        this.mainImage = mainImage;
        this.displayImage = displayImage;
        mainImageBoundingBox = boundingBox;
        this.keyCode = keyCode;
    }

    public Action(Actions actions, BufferedImage mainImage, BufferedImage displayImage, Rectangle boundingBox) {
        chosenActionPerform = actions;
        this.mainImage = mainImage;
        this.displayImage = displayImage;
        mainImageBoundingBox = boundingBox;
    }

    public abstract void performAction();
}
