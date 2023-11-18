package com.demo.amime.core.face.detection.utils;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Size;

import com.demo.amime.core.face.detection.models.Anchor;
import com.demo.amime.core.face.detection.models.Face;
import com.demo.amime.core.face.detection.models.TensorToFacesOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TensorToFaces {

    public List<Face> process(Size imageSize, TensorToFacesOptions options, float[][][] rawScores, float[][][] rawBoxes, List<Anchor> anchors) {
        if (rawBoxes.length != 1 || rawBoxes[0].length != options.getNumBoxes() || rawBoxes[0][0].length != options.getNumCoordinates())
            throw new IllegalArgumentException("RawBoxes dimensions is not correct");
        if (rawScores.length != 1 || rawScores[0].length != options.getNumBoxes() || rawScores[0][0].length != options.getNumClasses())
            throw new IllegalArgumentException("RawScores dimensions is not correct");
        if (options.getMaxNumberOfFaces() == 0 || options.getMaxNumberOfFaces() < -1)
            throw new IllegalArgumentException("MaxNumberOfFaces must be greater than 0 or -1");

        List<Float> detectionScores = new ArrayList<>(options.getNumBoxes());

        for (int i = 0; i < options.getNumBoxes(); i++) {
            float maxScore = Float.MIN_VALUE;
            float score = rawScores[0][i][0];
            if (options.getScoreClippingThreshold() > 0) {
                if (score < -options.getScoreClippingThreshold())
                    score = (float) -options.getScoreClippingThreshold();
                if (score > options.getScoreClippingThreshold())
                    score = (float) options.getScoreClippingThreshold();
                score = 1.0f / (1.0f + (float) Math.exp(-score));
                if (score > maxScore)
                    maxScore = score;
            }
            detectionScores.add(maxScore);
        }

        List<Face> faces = this.convertToFaces(options, rawBoxes, detectionScores, anchors);
        faces = this.nonMaxSuppression(faces, options.getIouThreshold());
        if (options.getMaxNumberOfFaces() != -1)
            faces = this.getFacesWithHigherScore(faces, options.getMaxNumberOfFaces());
        faces = this.projectCoordinate(faces, imageSize);
        return faces;
    }

    private List<Face> convertToFaces(TensorToFacesOptions options, float[][][] rawBoxes, List<Float> detectionScores, List<Anchor> anchors) {
        List<Face> outputFaces = new ArrayList<>();
        for (int i = 0; i < options.getNumBoxes(); i++) {
            if (detectionScores.get(i) < options.getMinScoreThreshold())
                continue;
            float[] decodedBox = this.decodeBox(rawBoxes[0], i, anchors, options);
            Face face = this.convertToFace(decodedBox, detectionScores.get(i));
            outputFaces.add(face);
        }

        return outputFaces;
    }

    private float[] decodeBox(float[][] rawBoxes, int i, List<Anchor> anchors, TensorToFacesOptions options) {
        float[] boxData = new float[options.getNumCoordinates()];
        float xCenter = rawBoxes[i][0];
        float yCenter = rawBoxes[i][1];
        float width = rawBoxes[i][2];
        float height = rawBoxes[i][3];

        xCenter = xCenter / options.getXScale() * anchors.get(i).getWidth() + anchors.get(i).getXCenter();
        yCenter = yCenter / options.getYScale() * anchors.get(i).getHeight() + anchors.get(i).getYCenter();

        height = height / options.getHeightScale() * anchors.get(i).getHeight();
        width = width / options.getWidthScale() * anchors.get(i).getWidth();

        float yMin = yCenter - height / 2;
        float xMin = xCenter - width / 2;
        float yMax = yCenter + height / 2;
        float xMax = xCenter + width / 2;

        boxData[0] = yMin;
        boxData[1] = xMin;
        boxData[2] = yMax;
        boxData[3] = xMax;

        if (options.getNumKeyPoints() > 0) {
            for (int k = 0; k < options.getNumKeyPoints(); k++) {
                int offset = options.getKeypointCoordinateOffset() + k * options.getNumValuesPerKeypoint();
                float keyPointX = rawBoxes[i][offset];
                float keyPointY = rawBoxes[i][offset + 1];
                boxData[4 + k * options.getNumValuesPerKeypoint()] = keyPointX / options.getXScale() * anchors.get(i).getWidth() + anchors.get(i).getXCenter();
                boxData[4 + k * options.getNumValuesPerKeypoint() + 1] = keyPointY / options.getYScale() * anchors.get(i).getHeight() + anchors.get(i).getYCenter();
            }
        }

        return boxData;
    }

    private Face convertToFace(float[] decodedBox, float detectionScore) {
        List<PointF> relativeKeyPoints = new ArrayList<>();
        for (int i = 4; i < decodedBox.length - 1; i = i + 2) {
            relativeKeyPoints.add(new PointF(decodedBox[i], decodedBox[i + 1]));
        }
        return new Face(detectionScore, new RectF(decodedBox[1], decodedBox[0], decodedBox[3], decodedBox[2]), relativeKeyPoints);
    }

    private List<Face> nonMaxSuppression(List<Face> faces, float threshold) {
        if (faces.size() == 0)
            return faces;

        List<Face> outputFaces = new ArrayList<>();

        List<Face> remained = new ArrayList<>();
        List<Face> candidates = new ArrayList<>();

        Collections.sort(faces, (o1, o2) -> Float.compare(o2.getScore(), o1.getScore()));
        while (!faces.isEmpty()) {
            int original_faces_size = faces.size();

            candidates.clear();
            remained.clear();
            Face detection = faces.get(0);
            for (Face face : faces) {
                float similarity = calculateOverlapSimilarity(face.getRelativeCoordinate(), detection.getRelativeCoordinate());
                if (similarity > threshold) {
                    candidates.add(face);
                } else {
                    remained.add(face);
                }
            }

            Face candidateFace = detection;

            if (!candidates.isEmpty()) {
                float totalScore = 0.0f;
                RectF weightedBoundingBox = new RectF();
                List<PointF> weightedRelativeKeyPoints = new ArrayList<>(candidateFace.getRelativeKeyPoints().size());
                for (int i = 0; i < candidateFace.getRelativeKeyPoints().size(); i++) {
                    weightedRelativeKeyPoints.add(new PointF());
                }

                for (Face candidate : candidates) {
                    totalScore += candidate.getScore();
                    weightedBoundingBox.top += candidate.getRelativeCoordinate().top * candidate.getScore();
                    weightedBoundingBox.bottom += candidate.getRelativeCoordinate().bottom * candidate.getScore();
                    weightedBoundingBox.left += candidate.getRelativeCoordinate().left * candidate.getScore();
                    weightedBoundingBox.right += candidate.getRelativeCoordinate().right * candidate.getScore();

                    for (int i = 0; i < weightedRelativeKeyPoints.size(); i++) {
                        weightedRelativeKeyPoints.get(i).x += candidate.getRelativeKeyPoints().get(i).x * candidate.getScore();
                        weightedRelativeKeyPoints.get(i).y += candidate.getRelativeKeyPoints().get(i).y * candidate.getScore();
                    }
                }

                weightedBoundingBox.top = weightedBoundingBox.top / totalScore;
                weightedBoundingBox.bottom = weightedBoundingBox.bottom / totalScore;
                weightedBoundingBox.left = weightedBoundingBox.left / totalScore;
                weightedBoundingBox.right = weightedBoundingBox.right / totalScore;

                for (int i = 0; i < weightedRelativeKeyPoints.size(); i++) {
                    weightedRelativeKeyPoints.get(i).x = weightedRelativeKeyPoints.get(i).x / totalScore;
                    weightedRelativeKeyPoints.get(i).y = weightedRelativeKeyPoints.get(i).y / totalScore;
                }

                candidateFace = new Face(totalScore / candidates.size(), weightedBoundingBox, weightedRelativeKeyPoints);
            }

            outputFaces.add(candidateFace);

            if (original_faces_size == remained.size()) {
                break;
            } else {
                faces.clear();
                faces.addAll(remained);
            }
        }

        return outputFaces;
    }

    private float calculateOverlapSimilarity(RectF boundingBox1, RectF boundingBox2) {
        float left = Math.max(boundingBox1.left, boundingBox2.left);
        float top = Math.max(boundingBox1.top, boundingBox2.top);
        float right = Math.min(boundingBox1.right, boundingBox2.right);
        float bottom = Math.min(boundingBox1.bottom, boundingBox2.bottom);

        float height = Math.max(0, bottom - top);
        float width = Math.max(0, right - left);

        float intersectionArea = height * width;

        float unionArea = this.area(boundingBox1) + this.area(boundingBox2) - intersectionArea;
        return intersectionArea / unionArea;
    }

    private float area(RectF boundingBox) {
        return (boundingBox.right - boundingBox.left) * (boundingBox.bottom - boundingBox.top);
    }

    private List<Face> getFacesWithHigherScore(List<Face> faces, int maxNumberOfFaces) {
        if (faces.size() == 0)
            return faces;

        Collections.sort(faces, (o1, o2) -> Float.compare(o2.getScore(), o1.getScore()));
        return faces.subList(0, Math.min(maxNumberOfFaces, faces.size()));
    }

    private List<Face> projectCoordinate(List<Face> faces, Size imageSize) {
        if (imageSize.getWidth() == imageSize.getHeight())
            return faces;

        float offsetX;
        float offsetY;
        float multiplyY;
        float multiplyX;


        if (imageSize.getWidth() < imageSize.getHeight()) {
            //Image is portrait
            offsetY = 0.0f;
            multiplyY = 1.0f;
            offsetX = (1.0f - (float) imageSize.getWidth() / imageSize.getHeight()) / 2.0f;
            multiplyX = (float) imageSize.getHeight() / imageSize.getWidth();
        } else {
            offsetY = (1.0f - (float) imageSize.getHeight() / imageSize.getWidth()) / 2.0f;
            multiplyY = (float) imageSize.getWidth() / imageSize.getHeight();
            offsetX = 0.0f;
            multiplyX = 1.0f;
        }

        for (Face face : faces) {
            face.getRelativeCoordinate().left -= offsetX;
            face.getRelativeCoordinate().left *= multiplyX;
            face.getRelativeCoordinate().right -= offsetX;
            face.getRelativeCoordinate().right *= multiplyX;
            face.getRelativeCoordinate().top -= offsetY;
            face.getRelativeCoordinate().top *= multiplyY;
            face.getRelativeCoordinate().bottom -= offsetY;
            face.getRelativeCoordinate().bottom *= multiplyY;

            for (int i = 0; i < face.getRelativeKeyPoints().size(); i++) {
                face.getRelativeKeyPoints().get(i).x -= offsetX;
                face.getRelativeKeyPoints().get(i).x *= multiplyX;
                face.getRelativeKeyPoints().get(i).y -= offsetY;
                face.getRelativeKeyPoints().get(i).y *= multiplyY;
            }
        }
        return faces;
    }

}
