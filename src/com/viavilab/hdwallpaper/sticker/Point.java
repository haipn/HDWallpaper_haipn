package com.viavilab.hdwallpaper.sticker;

public class Point {
    float x;
    float y;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "x: " + x + ",y: " + y;
    }
}