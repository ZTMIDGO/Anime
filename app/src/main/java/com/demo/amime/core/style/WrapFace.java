package com.demo.amime.core.style;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Pair;

import com.demo.amime.bean.MyPoint;
import com.demo.amime.bean.OptionsConfig;
import com.demo.amime.core.base.Net;
import com.demo.amime.core.face.detection.FaceDetection;
import com.demo.amime.core.face.detection.models.FaceDetectionResult;
import com.demo.amime.core.face.landmark.FaceMeshDetection;
import com.demo.amime.manager.PathManager;
import com.demo.amime.utils.ArrayUtils;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import com.demo.amime.R;
import com.demo.amime.core.face.landmark.models.FaceMesh;
import com.demo.amime.core.face.landmark.models.FaceMeshResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by ZTMIDGO 2023/9/25
 */
public class WrapFace implements Net {
    private final float[] crop_size = new float[]{288, 288};
    private final double[][] f5p = new double[5][2];

    private final double[][] TreflectY = new double[][]{
            new double[]{-1, 0, 0},
            new double[]{0, 1, 0},
            new double[]{0, 0, 1},
    };

    private Context context;
    private FaceDetection faceDetection;
    private FaceMeshDetection meshDetection;
    private Mat mask;

    public WrapFace(Context context){
        this.context = context;
        faceDetection = new FaceDetection(context);
        meshDetection = new FaceMeshDetection(context);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        Mat global_mask = new Mat();
        Utils.bitmapToMat(BitmapFactory.decodeResource(context.getResources(), R.drawable.alpha, options).copy(Bitmap.Config.ARGB_8888, true), global_mask);
        Mat grayMat = new Mat();
        Imgproc.cvtColor(global_mask, grayMat, Imgproc.COLOR_RGB2GRAY);

        mask = new Mat(grayMat.rows(), grayMat.cols(), CvType.CV_32FC1);
        for (int i = 0; i < mask.rows(); i++){
            for (int j = 0; j < mask.cols(); j++){
                double[] value = grayMat.get(i, j);
                for (int x = 0; x < value.length; x++){
                    value[x] = value[x] / 255f;
                }
                mask.put(i, j, Arrays.copyOf(value, value.length));
            }
        }
    }

    public Bitmap forward(Bitmap source, Bitmap resultBitmap, DCTNet net, OptionsConfig config) throws Exception {
        if (!faceDetection.isComplete()) faceDetection.init(PathManager.getFaceDetectionModelPath(context));
        FaceDetectionResult detectionResult = faceDetection.detect(source);


        if (!meshDetection.isComplete()) meshDetection.init(PathManager.getFaceMeshModelPath(context));
        FaceMeshResult meshResult = meshDetection.detect(source, detectionResult.getRectFS());

        int[][][] colors = ArrayUtils.bitmapToRgb(resultBitmap);
        net.init(config.getModelPath() + "/" + config.getModelName(), 288, 288);

        for (FaceMesh mesh : meshResult.getFacesMesh()){
            List<MyPoint> points = mesh.getPoints();
            f5p[0] = points.get(468).arrayAsDouble();
            f5p[1] = points.get(473).arrayAsDouble();
            f5p[2] = points.get(1).arrayAsDouble();
            f5p[3] = points.get(61).arrayAsDouble();
            f5p[4] = points.get(291).arrayAsDouble();

            Pair<Bitmap, double[][]> pair = wrapCropFace(source, f5p);
            Bitmap head_img = pair.first;
            double[][] trans_inv = pair.second;

            Bitmap head_res = net.forward(head_img);

            long time = System.currentTimeMillis();
            Mat trans_inv_mat = new Mat(trans_inv.length, trans_inv[0].length, CvType.CV_32FC1);
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 3; j++) {
                    trans_inv_mat.put(i, j, trans_inv[i][j]);
                }
            }

            Mat head_res_mat = new Mat();
            Utils.bitmapToMat(head_res, head_res_mat);
            Mat head_trans_inv_mat = new Mat();

