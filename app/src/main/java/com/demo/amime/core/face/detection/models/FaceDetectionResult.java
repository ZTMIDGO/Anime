package com.demo.amime.core.face.detection.models;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

public final class FaceDetectionResult {
    private final List<RectF> rectFS = new ArrayList<>();
    private final List<Face> faces;
    private final Bitmap inputBitmap;

    public FaceDetectionResult(List<Face> faces, Bitmap inputBitmap) {
        this.faces = faces;
        for (Face face : faces) rectFS.add(face.getRelativeCoordinate());
        this.inputBitmap = inputBitmap;
    }

    public List<Face> getFaces() {
        return faces;
    }

    public List<RectF> getRectFS() {
        return rectFS;
    }

    public Bitmap getInputBitmap() {
        return inputBitmap;
    }
}
