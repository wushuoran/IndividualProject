package com.example.matchandride;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.okhttp.internal.DiskLruCache;

import java.util.ArrayList;
import java.util.HashMap;

public class SendInvActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView meetMap;
    private GoogleMap googleMap;
    private LinearLayout invitedUsers;
    private EditText rideNotes;
    private Button sendInv;
    private ArrayList<String> invUids;
    private ViewGroup.MarginLayoutParams params;
    public static FirebaseAuth mAuth;
    public static FirebaseFirestore mStore;
    public static FirebaseStorage mStra;
    public static StorageReference straRef;
    public static DatabaseReference mDbLoc;
    public static DatabaseReference mDbInv;
    public static DatabaseReference mDbAcc;
    private LatLng meetUpLoc;
    public static final String TAG = "TAG";
    private HashMap<String, Marker> nearbyUserMap = new HashMap<String, Marker>();
    private HashMap<String,TextView> invitedUserTV = new HashMap<>();

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_inv);

        invUids = new ArrayList<>();
        getInvData();

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStra = FirebaseStorage.getInstance();
        straRef = mStra.getReference();
        mDbLoc = FirebaseDatabase.getInstance().getReference("rt-location");
        mDbInv = FirebaseDatabase.getInstance().getReference("rt-invitation");
        mDbAcc = FirebaseDatabase.getInstance().getReference("rt-accept");

        meetMap = (MapView) findViewById(R.id.map_meet_up);
        invitedUsers = (LinearLayout) findViewById(R.id.sv_inv_list_layout);
        rideNotes = (EditText) findViewById(R.id.edit_ride_notes);
        sendInv = (Button) findViewById(R.id.btn_send_inv);

        params = (ViewGroup.MarginLayoutParams) invitedUsers.getLayoutParams();

        meetMap.onCreate(savedInstanceState);
        meetMap.getMapAsync(this);

        setListeners();

    }

    private void setListeners() {

        if (invUids.size()>0){
            for (String uid : invUids){
                TextView tv = new TextView(getApplicationContext());
                tv.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                tv.setHeight((int) ((int) 45*(getApplicationContext().getResources().getDisplayMetrics().density) + 0.5f));
                params.topMargin = (int) ((int) 5*(getApplicationContext().getResources().getDisplayMetrics().density) + 0.5f);
                tv.setLayoutParams(params);
                tv.setTextColor(Color.DKGRAY);
                tv.setTextSize(15);
                tv.setGravity(Gravity.CENTER);
                tv.setBackground(getResources().getDrawable(R.drawable.chart_bound1));
                setInvUserInfo(uid,tv);
                invitedUsers.addView(tv);
                invitedUserTV.put(uid,tv);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SendInvActivity.this);
                        builder.setMessage("Are you sure to remove this user?")
                                .setTitle("Confirm").setCancelable(true);
                        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                invitedUsers.removeView(tv);
                                invUids.remove(uid);
                                invitedUserTV.remove(uid);
                                try{
                                    nearbyUserMap.get(uid).remove();
                                    nearbyUserMap.remove(uid);
                                }catch(Exception e){e.printStackTrace();}
                                if (invUids.size()==0){
                                    cannotSetInv();
                                }
                            }
                        });
                        builder.setNegativeButton("Keep", new DialogInterface.OnClickListener() {
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
        }

        sendInv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (invUids.size()>0 && meetUpLoc != null)
                    sendInvitations();
                else if(invUids.size()>0 && meetUpLoc == null)
                    Toast.makeText(SendInvActivity.this,"Meet-up place not set", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(SendInvActivity.this,"Nobody in invite list", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void sendInvitations(){

        // FORMAT: lat,lng,participants,(notes)
        String invMsg = meetUpLoc.latitude + "," + meetUpLoc.longitude + "," + (invUids.size()+1) + "," + rideNotes.getText();

        for (String uid : invUids){
            String childname = mAuth.getCurrentUser().getUid() + ":" + uid;
            mDbInv.child(childname).setValue(invMsg);
        }
        Intent intent = new Intent(SendInvActivity.this, WaitAccActivity.class);
        intent.putExtra("invUids", invUids);
        startActivity(intent);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        @SuppressLint("MissingPermission") android.location.Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        double userLat = lastKnownLocation.getLatitude();
        double userLong = lastKnownLocation.getLongitude();
        LatLng myLoc = new LatLng(userLat,userLong);

        this.googleMap = googleMap;
        this.googleMap.setMyLocationEnabled(true);

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 13));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                // set the center of the map to user's location
                .target(myLoc)
                .zoom(13)             // Sets the zoom
                .build();             // Creates a CameraPosition from the builder
        System.out.println(myLoc);
        setNearbyMap(myLoc);

        this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Click here to pick this place");
                //googleMap.clear();
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                googleMap.addMarker(markerOptions);
            }
        });
        this.googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                LatLng setLoc = marker.getPosition();
                float[] distanceResult = new float[1]; // distance result will stored in this array
                Location.distanceBetween(setLoc.latitude, setLoc.longitude, userLat, userLong, distanceResult);
                if (distanceResult[0] <= 5000 || checkLocNearToUsers(setLoc)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(SendInvActivity.this);
                    builder.setMessage("Are you sure to pick this location? \n\nIt cannot be changed once invitation is sent.")
                            .setTitle("Set Meet-up Place").setCancelable(true);
                    builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            meetUpLoc = marker.getPosition();
                            System.out.println("Meet-up Location " + meetUpLoc + " is selected");
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
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(SendInvActivity.this);
                    builder.setMessage("The selected meet-up place is too far away, please choose another one.")
                            .setTitle("Cannot Set Place").setCancelable(true);
                    builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            dialog.cancel();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

    }

    public void setNearbyMap(LatLng currentLat) {
        mDbLoc.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot sp : snapshot.getChildren()) { // for each user in the database
                    if (!sp.getKey().equals(mAuth.getCurrentUser().getUid())
                        && invUids.contains(sp.getKey())) { // not user himself
                        System.out.println("User " + sp.getKey() + " found!");
                        LatLng userLoc = new LatLng(Float.valueOf(sp.child("latitude").getValue().toString()), Float.valueOf(sp.child("longitude").getValue().toString()));
                        String nearUserUid = sp.getKey();
                        float[] distanceResult = new float[1]; // distance result will stored in this array
                        Location.distanceBetween(currentLat.latitude, currentLat.longitude, userLoc.latitude, userLoc.longitude, distanceResult);
                        // show users within the distance of 10km, and the user is newly found
                        if (distanceResult[0] <= 5000 && !nearbyUserMap.containsKey(nearUserUid)) {
                            System.out.println("User " + sp.getKey() + " is in 10km");
                            // get current nearby user information
                            System.out.println("get current nearby user information");
                            if (nearbyUserMap.containsKey(nearUserUid))
                                nearbyUserMap.get(nearUserUid).setPosition(userLoc);
                            else {
                                Marker userMk = googleMap.addMarker(new MarkerOptions()
                                        .position(userLoc)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_red_16)));
                                nearbyUserMap.put(nearUserUid, userMk);
                            }
                        }
                        // an existing user within the range 10km
                        else if (distanceResult[0] <= 5000 && nearbyUserMap.containsKey(nearUserUid)) {
                            System.out.println("Have found this guy, update his location");
                            Marker userMk = nearbyUserMap.get(nearUserUid);
                            userMk.setPosition(userLoc);
                        }
                        // an existing user goes out of the range, remove it from map
                        else if (distanceResult[0] > 5000 && nearbyUserMap.containsKey(nearUserUid)) {
                            System.out.println("A user travels out of range");
                            nearbyUserMap.get(nearUserUid).remove();
                            nearbyUserMap.remove(nearUserUid);
                            Toast.makeText(getApplicationContext(), "One user travels out of nearby range", Toast.LENGTH_SHORT).show();
                            // if user travels out of range, delete the user from invite list
                            try{
                                invUids.remove(nearUserUid);
                                invitedUsers.removeView(invitedUserTV.get(nearUserUid));
                                invitedUserTV.remove(nearUserUid);
                                if (invUids.size()==0){
                                    cannotSetInv();
                                }
                            }catch (Exception e){e.printStackTrace();}

                        }
                    }
                }
                // check offline users in nearby user map, remove them
                ArrayList<String> offLineUser = new ArrayList<>();
                for (String existingUser : nearbyUserMap.keySet()) {
                    boolean isOffline = true;
                    for (DataSnapshot sp : snapshot.getChildren()) {
                        if (sp.getKey().equals(existingUser)) isOffline = false;
                    }
                    if (isOffline) offLineUser.add(existingUser);
                }
                if (!offLineUser.isEmpty()) System.out.println("find offline user, removing it...");
                for (String offlineuser : offLineUser) {
                    nearbyUserMap.get(offlineuser).remove();
                    nearbyUserMap.remove(offlineuser);
                    Toast.makeText(getApplicationContext(), "One user is offline", Toast.LENGTH_SHORT).show();
                    // if user travels out of range, delete the user from invite list
                    try{
                        invUids.remove(offlineuser);
                        invitedUsers.removeView(invitedUserTV.get(offlineuser));
                        invitedUserTV.remove(offlineuser);
                        if (invUids.size()==0){
                            cannotSetInv();
                        }
                    }catch (Exception e){e.printStackTrace();}
                }
                offLineUser.clear();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setInvUserInfo(String uid, TextView tv){
        DocumentReference dRef = mStore.collection("UserNames").document(uid);
        final String[] nearUserInfo = new String[3];
        dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        nearUserInfo[0] = document.getString("Username");
                        /* GET&SET MORE INFO HERE, AVG SPEED, RATING, .... */
                        tv.setText(nearUserInfo[0] + ", " + "25Km/h, 5/5");
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

    public void cannotSetInv(){
        AlertDialog.Builder builder = new AlertDialog.Builder(SendInvActivity.this);
        builder.setMessage("There is no one in the invite list now, please go back and select someone to invite.")
                .setTitle("Cannot Set Invitation").setCancelable(true);
        builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.cancel();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean checkLocNearToUsers(LatLng setLoc){
        boolean isNear = true;
        try{
            for (String uid : nearbyUserMap.keySet()){
                LatLng userLoc = nearbyUserMap.get(uid).getPosition();
                float[] distanceResult = new float[1]; // distance result will stored in this array
                Location.distanceBetween(setLoc.latitude, setLoc.longitude, userLoc.latitude, userLoc.longitude, distanceResult);
                if (distanceResult[0]>5000) isNear = false;
            }
        }catch(Exception e){e.printStackTrace();}

        return isNear;
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

    private void getInvData(){
        Bundle extras = this.getIntent().getExtras();
        this.invUids = (ArrayList<String>) extras.get("inviteList");
    }

}
