package org.dev.Operation.Condition;

import lombok.Getter;
import lombok.Setter;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import org.dev.Enum.ReadingCondition;
import org.dev.Operation.ImageSerialization;
import org.dev.Menu.ConditionTextMenuController;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Set;

@Getter @Setter
public class TextCondition extends Condition {

    private Set<String> savedText;
    private double currentTextScale;
    private transient final String className = this.getClass().getSimpleName();

    @Serial
    private static final long serialVersionUID = 1L;

    public TextCondition(ReadingCondition chosenReadingCondition, BufferedImage mainImage, Rectangle mainImageBoundingBox,
                         boolean not, boolean required, double textScale, Set<String> texts) {
        super(chosenReadingCondition, mainImage, mainImageBoundingBox, not, required);
        currentTextScale = textScale;
        savedText = texts;
    }

    @Override
    public String getExpectedResult() { return savedText.toString(); }

    @Override
    public String getActualResult() { return readResult; }

    @Override
    public ImageCheckResult checkCondition() {
        try {
            ImageCheckResult checkResult = readTextFromCurrentScreen(mainImageBoundingBox, getCurrentTextScale());
            readResult = checkResult.getReadResult();
            if (not)
                checkResult.setPass(!checkCondition().isPass());
            return checkResult;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail checking text condition");
            return null;
        }
    }

    private ImageCheckResult readTextFromCurrentScreen(Rectangle boundingBox, double scale) throws AWTException, TesseractException {
        BufferedImage seenImage = ConditionTextMenuController.captureCurrentScreen(boundingBox);
        BufferedImage seenImageWithEdges = createImageWithEdges(seenImage, getImageWithEdges(boundingBox, mainImage));
        if (scale != 1.00)
            seenImage = ConditionTextMenuController.getScaledImage(seenImage, scale);
        String readText = ConditionTextMenuController.readTextFromImage(seenImage).replace("\n", "");
        return new ImageCheckResult(readText, seenImageWithEdges, savedText.contains(readResult));
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ImageSerialization.serializeBufferedImageWriteObject(out, mainImage);
        AppScene.addLog(LogLevel.TRACE, className, "Serialized main image");
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String imageString = (String) in.readObject();
        mainImage = ImageSerialization.deserializeBufferedImageReadObject(in, imageString, false);
        AppScene.addLog(LogLevel.TRACE, className, "Deserialized main image");
    }
}
