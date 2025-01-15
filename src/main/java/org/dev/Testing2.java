package org.dev;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Testing2 {


    public static void main(String[] args) throws AWTException, IOException {
//        BufferedImage img = new Robot().createScreenCapture(new Rectangle(60, 60, 50, 50));
//        ImageIO.write(img, "png", new File("test2.png"));

//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        BufferedImage currentScreen = new Robot().createScreenCapture(new Rectangle(0, 0, screenSize.width-1, screenSize.height-1));
//        System.out.println(currentScreen.getType());


        BufferedImage big = ImageIO.read(new File("C:\\Users\\admin\\IdeaProjects\\SmartClick\\Screenshot 2025-01-14 161020.png"));
        System.out.println(big.getType());
        BufferedImage small = ImageIO.read(new File("C:\\Users\\admin\\IdeaProjects\\SmartClick\\Screenshot 2025-01-14 161043.png"));
        System.out.println(small.getType());
        System.out.println(checkPixelFromCurrentScreen(small, big));
    }

    public static boolean checkPixelFromCurrentScreen(BufferedImage smallerImage, BufferedImage biggerImage) throws AWTException {
        int smallX = 0, smallY = 0;
        int bigX = 0, bigY = 0;
        int smallWidth = smallerImage.getWidth()-1, smallHeight = smallerImage.getHeight()-1;
        int bigWidth = biggerImage.getWidth()-1, bigHeight = biggerImage.getHeight()-1;

        while (bigY <= bigHeight) {
            if (smallerImage.getRGB(smallX, smallY) == biggerImage.getRGB(bigX, bigY)) {
                smallX++;
                if (smallX == smallWidth) {
                    smallX = 0;
                    smallY++;
                    if (smallY == smallHeight)
                        return true;
                }
                System.out.println(smallX + " " + smallY);
            }
            else {
                smallX = 0;
                smallY = 0;
            }
            bigX++;
            if (bigX == bigWidth) {
                bigX = 0;
                bigY++;
            }
        }
        return false;
    }
}
