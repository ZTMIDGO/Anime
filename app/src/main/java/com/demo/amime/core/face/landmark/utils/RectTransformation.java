package com.demo.amime.core.face.landmark.utils;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Size;

public final class RectTransformation {

    private final static float SCALE_X = 1.5f;
    private final static float SCALE_Y = 1.5f;
    private final static float SHIFT_X = 0.0f;
    private final static float SHIFT_Y = 0.0f;

    public static RectF transform(RectF unNormalized, float rotationDegree) {
        float x_center = unNormalized.centerX();
        float y_center = unNormalized.centerY();
        float width = unNormalized.width();
        float height = unNormalized.height();

        float new_x_center;
        float new_y_center;
        float new_width;
        float new_height;

        if (rotationDegree == 0) {
            new_x_center = x_center + width * SHIFT_X;
            new_y_center = y_center + height * SHIFT_Y;
        } else {
            float x_shift = (float) (width * SHIFT_X * Math.cos(rotationDegree) - height * SHIFT_Y * Math.sin(rotationDegree));
            float y_shift = (float) (width * SHIFT_X * Math.sin(rotationDegree) + height * SHIFT_Y * Math.cos(rotationDegree));
            new_x_center = x_center + x_shift;
            new_y_center = y_center + y_shift;
        }

        float long_side = Math.max(width, height);

        new_width = long_side * SCALE_X;
        new_height = long_side * SCALE_Y;

        RectF result = new RectF();
        result.left = new_x_center - new_width / 2;
        result.right = new_x_center + new_width / 2;
        result.top = new_y_center - new_height / 2;
        result.bottom = new_y_center + new_height / 2;
        return result;
    }

    public static RectF unNormalizeRectF(RectF rect, Size imageSize) {
        RectF result = new RectF();
        result.top = rect.top * imageSize.getHeight();
        result.bottom = rect.bottom * imageSize.getHeight();
        result.left = rect.left * imageSize.getWidth();
        result.right = rect.right * imageSize.getWidth();

        return result;
    }

    public static float getRotationRadian(PointF point1, PointF point2) {
        return (float) Math.atan2(-(point2.y - point1.y), point2.x - point1.x);
    }

    public static PointF unNormalizePointF(PointF point, Size imageSize) {
        PointF result = new PointF();
        result.x = point.x * imageSize.getWidth();
        result.y = point.y * imageSize.getHeight();
        return result;
    }
}
