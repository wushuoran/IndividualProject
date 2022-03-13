package com.example.matchandride;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.util.ArrayList;

/*
* About rides in internal storage (local) and Firebase (cloud):
* Once logged in, user can manually upload the local rides to cloud
* Once local ride is uploaded to cloud, the local files will be deleted.
* */

public class ManageRideActivity extends AppCompatActivity {

    private LinearLayout rideItems;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private static final String TAG = "TAG";
    private String curUserId;
    private LinearLayout.MarginLayoutParams params;
    private ArrayList<Button> rideBtns,rideBtnsLocal;
    public static Activity thisAct;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_MyApplicationNoBar);
        setContentView(R.layout.activity_manage_ride);
        thisAct = this;

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        rideBtns = new ArrayList<Button>();
        rideBtnsLocal = new ArrayList<>();

        rideItems = (LinearLayout) findViewById(R.id.sv_items_layout);
        params = (ViewGroup.MarginLayoutParams) rideItems.getLayoutParams();

        try{curUserId = mAuth.getCurrentUser().getUid();}catch (Exception e){}
        if (curUserId != null) getDataFirebase();
        else getDataLocal();

    }

    public void getDataLocal(){

        File files = getFilesDir();
        File[] fileList = files.listFiles();
        int localRideCount = 0;
        for (File file : fileList) if (file.getName().contains("info")) localRideCount++;
        if (mAuth.getCurrentUser() == null && localRideCount > 0){
            TextView tv = new TextView(getApplicationContext());
            tv.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            tv.setHeight((int) ((int) 105*(getApplicationContext().getResources().getDisplayMetrics().density) + 0.5f));
            params.topMargin = (int) ((int) 5*(getApplicationContext().getResources().getDisplayMetrics().density) + 0.5f);
            tv.setLayoutParams(params);
            tv.setTextColor(Color.DKGRAY);
            tv.setTextSize(15);
            tv.setGravity(Gravity.CENTER);
            tv.setText("Below are the rides in local storage" +
                    "\nThese rides will be lost when using new device" +
                    "\nWe advise uploading the rides by login");
            rideItems.addView(tv);
        }
        for (File file : fileList){
            if (file.getName().contains("info")){
                String curFile = file.getName();
                Button rideDate = new Button(getApplicationContext());
                rideDate.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                rideDate.setHeight((int) ((int) 65*(getApplicationContext().getResources().getDisplayMetrics().density) + 0.5f));
                params.topMargin = (int) ((int) 5*(getApplicationContext().getResources().getDisplayMetrics().density) + 0.5f);
                rideDate.setLayoutParams(params);
                rideDate.setText(curFile.replaceAll("info", ""));
                rideDate.setTextSize(20);
                rideDate.setGravity(Gravity.CENTER);
                rideDate.setBackground(getResources().getDrawable(R.drawable.chart_bound1));
                rideItems.addView(rideDate);
                rideBtnsLocal.add(rideDate); // add to buttons arraylist
            }
            setButtonListeners();
        }
    }

    public void getDataFirebase(){

        File files = getFilesDir();
        File[] fileList = files.listFiles();
        int localRideCount = 0;
        for (File file : fileList) if (file.getName().contains("info")) localRideCount++;
        if (localRideCount > 0){
            System.out.println("Local rides detected.");
            TextView tv = new TextView(getApplicationContext());
            tv.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            tv.setHeight((int) ((int) 105*(getApplicationContext().getResources().getDisplayMetrics().density) + 0.5f));
            params.topMargin = (int) ((int) 5*(getApplicationContext().getResources().getDisplayMetrics().density) + 0.5f);
            tv.setLayoutParams(params);
            tv.setTextColor(Color.DKGRAY);
            tv.setTextSize(15);
            tv.setGravity(Gravity.CENTER);
            tv.setText("Below are the ride(s) stored in local storage " +
                    "\n Upload to cloud in ride details page " +
                    "\n Uploaded rides can be viewed on any new device " +
                    "\n Local file will be deleted once uploaded");
            rideItems.addView(tv);
            getDataLocal();
        }
        mStore.collection("Rides-" + curUserId).orderBy("Timestamp", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    System.out.println("Get ride data collection, rides count: " + task.getResult().getDocuments().size());
                    TextView tv = new TextView(getApplicationContext());
                    tv.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                    tv.setHeight((int) ((int) 45*(getApplicationContext().getResources().getDisplayMetrics().density) + 0.5f));
                    params.topMargin = (int) ((int) 5*(getApplicationContext().getResources().getDisplayMetrics().density) + 0.5f);
                    tv.setLayoutParams(params);
                    tv.setTextColor(Color.DKGRAY);
                    tv.setTextSize(15);
                    tv.setGravity(Gravity.CENTER);
                    tv.setText("Below are the uploaded rides");
                    rideItems.addView(tv);
                    // for each ride, create a button to view details of it
                    for (QueryDocumentSnapshot document : task.getResult()) { // reverse order
                        //DocumentSnapshot document = doc.;
                        Button rideDate = new Button(getApplicationContext());
                        rideDate.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                        rideDate.setHeight((int) ((int) 65*(getApplicationContext().getResources().getDisplayMetrics().density) + 0.5f));
                        params.topMargin = (int) ((int) 5*(getApplicationContext().getResources().getDisplayMetrics().density) + 0.5f);
                        rideDate.setLayoutParams(params);
                        rideDate.setText(document.getId());
                        rideDate.setTextSize(20);
                        rideDate.setGravity(Gravity.CENTER);
                        rideDate.setBackground(getResources().getDrawable(R.drawable.chart_bound1));
                        rideItems.addView(rideDate);
                        rideBtns.add(rideDate); // add to buttons arraylist
                    }
                    // once finished creating buttons, set listeners to those buttons
                    setButtonListeners();
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

    }

    public void setButtonListeners(){

        for (Button btn : rideBtns){
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ManageRideActivity.this, ViewRideActivity.class);
                    intent.putExtra("dateTime", btn.getText().toString());
                    intent.putExtra("isLocal", "cloud");
                    System.out.println(btn.getText().toString());
                    startActivity(intent);
                }
            });
        }

        for (Button btn : rideBtnsLocal){
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ManageRideActivity.this, ViewRideActivity.class);
                    intent.putExtra("dateTime", btn.getText().toString());
                    intent.putExtra("isLocal", "local");
                    System.out.println(btn.getText().toString());
                    startActivity(intent);
                }
            });
        }

    }


}
