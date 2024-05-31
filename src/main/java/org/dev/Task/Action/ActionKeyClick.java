package org.dev.Task.Action;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import org.dev.Enum.Actions;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ActionKeyClick extends ActionKeyPress {

    public ActionKeyClick(Actions actions, BufferedImage mainImage, BufferedImage displayImage, Rectangle boundingBox,
                  int keyCode) {
        super(actions, mainImage, displayImage, boundingBox, keyCode);
    }

    @Override
    public void performAction() {
        try {
            performKeyClick(keyCode);
        } catch (AWTException e) {
            System.out.println("Error with action key clicking");
        }
    }

    public void performKeyClick(int nativeKey) throws AWTException {
        int key = mapNativeKeyToKeyEvent(nativeKey);
        if (key == -1) {
            System.out.println("Key is not currently supported: " + NativeKeyEvent.getKeyText(keyCode));
            return;
        }
        Robot robot = new Robot();
        robot.keyPress(key);
        robot.delay(70 + (int) (Math.random() * 50));
        robot.keyRelease(key);
    }
}
