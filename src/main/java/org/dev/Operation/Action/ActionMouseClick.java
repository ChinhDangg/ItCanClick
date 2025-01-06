package org.dev.Operation.Action;

import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import java.awt.AWTException;

public class ActionMouseClick extends Action {
    @Override
    public void performAction() {
        try {
            performMouseClick(mainImageBoundingBox);
        } catch (AWTException e) {
            AppScene.addLog(LogLevel.ERROR, this.getClass().getSimpleName(), "Error performing mouse click action: " + e.getMessage());
        }
    }
}
