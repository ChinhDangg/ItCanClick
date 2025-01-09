package org.dev.Operation.Action;

import org.dev.AppScene;
import org.dev.Enum.LogLevel;

import java.awt.AWTException;

public class ActionMouseDoubleClick extends ActionMouseClick {

    @Override
    public void performAction() {
        try {
            performMouseClick(mainImageBoundingBox, 2);
        } catch (AWTException e) {
            AppScene.addLog(LogLevel.ERROR, this.getClass().getSimpleName(), "Error with action double mouse clicks: " + e.getMessage());
        }
    }
}
