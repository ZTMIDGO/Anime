package com.demo.amime.bean;

import android.graphics.Point;

/**
 * Created by ZTMIDGO 2023/9/13
 */
public class MyPoint extends Point {
    public MyPoint() {
        super();
    }

    public MyPoint(int x, int y) {
        super(x, y);
    }

    public MyPoint(Point src) {
        super(src);
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public double[] arrayAsDouble(){
        return new double[]{x, y};
    }
}
