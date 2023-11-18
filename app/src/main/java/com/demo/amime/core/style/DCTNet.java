package com.demo.amime.core.style;

import android.content.Context;
import android.graphics.Bitmap;

import com.demo.amime.core.base.Net;
import com.demo.amime.utils.AndroidSystem;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.File;
import java.io.IOException;

/**
 * Created by ZTMIDGO 2023/9/20
 */
public class DCTNet implements Net {
    private final DCTPreprocess preprocess = new DCTPreprocess();
    private final CompatibilityList compatList = new CompatibilityList();
    private final Context context;
    private Interpreter session;
    private boolean isComplete;

    public DCTNet(Context context){
        this.context = context;
    }

    public void init(String modelPath, int width, int height) throws IOException {
        Interpreter.Options options = getInterpreterOptions();
        session = new Interpreter(AndroidSystem.readAssetFile(context.getAssets(), modelPath), options);
        preprocess.setWH(width, height);
        isComplete = true;
    }

    public Bitmap forward(Bitmap bitmap) throws Exception {
        float[][][][] output = new float[1][preprocess.getHeight()][preprocess.getWidth()][3];
        session.run(preprocess.getInput(bitmap), output);
        int[] decoder = preprocess.decoder(output);
        return Bitmap.createBitmap(decoder, preprocess.getWidth(), preprocess.getHeight(), Bitmap.Config.RGB_565) ;
    }

    @Override
    public void close() {
        if (session != null) session.close();
        session = null;
        isComplete = false;
    }

    @Override
    public boolean isComplete() {
        return isComplete;
    }

    protected Interpreter.Options getInterpreterOptions() {
        Interpreter.Options options = new Interpreter.Options();
        if(compatList.isDelegateSupportedOnThisDevice()) {
            options.addDelegate(new GpuDelegate());
        }else {
            options.setNumThreads(4);
            options.setUseXNNPACK(true);
        }
        return options;
    }

    public int getWidth(){
        return preprocess.getWidth();
    }

    public int getHeight(){
        return preprocess.getHeight();
    }
}
