package org.dev.Operation.Action;

import org.dev.AppScene;
import org.dev.Enum.LogLevel;

import java.awt.AWTException;
import java.awt.Robot;

public class ActionKeyPress extends Action {

    @Override
    public void performAction() {
        try {
            Robot robot = new Robot();
            performKeyPress(robot, keyCode);
        } catch (AWTException e) {
            AppScene.addLog(LogLevel.ERROR, this.getClass().getSimpleName(), "Error with action key press");
        }
    }
}
