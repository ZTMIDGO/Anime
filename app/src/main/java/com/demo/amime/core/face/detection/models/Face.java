package com.demo.amime.core.face.detection.models;

import android.graphics.PointF;
import android.graphics.RectF;

import java.util.List;

public final class Face {

    public enum Landmarks {
        RIGHT_EYE(0),
        LEFT_EYE(1),
        NOSE(2),
        MOUTH(3),
        RIGHT_EAR(4),
        LEFT_EAR(5);

        private final int id;

        Landmarks(int id) {
            this.id = id;
        }
    }

    private final float score;
    private final RectF relativeCoordinate;
    private final List<PointF> relativeKeyPoints;

    public Face(float score, RectF relativeCoordinate, List<PointF> relativeKeyPoints) {
        this.score = score;
        this.relativeCoordinate = relativeCoordinate;
        this.relativeKeyPoints = relativeKeyPoints;
    }

    public float getScore() {
        return score;
    }

    public RectF getRelativeCoordinate() {
        return relativeCoordinate;
    }

    public PointF getRelativeKeyPoint(Landmarks landmarks) {
        return this.relativeKeyPoints.get(landmarks.id);
    }

    public List<PointF> getRelativeKeyPoints() {
        return this.relativeKeyPoints;
    }
}
