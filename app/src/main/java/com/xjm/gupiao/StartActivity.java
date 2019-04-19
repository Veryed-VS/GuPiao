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

public class StartActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        findViewById(R.id.one_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent allSharesIntent = new Intent(StartActivity.this,AllSharesActivity.class);
                StartActivity.this.startActivity(allSharesIntent);
            }
        });
        findViewById(R.id.two_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent starIntent = new Intent(StartActivity.this, BoDuanActivity.class);
                StartActivity.this.startActivity(starIntent);
            }
        });

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
}
