package org.dev.Operation.Action;

import java.awt.*;

public class ActionMouseDoubleClick extends Action {
    @Override
    public void performAction() {
        try {
            performMouseDoubleClick(mainImageBoundingBox);
        } catch (AWTException e) {
            System.out.println("Error with action double mouse clicks");
        }
    }
}
