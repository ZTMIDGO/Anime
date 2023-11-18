package com.demo.amime.core.face.landmark.models;

public final class FaceMeshOptions {

    private final float minConfidence;
    private final int maxNumberOfFaces;

    private FaceMeshOptions(Builder builder) {
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
        private int maxNumberOfFaces = 1;

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

        public FaceMeshOptions build() {
            FaceMeshOptions options = new FaceMeshOptions(this);
            validate(options);
            return options;
        }

        private static void validate(FaceMeshOptions options) {
            if (options.getMaxNumberOfFaces() < 1) {
                throw new IllegalArgumentException("MaxNumberOfFaces must be greater than 0, maxNumberOfFaces: " + options.getMaxNumberOfFaces());
            }
            if (options.getMinConfidence() < 0 || options.getMinConfidence() > 1) {
                throw new IllegalArgumentException("MinConfidence must be between 0 and 1");
            }
        }
    }
}
