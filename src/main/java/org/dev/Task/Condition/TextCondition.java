package org.dev.Task.Condition;

import lombok.Getter;
import lombok.Setter;
import org.dev.Enum.ReadingCondition;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

@Getter
@Setter
public class TextCondition extends Condition {

    private List<String> readText;
    private double currentTextScale;

    public TextCondition(ReadingCondition chosenReadingCondition, BufferedImage mainImage, Rectangle mainImageBoundingBox,
                         boolean not, boolean required, double textScale, List<String> texts) {
        super(chosenReadingCondition, mainImage, mainImageBoundingBox, not, required);
        currentTextScale = textScale;
        readText = texts;
    }
}
