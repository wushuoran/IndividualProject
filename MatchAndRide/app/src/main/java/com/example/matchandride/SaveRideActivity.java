package com.example.matchandride;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.matchandride.tools.RideObject;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SaveRideActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView timeTotal, disTotal, climbTotal, avgSpd, discardRide;
    private Button btnSave;
    private MapView traceMap;
    private String timeMills, distance, climb, avgspd;
    private ArrayList<LatLng> routePoints;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private FirebaseStorage mStra;
    private StorageReference straRef;
    public static final String TAG = "TAG";
    private boolean isGroup = false;
    private ArrayList<String> groupMembers;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.finish_save_ride);
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStra = FirebaseStorage.getInstance();
        straRef = mStra.getReference();

        routePoints = new ArrayList<>();
        groupMembers = new ArrayList<>();

        getRideData();

        System.out.println(timeMills);
        System.out.println(distance);
        timeTotal = (TextView) findViewById(R.id.txt_time_final);
        disTotal = (TextView) findViewById(R.id.txt_distance_final);
        climbTotal = (TextView) findViewById(R.id.txt_climb_final);
        avgSpd = (TextView) findViewById(R.id.txt_avg_spd_final);
        discardRide = (TextView) findViewById(R.id.txt_discard);
        btnSave = (Button) findViewById(R.id.btn_save);
        traceMap = (MapView) findViewById(R.id.map_record_final);

        timeTotal.setText(timeMills);
        disTotal.setText(distance);
        climbTotal.setText(climb);
        avgSpd.setText(avgspd);
        discardRide.setPaintFlags(discardRide.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

        traceMap.onCreate(savedInstanceState);
        traceMap.getMapAsync(this);

        setListeners();


    }

    public void setListeners(){

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveRide();
                MainActivity.goToOtherAct = false;
                if (!isGroup){
                    startActivity(new Intent(SaveRideActivity.this, MainActivity.class));
                    finish();
                }else{
                    Intent intent = new Intent(SaveRideActivity.this, RateUsersActivity.class);
                    intent.putExtra("groupMembers", groupMembers);
                    startActivity(intent);
                    finish();
                }
            }
        });

        discardRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SaveRideActivity.this);
                builder.setMessage("Are you sure to discard this ride?")
                        .setTitle("Confirm").setCancelable(true);
                builder.setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (!isGroup){
                            MainActivity.goToOtherAct = false;
                            startActivity(new Intent(SaveRideActivity.this, MainActivity.class));
                            finish();
                        }else{
                            Intent intent = new Intent(SaveRideActivity.this, RateUsersActivity.class);
                            intent.putExtra("groupMembers", groupMembers);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    public void getRideData(){
        Bundle extras = this.getIntent().getExtras();
        this.timeMills = (String) extras.get("timeTotal");
        this.distance = (String) extras.get("disTotal");
        this.climb = (String) extras.get("climbTotal");
        this.avgspd = (String) extras.get("avgSpd");
        this.routePoints = this.getIntent().getParcelableArrayListExtra("routePoints");
        this.isGroup = (boolean) extras.get("isGroup");
        if (isGroup) groupMembers = (ArrayList<String>) extras.get("groupMembers");
    }

    public void saveRide(){
        SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        Date date1 = new Date();
        String dateTime = formatter1.format(date1);
        SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy");
        Date date2 = new Date();
        String date = formatter2.format(date2);
        Map<String, Object> rideInfo = new HashMap<>();
        rideInfo.put("Duration", timeMills);
        rideInfo.put("Distance", distance);
        rideInfo.put("Climb", climb);
        rideInfo.put("AVGspd", avgspd);
        rideInfo.put("Timestamp", new Timestamp(0,0).now());
        if (mAuth.getCurrentUser() != null){ // user has logged in, ride should be stored at Firebase
            // store ride basic info into Firebase
            mStore.collection("Rides-" + mAuth.getCurrentUser().getUid())
                    .document(dateTime).set(rideInfo)
                    .addOnCompleteListener((OnCompleteListener<Void>) (aVoid) -> {
                        Log.d(TAG, "DocumentSnapshot added");
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error adding document", e);
                }
            });

            // store ride path file into Firebase
            try{
                String filename = mAuth.getCurrentUser().getUid()+"_"+dateTime+".csv"; // file name
                File file = new File(this.getBaseContext().getFilesDir(), filename);   // should write to internal storage first
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);
                if (!routePoints.isEmpty()){
                    for (LatLng point : routePoints){
                        bw.write(point.latitude + "," + point.longitude);
                        bw.newLine();
                    }
                    bw.close();
                    fw.close();
                    StorageReference ref = straRef.child("UserRideHistory/" + filename);
                    Uri fromFile = Uri.fromFile(file);
                    ref.putFile(fromFile).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(SaveRideActivity.this, "Route Upload FAILED", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }catch (Exception e){e.printStackTrace();}
            // update the history avg speed
            try{updateHisAvgSpeed();}catch (Exception e){e.printStackTrace();}
            Toast.makeText(SaveRideActivity.this, "Ride Uploaded", Toast.LENGTH_SHORT).show();
        }else{ // user has not login, store the data in local storage
            File fileInfo = new File(getApplicationContext().getFilesDir(), dateTime + "info");
            File fileRoute = new File(getApplicationContext().getFilesDir(), dateTime + "route");
            try {
                FileWriter fw = new FileWriter(fileInfo);
                BufferedWriter bf = new BufferedWriter(fw);
                for (String entry : rideInfo.keySet()) {
                    bf.write(entry + ":" + rideInfo.get(entry));
                    System.out.println("Write " + entry + ":" + rideInfo.get(entry));
                    bf.newLine();
                }
                //bf.flush();
                bf.close();
                fw.close();
            } catch (IOException e) { e.printStackTrace(); }
            try{
                FileWriter fw = new FileWriter(fileRoute);
                BufferedWriter bw = new BufferedWriter(fw);
                if (!routePoints.isEmpty()){
                    for (LatLng point : routePoints){
                        bw.write(point.latitude + "," + point.longitude);
                        bw.newLine();
                    }
                    bw.close();
                    fw.close();
                }
            }catch (Exception e){e.printStackTrace();}
            Toast.makeText(SaveRideActivity.this, "Ride Saved to Local Storage", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //gMap = googleMap;
        Polyline route = googleMap.addPolyline(new PolylineOptions());
        if (!routePoints.isEmpty()){
            route.setPoints(routePoints);
            // zoom the map to some suitable ratio (to fit the whole path)
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (LatLng latLngPoint : routePoints)
                boundsBuilder.include(latLngPoint);
            int routePadding = 100;
            LatLngBounds latLngBounds = boundsBuilder.build();
            try {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));
            }catch (Exception e){e.printStackTrace();}
        }
    }

    public void updateHisAvgSpeed(){

        String collectionName = "Rides-" + mAuth.getCurrentUser().getUid();

        mStore.collection(collectionName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    ArrayList<Double> allAVGspd = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()){
                        RideObject ro = document.toObject(RideObject.class);
                        double avgspd = 0;
                        try {
                            avgspd = ro.getAVGspd();
                        }catch (Exception e){e.printStackTrace();}
                        //System.out.println("AVG spd got hahahahah" + avgspd);
                        allAVGspd.add(avgspd);
                        if (!allAVGspd.isEmpty()){
                            double sum = 0;
                            for (Double d : allAVGspd) sum = sum + d;
                            Double hisAVGspd = (double) Math.round((sum / allAVGspd.size()) * 10) / 10 ;
                            //System.out.println("history avg spd calculated jajajajaja" + hisAVGspd);
                            Map<String, Object> updateInfo = new HashMap<String, Object>();
                            updateInfo.put("AVGspd", hisAVGspd);
                            mStore.collection("UserNames").document(mAuth.getCurrentUser().getUid()).update(updateInfo);
                            //System.out.println("his avg spd updated wahoooooo");
                        }
                    }
                }else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });


    }

    @Override
    public void onResume() {
        traceMap.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        traceMap.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        traceMap.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        traceMap.onLowMemory();
    }

}