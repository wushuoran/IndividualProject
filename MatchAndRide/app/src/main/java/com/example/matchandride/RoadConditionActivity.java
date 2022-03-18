package com.example.matchandride;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class RoadConditionActivity extends AppCompatActivity {

    private Button reportCon;
    private ListView lvRoads;
    public static Activity roadCondsAct;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_road_cond);

        roadCondsAct = this;

        reportCon = (Button) findViewById(R.id.btn_report_road);
        reportCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RoadConditionActivity.this, ReportRoadConActivity.class));
            }
        });

        lvRoads = (ListView) findViewById(R.id.lv_road_conds);

    }

}
