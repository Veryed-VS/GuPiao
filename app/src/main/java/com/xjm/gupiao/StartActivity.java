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
import android.widget.Button;

import com.afollestad.materialdialogs.DialogAction;
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

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    private MaterialDialog progressDialog;
    private AnalysisData analysisData = new AnalysisData();
    private Button oneButton;
    private Button twoButton;
    private Button fourButton;
    private Button fiveButton;
    private Button sixButton;
    private Button threeButton;
    private Button sevenButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        EventBus.getDefault().register(this);

        oneButton = findViewById(R.id.button_one);
        oneButton.setOnClickListener(this);
        twoButton = findViewById(R.id.button_two);
        twoButton.setOnClickListener(this);
        fourButton = findViewById(R.id.button_four);
        fourButton.setOnClickListener(this);
        fiveButton = findViewById(R.id.button_five);
        fiveButton.setOnClickListener(this);
        sixButton = findViewById(R.id.button_six);
        sixButton.setOnClickListener(this);
        threeButton = findViewById(R.id.button_three);
        threeButton.setOnClickListener(this);
        sevenButton = findViewById(R.id.button_seven);
        sevenButton.setOnClickListener(this);
        findViewById(R.id.refresh_imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGuPiaoData();
            }
        });
        if (ContextCompat.checkSelfPermission(StartActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(StartActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        } else {
            getHistoryData();
        }
    }

    private void getGuPiaoData() {
        progressDialog = new MaterialDialog.Builder(StartActivity.this).title("分析进度")
                .progress(false, 100, true)
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .show();
        new Thread() {
            @Override
            public void run() {
                try {
                    analysisData.runAnalysisData();
                    handler.sendMessage(new Message());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            getHistoryData();
        } else {
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

    private void getHistoryData() {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            File file = new File(getApplication().getFilesDir().getAbsolutePath(), "/history.txt");
            if(!file.exists()){
                return;
            }

            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            ResultData resultData = (ResultData) ois.readObject();
            analysisData.setCuiziList(resultData.getCuiziList());
            analysisData.setLineList(resultData.getLineList());
            analysisData.setLongtouList(resultData.getLongtouList());
            analysisData.setMinNumberList(resultData.getMinNumberList());
            analysisData.setStarList(resultData.getStarList());
            analysisData.setZhongjiList(resultData.getZhongjiList());
            analysisData.setXiaoboList(resultData.getXiaoboList());

            oneButton.setText(oneButton.getText().toString() + "(" + analysisData.getCuiziList().size() + ")");
            twoButton.setText(twoButton.getText().toString() + "(" + analysisData.getZhongjiList().size() + ")");
            threeButton.setText(threeButton.getText().toString() + "(" + analysisData.getLongtouList().size() + ")");
            fourButton.setText(fourButton.getText().toString() + "(" + analysisData.getLineList().size() + ")");
            fiveButton.setText(fiveButton.getText().toString() + "(" + analysisData.getMinNumberList().size() + ")");
            sixButton.setText(sixButton.getText().toString() + "(" + analysisData.getStarList().size() + ")");
            sevenButton.setText(sevenButton.getText().toString() + "(" + analysisData.getXiaoboList().size() + ")");
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
            oneButton.setText(oneButton.getText().toString() + "(" + analysisData.getCuiziList().size() + ")");
            twoButton.setText(twoButton.getText().toString() + "(" + analysisData.getZhongjiList().size() + ")");
            threeButton.setText(threeButton.getText().toString() + "(" + analysisData.getLongtouList().size() + ")");
            fourButton.setText(fourButton.getText().toString() + "(" + analysisData.getLineList().size() + ")");
            fiveButton.setText(fiveButton.getText().toString() + "(" + analysisData.getMinNumberList().size() + ")");
            sixButton.setText(sixButton.getText().toString() + "(" + analysisData.getStarList().size() + ")");
            sevenButton.setText(sevenButton.getText().toString() + "(" + analysisData.getXiaoboList().size() + ")");

            ResultData resultData = new ResultData();
            resultData.setCuiziList(analysisData.getCuiziList());
            resultData.setLineList(analysisData.getLineList());
            resultData.setLongtouList(analysisData.getLongtouList());
            resultData.setMinNumberList(analysisData.getMinNumberList());
            resultData.setStarList(analysisData.getStarList());
            resultData.setZhongjiList(analysisData.getZhongjiList());
            resultData.setXiaoboList(analysisData.getXiaoboList());
            FileOutputStream fos = null;
            ObjectOutputStream os = null;
            try {
                File file = new File(getApplication().getFilesDir().getAbsolutePath(), "/history.txt");
                if(!file.exists()){
                    file.createNewFile();
                }
                fos = new FileOutputStream(file);
                os = new ObjectOutputStream(fos);
                os.writeObject(resultData);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_one: {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.putExtra("title", "倒锤子形态");
                intent.putParcelableArrayListExtra("list", analysisData.getCuiziList());
                StartActivity.this.startActivity(intent);
            }
            break;
            case R.id.button_two: {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.putExtra("title", "上涨中继");
                intent.putParcelableArrayListExtra("list", analysisData.getZhongjiList());
                StartActivity.this.startActivity(intent);
            }
            break;
            case R.id.button_four: {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.putExtra("title", "三线合一");
                intent.putParcelableArrayListExtra("list", analysisData.getLineList());
                StartActivity.this.startActivity(intent);
            }
            break;
            case R.id.button_five: {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.putExtra("title", "量能最低");
                intent.putParcelableArrayListExtra("list", analysisData.getMinNumberList());
                StartActivity.this.startActivity(intent);
            }
            break;
            case R.id.button_six: {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.putExtra("title", "小十字星");
                intent.putParcelableArrayListExtra("list", analysisData.getStarList());
                StartActivity.this.startActivity(intent);
            }
            break;
            case R.id.button_three: {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.putExtra("title", "龙头牛股");
                intent.putParcelableArrayListExtra("list", analysisData.getLongtouList());
                StartActivity.this.startActivity(intent);
            }
            break;
            case R.id.button_seven:
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.putExtra("title", "小波动股");
                intent.putParcelableArrayListExtra("list", analysisData.getXiaoboList());
                StartActivity.this.startActivity(intent);
                break;
        }
    }
}
