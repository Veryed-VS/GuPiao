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
    private HttpClient httpClient = new DefaultHttpClient();

    public ArrayList<ResultBean> runAnalysisData() throws IOException, JSONException {
        HttpGet httpGet = new HttpGet(DataTools.ALL_CODE_URL);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();
        //全部的十字星
        ArrayList<AllSharesBean> allSharesBeans = new ArrayList<>();
        //倒锤子形态
        ArrayList<ResultBean> sinkerBeans = new ArrayList<>();
        //大涨后得十字星
        ArrayList<ResultBean> starsBeans = new ArrayList<>();
        //三线合一
        ArrayList<ResultBean> lineBeans = new ArrayList<>();

        ArrayList<ResultBean> resultBeans = new ArrayList<>();
        if (httpEntity == null) {
            return resultBeans;

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
            if (currentPrice < 3 || currentPrice > 30) {
                continue;
            }
            float currentWave = Float.valueOf(sharesStr[6]);
            if (currentWave >= -2 && currentWave <= 2) {
                int mode = Integer.valueOf(sharesStr[0].replace("\"", "")) - 1;
                String code = sharesStr[1];
                String name = sharesStr[2];
                String trade = sharesStr[13];
                AllSharesBean allSharesBean = new AllSharesBean(code, name, mode, currentWave, trade);
                allSharesBeans.add(allSharesBean);
            } else if (currentWave >= 5) {
                int mode = Integer.valueOf(sharesStr[0].replace("\"", "")) - 1;
                String code = sharesStr[1];
                HttpGet httpGet1 = new HttpGet(DataTools.getSHARES_OLD_URL(mode, code));
                HttpResponse httpResponse1 = httpClient.execute(httpGet1);
                HttpEntity httpEntity1 = httpResponse1.getEntity();
                if (httpEntity1 == null) {
                    continue;
                }
                ArrayList<SharesBean> sharesBeans = new ArrayList<>();
                InputStream is1 = httpEntity1.getContent();
                BufferedReader br1 = new BufferedReader(new InputStreamReader(is1, "utf-8"));
                br1.readLine(); // 第一行信息，为标题信息
                //日期 股票代码 名称 收盘价 最高价 最低价 开盘价 前收盘 涨跌额 涨跌幅 成交量 换手率
                String lineStr;
                while ((lineStr = br1.readLine()) != null) {
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
                    SharesBean bean = new SharesBean();
                    bean.setOpenPrice(Float.valueOf(line_item[6]));     // 开盘价
                    bean.setClosePrice(Float.valueOf(line_item[3]));    // 收盘价
                    bean.setNumber(Long.valueOf(line_item[10]));        // 成交量
                    bean.setTurnover(Float.valueOf(line_item[11]));     // 换手率
                    bean.setHeightPrice(Float.valueOf(line_item[4]));   // 最高价
                    bean.setLowPrice(Float.valueOf(line_item[5]));      // 最低价
                    bean.setWave(Float.valueOf(9));                     // 涨跌幅
                    sharesBeans.add(bean);
                }
                if (sharesBeans.size() == 0) {
                    continue;
                }
                //三线合一
                float curr_price = Float.valueOf(sharesStr[3]);
                EventBus.getDefault().post(new ProgressBean(arrayLength, i));
                if (sharesBeans.size() >= 20) {
                    float priceCount = curr_price;
                    for (int a = 0; a < 4; a++) {
                        priceCount += sharesBeans.get(a).getClosePrice();
                    }
                    float fivePrice = priceCount / 5.0f;
                    priceCount = curr_price;
                    for (int a = 0; a < 9; a++) {
                        priceCount += sharesBeans.get(a).getClosePrice();
                    }
                    float tenPrice = priceCount / 10.0f;
                    priceCount = curr_price;
                    for (int a = 0; a < 19; a++) {
                        priceCount += sharesBeans.get(a).getClosePrice();
                    }
                    float twentyPrice = priceCount / 20.f;
                    if ((Math.max(Math.max(fivePrice, tenPrice), Math.max(fivePrice, twentyPrice)) -
                            Math.min(Math.min(fivePrice, tenPrice), Math.min(fivePrice, twentyPrice))) /
                            curr_price <= 0.01) {
                        if (curr_price > Math.max(Math.max(fivePrice, tenPrice), Math.max(fivePrice, twentyPrice))) {
                            ResultBean resultBean = new ResultBean(mode,
                                    code, sharesStr[2],
                                    sharesStr[13], 0);
                            lineBeans.add(resultBean);
                            continue;
                        }
                    }
                }
            }
        }

        int progress = 0;
        for (AllSharesBean sharesBean : allSharesBeans) {
            progress++;
            EventBus.getDefault().post(new ProgressBean(allSharesBeans.size(), progress));

            HttpGet httpGet1 = new HttpGet(DataTools.getSHARES_PAN_URL(sharesBean.getCode()));
            HttpResponse httpResponse1 = httpClient.execute(httpGet1);
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
            float open = Float.valueOf(line_data[1]);
            float close = Float.valueOf(line_data[3]);
            float height = Float.valueOf(line_data[4]);
            float low = Float.valueOf(line_data[5]); //低
            double number = Double.valueOf(line_data[9]); //额

            float maxMinDiff = Math.abs(height - low);
            float openCloseDiff = Math.abs(open - close);

            if (open >= close) {  //涨
                if (((height - close) >= 1.5 * (close - open)) && ((height - close) >= 1.5 * (open - low))) {
                    ResultBean resultBean = new ResultBean(sharesBean.getMode(),
                            sharesBean.getCode(), sharesBean.getName(),
                            sharesBean.getTrade(), number);
                    sinkerBeans.add(resultBean);
                    continue;
                }
            } else {  //跌
                if (((height - close) >= 1.5 * (open - close)) && ((height - open) >= 1.5 * (close - low))) {
                    ResultBean resultBean = new ResultBean(sharesBean.getMode(),
                            sharesBean.getCode(), sharesBean.getName(),
                            sharesBean.getTrade(), number);
                    sinkerBeans.add(resultBean);
                    continue;
                }
            }
            if (openCloseDiff == 0 || maxMinDiff / openCloseDiff >= 4) {
                ResultBean resultBean = new ResultBean(sharesBean.getMode(),
                        sharesBean.getCode(), sharesBean.getName(),
                        sharesBean.getTrade(), number);
                resultBeans.add(resultBean);
            }

        }
        progress = 0;
        for (ResultBean resultBean : resultBeans) {
            progress++;
            EventBus.getDefault().post(new ProgressBean(resultBeans.size(), progress));

            HttpGet httpGet1 = new HttpGet(DataTools
                    .getSHARES_OLD_URL(resultBean.getMode(), resultBean.getCode()));
            HttpResponse httpResponse1 = httpClient.execute(httpGet1);
            HttpEntity httpEntity1 = httpResponse1.getEntity();
            if (httpEntity1 == null) {
                continue;
            }
            ArrayList<SharesBean> sharesBeans = new ArrayList<>();
            InputStream is1 = httpEntity1.getContent();
            BufferedReader br1 = new BufferedReader(new InputStreamReader(is1, "utf-8"));
            br1.readLine(); // 第一行信息，为标题信息
            //日期 股票代码 名称 收盘价 最高价 最低价 开盘价 前收盘 涨跌额 涨跌幅 成交量 换手率
            String lineStr;
            while ((lineStr = br1.readLine()) != null) {
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
                SharesBean bean = new SharesBean();
                bean.setOpenPrice(Float.valueOf(line_item[6]));     // 开盘价
                bean.setClosePrice(Float.valueOf(line_item[3]));    // 收盘价
                bean.setNumber(Long.valueOf(line_item[10]));        // 成交量
                bean.setTurnover(Float.valueOf(line_item[11]));     // 换手率
                bean.setHeightPrice(Float.valueOf(line_item[4]));   // 最高价
                bean.setLowPrice(Float.valueOf(line_item[5]));      // 最低价
                bean.setWave(Float.valueOf(9));                     // 涨跌幅
                sharesBeans.add(bean);
            }
            if (sharesBeans.size() == 0) {
                continue;
            }
            SharesBean firstBean = sharesBeans.get(0);
            if (firstBean.getWave() > 3) {
                //大涨后得十字星
                starsBeans.add(resultBean);
            } else {
                //五日量最低
                boolean isNumberMin = true;
                for (int i = 0; (i < sharesBeans.size() && i < 4); i++) {
                    if (sharesBeans.get(i).getNumber() < firstBean.getNumber()) {
                        isNumberMin = false;
                        break;
                    }
                }
                if (isNumberMin) {
                    starsBeans.add(resultBean);
                    continue;
                }
                //十天量能一半
                long numberCount = 0;
                int numberIndex = 0;
                for (int i = 0; (i < sharesBeans.size() && i < 9); i++) {
                    numberCount += sharesBeans.get(i).getNumber();
                    numberIndex = i;
                }
                if (firstBean.getNumber() <= (numberCount / (numberIndex * 1.f)) * 0.5f) {
                    starsBeans.add(resultBean);
                    continue;
                }
                //五日换手率最低
                boolean isTurnover = true;
                for (int i = 0; (i < sharesBeans.size() && i < 4); i++) {
                    if (sharesBeans.get(i).getTurnover() < firstBean.getTurnover()) {
                        isTurnover = false;
                        break;
                    }
                }
                if (isTurnover) {
                    starsBeans.add(resultBean);
                    continue;
                }
                //十天换手率一半
                if (sharesBeans.size() >= 10) {
                    float turnoverCount = 0;
                    int turnIndex = 0;
                    for (int i = 0; i < 9; i++) {
                        turnoverCount += sharesBeans.get(i).getTurnover();
                        turnIndex = i;
                    }
                    if (firstBean.getTurnover() <= (turnoverCount / (turnIndex * 1.f)) * 0.5f) {
                        starsBeans.add(resultBean);
                        continue;
                    }
                }
            }
        }

        Collections.sort(starsBeans, new Comparator<ResultBean>() {
            @Override
            public int compare(ResultBean o1, ResultBean o2) {
                return (int) (o2.getNumber() - o1.getNumber());
            }
        });
        Collections.sort(sinkerBeans, new Comparator<ResultBean>() {
            @Override
            public int compare(ResultBean o1, ResultBean o2) {
                return (int) (o2.getNumber() - o1.getNumber());
            }
        });
        lineBeans.addAll(sinkerBeans);
        lineBeans.addAll(starsBeans);
        resultBeans = lineBeans;
        return resultBeans;
    }
}
