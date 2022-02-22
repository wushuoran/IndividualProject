package com.example.matchandride.ui.dashboard;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.matchandride.LoginActivity;
import com.example.matchandride.MainActivity;
import com.example.matchandride.R;
import com.example.matchandride.databinding.FragmentDiscoverBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class DiscoverFragment extends Fragment implements OnMapReadyCallback{

    private DiscoverViewModel discoverViewModel;
    private FragmentDiscoverBinding binding;
    private Switch bikeFilter;
    private Button addList, sendInv;
    private MapView nearbyMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private Marker mylocationMarker;
    private GoogleMap googleMap;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        discoverViewModel =
                new ViewModelProvider(this).get(DiscoverViewModel.class);

        binding = FragmentDiscoverBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        /*        final TextView textView = binding.textDashboard;
        discoverViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        bikeFilter = (Switch) root.findViewById(R.id.switch_filter);
        nearbyMap = (MapView) root.findViewById(R.id.map_nearby);
        addList = (Button) root.findViewById(R.id.btn_add_to_list);
        sendInv = (Button) root.findViewById(R.id.btn_send_inv);


        nearbyMap.onCreate(savedInstanceState);
        nearbyMap.getMapAsync(this);

        setListeners();

        return root;
    }

    public void setListeners(){

        addList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.mAuth.getCurrentUser() == null)
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                else {

                }
            }
        });

        sendInv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.mAuth.getCurrentUser() == null)
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                else {

                }
            }
        });

    }

    public void setMap(){

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
        else if(ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            this.googleMap.setTrafficEnabled(true);
            this.googleMap.setIndoorEnabled(true);
            this.googleMap.setMyLocationEnabled(true);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng currentLat = new LatLng(latitude, longitude);
                    if (mylocationMarker!=null) mylocationMarker.remove();
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            // set the center of the map to user's location
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))
                            .zoom(15)             // Sets the zoom
                            .build();             // Creates a CameraPosition from the builder
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    mylocationMarker = googleMap.addMarker(
                            new MarkerOptions()
                                    .position(currentLat)
                                    .title("Me")
                                    .snippet("20kph 5/5")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_384380_16)));

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        setMap();
    }

    @Override
    public void onResume() {
        nearbyMap.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        nearbyMap.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        nearbyMap.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        nearbyMap.onLowMemory();
    }

}