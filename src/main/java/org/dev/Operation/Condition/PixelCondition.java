package org.dev.Operation.Condition;

import lombok.Getter;
import lombok.Setter;
import org.dev.Enum.ReadingCondition;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;

@Getter
@Setter
public class PixelCondition extends Condition {

    private BufferedImage displayImage;
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
            boolean pass = (globalSearch) ? checkPixelFromCurrentScreen(mainImageBoundingBox, mainImage)
                    : checkPixelFromBoundingBox(mainImageBoundingBox, mainImage);
            if (not)
                return !pass;
            return pass;
        } catch (Exception e) {
            System.out.println("Fail checking pixel condition");
        }
        return false;
    }

    private boolean checkPixelFromBoundingBox(Rectangle boundingBox, BufferedImage img2) throws AWTException {
        BufferedImage img1 = new Robot().createScreenCapture(boundingBox);
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

    private boolean checkPixelFromCurrentScreen(Rectangle boundingBox, BufferedImage img2) throws AWTException, IOException {
        BufferedImage currentScreen = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
//        File file = new File("screenshot.png");
//        ImageIO.write(currentScreen, "png", file);

        int xx = boundingBox.x;
        int yy = boundingBox.y;
        System.out.println(boundingBox.width == img2.getWidth());
        System.out.println(boundingBox.height == img2.getHeight());
        for (int j = 0; j < boundingBox.width; j++) {
            int c = currentScreen.getRGB(xx + j, yy);
            int i = img2.getRGB(j, 0);
            System.out.println(c + " | " + i + " " + (c == i));
        }
        System.out.println("++++++++++++");

        int heightDif = currentScreen.getHeight() - img2.getHeight();
        int widthDif = currentScreen.getWidth() - img2.getWidth();
        for (int y = 0; y < heightDif; y++) {
            for (int x = 0; x < widthDif; x++) {
                if (isSubImage(currentScreen, img2, x, y)) {
                    System.out.println("Found " + x + " " + y);
                    return true;
                }
            }
        }
        return false;
    }
    private boolean isSubImage(BufferedImage img1, BufferedImage img2, int startX, int startY) {
        int height = img2.getHeight();
        int width = img2.getWidth();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (img1.getRGB(startX + x, startY + y) != img2.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }
}
