package com.xjm.gupiao;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        findViewById(R.id.button_one).setOnClickListener(this);
        findViewById(R.id.button_two).setOnClickListener(this);
        findViewById(R.id.button_three).setOnClickListener(this);
        findViewById(R.id.button_four).setOnClickListener(this);
        findViewById(R.id.button_five).setOnClickListener(this);
        if (ContextCompat.checkSelfPermission(StartActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(StartActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_one: {
                Intent intent = new Intent(StartActivity.this, CuiZiActivity.class);
                StartActivity.this.startActivity(intent);
            }
            break;
            case R.id.button_two: {
                Intent intent = new Intent(StartActivity.this, ShiZiActivity.class);
                StartActivity.this.startActivity(intent);
            }
            break;
            case R.id.button_three: {
                Intent intent = new Intent(StartActivity.this, LineActivity.class);
                StartActivity.this.startActivity(intent);
            }
            break;
            case R.id.button_four: {
                Intent intent = new Intent(StartActivity.this, LongTouActivity.class);
                StartActivity.this.startActivity(intent);
            }
            break;
            case R.id.button_five: {
                Intent intent = new Intent(StartActivity.this, YangActivity.class);
                StartActivity.this.startActivity(intent);
            }
            break;
        }
    }
}
