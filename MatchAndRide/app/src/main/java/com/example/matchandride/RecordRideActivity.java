package com.example.matchandride;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RecordRideActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView recordMap;
    private GoogleMap gMap;
    private TextView txtDis, txtCurSpd, txtClimb, txtAvgSpd;
    private Chronometer chrTimer;
    private Button pauseRes, endRide;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private ArrayList<LatLng> routePoints;
    private double lastAltitude = -10000;
    private double totalClimb = 0;
    private LatLng lastLat;
    private double totalDistance = 0;
    private long timeStopped;
    public static FirebaseAuth mAuth;
    public static DatabaseReference mDbLoc;
    private Marker marker;
    private ValueEventListener invEvnLis;
    public static ValueEventListener pubLis;
    public static DatabaseReference mDbInv;
    public double curSpeed;
    public boolean isPaused = false;
    private Vibrator vib;
    private boolean isOnline;
    public static Activity rrAct;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_ride);

        mAuth = FirebaseAuth.getInstance();
        mDbLoc = FirebaseDatabase.getInstance().getReference("rt-location");
        mDbInv = FirebaseDatabase.getInstance().getReference("rt-invitation");

        rrAct = this;

        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Bundle extras = this.getIntent().getExtras();
        isOnline = (boolean) extras.get("isOnline");

        recordMap = (MapView) findViewById(R.id.map_record);
        txtDis = (TextView) findViewById(R.id.txt_distance);
        txtCurSpd = (TextView) findViewById(R.id.txt_cur_spd);
        txtAvgSpd = (TextView) findViewById(R.id.txt_avg_spd);
        txtClimb = (TextView) findViewById(R.id.txt_climb);
        pauseRes = (Button) findViewById(R.id.btn_pau_con);
        endRide = (Button) findViewById(R.id.btn_end_ride);
        chrTimer = (Chronometer) findViewById(R.id.chr_time);

        recordMap.onCreate(savedInstanceState);
        recordMap.getMapAsync(this);

        routePoints = new ArrayList<LatLng>();
        txtClimb.setText("0");

        listenToInv();

        setLocationListeners();

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 200, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 0, locationListener);

        setButtonListeners();

    }

    public void setButtonListeners(){

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

        endRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(RecordRideActivity.this, "Long Press to End Ride", Toast.LENGTH_SHORT).show();
            }
        });

        endRide.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onStop();
                Intent intent = new Intent(RecordRideActivity.this, SaveRideActivity.class);
                intent.putExtra("timeTotal", chrTimer.getText().toString());
                intent.putExtra("disTotal", txtDis.getText().toString());
                intent.putExtra("climbTotal", txtClimb.getText().toString());
                intent.putExtra("avgSpd", txtAvgSpd.getText().toString());
                intent.putParcelableArrayListExtra("routePoints", routePoints);
                intent.putExtra("isGroup", false);
                startActivity(intent);
                finish();
                return false;
            }
        });

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
                    LatLng currentLat = new LatLng(latitude, longitude);
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
                        if (isOnline){
                            if (marker != null) marker.remove();
                            marker = gMap.addMarker(new MarkerOptions().position(currentLat).title("Me").snippet("20kph 5/5")
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_384380_16)));
                        }else{
                            if (marker != null) marker.remove();
                            marker = gMap.addMarker(new MarkerOptions().position(currentLat).title("Me").snippet("20kph 5/5")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_grey_16)));
                        }
                        if (mAuth.getCurrentUser() != null && isOnline){
                            mDbLoc.child(mAuth.getUid()).setValue(currentLat);
                        }
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

    public void listenToInv() {
        if (MainActivity.onlineSwitchStatus && !(invEvnLis != null)) {
            invEvnLis = new ValueEventListener() {
                @SuppressLint("MissingPermission")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot sp : snapshot.getChildren()) {
                        String childname = sp.getKey();
                        String[] splitChild = childname.split(":");
                        String invSender = splitChild[0];
                        String invReci = splitChild[1];
                        if (invReci.equals(mAuth.getCurrentUser().getUid())) {
                            System.out.println("Invitation Detected!");
                            String invMsg = sp.getValue().toString();
                            String[] splitMsg = invMsg.split(",");
                            LatLng meetPlace = new LatLng(Double.valueOf(splitMsg[0]), Double.valueOf(splitMsg[1]));
                            int estParti = Integer.valueOf(splitMsg[2]);
                            AlertDialog.Builder builder = new AlertDialog.Builder(RecordRideActivity.this);
                            builder.setMessage("Find a safe place to STOP and view the invitation." +
                                    "\nOnce accepted, current ride will save automatically")
                                    .setTitle("New invitation received").setCancelable(true);
                            builder.setPositiveButton("View Invitation", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                            builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                    dialog.cancel();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            // Vibrate for 500 milliseconds
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vib.vibrate(VibrationEffect.createOneShot(20000, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                //deprecated in API 26
                                vib.vibrate(20000);
                            }
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (curSpeed>5 && !isPaused){
                                        Toast.makeText(RecordRideActivity.this, "Please STOP", Toast.LENGTH_LONG).show();
                                    }
                                    if (curSpeed<=5 || isPaused){ // if user stops or ride is paused, let user view the invitation
                                        try {
                                            Intent intent = new Intent(RecordRideActivity.this, AcceptInvActivity.class);
                                            intent.putExtra("meetPlace", meetPlace);
                                            intent.putExtra("senderUid", invSender);
                                            intent.putExtra("estParti", estParti);
                                            intent.putExtra("curAct", "record");
                                            if (splitMsg.length == 4) intent.putExtra("addNotes", splitMsg[3]);
                                            intent.putExtra("timeTotal", chrTimer.getText().toString());
                                            intent.putExtra("disTotal", txtDis.getText().toString());
                                            intent.putExtra("climbTotal", txtClimb.getText().toString());
                                            intent.putExtra("avgSpd", txtAvgSpd.getText().toString());
                                            intent.putParcelableArrayListExtra("routePoints", routePoints);
                                            vib.cancel();
                                            dialog.cancel();
                                            startActivity(intent);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        dialog.dismiss();
                                    }
                                }
                            });
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            pubLis = invEvnLis;
            mDbInv.addValueEventListener(invEvnLis);
            System.out.println("Accept RTDB Listener Set");
        }

    }


}