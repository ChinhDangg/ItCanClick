package org.dev.Operation.Condition;

import lombok.Getter;
import lombok.Setter;
import org.dev.Enum.ReadingCondition;
import org.dev.ImageSerialization;
import org.dev.Menu.ConditionTextMenuController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.Set;

@Getter
@Setter
public class TextCondition extends Condition {

    private Set<String> readText;
    private double currentTextScale;

    public TextCondition(ReadingCondition chosenReadingCondition, BufferedImage mainImage, Rectangle mainImageBoundingBox,
                         boolean not, boolean required, double textScale, Set<String> texts) {
        super(chosenReadingCondition, mainImage, mainImageBoundingBox, not, required);
        currentTextScale = textScale;
        readText = texts;
    }

    @Override
    public boolean checkCondition() {
        try {
            String text = ConditionTextMenuController.readTextFromCurrentScreen(mainImageBoundingBox, getCurrentTextScale());
            text = text.replace("\n", "");
            boolean pass = readText.contains(text);
            if (not)
                return !pass;
            return pass;
        } catch (Exception e) {
            System.out.println("Fail checking text condition");
        }
        return false;
    }

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
