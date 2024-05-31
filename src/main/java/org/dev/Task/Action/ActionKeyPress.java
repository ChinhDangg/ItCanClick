package org.dev.Task.Action;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import org.dev.Enum.Actions;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ActionKeyPress extends Action {

    public ActionKeyPress(Actions actions, BufferedImage mainImage, BufferedImage displayImage, Rectangle boundingBox,
                          int keyCode) {
        super(actions, mainImage, displayImage, boundingBox, keyCode);
    }

    @Override
    public void performAction() {
        try {
            Robot robot = new Robot();
            performKeyPress(robot, keyCode);
        } catch (AWTException e) {
            System.out.println("Error with action key pressing");
        }
    }

    public void performKeyPress(Robot robot, int nativeKey) throws AWTException {
        int key = mapNativeKeyToKeyEvent(nativeKey);
        if (key == -1) {
            System.out.println("Key is not currently supported: " + NativeKeyEvent.getKeyText(keyCode));
            return;
        }
        if (robot == null)
            throw new NullPointerException();
        robot.keyPress(key);
    }

    protected int mapNativeKeyToKeyEvent(int nativeKey) {
        if (nativeKey > 1 && nativeKey < 12) { // 0-9
            if (nativeKey == 11) // 0
                return 48;
            return nativeKey + 47; // 1 - 9
        }
        else if (nativeKey > 29 && nativeKey < 45) //A-Z
            return nativeKey + 71;
        return -1;
    }
}
