package org.dev.Operation.Action;

import org.dev.AppScene;
import org.dev.Enum.LogLevel;

import java.awt.Robot;
import java.awt.AWTException;
import java.awt.event.KeyEvent;

public class ActionKeyClick extends Action {
    private final String className = this.getClass().getSimpleName();

    @Override
    public void performAction() {
        try {
            Robot robot = new Robot();
            performKeyPress(robot, keyCode);
            robot.delay(70 + (int) (Math.random() * 50));
            robot.keyRelease(keyCode);
            AppScene.addLog(LogLevel.DEBUG, className, "Key released: " + KeyEvent.getKeyText(keyCode));
        } catch (AWTException e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error with action key click: " + e.getMessage());
        }
    }
}
