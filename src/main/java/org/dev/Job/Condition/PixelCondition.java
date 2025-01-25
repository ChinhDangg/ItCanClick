package org.dev.Job.Condition;

import lombok.Getter;
import lombok.Setter;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import org.dev.Enum.ReadingCondition;
import org.dev.Menu.ConditionPixelMenuController;
import org.dev.Job.ImageSerialization;
import org.dev.RunJob.RunningStatus;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

@Getter @Setter
public class PixelCondition extends Condition {
    private boolean globalSearch;
    private transient final String className = this.getClass().getSimpleName();

    @Serial
    private static final long serialVersionUID = 1L;

    public PixelCondition(ReadingCondition chosenReadingCondition, BufferedImage displayImage,
                          Rectangle mainImageBoundingBox, boolean not, boolean required, boolean globalSearch) {
        super(chosenReadingCondition, displayImage, mainImageBoundingBox, not, required);
        this.globalSearch = globalSearch;
    }

    @Override
    public PixelCondition getDeepCopied() {
        return new PixelCondition(chosenReadingCondition, displayImage, mainImageBoundingBox, not, required, globalSearch);
    }

    @Override
    public BufferedImage getMainDisplayImage() {
        BufferedImage box = drawBox(mainImageBoundingBox.width, mainImageBoundingBox.height, Color.BLACK);
        return getImageWithEdges(box, displayImage, 1.0f);
    }

    @Override
    public String getExpectedResult() { return ReadingCondition.Pixel.name(); }

    @Override
    public String getActualResult() { return readResult; }

    @Override
    public ImageCheckResult checkCondition() {
        try {
            ImageCheckResult imageResult = (globalSearch) ? checkPixelFromCurrentScreen(displayImage)
                    : checkPixelFromBoundingBox(mainImageBoundingBox, displayImage);
            readResult = imageResult.getReadResult();
            if (not)
                imageResult.setPass(!imageResult.isPass());
            return imageResult;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error checking pixel condition: " + e.getMessage());
            return null;
        }
    }

    public static BufferedImage drawBox(int width, int height, Color color) {
        BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = temp.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return temp;
    }

    private ImageCheckResult checkPixelFromBoundingBox(Rectangle boundingBox, BufferedImage fullImage) throws AWTException, IOException {
        BufferedImage seen = ConditionPixelMenuController.captureCurrentScreen(boundingBox);

        int x = (fullImage.getWidth()-boundingBox.width)/2, y = (fullImage.getHeight()-boundingBox.height)/2;
        BufferedImage saved = fullImage.getSubimage(x, y, boundingBox.width, boundingBox.height);
        BufferedImage seenImageWithEdges = getFullImage(boundingBox, fullImage);

        ImageIO.write(seen, "png", new File("seen.png"));
        ImageIO.write(saved, "png", new File("saved.png"));

        AppScene.addLog(LogLevel.TRACE, className, "Img1 type: " + seen.getType() + " | Img2 type: " + saved.getType());
        AppScene.addLog(LogLevel.TRACE, className, "Img1 color model: " + seen.getColorModel() + " | Img2 color model: " + saved.getColorModel());
        AppScene.addLog(LogLevel.TRACE, className, "Img1 width: " + seen.getWidth() + " | Img2 width: " + saved.getWidth());
        AppScene.addLog(LogLevel.TRACE, className, "Img1 height: " + seen.getHeight() + " | Img2 height: " + saved.getHeight());

        boolean pass = true;
        int seenWidth = seen.getWidth(), seenHeight = seen.getHeight();
        for (int h = 0; h < seenHeight; h++)
            for (int w = 0; w < seenWidth; w++)
                if (seen.getRGB(w, h) != saved.getRGB(w, h)) {
                    pass = false;
                    break;
                }
        System.out.println("Passed: " + pass);
        RunningStatus readResult = pass ? RunningStatus.Passed : RunningStatus.Failed;
        return new ImageCheckResult(readResult.name(), getImageWithEdges(boundingBox, seenImageWithEdges, 0.5f), pass);
    }

    // TODO : check pixel in entire screen
    private ImageCheckResult checkPixelFromCurrentScreen(BufferedImage img2) {
        return new ImageCheckResult(RunningStatus.Failed.name(), null, false);
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ImageSerialization.serializeBufferedImageWriteObject(out, displayImage);
        AppScene.addLog(LogLevel.TRACE, className, "Serialized display image");
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String displayImageString = (String) in.readObject();
        displayImage = ImageSerialization.deserializeBufferedImageReadObject(in, displayImageString, true);
        AppScene.addLog(LogLevel.TRACE, className, "Deserialized display image");
    }
}
