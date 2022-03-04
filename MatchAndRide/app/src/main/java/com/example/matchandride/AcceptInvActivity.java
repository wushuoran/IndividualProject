package com.example.matchandride;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AcceptInvActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView rideInfo, addInfo;
    private Button refuseBtn, acceptBtn;
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

        getInvInfo();
        childname = mAuth.getCurrentUser().getUid() + ":" + senderUid;
        invChildname = senderUid + ":" + mAuth.getCurrentUser().getUid();

        MyCountDown timer = new MyCountDown(61000, 1000); //11s for testing

        rideInfo = (TextView) findViewById(R.id.tv_inv_inviter_info);
        setRideInfo();
        addInfo = (TextView) findViewById(R.id.tv_inv_ride_note);
        try{addInfo.setText(addNote);}catch(NullPointerException e){e.printStackTrace();}
        refuseBtn = (Button) findViewById(R.id.btn_refuse_inv);
        refuseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDbAcc.child(childname).setValue(false);
                mDbInv.child(invChildname).removeValue();
                timer.operationMade = true;
                startActivity(new Intent(AcceptInvActivity.this, MainActivity.class));
                finish();
            }
        });
        acceptBtn = (Button) findViewById(R.id.btn_accept_inv);
        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDbAcc.child(childname).setValue(true);
                mDbInv.child(invChildname).removeValue();
                timer.operationMade = true;
                Intent intent = new Intent(AcceptInvActivity.this, RecordRideInvActivity.class);
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
                                        + "\nAVG Speed: 20Kph"
                                        + "\nRating: 5/5"
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
        Bundle extras = this.getIntent().getExtras();
        this.senderUid = (String) extras.get("senderUid");
        this.meetPlace = (LatLng) extras.get("meetPlace");
        this.estParti = (int) extras.get("estParti");
        try{
            this.addNote = (String) extras.get("addNotes");
        }catch(Exception e){e.printStackTrace();}

    }

    @Override
    public void onBackPressed() {
        return;
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
