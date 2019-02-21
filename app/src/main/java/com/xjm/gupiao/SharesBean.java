package com.xjm.gupiao;

public class SharesBean {
	private float openPrice;
	private float closePrice; 	//收盘价
	private long number;      	//成交量
	private float wave;         //涨跌幅

	public float getOpenPrice() {
		return openPrice;
	}

	public void setOpenPrice(float openPrice) {
		this.openPrice = openPrice;
	}

	public float getWave() {
		return wave;
	}

	public void setWave(float wave) {
		this.wave = wave;
	}

	public float getClosePrice() {
		return closePrice;
	}

	public void setClosePrice(float closePrice) {
		this.closePrice = closePrice;
	}

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}
}
