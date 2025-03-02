package org.dev;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_core.Point;

import javax.imageio.ImageIO;

import static org.bytedeco.opencv.global.opencv_core.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

public class Testing2 {

    public static void main(String[] args) throws Exception {
        String bigImagePath = "C:\\Users\\admin\\IdeaProjects\\ItCanClick\\testBig1.png";
        String smallImagePath = "C:\\Users\\admin\\IdeaProjects\\ItCanClick\\testSmall1.png";

        Point found = containsImage(bigImagePath, smallImagePath);
        if (found != null)
            System.out.println("Small image found in big image: " + found.x() + " " + found.y());
        else
            System.out.println("Small image not found in big image");
    }

    public static void record() throws AWTException, IOException {
        BufferedImage big = new Robot().createScreenCapture(new Rectangle(60, 150, 100, 100));
        ImageIO.write(big,  "png", new File("big1.png"));

//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        BufferedImage currentScreen = new Robot().createScreenCapture(new Rectangle(0, 0, screenSize.width-1, screenSize.height-1));
//        System.out.println(currentScreen.getType());
    }

    public static Point containsImage(String bigImagePath, String smallImagePath) {
        // Load OpenCV native libraries
        Loader.load(opencv_core.class);

        // Read images
        Mat bigImage = imread(bigImagePath);
        Mat smallImage = imread(smallImagePath);

        if (bigImage.empty() || smallImage.empty()) {
            System.out.println("Failed to load one or both images!");
            return null;
        }

        // Perform template matching
        Mat result = new Mat();
        opencv_imgproc.matchTemplate(bigImage, smallImage, result, opencv_imgproc.TM_CCOEFF_NORMED);

        // Find best match score
        DoublePointer minVal = new DoublePointer(1);
        DoublePointer maxVal = new DoublePointer(1);
        Point minLoc = new Point();
        Point maxLoc = new Point();

        minMaxLoc(result, minVal, maxVal, minLoc, maxLoc, null);

        // Print match score
        System.out.println("Match Score: " + maxVal.get());

        // Return maxLoc if the match score is high
        return (maxVal.get() >= 0.9) ? maxLoc : null;
    }
}
