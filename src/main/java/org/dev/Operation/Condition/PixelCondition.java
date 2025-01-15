package org.dev.Operation.Condition;

import lombok.Getter;
import lombok.Setter;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import org.dev.Enum.ReadingCondition;
import org.dev.Menu.ConditionPixelMenuController;
import org.dev.Operation.ImageSerialization;
import org.dev.RunOperation.RunningStatus;
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

    public PixelCondition(ReadingCondition chosenReadingCondition, BufferedImage mainImage,
                          Rectangle mainImageBoundingBox, boolean not, boolean required,
                          BufferedImage displayImage, boolean globalSearch) {
        super(chosenReadingCondition, mainImage, mainImageBoundingBox, not, required);
        this.displayImage = displayImage;
        this.globalSearch = globalSearch;
    }

    @Override
    public String getExpectedResult() { return ReadingCondition.Pixel.name(); }

    @Override
    public String getActualResult() { return readResult; }

    @Override
    public ImageCheckResult checkCondition() {
        try {
            ImageCheckResult imageResult = (globalSearch) ? checkPixelFromCurrentScreen(mainImage)
                    : checkPixelFromBoundingBox(mainImageBoundingBox, mainImage);
            readResult = imageResult.getReadResult();
            if (not)
                imageResult.setPass(!imageResult.isPass());
            return imageResult;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Error checking pixel condition: " + e.getMessage());
            return null;
        }
    }

    private ImageCheckResult checkPixelFromBoundingBox(Rectangle boundingBox, BufferedImage img2) throws AWTException {
        BufferedImage img1 = ConditionPixelMenuController.captureCurrentScreen(boundingBox);
        BufferedImage seenImageWithEdges = getFullImage(boundingBox, displayImage);

//        ImageIO.write(img1, "png", new File("img1.png"));
//        ImageIO.write(img2, "png", new File("img2.png"));
        DataBuffer db1 = img1.getRaster().getDataBuffer();
        DataBuffer db2 = img2.getRaster().getDataBuffer();
        int size1 = db1.getSize();
        int size2 = db2.getSize();

        AppScene.addLog(LogLevel.TRACE, className, "Img1 type: " + img1.getType() + " | Img2 type: " + img2.getType());
        AppScene.addLog(LogLevel.TRACE, className, "Img1 color model: " + img1.getColorModel() + " | Img2 color model: " + img2.getColorModel());
        AppScene.addLog(LogLevel.TRACE, className, "Img1 width: " + img1.getWidth() + " | Img2 width: " + img2.getWidth());
        AppScene.addLog(LogLevel.TRACE, className, "Img1 height: " + img1.getHeight() + " | Img2 height: " + img2.getHeight());
        AppScene.addLog(LogLevel.TRACE, className, "Img1 size: " + size1 + " | Img2 size: " + size2);

        boolean pass = true;
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight())
            pass = false;
        else if (size1 != size2)
            pass = false;
        for (int i = 0; i < size1; i++)
            if (db1.getElem(i) != db2.getElem(i))
                pass = false;
        RunningStatus readResult = pass ? RunningStatus.Passed : RunningStatus.Failed;
        return new ImageCheckResult(readResult.name(), createImageWithEdges(boundingBox, seenImageWithEdges), pass);
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
        ImageSerialization.serializeBufferedImageWriteObject(out, mainImage);
        ImageSerialization.serializeBufferedImageWriteObject(out, displayImage);
        AppScene.addLog(LogLevel.TRACE, className, "Serialized main image and display image");
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String mainImageString = (String) in.readObject();
        mainImage = ImageSerialization.deserializeBufferedImageReadObject(in, mainImageString, true);
        String displayImageString = (String) in.readObject();
        displayImage = ImageSerialization.deserializeBufferedImageReadObject(in, displayImageString, true);
        AppScene.addLog(LogLevel.TRACE, className, "Deserialized main image and display image");
    }
}
