package org.dev.Operation.Action;

import lombok.Getter;
import lombok.Setter;
import org.dev.Enum.ActionTypes;
import org.dev.ImageSerialization;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

@Getter
public abstract class Action implements Serializable {
    @Setter
    protected String actionName = "Action Name";
    @Setter
    protected ActionTypes chosenActionPerform;
    protected transient BufferedImage mainImage;
    protected transient BufferedImage displayImage;
    protected Rectangle mainImageBoundingBox;
    protected int attempt;
    protected int keyCode;
    protected boolean progressiveSearch;
    protected int progressiveSearchTime, waitBeforeTime, waitAfterTime;
    @Setter
    protected boolean required, previousPass;

    public abstract void performAction();

    protected void performMouseClick(Rectangle box) throws AWTException {
        Robot robot = new Robot();
        int randomX = (int) (box.getX() + Math.random() * (box.getWidth() + 1));
        int randomY = (int) (box.getY() + Math.random() * (box.getHeight() + 1));
        robot.mouseMove(randomX, randomY);
        robot.delay(50 + (int) (Math.random() * 100));
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); // left mouse
        robot.delay(50 + (int) (Math.random() * 100));
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        System.out.println("Mouse clicked at (" + randomX + ", " + randomY + ")");
    }

    protected void performMouseDoubleClick(Rectangle box) throws AWTException {
        int randomX = (int) (box.getX() + Math.random() * (box.getWidth() + 1));
        int randomY = (int) (box.getY() + Math.random() * (box.getHeight() + 1));
        Robot robot = new Robot();
        robot.mouseMove(randomX, randomY);
        robot.delay(50 + (int) (Math.random() * 100));
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); // left mouse
        robot.delay(50 + (int) (Math.random() * 100));
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(50 + (int) (Math.random() * 50));
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); // left mouse
        robot.delay(50 + (int) (Math.random() * 100));
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        System.out.println("Mouse clicked at (" + randomX + ", " + randomY + ")");
    }

    protected void performKeyPress(Robot robot, int eventKey) throws AWTException {
        if (robot == null)
            throw new NullPointerException();
        robot.keyPress(eventKey);
        System.out.println("Key pressed " + KeyEvent.getKeyText(eventKey));
    }

    public void setActionOptions(int attempt, boolean progressive, int progressiveSearchTime, int beforeTime, int afterTime, ActionTypes actionTypes,
                                 BufferedImage mainImage, BufferedImage displayImage, Rectangle boundingBox,
                                 int keyCode) {
        this.attempt = attempt;
        progressiveSearch = progressive;
        this.progressiveSearchTime = progressiveSearchTime;
        waitBeforeTime = beforeTime;
        waitAfterTime = afterTime;
        chosenActionPerform = actionTypes;
        this.mainImage = mainImage;
        this.displayImage = displayImage;
        mainImageBoundingBox = boundingBox;
        this.keyCode = keyCode;
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ImageSerialization.serializeBufferedImageWriteObject(out, mainImage);   // Serialize mainImage
        ImageSerialization.serializeBufferedImageWriteObject(out, displayImage);// Serialize displayImage
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String mainImageString = (String) in.readObject();
        mainImage = ImageSerialization.deserializeBufferedImageReadObject(in, mainImageString, false);
        String displayImageString = (String) in.readObject();
        displayImage = ImageSerialization.deserializeBufferedImageReadObject(in, displayImageString, false);
    }
}
