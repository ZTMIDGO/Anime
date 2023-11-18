package com.demo.amime.core.face.detection.models;

public final class TensorToFacesOptions {
    private final int maxNumberOfFaces;
    private final int numClasses;
    private final int numBoxes;
    private final int numCoordinates;
    private final int keypointCoordinateOffset;
    private final double scoreClippingThreshold;
    private final double minScoreThreshold;
    private final int numKeyPoints;
    private final int numValuesPerKeypoint;
    private final float xScale;
    private final float yScale;
    private final float widthScale;
    private final float heightScale;
    private final float iouThreshold;

    private TensorToFacesOptions(int maxNumberOfFaces,
                                 int numBoxes,
                                 int numCoordinates,
                                 int keypointCoordinateOffset,
                                 double scoreClippingThreshold,
                                 double minScoreThreshold,
                                 int numKeyPoints,
                                 int numValuesPerKeypoint,
                                 float xScale,
                                 float yScale,
                                 float widthScale,
                                 float heightScale,
                                 float iouThreshold
    ) {
        this.maxNumberOfFaces = maxNumberOfFaces;
        this.numClasses = 1;
        this.numBoxes = numBoxes;
        this.numCoordinates = numCoordinates;
        this.keypointCoordinateOffset = keypointCoordinateOffset;
        this.scoreClippingThreshold = scoreClippingThreshold;
        this.minScoreThreshold = minScoreThreshold;
        this.numKeyPoints = numKeyPoints;
        this.numValuesPerKeypoint = numValuesPerKeypoint;
        this.xScale = xScale;
        this.yScale = yScale;
        this.widthScale = widthScale;
        this.heightScale = heightScale;
        this.iouThreshold = iouThreshold;
    }

    public int getMaxNumberOfFaces() {
        return maxNumberOfFaces;
    }

    public int getNumClasses() {
        return numClasses;
    }

    public int getNumBoxes() {
        return numBoxes;
    }

    public int getNumCoordinates() {
        return numCoordinates;
    }

    public int getKeypointCoordinateOffset() {
        return keypointCoordinateOffset;
    }

    public double getScoreClippingThreshold() {
        return scoreClippingThreshold;
    }

    public double getMinScoreThreshold() {
        return minScoreThreshold;
    }

    public int getNumKeyPoints() {
        return numKeyPoints;
    }

    public int getNumValuesPerKeypoint() {
        return numValuesPerKeypoint;
    }

    public float getXScale() {
        return xScale;
    }

    public float getYScale() {
        return yScale;
    }

    public float getWidthScale() {
        return widthScale;
    }

    public float getHeightScale() {
        return heightScale;
    }

    public float getIouThreshold() {
        return iouThreshold;
    }

    public static TensorToFacesOptions withDefaultValues(double minScoreThreshold, int maxNumberOfFaces) {
        return new TensorToFacesOptions(
                maxNumberOfFaces,
                896,
                16,
                4,
                100.0,
                minScoreThreshold,
                6,
                2,
                128.0f,
                128.0f,
                128.0f,
                128.0f,
                0.3f);
    }
}
