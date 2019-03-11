package com.xjm.gupiao;

import android.os.Bundle;
import android.os.Handler;
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

public class ShiZiActivity extends AppCompatActivity {
    private ListView listView;
    private MaterialDialog progressDialog;
    private ArrayList<AllSharesBean> shiziList = new ArrayList<>();
    private TextView titleTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

        titleTextView = findViewById(R.id.title_textView);
        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShiZiActivity.this.finish();
            }
        });
        findViewById(R.id.refresh_imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new MaterialDialog.Builder(ShiZiActivity.this).title("分析进度")
                        .progress(false, 100, true)
                        .canceledOnTouchOutside(false)
                        .cancelable(false)
                        .show();
                new Thread() {
                    @Override
                    public void run() {
                        try {
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
        listView = findViewById(R.id.list_view);
        getHistoryData();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            SharesAdapter adapter = new SharesAdapter(shiziList, ShiZiActivity.this);
            listView.setAdapter(adapter);
            String titleStr = "十字星形态" + "(" + shiziList.size() + ")";
            titleTextView.setText(titleStr);
            FileOutputStream fos = null;
            ObjectOutputStream os = null;
            try {
                File file = new File(getApplication().getFilesDir().getAbsolutePath(), "/shizi.txt");
                if (!file.exists()) {
                    file.createNewFile();
                }
                fos = new FileOutputStream(file);
                os = new ObjectOutputStream(fos);
                os.writeObject(shiziList);
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
        }
    };

    private void getHistoryData() {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            File file = new File(getApplication().getFilesDir().getAbsolutePath(), "/shizi.txt");
            if (!file.exists()) {
                return;
            }
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            shiziList = (ArrayList<AllSharesBean>) ois.readObject();
            if (shiziList == null) {
                shiziList = new ArrayList<>();
                return;
            }
            SharesAdapter adapter = new SharesAdapter(shiziList, ShiZiActivity.this);
            listView.setAdapter(adapter);
            String titleStr = "十字星形态" + "(" + shiziList.size() + ")";
            titleTextView.setText(titleStr);
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

    private void refreshData() throws IOException, JSONException {
        //清空数据
        shiziList.clear();

        HttpClient httpClient1 = new DefaultHttpClient();
        HttpClient httpClient2 = new DefaultHttpClient();
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
            // 价格低于3块高于40的不考虑
            float currentPrice = Float.valueOf(sharesStr[3]);
            if (currentPrice < 3 || currentPrice > 80) {
                continue;
            }
            float currentWave = Float.valueOf(sharesStr[6]);
            if (currentWave < -3 || currentWave > 3) {
                continue;
            }

            EventBus.getDefault().post(new ProgressBean(arrayLength, i));

            //今日具体数据
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
            //名称   开盘价   前收盘价   当前价格   最高价   最低价    未知   未知   成交量   成交额
            String[] line_data = buffer1.toString().split(",");
            float open = Float.valueOf(line_data[1]);     // 开盘价
            float close = Float.valueOf(line_data[3]);    // 当前价
            float height = Float.valueOf(line_data[4]);   // 最高价
            float low = Float.valueOf(line_data[5]);      // 最低价
            float old_open = Float.valueOf(line_data[2]); // 前收盘

            float maxMinDiff = Math.abs(height - low);
            float openCloseDiff = Math.abs(open - close);

            //跳空
            if (Math.abs(open - old_open) / old_open >= 0.01) {
                continue;
            }
            //波动太大的不要
            if ((height - low) / open >= 0.02) {
                continue;
            }
            if ((openCloseDiff == 0 || maxMinDiff / openCloseDiff >= 4)) {
                AllSharesBean allSharesBean = new AllSharesBean();
                allSharesBean.setCode(sharesStr[1]);
                allSharesBean.setName(sharesStr[2]);
                allSharesBean.setNumber(Float.valueOf(line_data[9]));
                allSharesBean.setTrade(sharesStr[13]);
                shiziList.add(allSharesBean);

            }
        }
        Collections.sort(shiziList, new Comparator<AllSharesBean>() {
            @Override
            public int compare(AllSharesBean o1, AllSharesBean o2) {
                return (int) (o2.getNumber() - o1.getNumber());
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void busMessageEventBus(ProgressBean bean) {
        progressDialog.setMaxProgress(bean.getMax());
        progressDialog.setProgress(bean.getProgress());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
