package com.swipecrowd.dinogame.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class ImageUtils {
    public static BufferedImage loadImage(final String path) {
        try {
            return ImageIO.read(Objects.requireNonNull(ImageUtils.class.getClassLoader().getResourceAsStream(path)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
