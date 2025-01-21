package org.dev.Job.Action;

import org.dev.AppScene;
import org.dev.Enum.LogLevel;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

public class ActionKeyPressMouseClick extends ActionKeyPress {

    private final String className = this.getClass().getSimpleName();

    @Override
    public void performAction() {
        try {
            Robot robot = new Robot();
            performKeyPressAndMouseClick(robot, keyCode);
        } catch (AWTException e) {
            AppScene.addLog(LogLevel.DEBUG, className, "Error with action perform key press mouse click");
        }
    }

    protected void performKeyPressAndMouseClick(Robot robot, int eventKey) throws AWTException {
        performKeyPress(robot, eventKey);
        new ActionMouseClick().performMouseClick(mainImageBoundingBox, 1);
        robot.delay(70 + (int) (Math.random() * 50));
        robot.keyRelease(eventKey);
        AppScene.addLog(LogLevel.DEBUG, className, "Key released: " + KeyEvent.getKeyText(eventKey));
    }
}
