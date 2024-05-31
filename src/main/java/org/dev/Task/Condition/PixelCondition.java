package org.dev.Task.Condition;

import lombok.Getter;
import lombok.Setter;
import org.dev.Enum.ReadingCondition;

import java.awt.*;
import java.awt.image.BufferedImage;

@Getter
@Setter
public class PixelCondition extends Condition {

    private BufferedImage displayImage;
    public PixelCondition(ReadingCondition chosenReadingCondition, BufferedImage mainImage,
                          Rectangle mainImageBoundingBox, boolean not, boolean required, BufferedImage displayImage) {
        super(chosenReadingCondition, mainImage, mainImageBoundingBox, not, required);
        this.displayImage = displayImage;
    }

    @Override
    public BufferedImage getMainDisplayImage() {
        return displayImage;
    }
}
