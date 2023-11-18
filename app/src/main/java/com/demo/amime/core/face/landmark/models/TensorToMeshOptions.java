package com.demo.amime.core.face.landmark.models;

public final class TensorToMeshOptions {

    private final int numLandmarks;
    private final int numCoordinates;
    private final float minScoreThreshold;
    private final float xScale;
    private final float yScale;

    public TensorToMeshOptions(int numLandmarks, int numCoordinates, float minScoreThreshold, float xScale, float yScale) {
        this.numLandmarks = numLandmarks;
        this.numCoordinates = numCoordinates;
        this.minScoreThreshold = minScoreThreshold;
        this.xScale = xScale;
        this.yScale = yScale;
    }

    public int getNumLandmarks() {
        return numLandmarks;
    }

    public int getNumCoordinates() {
        return numCoordinates;
    }

    public float getMinScoreThreshold() {
        return minScoreThreshold;
    }

    public float getXScale() {
        return xScale;
    }

    public float getYScale() {
        return yScale;
    }

    public static TensorToMeshOptions withDefaultValues() {
        return new TensorToMeshOptions(
                478,
                3,
                0.5f,
                256.0f,
                256.0f);
    }
}
