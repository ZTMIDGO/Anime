package com.demo.amime.manager;

import android.content.Context;

import com.demo.amime.bean.OptionsConfig;

/**
 * Created by ZTMIDGO 2023/9/1
 */
public class PathManager {

    public static final String getStylePath(Context context, String uuid){
        return "style/" + uuid;
    }

    public static final String getCachePath(Context context){
        return context.getFilesDir().getAbsolutePath() + "/cache";
    }

    public static final String getFaceDetectionModelPath(Context context){
        return "face_detection/model.tflite";
    }

    public static final String getFaceMeshModelPath(Context context){
        return "face_mesh/model.tflite";
    }

    public static OptionsConfig getStyleConfig(Context context, int id, int width, int height, boolean isHeader){
        String value = null;

        String name = "model.tflite";
        int w = 720, h = 720;
        boolean lossAllowed = true;

        switch (id){
            case 0:
                value = PathManager.getStylePath(context, "01");
                break;
            case 1:
                value = PathManager.getStylePath(context, "02");
                break;
            case 2:
                value = PathManager.getStylePath(context, "03");
                break;
            case 3:
                value = PathManager.getStylePath(context, "04");
                break;
            case 4:
                value = PathManager.getStylePath(context, "05");
                break;
            case 5:
                value = PathManager.getStylePath(context, "06");
                break;
            case 6:
                value = PathManager.getStylePath(context, "07");
                break;
        }

        if (isHeader){
            w = width;
            h = height;
        }

        return new OptionsConfig(value, isHeader ? "model_header.tflite" : name, w, h, lossAllowed);
    }

}
