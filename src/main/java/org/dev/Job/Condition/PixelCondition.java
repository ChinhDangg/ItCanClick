package org.dev.Job.Condition;

import lombok.Getter;
import lombok.Setter;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import org.dev.Enum.ReadingCondition;
import org.dev.Menu.ConditionPixelMenuController;
import org.dev.Job.ImageSerialization;
import org.dev.RunJob.RunningStatus;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
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

    private ImageCheckResult checkPixelFromBoundingBox(Rectangle boundingBox, BufferedImage fullImage) throws AWTException {
        BufferedImage seen = ConditionPixelMenuController.captureCurrentScreen(boundingBox);
        int x = (fullImage.getWidth()-boundingBox.width)/2, y = (fullImage.getHeight()-boundingBox.height)/2;
        BufferedImage saved = fullImage.getSubimage(x, y, boundingBox.width, boundingBox.height);
        BufferedImage seenImageWithEdges = getFullImage(boundingBox, fullImage);

//        ImageIO.write(seen, "png", new File("seen.png"));
//        ImageIO.write(saved, "png", new File("saved.png"));
        DataBuffer db1 = seen.getRaster().getDataBuffer();
        DataBuffer db2 = saved.getRaster().getDataBuffer();
        int size1 = db1.getSize();
        int size2 = db2.getSize();

        AppScene.addLog(LogLevel.TRACE, className, "Img1 type: " + seen.getType() + " | Img2 type: " + saved.getType());
        AppScene.addLog(LogLevel.TRACE, className, "Img1 color model: " + seen.getColorModel() + " | Img2 color model: " + saved.getColorModel());
        AppScene.addLog(LogLevel.TRACE, className, "Img1 width: " + seen.getWidth() + " | Img2 width: " + saved.getWidth());
        AppScene.addLog(LogLevel.TRACE, className, "Img1 height: " + seen.getHeight() + " | Img2 height: " + saved.getHeight());
        AppScene.addLog(LogLevel.TRACE, className, "Img1 size: " + size1 + " | Img2 size: " + size2);

        boolean pass = true;
        if (seen.getWidth() != saved.getWidth() || seen.getHeight() != saved.getHeight())
            pass = false;
        else if (size1 != size2)
            pass = false;
        for (int i = 0; i < size1; i++)
            if (db1.getElem(i) != db2.getElem(i))
                pass = false;
        RunningStatus readResult = pass ? RunningStatus.Passed : RunningStatus.Failed;
        return new ImageCheckResult(readResult.name(), getImageWithEdges(boundingBox, seenImageWithEdges, 0.5f), pass);
    }

    // TODO : check pixel in entire screen
    private ImageCheckResult checkPixelFromCurrentScreen(BufferedImage img2) {
        return new ImageCheckResult(RunningStatus.Failed.name(), null, false);
    }
    private boolean checkPixelFromCurrentScreen(BufferedImage img2, BufferedImage NOT_USED) throws AWTException {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        BufferedImage currentScreen = new Robot().createScreenCapture(new Rectangle(0, 0, screenSize.width-1, screenSize.height-1));
        int heightDif = currentScreen.getHeight() - img2.getHeight();
        int widthDif = currentScreen.getWidth() - img2.getWidth();
        for (int y = 0; y < heightDif; y++)
            for (int x = 0; x < widthDif; x++)
                if (isSubImage(currentScreen, img2, x, y)) {
                    System.out.println("Found " + x + " " + y);
                    return true;
                }
        return false;
    }
    private boolean isSubImage(BufferedImage img1, BufferedImage img2, int startX, int startY) {
        int height = img2.getHeight();
        int width = img2.getWidth();
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                if (img1.getRGB(startX + x, startY + y) != img2.getRGB(x, y))
                    return false;
                else
                    System.out.println((startX + x) + " " + (startY + y));
        return true;
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
