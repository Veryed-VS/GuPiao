package com.xjm.gupiao;

import java.io.Serializable;

public class AllSharesBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String code;   //代码
    private String name;   //名称
    private float number;  //成交额
    private String trade;  //行业

    public float getNumber() {
        return number;
    }

    public void setNumber(float number) {
        this.number = number;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        this.trade = trade;
    }
}
