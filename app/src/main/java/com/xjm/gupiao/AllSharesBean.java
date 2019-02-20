package com.xjm.gupiao;

public class AllSharesBean {
    private String code;
    private String name;
    private int mode;
    private String trade;
    private float wave;

    public AllSharesBean(String code, String name, int mode,float wave, String trade) {
        this.code = code;
        this.name = name;
        this.mode = mode;
        this.trade = trade;
        this.wave = wave;
    }

    public float getWave() {
        return wave;
    }

    public String getTrade() {
        return trade;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public int getMode() {
        return mode;
    }
}
