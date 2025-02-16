package org.dev.Job.Action;

import lombok.Getter;
import lombok.Setter;
import org.dev.AppScene;
import org.dev.Enum.ActionTypes;
import org.dev.Enum.LogLevel;
import org.dev.Job.Condition.Condition;
import org.dev.Job.ImageSerialization;
import org.dev.Job.MainJob;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

@Getter
public abstract class Action implements MainJob, Serializable {

    @Setter
    protected String actionName = "Action Name";
    @Setter
    protected ActionTypes chosenActionPerform;
    protected transient BufferedImage displayImage;
    protected Rectangle mainImageBoundingBox;
    protected int attempt;
    protected int keyCode;
    protected boolean progressiveSearch;
    protected int progressiveSearchTime, waitBeforeTime, waitAfterTime;
    @Setter
    protected boolean required, previousPass;
    private final String className = this.getClass().getSimpleName();

    @Serial
    private static final long serialVersionUID = 1L;

    public void setActionOptions(int attempt, boolean progressive, int progressiveSearchTime, int beforeTime,
                                 int afterTime, ActionTypes actionTypes, BufferedImage displayImage,
                                 Rectangle boundingBox, int keyCode) {
        this.attempt = attempt;
        progressiveSearch = progressive;
        this.progressiveSearchTime = progressiveSearchTime;
        waitBeforeTime = beforeTime;
        waitAfterTime = afterTime;
        chosenActionPerform = actionTypes;
        this.displayImage = displayImage;
        mainImageBoundingBox = boundingBox;
        this.keyCode = keyCode;
    }

    public BufferedImage getMainDisplayImage() {
        return Condition.getImageWithEdges(mainImageBoundingBox, displayImage, 0.5f);
    }

    public BufferedImage getSeenImage() throws AWTException {
        BufferedImage fullImage = Condition.getFullImage(mainImageBoundingBox, displayImage);
        return Condition.getImageWithEdges(mainImageBoundingBox, fullImage, 0.5f);
    }

    public static Action getCorrespondAction(ActionTypes actionTypes) {
        return switch (actionTypes) {
            case MouseClick -> new ActionMouseClick();
            case MouseDoubleClick -> new ActionMouseDoubleClick();
            case KeyClick -> new ActionKeyClick();
            case KeyPress -> new ActionKeyPress();
            case KeyPressMouseClick -> new ActionKeyPressMouseClick();
        };
    }

    @Override
    public Action cloneData() {
        Action action = getCorrespondAction(chosenActionPerform);
        action.setActionName(actionName);
        action.setRequired(required);
        action.setPreviousPass(previousPass);
        action.setActionOptions(attempt, isProgressiveSearch(), progressiveSearchTime, waitBeforeTime, getWaitAfterTime(),
                chosenActionPerform, displayImage, mainImageBoundingBox, keyCode);
        return action;
    }

    public abstract void performAction();

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ImageSerialization.serializeBufferedImageWriteObject(out, displayImage);// Serialize displayImage
        AppScene.addLog(LogLevel.TRACE, className, "Serialized display image");
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String displayImageString = (String) in.readObject();
        displayImage = ImageSerialization.deserializeBufferedImageReadObject(in, displayImageString, false);
        AppScene.addLog(LogLevel.TRACE, className, "Deserialized display image");
    }
}
