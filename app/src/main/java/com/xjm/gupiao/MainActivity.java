package com.xjm.gupiao;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        TextView titleTxt = findViewById(R.id.title_textView);
        ArrayList<AllSharesBean> sharesBeans = intent.getParcelableArrayListExtra("list");

        String titleStr = intent.getStringExtra("title");
        titleTxt.setText(titleStr);

        ListView listView = findViewById(R.id.list_view);
        SharesAdapter adapter = new SharesAdapter(sharesBeans, MainActivity.this);
        listView.setAdapter(adapter);

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.finish();
            }
        });
    }
}