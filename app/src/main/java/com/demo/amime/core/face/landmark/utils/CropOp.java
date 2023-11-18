package com.demo.amime.core.face.landmark.utils;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;

import org.tensorflow.lite.support.common.internal.SupportPreconditions;
import org.tensorflow.lite.support.image.ColorSpaceType;
import org.tensorflow.lite.support.image.ImageOperator;
import org.tensorflow.lite.support.image.TensorImage;

public class CropOp implements ImageOperator {

    private final RectF roi;

    public CropOp(RectF roi) {
        this.roi = roi;
    }

    @Override
    public TensorImage apply(TensorImage image) {
        SupportPreconditions.checkArgument(image.getColorSpaceType() == ColorSpaceType.RGB, "Only RGB images are supported in CropOp, but not " + image.getColorSpaceType().name());
        Bitmap input = image.getBitmap();

        int x = (int) Math.max(this.roi.left, 0);
        int y = (int) Math.max(this.roi.top, 0);
        int width = (int) Math.min(this.roi.width(), input.getWidth() - x);
        int height = (int) Math.min(this.roi.height(), input.getHeight() - y);

        Bitmap output = Bitmap.createBitmap(input, x, y, width, height);
        image.load(output);
        return image;
    }

    @Override
    public int getOutputImageWidth(int inputImageHeight, int inputImageWidth) {
        return (int) Math.min(this.roi.width(), inputImageWidth - (int) Math.max(this.roi.left, 0));
    }

    @Override
    public int getOutputImageHeight(int inputImageHeight, int inputImageWidth) {
        return (int) Math.min(this.roi.height(), inputImageHeight - (int) Math.max(this.roi.top, 0));
    }

    @Override
    public PointF inverseTransform(PointF pointF, int inputImageHeight, int inputImageWidth) {
        return new PointF(pointF.x + Math.max(this.roi.left, 0), pointF.y + Math.max(this.roi.top, 0));
    }
}
