package com.xjm.gupiao;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<ResultBean> sharesBeans = new ArrayList<>();
    private MaterialDialog progressDialog;
    private SharesAdapter adapter;
    private ListView listView;
    private SwipeRefreshLayout refreshLayout;
    private TextView titleTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

        listView = findViewById(R.id.list_view);
        titleTxt = findViewById(R.id.title_layout);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        } else {
            getHistoryData();
        }

        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                progressDialog = new MaterialDialog.Builder(MainActivity.this).title("分析进度")
                        .progress(false, 100, true)
                        .canceledOnTouchOutside(false)
                        .cancelable(false)
                        .show();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            sharesBeans = new AnalysisData().runAnalysisData();
                            handler.sendMessage(new Message());
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                refreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            getHistoryData();
        }
    }

    private void getHistoryData() {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            File txtFilePath = new File(getApplicationContext().
                    getFilesDir().getAbsolutePath() + "/data");
            if (!txtFilePath.exists()) {
                txtFilePath.mkdirs();
            }
            File txtFile = new File(getApplicationContext().
                    getFilesDir().getAbsolutePath() + "/data/history.txt");
            if (!txtFile.exists()) {
                txtFile.createNewFile();
            } else {
                fis = new FileInputStream(txtFile);
                ois = new ObjectInputStream(fis);
                sharesBeans = (ArrayList<ResultBean>) ois.readObject();
                adapter = new SharesAdapter(sharesBeans, MainActivity.this);
                listView.setAdapter(adapter);
                titleTxt.setText(getString(R.string.app_name)+"("+sharesBeans.size()+")");
            }
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
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            adapter = new SharesAdapter(sharesBeans, MainActivity.this);
            listView.setAdapter(adapter);
            titleTxt.setText(getString(R.string.app_name)+"("+sharesBeans.size()+")");
            File txtFilePath = new File(getApplicationContext().
                    getFilesDir().getAbsolutePath() + "/data");
            if (!txtFilePath.exists()) {
                txtFilePath.mkdirs();
            }
            File txtFile = new File(getApplicationContext().
                    getFilesDir().getAbsolutePath() + "/data/history.txt");
            if (!txtFile.exists()) {
                try {
                    txtFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileOutputStream fos = null;
            ObjectOutputStream os = null;
            try {
                fos = new FileOutputStream(txtFile);
                os = new ObjectOutputStream(fos);
                os.writeObject(sharesBeans);
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