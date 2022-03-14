package com.example.matchandride;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

//import com.example.matchandride.ui.home.RideFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RecordRideInvActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button endBtn;
    private MapView recordMap;
    private GoogleMap gMap;
    private TextView txtDis, txtCurSpd, txtClimb, txtAvgSpd;
    private Chronometer chrTimer;
    private Button pauseRes;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private ArrayList<LatLng> routePoints;
    private double lastAltitude = -10000;
    private double totalClimb = 0;
    private LatLng lastLat, currentLat;
    private double totalDistance = 0;
    private long timeStopped;
    public static FirebaseAuth mAuth;
    public static FirebaseFirestore mStore;
    public static DatabaseReference mDbLoc;
    public static DatabaseReference mDbInv;
    public static DatabaseReference mDbAcc;
    public static DatabaseReference mDbGrp;
    //public static DatabaseReference mDbInvited;
    private Marker marker;
    public double curSpeed;
    public boolean isPaused = false;
    private ArrayList<String> groupMembers;
    public static final String TAG = "TAG";
    private HashMap<String, Marker> nearbyUserMap = new HashMap<String, Marker>();
    private String senderUid;
    private boolean isOrganizer = false;
    private ValueEventListener groupDbLis;

    @SuppressLint("MissingPermission")
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_ride_inv);

        try { RecordRideActivity.rrAct.finish(); }catch (Exception e){e.printStackTrace();}

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mDbLoc = FirebaseDatabase.getInstance().getReference("rt-location");
        mDbInv = FirebaseDatabase.getInstance().getReference("rt-invitation");
        mDbAcc = FirebaseDatabase.getInstance().getReference("rt-accept");
        mDbGrp = FirebaseDatabase.getInstance().getReference("rt-groups");
        //mDbInvited = FirebaseDatabase.getInstance().getReference("rt-invited");

        groupMembers = new ArrayList<>();
        routePoints = new ArrayList<>();
        Bundle extra = this.getIntent().getExtras();
        senderUid = (String) extra.get("sender");
        isOrganizer = (boolean) extra.get("organizer");
        //groupMembers = (ArrayList<String>) extra.get("members");

        recordMap = (MapView) findViewById(R.id.map_record_inv);
        txtDis = (TextView) findViewById(R.id.txt_distance_inv);
        txtCurSpd = (TextView) findViewById(R.id.txt_cur_spd_inv);
        txtAvgSpd = (TextView) findViewById(R.id.txt_avg_spd_inv);
        txtClimb = (TextView) findViewById(R.id.txt_climb_inv);
        pauseRes = (Button) findViewById(R.id.btn_pause_inv);
        chrTimer = (Chronometer) findViewById(R.id.chr_time_inv);

        endBtn = (Button) findViewById(R.id.btn_end);
        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(RecordRideInvActivity.this, "Long Press to End Ride", Toast.LENGTH_SHORT).show();
            }
        });

        endBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onStop();
                Intent intent = new Intent(RecordRideInvActivity.this, SaveRideActivity.class);
                intent.putExtra("timeTotal", chrTimer.getText().toString());
                intent.putExtra("disTotal", txtDis.getText().toString());
                intent.putExtra("climbTotal", txtClimb.getText().toString());
                intent.putExtra("avgSpd", txtAvgSpd.getText().toString());
                intent.putParcelableArrayListExtra("routePoints", routePoints);
                // if event organizer terminates the ride, delete the group info
                if (isOrganizer) mDbGrp.child(mAuth.getCurrentUser().getUid()).removeValue();
                Map<String,Object> invStatus = new HashMap<String,Object>();
                invStatus.put("isInvd", false);
                mStore.collection("UserNames").document(mAuth.getCurrentUser().getUid()).update(invStatus);
                // set user himself not invited
                //mDbInvited.child(mAuth.getCurrentUser().getUid()).setValue(false);
                startActivity(intent);
                finish();
                return false;
            }
        });

        pauseRes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pauseRes.getText().toString().equals("Pause")){
                    onPause();
                    pauseRes.setText("Resume");
                    txtCurSpd.setText("--");
                }else if(pauseRes.getText().toString().equals("Resume")){
                    onResume();
                    pauseRes.setText("Pause");
                }
            }
        });

        recordMap.onCreate(savedInstanceState);
        recordMap.getMapAsync(this);

        getGroupMembers();
        setLocationListeners();
        setNearbyUsers();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 200, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 0, locationListener);


    }

    public void setLocationListeners(){

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
        else{
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    currentLat = new LatLng(latitude, longitude);
                    // get the speed
                    if (location.hasSpeed()){
                        txtCurSpd.setText(String.format("%.2f", (location.getSpeed())*3.6));
                        curSpeed = (location.getSpeed())*3.6;
                    }
                    if (location != null){
                        System.out.println(latitude + " " + longitude);
                        // calculate the climb
                        if(location.hasAltitude()){
                            double altitude = location.getAltitude(); //get current altitude
                            if (lastAltitude == -10000){ // initial value
                                lastAltitude = altitude;
                            }else {
                                if (altitude > lastAltitude){  // if current altitude is higher than last location point
                                    double currentClimb = altitude - lastAltitude;            // calculate the climb
                                    totalClimb = currentClimb + totalClimb;                   // add current climb to total climb
                                    txtClimb.setText(String.format("%.0f",totalClimb) + "m"); // update the climb field
                                }
                                lastAltitude = altitude; // update last altitude
                            }
                        }
                        // calculate the distance
                        if (lastLat != null){
                            float[] distanceResult = new float[1]; // distance result will stored in this array
                            Location.distanceBetween(lastLat.latitude, lastLat.longitude, latitude, longitude, distanceResult);
                            totalDistance = distanceResult[0] + totalDistance; // update the total distance
                            if (totalDistance<1000) txtDis.setText(String.format("%.0f", totalDistance)+"m"); // display unit is meter if total<1km
                            else txtDis.setText(String.format("%.2f", totalDistance/1000)+"km");
                            lastLat = currentLat; // last LatLng is the current one
                            int timePassed = convertTimeToSecs(chrTimer.getText().toString());
                            double avgSpeed = (double) ((totalDistance/timePassed)*3.6);
                            txtAvgSpd.setText(String.format("%.2f", avgSpeed));
                        }else {
                            lastLat = currentLat;
                            chrTimer.start(); // the first location point is found, start the timer
                        }
                        // draw the route, route is consist of lots of LatLng points
                        routePoints.add(currentLat);
                        Polyline route = gMap.addPolyline(new PolylineOptions());
                        route.setPoints(routePoints);
                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                // set the center of the map to user's location
                                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                .zoom(17)             // Sets the zoom
                                .build();             // Creates a CameraPosition from the builder
                        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        if (marker != null) marker.remove();
                        marker = gMap.addMarker(new MarkerOptions().position(currentLat).title("Me").snippet("20kph 5/5")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_384380_16)));
                        mDbLoc.child(mAuth.getUid()).setValue(currentLat);

                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
        }

    }

    public int convertTimeToSecs(String time){
        String[] splitTime = time.split(":");
        int finaltime = 0;
        if (splitTime.length == 1)
            finaltime = Integer.valueOf(splitTime[0]);
        else if (splitTime.length == 2)
            finaltime = 60*Integer.valueOf(splitTime[0]) + Integer.valueOf(splitTime[1]);
        else if (splitTime.length == 3)
            finaltime = 60*60*Integer.valueOf(splitTime[0]) + 60*Integer.valueOf(splitTime[1]) + Integer.valueOf(splitTime[2]);
        System.out.println(finaltime);
        return finaltime;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        isPaused = false;
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 200, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 0, locationListener);
        chrTimer.setBase(SystemClock.elapsedRealtime() + timeStopped);
        chrTimer.start();
        recordMap.onResume();
        super.onResume();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onPause() {
        isPaused = true;
        locationManager.removeUpdates(locationListener);
        lastAltitude = -10000; // do not calculate the altitude rised during pause period
        lastLat = null;        // do not calculate the distance travelled during pause period
        chrTimer.stop();       // stop the timer
        timeStopped = chrTimer.getBase() - SystemClock.elapsedRealtime();
        super.onPause();
        recordMap.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recordMap.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        recordMap.onLowMemory();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStop() {
        locationManager.removeUpdates(locationListener);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        return;
    }

    public void setNearbyUsers(){

            System.out.println("Start finding nearby users...");
            //this.rtDbIsSet = true;
            MainActivity.mDbLoc.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!groupMembers.isEmpty()) {
                        try {
                            mDbGrp.removeEventListener(groupDbLis);
                        }catch (Exception e){e.printStackTrace();}
                    }
                    if (currentLat!=null){
                        for (DataSnapshot sp : snapshot.getChildren()){ // for each user in the database
                            if (!sp.getKey().equals(MainActivity.mAuth.getCurrentUser().getUid()) && groupMembers.contains(sp.getKey())){ // not user himself, and the user is in group
                                System.out.println("User " + sp.getKey() + " found!");
                                LatLng userLoc = new LatLng(Float.valueOf(sp.child("latitude").getValue().toString()), Float.valueOf(sp.child("longitude").getValue().toString()));
                                String nearUserUid = sp.getKey();
                                float[] distanceResult = new float[1]; // distance result will stored in this array
                                Location.distanceBetween(currentLat.latitude, currentLat.longitude, userLoc.latitude, userLoc.longitude, distanceResult);
                                // show users within the distance of 10km, and the user is newly found
                                if (distanceResult[0] <= 5000 && !nearbyUserMap.containsKey(nearUserUid)){
                                    System.out.println("User " + sp.getKey() + " is in 10km");
                                    // get current nearby user information
                                    System.out.println("get current nearby user information");
                                    DocumentReference dRef = MainActivity.mStore.collection("UserNames").document(nearUserUid);
                                    final String[] nearUserInfo = new String[1];
                                    ArrayList<String> testarr = new ArrayList<>();
                                    dRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    nearUserInfo[0] = document.getString("Username");
                                                    System.out.println(nearUserInfo[0]);
                                                    /* GET&SET MORE INFO HERE, AVG SPEED, RATING, .... */
                                                    testarr.add(nearUserInfo[0]);

                                                    if (nearbyUserMap.containsKey(nearUserUid))
                                                        nearbyUserMap.get(nearUserUid).setPosition(userLoc);
                                                    else{
                                                        Marker userMk = gMap.addMarker(new MarkerOptions()
                                                                .position(userLoc)
                                                                .title(nearUserInfo[0])
                                                                .snippet("20kph, 5/5")
                                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_red_16)));
                                                        nearbyUserMap.put(nearUserUid, userMk);
                                                    }

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
                                // an existing user within the range 10km
                                else if (distanceResult[0] <= 5000 && nearbyUserMap.containsKey(nearUserUid)){
                                    System.out.println("Have found this guy, update his location");
                                    Marker userMk = nearbyUserMap.get(nearUserUid);
                                    userMk.setPosition(userLoc);
                                }
                                // an existing user goes out of the range, remove it from map
                                else if (distanceResult[0] > 5000 && nearbyUserMap.containsKey(nearUserUid)){
                                    System.out.println("A user travels out of range");
                                    nearbyUserMap.get(nearUserUid).remove();
                                    nearbyUserMap.remove(nearUserUid);
                                }
                            }
                        }
                        // check offline users in nearby user map, remove them
                        ArrayList<String> offLineUser = new ArrayList<>();
                        for (String existingUser : nearbyUserMap.keySet()){
                            boolean isOffline = true;
                            for (DataSnapshot sp : snapshot.getChildren()){
                                if (sp.getKey().equals(existingUser)) isOffline = false;
                            }
                            if (isOffline) offLineUser.add(existingUser);
                        }
                        if (!offLineUser.isEmpty()) System.out.println("find offline user, removing it...");
                        for (String offlineuser : offLineUser) {
                            nearbyUserMap.get(offlineuser).remove();
                            nearbyUserMap.remove(offlineuser);
                        }
                        offLineUser.clear();
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

    }

    public void getGroupMembers(){
        groupDbLis = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot sp : snapshot.getChildren()){
                    if(sp.getKey().equals(senderUid)){
                        String memberStr = sp.getValue().toString();
                        String[] membersSplit = memberStr.split(",");
                        for (String m : membersSplit) groupMembers.add(m);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mDbGrp.addValueEventListener(groupDbLis);
    }


}
