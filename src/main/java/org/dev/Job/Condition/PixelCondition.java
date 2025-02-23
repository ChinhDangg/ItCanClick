package org.dev.Job.Condition;

import lombok.Getter;
import lombok.Setter;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import org.dev.Enum.ReadingCondition;
import org.dev.Menu.ConditionPixelMenuController;
import org.dev.RunJob.RunningStatus;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;

@Getter @Setter
public class PixelCondition extends Condition {

    private boolean exactSearch;
    private boolean subImageSearch;
    private boolean globalSearch;
    private transient final String className = this.getClass().getSimpleName();

    @Serial
    private static final long serialVersionUID = 1L;

    public PixelCondition(ReadingCondition chosenReadingCondition, BufferedImage displayImage,
                          Rectangle mainImageBoundingBox, boolean not, boolean required,
                          boolean exactSearch, boolean subImageSearch, boolean globalSearch) {
        super(chosenReadingCondition, displayImage, mainImageBoundingBox, not, required);
        this.exactSearch = exactSearch;
        this.subImageSearch = subImageSearch;
        this.globalSearch = globalSearch;
    }

    @Override
    public PixelCondition cloneData() {
        return new PixelCondition(chosenReadingCondition, displayImage, mainImageBoundingBox, not, required,
                exactSearch, subImageSearch, globalSearch);
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
            ImageCheckResult imageResult = (globalSearch) ? checkPixelFromCurrentScreen(mainImageBoundingBox, displayImage)
                    : (subImageSearch) ? checkPixelWithinBoundingBox(mainImageBoundingBox, displayImage)
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

    private ImageCheckResult checkPixelFromBoundingBox(Rectangle boundingBox, BufferedImage fullSaved) throws AWTException {
        int fullImageWidth = fullSaved.getWidth(), fullImageHeight = fullSaved.getHeight();
        int difX = (fullImageWidth - boundingBox.width)/2;
        int difY = (fullImageHeight - boundingBox.height)/2;
        Rectangle fullBounding = new Rectangle(boundingBox.x - difX, boundingBox.y - difY, fullImageWidth, fullImageHeight);
        BufferedImage fullSeen = ConditionPixelMenuController.captureCurrentScreen(fullBounding);

        BufferedImage seen = fullSeen.getSubimage(difX, difY, boundingBox.width, boundingBox.height);
        BufferedImage saved = fullSaved.getSubimage(difX, difY, boundingBox.width, boundingBox.height);
        BufferedImage seenImageWithEdges = getFullImage(boundingBox, fullSaved);

//        ImageIO.write(seen, "png", new File("seen.png"));
//        ImageIO.write(saved, "png", new File("saved.png"));

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
        RunningStatus readResult = pass ? RunningStatus.Passed : RunningStatus.Failed;
        return new ImageCheckResult(readResult.name(), getImageWithEdges(boundingBox, seenImageWithEdges, 0.5f), pass);
    }

    private ImageCheckResult checkPixelWithinBoundingBox(Rectangle boundingBox, BufferedImage fullSaved) throws AWTException {
        int fullImageWidth = fullSaved.getWidth(), fullImageHeight = fullSaved.getHeight();
        int difX = (fullImageWidth - boundingBox.width)/2;
        int difY = (fullImageHeight - boundingBox.height)/2;
        Rectangle fullBounding = new Rectangle(boundingBox.x - difX, boundingBox.y - difY, fullImageWidth, fullImageHeight);
        BufferedImage fullSeen = ConditionPixelMenuController.captureCurrentScreen(fullBounding);

        BufferedImage smallSaved = fullSaved.getSubimage(difX, difY, boundingBox.width, boundingBox.height);

        // Convert BufferedImage to Mat
        Mat bigMat = bufferedImageToMat(fullSeen);
        Mat smallMat = bufferedImageToMat(smallSaved);

        // Perform template matching
        Mat result = new Mat();
        opencv_imgproc.matchTemplate(bigMat, smallMat, result, opencv_imgproc.TM_CCOEFF_NORMED);

        // Find best match score
        DoublePointer minVal = new DoublePointer(1);
        DoublePointer maxVal = new DoublePointer(1);
        org.bytedeco.opencv.opencv_core.Point minLoc = new org.bytedeco.opencv.opencv_core.Point();
        org.bytedeco.opencv.opencv_core.Point maxLoc = new Point();

        opencv_core.minMaxLoc(result, minVal, maxVal, minLoc, maxLoc, null);

        String matchScore = String.format("Match Score: %.2f and Match Loc: %s,%s", maxVal.get(), maxLoc.x(), maxLoc.y());
        AppScene.addLog(LogLevel.TRACE, className, matchScore);

        return new ImageCheckResult(matchScore, getImageWithEdges(maxLoc.x(), maxLoc.y(), boundingBox, fullSeen, 0.5f), (maxVal.get() >= 0.9));
    }

    // check pixel in entire screen
    private ImageCheckResult checkPixelFromCurrentScreen(Rectangle boundingBox, BufferedImage fullSaved) throws AWTException {
        int fullImageWidth = fullSaved.getWidth(), fullImageHeight = fullSaved.getHeight();
        int difX = (fullImageWidth - boundingBox.width)/2;
        int difY = (fullImageHeight - boundingBox.height)/2;
        BufferedImage smallSaved = fullSaved.getSubimage(difX, difY, boundingBox.width, boundingBox.height);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        BufferedImage currentScreen = new Robot().createScreenCapture(new Rectangle(0, 0, screenSize.width-1, screenSize.height-1));

        // Convert BufferedImage to Mat
        Mat bigMat = bufferedImageToMat(currentScreen);
        Mat smallMat = bufferedImageToMat(smallSaved);

        // Perform template matching
        Mat result = new Mat();
        opencv_imgproc.matchTemplate(bigMat, smallMat, result, opencv_imgproc.TM_CCOEFF_NORMED);

        // Find best match score
        DoublePointer minVal = new DoublePointer(1);
        DoublePointer maxVal = new DoublePointer(1);
        org.bytedeco.opencv.opencv_core.Point minLoc = new org.bytedeco.opencv.opencv_core.Point();
        org.bytedeco.opencv.opencv_core.Point maxLoc = new Point();

        opencv_core.minMaxLoc(result, minVal, maxVal, minLoc, maxLoc, null);

        String matchScore = String.format("Match Score: %.2f and Match Loc: %s,%s", maxVal.get(), maxLoc.x(), maxLoc.y());
        AppScene.addLog(LogLevel.TRACE, className, matchScore);

        return new ImageCheckResult(matchScore, getImageWithEdges(maxLoc.x(), maxLoc.y(), boundingBox, currentScreen, 0.5f), (maxVal.get() >= 0.9));
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
