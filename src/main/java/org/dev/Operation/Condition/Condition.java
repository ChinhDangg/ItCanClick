package org.dev.Operation.Condition;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.dev.Enum.ReadingCondition;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

@Getter @Setter
public abstract class Condition implements Serializable {
    protected ReadingCondition chosenReadingCondition;
    protected transient BufferedImage mainImage;
    protected Rectangle mainImageBoundingBox;
    protected boolean not;
    protected boolean required;
    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    protected String readResult;

    @Serial
    private static final long serialVersionUID = 1L;

    public Condition(ReadingCondition chosenReadingCondition, BufferedImage mainImage, Rectangle mainImageBoundingBox,
                     boolean not, boolean required) {
        this.chosenReadingCondition = chosenReadingCondition;
        this.mainImage = mainImage;
        this.mainImageBoundingBox = mainImageBoundingBox;
        this.not = not;
        this.required = required;
    }
    public BufferedImage getMainDisplayImage() {
        return mainImage;
    }
    public abstract ImageCheckResult checkCondition();

    public abstract String getExpectedResult();
    public abstract String getActualResult();

    // will return wrong image if display Image has the same or smaller width and height then bounding box
    protected BufferedImage getImageWithEdges(Rectangle innerBoundingBox, BufferedImage fullImage) throws AWTException {
        int width = fullImage.getWidth(), height = fullImage.getHeight();
        return new Robot().createScreenCapture(new Rectangle(innerBoundingBox.x-width/2, innerBoundingBox.y-height/2, width, height));
    }

    protected BufferedImage createImageWithEdges(BufferedImage seenImage, BufferedImage seenImageWithEdges) {
        int width = seenImageWithEdges.getWidth(), height = seenImageWithEdges.getHeight();
        if (seenImage.getWidth() <= width && seenImage.getHeight() <= height)
            return seenImage;
        BufferedImage completeImage = new BufferedImage(width, height, seenImageWithEdges.getType());
        Graphics2D g = completeImage.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g.drawImage(seenImageWithEdges, 0, 0, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g.drawImage(seenImage, width/2, height/2, null);
        g.dispose();
        return completeImage;
    }
}
