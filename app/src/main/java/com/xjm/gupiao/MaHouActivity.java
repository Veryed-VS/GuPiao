package com.xjm.gupiao;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tifezh.kchartlib.chart.KChartView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class MaHouActivity extends AppCompatActivity {

    private MaterialDialog progressDialog;
    private KChartView chartView_1, chartView_2;
    private ArrayList<SharesBean> allSharesBeans = new ArrayList<>();
    private int index = 0;
    private ArrayList<KLineEntity> entity_1, entity_2;
    private TextView name_1, name_2,titleText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mahou);

        EventBus.getDefault().register(this);

        chartView_1 = findViewById(R.id.kchart_view_1);
        chartView_1.setRefreshListener(new KChartView.KChartRefreshListener() {
            @Override
            public void onLoadMoreBegin(KChartView chart) {
                chartView_1.refreshEnd();
            }
        });
        chartView_2 = findViewById(R.id.kchart_view_2);
        chartView_2.setRefreshListener(new KChartView.KChartRefreshListener() {
            @Override
            public void onLoadMoreBegin(KChartView chart) {
                chartView_2.refreshEnd();
            }
        });

        name_1 = findViewById(R.id.name_1);
        name_2 = findViewById(R.id.name_2);
        titleText = findViewById(R.id.title_textView);

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaHouActivity.this.finish();
            }
        });
        findViewById(R.id.next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index >= Math.abs(allSharesBeans.size() / 2.f)) {
                    return;
                }
                index += 1;
                if (progressDialog != null) {
                    progressDialog.show();
                }
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Looper.prepare();
                            SharesBean bean_1 = allSharesBeans.get(index * 2);
                            SharesBean bean_2 = allSharesBeans.get(index * 2 + 1);
                            entity_1 = getHistoryData(bean_1);
                            DataHelper.calculate(entity_1);
                            entity_2 = getHistoryData(bean_2);
                            DataHelper.calculate(entity_2);
                            allHandler.sendMessage(new Message());
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
        findViewById(R.id.previous_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index == 0) {
                    return;
                }
                index -= 1;
                if (progressDialog != null) {
                    progressDialog.show();
                }
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Looper.prepare();
                            SharesBean bean_1 = allSharesBeans.get(index * 2);
                            SharesBean bean_2 = allSharesBeans.get(index * 2 + 1);
                            entity_1 = getHistoryData(bean_1);
                            DataHelper.calculate(entity_1);
                            entity_2 = getHistoryData(bean_2);
                            DataHelper.calculate(entity_2);
                            allHandler.sendMessage(new Message());
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });

        progressDialog = new MaterialDialog.Builder(MaHouActivity.this).title("分析进度")
                .progress(false, 100, true)
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .show();

        new Thread() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    getAllSharesData();
                    SharesBean bean_1 = allSharesBeans.get(index * 2);
                    SharesBean bean_2 = allSharesBeans.get(index * 2 + 1);
                    entity_1 = getHistoryData(bean_1);
                    DataHelper.calculate(entity_1);
                    entity_2 = getHistoryData(bean_2);
                    DataHelper.calculate(entity_2);
                    allHandler.sendMessage(new Message());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void busMessageEventBus(ProgressBean bean) {
        progressDialog.setMaxProgress(bean.getMax());
        progressDialog.setProgress(bean.getProgress());
    }

    //全部股票获取完毕
    private Handler allHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            KChartAdapter mAdapter_1 = new KChartAdapter(entity_1);
            chartView_1.setAdapter(mAdapter_1);
            chartView_1.setGridRows(5);
            chartView_1.setGridColumns(5);
            SharesBean bean_1 = allSharesBeans.get(index * 2);
            name_1.setText(bean_1.name + "    " + bean_1.code);

            KChartAdapter mAdapter_2 = new KChartAdapter(entity_2);
            chartView_2.setAdapter(mAdapter_2);
            chartView_2.setGridRows(5);
            chartView_2.setGridColumns(5);
            SharesBean bean_2 = allSharesBeans.get(index * 2 + 1);
            name_2.setText(bean_2.name + "    " + bean_2.code);

            titleText.setText("马后炮捉妖"+"("+allSharesBeans.size()/2+")"+index);

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    };

    private void getAllSharesData() throws IOException, JSONException {
        HttpClient httpClient1 = new DefaultHttpClient();

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

        ArrayList<SharesBean> zhangArray = new ArrayList<>();
        ArrayList<SharesBean> starArray = new ArrayList<>();
        ArrayList<SharesBean> cuiziArray = new ArrayList<>();

        //模式,代码,名称,当前价格,主力流入净占比,今日主力排行,今日涨幅,5日主力流入净占比,5日主力排行,5日涨幅,10日主力流入净占比,10日主力排行,10日涨幅,行业
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
            // 价格低于3块高于40的不考虑
            float currentPrice = Float.valueOf(sharesStr[3]);
            if (currentPrice < 3 || currentPrice > 40) {
                continue;
            }
            float currentWave = Float.valueOf(sharesStr[6]);
            if (currentWave <= -3) {
                continue;
            }

            EventBus.getDefault().post(new ProgressBean(arrayLength, i));

            int mode = Integer.valueOf(sharesStr[0].replace("\"", "")) - 1;
            SharesBean bean = new SharesBean(sharesStr[1], sharesStr[2], mode);

            HttpClient httpClient2 = new DefaultHttpClient();
            //名称   开盘价   前收盘价   当前价格   最高价   最低价    未知   未知   成交量   成交额
            HttpGet httpGet1 = new HttpGet(DataTools.getSHARES_PAN_URL(sharesStr[1]));
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
            float open = Float.valueOf(line_data[1]);
            float close = Float.valueOf(line_data[3]);
            float height = Float.valueOf(line_data[4]);
            float low = Float.valueOf(line_data[5]);
            long number = Long.parseLong(line_data[8]);
            String dataStr = line_data[30];

            bean.close =close;
            bean.dataStr = dataStr;
            bean.height=height;
            bean.open = open;
            bean.low = low;
            bean.number = number;

            if(currentWave > 5){
                zhangArray.add(bean);
            }else if(height == close && low == open){
                //光头光脚
                cuiziArray.add(bean);
            }else if (open == low) {
                //光脚倒锤子
                cuiziArray.add(bean);
            } else if (((height - close) >= (close - open)) && ((height - close) > 2 * (open - low))) {
                //倒锤子
                cuiziArray.add(bean);
            }else{
                float maxMinDiff = Math.abs(height - low);
                float openCloseDiff = Math.abs(open - close);
                if((openCloseDiff == 0 && maxMinDiff/open < 0.01) || maxMinDiff / openCloseDiff >= 4){
                    starArray.add(bean);
                }
            }
        }
        allSharesBeans.addAll(zhangArray);
        allSharesBeans.addAll(cuiziArray);
        allSharesBeans.addAll(starArray);
    }

    private ArrayList<KLineEntity> getHistoryData(SharesBean bean) throws IOException, ParseException {
        HttpClient httpClient3 = new DefaultHttpClient();
        HttpGet httpGet2 = new HttpGet(DataTools.getSHARES_OLD_URL(bean.mode, bean.code, 120));
        HttpResponse httpResponse2 = httpClient3.execute(httpGet2);
        HttpEntity httpEntity2 = httpResponse2.getEntity();
        if (httpEntity2 == null) {
            return new ArrayList<>();
        }
        InputStream is2 = httpEntity2.getContent();
        BufferedReader br2 = new BufferedReader(new InputStreamReader(is2, "utf-8"));
        br2.readLine(); // 第一行信息，为标题信息
        String lineStr;

        ArrayList<KLineEntity> entities = new ArrayList<>();

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
            //日期 股票代码 名称 收盘价 最高价 最低价 开盘价 前收盘 涨跌幅 成交量
            String old_date = line_item[0];
            float old_open = Float.valueOf(line_item[6]);     // 开盘价
            float old_close = Float.valueOf(line_item[3]);    // 收盘价
            float old_height = Float.valueOf(line_item[4]);   // 最高价
            float old_low = Float.valueOf(line_item[5]);      // 最低价
            long old_number = Long.parseLong(line_item[9]);  // 成交量

            KLineEntity entity = new KLineEntity();
            entity.High = old_height;
            entity.Close = old_close;
            entity.Low = old_low;
            entity.Open = old_open;
            entity.Date = old_date;
            entity.Volume = old_number;
            entities.add(entity);
        }
        //翻转数据
        Collections.reverse(entities);
        if (!bean.dataStr.equals(entities.get(entities.size()-1).Date)) {
            KLineEntity entity = new KLineEntity();
            entity.High = bean.height;
            entity.Close = bean.close;
            entity.Low = bean.low;
            entity.Open = bean.open;
            entity.Date = bean.dataStr;
            entity.Volume = bean.number;
            entities.add(entity);
        }
        //加入模拟数据
        for (int i = 0; i < 3; i++) {
            KLineEntity entity = new KLineEntity();
            KLineEntity old_entity = entities.get(entities.size() - 1);
            entity.Volume = Math.abs(old_entity.Volume*1.5f);
            entity.Open = old_entity.Close;
            entity.Close = old_entity.Close * 1.1f;
            entity.Low = old_entity.Close;
            entity.High = old_entity.Close * 1.1f;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date old_Date = sdf.parse(old_entity.Date);
            Calendar nowCalendar = Calendar.getInstance();
            nowCalendar.setTime(old_Date);
            nowCalendar.set(Calendar.DATE, nowCalendar.get(Calendar.DATE) + 1);
            String result = sdf.format(nowCalendar.getTime());

            entity.Date = result;
            entities.add(entity);
        }
        return entities;
    }

    private class SharesBean {
        private String code;   //代码
        private String name;   //名称

        private float open;    //开盘价
        private float close;   //收盘价
        private float height;  //最高价
        private float low;     //最低价
        private long number;   //成交量
        private String dataStr;//当前日期

        private int mode;

        public SharesBean(String code, String name, int mode) {
            this.code = code;
            this.name = name;
            this.mode = mode;
        }
    }
}
