package com.demo.amime.core.face.detection.models;

import java.util.ArrayList;
import java.util.List;

public final class AnchorOptions {
    private final int numLayers;
    private final double minScale;
    private final double maxScale;
    private final int inputSizeHeight;
    private final int inputSizeWidth;
    private final double anchorOffsetX;
    private final double anchorOffsetY;
    private final double interpolatedScaleAspectRatio;
    private final List<Integer> featureMapWidth;
    private final List<Integer> featureMapHeight;
    private final List<Integer> strides;
    private final List<Double> aspectRatios;
    private final boolean fixedAnchorSize;
    private final boolean reduceBoxesInLowestLayer;

    private AnchorOptions(int numLayers,
                          double minScale,
                          double maxScale,
                          int inputSizeHeight,
                          int inputSizeWidth,
                          double anchorOffsetX,
                          double anchorOffsetY,
                          double interpolatedScaleAspectRatio,
                          List<Integer> featureMapWidth,
                          List<Integer> featureMapHeight,
                          List<Integer> strides,
                          List<Double> aspectRatios,
                          boolean fixedAnchorSize,
                          boolean reduceBoxesInLowestLayer) {
        this.numLayers = numLayers;
        this.minScale = minScale;
        this.maxScale = maxScale;
        this.inputSizeHeight = inputSizeHeight;
        this.inputSizeWidth = inputSizeWidth;
        this.anchorOffsetX = anchorOffsetX;
        this.anchorOffsetY = anchorOffsetY;
        this.interpolatedScaleAspectRatio = interpolatedScaleAspectRatio;
        this.featureMapWidth = featureMapWidth;
        this.featureMapHeight = featureMapHeight;
        this.strides = strides;
        this.aspectRatios = aspectRatios;
        this.fixedAnchorSize = fixedAnchorSize;
        this.reduceBoxesInLowestLayer = reduceBoxesInLowestLayer;
    }

    public int getNumLayers() {
        return numLayers;
    }

    public double getMinScale() {
        return minScale;
    }

    public double getMaxScale() {
        return maxScale;
    }

    public int getInputSizeHeight() {
        return inputSizeHeight;
    }

    public int getInputSizeWidth() {
        return inputSizeWidth;
    }

    public double getAnchorOffsetX() {
        return anchorOffsetX;
    }

    public double getAnchorOffsetY() {
        return anchorOffsetY;
    }

    public double getInterpolatedScaleAspectRatio() {
        return interpolatedScaleAspectRatio;
    }

    public List<Integer> getFeatureMapWidth() {
        return featureMapWidth;
    }

    public List<Integer> getFeatureMapHeight() {
        return featureMapHeight;
    }

    public List<Integer> getStrides() {
        return strides;
    }

    public List<Double> getAspectRatios() {
        return aspectRatios;
    }

    public boolean isFixedAnchorSize() {
        return fixedAnchorSize;
    }

    public boolean isReduceBoxesInLowestLayer() {
        return reduceBoxesInLowestLayer;
    }

    public static AnchorOptions withDefaultValues() {
        List<Integer> strides = new ArrayList<>(4);
        strides.add(8);
        strides.add(16);
        strides.add(16);
        strides.add(16);

        List<Double> aspectRatios = new ArrayList<>(1);
        aspectRatios.add(1.0);

        return new AnchorOptions(
                4,
                0.1484375f,
                0.75,
                128,
                128,
                0.5,
                0.5,
                1.0,
                new ArrayList<>(),
                new ArrayList<>(),
                strides,
                aspectRatios,
                true,
                false);
    }
}
