package com.demo.amime.core.face.landmark.models;

import android.graphics.Bitmap;

import java.util.List;

public final class FaceMeshResult {

    private List<FaceMesh> facesMesh;
    private Bitmap inputBitmap;

    public FaceMeshResult() {
    }

    public void setFacesMesh(List<FaceMesh> facesMesh) {
        this.facesMesh = facesMesh;
    }

    public void setInputBitmap(Bitmap inputBitmap) {
        this.inputBitmap = inputBitmap;
    }

    public List<FaceMesh> getFacesMesh() {
        return facesMesh;
    }

    public Bitmap getInputBitmap() {
        return inputBitmap;
    }
}
