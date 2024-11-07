package org.dev.Operation.Condition;

import lombok.Getter;
import lombok.Setter;
import org.dev.Enum.ReadingCondition;
import org.dev.ImageSerialization;
import org.dev.RunOperation.RunningStatus;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.*;
import java.util.Base64;

@Getter @Setter
public class PixelCondition extends Condition {

    private transient BufferedImage displayImage;
    private boolean globalSearch;

    public PixelCondition(ReadingCondition chosenReadingCondition, BufferedImage mainImage,
                          Rectangle mainImageBoundingBox, boolean not, boolean required,
                          BufferedImage displayImage, boolean globalSearch) {
        super(chosenReadingCondition, mainImage, mainImageBoundingBox, not, required);
        this.displayImage = displayImage;
        this.globalSearch = globalSearch;
    }

    @Override
    public BufferedImage getMainDisplayImage() { return displayImage; }

    @Override
    public boolean checkCondition() {
        try {
            boolean pass = (globalSearch) ? checkPixelFromCurrentScreen(mainImage)
                    : checkPixelFromBoundingBox(mainImageBoundingBox, mainImage);
            readResult = pass ? RunningStatus.Passed.name() : RunningStatus.Failed.name();
            if (not)
                return !pass;
            return pass;
        } catch (Exception e) {
            System.out.println("Fail checking pixel condition");
        }
        return false;
    }

    @Override
    public String getExpectedResult() {
        return ReadingCondition.Pixel.name();
    }

    @Override
    public String getActualResult() {
        return readResult;
    }

    private boolean checkPixelFromBoundingBox(Rectangle boundingBox, BufferedImage img2) throws AWTException, IOException {
        BufferedImage img1 = new Robot().createScreenCapture(boundingBox);
//        ImageIO.write(img1, "png", new File("img1.png"));
//        ImageIO.write(img2, "png", new File("img2.png"));
//
//        System.out.println(img2.getType()); //BGR
//        System.out.println(img1.getType()); //RGB
//        System.out.println(img2.getColorModel());
//        System.out.println(img1.getColorModel());

        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight())
            return false;
        DataBuffer db1 = img1.getRaster().getDataBuffer();
        DataBuffer db2 = img2.getRaster().getDataBuffer();
        int size1 = db1.getSize();
        int size2 = db2.getSize();
        if (size1 != size2)
            return false;
        for (int i = 0; i < size1; i++)
            if (db1.getElem(i) != db2.getElem(i))
                return false;
        return true;
    }

    private boolean checkPixelFromCurrentScreen(BufferedImage img2) throws AWTException {
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
        ImageSerialization.serializeBufferedImageWriteObject(out, mainImage);   // Serialize mainImage
        ImageSerialization.serializeBufferedImageWriteObject(out, displayImage);// Serialize displayImage
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String mainImageString = (String) in.readObject();
        mainImage = ImageSerialization.deserializeBufferedImageReadObject(in, mainImageString, true);        // Deserialize mainImage
        String displayImageString = (String) in.readObject();
        displayImage = ImageSerialization.deserializeBufferedImageReadObject(in, displayImageString, true);  // Deserialize mainImage
    }
}
