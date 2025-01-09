package org.dev.Operation.Action;

import org.dev.AppScene;
import org.dev.Enum.LogLevel;

import java.awt.Robot;
import java.awt.AWTException;
import java.awt.event.KeyEvent;

public class ActionKeyClick extends ActionKeyPress {
    private final String className = this.getClass().getSimpleName();

    @Override
    public void performAction() {
        try {
            Robot robot = new Robot();
            performKeyClick(robot, keyCode);
        } catch (AWTException e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error with action key click: " + e.getMessage());
        }
    }

    protected void performKeyClick(Robot robot, int keyCode) throws AWTException {
        performKeyPress(robot, keyCode);
        robot.delay(70 + (int) (Math.random() * 50));
        robot.keyRelease(keyCode);
        AppScene.addLog(LogLevel.DEBUG, className, "Key released: " + KeyEvent.getKeyText(keyCode));
    }
}
