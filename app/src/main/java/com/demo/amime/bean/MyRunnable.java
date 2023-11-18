package com.demo.amime.bean;

public abstract class MyRunnable implements Runnable{
    private boolean interrupt;

    public void interrupt() {
        interrupt = true;
    }

    public boolean isInterrupt() {
        return interrupt;
    }
}
