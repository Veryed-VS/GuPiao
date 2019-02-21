package com.xjm.gupiao;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import com.afollestad.materialdialogs.MaterialDialog;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import java.io.IOException;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    private MaterialDialog progressDialog;
    private AnalysisData analysisData;
    private Button oneButton;
    private Button twoButton;
    private Button fourButton;
    private Button fiveButton;
    private Button sixButton;

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

        getGuPiaoData();
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
                    analysisData = new AnalysisData();
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

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            oneButton.setText(oneButton.getText().toString() + "(" + analysisData.getCuiziList().size() + ")");
            twoButton.setText(twoButton.getText().toString() + "(" + analysisData.getZhongjiList().size() + ")");
            fourButton.setText(fourButton.getText().toString() + "(" + analysisData.getLineList().size() + ")");
            fiveButton.setText(fiveButton.getText().toString() + "(" + analysisData.getMinNumberList().size() + ")");
            sixButton.setText(sixButton.getText().toString() + "(" + analysisData.getStarList().size() + ")");
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
            case R.id.button_six: {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.putExtra("title", "小十字星");
                intent.putParcelableArrayListExtra("list", analysisData.getStarList());
                StartActivity.this.startActivity(intent);
            }
            break;
        }
    }
}
