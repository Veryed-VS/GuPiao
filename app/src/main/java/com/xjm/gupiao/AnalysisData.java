package com.xjm.gupiao;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AnalysisData {
    private HttpClient httpClient1 = new DefaultHttpClient();
    private HttpClient httpClient2 = new DefaultHttpClient();
    private HttpClient httpClient3 = new DefaultHttpClient();
    private ArrayList<AllSharesBean> cuiziList = new ArrayList<>();   //倒锤子形态
    private ArrayList<AllSharesBean> zhongjiList = new ArrayList<>(); //上涨中继
    private ArrayList<AllSharesBean> minNumberList = new ArrayList<>();  //量能最低
    private ArrayList<AllSharesBean> lineList = new ArrayList<>();   //三线合一
    private ArrayList<AllSharesBean> starList = new ArrayList<>();   //小十字星
    private ArrayList<AllSharesBean> longtouList = new ArrayList<>();//龙头
    private ArrayList<AllSharesBean> xiaoboList = new ArrayList<>(); //小波动

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

    public ArrayList<AllSharesBean> getXiaoboList() {
        return xiaoboList;
    }

    public void setXiaoboList(ArrayList<AllSharesBean> xiaoboList) {
        this.xiaoboList = xiaoboList;
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

    private void cleanAll(){
        cuiziList.clear();
        zhongjiList.clear();
        lineList.clear();
        longtouList.clear();
        minNumberList.clear();
        starList.clear();
        xiaoboList.clear();
    }

    public void runAnalysisData() throws IOException, JSONException {
        cleanAll();

        HttpGet httpGet = new HttpGet(DataTools.ALL_CODE_URL);
        HttpResponse httpResponse = httpClient1.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();
        if (httpEntity == null) {
            return;
        }
        InputStream is = httpEntity.getContent();
        // 转换为字节输入流
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null) {
            buffer.append(line);
        }
        String content = buffer.toString().replace("var mozselQI=", "");
        ObjectMapper mapper = new ObjectMapper();
        // 解决key没有双引号的关键代码
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 忽略不需要的字段
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode node = mapper.readTree(content);
        String userJson = node.get("data").toString();
        JSONArray jsonArray = new JSONArray(userJson);
        int arrayLength = jsonArray.length();

        for (int i = 0; i < arrayLength; i++) {
            String jsonStr = jsonArray.get(i).toString();
            String[] sharesStr = jsonStr.split(",");
            boolean isError3 = false;
            for (int j = 0; j < sharesStr.length; j++) {
                if (sharesStr[j].equals("None") || sharesStr[j].equals("-")) {
                    isError3 = true;
                }
            }
            if (isError3) {
                continue;
            }
            // 买不到不考虑
            if (!sharesStr[1].startsWith("0") && !sharesStr[1].startsWith("6")) {
                continue;
            }
            // 停牌的
            if (sharesStr[6].equals("-")) {
                continue;
            }
            // ST不考虑
            if (sharesStr[2].startsWith("ST") || sharesStr[2].startsWith("*ST")) {
                continue;
            }
            // 价格低于3块高于30的不考虑
            float currentPrice = Float.valueOf(sharesStr[3]);
            if (currentPrice < 3 || currentPrice > 40) {
                continue;
            }
            float currentWave = Float.valueOf(sharesStr[6]);  //当前涨幅
            if (currentWave < -3) {
                continue;
            }
            EventBus.getDefault().post(new ProgressBean(arrayLength, i));

            //模式,代码,名称,当前价格,主力流入净占比,今日主力排行,今日涨幅,5日主力流入净占比,5日主力排行,5日涨幅,10日主力流入净占比,10日主力排行,10日涨幅,行业
            String code = sharesStr[1];       //代码
            String name = sharesStr[2];       //名称
            String trade = sharesStr[13];     //行业
            int ranking = Integer.valueOf(5); //今日主力排行
            int mode = Integer.valueOf(sharesStr[0].replace("\"", "")) - 1; //模式

            //今日具体数据
            HttpGet httpGet1 = new HttpGet(DataTools.getSHARES_PAN_URL(code));
            HttpResponse httpResponse1 = httpClient2.execute(httpGet1);
            HttpEntity httpEntity1 = httpResponse1.getEntity();
            if (httpEntity1 == null) {
                continue;
            }
            InputStream is1 = httpEntity1.getContent();
            // 转换为字节输入流
            BufferedReader br1 = new BufferedReader(new InputStreamReader(is1, "utf-8"));
            StringBuffer buffer1 = new StringBuffer();
            String line1;
            while ((line1 = br1.readLine()) != null) {
                buffer1.append(line1);
            }
            String[] line_data = buffer1.toString().split(",");
            //名称   开盘价   前收盘价   当前价格   最高价   最低价    未知   未知   成交量   成交额
            float open = Float.valueOf(line_data[1]);     // 开盘价
            float old_open = Float.valueOf(line_data[2]); // 前收盘
            float close = Float.valueOf(line_data[3]);    // 当前价
            float height = Float.valueOf(line_data[4]);   // 最高价
            float low = Float.valueOf(line_data[5]);      // 最低价
            long number = Long.valueOf(line_data[8]);     // 成交量

            HttpGet httpGet2 = new HttpGet(DataTools.getSHARES_OLD_URL(mode, code));
            HttpResponse httpResponse2 = httpClient3.execute(httpGet2);
            HttpEntity httpEntity2 = httpResponse2.getEntity();
            if (httpEntity2 == null) {
                continue;
            }
            ArrayList<SharesBean> sharesBeans = new ArrayList<>();
            InputStream is2 = httpEntity2.getContent();
            BufferedReader br2 = new BufferedReader(new InputStreamReader(is2, "utf-8"));
            br2.readLine(); // 第一行信息，为标题信息
            //日期 股票代码 名称 收盘价 最高价 最低价 开盘价 前收盘 涨跌额 涨跌幅 成交量 换手率
            String lineStr;
            while ((lineStr = br2.readLine()) != null) {
                String line_item[] = lineStr.split(",");// CSV格式文件为逗号分隔符文件
                boolean isError = false;
                for (int j = 0; j < line_item.length; j++) {
                    if (line_item[j].equals("None") || line_item[j].equals("-")) {
                        isError = true;
                    }
                }
                if (isError) {
                    continue;
                }

                //可能盘后运行   不要第一行数据
                if(Float.valueOf(line_item[3]) == currentPrice && number == Long.valueOf(line_item[10])){
                    continue;
                }

                SharesBean bean = new SharesBean();
                bean.setOpenPrice(Float.valueOf(line_item[6]));     // 开盘价
                bean.setClosePrice(Float.valueOf(line_item[3]));    // 收盘价
                bean.setNumber(Long.valueOf(line_item[10]));        // 成交量
                bean.setWave(Float.valueOf(9));                     // 涨跌幅
                sharesBeans.add(bean);
            }

            if (sharesBeans.size() < 20) {
                continue;
            }

            float priceCount = currentPrice;
            for (int a = 0; a < 4; a++) {
                priceCount += sharesBeans.get(a).getClosePrice();
            }
            float fivePrice = priceCount / 5.0f;
            priceCount = currentPrice;
            for (int a = 0; a < 9; a++) {
                priceCount += sharesBeans.get(a).getClosePrice();
            }
            float tenPrice = priceCount / 10.0f;
            priceCount = currentPrice;
            for (int a = 0; a < 19; a++) {
                priceCount += sharesBeans.get(a).getClosePrice();
            }
            float twentyPrice = priceCount / 20.f;

            //龙头股
            if(currentWave >= 6){
                AllSharesBean allSharesBean = new AllSharesBean();
                allSharesBean.setCode(code);
                allSharesBean.setName(name);
                allSharesBean.setMode(mode);
                allSharesBean.setRanking(ranking);
                allSharesBean.setTrade(trade);
                longtouList.add(allSharesBean);
            }

            //三线合一
            float maxPrice = Math.max(Math.max(fivePrice, tenPrice), Math.max(fivePrice, twentyPrice));
            float minPrice = Math.min(Math.min(fivePrice, tenPrice), Math.min(fivePrice, twentyPrice));
            if (((maxPrice - minPrice) / currentPrice) <= 0.008) {
                if (currentPrice > maxPrice) {
                    AllSharesBean allSharesBean = new AllSharesBean();
                    allSharesBean.setCode(code);
                    allSharesBean.setName(name);
                    allSharesBean.setMode(mode);
                    allSharesBean.setRanking(ranking);
                    allSharesBean.setTrade(trade);
                    lineList.add(allSharesBean);
                }
                continue;
            }

            //找倒锤子形态
            if (currentWave < 3 && currentWave > -2) {
                //不可以跳空
                if ((Math.abs(open - old_open)) / old_open < 0.008) {
                    if (open < close) {  //涨
                        if (((height - close) >= 1.5 * (close - open)) && ((height - close) >= 1.5 * (open - low))) {
                            if (currentPrice > maxPrice) {
                                AllSharesBean allSharesBean = new AllSharesBean();
                                allSharesBean.setCode(code);
                                allSharesBean.setName(name);
                                allSharesBean.setMode(mode);
                                allSharesBean.setRanking(ranking);
                                allSharesBean.setTrade(trade);
                                cuiziList.add(allSharesBean);
                            }
                            continue;
                        }
                    } else {  //跌
                        if (((height - close) >= 1.5 * (open - close)) && ((height - open) >= 1.5 * (close - low))) {
                            if (currentPrice > maxPrice) {
                                AllSharesBean allSharesBean = new AllSharesBean();
                                allSharesBean.setCode(code);
                                allSharesBean.setName(name);
                                allSharesBean.setMode(mode);
                                allSharesBean.setRanking(ranking);
                                allSharesBean.setTrade(trade);
                                cuiziList.add(allSharesBean);
                            }
                            continue;
                        }
                    }
                }
            }

            //另一种倒锤子形态
            if (currentWave >= 1.5) {
                if ((Math.abs(open - old_open)) / old_open < 0.008) {
                    if (((height - close) > (open - low)) && ((height - close) > (close - open))) {
                        if (currentPrice > maxPrice) {
                            AllSharesBean allSharesBean = new AllSharesBean();
                            allSharesBean.setCode(code);
                            allSharesBean.setName(name);
                            allSharesBean.setMode(mode);
                            allSharesBean.setRanking(ranking);
                            allSharesBean.setTrade(trade);
                            cuiziList.add(allSharesBean);
                        }
                    }
                }
            }
            float maxMinDiff = Math.abs(height - low);
            float openCloseDiff = Math.abs(open - close);
            //小十字星
            if (currentWave > -2 && currentWave < 2) {
                if ((Math.abs(open - old_open)) / old_open < 0.008) {
                    if ((openCloseDiff == 0 || maxMinDiff / openCloseDiff >= 3)
                            && (maxMinDiff / open) <= 0.03) {
                        AllSharesBean allSharesBean = new AllSharesBean();
                        allSharesBean.setCode(code);
                        allSharesBean.setName(name);
                        allSharesBean.setMode(mode);
                        allSharesBean.setRanking(ranking);
                        allSharesBean.setTrade(trade);
                        starList.add(allSharesBean);
                    }
                }
            }

            //上涨中继
            if (currentWave > -2 && currentWave < 2) {
                if (openCloseDiff == 0 || maxMinDiff / openCloseDiff >= 4) {
                    SharesBean sharesBean = sharesBeans.get(0);
                    //今天不能跳空
                    if ((Math.abs(open - old_open)) / old_open < 0.008) {
                        //昨天不能是阴涨  也不能是跳空涨
                        if ((sharesBean.getClosePrice() > sharesBean.getOpenPrice())
                                && (sharesBean.getClosePrice() - sharesBean.getOpenPrice()) / sharesBean.getOpenPrice() >= 0.03) {
                            AllSharesBean allSharesBean = new AllSharesBean();
                            allSharesBean.setCode(code);
                            allSharesBean.setName(name);
                            allSharesBean.setMode(mode);
                            allSharesBean.setRanking(ranking);
                            allSharesBean.setTrade(trade);
                            zhongjiList.add(allSharesBean);
                        }
                    }
                }
            }

            //5日量能最小
            if (currentWave < 2 && currentWave > -5) {
                boolean isNumberMin = true;
                for (int a = 0; a < 6; a++) {
                    if (number > sharesBeans.get(a).getNumber()) {
                        isNumberMin = false;
                        break;
                    }
                }
                if (isNumberMin) {
                    AllSharesBean allSharesBean = new AllSharesBean();
                    allSharesBean.setCode(code);
                    allSharesBean.setName(name);
                    allSharesBean.setMode(mode);
                    allSharesBean.setRanking(ranking);
                    allSharesBean.setTrade(trade);
                    minNumberList.add(allSharesBean);
                }

                //十天量能一半
                long numberCount = 0;
                int numberIndex = 0;
                for (int  a = 0; a < 10; a++) {
                    numberCount += sharesBeans.get(a).getNumber();
                    numberIndex += 1;
                }
                if (number <= (numberCount / (numberIndex * 1.f)) * 0.5f) {
                    AllSharesBean allSharesBean = new AllSharesBean();
                    allSharesBean.setCode(code);
                    allSharesBean.setName(name);
                    allSharesBean.setMode(mode);
                    allSharesBean.setRanking(ranking);
                    allSharesBean.setTrade(trade);
                    minNumberList.add(allSharesBean);
                }
            }
            if(currentWave <1 && currentWave > -1){
                if ((Math.abs(open - old_open)) / old_open < 0.06) {
                    if((height-low)/open < 0.01){
                        AllSharesBean allSharesBean = new AllSharesBean();
                        allSharesBean.setCode(code);
                        allSharesBean.setName(name);
                        allSharesBean.setMode(mode);
                        allSharesBean.setRanking(ranking);
                        allSharesBean.setTrade(trade);
                        xiaoboList.add(allSharesBean);
                    }
                }
            }
        }
        Collections.sort(cuiziList, new Comparator<AllSharesBean>() {
            @Override
            public int compare(AllSharesBean o1, AllSharesBean o2) {
                return o2.getRanking() - o1.getRanking();
            }
        });
        Collections.sort(zhongjiList, new Comparator<AllSharesBean>() {
            @Override
            public int compare(AllSharesBean o1, AllSharesBean o2) {
                return o2.getRanking() - o1.getRanking();
            }
        });
        Collections.sort(minNumberList, new Comparator<AllSharesBean>() {
            @Override
            public int compare(AllSharesBean o1, AllSharesBean o2) {
                return o2.getRanking() - o1.getRanking();
            }
        });
        Collections.sort(lineList, new Comparator<AllSharesBean>() {
            @Override
            public int compare(AllSharesBean o1, AllSharesBean o2) {
                return o2.getRanking() - o1.getRanking();
            }
        });
        Collections.sort(starList, new Comparator<AllSharesBean>() {
            @Override
            public int compare(AllSharesBean o1, AllSharesBean o2) {
                return o2.getRanking() - o1.getRanking();
            }
        });
    }
}
