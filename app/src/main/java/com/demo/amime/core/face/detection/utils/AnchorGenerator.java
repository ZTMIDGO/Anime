package com.demo.amime.core.face.detection.utils;

import com.demo.amime.core.face.detection.models.Anchor;
import com.demo.amime.core.face.detection.models.AnchorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AnchorGenerator {

    public static List<Anchor> generate(AnchorOptions options) {
        if (options.getNumLayers() <= 0)
            throw new IllegalArgumentException("NumLayers must be greater than 0, numLayers: " + options.getNumLayers());
        if (options.getNumLayers() != Objects.requireNonNull(options.getStrides()).size())
            throw new IllegalArgumentException("Strides size must equal to NumLayers");

        List<Anchor> anchors = new ArrayList<>();

        int layerId = 0;
        while (layerId < options.getNumLayers()) {
            List<Double> anchorHeight = new ArrayList<>();
            List<Double> anchorWidth = new ArrayList<>();
            List<Double> aspectRatios = new ArrayList<>();
            List<Double> scales = new ArrayList<>();

            int lastSameStrideLayer = layerId;
            while (lastSameStrideLayer < options.getStrides().size() &&
                    options.getStrides().get(lastSameStrideLayer).equals(options.getStrides().get(layerId))) {
                double scale = calculateScale(options.getMinScale(), options.getMaxScale(), lastSameStrideLayer, options.getStrides().size());
                if (lastSameStrideLayer == 0 && options.isReduceBoxesInLowestLayer()) {
                    aspectRatios.add(1.0);
                    aspectRatios.add(2.0);
                    aspectRatios.add(0.5);
                    scales.add(0.1);
                    scales.add(scale);
                    scales.add(scale);
                } else {
                    for (int i = 0; i < options.getAspectRatios().size(); i++) {
                        aspectRatios.add(options.getAspectRatios().get(i));
                        scales.add(scale);
                    }
                    if (options.getInterpolatedScaleAspectRatio() > 0.0) {
                        double scaleNext = lastSameStrideLayer == options.getStrides().size() - 1 ?
                                1.0 : calculateScale(options.getMinScale(), options.getMaxScale(), lastSameStrideLayer, options.getStrides().size());
                        scales.add(Math.sqrt(scale * scaleNext));
                        aspectRatios.add(options.getInterpolatedScaleAspectRatio());
                    }
                }
                lastSameStrideLayer++;
            }
            for (int i = 0; i < aspectRatios.size(); i++) {
                double ratioSqrt = Math.sqrt(aspectRatios.get(i));
                anchorHeight.add(scales.get(i) / ratioSqrt);
                anchorWidth.add(scales.get(i) * ratioSqrt);
            }
            int featureMapHeight = 0;
            int featureMapWidth = 0;

            if (options.getFeatureMapHeight().size() > 0) {
                featureMapHeight = options.getFeatureMapHeight().get(layerId);
                featureMapWidth = options.getFeatureMapWidth().get(layerId);
            } else {
                int stride = options.getStrides().get(layerId);
                featureMapHeight = (int) Math.ceil(1.0 * options.getInputSizeHeight() / stride);
                featureMapWidth = (int) Math.ceil(1.0 * options.getInputSizeWidth() / stride);
            }
            for (int y = 0; y < featureMapHeight; y++) {
                for (int x = 0; x < featureMapWidth; x++) {
                    for (int anchorId = 0; anchorId < anchorHeight.size(); anchorId++) {
                        double xCenter = (x + options.getAnchorOffsetX()) * 1.0 / featureMapWidth;
                        double yCenter = (y + options.getAnchorOffsetY()) * 1.0 / featureMapHeight;
                        double width = 0;
                        double height = 0;
                        if (options.isFixedAnchorSize()) {
                            width = 1.0;
                            height = 1.0;
                        } else {
                            width = anchorWidth.get(anchorId);
                            height = anchorHeight.get(anchorId);
                        }
                        anchors.add(new Anchor((float) xCenter, (float) yCenter, (float) width, (float) height));
                    }
                }
            }
            layerId = lastSameStrideLayer;
        }

        return anchors;
    }

    private static double calculateScale(double minScale, double maxScale, int strideIndex, int numStrides) {
        if (numStrides == 1) {
            return (minScale + maxScale) * 0.5;
        } else {
            return minScale + (maxScale - minScale) * 1.0 * strideIndex / (numStrides - 1.0f);
        }
    }
}
