package com.xjm.gupiao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class SharesListBean {
    @Id(autoincrement = true)
    private long ID;
    private long sharesID;
    private float height;
    private float low;
    private float open;
    private float close;
    private float oldClose;
    private long number;
    private float wave;

    @Generated(hash = 1487062602)
    public SharesListBean(long ID, long sharesID, float height, float low,
            float open, float close, float oldClose, long number, float wave) {
        this.ID = ID;
        this.sharesID = sharesID;
        this.height = height;
        this.low = low;
        this.open = open;
        this.close = close;
        this.oldClose = oldClose;
        this.number = number;
        this.wave = wave;
    }

    @Generated(hash = 2034999694)
    public SharesListBean() {
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getLow() {
        return low;
    }

    public void setLow(float low) {
        this.low = low;
    }

    public float getOpen() {
        return open;
    }

    public void setOpen(float open) {
        this.open = open;
    }

    public float getClose() {
        return close;
    }

    public void setClose(float close) {
        this.close = close;
    }

    public float getOldClose() {
        return oldClose;
    }

    public void setOldClose(float oldClose) {
        this.oldClose = oldClose;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public float getWave() {
        return wave;
    }

    public void setWave(float wave) {
        this.wave = wave;
    }

    public long getID() {
        return this.ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public long getSharesID() {
        return this.sharesID;
    }

    public void setSharesID(long sharesID) {
        this.sharesID = sharesID;
    }
}
