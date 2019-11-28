package com.swipecrowd.dinogame.ui;

import com.swipecrowd.dinogame.utils.ImageUtils;

import java.awt.image.BufferedImage;

public class Images {
    public static final BufferedImage dinoRunningImage0 = ImageUtils.loadImage("dinorun0000.png");
    public static final BufferedImage dinoRunningImage1 = ImageUtils.loadImage("dinorun0001.png");
    public static final BufferedImage dinoJumpingImage = ImageUtils.loadImage("dinoJump0000.png");

    public static final BufferedImage cactusSmallImage = ImageUtils.loadImage("cactusSmall0000.png");
    public static final BufferedImage cactusBigImage = ImageUtils.loadImage("cactusBig0000.png");
    public static final BufferedImage cactusSmallManyImage = ImageUtils.loadImage("cactusSmallMany0000.png");


//    private static BufferedImage enlarge(final BufferedImage loadImage, final double factor) {
//        return Thumbnailator.createThumbnail(loadImage,
//                (int) (loadImage.getWidth() * factor),
//                (int) (loadImage.getHeight()*factor));
//    }
}
