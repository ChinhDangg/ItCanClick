package org.dev.Operation.Condition;

import lombok.Getter;
import lombok.Setter;
import org.dev.Enum.ReadingCondition;
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
        // Serialize mainImage
        if (mainImage != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(mainImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            out.writeObject(Base64.getEncoder().encodeToString(imageBytes));
        } else {
            out.writeObject(null);
        }
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Deserialize mainImage
        String imageString = (String) in.readObject();
        if (imageString != null) {
            byte[] imageBytes = Base64.getDecoder().decode(imageString);
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            mainImage = ImageIO.read(bais);
        }
    }
}
