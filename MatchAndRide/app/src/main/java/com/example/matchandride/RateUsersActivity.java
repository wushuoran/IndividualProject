package com.example.matchandride;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.matchandride.tools.RateUsersAdapter;

import java.util.ArrayList;

public class RateUsersActivity  extends AppCompatActivity {

    private Button submit;
    private ListView listView;
    private ArrayList<String> groupMembers;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_users);

        Bundle extras = this.getIntent().getExtras();
        groupMembers = new ArrayList<>();
        groupMembers = (ArrayList<String>) extras.get("groupMembers");

        listView = (ListView) findViewById(R.id.lv_rate);
        listView.setAdapter(new RateUsersAdapter(RateUsersActivity.this, groupMembers));

        submit = (Button) findViewById(R.id.btn_submit_rating);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RateUsersActivity.this, MainActivity.class));
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        return;
    }

}
