package com.example.matchandride;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class RecordRideActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView recordMap;
    private GoogleMap gMap;
    private TextView txtTime, txtDis, txtCurSpd, txtClimb, txtAvgSpd;
    private Button pauseRes, endRide;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_ride);

        recordMap = (MapView) findViewById(R.id.map_record);
        txtTime = (TextView) findViewById(R.id.txt_time);
        txtDis = (TextView) findViewById(R.id.txt_distance);
        txtCurSpd = (TextView) findViewById(R.id.txt_cur_spd);
        txtAvgSpd = (TextView) findViewById(R.id.txt_avg_spd);
        txtClimb = (TextView) findViewById(R.id.txt_climb);
        pauseRes = (Button) findViewById(R.id.btn_pau_con);
        endRide = (Button) findViewById(R.id.btn_end_ride);

        recordMap.onCreate(savedInstanceState);
        recordMap.getMapAsync(this);

        chkPermissionAndInitLocalListener();

    }

    public void chkPermissionAndInitLocalListener(){

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
                    // get the speed
                    if (location.hasSpeed()){
                        txtCurSpd.setText(String.format("%.2f", location.getSpeed()));
                    }
                    //get the location name from latitude and longitude
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        String result = addresses.get(0).getLocality()+":";
                        result += addresses.get(0).getCountryName();
                        LatLng latLng = new LatLng(latitude, longitude);
                        if (marker != null){
                            marker.remove();
                            marker = gMap.addMarker(new MarkerOptions().position(latLng).title(result));
                            gMap.setMaxZoomPreference(20);
                            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
                        }
                        else{
                            marker = gMap.addMarker(new MarkerOptions().position(latLng).title(result));
                            gMap.setMaxZoomPreference(20);
                            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
                        }
                    } catch (Exception e) { e.printStackTrace(); }
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
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 200, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 1, locationListener);
        }

    }

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
                    //get the location name from latitude and longitude
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addresses =
                                geocoder.getFromLocation(latitude, longitude, 1);
                        String result = addresses.get(0).getLocality()+":";
                        result += addresses.get(0).getCountryName();
                        LatLng latLng = new LatLng(latitude, longitude);
                        if (marker != null){
                            marker.remove();
                            marker = gMap.addMarker(new MarkerOptions().position(latLng).title(result));
                            gMap.setMaxZoomPreference(20);
                            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
                        }
                        else{
                            marker = gMap.addMarker(new MarkerOptions().position(latLng).title(result));
                            gMap.setMaxZoomPreference(20);
                            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 21.0f));
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
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
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 200, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 1, locationListener);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
    }

    @Override
    public void onResume() {
        recordMap.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
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
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }


}
