package com.demo.amime.core.face.detection.models;

public final class Anchor {
    private final float xCenter;
    private final float yCenter;
    private final float width;
    private final float height;

    public Anchor(float xCenter, float yCenter, float width, float height) {
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.width = width;
        this.height = height;
    }

    public float getXCenter() {
        return xCenter;
    }

    public float getYCenter() {
        return yCenter;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
