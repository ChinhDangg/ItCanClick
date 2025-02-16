package org.dev.Job.Condition;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.dev.Enum.ConditionType;
import org.dev.Enum.ReadingCondition;
import org.dev.Job.MainJob;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

@Getter @Setter
public abstract class Condition implements MainJob, Serializable {
    protected ConditionType conditionType;
    protected ReadingCondition chosenReadingCondition;
    protected transient BufferedImage displayImage;
    protected Rectangle mainImageBoundingBox;
    protected boolean not;
    protected boolean required;

    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    protected transient String readResult;

    @Serial
    private static final long serialVersionUID = 1L;

    public Condition(ReadingCondition chosenReadingCondition, BufferedImage displayImage, Rectangle mainImageBoundingBox,
                     boolean not, boolean required) {
        this.chosenReadingCondition = chosenReadingCondition;
        this.displayImage = displayImage;
        this.mainImageBoundingBox = mainImageBoundingBox;
        this.not = not;
        this.required = required;
    }

    @Override
    public Condition cloneData() { return null; }

    public abstract BufferedImage getMainDisplayImage();
    public abstract ImageCheckResult checkCondition();
    public abstract String getExpectedResult();
    public abstract String getActualResult();

    public static BufferedImage getFullImage(Rectangle innerBoundingBox, BufferedImage previousFullImage) throws AWTException {
        int width = previousFullImage.getWidth(), height = previousFullImage.getHeight();
        return new Robot().createScreenCapture(new Rectangle(innerBoundingBox.x - (width - innerBoundingBox.width)/2
                , innerBoundingBox.y - (height - innerBoundingBox.height)/2, width, height));
    }

    public static BufferedImage getImageWithEdges(Rectangle innerBoundingBox, BufferedImage fullImage, float opacity) {
        int fullWidth = fullImage.getWidth(), fullHeight = fullImage.getHeight();
        int innerWidth = innerBoundingBox.width, innerHeight = innerBoundingBox.height;
        if (innerWidth == fullWidth && innerHeight == fullHeight)
            return fullImage;
        int innerX = (fullWidth - innerWidth) / 2, innerY = (fullHeight - innerHeight) / 2;
        BufferedImage innerImage = fullImage.getSubimage(innerX, innerY, innerWidth, innerHeight);
        BufferedImage completeImage = new BufferedImage(fullWidth, fullHeight, fullImage.getType());
        Graphics2D g = completeImage.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        g.drawImage(fullImage, 0, 0, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g.drawImage(innerImage, innerX, innerY, null);
        g.dispose();
        return completeImage;
    }

    public static BufferedImage getImageWithEdges(BufferedImage seenImage, BufferedImage fullImage, float opacity) {
        int fullWidth = fullImage.getWidth(), fullHeight = fullImage.getHeight();
        int innerWidth = seenImage.getWidth(), innerHeight = seenImage.getHeight();
        if (innerWidth == fullWidth && innerHeight == fullHeight)
            return fullImage;
        BufferedImage completeImage = new BufferedImage(fullWidth, fullHeight, fullImage.getType());
        Graphics2D g = completeImage.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        g.drawImage(fullImage, 0, 0, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g.drawImage(seenImage, (fullWidth - innerWidth)/2, (fullHeight - innerHeight)/2, null);
        g.dispose();
        return completeImage;
    }

    public static BufferedImage getScaledImage(BufferedImage image, double scaleValue) {
        int w = (int) ((double) image.getWidth() * scaleValue);
        int h = (int) ((double) image.getHeight() * scaleValue);
        BufferedImage tempImage = new BufferedImage(w, h, image.getType());
        Graphics2D g = tempImage.createGraphics();
        g.drawImage(image, 0,0, w, h,null);
        g.dispose();
        return tempImage;
    }
}
