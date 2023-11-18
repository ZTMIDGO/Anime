package com.demo.amime.core.face.landmark.utils;

import android.graphics.RectF;
import android.util.Size;

import com.demo.amime.core.face.landmark.models.FaceMesh;
import com.demo.amime.core.face.landmark.models.Landmark;
import com.demo.amime.core.face.landmark.models.TensorToMeshOptions;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class TensorToMesh {

    @Nullable
    public FaceMesh process(Size imageSize, TensorToMeshOptions options, float[][][][] rawScores, float[][][][] rawMeshes, RectF roi) {

        float rawScore = rawScores[0][0][0][0];
        float detectionScore = 1.0f / (1.0f + (float) Math.exp(-rawScore));

        if (detectionScore < options.getMinScoreThreshold())
            return null;

        FaceMesh faceMesh = new FaceMesh(imageSize.getWidth(), imageSize.getHeight());
        List<Landmark> landmarks = this.convertToLandmarks(options, rawMeshes[0][0][0]);
        landmarks = this.projectCoordinate(options, landmarks, imageSize, roi);
        faceMesh.setFaceScorePresence(detectionScore);
        faceMesh.setRelativeLandmarks(landmarks);
        return faceMesh;
    }

    private List<Landmark> convertToLandmarks(TensorToMeshOptions options, float[] rawMeshes) {
        List<Landmark> landmarks = new ArrayList<>(options.getNumLandmarks());
        for (int i = 0; i < options.getNumLandmarks(); i++) {
            int offset = i * options.getNumCoordinates();
            float x = rawMeshes[offset];
            float y = rawMeshes[offset + 1];
            float z = rawMeshes[offset + 2];
            landmarks.add(new Landmark(x, y, z));
        }
        return landmarks;
    }

    private List<Landmark> projectCoordinate(TensorToMeshOptions options, List<Landmark> landmarks, Size imageSize, RectF roi) {
        int x = (int) Math.max(roi.left, 0);
        int y = (int) Math.max(roi.top, 0);
        int width = (int) Math.min(roi.width(), imageSize.getWidth() - x);
        int height = (int) Math.min(roi.height(), imageSize.getHeight() - y);

        for (Landmark landmark : landmarks) {
            landmark.x *= width / options.getXScale();
            landmark.x += x;
            landmark.x /= imageSize.getWidth();

            landmark.y *= height / options.getYScale();
            landmark.y += y;
            landmark.y /= imageSize.getHeight();

            landmark.z *= width / options.getXScale();
            landmark.z /= imageSize.getWidth();
        }
        return landmarks;
    }
}
