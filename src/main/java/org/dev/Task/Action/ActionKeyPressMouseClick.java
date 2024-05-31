package org.dev.Task.Action;

import org.dev.Enum.Actions;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ActionKeyPressMouseClick extends Action {

    public ActionKeyPressMouseClick(Actions actions, BufferedImage mainImage, BufferedImage displayImage, Rectangle boundingBox,
                          int keyCode) {
        super(actions, mainImage, displayImage, boundingBox, keyCode);
    }

    @Override
    public void performAction() {
        try {
            Robot robot = new Robot();

        } catch (AWTException e) {
            System.out.println("Error with action perform key press mouse click");
        }
    }
}
