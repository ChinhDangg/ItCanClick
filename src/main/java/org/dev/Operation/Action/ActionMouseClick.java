package org.dev.Operation.Action;

import java.awt.AWTException;

public class ActionMouseClick extends Action {
    @Override
    public void performAction() {
        try {
            performMouseClick(mainImageBoundingBox);
        } catch (AWTException e) {
            System.out.println("Fail performing mouse click action");
        }
    }
}
