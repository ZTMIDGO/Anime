package com.demo.amime.utils;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

/**
 * Created by ZTMIDGO 2023/8/23
 */
public class ArrayUtils {
    public static int rgbToInt(int[] rgb){
        return Color.rgb(rgb[0], rgb[1], rgb[2]);
    }

    public static int[] colorArrayToIntArray(int[][][] colorArray) {
        int height = colorArray.length;
        int width = colorArray[0].length;
        int[] result = new int[height * width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int red = colorArray[i][j][0];
                int green = colorArray[i][j][1];
                int blue = colorArray[i][j][2];
                result[i * width + j] = (red << 16) | (green << 8) | blue;
            }
        }
        return result;
    }

    public static int[] x32(int width, int height){
        float ratio = (height * 1f) / (width * 1f);

        int h = height < 256 ? 256 : height - height % 32;
        int w = (int) ((height / ratio) - (height / ratio) % 32);
        return new int[]{w, h};
    }

    public static int[] intToRGB(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        return new int[]{red, green, blue};
    }

    public static double[][][] gaussian(int[][][] data, double sigma) {
        int width = data.length;
        int height = data[0].length;
        int depth = data[0][0].length;
        double[][][] result = new double[width][height][depth];
        double sum = 0.2;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    double value = data[x][y][z];
                    double exponent = -(x * x + y * y + z * z) / (2 * sigma * sigma);
                    result[x][y][z] = value * Math.exp(exponent);
                    sum += result[x][y][z];
                }
            }
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    result[x][y][z] /= sum;
                }
            }
        }
        return result;
    }

    public static int getMax(int[][] array) {
        int max = array[0][0];
        for (int[] row : array) {
            for (int element : row) {
                if (element > max) {
                    max = element;
                }
            }
        }
        return max;
    }

    public static int getMin(int[][] array) {
        int min = array[0][0];
        for (int[] row : array) {
            for (int element : row) {
                if (element < min) {
                    min = element;
                }
            }
        }
        return min;
    }

    public static int getMax(int[] array) {
        int max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    public static int getMin(int[] array) {
        int min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    public static float getMax(float[][][][] arr) {
        float max = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                for (int k = 0; k < arr[i][j].length; k++) {
                    for (int l = 0; l < arr[i][j][k].length; l++) {
                        if (arr[i][j][k][l] > max) {
                            max = arr[i][j][k][l];
                        }
                    }
                }
            }
        }
        return max;
    }

    public static float getMin(float[][][][] arr) {
        float min = Float.MAX_VALUE;
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                for (int k = 0; k < arr[i][j].length; k++) {
                    for (int l = 0; l < arr[i][j][k].length; l++) {
                        if (arr[i][j][k][l] < min) {
                            min = arr[i][j][k][l];
                        }
                    }
                }
            }
        }
        return min;
    }

    public static int[][] interpolate(int[][] input, int[] shape) {
        int rows = shape[0];
        int cols = shape[1];
        int[][] output = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int x = (int) Math.round((double) i / rows * input.length);
                int y = (int) Math.round((double) j / cols * input[0].length);
                x = Math.min(x, input.length - 1);
                y = Math.min(y, input[0].length - 1);
                output[i][j] = input[x][y];
            }
        }
        return output;
    }

    public static int[][][] bitmapToRgb(Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[][][] result = new int[height][width][3];
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                int color = bitmap.getPixel(x, y);
                int[] rgb = new int[]{red(color), green(color), blue(color)};
                result[y][x][0] = rgb[0];
                result[y][x][1] = rgb[1];
                result[y][x][2] = rgb[2];
            }
        }
        return result;
    }

    public static float[][][] bitmapToBGRAsFloat(Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float[][][] result = new float[height][width][3];
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                int color = bitmap.getPixel(x, y);
                int[] rgb = new int[]{red(color), green(color), blue(color)};
                result[y][x][0] = rgb[2];
                result[y][x][1] = rgb[1];
                result[y][x][2] = rgb[0];
            }
        }
        return result;
    }

    public static float[] arange(float start, float stop, Float step) {
        int size = (int) (step != null ? Math.ceil((stop - start) / step) : Math.ceil(stop - start));
        float[] result = new float[size];
        for (int i = 0; i < size; i++) {
            result[i] = step != null ? start + i * step : start + i;
        }
        return result;
    }

    public static double[] arange(double start, double stop, Float step) {
        float[] arange = arange((float) start, (float) stop, step);
        double[] result = new double[arange.length];
        for (int i = 0; i < result.length; i++) result[i] = arange[i];
        return result;
    }

    public static double quadraticInterpolation(double[] x, double[] y, double xi) {
        int n = x.length;
        double result = 0;
        for (int i = 0; i < n; i++) {
            double term = y[i];
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    term = term * (xi - x[j]) / (x[i] - x[j]);
                }
            }
            result += term;
        }
        return result;
    }

    public static float[] rgbToLab(int R, int G, int B) {
        double r, g, b, X, Y, Z, xr, yr, zr;

        double Xr = 95.047;
        double Yr = 100.0;
        double Zr = 108.883;

        r = R/255.0;
        g = G/255.0;
        b = B/255.0;

        if (r > 0.04045)
            r = Math.pow((r+0.055)/1.055,2.4);
        else
            r = r/12.92;

        if (g > 0.04045)
            g = Math.pow((g+0.055)/1.055,2.4);
        else
            g = g/12.92;

        if (b > 0.04045)
            b = Math.pow((b+0.055)/1.055,2.4);
        else
            b = b/12.92 ;

        r*=100;
        g*=100;
        b*=100;

        X =  0.4124*r + 0.3576*g + 0.1805*b;
        Y =  0.2126*r + 0.7152*g + 0.0722*b;
        Z =  0.0193*r + 0.1192*g + 0.9505*b;

        xr = X/Xr;
        yr = Y/Yr;
        zr = Z/Zr;

        if ( xr > 0.008856 )
            xr =  (float) Math.pow(xr, 1/3.);
        else
            xr = (float) ((7.787 * xr) + 16 / 116.0);

        if ( yr > 0.008856 )
            yr =  (float) Math.pow(yr, 1/3.);
        else
            yr = (float) ((7.787 * yr) + 16 / 116.0);

        if ( zr > 0.008856 )
            zr =  (float) Math.pow(zr, 1/3.);
        else
            zr = (float) ((7.787 * zr) + 16 / 116.0);


        float[] lab = new float[3];

        lab[0] = (float) ((116*yr)-16);
        lab[1] = (float) (500*(xr-yr));
        lab[2] = (float) (200*(yr-zr));

        return lab;
    }

    public static float[][][] rgbToLab(int[][][] colors){
        float[][][] labs = new float[colors.length][colors[0].length][3];
        for (int y = 0; y < labs.length; y++){
            for (int x = 0; x < labs[0].length; x++){
                int[] rgb = colors[y][x];
                labs[y][x] = rgbToLab(rgb[0], rgb[1], rgb[2]);
            }
        }
        return labs;
    }

    public static int average(int[] array){
        int number = 0;
        for (int val : array) number += val;
        return number / array.length;
    }

    public static double[][] pseudoInverse(double[][] data) {
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        SingularValueDecomposition svd = new SingularValueDecomposition(matrix);
        RealMatrix pseudoInverse = svd.getSolver().getInverse();
        return pseudoInverse.getData();
    }

    public static double[] leastSquaresSolution(double[][] coefficients, double[] constants) {
        RealMatrix coefficientsMatrix = new Array2DRowRealMatrix(coefficients, true);
        DecompositionSolver solver = new QRDecomposition(coefficientsMatrix).getSolver();
        RealVector constantsVector = new ArrayRealVector(constants, true);
        RealVector solution = solver.solve(constantsVector);
        return solution.toArray();
    }

    public static double[][] dot(double[][] a, double[][] b){
        double[][] result = new double[a.length][a[0].length];
        for (int i = 0; i < result.length; i++){
            for (int j = 0; j < result[0].length; j++){
                double value = 0;
                for (int k = 0; k < b.length; k++){
                    value += a[i][j] * b[k][j];
                }
                result[i][j] = value;
            }
        }
        return result;
    }

    public static double norm(double[][] array) {
        double sum = 0;
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                sum += Math.pow(array[i][j], 2);
            }
        }
        return Math.sqrt(sum);
    }
}
