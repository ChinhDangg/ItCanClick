package org.dev.Operation.Action;

import org.dev.AppScene;
import org.dev.Enum.LogLevel;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

public class ActionKeyPress extends Action {

    private final String className = this.getClass().getSimpleName();

    @Override
    public void performAction() {
        try {
            Robot robot = new Robot();
            performKeyPress(robot, keyCode);
        } catch (AWTException e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error with action key press: " + e.getMessage());
        }
    }

    protected void performKeyPress(Robot robot, int eventKey) throws AWTException {
        if (robot == null)
            throw new NullPointerException();
        robot.keyPress(eventKey);
        AppScene.addLog(LogLevel.DEBUG, className, "Key pressed: " + KeyEvent.getKeyText(eventKey));
    }
}
