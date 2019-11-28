package com.swipecrowd.dinogame;

import com.swipecrowd.dinogame.game.Emulation;

public class Main {
    public static void main(String[] args) {
        try {
            new Emulation().start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
