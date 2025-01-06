package org.dev.Operation.Action;

import org.dev.AppScene;
import org.dev.Enum.LogLevel;

import java.awt.AWTException;

public class ActionMouseDoubleClick extends Action {
    @Override
    public void performAction() {
        try {
            performMouseDoubleClick(mainImageBoundingBox);
        } catch (AWTException e) {
            AppScene.addLog(LogLevel.ERROR, this.getClass().getSimpleName(), "Error with action double mouse clicks: " + e.getMessage());
        }
    }
}
