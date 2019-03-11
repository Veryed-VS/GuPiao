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

public class LineActivity extends AppCompatActivity {
    private ListView listView;
    private MaterialDialog progressDialog;
    private ArrayList<AllSharesBean> lineList = new ArrayList<>();
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
                LineActivity.this.finish();
            }
        });
        findViewById(R.id.refresh_imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new MaterialDialog.Builder(LineActivity.this).title("分析进度")
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
            SharesAdapter adapter = new SharesAdapter(lineList, LineActivity.this);
            listView.setAdapter(adapter);
            String titleStr = "三线合一" + "(" + lineList.size() + ")";
            titleTextView.setText(titleStr);
            FileOutputStream fos = null;
            ObjectOutputStream os = null;
            try {
                File file = new File(getApplication().getFilesDir().getAbsolutePath(), "/line.txt");
                if (!file.exists()) {
                    file.createNewFile();
                }
                fos = new FileOutputStream(file);
                os = new ObjectOutputStream(fos);
                os.writeObject(lineList);
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
            File file = new File(getApplication().getFilesDir().getAbsolutePath(), "/line.txt");
            if (!file.exists()) {
                return;
            }
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            lineList = (ArrayList<AllSharesBean>) ois.readObject();
            if (lineList == null) {
                lineList = new ArrayList<>();
                return;
            }
            SharesAdapter adapter = new SharesAdapter(lineList, LineActivity.this);
            listView.setAdapter(adapter);
            String titleStr = "三线合一" + "(" + lineList.size() + ")";
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
        lineList.clear();

        HttpClient httpClient1 = new DefaultHttpClient();
        HttpClient httpClient2 = new DefaultHttpClient();
        HttpClient httpClient3 = new DefaultHttpClient();

        HttpGet httpGet = new HttpGet(DataTools.ALL_CODE_URL);
        HttpResponse httpResponse = httpClient1.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();

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
            if (currentWave >= 0) {

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
                String[] line_data = buffer1.toString().split(",");
                float open = Float.valueOf(line_data[1]);     // 开盘价

                int mode = Integer.valueOf(sharesStr[0].replace("\"", "")) - 1;
                String code = sharesStr[1];
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
                    if(Float.valueOf(line_item[3]) == currentPrice && Float.valueOf(6) == open){
                        continue;
                    }
                    SharesBean bean = new SharesBean();
                    bean.setOpenPrice(Float.valueOf(line_item[6]));     // 开盘价
                    bean.setClosePrice(Float.valueOf(line_item[3]));    // 收盘价
                    bean.setNumber(Long.valueOf(line_item[10]));        // 成交量
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
                            AllSharesBean allSharesBean = new AllSharesBean();
                            allSharesBean.setCode(sharesStr[1]);
                            allSharesBean.setName(sharesStr[2]);
                            allSharesBean.setNumber(Float.valueOf(line_data[9]));
                            allSharesBean.setTrade(sharesStr[13]);
                            lineList.add(allSharesBean);
                            continue;
                        }
                    }
                }
            }
        }

        Collections.sort(lineList, new Comparator<AllSharesBean>() {
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
