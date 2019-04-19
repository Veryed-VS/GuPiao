package com.xjm.gupiao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DataTools {
    //获取当天所有股票：模式,代码,名称,当前价格,主力流入净占比,今日主力排行,今日涨幅,5日主力流入净占比,5日主力排行,5日涨幅,10日主力流入净占比,10日主力排行,10日涨幅,行业
    public static final String ALL_CODE_URL = "http://nufm.dfcfw.com/EM_Finance2014NumericApplication/JS.aspx/JS.aspx?"
            + "type=ct&st=(FFRank)&sr=1&p=1&ps=5000&js=var%20mozselQI=%7bpages:(pc),"
            + "data:[(x)]%7d&token=894050c76af8597a853f5b408b759f5d&cmd=C._AB&sty=DCFFITAM&rt=49461817";

    public static final String image_url = "http://image.sinajs.cn/newchart/daily/n/";
    public static final String min_image_url = "http://image.sinajs.cn/newchart/min/n/";

    //当天详细数据：名称   开盘价   前收盘价   当前价格   最高价   最低价    未知   未知   成交量   成交额
    private static final String SHARES_PAN_DATA = "http://hq.sinajs.cn/list=";
    public static final String getSHARES_PAN_URL(String code) {
        return SHARES_PAN_DATA + (code.startsWith("0") ? "sz" : "sh") + code;
    }

    //历史数据：日期 股票代码 名称 收盘价 最高价 最低价 开盘价 前收盘 涨跌幅 成交量
    private static final String SHARES_OLD_URL_1 = "http://quotes.money.163.com/service/chddata.html?code=";
    public static String getSHARES_OLD_URL(int mode, String code,int day) {
        return SHARES_OLD_URL_1 + mode + code
                + "&start=" + DataTools.getDateBefore(day)
                + "&end=" + DataTools.getDateNext()
                + "&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;PCHG;VOTURNOVER";
    }

    public static String getDateNext() {
        Date nowDate = new Date();
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTime(nowDate);
        nowCalendar.set(Calendar.DATE, nowCalendar.get(Calendar.DATE));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String result = sdf.format(nowCalendar.getTime());
        return result;
    }

    public static String getDateBefore(int day) {
        Date nowDate = new Date();
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTime(nowDate);
        nowCalendar.set(Calendar.DATE, nowCalendar.get(Calendar.DATE) - day);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String result = sdf.format(nowCalendar.getTime());
        return result;
    }
}
