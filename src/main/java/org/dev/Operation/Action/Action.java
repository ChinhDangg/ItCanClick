package org.dev.Operation.Action;

import lombok.Getter;
import lombok.Setter;
import org.dev.AppScene;
import org.dev.Enum.ActionTypes;
import org.dev.Enum.LogLevel;
import org.dev.Operation.Condition.Condition;
import org.dev.Operation.ImageSerialization;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

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
    private final String className = this.getClass().getSimpleName();

    @Serial
    private static final long serialVersionUID = 1L;

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

    public BufferedImage getSeenImage() throws AWTException {
        BufferedImage fullImage = Condition.getFullImage(mainImageBoundingBox, displayImage);
        return Condition.createImageWithEdges(mainImageBoundingBox, fullImage);
    }

    public abstract void performAction();

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ImageSerialization.serializeBufferedImageWriteObject(out, mainImage);   // Serialize mainImage
        ImageSerialization.serializeBufferedImageWriteObject(out, displayImage);// Serialize displayImage
        AppScene.addLog(LogLevel.TRACE, className, "Serialized main image and display image");
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String mainImageString = (String) in.readObject();
        mainImage = ImageSerialization.deserializeBufferedImageReadObject(in, mainImageString, false);
        String displayImageString = (String) in.readObject();
        displayImage = ImageSerialization.deserializeBufferedImageReadObject(in, displayImageString, false);
        AppScene.addLog(LogLevel.TRACE, className, "Deserialized main image and display image");
    }
}
