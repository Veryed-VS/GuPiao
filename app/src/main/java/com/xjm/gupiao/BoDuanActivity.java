package com.xjm.gupiao;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
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
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//十字星
public class BoDuanActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayList<AllSharesBean> resultList = new ArrayList<>();
    private MaterialDialog progressDialog;
    private SharesAdapter adapter;
    private TextView titleTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boduan);
        EventBus.getDefault().register(this);
        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BoDuanActivity.this.finish();
            }
        });
        findViewById(R.id.refresh_imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new MaterialDialog.Builder(BoDuanActivity.this).title("分析进度")
                        .progress(false, 100, true)
                        .canceledOnTouchOutside(false)
                        .cancelable(false)
                        .show();
                if (adapter != null) {
                    resultList.clear();
                    adapter.notifyDataSetChanged();
                }
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Looper.prepare();
                            refreshData();
                            handler.sendMessage(new Message());
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
        titleTextView = findViewById(R.id.title_textView);
        listView = findViewById(R.id.list_view);
        getHistoryData();
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

    private void getHistoryData() {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            File file = new File(getApplication().getFilesDir().getAbsolutePath(), "/boduan.txt");
            if (!file.exists()) {
                return;
            }
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            resultList = (ArrayList<AllSharesBean>) ois.readObject();
            if (resultList == null) {
                resultList = new ArrayList<>();
                return;
            }
            adapter = new SharesAdapter(resultList, BoDuanActivity.this);
            listView.setAdapter(adapter);
            titleTextView.setText("波段助手" + "(" + resultList.size() + ")");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            adapter = new SharesAdapter(resultList, BoDuanActivity.this);
            listView.setAdapter(adapter);
            titleTextView.setText("波段助手" + "(" + resultList.size() + ")");
            FileOutputStream fos = null;
            ObjectOutputStream os = null;
            try {
                File file = new File(getApplication().getFilesDir().getAbsolutePath(), "/boduan.txt");
                if (!file.exists()) {
                    file.createNewFile();
                }
                fos = new FileOutputStream(file);
                os = new ObjectOutputStream(fos);
                os.writeObject(resultList);
                os.close();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    };

    private void refreshData() throws IOException, JSONException {
        HttpClient httpClient1 = new DefaultHttpClient();
        HttpClient httpClient2 = new DefaultHttpClient();
        HttpClient httpClient3 = new DefaultHttpClient();

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
            if (currentWave <= -2 || currentPrice >= 6) {
                continue;
            }

            EventBus.getDefault().post(new ProgressBean(arrayLength, i));

            String code = sharesStr[1];
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
            float open = Float.valueOf(line_data[1]);
            float old = Float.valueOf(line_data[2]);
            float close = Float.valueOf(line_data[3]);
            float height = Float.valueOf(line_data[4]);
            float low = Float.valueOf(line_data[5]);

            if (close > open) {
                if (open == low) {
                    if ((height - close) >= (close - open)) {
                        AllSharesBean allSharesBean = new AllSharesBean();
                        allSharesBean.setCode(sharesStr[1]);
                        allSharesBean.setName(sharesStr[2]);
                        allSharesBean.setRank(Integer.valueOf(sharesStr[11]));
                        resultList.add(allSharesBean);
                        continue;
                    }
                } else if (((height - close) > (close - open)) && ((height - close) > 3 * (open - low))) {
                    AllSharesBean allSharesBean = new AllSharesBean();
                    allSharesBean.setCode(sharesStr[1]);
                    allSharesBean.setName(sharesStr[2]);
                    allSharesBean.setRank(Integer.valueOf(sharesStr[11]));
                    resultList.add(allSharesBean);
                    continue;
                }
            }
            if (close > open) {
                //必须要实体 波动大于2
                if ((close - open) / old >= 0.02) {
                    int mode = Integer.valueOf(sharesStr[0].replace("\"", "")) - 1;
                    HttpGet httpGet2 = new HttpGet(DataTools.getSHARES_OLD_URL(mode, code, 15));
                    HttpResponse httpResponse2 = httpClient3.execute(httpGet2);
                    HttpEntity httpEntity2 = httpResponse2.getEntity();
                    if (httpEntity2 == null) {
                        continue;
                    }
                    InputStream is2 = httpEntity2.getContent();
                    BufferedReader br2 = new BufferedReader(new InputStreamReader(is2, "utf-8"));
                    br2.readLine(); // 第一行信息，为标题信息
                    String lineStr;
                    ArrayList<SharesBean> sharesBeans = new ArrayList<>();
                    for (int a = 0; (lineStr = br2.readLine()) != null && a < 6; a++) {
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
                        float old_open = Float.valueOf(line_item[6]);     // 开盘价
                        float old_close = Float.valueOf(line_item[3]);    // 收盘价
                        float old_height = Float.valueOf(line_item[4]);   // 最高价
                        float old_low = Float.valueOf(line_item[5]);      // 最低价
                        long old_number = Long.valueOf(line_item[9]);     // 成交量
                        float old_old_close = Float.valueOf(line_item[7]);

                        SharesBean sharesBean = new SharesBean(old_open, old_close, old_height, old_low,old_old_close, old_number);
                        sharesBeans.add(sharesBean);
                    }
                    SharesBean old_sharesBean;
                    if(sharesBeans.get(0).getOpen() == open && sharesBeans.get(0).getClose() == close && sharesBeans.get(0).getOld() == old){
                        old_sharesBean = sharesBeans.get(1);
                    }else{
                        old_sharesBean = sharesBeans.get(0);
                    }
                    float maxMinDiff = Math.abs(old_sharesBean.getHeight() - old_sharesBean.getLow());
                    float openCloseDiff = Math.abs(old_sharesBean.getOpen() - old_sharesBean.getClose());
                    if(openCloseDiff == 0 || maxMinDiff / openCloseDiff >= 4){
                        AllSharesBean allSharesBean = new AllSharesBean();
                        allSharesBean.setCode(sharesStr[1]);
                        allSharesBean.setName(sharesStr[2]);
                        allSharesBean.setRank(Integer.valueOf(sharesStr[11]));
                        resultList.add(allSharesBean);
                    }
                }
            }
        }
        //按照量能
        Collections.sort(resultList, new Comparator<AllSharesBean>() {
            @Override
            public int compare(AllSharesBean o1, AllSharesBean o2) {
                return o1.getRank() - o2.getRank();
            }
        });
    }

    private class SharesBean {
        private float open;    //开盘价
        private float close;   //收盘价
        private float height;  //最高价
        private float low;     //最低价
        private long number;   //成交量
        private float old;     //前收盘

        public SharesBean(float open, float close, float height, float low, float old, long number) {
            this.open = open;
            this.close = close;
            this.height = height;
            this.low = low;
            this.number = number;
            this.old = old;
        }

        public float getOld() {
            return old;
        }

        public float getOpen() {
            return open;
        }

        public float getClose() {
            return close;
        }

        public float getHeight() {
            return height;
        }

        public float getLow() {
            return low;
        }

        public long getNumber() {
            return number;
        }
    }
}
