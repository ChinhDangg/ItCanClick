package org.dev.Operation.Condition;

import lombok.Getter;
import lombok.Setter;
import org.dev.Enum.ReadingCondition;
import org.dev.ImageSerialization;
import org.dev.Menu.ConditionTextMenuController;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Set;

@Getter @Setter
public class TextCondition extends Condition {

    private Set<String> savedText;
    private double currentTextScale;

    public TextCondition(ReadingCondition chosenReadingCondition, BufferedImage mainImage, Rectangle mainImageBoundingBox,
                         boolean not, boolean required, double textScale, Set<String> texts) {
        super(chosenReadingCondition, mainImage, mainImageBoundingBox, not, required);
        currentTextScale = textScale;
        savedText = texts;
    }

    @Override
    public boolean checkCondition() {
        try {
            readResult = ConditionTextMenuController.readTextFromCurrentScreen(mainImageBoundingBox, getCurrentTextScale());
            readResult = readResult.replace("\n", "");
            boolean pass = savedText.contains(readResult);
            if (not)
                return !pass;
            return pass;
        } catch (Exception e) {
            System.out.println("Fail checking text condition");
        }
        return false;
    }

    @Override
    public String getExpectedResult() { return savedText.toString(); }

    @Override
    public String getActualResult() { return readResult; }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ImageSerialization.serializeBufferedImageWriteObject(out, mainImage); // Serialize mainImage
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String imageString = (String) in.readObject();
        mainImage = ImageSerialization.deserializeBufferedImageReadObject(in, imageString, false); // Deserialize mainImage
    }
}
