package com.xjm.gupiao;

public class ProgressBean {
    public ProgressBean(int max,int progress){
        this.max = max;
        this.progress = progress;
    }
    private int max;
    private int progress;

    public int getProgress() {
        return progress;
    }

    public int getMax() {
        return max;
    }
}
