package org.dev.Task.Action;

import java.awt.AWTException;

public class ActionKeyClick extends Action {

    @Override
    public void performAction() {
        try {
            performMouseClick(mainImageBoundingBox);
        } catch (AWTException e) {
            System.out.println("Error with action key clicking");
        }
    }
}
