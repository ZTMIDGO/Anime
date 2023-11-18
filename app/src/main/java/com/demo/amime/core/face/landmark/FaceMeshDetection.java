package com.demo.amime.core.face.landmark;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Size;

import com.demo.amime.core.face.landmark.models.FaceMesh;
import com.demo.amime.core.face.landmark.models.FaceMeshOptions;
import com.demo.amime.core.face.landmark.models.FaceMeshResult;
import com.demo.amime.core.face.landmark.models.TensorToMeshOptions;
import com.demo.amime.core.face.landmark.utils.CropOp;
import com.demo.amime.core.face.landmark.utils.RectTransformation;
import com.demo.amime.core.face.landmark.utils.TensorToMesh;
import com.demo.amime.core.base.Net;
import com.demo.amime.utils.AndroidSystem;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ZTMIDGO 2023/8/31
 */
public class FaceMeshDetection implements Net {
    private static final String MODEL_PATH = "face_landmark.tflite";
    private static final int IMAGE_WIDTH = 256;
    private static final int IMAGE_HEIGHT = 256;
    private static final float IMAGE_MEAN = 0.0f;
    private static final float IMAGE_STD = 255.0f;
    private final ImageProcessor resizeAndNormalizeProcessor;
    private final TensorToMeshOptions tensorToMeshOptions;
    private final CompatibilityList compatList = new CompatibilityList();
    private final TensorToMesh tensorToMesh;

    private float[][][][] regressionOutput;
    private float[][][][] classificationOutput;
    private Interpreter interpreter;
    private Context context;
    private boolean isComplete;

    public FaceMeshDetection(Context context){
        this(context, new FaceMeshOptions.Builder().build());
    }

    public FaceMeshDetection(Context context, FaceMeshOptions options){
        this.context = context;

        this.resizeAndNormalizeProcessor = new ImageProcessor.Builder().
                add(new ResizeOp(IMAGE_HEIGHT, IMAGE_WIDTH, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR)).
                add(new NormalizeOp(IMAGE_MEAN, IMAGE_STD)).
                build();

        tensorToMeshOptions = TensorToMeshOptions.withDefaultValues();
        tensorToMesh = new TensorToMesh();
    }

    public void init(String modelPath) throws IOException {
        Interpreter.Options options = this.getInterpreterOptions();
        interpreter = new Interpreter(AndroidSystem.readAssetFile(context.getAssets(), modelPath), options);

        int[] regressionOutputShape = getOutputTensorShape(0);
        regressionOutput = new float[regressionOutputShape[0]][regressionOutputShape[1]][regressionOutputShape[2]][regressionOutputShape[3]];

        int[] classificationOutputShape = getOutputTensorShape(1);
        classificationOutput = new float[classificationOutputShape[0]][classificationOutputShape[1]][classificationOutputShape[2]][classificationOutputShape[3]];
        isComplete = true;
    }

    public FaceMeshResult detect(Bitmap bitmap, List<RectF> faces){
        FaceMeshResult result = new FaceMeshResult();
        Size bitmapSize = new Size(bitmap.getWidth(), bitmap.getHeight());

        List<FaceMesh> facesMesh = new ArrayList<>();

        result.setFacesMesh(facesMesh);
        result.setInputBitmap(bitmap);
        for (RectF face : faces) {
            RectF roi = RectTransformation.transform(RectTransformation.unNormalizeRectF(face, bitmapSize), 0);

            TensorImage croppedTensor = new TensorImage(DataType.FLOAT32);
            croppedTensor.load(bitmap);
            Bitmap croppedBitmap = new ImageProcessor.Builder().add(new CropOp(roi)).build().process(croppedTensor).getBitmap();

            FaceMesh faceMesh = detect(croppedBitmap, bitmap.getWidth(), bitmap.getHeight(), roi);
            if (faceMesh != null) facesMesh.add(faceMesh);
        }
        return result;
    }

    private FaceMesh detect(Bitmap bitmap, int width, int height, RectF roi){
        Object[] inputs = getInputs(bitmap);
        Map<Integer, Object> outputs = getOutputs();
        interpreter.runForMultipleInputsOutputs(inputs, outputs);

        FaceMesh faceMesh = this.tensorToMesh.process(
                new Size(width, height),
                this.tensorToMeshOptions,
                this.classificationOutput,
                this.regressionOutput, roi
        );
        return faceMesh;
    }


    private MappedByteBuffer loadModel(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
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

    protected Object[] getInputs(Bitmap input) {
        TensorImage image = new TensorImage(DataType.FLOAT32);
        image.load(input);
        image = resizeAndNormalizeProcessor.process(image);
        return new Object[]{image.getBuffer()};
    }

    protected Map<Integer, Object> getOutputs() {
        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, regressionOutput);
        outputMap.put(1, classificationOutput);
        return outputMap;
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
