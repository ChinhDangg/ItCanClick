package org.dev;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

public class ImageSerialization {

    public static void serializeBufferedImageWriteObject(ObjectOutputStream out, BufferedImage image) throws IOException {
        if (image != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", bos);
            byte[] data = bos.toByteArray();
            out.writeObject(Base64.getEncoder().encodeToString(data));
        }
        else
            out.writeObject(null);
    }

    public static BufferedImage deserializeBufferedImageReadObject(ObjectInputStream in, String inputStringStream, boolean getRGB) throws IOException {
        if (inputStringStream != null) {
            byte[] data = Base64.getDecoder().decode(inputStringStream);
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            BufferedImage image = ImageIO.read(bis);
            if (getRGB && image.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                newImage.getGraphics().drawImage(image, 0, 0, null);
            }
            return image;
        }
        return null;
    }
}
