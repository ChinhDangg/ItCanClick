package org.dev.Operation.Condition;
import lombok.Getter;
import lombok.Setter;
import org.dev.Enum.ReadingCondition;
import java.awt.*;
import java.awt.image.BufferedImage;

@Getter
@Setter
public abstract class Condition {
    protected ReadingCondition chosenReadingCondition;
    protected BufferedImage mainImage;
    protected Rectangle mainImageBoundingBox;
    protected boolean not;
    protected boolean required;

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
    public abstract boolean checkCondition();
}
