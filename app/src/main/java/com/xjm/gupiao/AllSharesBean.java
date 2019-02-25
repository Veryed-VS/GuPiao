package com.xjm.gupiao;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class AllSharesBean implements Parcelable, Serializable {
    private static final long serialVersionUID = 1L;
    private String code;   //代码
    private String name;   //名称
    private int ranking;   //今日主力排行
    private String trade;  //行业

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

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        this.trade = trade;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.code);
        dest.writeString(this.name);
        dest.writeInt(this.ranking);
        dest.writeString(this.trade);
    }

    public AllSharesBean() {
    }

    protected AllSharesBean(Parcel in) {
        this.code = in.readString();
        this.name = in.readString();
        this.ranking = in.readInt();
        this.trade = in.readString();
    }

    public static final Creator<AllSharesBean> CREATOR = new Creator<AllSharesBean>() {
        @Override
        public AllSharesBean createFromParcel(Parcel source) {
            return new AllSharesBean(source);
        }

        @Override
        public AllSharesBean[] newArray(int size) {
            return new AllSharesBean[size];
        }
    };
}
