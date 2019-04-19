package com.xjm.gupiao;

import java.io.Serializable;

public class AllSharesBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String code;   //代码
    private String name;   //名称
    private int rank;      //10日排行

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
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
}
