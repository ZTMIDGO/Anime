package com.demo.amime.bean;

/**
 * Created by ZTMIDGO 2023/9/17
 */
public class OptionsConfig {
    private final String modelPath;
    private final String modelName;
    private final int width;
    private final int height;
    private final boolean lossAllowed;

    public OptionsConfig(String modelPath, String modelName, int width, int height, boolean lossAllowed) {
        this.modelPath = modelPath;
        this.modelName = modelName;
        this.width = width;
        this.height = height;
        this.lossAllowed = lossAllowed;
    }

    public String getModelPath() {
        return modelPath;
    }

    public String getModelName() {
        return modelName;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isLossAllowed() {
        return lossAllowed;
    }
}
