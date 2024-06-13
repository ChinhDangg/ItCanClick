package org.dev.Task.Action;

import lombok.Getter;
import lombok.Setter;
import org.dev.Enum.ActionTypes;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

@Getter
public abstract class Action {
    @Setter
    protected ActionTypes chosenActionPerform;
    protected BufferedImage mainImage;
    protected BufferedImage displayImage;
    protected Rectangle mainImageBoundingBox;
    protected int keyCode;
    protected boolean progressiveSearch;
    protected int waitBeforeTime, waitAfterTime;

    public abstract void performAction();

    protected void performMouseClick(Rectangle box) throws AWTException {
        Robot robot = new Robot();
        int randomX = (int) (box.getX() + Math.random() * (box.getWidth() + 1));
        int randomY = (int) (box.getY() + Math.random() * (box.getHeight() + 1));
        robot.mouseMove(randomX, randomY);
        robot.delay(50 + (int) (Math.random() * 100));
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); // left mouse
        robot.delay(50 + (int) (Math.random() * 100));
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    protected void performKeyPress(Robot robot, int eventKey) throws AWTException {
        if (robot == null)
            throw new NullPointerException();
        robot.keyPress(eventKey);
    }

    public void setActionOptions(boolean progressive, int beforeTime, int afterTime, ActionTypes actionTypes,
                                 BufferedImage mainImage, BufferedImage displayImage, Rectangle boundingBox,
                                 int keyCode) {
        progressiveSearch = progressive;
        waitBeforeTime = beforeTime;
        waitAfterTime = afterTime;
        chosenActionPerform = actionTypes;
        this.mainImage = mainImage;
        this.displayImage = displayImage;
        mainImageBoundingBox = boundingBox;
        this.keyCode = keyCode;
    }
}
