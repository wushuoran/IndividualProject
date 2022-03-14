package com.example.matchandride;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AcceptInvActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView rideInfo, addInfo;
    public static Button acceptBtn;
    public static Button refuseBtn;
    private MapView meetMap;
    private String senderUid, addNote, childname, invChildname;
    private int estParti;
    private LatLng meetPlace;
    public static final String TAG = "TAG";
    public static FirebaseAuth mAuth;
    public static FirebaseFirestore mStore;
    public static FirebaseStorage mStra;
    public static StorageReference straRef;
    public static DatabaseReference mDbLoc;
    public static DatabaseReference mDbInv;
    public static DatabaseReference mDbAcc;
    public static DatabaseReference mDbGrp;
    private String lastAct;
    private Vibrator vib;
    private Bundle extras;
    private String timeMills, distance, climb, avgspd;
    private ArrayList<LatLng> routePoints;

    @SuppressLint("MissingPermission")
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_inv);
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStra = FirebaseStorage.getInstance();
        straRef = mStra.getReference();
        mDbLoc = FirebaseDatabase.getInstance().getReference("rt-location");
        mDbInv = FirebaseDatabase.getInstance().getReference("rt-invitation");
        mDbAcc = FirebaseDatabase.getInstance().getReference("rt-accept");
        mDbGrp = FirebaseDatabase.getInstance().getReference("rt-groups");

        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        getInvInfo();
        childname = mAuth.getCurrentUser().getUid() + ":" + senderUid;
        invChildname = senderUid + ":" + mAuth.getCurrentUser().getUid();

        if (lastAct.equals("main")){
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(20000, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                vib.vibrate(20000);
            }
        }


        MyCountDown timer = new MyCountDown(61000, 1000); //11s for testing

        rideInfo = (TextView) findViewById(R.id.tv_inv_inviter_info);
        setRideInfo();
        addInfo = (TextView) findViewById(R.id.tv_inv_ride_note);
        try{addInfo.setText(addNote);}catch(NullPointerException e){e.printStackTrace();}
        refuseBtn = (Button) findViewById(R.id.btn_refuse_inv);
        refuseBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                mDbAcc.child(childname).setValue(false);
                mDbInv.child(invChildname).removeValue();
                timer.operationMade = true;
                vib.cancel();
                finish();
            }
        });
        acceptBtn = (Button) findViewById(R.id.btn_accept_inv);
        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                mDbAcc.child(childname).setValue(true);
                mDbInv.child(invChildname).removeValue();
                timer.operationMade = true;
                vib.cancel();
                if(lastAct.equals("record")){
                    MainActivity.goToOtherAct = false;
                    RecordRideActivity.mDbInv.removeEventListener(RecordRideActivity.pubLis);
                    System.out.println("listener in recording activity removed!");
                    saveRide();
                }
                Map<String,Object> invStatus = new HashMap<String,Object>();
                invStatus.put("isInvd", true);
                mStore.collection("UserNames").document(mAuth.getCurrentUser().getUid()).update(invStatus);
                Intent intent = new Intent(AcceptInvActivity.this, RecordRideInvActivity.class);
                intent.putExtra("sender", senderUid);
                intent.putExtra("organizer",false);
                startActivity(intent);
                finish();
            }
        });
        meetMap = (MapView) findViewById(R.id.map_meet_up_reci);

        meetMap.onCreate(savedInstanceState);
        meetMap.getMapAsync(this);

    }

    public void setRideInfo(){
        DocumentReference dRef = mStore.collection("UserNames").document(senderUid);
        final String[] nearUserInfo = new String[3];
        dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        nearUserInfo[0] = document.getString("Username");
                        rideInfo.setText("Inviter: " + nearUserInfo[0]
                                        + "\nAVG Speed: " + document.get("AVGspd").toString() + "kph"
                                        + "\nRating: " + document.get("Rating").toString() + "/5"
                                        + "\nEst. Participants: " + estParti);
                        Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        @SuppressLint("MissingPermission") android.location.Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        double userLat=0, userLong=0;
        try{
            userLat = lastKnownLocation.getLatitude();
            userLong = lastKnownLocation.getLongitude();
            googleMap.setMyLocationEnabled(true);
        }catch (NullPointerException e){e.printStackTrace();}
        if (meetPlace!=null){
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(meetPlace, 13));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    // set the center of the map to user's location
                    .target(meetPlace)
                    .zoom(13)             // Sets the zoom
                    .build();             // Creates a CameraPosition from the builder
            MarkerOptions mkop = new MarkerOptions().position(meetPlace).title("Distance");
            try{
                float[] distanceResult = new float[1]; // distance result will stored in this array
                Location.distanceBetween(meetPlace.latitude, meetPlace.longitude, userLat, userLong, distanceResult);
                mkop.snippet((Double.valueOf(distanceResult[0])).toString());
            }catch (Exception e){e.printStackTrace();}
            googleMap.addMarker(mkop);
        }

    }

    @Override
    public void onResume() {
        meetMap.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        meetMap.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        meetMap.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        meetMap.onLowMemory();
    }

    public void getInvInfo(){
        extras = this.getIntent().getExtras();
        this.senderUid = (String) extras.get("senderUid");
        this.meetPlace = (LatLng) extras.get("meetPlace");
        this.estParti = (int) extras.get("estParti");
        this.lastAct = (String) extras.get("curAct");
        try{
            this.addNote = (String) extras.get("addNotes");
        }catch(Exception e){e.printStackTrace();}

    }

    @Override
    public void onBackPressed() {
        return;
    }

    public void saveRide(){
        this.timeMills = (String) extras.get("timeTotal");
        this.distance = (String) extras.get("disTotal");
        this.climb = (String) extras.get("climbTotal");
        this.avgspd = (String) extras.get("avgSpd");
        this.routePoints = this.getIntent().getParcelableArrayListExtra("routePoints");

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
                        Toast.makeText(AcceptInvActivity.this, "Route Upload FAILED", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }catch (Exception e){e.printStackTrace();}
        Toast.makeText(AcceptInvActivity.this, "Ride Uploaded", Toast.LENGTH_SHORT).show();

    }

    private class MyCountDown extends CountDownTimer {

        int secs;
        boolean operationMade = false;

        public MyCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            start();
        }

        @Override
        public void onFinish() {
            secs = 10;
            if (!operationMade){
                mDbAcc.child(childname).setValue(false);
                mDbInv.child(invChildname).removeValue();
                AcceptInvActivity.this.finish();
            }
        }

        @Override
        public void onTick(long duration) {
            secs = secs - 1;
        }
    }
}
