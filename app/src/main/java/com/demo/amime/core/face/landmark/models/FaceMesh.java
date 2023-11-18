package com.demo.amime.core.face.landmark.models;

import com.demo.amime.bean.MyPoint;

import java.util.ArrayList;
import java.util.List;

public final class FaceMesh {

    private final int width;
    private final int height;
    private final List<Landmark> relativeLandmarks = new ArrayList<>();
    private final List<MyPoint> points = new ArrayList<>();
    private float faceScorePresence;

    public FaceMesh(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setRelativeLandmarks(List<Landmark> landmarks) {
        points.clear();
        relativeLandmarks.clear();
        relativeLandmarks.addAll(landmarks);

        for (Landmark landmark : relativeLandmarks){
            float x = landmark.x * width;
            float y = landmark.y * height;
            points.add(new MyPoint((int) x, (int) y));
        }
    }

    public void setFaceScorePresence(float faceScorePresence) {
        this.faceScorePresence = faceScorePresence;
    }

    public List<Landmark> getRelativeLandmarks() {
        return relativeLandmarks;
    }

    public float getFaceScorePresence() {
        return faceScorePresence;
    }

    public List<MyPoint> getPoints() {
        return points;
    }

    public List<MyPoint> getEyeLeftUP(){
        List<MyPoint> result = new ArrayList<>(9);
        int[] index = {362, 398, 384, 385, 386, 387, 388, 466, 263};
        for (int i : index) {
            MyPoint point = points.get(i);
            result.add(new MyPoint(point));
        }
        return result;
    }

    public List<MyPoint> getEyeLeftDown(){
        List<MyPoint> result = new ArrayList<>(9);
        int[] index = {362, 382, 381, 380, 374, 373, 390, 249, 263};
        for (int i : index) {
            MyPoint point = points.get(i);
            result.add(new MyPoint(point));
        }
        return result;
    }

    public List<MyPoint> getEyebrowLeftUP(){
        List<MyPoint> result = new ArrayList<>(5);
        int[] index = {336, 296, 334, 293, 300};
        for (int i : index) {
            MyPoint point = points.get(i);
            result.add(new MyPoint(point));
        }
        return result;
    }

    public List<MyPoint> getEyebrowLeftDown(){
        List<MyPoint> result = new ArrayList<>(9);
        //int[] index = {463, 414, 286, 258, 257, 259, 260, 342};
        int[] index = {464, 413, 441, 442, 443, 444, 445, 353};

        for (int i : index) {
            MyPoint point = points.get(i);
            result.add(new MyPoint(point));
        }
        return result;
    }

    public List<MyPoint> getEyeRightUP(){
        List<MyPoint> result = new ArrayList<>(9);
        int[] index = {133, 173, 157, 158, 159, 160, 161, 246, 33};
        for (int i : index) {
            MyPoint point = points.get(i);
            result.add(new MyPoint(point));
        }
        return result;
    }

    public List<MyPoint> getEyeRightDown(){
        List<MyPoint> result = new ArrayList<>(9);
        int[] index = {133, 155, 154, 153, 145, 144, 163, 7, 33};
        for (int i : index) {
            MyPoint point = points.get(i);
            result.add(new MyPoint(point));
        }
        return result;
    }

    public List<MyPoint> getEyebrowRightUP(){
        List<MyPoint> result = new ArrayList<>(5);
        int[] index = {55, 65, 52, 53, 46};
        for (int i : index) {
            MyPoint point = points.get(i);
            result.add(new MyPoint(point));
        }
        return result;
    }

    public List<MyPoint> getEyebrowRightDown(){
        List<MyPoint> result = new ArrayList<>(5);
        int[] index = {107, 66, 105, 63, 70};
        for (int i : index) {
            MyPoint point = points.get(i);
            result.add(new MyPoint(point));
        }
        return result;
    }
}
