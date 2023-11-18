package com.demo.amime.core.face.detection.models;

public final class FaceDetectionOptions {

    private final float minConfidence;
    private final int maxNumberOfFaces;

    private FaceDetectionOptions(Builder builder) {
        this.minConfidence = builder.minConfidence;
        this.maxNumberOfFaces = builder.maxNumberOfFaces;
    }

    public float getMinConfidence() {
        return minConfidence;
    }

    public int getMaxNumberOfFaces() {
        return maxNumberOfFaces;
    }

    public final static class Builder {
        private float minConfidence = 0.5f;
        private int maxNumberOfFaces = -1;

        public Builder() {
        }

        public Builder setMinConfidence(float minConfidence) {
            this.minConfidence = minConfidence;
            return this;
        }

        public Builder setMaxNumberOfFaces(int maxNumberOfFaces) {
            this.maxNumberOfFaces = maxNumberOfFaces;
            return this;
        }

        public FaceDetectionOptions build() {
            FaceDetectionOptions options = new FaceDetectionOptions(this);
            validate(options);
            return options;
        }

        private static void validate(FaceDetectionOptions options) {
            if (options.getMaxNumberOfFaces() == 0 || options.getMaxNumberOfFaces() < -1) {
                throw new IllegalArgumentException("MaxNumberOfFaces must be greater than 0 or -1, maxNumberOfFaces: " + options.getMaxNumberOfFaces());
            }
            if (options.getMinConfidence() < 0 || options.getMinConfidence() > 1) {
                throw new IllegalArgumentException("MinConfidence must be between 0 and 1");
            }
        }
    }
}
