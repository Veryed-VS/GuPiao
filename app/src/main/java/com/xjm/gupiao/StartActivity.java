package com.xjm.gupiao;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
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

public class StartActivity extends AppCompatActivity {

    private ArrayList<AllSharesBean> resultList = new ArrayList<>();
    private ListView listView;
    private MaterialDialog progressDialog;
    private TextView titleTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        EventBus.getDefault().register(this);
        titleTextView = findViewById(R.id.title_textView);

        if (ContextCompat.checkSelfPermission(StartActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(StartActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
        findViewById(R.id.refresh_imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new MaterialDialog.Builder(StartActivity.this).title("分析进度")
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
            SharesAdapter adapter = new SharesAdapter(resultList, StartActivity.this);
            listView.setAdapter(adapter);
            String titleStr = "明哥选股" + "(" + resultList.size() + ")";
            titleTextView.setText(titleStr);
            FileOutputStream fos = null;
            ObjectOutputStream os = null;
            try {
                File file = new File(getApplication().getFilesDir().getAbsolutePath(), "/resultList.txt");
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
        }
    };

    private void getHistoryData() {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            File file = new File(getApplication().getFilesDir().getAbsolutePath(), "/resultList.txt");
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
            SharesAdapter adapter = new SharesAdapter(resultList, StartActivity.this);
            listView.setAdapter(adapter);
            String titleStr = "明哥选股" + "(" + resultList.size() + ")";
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED
                || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
            new MaterialDialog.Builder(StartActivity.this).title("傻逼")
                    .content("不给权限?那就去死吧!")
                    .positiveText("好,我去死")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            StartActivity.this.finish();
                        }
                    }).build().show();
        }
    }
    private void refreshData() throws IOException, JSONException {
        resultList.clear();

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
            if (currentWave < -2 || currentWave > 4) {
                continue;
            }
            EventBus.getDefault().post(new ProgressBean(arrayLength, i));

            String code = sharesStr[1];       //代码
            int mode = Integer.valueOf(sharesStr[0].replace("\"", "")) - 1; //模式
            HttpGet httpGet2 = new HttpGet(DataTools.getSHARES_OLD_URL(mode, code));
            HttpResponse httpResponse2 = httpClient3.execute(httpGet2);
            HttpEntity httpEntity2 = httpResponse2.getEntity();
            if (httpEntity2 == null) {
                continue;
            }
            InputStream is2 = httpEntity2.getContent();
            BufferedReader br2 = new BufferedReader(new InputStreamReader(is2, "utf-8"));
            br2.readLine(); // 第一行信息，为标题信息
//            br2.readLine(); // 必须盘后运行 不要第一行数据
            //日期 股票代码 名称 收盘价 最高价 最低价 开盘价 前收盘 涨跌额 涨跌幅 成交量 换手率
            String lineStr;
            ArrayList<SharesBean> sharesBeans = new ArrayList<>();
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
                SharesBean bean = new SharesBean();
                bean.setOpenPrice(Float.valueOf(line_item[6]));     // 开盘价
                bean.setClosePrice(Float.valueOf(line_item[3]));    // 收盘价
                bean.setNumber(Long.valueOf(line_item[10]));        // 成交量
                bean.setWave(Float.valueOf(9));                     // 涨跌幅
                sharesBeans.add(bean);
            }

            if (sharesBeans.size() < 15) {
                continue;
            }
            int waveNumber = 0;
            for (int a = 0;a < 10;a++){
                if(sharesBeans.get(a).getWave()>= 8){
                    waveNumber +=1;
                }
            }
            if(waveNumber < 3){
                continue;
            }

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
            long number =Long.valueOf(line_data[8]);      // 成交量

            boolean isMinNumber = true;
            for (int a =0; a<5;a++){
                if(number > sharesBeans.get(a).getNumber()){
                    isMinNumber = false;
                    break;
                }
            }
            if(isMinNumber){
                AllSharesBean allSharesBean = new AllSharesBean();
                allSharesBean.setCode(sharesStr[1]);
                allSharesBean.setName(sharesStr[2]);
                allSharesBean.setNumber(Float.valueOf(line_data[9]));
                allSharesBean.setTrade(sharesStr[13]);
                resultList.add(allSharesBean);
                continue;
            }

            if (open <= close) {  //涨
                if (open == low) {   //光脚阳线
                    if ((height - close) >= ((close - open) * 0.5)) {
                        AllSharesBean allSharesBean = new AllSharesBean();
                        allSharesBean.setCode(sharesStr[1]);
                        allSharesBean.setName(sharesStr[2]);
                        allSharesBean.setNumber(Float.valueOf(line_data[9]));
                        allSharesBean.setTrade(sharesStr[13]);
                        resultList.add(allSharesBean);
                        continue;
                    }
                } else {
                    if (((height - close) >= 1.2 * (close - open)) && ((height - close) >= 1.2 * (open - low))) {
                        //模式,代码,名称,当前价格,主力流入净占比,今日主力排行,今日涨幅,5日主力流入净占比,5日主力排行,5日涨幅,10日主力流入净占比,10日主力排行,10日涨幅,行业
                        AllSharesBean allSharesBean = new AllSharesBean();
                        allSharesBean.setCode(sharesStr[1]);
                        allSharesBean.setName(sharesStr[2]);
                        allSharesBean.setNumber(Float.valueOf(line_data[9]));
                        allSharesBean.setTrade(sharesStr[13]);
                        resultList.add(allSharesBean);
                        continue;
                    }
                }
            } else {  //跌
                if (((height - open) >=  (open - close)) && ((height - open) >= (close - low))) {
                    AllSharesBean allSharesBean = new AllSharesBean();
                    allSharesBean.setCode(sharesStr[1]);
                    allSharesBean.setName(sharesStr[2]);
                    allSharesBean.setNumber(Float.valueOf(line_data[9]));
                    allSharesBean.setTrade(sharesStr[13]);
                    resultList.add(allSharesBean);
                    continue;
                }
            }

            float maxMinDiff = Math.abs(height - low);
            float openCloseDiff = Math.abs(open - close);
            if ((openCloseDiff == 0 || maxMinDiff / openCloseDiff >= 4)) {
                AllSharesBean allSharesBean = new AllSharesBean();
                allSharesBean.setCode(sharesStr[1]);
                allSharesBean.setName(sharesStr[2]);
                allSharesBean.setNumber(Float.valueOf(line_data[9]));
                allSharesBean.setTrade(sharesStr[13]);
                resultList.add(allSharesBean);
                continue;
            }
        }
        Collections.sort(resultList, new Comparator<AllSharesBean>() {
            @Override
            public int compare(AllSharesBean o1, AllSharesBean o2) {
                return (int) (o2.getNumber() - o1.getNumber());
            }
        });
    }
}
