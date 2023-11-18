package com.demo.amime.core.face.detection;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Size;

import com.demo.amime.core.face.detection.models.Anchor;
import com.demo.amime.core.face.detection.models.AnchorOptions;
import com.demo.amime.core.face.detection.models.Face;
import com.demo.amime.core.face.detection.models.FaceDetectionOptions;
import com.demo.amime.core.face.detection.utils.AnchorGenerator;
import com.demo.amime.core.face.detection.utils.ImageProcessorUtil;
import com.demo.amime.core.face.detection.utils.TensorToFaces;
import com.demo.amime.core.base.Net;
import com.demo.amime.core.face.detection.models.FaceDetectionResult;
import com.demo.amime.core.face.detection.models.TensorToFacesOptions;
import com.demo.amime.utils.AndroidSystem;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ZTMIDGO 2023/8/31
 */
public class FaceDetection implements Net {
    private static final int IMAGE_WIDTH = 128;
    private static final int IMAGE_HEIGHT = 128;
    private static final float IMAGE_MEAN = 127.5f;
    private static final float IMAGE_STD = 127.5f;
    private final ImageProcessorUtil imageProcessorUtil;
    private final CompatibilityList compatList = new CompatibilityList();

    private float[][][] regressionOutput;
    private float[][][] classificationOutput;

    private final List<Anchor> anchors;
    private final TensorToFacesOptions detectionsOption;
    private final TensorToFaces tensorToFaces;

    private Context context;
    private Interpreter interpreter;
    private boolean isComplete;

    public FaceDetection(Context context){
        this(context, new FaceDetectionOptions.Builder().build());
    }

    public FaceDetection(Context context, FaceDetectionOptions options){
        this.context = context;
        imageProcessorUtil = new ImageProcessorUtil(IMAGE_MEAN, IMAGE_STD, IMAGE_WIDTH, IMAGE_HEIGHT);

        anchors = AnchorGenerator.generate(AnchorOptions.withDefaultValues());
        detectionsOption = TensorToFacesOptions.withDefaultValues(options.getMinConfidence(), options.getMaxNumberOfFaces());
        tensorToFaces = new TensorToFaces();
    }

    public void init(String modelPath) throws IOException {
        Interpreter.Options options = getInterpreterOptions();
        interpreter = new Interpreter(AndroidSystem.readAssetFile(context.getAssets(), modelPath), options);


        int[] regressionOutputShape = getOutputTensorShape(0);
        regressionOutput = new float[regressionOutputShape[0]][regressionOutputShape[1]][regressionOutputShape[2]];

        int[] classificationOutputShape = getOutputTensorShape(1);
        classificationOutput = new float[classificationOutputShape[0]][classificationOutputShape[1]][classificationOutputShape[2]];
        isComplete = true;
    }

    public FaceDetectionResult detect(Bitmap bitmap) throws Exception {
        Object[] inputs = getInputs(bitmap);
        Map<Integer, Object> outputs = getOutputs();
        interpreter.runForMultipleInputsOutputs(inputs, outputs);
        List<Face> faces = tensorToFaces.process(
                new Size(bitmap.getWidth(),
                        bitmap.getHeight()),
                detectionsOption,
                classificationOutput,
                regressionOutput,
                anchors);
        return new FaceDetectionResult(faces, bitmap);
    }

    protected Object[] getInputs(Bitmap input) {
        TensorImage image = new TensorImage(DataType.FLOAT32);
        image.load(input);
        ImageProcessor imageProcessor = imageProcessorUtil.getImageProcessor(input.getWidth(), input.getHeight());
        image = imageProcessor.process(image);
        return new Object[]{image.getBuffer()};
    }

    protected Map<Integer, Object> getOutputs() {
        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, regressionOutput);
        outputMap.put(1, classificationOutput);
        return outputMap;
    }

    protected Interpreter.Options getInterpreterOptions() {
        Interpreter.Options options = new Interpreter.Options();
        if (compatList.isDelegateSupportedOnThisDevice()){
            options.addDelegate(new GpuDelegate());
            options.setCancellable(true);
        }else {
            options.setNumThreads(4);
            options.setUseXNNPACK(true);
            options.setCancellable(true);
        }
        return options;
    }

    private int[] getOutputTensorShape(int index) {
        return interpreter.getOutputTensor(index).shape();
    }

    @Override
    public void close() {
        if (interpreter != null) interpreter.close();
        isComplete = false;
    }

    @Override
    public boolean isComplete() {
        return isComplete;
    }
}
