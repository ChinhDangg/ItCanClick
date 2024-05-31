package org.dev.Task.Action;

import org.dev.Enum.Actions;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

public class ActionMouseClick extends Action {
    public ActionMouseClick(Actions actions, BufferedImage mainImage, BufferedImage displayImage, Rectangle boundingBox) {
        super(actions, mainImage, displayImage, boundingBox);
    }

    @Override
    public void performAction() {
        try {
            performMouseClick(mainImageBoundingBox);
        } catch (AWTException e) {
            System.out.println("Fail performing mouse click action");
        }
    }

    public void performMouseClick(Rectangle box) throws AWTException {
        Robot robot = new Robot();
        int randomX = (int) (box.getX() + Math.random() * (box.getWidth() + 1));
        int randomY = (int) (box.getY() + Math.random() * (box.getHeight() + 1));
        robot.mouseMove(randomX, randomY);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); // left mouse
        robot.delay(90 + (int) (Math.random() * 100));
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

}
