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
        int bigXMax = biggerImage.getWidth() - smallerImage.getWidth();
        int bigYMax = biggerImage.getHeight() - smallerImage.getHeight();
        bigYMax = (bigYMax == 0) ? biggerImage.getHeight() : bigYMax;
        bigXMax = (bigXMax == 0) ? biggerImage.getWidth() : bigXMax;

        while (bigY < bigYMax) {
            boolean pass = checkSubImage(bigX, bigY, biggerImage, smallerImage);
            if (!pass) {
                bigX++;
                if (bigX >= bigXMax) {
                    bigX = 0;
                    bigY++;
                }
                System.out.println(bigX + " " + bigY);
            }
            else
                return true;
        }
        return false;
    }

    private static boolean checkSubImage(int xStart, int yStart, BufferedImage bigImage, BufferedImage smallImage) {
        int yEnd = yStart + smallImage.getHeight();
        int xEnd = xStart + smallImage.getWidth();
        int smallX = 0, smallY = 0;

        try {
            while (yStart < yEnd) {
                if (smallImage.getRGB(smallX, smallY) == bigImage.getRGB(xStart, yStart)) {
                    smallX++;
                    if (smallX >= smallImage.getWidth()) {
                        smallX = 0;
                        smallY++;
                        if (smallY >= smallImage.getHeight())
                            return true;
                    }
                } else
                    return false;
                xStart++;
                if (xStart >= xEnd) {
                    xStart = 0;
                    yStart++;
                }
            }
        } catch (Exception e) {
            System.out.println(bigImage.getWidth() + " " + bigImage.getHeight());
            System.out.println(xStart + " " + yStart);
            System.out.println(xEnd + " " + yEnd);
            e.printStackTrace();
            System.exit(1);
        }

        return true;
    }

}