            Imgproc.warpAffine(head_res_mat, head_trans_inv_mat, trans_inv_mat, new Size(resultBitmap.getWidth(), resultBitmap.getHeight()), Imgproc.INTER_LINEAR, 0, new Scalar(0, 0, 0));

            Mat mask_trans_inv = new Mat();
            Imgproc.warpAffine(mask, mask_trans_inv, trans_inv_mat, new Size(resultBitmap.getWidth(), resultBitmap.getHeight()), Imgproc.INTER_LINEAR, 0, new Scalar(0, 0, 0));

            for (int y = 0; y < colors.length; y++){
                for (int x = 0; x < colors[0].length; x++){
                    for (int i = 0; i < colors[0][0].length; i++){
                        int value = colors[y][x][i];
                        colors[y][x][i] = (int) (mask_trans_inv.get(y, x)[0] * head_trans_inv_mat.get(y, x)[i] + (1f - mask_trans_inv.get(y, x)[0]) * value);
                    }
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(ArrayUtils.colorArrayToIntArray(colors), resultBitmap.getWidth(), resultBitmap.getHeight(), Bitmap.Config.RGB_565);
        return bitmap;
    }

    private Pair<Bitmap, double[][]> wrapCropFace(Bitmap src_img, double[][] src_pts){

        double[][] ref_pts = {
                new double[]{111.78243,  137.62857},
                new double[]{179.7399,   137.2527 },
                new double[]{145.97716,  176.27773},
                new double[]{118.059364, 216.06203},
                new double[]{174.33623,  215.75076}
        };

        Pair<double[][], double[][]> pair = similarityTransformCV2(src_pts, ref_pts);
        double[][] tfm = pair.first;
        double[][] tfm_inv = pair.second;
        Mat source = new Mat();

        Utils.bitmapToMat(src_img.copy(Bitmap.Config.ARGB_8888, true), source);

        Mat matrix = new Mat(tfm.length, tfm[0].length, CvType.CV_32FC1);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                matrix.put(i, j, tfm[i][j]);
            }
        }

        Mat dst = new Mat(source.rows(), source.cols(), source.type());
        Imgproc.warpAffine(source, dst, matrix, new Size(crop_size[0], crop_size[1]), Imgproc.INTER_LINEAR, 0, new Scalar(255, 255, 255));

