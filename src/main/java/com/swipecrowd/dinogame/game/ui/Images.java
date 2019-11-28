package com.swipecrowd.dinogame.game.ui;

import com.swipecrowd.dinogame.utils.ImageUtils;

import java.awt.image.BufferedImage;

public class Images {
    public static final BufferedImage dinoRunningImage0 = ImageUtils.loadImage("dinorun0000.png");
    public static final BufferedImage dinoRunningImage1 = ImageUtils.loadImage("dinorun0001.png");
    public static final BufferedImage dinoJumpingImage = ImageUtils.loadImage("dinoJump0000.png");

    public static final BufferedImage cactusSmallImage = ImageUtils.loadImage("cactusSmall0000.png");
    public static final BufferedImage cactusBigImage = ImageUtils.loadImage("cactusBig0000.png");
    public static final BufferedImage cactusSmallManyImage = ImageUtils.loadImage("cactusSmallMany0000.png");
    public static final BufferedImage birdImage0 = ImageUtils.loadImage("berd.png");
    public static final BufferedImage birdImage1 = ImageUtils.loadImage("berd2.png");

    public static final BufferedImage dinoDuck0 = ImageUtils.loadImage("dinoduck0000.png");
    public static final BufferedImage dinoDuck1 = ImageUtils.loadImage("dinoduck0001.png");


//    private static BufferedImage enlarge(final BufferedImage loadImage, final double factor) {
//        return Thumbnailator.createThumbnail(loadImage,
//                (int) (loadImage.getWidth() * factor),
//                (int) (loadImage.getHeight()*factor));
//    }
}
