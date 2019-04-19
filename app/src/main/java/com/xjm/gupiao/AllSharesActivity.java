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

public class AllSharesActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayList<AllSharesBean> resultList = new ArrayList<>();
    private MaterialDialog progressDialog;
    private SharesAdapter adapter;
    private TextView titleTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_shares);
        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllSharesActivity.this.finish();
            }
        });
        findViewById(R.id.refresh_imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new MaterialDialog.Builder(AllSharesActivity.this)
                        .content("股票全览...")
                        .progress(true, 0)
                        .canceledOnTouchOutside(false)
                        .cancelable(false)
                        .show();
                if(adapter != null){
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

    private void getHistoryData() {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            File file = new File(getApplication().getFilesDir().getAbsolutePath(), "/allList.txt");
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
            adapter = new SharesAdapter(resultList, AllSharesActivity.this);
            listView.setAdapter(adapter);
            titleTextView.setText("股票全览"+"("+resultList.size()+")");
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
            adapter = new SharesAdapter(resultList, AllSharesActivity.this);
            listView.setAdapter(adapter);
            titleTextView.setText(titleTextView.getText().toString()+"("+resultList.size()+")");
            FileOutputStream fos = null;
            ObjectOutputStream os = null;
            try {
                File file = new File(getApplication().getFilesDir().getAbsolutePath(), "/allList.txt");
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
            if (currentPrice < 3 || currentPrice > 80) {
                continue;
            }
            float currentWave = Float.valueOf(sharesStr[6]);
            if (currentWave < -3) {
                continue;
            }
            AllSharesBean allSharesBean = new AllSharesBean();
            allSharesBean.setCode(sharesStr[1]);
            allSharesBean.setName(sharesStr[2]);
            allSharesBean.setWave(currentWave);
            resultList.add(allSharesBean);
        }
        Collections.sort(resultList, new Comparator<AllSharesBean>() {
            @Override
            public int compare(AllSharesBean o1, AllSharesBean o2) {
                return (int) (o2.getWave() * 100) - (int) (o1.getWave() * 100);
            }
        });
    }
}