        Bitmap bitmap = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, bitmap);

        return new Pair<>(bitmap, tfm_inv);
    }

    private Pair<double[][], double[][]> similarityTransformCV2(double[][] src_pts, double[][] dst_pts){
        Pair<double[][], double[][]> pair = similarityTransform(src_pts, dst_pts);
        double[][] trans = pair.first;
        double[][] trans_inv = pair.second;

        double[][] cv2_trans = cvt_tform_mat_for_cv2(trans);
        double[][] cv_trans_inv = cvt_tform_mat_for_cv2(trans_inv);
        return new Pair<>(cv2_trans, cv_trans_inv);
    }

    private double[][] cvt_tform_mat_for_cv2(double[][] trans){
        double[][] cv2_trans = new double[2][trans.length];
        for (int i = 0; i < cv2_trans.length; i++){
            double[] value = new double[trans.length];
            for (int j = 0; j < trans.length; j++){
                value[j] = trans[j][i];
            }
            cv2_trans[i] = value;
        }
        return cv2_trans;
    }

    private Pair<double[][], double[][]> similarityTransform(double[][] src_pts, double[][] dst_pts){
        return findSimilarity(src_pts, dst_pts);
    }

    private Pair<double[][], double[][]> findSimilarity(double[][] uv, double[][] xy){
        Pair<double[][], double[][]> pair = findNonreflectiveSimilarity(uv, xy, 0);
        double[][] trans1 = pair.first;
        double[][] trans1_inv = pair.second;
        for (int i = 0; i < xy.length; i++){
            xy[i][0] = -1 * xy[i][0];
        }

        pair = findNonreflectiveSimilarity(uv, xy, 1);
        double[][] trans2r = pair.first;
        double[][] trans2r_inv = pair.second;

        double[][] trans2 = ArrayUtils.dot(trans2r, TreflectY);
        double[][] xy1 = tformfwd(trans1, uv);
        double[][] narr = new double[xy1.length][xy1[0].length];
        for (int i = 0; i < narr.length; i++){
            for (int j = 0; j < narr[0].length; j++){
                narr[i][j] = xy1[i][j] - xy[i][j];
            }
        }
        double norm1 = ArrayUtils.norm(narr);

        double[][] xy2 = tformfwd(trans2, uv);
        narr = new double[xy2.length][xy2[0].length];
        for (int i = 0; i < narr.length; i++){
            for (int j = 0; j < narr[0].length; j++){
                narr[i][j] = xy2[i][j] - xy[i][j];
            }
        }
        double norm2 = ArrayUtils.norm(narr);

        if (norm1 <= norm2){
            return new Pair<>(trans1, trans1_inv);
        }else {
            trans2r_inv = ArrayUtils.pseudoInverse(trans2);
            return new Pair<>(trans2, trans2r_inv);
        }
    }

    private double[][] tformfwd(double[][] trans, double[][] uv){
        double[][] uv_hstack = new double[uv.length][uv[0].length + 1];
        for (int i = 0; i < uv.length; i++){
            for (int j = 0; j < uv[0].length; j++){
                uv_hstack[i][j] = uv[i][j];
            }
            uv_hstack[i][uv_hstack[0].length - 1] = 1;
        }
        double[][] xy = ArrayUtils.dot(uv_hstack, trans);
        double[][] result = new double[xy.length][xy[0].length - 1];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[0].length; j++) {
                result[i][j] = xy[i][j];
            }
        }
        return result;
    }

    private Pair<double[][], double[][]> findNonreflectiveSimilarity(double[][] uv, double[][] xy, int type){
        int K = 2;
        int M = xy.length;
        double[] x = new double[xy.length];
        double[] y = new double[xy.length];
        for (int i = 0; i < x.length; i++) {
            x[i] = xy[i][0];
            y[i] = xy[i][1];
        }

        double[][] tmp1 = new double[M][4];
        double[][] tmp2 = new double[M][4];
        for (int i = 0; i < M; i++){
            tmp1[i] = new double[]{x[i], y[i], 1, 0};
            tmp2[i] = new double[]{y[i], -x[i], 0, 1};
        }

        double[][] X = new double[tmp1.length + tmp2.length][4];
        for (int i = 0; i < X.length; i++){
            if (i < tmp1.length){
                X[i] = tmp1[i];
            }else {
                X[i] = tmp2[i - tmp1.length];
            }
        }

        double[] u = new double[uv.length];
        double[] v = new double[uv.length];

        for (int i = 0; i < uv.length; i++){
            u[i] = uv[i][0];
            v[i] = uv[i][1];
        }

        double[] U = new double[u.length + v.length];
        for (int i = 0; i < U.length; i++){
            if (i < tmp1.length){
                U[i] = u[i];
            }else {
                U[i] = v[i - tmp1.length];
            }
        }

        double[] r = ArrayUtils.leastSquaresSolution(X, U);

        double sc = r[0];
        double ss = r[1];
        double tx = r[2];
        double ty = r[3];

        double[][] Tinv = new double[3][3];
        Tinv[0] = new double[]{sc, -ss, 0};
        Tinv[1] = new double[]{ss, sc, 0};
        Tinv[2] = new double[]{tx, ty, 1};

        double[][] T = ArrayUtils.pseudoInverse(Tinv);
        T[0][2] = 0;
        T[1][2] = 0;
        T[2][2] = 1;
        return new Pair<>(T, Tinv);
    }

    @Override
    public void close() {
        if (faceDetection != null) faceDetection.close();
        if (meshDetection != null) meshDetection.close();
    }

    @Override
    public boolean isComplete() {
        return false;
    }
}
