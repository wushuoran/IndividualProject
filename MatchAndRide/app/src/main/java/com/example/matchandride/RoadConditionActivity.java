package com.example.matchandride;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.matchandride.tools.RoadConditionAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoadConditionActivity extends AppCompatActivity {

    private Button reportCon;
    private ListView lvRoads;
    public static Activity roadCondsAct;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private FirebaseStorage mStra;
    private StorageReference straRef;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_road_cond);

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStra = FirebaseStorage.getInstance();
        straRef = mStra.getReference();

        roadCondsAct = this;

        reportCon = (Button) findViewById(R.id.btn_report_road);
        reportCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RoadConditionActivity.this, ReportRoadConActivity.class));
            }
        });

        lvRoads = (ListView) findViewById(R.id.lv_road_conds);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        @SuppressLint("MissingPermission") android.location.Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        double userLat = lastKnownLocation.getLatitude();
        double userLong = lastKnownLocation.getLongitude();
        LatLng myLoc = new LatLng(userLat,userLong);

        System.out.println("Finding road issues near " + myLoc);

        mStore.collection("RoadConditions").orderBy("CreateTime", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            HashMap<Integer, String> issueLocs = new HashMap<>();
                            HashMap<Integer, String> issuePhos = new HashMap<>();
                            int nearIssueCount = 0;
                            for (QueryDocumentSnapshot document : task.getResult()){
                                GeoPoint geoPoint = document.getGeoPoint("Location");
                                double lat = geoPoint.getLatitude();
                                double lng = geoPoint.getLongitude();
                                LatLng issueLoc = new LatLng(lat, lng);
                                String issuePic = document.get("PhotoName").toString();
                                if (issueLoc!=null){
                                    float[] distanceResult = new float[1]; // distance result will stored in this array
                                    Location.distanceBetween(issueLoc.latitude, issueLoc.longitude, myLoc.latitude, myLoc.longitude, distanceResult);
                                    if (distanceResult[0]<5000) {
                                        issueLocs.put(nearIssueCount, document.getId());
                                        issuePhos.put(nearIssueCount, issuePic);
                                        nearIssueCount++;
                                    }
                                }
                            }
                            lvRoads.setAdapter(new RoadConditionAdapter(RoadConditionActivity.this, issueLocs, issuePhos));
                        }
                    }
        });

    }

}
