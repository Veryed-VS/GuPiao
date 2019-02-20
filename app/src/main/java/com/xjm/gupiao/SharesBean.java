package com.xjm.gupiao;

public class SharesBean {
	private float openPrice;  	//开盘价
	private float closePrice; 	//收盘价
	private float heightPrice; 	//最高价
	private float lowPrice;   	//最低价
	private long number;      	//成交量
	private float turnover;   	//换手率
	private float wave;         //涨跌幅

	public float getWave() {
		return wave;
	}

	public void setWave(float wave) {
		this.wave = wave;
	}

	public float getHeightPrice() {
		return heightPrice;
	}

	public void setHeightPrice(float heightPrice) {
		this.heightPrice = heightPrice;
	}

	public float getOpenPrice() {
		return openPrice;
	}

	public void setOpenPrice(float openPrice) {
		this.openPrice = openPrice;
	}

	public float getClosePrice() {
		return closePrice;
	}

	public void setClosePrice(float closePrice) {
		this.closePrice = closePrice;
	}

	public float getLowPrice() {
		return lowPrice;
	}

	public void setLowPrice(float lowPrice) {
		this.lowPrice = lowPrice;
	}

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}

	public float getTurnover() {
		return turnover;
	}

	public void setTurnover(float turnover) {
		this.turnover = turnover;
	}
}
