package org.dev;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_core.Point;
import static org.bytedeco.opencv.global.opencv_core.*;




import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

public class Testing2 {

    public static void main(String[] args) throws Exception {
        String bigImagePath = "C:\\Users\\admin\\IdeaProjects\\SmartClick\\testImageBig.png";
        String smallImagePath = "C:\\Users\\admin\\IdeaProjects\\SmartClick\\testImageSmall.png";

//        boolean found = containsImage(bigImagePath, smallImagePath);
//        System.out.println("Small image found in big image: " + found);
        record();
    }

    public static void record() throws AWTException, IOException {
        BufferedImage big = new Robot().createScreenCapture(new Rectangle(60, 150, 100, 100));

        BufferedImage small = new Robot().createScreenCapture(new Rectangle(160, 250, 100, 100));

        System.out.println(containsImage(big, small));

//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        BufferedImage currentScreen = new Robot().createScreenCapture(new Rectangle(0, 0, screenSize.width-1, screenSize.height-1));
//        System.out.println(currentScreen.getType());
    }

    public static boolean containsImage(String bigImagePath, String smallImagePath) {
        // Load OpenCV native libraries
        Loader.load(opencv_core.class);

        // Read images
        Mat bigImage = imread(bigImagePath);
        Mat smallImage = imread(smallImagePath);

        if (bigImage.empty() || smallImage.empty()) {
            System.out.println("Failed to load one or both images!");
            return false;
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

        // Return true if the match score is high
        return maxVal.get() >= 0.9;
    }

    public static boolean containsImage(BufferedImage bigImage, BufferedImage smallImage) {
        // Convert BufferedImage to Mat
        Mat bigMat = bufferedImageToMat(bigImage);
        Mat smallMat = bufferedImageToMat(smallImage);

        // Check if images are valid
        if (bigMat.empty() || smallMat.empty()) {
            System.out.println("Error: One or both images are empty!");
            return false;
        }

        // Perform template matching
        Mat result = new Mat();
        opencv_imgproc.matchTemplate(bigMat, smallMat, result, opencv_imgproc.TM_CCOEFF_NORMED);

        // Find best match score
        DoublePointer minVal = new DoublePointer(1);
        DoublePointer maxVal = new DoublePointer(1);
        Point minLoc = new Point();
        Point maxLoc = new Point();

        opencv_core.minMaxLoc(result, minVal, maxVal, minLoc, maxLoc, null);

        // Print match score
        System.out.println("Match Score: " + maxVal.get());

        // Return true if similarity is high enough
        return maxVal.get() >= 0.9;
    }

    public static Mat bufferedImageToMat(BufferedImage image) {
        // Check if the BufferedImage is already in a compatible format
        // javacv probably use 3byte-bgr format for buffered image
        if (image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = convertedImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            image = convertedImage; // Replace with converted image
        }

        // Extract pixel data as byte array
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        // Create OpenCV Mat with correct format
        Mat mat = new Mat(image.getHeight(), image.getWidth(), opencv_core.CV_8UC3);
        mat.data().put(pixels);

        return mat;
    }
}
