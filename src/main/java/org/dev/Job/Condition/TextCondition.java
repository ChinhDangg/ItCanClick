package org.dev.Job.Condition;

import lombok.Getter;
import lombok.Setter;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import org.dev.Enum.ReadingCondition;
import org.dev.Menu.ConditionTextMenuController;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Set;

@Getter @Setter
public class TextCondition extends Condition {

    private Set<String> savedText;
    private double currentTextScale;

    @Serial
    private static final long serialVersionUID = 1L;

    public TextCondition(ReadingCondition chosenReadingCondition, BufferedImage displayImage, Rectangle mainImageBoundingBox,
                         boolean not, boolean required, double textScale, Set<String> texts) {
        super(chosenReadingCondition, displayImage, mainImageBoundingBox, not, required);
        currentTextScale = textScale;
        savedText = texts;
    }

    @Override
    public TextCondition cloneData() {
        TextCondition condition = new TextCondition(chosenReadingCondition, displayImage, mainImageBoundingBox, not, required,
                currentTextScale, savedText);
        condition.setConditionType(conditionType);
        return condition;
    }

    @Override
    public BufferedImage getMainDisplayImage() { return getImageWithEdges(mainImageBoundingBox, displayImage, 0.5f); }

    @Override
    public String getExpectedResult() { return savedText.toString(); }

    @Override
    public ImageCheckResult checkCondition() {
        try {
            ImageCheckResult checkResult = readTextFromCurrentScreen(mainImageBoundingBox, getCurrentTextScale());
            if (not)
                checkResult.setPass(!checkCondition().isPass());
            return checkResult;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, this.getClass().getSimpleName(), "Error checking text condition: " + e.getMessage());
            return null;
        }
    }

    private ImageCheckResult readTextFromCurrentScreen(Rectangle boundingBox, double scale) throws AWTException, TesseractException {
        BufferedImage seenImage = ConditionTextMenuController.captureCurrentScreen(boundingBox);
        if (scale != 1.00)
            seenImage = getScaledImage(seenImage, scale);
        String readText = readTextFromImage(seenImage).replace("\n", "");
        BufferedImage seenImageWithEdges = getImageWithEdges(seenImage, getFullImage(boundingBox, displayImage), 0.5f);
        return new ImageCheckResult(readText, 100, boundingBox, seenImageWithEdges, checkTextInList(readText));
    }

    private boolean checkTextInList(String readText) {
        if (readText.isEmpty())
            return false;
        for (String s : savedText)
            if (s.contains(readText) || readText.contains(s))
                return true;
        return false;
    }

    public static String readTextFromImage(BufferedImage image) throws TesseractException {
        AppScene.addLog(LogLevel.DEBUG, TextCondition.class.getSimpleName(), "Reading text from an image");
        ITesseract tess = new Tesseract();
        //tess.setTessVariable("debug_file", "/dev/null");
        tess.setDatapath("tessdata");
        return tess.doOCR(image);
    }

}
