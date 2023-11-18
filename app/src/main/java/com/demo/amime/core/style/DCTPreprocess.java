package com.demo.amime.core.style;

import android.graphics.Bitmap;

import com.demo.amime.utils.ArrayUtils;

/**
 * Created by ZTMIDGO 2023/9/20
 */
public class DCTPreprocess {
    private final long[] inputShape = {1, 512, 512, 3};

    public float[][][] getInput(Bitmap bitmap){
        int width = getWidth();
        int height = getHeight();

        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

        return ArrayUtils.bitmapToBGRAsFloat(bitmap);
    }

    public int[] decoder(float[][][][] output){
        int width = output[0][0].length;
        int height = output[0].length;
        int channel = output[0][0][0].length;

        int[][][] rgbs = new int[height][width][channel];

        for (int y = 0; y < height; y++){
            for (int x = 0; x <width; x++){
                for (int i = 0; i < channel; i++){
                    float value = output[0][y][x][channel - 1 - i];
                    if (value < -0.999999) value = -0.999999f;
                    if (value > 0.999999) value = 0.999999f;

                    int color = (int) ((value + 1.0f) * 127.5f);
                    if (color < 0) color = 0;
                    if (color > 255) color = 255;
                    rgbs[y][x][i] = color;
                }
            }
        }
        return ArrayUtils.colorArrayToIntArray(rgbs);
    }

    public int getWidth(){
        return (int) inputShape[2];
    }

    public int getHeight(){
        return (int) inputShape[1];
    }

    public void setWH(int width, int height){
        inputShape[2] = width;
        inputShape[1] = height;
    }
}
