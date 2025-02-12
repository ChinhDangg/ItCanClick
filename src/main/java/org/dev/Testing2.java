package org.dev;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Testing2 {


    public static void main(String[] args) throws AWTException, IOException {
//        BufferedImage img = new Robot().createScreenCapture(new Rectangle(60+20, 60+20, 100-50, 100-50));
//        ImageIO.write(img, "png", new File("small.png"));

//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        BufferedImage currentScreen = new Robot().createScreenCapture(new Rectangle(0, 0, screenSize.width-1, screenSize.height-1));
//        System.out.println(currentScreen.getType());

        BufferedImage small = ImageIO.read(new File("small.png"));
        BufferedImage big = ImageIO.read(new File("big.png"));


        System.out.println(small.getType());
        System.out.println(big.getType());
        System.out.println(checkPixelFromCurrentScreen(small, big));
    }

    public static boolean checkPixelFromCurrentScreen(BufferedImage smallerImage, BufferedImage biggerImage) {
        int bigX = 0, bigY = 0;
        int smallWidth = smallerImage.getWidth(), smallHeight = smallerImage.getHeight();
        int bigWidth = biggerImage.getWidth(), bigHeight = biggerImage.getHeight();

        while (bigY < bigHeight) {
            boolean pass = checkSubImage(bigX, bigY, biggerImage, smallerImage);
            if (!pass) {
                bigX += smallWidth;
                if (bigX >= bigWidth) {
                    bigX = 0;
                    bigY += smallHeight;
                }
            }
            else
                return true;
        }
        return false;
    }

    private static boolean checkSubImage(int xStart, int yStart, BufferedImage bigImage, BufferedImage smallImage) {
        int yEnd = yStart + smallImage.getHeight();
        int smallX = 0, smallY = 0;

        while (yStart < yEnd) {
            if (smallImage.getRGB(smallX, smallY) == bigImage.getRGB(xStart, yStart)) {
                smallX++;
                if (smallX == smallImage.getWidth()) {
                    smallX = 0;
                    smallY++;
                    if (smallY == smallImage.getHeight())
                        return true;
                }
            } else
                return false;
            xStart++;
            if (xStart == bigImage.getWidth()) {
                xStart = 0;
                yStart++;
            }
        }

        return true;
    }

}
