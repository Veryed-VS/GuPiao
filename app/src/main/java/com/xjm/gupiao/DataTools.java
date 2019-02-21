package com.xjm.gupiao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DataTools {
    public static final String ALL_CODE_URL = "http://nufm.dfcfw.com/EM_Finance2014NumericApplication/JS.aspx/JS.aspx?"
            + "type=ct&st=(FFRank)&sr=1&p=1&ps=5000&js=var%20mozselQI=%7bpages:(pc),"
            + "data:[(x)]%7d&token=894050c76af8597a853f5b408b759f5d&cmd=C._AB&sty=DCFFITAM&rt=49461817";
    public static final String SHARES_OLD_URL_1 = "http://quotes.money.163.com/service/chddata.html?code=";

    public static final String SHARES_PAN_DATA = "http://hq.sinajs.cn/list=";

    public static final String getSHARES_PAN_URL(String code) {
        return SHARES_PAN_DATA + (code.startsWith("0") ? "sz" : "sh") + code;
    }

    public static String getSHARES_OLD_URL(int mode, String code) {
        return SHARES_OLD_URL_1 + mode + code
                + "&start=" + DataTools.getDateBefore()
                + "&end=" + DataTools.getDateNext()
                + "&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;VOTURNOVER;TURNOVER";
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

    public static String getDateBefore() {
        Date nowDate = new Date();
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTime(nowDate);
        nowCalendar.set(Calendar.DATE, nowCalendar.get(Calendar.DATE) - 60);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String result = sdf.format(nowCalendar.getTime());
        return result;
    }
}
