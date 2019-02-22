package com.xjm.gupiao;

import java.io.Serializable;
import java.util.ArrayList;

public class ResultData implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<AllSharesBean> cuiziList = new ArrayList<>();   //倒锤子形态
    private ArrayList<AllSharesBean> zhongjiList = new ArrayList<>(); //上涨中继
    private ArrayList<AllSharesBean> minNumberList = new ArrayList<>();  //量能最低
    private ArrayList<AllSharesBean> lineList = new ArrayList<>();   //三线合一
    private ArrayList<AllSharesBean> starList = new ArrayList<>();   //小十字星
    private ArrayList<AllSharesBean> longtouList = new ArrayList<>();//龙头
    private ArrayList<AllSharesBean> xiaoboList = new ArrayList<>(); //小波动

    public ArrayList<AllSharesBean> getXiaoboList() {
        return xiaoboList;
    }

    public ArrayList<AllSharesBean> getCuiziList() {
        return cuiziList;
    }

    public ArrayList<AllSharesBean> getZhongjiList() {
        return zhongjiList;
    }

    public ArrayList<AllSharesBean> getMinNumberList() {
        return minNumberList;
    }

    public ArrayList<AllSharesBean> getLineList() {
        return lineList;
    }

    public ArrayList<AllSharesBean> getStarList() {
        return starList;
    }

    public ArrayList<AllSharesBean> getLongtouList() {
        return longtouList;
    }

    public void setCuiziList(ArrayList<AllSharesBean> cuiziList) {
        this.cuiziList = cuiziList;
    }

    public void setZhongjiList(ArrayList<AllSharesBean> zhongjiList) {
        this.zhongjiList = zhongjiList;
    }

    public void setLineList(ArrayList<AllSharesBean> lineList) {
        this.lineList = lineList;
    }

    public void setLongtouList(ArrayList<AllSharesBean> longtouList) {
        this.longtouList = longtouList;
    }

    public void setMinNumberList(ArrayList<AllSharesBean> minNumberList) {
        this.minNumberList = minNumberList;
    }

    public void setStarList(ArrayList<AllSharesBean> starList) {
        this.starList = starList;
    }

    public void setXiaoboList(ArrayList<AllSharesBean> xiaoboList) {
        this.xiaoboList = xiaoboList;
    }
}
