package eu.mjdev.desktop.helpers.gif;

import java.awt.image.BufferedImage;

public class GifFrame {
    public BufferedImage image;
    public int delay;

    public GifFrame(BufferedImage im, int del) {
        image = im;
        delay = del;
    }
}