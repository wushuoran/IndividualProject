package com.example.matchandride;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecordRideActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView recordMap;
    private GoogleMap gMap;
    private TextView txtDis, txtCurSpd, txtClimb, txtAvgSpd;
    private Chronometer chrTimer;
    private Button pauseRes, endRide;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private List<LatLng> routePoints;
    private double lastAltitude = -10000;
    private double totalClimb = 0;
    private LatLng lastLat;
    private double totalDistance = 0;
    private long timeStopped;

    /*
    * 还不能保存ride 不知道如何保存轨迹
    * sprint2 要共享位置
    * 结束之后跳转总结页并让用户决定是否保存
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_ride);

        recordMap = (MapView) findViewById(R.id.map_record);
        txtDis = (TextView) findViewById(R.id.txt_distance);
        txtCurSpd = (TextView) findViewById(R.id.txt_cur_spd);
        txtAvgSpd = (TextView) findViewById(R.id.txt_avg_spd);
        txtClimb = (TextView) findViewById(R.id.txt_climb);
        txtAvgSpd = (TextView) findViewById(R.id.txt_avg_spd);
        pauseRes = (Button) findViewById(R.id.btn_pau_con);
        endRide = (Button) findViewById(R.id.btn_end_ride);
        chrTimer = (Chronometer) findViewById(R.id.chr_time);

        recordMap.onCreate(savedInstanceState);
        recordMap.getMapAsync(this);

        routePoints = new ArrayList<LatLng>();
        txtClimb.setText("0");

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
                onStop();
                finish();
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
                        txtCurSpd.setText(String.format("%.2f", location.getSpeed()));
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
                            long timePassed = chrTimer.getBase();
                            double timePassedToHrs = (double) (timePassed/3600000);
                            double avgSpeed = (double) ((totalDistance/1000)/timePassedToHrs);
                            txtAvgSpd.setText(String.format("%.2f", avgSpeed));
                        }else {
                            lastLat = currentLat;
                            chrTimer.start(); // the first location point is found, start the timer
                        }
                        // draw the route, route is consist of lots of LatLng points
                        routePoints.add(currentLat);
                        Polyline route = gMap.addPolyline(new PolylineOptions());
                        route.setPoints(routePoints);
                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(location.getLatitude(), location.getLongitude()), 13));
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                // set the center of the map to user's location
                                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                .zoom(17)             // Sets the zoom
                                .build();             // Creates a CameraPosition from the builder
                        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
    }

    @Override
    public void onResume() {
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 200, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 0, locationListener);
        chrTimer.setBase(SystemClock.elapsedRealtime() + timeStopped);
        chrTimer.start();
        recordMap.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        locationManager.removeUpdates(locationListener);
        chrTimer.stop();
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

    @Override
    protected void onStop() {
        locationManager.removeUpdates(locationListener);
        super.onStop();
    }

}
/*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    // get the speed
                    if (location.hasSpeed()){
                        txtCurSpd.setText(String.format("%.2f", location.getSpeed()));
                    }
                    if (location != null){
                        System.out.println(latitude + " " + longitude);
                        routePoints.add(new LatLng(latitude, longitude));
                        Polyline route = gMap.addPolyline(new PolylineOptions());
                        route.setPoints(routePoints);
                        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(location.getLatitude(), location.getLongitude()), 13));
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                                .zoom(17)                   // Sets the zoom
                                .build();                   // Creates a CameraPosition from the builder
                        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 200, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 0, locationListener);
        }
    }
*/