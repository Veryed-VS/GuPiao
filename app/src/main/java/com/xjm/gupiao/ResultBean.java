package com.xjm.gupiao;

import java.io.Serializable;

public class ResultBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String code;
    private String name;
    private String trade;
    private int mode;
    private double number;  //成交额排序

    public ResultBean(int mode,String code, String name, String trade,double number) {
        this.mode = mode;
        this.code = code;
        this.name = name;
        this.trade = trade;
        this.number = number;
    }

    public double getNumber() {
        return number;
    }

    public int getMode() {
        return mode;
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
}
