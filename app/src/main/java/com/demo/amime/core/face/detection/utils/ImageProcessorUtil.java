package com.demo.amime.core.face.detection.utils;

import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;

public final class ImageProcessorUtil {

    private final float mean;
    private final float std;
    private final int targetWidth;
    private final int targetHeight;
    private int currentWidth;
    private int currentHeight;
    private ImageProcessor imageProcessor;

    public ImageProcessorUtil(float mean, float std, int targetWidth, int targetHeight) {
        this.mean = mean;
        this.std = std;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }

    public synchronized ImageProcessor getImageProcessor(int currentWidth, int currentHeight) {
        if (this.imageProcessor == null) {
            this.currentWidth = currentWidth;
            this.currentHeight = currentHeight;

            int maxDimension = Math.max(currentWidth, currentHeight);
            this.imageProcessor = new ImageProcessor.Builder().
                    add(new ResizeWithCropOrPadOp(maxDimension, maxDimension)).
                    add(new ResizeOp(this.targetHeight, this.targetWidth, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR)).
                    add(new NormalizeOp(this.mean, this.std)).
                    build();
        } else {
            if (this.currentWidth != currentWidth || this.currentHeight != currentHeight) {
                this.imageProcessor = null;
                this.imageProcessor = this.getImageProcessor(currentWidth, currentHeight);
            }
        }
        return this.imageProcessor;
    }
}
