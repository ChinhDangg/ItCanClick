package org.dev.Operation.Action;

import java.awt.Robot;
import java.awt.AWTException;
import java.awt.event.KeyEvent;

public class ActionKeyClick extends Action {

    @Override
    public void performAction() {
        try {
            Robot robot = new Robot();
            performKeyPress(robot, keyCode);
            robot.delay(70 + (int) (Math.random() * 50));
            robot.keyRelease(keyCode);
            System.out.println(STR."Key released: \{KeyEvent.getKeyText(keyCode)}");
        } catch (AWTException e) {
            System.out.println("Error with action key clicking");
        }
    }
}
