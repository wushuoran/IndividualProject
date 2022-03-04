package com.example.matchandride;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class RecordRideInvActivity extends AppCompatActivity {

    private Button endBtn;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_ride_inv);

        endBtn = (Button) findViewById(R.id.btn_end);
        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecordRideInvActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

}
