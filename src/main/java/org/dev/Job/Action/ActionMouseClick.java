package org.dev.Job.Action;

import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import java.awt.*;
import java.awt.event.InputEvent;

public class ActionMouseClick extends Action {

    private final String className = this.getClass().getSimpleName();

    @Override
    public void performAction() {
        try {
            performMouseClick(mainImageBoundingBox, 1);
        } catch (AWTException e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error performing mouse click action: " + e.getMessage());
        }
    }

    protected void performMouseClick(Rectangle box, int numOfClick) throws AWTException {
        Robot robot = new Robot();
        int randomX = (int) (box.getX() + Math.random() * (box.getWidth() + 1));
        int randomY = (int) (box.getY() + Math.random() * (box.getHeight() + 1));
        robot.mouseMove(randomX, randomY);
        for (int i = 0; i < numOfClick; i++) {
            robot.delay(50 + (int) (Math.random() * 100));
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); // left mouse
            robot.delay(50 + (int) (Math.random() * 100));
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        }
        AppScene.addLog(LogLevel.DEBUG, className, "Mouse clicked at (" + randomX + ", " + randomY + ")");
    }
}
