package org.dev.Operation.Action;

import java.awt.AWTException;
import java.awt.Robot;

public class ActionKeyPress extends Action {

    @Override
    public void performAction() {
        try {
            Robot robot = new Robot();
            performKeyPress(robot, keyCode);
        } catch (AWTException e) {
            System.out.println("Error with action key pressing");
        }
    }
}
